package com.a3.textprocessor.service;

import com.a3.textprocessor.Grafo;
import com.a3.textprocessor.InputRead;
import com.a3.textprocessor.model.GrafoResponse;
import com.a3.textprocessor.model.Link;
import com.a3.textprocessor.model.Node;
import com.a3.textprocessor.sdk.Aresta;
import com.a3.textprocessor.sdk.Artigo;
import com.a3.textprocessor.sdk.Frase;
import com.a3.textprocessor.sdk.Vertice;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GrafoService {

    public GrafoResponse generateGraph(String fileName) {
        //Leitura dos arquivos
        Artigo artigo = InputRead.readArtigo("./src/main/resources/resumos", fileName, StandardCharsets.UTF_8);
        if (artigo == null) {
            throw new RuntimeException("Artigo com nome " + fileName + " não encontrado.");
        }
        System.out.println("Processando artigo: " + artigo.getTitulo());

        // Criar grafo
        Grafo grafo = criarGrafo(artigo);

        // Kruskal
        removerArestasDesnecessarias(grafo);

        // Criar resposta
        return createResponse(grafo);
    }

    public GrafoResponse generateTopWordsGraph(String fileName, int numberOfWords) {
        Artigo artigo = InputRead.readArtigo("./src/main/resources/resumos", fileName, StandardCharsets.UTF_8);
        if (artigo == null) {
            throw new RuntimeException("Artigo com nome " + fileName + " não encontrado.");
        }
        System.out.println("Processando artigo: " + artigo.getTitulo());

        // Top words
        Grafo novoGrafo = createGrafoTopWords(artigo, numberOfWords);

        return createResponse(novoGrafo);
    }

    public String[] listFiles() {
        return InputRead.listFiles(new File("./src/main/resources/resumos"));
    }

    // O(n*m*o) = O(n^3)
    public GrafoResponse generateCoautoriaGraph() {
        List<Artigo> artigos = InputRead.run("./src/main/resources/resumos", StandardCharsets.UTF_8);

        // Coautoria
        Grafo novoGrafo = new Grafo(new ArrayList<>(), new HashMap<>());
        for (Artigo artigo : artigos) {
            Grafo grafoArtigo = createGrafoTopWords(artigo, 3); // n

            for (String autor : artigo.getAutores()) {
                Vertice verticeAutor = getAndPutNode(novoGrafo.getVertices(), autor, 1); // n * m

                for (Map.Entry<String, Vertice> entry : grafoArtigo.getVertices().entrySet()) { // n * m * o
                    String topico = entry.getKey();
                    Vertice verticeTopico = getAndPutNode(novoGrafo.getVertices(), topico, 2);

                    Aresta aresta = new Aresta(verticeAutor, verticeTopico, 1);
                    novoGrafo.getListaDeAdjacencia().add(aresta);
                }
            }
        }
        return createResponse(novoGrafo);
    }

    private static Vertice getAndPutNode(Map<String, Vertice> vertices, String word, int groupId) {
        Vertice vertice = vertices.get(word);
        if(vertice == null) {
            vertice = new Vertice(word, 1);
            vertices.put(word, vertice);
            vertice.setOcorrencias(groupId);
        }
        return vertice;
    }

    // O(n)
    private static Grafo createGrafoTopWords(Artigo artigo, int numberOfWords) {
        Grafo grafo = criarGrafo(artigo);
        removerArestasDesnecessarias(grafo);

        Queue<Aresta> arestasPorPeso = new PriorityQueue<>((a, b) -> {
            int peso1 = a.getPeso() + a.getVertice1().getOcorrencias() + a.getVertice2().getOcorrencias();
            int peso2 = b.getPeso() + b.getVertice1().getOcorrencias() + b.getVertice2().getOcorrencias();
            return peso2 - peso1;
        });
        arestasPorPeso.addAll(grafo.getListaDeAdjacencia());

        Grafo novoGrafo = new Grafo(new ArrayList<>(), new HashMap<>());

        numberOfWords = Math.min(grafo.getVertices().size(), numberOfWords);
        while(novoGrafo.getVertices().size() < numberOfWords - 1) { // n - 1
            Aresta aresta = arestasPorPeso.poll();

            novoGrafo.getVertices().put(aresta.getVertice1().getPalavra(), aresta.getVertice1());
            novoGrafo.getVertices().put(aresta.getVertice2().getPalavra(), aresta.getVertice2());
            novoGrafo.getListaDeAdjacencia().add(aresta);
        }
        while(novoGrafo.getVertices().size() < numberOfWords) { // n
            Aresta aresta = arestasPorPeso.poll();
            if(aresta.getVertice1().getOcorrencias() > aresta.getVertice2().getOcorrencias()) {
                novoGrafo.getVertices().put(aresta.getVertice1().getPalavra(), aresta.getVertice1());
            } else {
                novoGrafo.getVertices().put(aresta.getVertice2().getPalavra(), aresta.getVertice2());
            }
        }
        return novoGrafo;
    }

    // O(n*m) = O(n^2)
    public GrafoResponse generateSimilaridadeTextosGraph() {

        List<Artigo> artigos = InputRead.run("./src/main/resources/resumos", StandardCharsets.UTF_8);

        // Similaridade de textos
        Grafo novoGrafo = new Grafo(new ArrayList<>(), new HashMap<>());
        Map<String, Set<String>> top3WordByArtigo = new HashMap<>(artigos.size());
        for (Artigo artigo : artigos) { // n
            Grafo grafoArtigo = createGrafoTopWords(artigo, 3);

            Vertice vertice = new Vertice(artigo.getFileName(), 2);
            novoGrafo.getVertices().put(artigo.getFileName(), vertice);

            Set<String> top3PalavrasAtual = grafoArtigo.getVertices().keySet();
            for (Map.Entry<String, Set<String>> entry : top3WordByArtigo.entrySet()) { // m
                int count = 0;
                Set<String> top3PalavrasExistente = entry.getValue();
                for (String atual : top3PalavrasAtual) { //  m * 3
                    if(top3PalavrasExistente.contains(atual)) {
                        count++;
                    }
                }

                if(count > 0) {
                    Vertice verticeExistente = novoGrafo.getVertices().get(entry.getKey());
                    Aresta aresta = new Aresta(vertice, verticeExistente, count);
                    novoGrafo.getListaDeAdjacencia().add(aresta);
                }
            }
            top3WordByArtigo.put(artigo.getFileName(), top3PalavrasAtual);
        }

        return createResponse(novoGrafo);
    }

    static class Subset {

        Vertice parent;
        int rank;

        public Subset(Vertice parent, int rank) {
            this.parent = parent;
            this.rank = rank;
        }
    }
    /**
     * Corre o algoritmo de kruskal no grafo para criar uma árvore geradora máxima e com isso remover arestas desnecessárias.
     *
     * @param grafo o grafo.
     */
    // O(n * log(m))
    private static void removerArestasDesnecessarias(Grafo grafo) {
        Map<String, Vertice> vertices = grafo.getVertices();

        List<Aresta> novaLista = new ArrayList<>(vertices.size());
        Map<String, Subset> subsets = new HashMap<>();

        for (Map.Entry<String, Vertice> entry : vertices.entrySet()) {
            subsets.put(entry.getKey(), new Subset(entry.getValue(), 0)); // n
        }

        PriorityQueue<Aresta> listaDePrioridades = new PriorityQueue<>((a, b) -> b.getPeso() - a.getPeso());
        listaDePrioridades.addAll(grafo.getListaDeAdjacencia());

        while (novaLista.size() < vertices.size() - 1) { // n
            if(listaDePrioridades.isEmpty()) {
                throw new RuntimeException("O grafo não é válido, nem todos os vértices estão ligados.");
            }
            Aresta aresta = listaDePrioridades.poll();

            Vertice x = findParent(subsets, aresta.getVertice1()); // log (m)
            Vertice y = findParent(subsets, aresta.getVertice2()); // log (m)

            if (!x.equals(y)) {
                novaLista.add(aresta);
                union(subsets, x, y);
            }
        }
        grafo.setListaDeAdjacencia(novaLista);
    }

    private static void union(Map<String, Subset> subsets, Vertice x, Vertice y) {
        Vertice rootX = findParent(subsets, x);
        Vertice rootY = findParent(subsets, y);

        if (subsets.get(rootY.getPalavra()).rank < subsets.get(rootX.getPalavra()).rank) {
            subsets.get(rootY.getPalavra()).parent = rootX;
        } else if (subsets.get(rootX.getPalavra()).rank < subsets.get(rootY.getPalavra()).rank) {
            subsets.get(rootX.getPalavra()).parent = rootY;
        } else {
            subsets.get(rootY.getPalavra()).parent = rootX;
            subsets.get(rootX.getPalavra()).rank++;
        }
    }

    private static Vertice findParent(Map<String, Subset> subsets, Vertice vertice) {
        if (subsets.get(vertice.getPalavra()).parent.equals(vertice)) return subsets.get(vertice.getPalavra()).parent;

        subsets.get(vertice.getPalavra()).parent = findParent(subsets, subsets.get(vertice.getPalavra()).parent);
        return subsets.get(vertice.getPalavra()).parent;
    }

    // O(n * m * (m - 1)) = O(n * m^2) = O(n^2)
    private static Grafo criarGrafo(Artigo artigo) {
        Map<String, Vertice> vertices = new HashMap<>();
        Map<Integer, Aresta> arestasPorHashCode = new HashMap<>();

        // Para garantir que a criação de um grafo válido foi necessário criar este vértice neutro e ligar a
        // primeira palavra de cada frase a este vértice. Isto porque podem existir frases que só tenham palavras que não aparecem
        // em nenhum outro momento do texto, o que faz com que a frase não tenha ligação nenhuma.
        Vertice verticeNeutro = new Vertice("###VERTICE_NEUTRO###", 0);
        vertices.put(verticeNeutro.getPalavra(), verticeNeutro);

        for (int i = 0; i < artigo.getFrasesFiltradas().size(); i++) { // n
            Frase frase = artigo.getFrasesFiltradas().get(i);
            if (frase.getListaPalavras().isEmpty() || frase.getListaPalavras().size() == 1) continue;

            String primeiraPalavra = frase.getListaPalavras().get(0);
            Vertice verticePrimeiraPalavra = new Vertice(primeiraPalavra, 0);

            Aresta arestaFrase = new Aresta(verticePrimeiraPalavra, verticeNeutro, 0);
            arestasPorHashCode.put(arestaFrase.hashCode(), arestaFrase);

            for (int j = 0; j < frase.getListaPalavras().size(); j++) { // n * m
                String palavra1 = frase.getListaPalavras().get(j);
                Vertice vertice1 = vertices.get(palavra1);
                if (vertice1 == null) {
                    vertice1 = new Vertice(palavra1, 1);
                    vertices.put(palavra1, vertice1);
                } else {
                    vertice1.setOcorrencias(vertice1.getOcorrencias() + 1);
                }

                for (int k = j + 1; k < frase.getListaPalavras().size(); k++) { // n * m * (m - 1)
                    String palavra2 = frase.getListaPalavras().get(k);

                    if (palavra2.equals(palavra1)) {
                        continue;
                    }

                    Vertice vertice2 = vertices.get(palavra2);
                    if (vertice2 == null) {
                        vertice2 = new Vertice(palavra2, 0);
                        vertices.put(palavra2, vertice2);
                    }

                    Aresta novaAresta = new Aresta(vertice1, vertice2, 1);
                    Aresta arestaExistente = arestasPorHashCode.get(novaAresta.hashCode());
                    if (arestaExistente == null) {
                        arestasPorHashCode.put(novaAresta.hashCode(), novaAresta);
                    } else {
                        arestaExistente.setPeso(arestaExistente.getPeso() + 1);
                    }
                }
            }
        }

        Grafo grafo = new Grafo();
        grafo.setListaDeAdjacencia(new ArrayList<>(arestasPorHashCode.values()));
        grafo.setVertices(vertices);
        return grafo;
    }

    // O(n + m) = O(n)
    private static GrafoResponse createResponse(Grafo grafo) {
        GrafoResponse response = new GrafoResponse(new ArrayList<>(), new ArrayList<>());
        for (Map.Entry<String, Vertice> entry : grafo.getVertices().entrySet()) { // n
            Node nodeObject = new Node();
            nodeObject.setId(entry.getKey());
            nodeObject.setGroup(entry.getValue().getOcorrencias());
            response.getNodes().add(nodeObject);
        }

        for (Aresta aresta : grafo.getListaDeAdjacencia()) { // m
            Link linkObject = new Link();
            linkObject.setSource(aresta.getVertice1().getPalavra());
            linkObject.setTarget(aresta.getVertice2().getPalavra());
            linkObject.setValue(aresta.getPeso());
            response.getLinks().add(linkObject);
        }
        return response;
    }
}

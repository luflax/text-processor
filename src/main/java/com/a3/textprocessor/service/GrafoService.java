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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GrafoService {

    public GrafoResponse generateGraph(String fileName) {
        Artigo artigo = InputRead.readArtigo("./src/main/resources/resumos", fileName, Charset.forName("Cp1252"));
        if (artigo == null) {
            throw new RuntimeException("Artigo com nome " + fileName + " não encontrado.");
        }
        System.out.println("Processando artigo: " + artigo.getTitulo());

        Grafo grafo = criarGrafo(artigo);
        removerArestasDesnecessarias(grafo);

        return createResponse(grafo);
    }

    public GrafoResponse generateTopWordsGraph(String fileName, int numberOfWords) {
        Artigo artigo = InputRead.readArtigo("./src/main/resources/resumos", fileName, Charset.forName("Cp1252"));
        if (artigo == null) {
            throw new RuntimeException("Artigo com nome " + fileName + " não encontrado.");
        }
        System.out.println("Processando artigo: " + artigo.getTitulo());

        Grafo novoGrafo = createGrafoTopWords(artigo, numberOfWords);

        return createResponse(novoGrafo);
    }

    public GrafoResponse generateCoautoriaGraph() {
        List<Artigo> artigos = InputRead.run("./src/main/resources/coautoria", StandardCharsets.UTF_8);

        Grafo novoGrafo = new Grafo(new ArrayList<>(), new HashMap<>());
        for (Artigo artigo : artigos) {

            Grafo grafoArtigo = createGrafoTopWords(artigo, 3);

            for (String autor : artigo.getAutores()) {
                Vertice verticeAutor = getOrPutNode(novoGrafo.getVertices(), autor, 1);

                for (Map.Entry<String, Vertice> entry : grafoArtigo.getVertices().entrySet()) {
                    String topico = entry.getKey();
                    Vertice verticeTopico = getOrPutNode(novoGrafo.getVertices(), topico, 2);

                    Aresta aresta = new Aresta(verticeAutor, verticeTopico, 1);
                    novoGrafo.getListaDeAdjacencia().add(aresta);
                }
            }
        }
        return createResponse(novoGrafo);
    }

    private static Vertice getOrPutNode(Map<String, Vertice> vertices, String word, int groupId) {
        Vertice vertice = vertices.get(word);
        if(vertice == null) {
            vertice = new Vertice(word, 1);
            vertices.put(word, vertice);
            vertice.setOcorrencias(groupId);
        }
        return vertice;
    }

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
        while(novoGrafo.getVertices().size() < numberOfWords - 1) {
            Aresta aresta = arestasPorPeso.poll();

            novoGrafo.getVertices().put(aresta.getVertice1().getPalavra(), aresta.getVertice1());
            novoGrafo.getVertices().put(aresta.getVertice2().getPalavra(), aresta.getVertice2());
            novoGrafo.getListaDeAdjacencia().add(aresta);
        }
        while(novoGrafo.getVertices().size() < numberOfWords) {
            Aresta aresta = arestasPorPeso.poll();
            if(aresta.getVertice1().getOcorrencias() > aresta.getVertice2().getOcorrencias()) {
                novoGrafo.getVertices().put(aresta.getVertice1().getPalavra(), aresta.getVertice1());
            } else {
                novoGrafo.getVertices().put(aresta.getVertice2().getPalavra(), aresta.getVertice2());
            }
        }
        return novoGrafo;
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
    private static void removerArestasDesnecessarias(Grafo grafo) {
        Map<String, Vertice> vertices = grafo.getVertices();

        List<Aresta> novaLista = new ArrayList<>(vertices.size());
        Map<String, Subset> subsets = new HashMap<>();

        for (Map.Entry<String, Vertice> entry : vertices.entrySet()) {
            subsets.put(entry.getKey(), new Subset(entry.getValue(), 0));
        }

        PriorityQueue<Aresta> listaDePrioridades = new PriorityQueue<>((a, b) -> b.getPeso() - a.getPeso());
        listaDePrioridades.addAll(grafo.getListaDeAdjacencia());

        while (novaLista.size() < vertices.size() - 1) {
            if(listaDePrioridades.isEmpty()) {
                throw new RuntimeException("O grafo não é válido, nem todos os vértices estão ligados.");
            }
            Aresta aresta = listaDePrioridades.poll();

            Vertice x = findParent(subsets, aresta.getVertice1());
            Vertice y = findParent(subsets, aresta.getVertice2());

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

    private static Grafo criarGrafo(Artigo artigo) {
        Map<String, Vertice> vertices = new HashMap<>();
        Map<Integer, Aresta> arestasPorHashCode = new HashMap<>();

        // Para garantir que a criação de um grafo válido foi necessário criar este vértice neutro e ligar a
        // primeira palavra de cada frase a este vértice. Isto porque podem existir frases que só tenham palavras que não aparecem
        // em nenhum outro momento do texto, o que faz com que a frase não tenha ligação nenhuma.
        Vertice verticeNeutro = new Vertice("###VERTICE_NEUTRO###", 0);
        vertices.put(verticeNeutro.getPalavra(), verticeNeutro);

        for (int i = 0; i < artigo.getFrasesFiltradas().size(); i++) {
            Frase frase = artigo.getFrasesFiltradas().get(i);
            if (frase.getListaPalavras().isEmpty() || frase.getListaPalavras().size() == 1) continue;

            String primeiraPalavra = frase.getListaPalavras().get(0);
            Vertice verticePrimeiraPalavra = new Vertice(primeiraPalavra, 0);

            Aresta arestaFrase = new Aresta(verticePrimeiraPalavra, verticeNeutro, 0);
            arestasPorHashCode.put(arestaFrase.hashCode(), arestaFrase);

            for (int j = 0; j < frase.getListaPalavras().size(); j++) {
                String palavra1 = frase.getListaPalavras().get(j);
                Vertice vertice1 = vertices.get(palavra1);
                if (vertice1 == null) {
                    vertice1 = new Vertice(palavra1, 1);
                    vertices.put(palavra1, vertice1);
                } else {
                    vertice1.setOcorrencias(vertice1.getOcorrencias() + 1);
                }

                for (int k = j + 1; k < frase.getListaPalavras().size(); k++) {
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

    private static GrafoResponse createResponse(Grafo grafo) {
        GrafoResponse response = new GrafoResponse(new ArrayList<>(), new ArrayList<>());
        for (Map.Entry<String, Vertice> entry : grafo.getVertices().entrySet()) {
            Node nodeObject = new Node();
            nodeObject.setId(entry.getKey());
            nodeObject.setGroup(entry.getValue().getOcorrencias());
            response.getNodes().add(nodeObject);
        }

        for (Aresta aresta : grafo.getListaDeAdjacencia()) {
            Link linkObject = new Link();
            linkObject.setSource(aresta.getVertice1().getPalavra());
            linkObject.setTarget(aresta.getVertice2().getPalavra());
            linkObject.setValue(aresta.getPeso());
            response.getLinks().add(linkObject);
        }
        return response;
    }
}

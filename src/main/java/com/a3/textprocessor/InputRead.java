package com.a3.textprocessor;

import com.a3.textprocessor.sdk.Artigo;
import com.a3.textprocessor.sdk.Frase;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputRead {

    // Dicionario de stopwords
    static List<String> dicionarioStopWords = new ArrayList<>(Arrays.asList("a", "as", "da", "das", "o", "os", "do", "dos", "e", "em", "de", "no", "nos", "na", "nas", "ante", "apos", "ate", "com", "contra", "desde", "entre", "para", "por", "perante", "sob", "sobre", "deste", "destes", "este", "estes", "esta", "estas", "desta", "destas", "esse", "essa", "isto", "isso", "se", "sendo", "tambem", "que"));

    public static void main(String[] args) throws IOException {
        run("./src/main/resources/resumos", Charset.forName("Cp1252"));
    }

    public static Artigo readArtigo(String resumePath, String fileName, Charset charset) {
        if (!fileName.contains(".txt")) {
            return null;
        }
        for (String file : listFiles(new File(resumePath))) {
            if(file.contains(fileName)) {
                try {
                    Artigo artigoAtual = readFiles(resumePath + '/' + file, charset);
                    normalizeArtigo(artigoAtual);
                    artigoAtual.quebrarFrases();
                    filtrarStopWords(artigoAtual);

                    System.out.println("Foi encontrado o artigo " + resumePath);

                    return artigoAtual;
                } catch (Exception e) {
                    throw new RuntimeException("Não foi possível carregar o artigo.");
                }
            }
        }
        return null;
    }

    public static List<Artigo> run(String resumePath, Charset charset) {
        List<Artigo> artigos = new ArrayList<>();
        List<Artigo> artigosNormalizados = new ArrayList<>();

        File folder = new File(resumePath);

        for (String file : listFiles(folder)) {
            if (file.contains(".txt")) {
                String path = folder.toString() + "/" + file;
                try {
                    Artigo artigoAtual = readFiles(path, charset);
                    artigos.add(artigoAtual);
                    //print("lido:" + file); // debug de quais arquivos foram lidos
                } catch (Exception e) {
                    //print("Arquivo não lido:" + file); // debug de quais arquivos nao foram lidos
                    //print(e.getLocalizedMessage());
                }
            }
        }

        for (Artigo artigo : artigos) {
            normalizeArtigo(artigo);
            artigo.quebrarFrases();
            filtrarStopWords(artigo);
            artigosNormalizados.add(artigo);
        }

        System.out.println("Foram encontrados " + artigosNormalizados.size() + " artigos.");

        return artigosNormalizados;
    }

    public static String[] listFiles(File resumePath) {
        String[] listaDeArquivos = resumePath.list();
//        print("Path dos resumos: " + resumePath.toString());
//        print("Lista de arquivos no repositorio de resumos:");
//        for (int i = 0; i < listaDeArquivos.length; i++) {
//            print(listaDeArquivos[i]);
//        }
        return listaDeArquivos;
    }

    public static Artigo readFiles(String fileName, Charset charset) throws IOException {
        Path path = Paths.get(fileName);
        List<String> allLines = Files.readAllLines(path, charset);

        Artigo artigo = new Artigo(allLines.get(0), allLines.get(1), allLines.get(2));
        if(allLines.size() > 3) {
            String[] autores = allLines.get(3).split(",");
            artigo.setAutores(Arrays.stream(autores).filter(a -> !a.isEmpty()).toList().toArray(new String[0]));
        }

        return artigo;
    }

    public static void normalizeArtigo(Artigo artigo) {
        artigo.setResumo(artigo.getResumo().toLowerCase());
        artigo.setResumo(Normalizer.normalize(artigo.getResumo(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")); // faz o replace de todos caracteres especiais como acentos
        artigo.setResumo(artigo.getResumo().replaceAll("[^\\w\\s.]", "")); // remove todas as pontuações
    }

    public static void filtrarStopWords(Artigo artigo) {
        List<Frase> frasesResult = new ArrayList<>();

        for (String frase : artigo.getFrases()) {
            Frase fraseFiltrada = filtrarFrase(frase);
            frasesResult.add(fraseFiltrada);
        }
        artigo.setFrasesFiltradas(frasesResult);
    }

    public static Frase filtrarFrase(String fraseInteira) {
        List<String> resultList = new ArrayList<>();
        String[] frase = fraseInteira.split(" ");
        for (String palavra : frase) {
            if (!dicionarioStopWords.contains(palavra) && !palavra.isEmpty()) {
                resultList.add(palavra);
            }
        }
        return new Frase(resultList);
    }
}

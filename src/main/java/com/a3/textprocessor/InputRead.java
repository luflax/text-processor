package com.a3.textprocessor;

import com.a3.textprocessor.sdk.Artigo;
import com.a3.textprocessor.sdk.Frase;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

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


    // O(N)
    public static Artigo readArtigo(String resumePath, String fileName, Charset charset) {
        if (!fileName.contains(".txt")) {
            return null;
        }
        for (String file : listFiles(new File(resumePath))) {
            if(file.contains(fileName)) { // n
                try {
                    Artigo artigoAtual = readFiles(resumePath + '/' + file, charset); // 1
                    normalizeArtigo(artigoAtual); // 1
                    artigoAtual.quebrarFrases(); // 1
                    filtrarStopWords(artigoAtual); // 1

                    System.out.println("Foi encontrado o artigo " + resumePath);

                    return artigoAtual; // 1
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
            if (file.contains(".txt")) { // n
                String path = folder + "/" + file; // n
                try {
                    Artigo artigoAtual = readFiles(path, charset); // n
                    artigos.add(artigoAtual); // n
                    //print("lido:" + file); // debug de quais arquivos foram lidos
                } catch (Exception e) {
                    //print("Arquivo não lido:" + file); // debug de quais arquivos nao foram lidos
                    //print(e.getLocalizedMessage());
                }
            }
        }

        for (Artigo artigo : artigos) {
            normalizeArtigo(artigo); // n
            artigo.quebrarFrases(); // n
            filtrarStopWords(artigo); // n
            artigosNormalizados.add(artigo); // n
        }

        System.out.println("Foram encontrados " + artigosNormalizados.size() + " artigos."); // 1

        return artigosNormalizados;
    }

    public static String[] listFiles(File resumePath) {
        String[] listaDeArquivos = resumePath.list();
        return listaDeArquivos;
    }

    public static Artigo readFiles(String fileName, Charset charset) throws IOException {
        Path path = Paths.get(fileName); // 1
        List<String> allLines = Files.readAllLines(path, charset); // 1

        Artigo artigo = new Artigo(allLines.get(0), allLines.get(1), allLines.get(2), path.getFileName().toString()); // 1
        if(allLines.size() > 3) { // 1
            String[] autores = allLines.get(3).split(","); // 1

            String[] array = Arrays.stream(autores).filter(a -> !a.isEmpty())
                    .map(a -> StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(
                            CaseUtils.toCamelCase(a, true)), ' '))
                    .toList().toArray(new String[0]); // n
            artigo.setAutores(array); // 1
        }

        return artigo;
    }

    public static void normalizeArtigo(Artigo artigo) {
        artigo.setResumo(artigo.getResumo().toLowerCase()); // 1

        // faz o replace de todos caracteres especiais como acentos
        artigo.setResumo(Normalizer.normalize(artigo.getResumo(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")); // n

        // remove todas as pontuações
        artigo.setResumo(artigo.getResumo().replaceAll("[^\\w\\s.]", "")); // n
    }

    public static void filtrarStopWords(Artigo artigo) {
        List<Frase> frasesResult = new ArrayList<>(); // 1

        for (String frase : artigo.getFrases()) { // 1
            Frase fraseFiltrada = filtrarFrase(frase); // n
            frasesResult.add(fraseFiltrada); // n
        }
        artigo.setFrasesFiltradas(frasesResult); // 1
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

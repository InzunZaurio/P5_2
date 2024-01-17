//      Ramirez Inzunza Pedro Miguel - 7CM3
//      Sistemas Distribuidos - Proyecto 5
//      Clase ProcessingServer: Encargada de ser la que desmenuza el texto y lo compara con los textos
//      además de dar el índice de recomendación. Más información en cada comentario.
//      Luego, regresa el resultado de índice de recomendación al WebServer para que este lo mande al cliente Web

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ProcessingServer {
    // Ruta de la carpeta que contiene los archivos de texto
    private static final String LIBROS_TXT_FOLDER = "LIBROS_TXT";

    // Textos almacenados en un mapa (título del libro -> contenido del libro)
    private static Map<String, String> storedTexts = new HashMap<>();

    // Cargar los textos almacenados al iniciar el servidor
    static {
        loadStoredTexts();
    }

    // Método para cargar los textos almacenados desde la carpeta LIBROS_TXT
    private static void loadStoredTexts() {
        File folder = new File(LIBROS_TXT_FOLDER);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
            if (files != null) {
                for (File file : files) {
                    try {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        storedTexts.put(file.getName(), content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Método para procesar las palabras y calcular el índice de recomendación para cada libro
    public static Map<String, Double> processWords(String[] userWords) {
        // Mapa para almacenar el índice de recomendación para cada libro
        Map<String, Double> recommendationIndexMap = new HashMap<>();

        // Iterar sobre cada libro
        for (Map.Entry<String, String> entry : storedTexts.entrySet()) {
            String title = entry.getKey();
            String text = entry.getValue().toLowerCase();

            // Imprimir en consola el título del libro
            System.out.println("\nProcesando el libro: " + title);

            // Contador para las coincidencias de palabras en el texto
            int matchCount = 0;

            // Mapa para almacenar la cantidad de ocurrencias de cada palabra en el texto
            Map<String, Integer> wordOccurrencesMap = new HashMap<>();

            // Imprimir las palabras recibidas
            System.out.println("Palabras recibidas en ProcessingServer:");
            for (String userWord : userWords) {
                // Imprimir la palabra
                System.out.println("- " + userWord);

                // Contar las coincidencias en el texto
                int occurrences = countOccurrences(text, userWord.toLowerCase());
                wordOccurrencesMap.put(userWord, occurrences);

                matchCount += occurrences;
            }

            // Calcular el índice de recomendación y almacenarlo en el mapa
            int totalWords = text.split("\\s+").length;
            double recommendationIndex = (double) matchCount / totalWords;
            recommendationIndexMap.put(title, recommendationIndex);

            // Imprimir la cantidad de ocurrencias de cada palabra en el texto
            System.out.println("Ocurrencias de palabras en el texto:");
            for (Map.Entry<String, Integer> wordEntry : wordOccurrencesMap.entrySet()) {
                System.out.println("- " + wordEntry.getKey() + ": " + wordEntry.getValue() + " ocurrencias");
            }

            // Imprimir el recommendationIndex
            System.out.println("Recommendation Index: " + recommendationIndex);
        }

        // Ordenar el mapa por valor (índice de recomendación) de mayor a menor
        Map<String, Double> sortedRecommendationIndexMap = sortByValueDesc(recommendationIndexMap);

        // Obtener los tres textos con mayor recommendationIndex
        List<String> topThreeTexts = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Double> entry : sortedRecommendationIndexMap.entrySet()) {
            if (count < 3) {
                topThreeTexts.add(entry.getKey());
                count++;
            } else {
                break;
            }
        }

        // Imprimir los tres textos con mayor recommendationIndex
        System.out.println("\nTres textos con mayor recommendationIndex:");
        for (String textTitle : topThreeTexts) {
            System.out.println("- " + textTitle);
        }

        // Enviar los tres textos al WebServer
        WebServer.setTopThreeTexts(topThreeTexts);

        // Retornar el mapa ordenado
        return sortedRecommendationIndexMap;
    }

    // Método para contar las ocurrencias de una palabra en un texto
    private static int countOccurrences(String text, String word) {
        int count = 0;
        int index = text.indexOf(word);
        while (index != -1) {
            count++;
            index = text.indexOf(word, index + 1);
        }
        return count;
    }

    // Método para ordenar un mapa por valor (de mayor a menor)
    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDesc(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        list.sort((o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}

package ld.sa_backend.external.nlp;


import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import java.util.List;
import java.util.Properties;

import ld.sa_backend.dto.Rating;
import ld.sa_backend.enums.ReviewType;

/**
 * Classe utilitaire pour analyser le sentiment d'un texte en utilisant un modèle 
 * de NLP (Natural Language Processing) hébergé sur Hugging Face.
 * <p>
 * Elle envoie le texte à un modèle pré-entraîné et renvoie un {@link ReviewType}
 * correspondant au sentiment dominant du texte.
 * </p>
 * <p>
 * Note : En cas d'erreur HTTP, d'échec de parsing ou de label non reconnu, le sentiment
 * renvoyé sera {@link ReviewType#NEUTRAL}.
 * </p>
 * 
 */


public class FeelingAnalyser {

    private static final String MODEL_URL = "https://api-inference.huggingface.co/models/nlptown/bert-base-multilingual-uncased-sentiment";

    /**
     * Lecture du token Hugging Face depuis le fichier config.properties.
     */
    private static String loadToken() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
            return props.getProperty("HUGGINGFACE_TOKEN");
        } catch (IOException e) {
            System.err.println("Impossible de charger le fichier config.properties ou clé manquante.");
            return null;
        }
    }

    /**
     * Analyse le sentiment d'un texte et renvoie le {@link ReviewType} correspondant.
     *
     * @param text Le texte à analyser
     * @return {@link ReviewType#NEGATIVE}, {@link ReviewType#NEUTRAL} ou {@link ReviewType#POSITIVE}
     */
    public static ReviewType analyzeFeelingType(String text) {

        String token = loadToken();
        if (token == null || token.isBlank()) {
            System.err.println("Token Hugging Face introuvable. Vérifie ton fichier config.properties.");
            return ReviewType.NEUTRAL;
        }
        
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = "{\"inputs\": \"" + text.replace("\"", "\\\"") + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MODEL_URL))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("Erreur HTTP : " + response.statusCode());
                return ReviewType.NEUTRAL;
            }

            ObjectMapper mapper = new ObjectMapper();
            List<List<Rating>> nestedList = mapper.readValue(response.body(), new TypeReference<List<List<Rating>>>(){});
            if (nestedList.isEmpty() || nestedList.get(0).isEmpty()) {
                return ReviewType.NEUTRAL;
            }

            // Recherche du rating avec le score le plus élevé
            Rating bestRating = nestedList.get(0).stream()
                    .max((r1, r2) -> Double.compare(r1.getScore(), r2.getScore()))
                    .orElse(null);

            if (bestRating == null || bestRating.getLabel() == null) {
                return ReviewType.NEUTRAL;
            }

            // Parsing sécurisé du label
            int stars;
            try {
                stars = Integer.parseInt(bestRating.getLabel().split(" ")[0]);
            } catch (NumberFormatException e) {
                return ReviewType.NEUTRAL;
            }

            if (stars <= 2) return ReviewType.NEGATIVE;
            else if (stars == 3) return ReviewType.NEUTRAL;
            else return ReviewType.POSITIVE;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ReviewType.NEUTRAL;
        }
    }

}

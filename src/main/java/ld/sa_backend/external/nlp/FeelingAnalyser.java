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

import ld.sa_backend.dto.NLPModelApiResponse;
import ld.sa_backend.dto.Rating;
import ld.sa_backend.enums.ReviewType;
import ld.sa_backend.exception.ExternalApiException;

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
     * Analyse le sentiment d'un texte et renvoie le {@link ReviewType} correspondant.
     *
     * @param textToAnalyse Le texte à analyser
     * @return {@link ReviewType#NEGATIVE}, {@link ReviewType#NEUTRAL} ou {@link ReviewType#POSITIVE}
     */
    public static ReviewType analyzeFeelingType(String textToAnalyse) {

        String token = loadToken();
        if (token == null || token.isBlank()) {
            System.err.println("Token for NLP model API unfound. Check the file config.properties.");
            return analyseTextFeelingTypeBasicly(textToAnalyse);
        }
        
        HttpClient client = HttpClient.newHttpClient();
        String jsonBody = "{\"inputs\": \"" + textToAnalyse.replace("\"", "\\\"") + "\"}";

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
                throw new ExternalApiException(response.statusCode(), response.body());
            }

            ObjectMapper mapper = new ObjectMapper();
            List<NLPModelApiResponse> responseList = mapper.readValue(
                response.body(),
                new TypeReference<List<NLPModelApiResponse>>() {});

            if (responseList.isEmpty() || responseList.get(0).getRatings().isEmpty()) {
                throw new ExternalApiException(502, "Empty or malformed response of NLP model API");
            }

            List<Rating> ratingList = responseList.get(0).getRatings();
            return convertRatingListToReviewType(ratingList);
            

        } catch (IOException e) {
            throw new ExternalApiException(503, "Error during communication with NLP model API : " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException(503, "NLP request interrupted.");
        }
    
    }

    /**
     * Analyse whether a text is positive or negative based on the presence of basic negative words
     * @param textToAnalyse
     * @return {@link ReviewType#NEGATIVE} if it contains negative words, {@link ReviewType#POSITIVE} ifnot
     */
    private static ReviewType analyseTextFeelingTypeBasicly(String textToAnalyse) {
        if ( 
            textToAnalyse.contains("ne") || textToAnalyse.contains("n'") 
            || textToAnalyse.contains("pas")
        ) {
            return ReviewType.NEGATIVE;
        }
        return ReviewType.POSITIVE;
    }


    private static ReviewType convertRatingListToReviewType(List<Rating> ratingList) {
        
        // Recherche du rating avec le score le plus élevé
        Rating bestRating = ratingList.stream()
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
    }

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

}

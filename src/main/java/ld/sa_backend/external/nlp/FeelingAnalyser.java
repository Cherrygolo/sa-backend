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

import java.util.Arrays;
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
     * Analyse le sentiment d'un texte (positif ou négatif) de manière simple.
     * Utilise quelques marqueurs de négation et mots courts indicateurs de sentiment.
     * Solution de secours lorsque l'API NLP externe n'est pas disponible.
     *
     * @param text le texte à analyser
     * @return ReviewType.POSITIVE ou ReviewType.NEGATIVE
     */
    private static ReviewType analyseTextFeelingTypeBasicly(String text) {
        // Marqueurs simples
        String[] negations = {"ne", "n'", "pas", "jamais", "aucun", "sans"};
        String[] simplePositive = {"bon", "bien", "ok"};
        String[] simpleNegative = {"mal", "non", "nul"};

        int score = 0;
        boolean negateNext = false;

        String[] words = text.toLowerCase().split("\\W+");
        for (String word : words) {
            if (Arrays.asList(negations).contains(word)) {
                negateNext = true;
                continue;
            }
            if (Arrays.asList(simplePositive).contains(word)) {
                score += negateNext ? -1 : 1;
            } else if (Arrays.asList(simpleNegative).contains(word)) {
                score += negateNext ? 1 : -1;
            }
            negateNext = false; // une négation ne s'applique qu'au mot suivant
        }

        return score >= 0 ? ReviewType.POSITIVE : ReviewType.NEGATIVE;
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

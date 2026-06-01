package com.example.cv_evaluator.client;

import com.example.cv_evaluator.config.AiProperties;
import com.example.cv_evaluator.exception.CvEvaluationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

@Component
public class GeminiAiClient implements AiClient {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiAiClient(AiProperties aiProperties, ObjectMapper objectMapper) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public String generateEvaluation(String prompt, byte[] imageBytes, String mimeType) {
        if (!"gemini".equalsIgnoreCase(aiProperties.getProvider())) {
            throw new CvEvaluationException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unsupported AI provider: " + aiProperties.getProvider());
        }

        String apiKey = aiProperties.getGemini().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new CvEvaluationException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Missing Gemini API key. Set GEMINI_API_KEY or cv-evaluator.ai.gemini.api-key.");
        }

        try {
            String requestBody = buildRequestBody(prompt, imageBytes, mimeType);
            List<String> candidateModels = candidateModels();
            for (int i = 0; i < candidateModels.size(); i++) {
                String model = candidateModels.get(i);
                HttpResponse<String> response = sendRequest(apiKey, model, requestBody);

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return extractText(response.body());
                }

                if (isRetryableModelStatus(response.statusCode()) && i < candidateModels.size() - 1) {
                    continue;
                }

                throw new CvEvaluationException(HttpStatus.BAD_GATEWAY,
                        "Gemini API returned HTTP " + response.statusCode() + " for model " + model + ": " + response.body());
            }

            throw new CvEvaluationException(HttpStatus.BAD_GATEWAY,
                    "Gemini API could not complete the request with any configured model");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CvEvaluationException(HttpStatus.BAD_GATEWAY, "Gemini request was interrupted");
        } catch (IOException exception) {
            throw new CvEvaluationException(HttpStatus.BAD_GATEWAY,
                    "Failed to call Gemini API: " + exception.getMessage());
        }
    }

    private String buildEndpoint(String baseUrl, String model, String apiKey) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + "/" + model + ":generateContent?key=" + apiKey;
    }

    private HttpResponse<String> sendRequest(String apiKey, String model, String requestBody) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildEndpoint(apiPropertiesBaseUrl(), model, apiKey)))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private List<String> candidateModels() {
        List<String> models = new ArrayList<>();
        String primaryModel = aiProperties.getGemini().getModel();
        if (primaryModel != null && !primaryModel.isBlank()) {
            models.add(primaryModel.trim());
        }
        List<String> fallbackModels = aiProperties.getGemini().getFallbackModels();
        if (fallbackModels != null) {
            for (String model : fallbackModels) {
                if (model != null && !model.isBlank()) {
                    String trimmed = model.trim();
                    if (!models.contains(trimmed)) {
                        models.add(trimmed);
                    }
                }
            }
        }
        return models;
    }

    private boolean isRetryableModelStatus(int statusCode) {
        return statusCode == 404 || statusCode == 429 || statusCode == 500 || statusCode == 503;
    }

    private String apiPropertiesBaseUrl() {
        String baseUrl = aiProperties.getGemini().getBaseUrl();
        return (baseUrl == null || baseUrl.isBlank())
                ? "https://generativelanguage.googleapis.com/v1beta/models"
                : baseUrl;
    }

    private String buildRequestBody(String prompt, byte[] imageBytes, String mimeType) throws IOException {
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode contents = root.putArray("contents");

        ObjectNode content = contents.addObject();
        content.put("role", "user");

        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", prompt);

        ObjectNode inlineData = parts.addObject().putObject("inlineData");
        inlineData.put("mimeType", mimeType);
        inlineData.put("data", Base64.getEncoder().encodeToString(imageBytes));

        ObjectNode generationConfig = root.putObject("generationConfig");
        generationConfig.put("temperature", 0.1);
        generationConfig.put("responseMimeType", "application/json");

        return objectMapper.writeValueAsString(root);
    }

    private String extractText(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode textNode = root.at("/candidates/0/content/parts/0/text");
        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new CvEvaluationException(HttpStatus.BAD_GATEWAY,
                    "Gemini API response did not contain any generated text");
        }
        return textNode.asText();
    }
}







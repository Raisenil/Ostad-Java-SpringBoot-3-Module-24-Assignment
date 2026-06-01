package com.example.cv_evaluator.service;

import com.example.cv_evaluator.exception.CvEvaluationException;
import com.example.cv_evaluator.model.CvEvaluationResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class CvEvaluationResponseParser {

    private final ObjectMapper objectMapper;

    public CvEvaluationResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CvEvaluationResult parse(String rawResponse) {
        try {
            String normalized = stripMarkdownFences(rawResponse);
            JsonNode jsonNode = objectMapper.readTree(extractJsonObject(normalized));

            int formattingScore = readScore(jsonNode, "formatting_score", "formattingScore");
            int contentScore = readScore(jsonNode, "content_score", "contentScore");
            int skillsScore = readScore(jsonNode, "skills_score", "skillsScore");
            int experienceScore = readScore(jsonNode, "experience_score", "experienceScore");
            int professionalismScore = readScore(jsonNode, "professionalism_score", "professionalismScore");

            int totalScore = formattingScore + contentScore + skillsScore + experienceScore + professionalismScore;
            int percentage = totalScore * 2;

            return new CvEvaluationResult(
                    formattingScore,
                    contentScore,
                    skillsScore,
                    experienceScore,
                    professionalismScore,
                    totalScore,
                    percentage,
                    readStringList(jsonNode, "strengths"),
                    readStringList(jsonNode, "weaknesses"),
                    readStringList(jsonNode, "suggestions")
            );
        } catch (RuntimeException exception) {
            throw new CvEvaluationException(HttpStatus.BAD_GATEWAY,
                    "Unable to parse AI response as JSON: " + exception.getMessage());
        }
    }

    private String stripMarkdownFences(String rawResponse) {
        String trimmed = rawResponse == null ? "" : rawResponse.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed;
    }

    private String extractJsonObject(String content) {
        int startIndex = content.indexOf('{');
        int endIndex = content.lastIndexOf('}');
        if (startIndex < 0 || endIndex < startIndex) {
            throw new CvEvaluationException(HttpStatus.BAD_GATEWAY,
                    "AI response does not contain a JSON object");
        }
        return content.substring(startIndex, endIndex + 1);
    }

    private int readScore(JsonNode jsonNode, String... fieldNames) {
        JsonNode node = findField(jsonNode, fieldNames);
        if (node == null || node.isNull()) {
            throw new CvEvaluationException(HttpStatus.BAD_GATEWAY,
                    "AI response is missing required score field: " + fieldNames[0]);
        }

        int value;
        if (node.isNumber()) {
            value = node.intValue();
        } else {
            String text = node.asText().trim();
            if (text.isEmpty()) {
                throw new CvEvaluationException(HttpStatus.BAD_GATEWAY,
                        "AI response contains an empty score field: " + fieldNames[0]);
            }
            String digits = text.replaceAll("[^0-9-]", "");
            if (digits.isBlank()) {
                throw new CvEvaluationException(HttpStatus.BAD_GATEWAY,
                        "AI response contains a non-numeric score field: " + fieldNames[0]);
            }
            value = Integer.parseInt(digits);
        }

        if (value < 0 || value > 10) {
            throw new CvEvaluationException(HttpStatus.BAD_GATEWAY,
                    "Score out of expected range (0-10) for field: " + fieldNames[0]);
        }

        return value;
    }

    private List<String> readStringList(JsonNode jsonNode, String fieldName) {
        JsonNode node = jsonNode.get(fieldName);
        if (node == null || node.isNull()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(item -> {
                if (item != null && !item.isNull()) {
                    String text = item.asText().trim();
                    if (!text.isEmpty()) {
                        values.add(text);
                    }
                }
            });
            return List.copyOf(values);
        }

        String text = node.asText().trim();
        if (text.isEmpty()) {
            return List.of();
        }

        for (String part : text.split("\\r?\\n|,|;")) {
            String candidate = part.trim();
            if (!candidate.isEmpty()) {
                values.add(candidate);
            }
        }
        return List.copyOf(values);
    }

    private JsonNode findField(JsonNode jsonNode, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode node = jsonNode.get(fieldName);
            if (node != null && !node.isNull()) {
                return node;
            }
        }
        return null;
    }
}





package com.example.cv_evaluator.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class OpenApiController {

    @GetMapping(value = "/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> apiDocs() {
        return ResponseEntity.ok(buildOpenApiSpec());
    }

    private Map<String, Object> buildOpenApiSpec() {
        Map<String, Object> responseSchema = new LinkedHashMap<>();
        responseSchema.put("type", "object");
        responseSchema.put("properties", Map.ofEntries(
                Map.entry("formatting_score", integerSchema("Formatting score from 0 to 10")),
                Map.entry("content_score", integerSchema("Content score from 0 to 10")),
                Map.entry("skills_score", integerSchema("Skills score from 0 to 10")),
                Map.entry("experience_score", integerSchema("Experience score from 0 to 10")),
                Map.entry("professionalism_score", integerSchema("Professionalism score from 0 to 10")),
                Map.entry("total_score", integerSchema("Total score out of 50")),
                Map.entry("percentage", integerSchema("Percentage score out of 100")),
                Map.entry("strengths", stringArraySchema("Strengths list")),
                Map.entry("weaknesses", stringArraySchema("Weaknesses list")),
                Map.entry("suggestions", stringArraySchema("Suggestions list"))
        ));
        responseSchema.put("required", List.of(
                "formatting_score",
                "content_score",
                "skills_score",
                "experience_score",
                "professionalism_score",
                "total_score",
                "percentage",
                "strengths",
                "weaknesses",
                "suggestions"
        ));

        Map<String, Object> multipartSchema = new LinkedHashMap<>();
        multipartSchema.put("type", "object");
        multipartSchema.put("properties", Map.of(
                "cvImage", Map.of(
                        "type", "string",
                        "format", "binary",
                        "description", "Uploaded CV image"
                )
        ));
        multipartSchema.put("required", List.of("cvImage"));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("required", true);
        requestBody.put("content", Map.of(
                "multipart/form-data", Map.of(
                        "schema", multipartSchema,
                        "encoding", Map.of(
                                "cvImage", Map.of("contentType", "image/*")
                        )
                )
        ));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("description", "Successful CV evaluation response");
        response.put("content", Map.of(
                "application/json", Map.of(
                        "schema", responseSchema,
                        "examples", Map.of(
                                "sample", Map.of(
                                        "summary", "Example CV evaluation result",
                                        "value", Map.of(
                                                "formatting_score", 7,
                                                "content_score", 6,
                                                "skills_score", 8,
                                                "experience_score", 5,
                                                "professionalism_score", 7,
                                                "total_score", 33,
                                                "percentage", 66,
                                                "strengths", List.of("Clear section headings", "Relevant skills listed", "Readable layout"),
                                                "weaknesses", List.of("No quantified achievements", "Generic experience descriptions", "Skills section could be more specific"),
                                                "suggestions", List.of("Add measurable outcomes", "Use stronger action verbs", "Tailor skills to job target")
                                        )
                                )
                        )
                )
        ));

        Map<String, Object> paths = new LinkedHashMap<>();
        paths.put("/api/cv/evaluate", Map.of(
                "post", Map.of(
                        "summary", "Evaluate a CV image using AI",
                        "description", "Uploads a CV image, sends it with the evaluation prompt to the AI service, and returns a structured score.",
                        "requestBody", requestBody,
                        "responses", Map.of("200", response)
                )
        ));

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", "CV Evaluator API");
        info.put("version", "1.0.0");
        info.put("description", "Swagger-style OpenAPI definition for the CV Evaluator backend.");

        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("openapi", "3.0.3");
        spec.put("info", info);
        spec.put("servers", List.of(Map.of("url", "http://localhost:8080")));
        spec.put("paths", paths);
        spec.put("components", Map.of(
                "schemas", Map.of(
                        "CvEvaluationResponse", responseSchema
                )
        ));
        return spec;
    }

    private Map<String, Object> integerSchema(String description) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "integer");
        schema.put("format", "int32");
        schema.put("description", description);
        return schema;
    }

    private Map<String, Object> stringArraySchema(String description) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "array");
        schema.put("description", description);
        schema.put("items", Map.of("type", "string"));
        return schema;
    }
}


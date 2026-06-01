package com.example.cv_evaluator.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CvEvaluationPromptProvider {

    private static final String PROMPT_RESOURCE = "prompts/cv-evaluation.prompt.md";

    private final String prompt;

    public CvEvaluationPromptProvider() {
        this.prompt = loadPrompt();
    }

    public String getPrompt() {
        return prompt;
    }

    private String loadPrompt() {
        ClassPathResource resource = new ClassPathResource(PROMPT_RESOURCE);

        if (!resource.exists()) {
            throw new IllegalStateException("Missing CV evaluation prompt resource: " + PROMPT_RESOURCE);
        }

        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read CV evaluation prompt resource", exception);
        }
    }
}


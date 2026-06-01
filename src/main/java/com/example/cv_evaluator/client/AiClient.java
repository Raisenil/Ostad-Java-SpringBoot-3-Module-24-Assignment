package com.example.cv_evaluator.client;

public interface AiClient {

    String generateEvaluation(String prompt, byte[] imageBytes, String mimeType);
}


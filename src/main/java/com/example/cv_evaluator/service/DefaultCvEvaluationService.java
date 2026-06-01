package com.example.cv_evaluator.service;

import com.example.cv_evaluator.client.AiClient;
import com.example.cv_evaluator.exception.CvEvaluationException;
import com.example.cv_evaluator.model.CvEvaluationResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Service
public class DefaultCvEvaluationService {

    private final AiClient aiClient;
    private final CvEvaluationPromptProvider promptProvider;
    private final CvEvaluationResponseParser responseParser;

    public DefaultCvEvaluationService(AiClient aiClient,
                                      CvEvaluationPromptProvider promptProvider,
                                      CvEvaluationResponseParser responseParser) {
        this.aiClient = aiClient;
        this.promptProvider = promptProvider;
        this.responseParser = responseParser;
    }

    public CvEvaluationResult evaluate(MultipartFile cvImage) {
        validate(cvImage);

        try {
            String mimeType = resolveMimeType(cvImage);
            String rawResponse = aiClient.generateEvaluation(
                    promptProvider.getPrompt(),
                    cvImage.getBytes(),
                    mimeType
            );
            return responseParser.parse(rawResponse);
        } catch (IOException exception) {
            throw new CvEvaluationException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to read uploaded CV image: " + exception.getMessage());
        }
    }

    private void validate(MultipartFile cvImage) {
        if (cvImage == null || cvImage.isEmpty()) {
            throw new CvEvaluationException(HttpStatus.BAD_REQUEST, "CV image is required");
        }

        if (!isImageUpload(cvImage)) {
            throw new CvEvaluationException(HttpStatus.BAD_REQUEST,
                    "Only image uploads are supported (png, jpg, jpeg, gif, webp, bmp, tiff)");
        }
    }

    private boolean isImageUpload(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return true;
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }

        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".webp")
                || lower.endsWith(".bmp")
                || lower.endsWith(".tif")
                || lower.endsWith(".tiff");
    }

    private String resolveMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return contentType;
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new CvEvaluationException(HttpStatus.BAD_REQUEST, "Unable to determine image type");
        }

        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".bmp")) {
            return "image/bmp";
        }
        if (lower.endsWith(".tif") || lower.endsWith(".tiff")) {
            return "image/tiff";
        }

        throw new CvEvaluationException(HttpStatus.BAD_REQUEST, "Unsupported image type");
    }
}


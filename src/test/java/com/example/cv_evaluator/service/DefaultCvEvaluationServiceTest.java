package com.example.cv_evaluator.service;

import com.example.cv_evaluator.client.AiClient;
import com.example.cv_evaluator.model.CvEvaluationResult;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultCvEvaluationServiceTest {

    private final AiClient aiClient = mock(AiClient.class);
    private final CvEvaluationPromptProvider promptProvider = mock(CvEvaluationPromptProvider.class);
    private final CvEvaluationResponseParser responseParser = mock(CvEvaluationResponseParser.class);
    private final DefaultCvEvaluationService service = new DefaultCvEvaluationService(aiClient, promptProvider, responseParser);

    @Test
    void evaluatesImageAndUsesPromptProvider() {
        MockMultipartFile file = new MockMultipartFile(
                "cvImage",
                "cv.png",
                "image/png",
                new byte[] {1, 2, 3}
        );

        when(promptProvider.getPrompt()).thenReturn("prompt text");
        when(aiClient.generateEvaluation("prompt text", new byte[] {1, 2, 3}, "image/png")).thenReturn("raw-json");
        when(responseParser.parse("raw-json")).thenReturn(new CvEvaluationResult(
                7, 6, 8, 5, 7, 33, 66,
                java.util.List.of("s1"), java.util.List.of("w1"), java.util.List.of("p1")
        ));

        CvEvaluationResult result = service.evaluate(file);

        assertThat(result.total_score()).isEqualTo(33);
        verify(promptProvider).getPrompt();
        verify(aiClient).generateEvaluation("prompt text", new byte[] {1, 2, 3}, "image/png");
        verify(responseParser).parse("raw-json");
    }

    @Test
    void rejectsNonImageUploads() {
        MockMultipartFile file = new MockMultipartFile(
                "cvImage",
                "cv.txt",
                "text/plain",
                "not an image".getBytes()
        );

        assertThatThrownBy(() -> service.evaluate(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only image uploads are supported");
    }
}



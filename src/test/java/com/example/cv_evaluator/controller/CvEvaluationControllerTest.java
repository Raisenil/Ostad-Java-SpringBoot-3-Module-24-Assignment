package com.example.cv_evaluator.controller;

import com.example.cv_evaluator.model.CvEvaluationResult;
import com.example.cv_evaluator.service.DefaultCvEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CvEvaluationControllerTest {

    private final DefaultCvEvaluationService service = mock(DefaultCvEvaluationService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CvEvaluationController(service))
            .setControllerAdvice(new com.example.cv_evaluator.exception.GlobalExceptionHandler())
            .build();

    @Test
    void returnsEvaluationJson() throws Exception {
        when(service.evaluate(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new CvEvaluationResult(
                        7, 6, 8, 5, 7, 33, 66,
                        List.of("Clear sections"), List.of("No metrics"), List.of("Add results")
                ));

        MockMultipartFile file = new MockMultipartFile(
                "cvImage",
                "cv.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[] {1, 2, 3}
        );

        mockMvc.perform(multipart("/api/cv/evaluate").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formatting_score").value(7))
                .andExpect(jsonPath("$.total_score").value(33))
                .andExpect(jsonPath("$.percentage").value(66))
                .andExpect(jsonPath("$.strengths[0]").value("Clear sections"));
    }
}


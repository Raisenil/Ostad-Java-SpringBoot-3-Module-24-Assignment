package com.example.cv_evaluator.service;

import com.example.cv_evaluator.model.CvEvaluationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import tools.jackson.databind.ObjectMapper;

class CvEvaluationResponseParserTest {

    private final CvEvaluationResponseParser parser = new CvEvaluationResponseParser(new ObjectMapper());

    @Test
    void parsesJsonInsideMarkdownFencesAndComputesDerivedValues() {
        String rawResponse = """
                Here is the evaluation:

                ```json
                {
                  "formatting_score": 7,
                  "content_score": 6,
                  "skills_score": 8,
                  "experience_score": 5,
                  "professionalism_score": 7,
                  "strengths": ["Clear sections", "Relevant skills"],
                  "weaknesses": ["No metrics", "Generic bullets"],
                  "suggestions": ["Add measurable results", "Use stronger verbs"]
                }
                ```
                """;

        CvEvaluationResult result = parser.parse(rawResponse);

        assertThat(result.formatting_score()).isEqualTo(7);
        assertThat(result.content_score()).isEqualTo(6);
        assertThat(result.skills_score()).isEqualTo(8);
        assertThat(result.experience_score()).isEqualTo(5);
        assertThat(result.professionalism_score()).isEqualTo(7);
        assertThat(result.total_score()).isEqualTo(33);
        assertThat(result.percentage()).isEqualTo(66);
        assertThat(result.strengths()).isEqualTo(List.of("Clear sections", "Relevant skills"));
        assertThat(result.weaknesses()).isEqualTo(List.of("No metrics", "Generic bullets"));
        assertThat(result.suggestions()).isEqualTo(List.of("Add measurable results", "Use stronger verbs"));
    }
}




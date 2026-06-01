package com.example.cv_evaluator.model;

import java.util.List;

public record CvEvaluationResult(
        int formatting_score,
        int content_score,
        int skills_score,
        int experience_score,
        int professionalism_score,
        int total_score,
        int percentage,
        List<String> strengths,
        List<String> weaknesses,
        List<String> suggestions
) {
}




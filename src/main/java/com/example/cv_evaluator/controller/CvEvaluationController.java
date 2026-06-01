package com.example.cv_evaluator.controller;

import com.example.cv_evaluator.model.CvEvaluationResult;
import com.example.cv_evaluator.service.DefaultCvEvaluationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv")
public class CvEvaluationController {

    private final DefaultCvEvaluationService evaluationService;

    public CvEvaluationController(DefaultCvEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping(value = "/evaluate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CvEvaluationResult> evaluate(@RequestParam(value = "cvImage", required = false) MultipartFile cvImage) {
        return ResponseEntity.ok(evaluationService.evaluate(cvImage));
    }
}



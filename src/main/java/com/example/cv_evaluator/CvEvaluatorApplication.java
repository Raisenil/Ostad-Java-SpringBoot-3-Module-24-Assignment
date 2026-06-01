package com.example.cv_evaluator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CvEvaluatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CvEvaluatorApplication.class, args);
    }

}

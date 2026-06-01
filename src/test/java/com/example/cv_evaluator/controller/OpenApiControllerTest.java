package com.example.cv_evaluator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OpenApiControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new OpenApiController()).build();

    @Test
    void returnsOpenApiDefinition() throws Exception {
        mockMvc.perform(get("/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value("3.0.3"))
                .andExpect(jsonPath("$.info.title").value("CV Evaluator API"))
                .andExpect(jsonPath("$.paths['/api/cv/evaluate'].post.summary").value("Evaluate a CV image using AI"));
    }
}


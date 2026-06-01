package com.example.cv_evaluator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
public class JacksonConfiguration {

    @Bean
    @Primary
    public JsonMapper objectMapper() {
        return JsonMapper.builder().build();
    }
}



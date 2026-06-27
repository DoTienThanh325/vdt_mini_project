package com.vdt.documenttransfer.common.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.core.StreamReadConstraints;

@Configuration
public class JacksonLimitConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.postConfigurer(objectMapper -> {
            objectMapper.getFactory().setStreamReadConstraints(
                    StreamReadConstraints.builder()
                            .maxStringLength(100_000_000)
                            .build());
        });
    }
}
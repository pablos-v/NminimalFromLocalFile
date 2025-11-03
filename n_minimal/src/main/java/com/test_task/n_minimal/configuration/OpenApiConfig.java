package com.test_task.n_minimal.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * Возвращает логгер для записи сообщений
     */
    @Bean
    public Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("N Minimal API")
                        .version("1.0")
                        .description("API for finding N-th minimal value from files"));
    }
}

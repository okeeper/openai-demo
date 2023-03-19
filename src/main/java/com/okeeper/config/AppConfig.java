package com.okeeper.config;

import com.okeeper.client.OpenAIClient;
import com.okeeper.openai.OpenAiApi;
import com.okeeper.openai.OpenAiService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
public class AppConfig {

    @Bean
    public OpenAiService openAiApi(@Value("${openai.token}") String token) {
        OpenAiService service = new OpenAiService(token);
        return service;
    }

    @Bean
    public OpenAIClient openAIClient(@Value("${openai.token}") String token, @Value("${openai.proxy:}") String proxy) {
        return new OpenAIClient(token, proxy);
    }
}

package com.okeeper.config;

import com.okeeper.controller.dto.MessageItemDTO;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAiConfig {
    private String token;
    private Map<Integer, ChatInitMessageDTO> chatInit = new HashMap<>();
    private Integer defaultChatTimes = 5;

    @Data
    public static class ChatInitMessageDTO {
        private String tips;
        private String system;
    }
}

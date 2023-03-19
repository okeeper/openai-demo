package com.okeeper.controller.dto;

import com.okeeper.entity.ChatMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatMessageBuilder {
    public static final String ROLE_TIPS = "tips";
    public static final String ROLE_USER = "user";
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_ASSISTANT = "assistant";

    public static ChatMessageBuilder builder() {
        return new ChatMessageBuilder();
    }

    private List<MessageItemDTO> messages = new ArrayList<>();
    private ChatMessageBuilder() {

    }
    public ChatMessageBuilder appendMessage(String role, String content) {
        messages.add(new MessageItemDTO(role, content));
        return this;
    }

    public ChatMessageBuilder fromMessageList(List<ChatMessage> chatMessageList) {
        for (ChatMessage chatMessage : chatMessageList) {
            if(!ROLE_TIPS.equals(chatMessage.getRole())) {
                messages.add(new MessageItemDTO(chatMessage.getRole(), chatMessage.getContent()));
            }
        }
        return this;
    }

    public  List<MessageItemDTO> build() {
        return messages;
    }
}

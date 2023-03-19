package com.okeeper.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ChatReq extends BaseReq {
    @NotNull
    private Long chatId;
    @NotEmpty
    private String userMessage;
}

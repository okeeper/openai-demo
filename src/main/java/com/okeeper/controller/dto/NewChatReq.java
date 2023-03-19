package com.okeeper.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class NewChatReq extends BaseReq {
    @NotNull
    private Integer chatType;
}

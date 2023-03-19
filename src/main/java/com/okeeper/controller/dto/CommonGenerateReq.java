package com.okeeper.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CommonGenerateReq extends BaseReq {
    @NotEmpty
    private String prompt;
    @NotNull
    private Integer chatType;
}

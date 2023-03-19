package com.okeeper.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
public class BaseReq implements Serializable {
    @NotEmpty
    protected String unionId;
    protected Long userId;
}

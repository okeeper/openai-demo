package com.okeeper.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class RewardRemainTimesReq extends BaseReq {
    @NotNull
    private Integer rewardType;
}

package com.okeeper.client;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PollDataDTO {
    private String errorMsg;
    private boolean isFinished;
    private String text;
}

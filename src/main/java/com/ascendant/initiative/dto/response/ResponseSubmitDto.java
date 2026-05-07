package com.ascendant.initiative.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class ResponseSubmitDto {
    private UUID responseId;
    private String status;
    private String message;
    private Integer estimatedSeconds;
    private Boolean aiLimited;
}

package com.ascendant.initiative.dto.response;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class ResponseSubmitRequest {
    @NotNull private UUID userId;
    @NotNull private UUID missionId;
    @NotNull private UUID scenarioId;

    @NotBlank @Size(min = 10, max = 2000)
    private String answerText;

    private String selectedChoice;
}

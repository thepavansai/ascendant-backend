package com.ascendant.initiative.dto.mission;

import com.ascendant.initiative.model.Mission;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class MissionSummaryDto {
    private UUID id;
    private String title;
    private Integer difficultyLevel;
    private Mission.MissionType missionType;
    private Boolean isLocked;
    private Boolean userCompleted;
    private Double bestScore;
}

package com.ascendant.initiative.dto.mission;

import com.ascendant.initiative.model.Mission;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data @Builder
public class MissionDetailDto {
    private UUID id;
    private String title;
    private String narrative;
    private Integer difficultyLevel;
    private Mission.MissionType missionType;
    private Double ruleWeight;
    private Double aiWeight;
    private Map<String, Double> attributeWeights;
    private ScenarioDto scenario;

    @Data @Builder
    public static class ScenarioDto {
        private UUID id;
        private String context;
        private List<Map<String, String>> choices;
        private Boolean openResponse;
        private Integer orderIndex;
    }
}

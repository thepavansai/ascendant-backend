package com.ascendant.initiative.util;

import com.ascendant.initiative.dto.mission.MissionDetailDto;
import com.ascendant.initiative.dto.mission.MissionSummaryDto;
import com.ascendant.initiative.model.Mission;
import com.ascendant.initiative.model.Scenario;
import org.springframework.stereotype.Component;

@Component
public class MissionMapper {

    public MissionSummaryDto toSummary(Mission mission, boolean isLocked,
                                       boolean completed, Double bestScore) {
        return MissionSummaryDto.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .difficultyLevel(mission.getDifficultyLevel())
                .missionType(mission.getMissionType())
                .isLocked(isLocked)
                .userCompleted(completed)
                .bestScore(bestScore)
                .build();
    }

    public MissionDetailDto toDetail(Mission mission) {
        MissionDetailDto.ScenarioDto scenarioDto = null;
        Scenario s = mission.getScenario();
        if (s != null) {
            scenarioDto = MissionDetailDto.ScenarioDto.builder()
                    .id(s.getId())
                    .context(s.getContext())
                    .choices(s.getChoices())
                    .openResponse(s.getOpenResponse())
                    .orderIndex(s.getOrderIndex())
                    .build();
        }

        return MissionDetailDto.builder()
                .id(mission.getId())
                .title(mission.getTitle())
                .narrative(mission.getNarrative())
                .difficultyLevel(mission.getDifficultyLevel())
                .missionType(mission.getMissionType())
                .ruleWeight(mission.getRuleWeight())
                .aiWeight(mission.getAiWeight())
                .attributeWeights(mission.getAttributeWeights())
                .scenario(scenarioDto)
                .build();
    }
}

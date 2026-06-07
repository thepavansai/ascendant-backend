package com.ascendant.initiative.controller;

import com.ascendant.initiative.exception.AppException;
import com.ascendant.initiative.model.Mission;
import com.ascendant.initiative.model.Scenario;
import com.ascendant.initiative.repository.MissionRepository;
import com.ascendant.initiative.repository.ScenarioRepository;
import com.ascendant.initiative.repository.UserRepository;
import com.ascendant.initiative.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final MissionRepository missionRepository;
    private final ScenarioRepository scenarioRepository;
    private final MissionService missionService;
    private final UserRepository userRepository;

    @PostMapping("/missions")
    public ResponseEntity<Map<String, Object>> createMission(
            @RequestBody Map<String, Object> body) {

        Mission mission = Mission.builder()
                .title((String) body.get("title"))
                .narrative((String) body.get("narrative"))
                .difficultyLevel((Integer) body.get("difficulty_level"))
                .missionType(Mission.MissionType.valueOf((String) body.get("mission_type")))
                .ruleWeight(body.containsKey("rule_weight")
                        ? ((Number) body.get("rule_weight")).doubleValue() : 0.3)
                .aiWeight(body.containsKey("ai_weight")
                        ? ((Number) body.get("ai_weight")).doubleValue() : 0.7)
                .isActive(true)
                .build();
        missionRepository.save(mission);

        // Create scenario if provided
        @SuppressWarnings("unchecked")
        Map<String, Object> scenarioBody = (Map<String, Object>) body.get("scenario");
        if (scenarioBody != null) {
            Scenario scenario = Scenario.builder()
                    .mission(mission)
                    .context((String) scenarioBody.get("context"))
                    .openResponse(Boolean.TRUE.equals(scenarioBody.get("open_response")))
                    .choices((List<Map<String, String>>) scenarioBody.get("choices"))
                    .orderIndex(1)
                    .build();
            scenarioRepository.save(scenario);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", mission.getId(),
                "title", mission.getTitle(),
                "message", "Mission created successfully."
        ));
    }

    @PutMapping("/missions/{id}")
    public ResponseEntity<Map<String, Object>> updateMission(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Mission not found: " + id));

        if (body.containsKey("title"))
            mission.setTitle((String) body.get("title"));
        if (body.containsKey("narrative"))
            mission.setNarrative((String) body.get("narrative"));
        if (body.containsKey("difficulty_level"))
            mission.setDifficultyLevel((Integer) body.get("difficulty_level"));
        if (body.containsKey("is_active"))
            mission.setIsActive((Boolean) body.get("is_active"));
        if (body.containsKey("mission_type"))
            mission.setMissionType(Mission.MissionType.valueOf((String) body.get("mission_type")));
        if (body.containsKey("rule_weight"))
            mission.setRuleWeight(((Number) body.get("rule_weight")).doubleValue());
        if (body.containsKey("ai_weight"))
            mission.setAiWeight(((Number) body.get("ai_weight")).doubleValue());

        missionRepository.save(mission);
        
        // Update scenario if provided
        if (body.containsKey("scenario")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> scenarioBody = (Map<String, Object>) body.get("scenario");
            Scenario scenario = scenarioRepository.findByMissionId(mission.getId())
                    .orElseGet(() -> Scenario.builder().mission(mission).orderIndex(1).build());
            
            if (scenarioBody.containsKey("context"))
                scenario.setContext((String) scenarioBody.get("context"));
            if (scenarioBody.containsKey("open_response"))
                scenario.setOpenResponse(Boolean.TRUE.equals(scenarioBody.get("open_response")));
            if (scenarioBody.containsKey("choices"))
                scenario.setChoices((List<Map<String, String>>) scenarioBody.get("choices"));
                
            scenarioRepository.save(scenario);
        }

        missionRepository.save(mission);
        missionService.invalidateCache(id);

        return ResponseEntity.ok(Map.of(
                "id", id,
                "message", "Mission updated. Cache invalidated."
        ));
    }

    @DeleteMapping("/missions/{id}")
    public ResponseEntity<Void> deactivateMission(@PathVariable UUID id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Mission not found: " + id));
        mission.setIsActive(false);
        missionRepository.save(mission);
        missionService.invalidateCache(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "50") int limit) {

        var users = role != null
                ? userRepository.findAll().stream()
                    .filter(u -> u.getRole().name().equals(role))
                    .limit(limit).toList()
                : userRepository.findAll().stream().limit(limit).toList();

        var result = users.stream().map(u -> Map.<String, Object>of(
                "id", u.getId(),
                "name", u.getName(),
                "email", u.getEmail(),
                "role", u.getRole(),
                "created_at", u.getCreatedAt()
        )).toList();

        return ResponseEntity.ok(result);
    }
}

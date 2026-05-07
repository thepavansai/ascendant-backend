package com.ascendant.initiative.controller;

import com.ascendant.initiative.dto.mission.MissionDetailDto;
import com.ascendant.initiative.dto.mission.MissionSummaryDto;
import com.ascendant.initiative.exception.AppException;
import com.ascendant.initiative.model.User;
import com.ascendant.initiative.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @GetMapping
    public ResponseEntity<List<MissionSummaryDto>> listMissions(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(missionService.getAllForUser(user.getId()));
    }

    @GetMapping("/next")
    public ResponseEntity<MissionDetailDto> getNext(
            @AuthenticationPrincipal User user) {
        return missionService.getNextForUser(user.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MissionDetailDto> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        MissionDetailDto dto = missionService.getById(id);
        return ResponseEntity.ok(dto);
    }
}

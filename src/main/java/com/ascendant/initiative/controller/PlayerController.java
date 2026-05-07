package com.ascendant.initiative.controller;

import com.ascendant.initiative.dto.player.PlayerProfileDto;
import com.ascendant.initiative.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<PlayerProfileDto> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(playerService.getProfile(userId));
    }

    @GetMapping("/{userId}/progression")
    public ResponseEntity<List<Map<String, Object>>> getProgression(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "30") int limit) {
        limit = Math.min(limit, 100);
        return ResponseEntity.ok(playerService.getProgression(userId, limit));
    }

    @GetMapping("/{userId}/stats/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyStats(@PathVariable UUID userId) {
        return ResponseEntity.ok(playerService.getWeeklyStats(userId));
    }
}

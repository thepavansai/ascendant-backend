package com.ascendant.initiative.controller;

import com.ascendant.initiative.dto.player.PlayerProfileDto;
import com.ascendant.initiative.exception.AppException;
import com.ascendant.initiative.model.User;
import com.ascendant.initiative.repository.ParentChildLinkRepository;
import com.ascendant.initiative.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final ParentChildLinkRepository parentChildLinkRepository;

    private void verifyAccess(User currentUser, UUID targetUserId) {
        if (currentUser.getRole() == User.Role.ADMIN) return;
        if (currentUser.getId().equals(targetUserId)) return;
        
        if (currentUser.getRole() == User.Role.PARENT) {
            boolean isLinked = parentChildLinkRepository.findByParentIdAndChildId(currentUser.getId(), targetUserId).isPresent();
            if (isLinked) return;
        }
        
        throw AppException.forbidden("You do not have permission to access this user's data");
    }

    @GetMapping("/{userId}/profile")
    public ResponseEntity<PlayerProfileDto> getProfile(
            @PathVariable UUID userId,
            @AuthenticationPrincipal User currentUser) {
        verifyAccess(currentUser, userId);
        return ResponseEntity.ok(playerService.getProfile(userId));
    }

    @GetMapping("/{userId}/progression")
    public ResponseEntity<List<Map<String, Object>>> getProgression(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "30") int limit,
            @AuthenticationPrincipal User currentUser) {
        verifyAccess(currentUser, userId);
        limit = Math.min(limit, 100);
        return ResponseEntity.ok(playerService.getProgression(userId, limit));
    }

    @GetMapping("/{userId}/stats/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyStats(
            @PathVariable UUID userId,
            @AuthenticationPrincipal User currentUser) {
        verifyAccess(currentUser, userId);
        return ResponseEntity.ok(playerService.getWeeklyStats(userId));
    }
}

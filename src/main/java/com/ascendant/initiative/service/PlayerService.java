package com.ascendant.initiative.service;

import com.ascendant.initiative.dto.player.PlayerProfileDto;
import com.ascendant.initiative.exception.AppException;
import com.ascendant.initiative.model.PlayerProfile;
import com.ascendant.initiative.repository.EvaluationRepository;
import com.ascendant.initiative.repository.PlayerProfileRepository;
import com.ascendant.initiative.repository.ProgressionLogRepository;
import com.ascendant.initiative.repository.ResponseRepository;
import com.ascendant.initiative.util.XpCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerService {

    private final PlayerProfileRepository playerProfileRepository;
    private final ProgressionLogRepository progressionLogRepository;
    private final ResponseRepository responseRepository;
    private final EvaluationRepository evaluationRepository;
    private final XpCalculator xpCalculator;

    @Transactional(readOnly = true)
    public PlayerProfileDto getProfile(UUID userId) {
        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> AppException.notFound("Player profile not found for user: " + userId));

        int missionsCompleted = countCompletedMissions(userId);
        double averageScore   = getAverageScore(userId);

        return PlayerProfileDto.builder()
                .userId(userId)
                .name(profile.getUser().getName())
                .identityType(profile.getIdentityType())
                .xp(profile.getXp())
                .level(profile.getLevel())
                .xpToNextLevel(xpCalculator.xpToNextLevel(profile.getXp()))
                .intellect(round(profile.getIntellect()))
                .judgment(round(profile.getJudgment()))
                .awareness(round(profile.getAwareness()))
                .clarity(round(profile.getClarity()))
                .streakDays(profile.getStreakDays())
                .lastActive(profile.getLastActive())
                .missionsCompleted(missionsCompleted)
                .averageScore(round(averageScore))
                .build();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProgression(UUID userId, int limit) {
        return progressionLogRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
                .stream()
                .map(log -> Map.<String, Object>of(
                        "id",            log.getId(),
                        "mission_id",    log.getMission().getId(),
                        "mission_title", log.getMission().getTitle(),
                        "xp_earned",     log.getXpEarned(),
                        "final_score",   log.getFinalScore(),
                        "level_before",  log.getLevelBefore(),
                        "level_after",   log.getLevelAfter(),
                        "leveled_up",    log.getLeveledUp(),
                        "created_at",    log.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getWeeklyStats(UUID userId) {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        var logs = progressionLogRepository.findByUserIdSince(userId, weekStart);

        int totalXp = logs.stream().mapToInt(l -> l.getXpEarned()).sum();
        double avgScore = logs.stream()
                .mapToDouble(l -> l.getFinalScore() != null ? l.getFinalScore() : 0.0)
                .average().orElse(0.0);

        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> AppException.notFound("Player not found"));

        String strongest = argmax(profile);
        String weakest   = argmin(profile);

        return Map.of(
                "missions_completed",    logs.size(),
                "total_xp_earned",       totalXp,
                "average_final_score",   round(avgScore),
                "strongest_attribute",   strongest,
                "weakest_attribute",     weakest
        );
    }

    // ── private helpers ──────────────────────────────────────────

    private int countCompletedMissions(UUID userId) {
        return (int) responseRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 1000))
                .stream()
                .filter(r -> evaluationRepository.findByResponseId(r.getId())
                        .map(e -> e.getEvalStatus() ==
                                com.ascendant.initiative.model.Evaluation.EvalStatus.DONE)
                        .orElse(false))
                .map(r -> r.getMission().getId())
                .distinct()
                .count();
    }

    private double getAverageScore(UUID userId) {
        return responseRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 1000))
                .stream()
                .map(r -> evaluationRepository.findByResponseId(r.getId()))
                .filter(o -> o.isPresent() && o.get().getFinalScore() != null)
                .mapToDouble(o -> o.get().getFinalScore())
                .average()
                .orElse(0.0);
    }

    private String argmax(PlayerProfile p) {
        double max = Math.max(Math.max(p.getIntellect(), p.getJudgment()),
                              Math.max(p.getAwareness(), p.getClarity()));
        if (max == p.getIntellect()) return "intellect";
        if (max == p.getJudgment())  return "judgment";
        if (max == p.getAwareness()) return "awareness";
        return "clarity";
    }

    private String argmin(PlayerProfile p) {
        double min = Math.min(Math.min(p.getIntellect(), p.getJudgment()),
                              Math.min(p.getAwareness(), p.getClarity()));
        if (min == p.getIntellect()) return "intellect";
        if (min == p.getJudgment())  return "judgment";
        if (min == p.getAwareness()) return "awareness";
        return "clarity";
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}

package com.ascendant.initiative.service;

import com.ascendant.initiative.model.*;
import com.ascendant.initiative.repository.PlayerProfileRepository;
import com.ascendant.initiative.repository.ProgressionLogRepository;
import com.ascendant.initiative.util.XpCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressService {

    private final PlayerProfileRepository playerProfileRepository;
    private final ProgressionLogRepository progressionLogRepository;
    private final GamificationService gamificationService;
    private final XpCalculator xpCalculator;

    @Transactional
    public ProgressResult record(User user, Mission mission, Evaluation evaluation) {
        PlayerProfile profile = playerProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    PlayerProfile newProfile = PlayerProfile.builder().user(user).build();
                    return playerProfileRepository.save(newProfile);
                });

        int levelBefore = profile.getLevel();
        int xpEarned = xpCalculator.calculateXpEarned(
                evaluation.getFinalScore(), mission.getDifficultyLevel());

        // Update XP and level
        int newXp = profile.getXp() + xpEarned;
        profile.setXp(newXp);
        int newLevel = xpCalculator.calculateLevel(newXp);
        profile.setLevel(newLevel);

        // Update attributes from AI scores
        if (evaluation.getIntellectScore() != null) {
            gamificationService.updateAttributes(profile,
                    evaluation.getIntellectScore(),
                    evaluation.getJudgmentScore(),
                    evaluation.getAwarenessScore(),
                    evaluation.getClarityScore());
        }

        // Update streak
        gamificationService.updateStreak(profile);

        playerProfileRepository.save(profile);

        // Write progression log
        boolean leveledUp = newLevel > levelBefore;
        ProgressionLog log = ProgressionLog.builder()
                .user(user)
                .mission(mission)
                .xpEarned(xpEarned)
                .finalScore(evaluation.getFinalScore())
                .levelBefore(levelBefore)
                .levelAfter(newLevel)
                .leveledUp(leveledUp)
                .response(evaluation.getResponse())
                .build();
        progressionLogRepository.save(log);

        return new ProgressResult(xpEarned, leveledUp, newLevel);
    }

    public record ProgressResult(int xpEarned, boolean leveledUp, int newLevel) {}
}

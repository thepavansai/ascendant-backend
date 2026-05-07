package com.ascendant.initiative.service;

import com.ascendant.initiative.model.PlayerProfile;
import com.ascendant.initiative.model.PlayerProfile.IdentityType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GamificationService {

    /**
     * Updates the player's 4 cognitive attributes using an exponential moving average.
     * New attribute = (old × 0.85) + (missionScore × 0.15 × 10)
     * This prevents single-mission spikes and creates gradual, realistic progression.
     */
    public void updateAttributes(PlayerProfile profile,
                                  double intellectScore,
                                  double judgmentScore,
                                  double awarenessScore,
                                  double clarityScore) {
        profile.setIntellect(blend(profile.getIntellect(), intellectScore * 10));
        profile.setJudgment(blend(profile.getJudgment(), judgmentScore * 10));
        profile.setAwareness(blend(profile.getAwareness(), awarenessScore * 10));
        profile.setClarity(blend(profile.getClarity(), clarityScore * 10));

        // Recalculate identity type after attribute update
        profile.setIdentityType(determineIdentity(profile));
        log.debug("Updated attributes for player {} → identity: {}", profile.getId(), profile.getIdentityType());
    }

    /**
     * Identity is determined by the highest attribute.
     * ANALYST   → highest intellect
     * STRATEGIST → highest judgment
     * CREATOR   → highest awareness
     * BUILDER   → highest clarity
     */
    public IdentityType determineIdentity(PlayerProfile profile) {
        double max = Math.max(
            Math.max(profile.getIntellect(), profile.getJudgment()),
            Math.max(profile.getAwareness(), profile.getClarity())
        );

        if (max == profile.getIntellect()) return IdentityType.ANALYST;
        if (max == profile.getJudgment())  return IdentityType.STRATEGIST;
        if (max == profile.getAwareness()) return IdentityType.CREATOR;
        return IdentityType.BUILDER;
    }

    /**
     * Updates streak: +1 if last active yesterday, reset to 1 if gap > 1 day.
     */
    public void updateStreak(PlayerProfile profile) {
        java.time.LocalDate today = java.time.LocalDate.now();
        if (profile.getLastActive() == null) {
            profile.setStreakDays(1);
        } else {
            java.time.LocalDate lastActiveDate = profile.getLastActive().toLocalDate();
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastActiveDate, today);
            if (daysBetween == 1) {
                profile.setStreakDays(profile.getStreakDays() + 1);
            } else if (daysBetween > 1) {
                profile.setStreakDays(1);
            }
            // daysBetween == 0 means same day — don't change streak
        }
        profile.setLastActive(java.time.LocalDateTime.now());
    }

    private double blend(double oldValue, double newValue) {
        return Math.min(10.0, (oldValue * 0.85) + (newValue * 0.15));
    }
}

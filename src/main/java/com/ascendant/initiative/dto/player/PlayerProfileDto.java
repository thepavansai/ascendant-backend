package com.ascendant.initiative.dto.player;

import com.ascendant.initiative.model.PlayerProfile;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class PlayerProfileDto {
    private UUID userId;
    private String name;
    private PlayerProfile.IdentityType identityType;
    private Integer xp;
    private Integer level;
    private Integer xpToNextLevel;
    private Double intellect;
    private Double judgment;
    private Double awareness;
    private Double clarity;
    private Integer streakDays;
    private LocalDateTime lastActive;
    private Integer missionsCompleted;
    private Double averageScore;
    private Boolean isApproved;
}

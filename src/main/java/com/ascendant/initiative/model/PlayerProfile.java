package com.ascendant.initiative.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlayerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type", length = 20)
    @Builder.Default
    private IdentityType identityType = IdentityType.ANALYST;

    @Column(nullable = false)
    @Builder.Default
    private Integer xp = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(nullable = false)
    @Builder.Default
    private Double intellect = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double judgment = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double awareness = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double clarity = 0.0;

    @Column(name = "streak_days", nullable = false)
    @Builder.Default
    private Integer streakDays = 0;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum IdentityType {
        STRATEGIST, BUILDER, ANALYST, CREATOR
    }
}

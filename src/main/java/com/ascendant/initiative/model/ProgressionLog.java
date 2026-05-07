package com.ascendant.initiative.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "progression_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProgressionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(name = "xp_earned", nullable = false)
    private Integer xpEarned;

    @Column(name = "level_before", nullable = false)
    private Integer levelBefore;

    @Column(name = "level_after", nullable = false)
    private Integer levelAfter;

    @Column(name = "leveled_up", nullable = false)
    @Builder.Default
    private Boolean leveledUp = false;

    @Column(name = "final_score")
    private Double finalScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

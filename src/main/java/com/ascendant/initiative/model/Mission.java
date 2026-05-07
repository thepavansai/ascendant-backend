package com.ascendant.initiative.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "missions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String narrative;

    @Column(name = "difficulty_level", nullable = false)
    private Integer difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type", nullable = false, length = 20)
    private MissionType missionType;

    @Column(name = "rule_weight", nullable = false)
    @Builder.Default
    private Double ruleWeight = 0.3;

    @Column(name = "ai_weight", nullable = false)
    @Builder.Default
    private Double aiWeight = 0.7;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attribute_weights", columnDefinition = "jsonb")
    private Map<String, Double> attributeWeights;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToOne(mappedBy = "mission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Scenario scenario;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum MissionType {
        FACTUAL, ANALYTICAL, OPEN_ENDED
    }
}

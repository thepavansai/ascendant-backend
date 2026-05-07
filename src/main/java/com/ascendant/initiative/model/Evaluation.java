package com.ascendant.initiative.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evaluations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false, unique = true)
    private Response response;

    @Column(name = "rule_score")
    private Double ruleScore;

    @Column(name = "ai_score")
    private Double aiScore;

    @Column(name = "final_score")
    private Double finalScore;

    @Column(name = "intellect_score")
    private Double intellectScore;

    @Column(name = "judgment_score")
    private Double judgmentScore;

    @Column(name = "awareness_score")
    private Double awarenessScore;

    @Column(name = "clarity_score")
    private Double clarityScore;

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    @Column(name = "ai_tokens_used")
    private Integer aiTokensUsed;

    @Enumerated(EnumType.STRING)
    @Column(name = "eval_status", nullable = false, length = 20)
    @Builder.Default
    private EvalStatus evalStatus = EvalStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum EvalStatus {
        PENDING, DONE, FAILED
    }
}

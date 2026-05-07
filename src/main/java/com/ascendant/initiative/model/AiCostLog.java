package com.ascendant.initiative.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_cost_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiCostLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "tokens_used", nullable = false)
    private Integer tokensUsed;

    @Column(name = "model", length = 50)
    private String model;

    @Column(name = "call_date", nullable = false)
    private LocalDate callDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

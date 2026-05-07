package com.ascendant.initiative.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "scenarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false, unique = true)
    private Mission mission;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String context;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, String>> choices;

    @Column(name = "open_response", nullable = false)
    @Builder.Default
    private Boolean openResponse = true;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 1;
}

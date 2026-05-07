package com.ascendant.initiative.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "parent_child_links")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParentChildLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child;

    @Column(nullable = false)
    @Builder.Default
    private Boolean approved = false;

    @CreationTimestamp
    @Column(name = "linked_at", updatable = false)
    private LocalDateTime linkedAt;
}

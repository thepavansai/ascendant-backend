package com.ascendant.initiative.repository;

import com.ascendant.initiative.model.ProgressionLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgressionLogRepository extends JpaRepository<ProgressionLog, UUID> {
    Optional<ProgressionLog> findByResponseId(UUID responseId);
    
    List<ProgressionLog> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT p FROM ProgressionLog p WHERE p.user.id = :userId AND p.createdAt >= :since ORDER BY p.createdAt DESC")
    List<ProgressionLog> findByUserIdSince(UUID userId, LocalDateTime since);
}

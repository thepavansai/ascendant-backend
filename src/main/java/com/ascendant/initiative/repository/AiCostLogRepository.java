package com.ascendant.initiative.repository;

import com.ascendant.initiative.model.AiCostLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface AiCostLogRepository extends JpaRepository<AiCostLog, UUID> {

    @Query("SELECT COUNT(a) FROM AiCostLog a WHERE a.user.id = :userId AND a.callDate = :date")
    long countByUserIdAndCallDate(UUID userId, LocalDate date);
}

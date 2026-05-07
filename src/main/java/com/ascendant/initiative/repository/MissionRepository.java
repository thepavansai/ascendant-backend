package com.ascendant.initiative.repository;

import com.ascendant.initiative.model.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MissionRepository extends JpaRepository<Mission, UUID> {

    List<Mission> findByIsActiveTrueOrderByDifficultyLevelAsc();

    @Query("SELECT m FROM Mission m WHERE m.isActive = true AND m.missionType = :type ORDER BY m.difficultyLevel ASC")
    List<Mission> findActiveByType(Mission.MissionType type);

    @Query("""
        SELECT m FROM Mission m
        WHERE m.isActive = true
        AND m.id NOT IN (
            SELECT r.mission.id FROM Response r
            WHERE r.user.id = :userId
            AND EXISTS (SELECT e FROM Evaluation e WHERE e.response = r AND e.evalStatus = 'DONE')
        )
        ORDER BY m.difficultyLevel ASC
    """)
    List<Mission> findNextForUser(UUID userId);
}

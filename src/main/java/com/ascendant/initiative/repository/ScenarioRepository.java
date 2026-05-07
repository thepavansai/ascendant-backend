package com.ascendant.initiative.repository;

import com.ascendant.initiative.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, UUID> {
    Optional<Scenario> findByMissionId(UUID missionId);
}

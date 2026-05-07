package com.ascendant.initiative.repository;

import com.ascendant.initiative.model.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResponseRepository extends JpaRepository<Response, UUID> {
    Page<Response> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    List<Response> findByUserIdAndMissionId(UUID userId, UUID missionId);
}

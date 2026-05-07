package com.ascendant.initiative.repository;

import com.ascendant.initiative.model.PlayerProfile;
import com.ascendant.initiative.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, UUID> {
    Optional<PlayerProfile> findByUser(User user);
    Optional<PlayerProfile> findByUserId(UUID userId);
}

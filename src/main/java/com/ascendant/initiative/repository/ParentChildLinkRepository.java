package com.ascendant.initiative.repository;

import com.ascendant.initiative.model.ParentChildLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParentChildLinkRepository extends JpaRepository<ParentChildLink, UUID> {
    List<ParentChildLink> findByParentIdAndApprovedTrue(UUID parentId);
    Optional<ParentChildLink> findByParentIdAndChildId(UUID parentId, UUID childId);
    Optional<ParentChildLink> findByChildId(UUID childId);
}

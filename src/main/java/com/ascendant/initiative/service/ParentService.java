package com.ascendant.initiative.service;

import com.ascendant.initiative.exception.AppException;
import com.ascendant.initiative.model.ParentChildLink;
import com.ascendant.initiative.model.PlayerProfile;
import com.ascendant.initiative.model.User;
import com.ascendant.initiative.repository.ParentChildLinkRepository;
import com.ascendant.initiative.repository.PlayerProfileRepository;
import com.ascendant.initiative.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentService {

    private final ParentChildLinkRepository parentChildLinkRepository;
    private final PlayerProfileRepository   playerProfileRepository;
    private final UserRepository            userRepository;
    private final PlayerService             playerService;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(UUID parentId) {
        List<ParentChildLink> links =
                parentChildLinkRepository.findByParentIdAndApprovedTrue(parentId);

        List<Map<String, Object>> children = links.stream().map(link -> {
            User child = link.getChild();
            PlayerProfile profile = playerProfileRepository
                    .findByUserId(child.getId()).orElse(null);

            Map<String, Object> weekly = Map.of();
            try {
                weekly = playerService.getWeeklyStats(child.getId());
            } catch (Exception ignored) {}

            return Map.<String, Object>of(
                    "child_id",       child.getId(),
                    "child_name",     child.getName(),
                    "level",          profile != null ? profile.getLevel() : 1,
                    "xp",             profile != null ? profile.getXp() : 0,
                    "streak_days",    profile != null ? profile.getStreakDays() : 0,
                    "last_active",    profile != null ? profile.getLastActive() : null,
                    "identity_type",  profile != null ? profile.getIdentityType() : null,
                    "weekly_summary", weekly
            );
        }).toList();

        return Map.of("parent_id", parentId, "children", children);
    }

    @Transactional
    public void approveChild(UUID parentId, UUID childId) {
        ParentChildLink link = parentChildLinkRepository
                .findByParentIdAndChildId(parentId, childId)
                .orElseThrow(() -> AppException.notFound(
                        "No pending link found for parent " + parentId + " and child " + childId));
        link.setApproved(true);
        parentChildLinkRepository.save(link);
        log.info("Parent {} approved child {}", parentId, childId);
    }

    @Transactional
    public void createLink(String parentEmail, String childEmail) {
        User parent = userRepository.findByEmail(parentEmail)
                .orElseThrow(() -> AppException.notFound("No user found with email: " + parentEmail));
        User child = userRepository.findByEmail(childEmail)
                .orElseThrow(() -> AppException.notFound("No user found with email: " + childEmail));

        if (parentChildLinkRepository.findByParentIdAndChildId(
                parent.getId(), child.getId()).isPresent()) {
            throw AppException.conflict("Link already exists between these accounts");
        }

        ParentChildLink link = ParentChildLink.builder()
                .parent(parent).child(child).approved(false).build();
        parentChildLinkRepository.save(link);
        log.info("Parent-child link created: {} → {}", parentEmail, childEmail);
    }
}

package com.ascendant.initiative.controller;

import com.ascendant.initiative.exception.AppException;
import com.ascendant.initiative.model.User;
import com.ascendant.initiative.service.ParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    private void verifyAccess(User currentUser, UUID targetParentId) {
        if (currentUser.getRole() == User.Role.ADMIN) return;
        if (currentUser.getId().equals(targetParentId)) return;
        throw AppException.forbidden("You do not have permission to access this parent's data");
    }

    @GetMapping("/{parentId}/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @PathVariable UUID parentId,
            @AuthenticationPrincipal User currentUser) {
        verifyAccess(currentUser, parentId);
        return ResponseEntity.ok(parentService.getDashboard(parentId));
    }

    @PostMapping("/{parentId}/approve/{childId}")
    public ResponseEntity<Map<String, String>> approveChild(
            @PathVariable UUID parentId,
            @PathVariable UUID childId,
            @AuthenticationPrincipal User currentUser) {
        verifyAccess(currentUser, parentId);
        parentService.approveChild(parentId, childId);
        return ResponseEntity.ok(Map.of("message", "Child account approved successfully."));
    }

    @PostMapping("/link")
    public ResponseEntity<Map<String, String>> createLink(
            @RequestBody Map<String, String> body) {
        // Link creation doesn't need IDOR check because it relies on the provided email
        // and doesn't expose sensitive info.
        parentService.createLink(
                body.get("parent_email"),
                body.get("child_email")
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Link created. Approval email sent to parent."));
    }
}

package com.ascendant.initiative.service;

import com.ascendant.initiative.dto.auth.AuthResponse;
import com.ascendant.initiative.dto.auth.LoginRequest;
import com.ascendant.initiative.dto.auth.RegisterRequest;
import com.ascendant.initiative.exception.AppException;
import com.ascendant.initiative.model.ParentChildLink;
import com.ascendant.initiative.model.PlayerProfile;
import com.ascendant.initiative.model.User;
import com.ascendant.initiative.repository.ParentChildLinkRepository;
import com.ascendant.initiative.repository.PlayerProfileRepository;
import com.ascendant.initiative.repository.UserRepository;
import com.ascendant.initiative.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final ParentChildLinkRepository parentChildLinkRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw AppException.conflict("Email already registered: " + req.getEmail());
        }

        if (req.getRole() == User.Role.CHILD && (req.getParentEmail() == null || req.getParentEmail().isBlank())) {
            throw AppException.badRequest("parentEmail is required when role is CHILD");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole())
                .build();
        userRepository.save(user);

        // Create player profile for child accounts
        if (req.getRole() == User.Role.CHILD) {
            PlayerProfile profile = PlayerProfile.builder().user(user).build();
            playerProfileRepository.save(profile);

            // Link to parent if parent exists
            Optional<User> parentOpt = userRepository.findByEmail(req.getParentEmail());
            if (parentOpt.isPresent()) {
                ParentChildLink link = ParentChildLink.builder()
                        .parent(parentOpt.get())
                        .child(user)
                        .approved(false)
                        .build();
                parentChildLinkRepository.save(link);
                log.info("Created pending parent-child link: {} -> {}", req.getParentEmail(), user.getEmail());
            }
        }

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> AppException.unauthorized("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw AppException.unauthorized("Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(String refreshToken) {
        try {
            var claims = jwtUtil.parseToken(refreshToken);
            if (!"refresh".equals(claims.get("type", String.class))) {
                throw AppException.unauthorized("Invalid refresh token");
            }
            var userId = java.util.UUID.fromString(claims.getSubject());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> AppException.unauthorized("User not found"));
            return buildAuthResponse(user);
        } catch (io.jsonwebtoken.JwtException e) {
            throw AppException.unauthorized("Refresh token expired or invalid");
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .createdAt(user.getCreatedAt())
                        .build())
                .build();
    }
}

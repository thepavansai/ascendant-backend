package com.ascendant.initiative.service;

import com.ascendant.initiative.dto.auth.AdminLoginRequest;
import com.ascendant.initiative.dto.auth.AuthResponse;
import com.ascendant.initiative.exception.AppException;
import com.ascendant.initiative.model.User;
import com.ascendant.initiative.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthService {

    public static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final JwtUtil jwtUtil;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public boolean isAdminEmail(String email) {
        return adminEmail.equalsIgnoreCase(email);
    }

    public boolean isAdminId(UUID userId) {
        return ADMIN_ID.equals(userId);
    }

    public AuthResponse authenticate(String email, String password) {
        if (!isAdminEmail(email) || !adminPassword.equals(password)) {
            throw AppException.unauthorized("Invalid credentials");
        }
        log.info("Admin login successful: {}", adminEmail);
        return buildAdminAuthResponse();
    }

    public AuthResponse adminLogin(AdminLoginRequest req) {
        return authenticate(req.getEmail(), req.getPassword());
    }

    public AuthResponse buildAdminAuthResponse() {
        String accessToken = jwtUtil.generateAccessToken(ADMIN_ID, adminEmail, "ADMIN");
        String refreshToken = jwtUtil.generateRefreshToken(ADMIN_ID);

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(AuthResponse.UserDto.builder()
                        .id(ADMIN_ID)
                        .name("Admin")
                        .email(adminEmail)
                        .role(User.Role.ADMIN)
                        .build())
                .build();
    }
}

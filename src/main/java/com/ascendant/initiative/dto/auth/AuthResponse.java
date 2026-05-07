package com.ascendant.initiative.dto.auth;

import com.ascendant.initiative.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private UserDto user;

    @Data @Builder
    public static class UserDto {
        private UUID id;
        private String name;
        private String email;
        private User.Role role;
        private LocalDateTime createdAt;
    }
}

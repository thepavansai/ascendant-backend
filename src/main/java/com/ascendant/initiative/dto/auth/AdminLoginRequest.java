package com.ascendant.initiative.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminLoginRequest {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;
}

package com.ascendant.initiative.dto.auth;

import com.ascendant.initiative.model.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(min = 2, max = 100)
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, max = 100)
    private String password;

    @NotNull
    private User.Role role;

    private String parentEmail; // Required when role == CHILD
}

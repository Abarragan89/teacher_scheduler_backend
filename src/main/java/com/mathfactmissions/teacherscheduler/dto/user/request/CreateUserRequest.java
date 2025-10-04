package com.mathfactmissions.teacherscheduler.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateUserRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email
) { }

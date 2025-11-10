package com.mathfactmissions.teacherscheduler.dto.magicLink.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record MagicLinkRequest(
        @Email
        @NotBlank
        String email
) {}
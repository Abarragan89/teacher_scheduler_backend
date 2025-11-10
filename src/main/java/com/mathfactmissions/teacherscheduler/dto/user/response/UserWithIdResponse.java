package com.mathfactmissions.teacherscheduler.dto.user.response;

import com.mathfactmissions.teacherscheduler.model.Role;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@Builder
public record UserWithIdResponse(
        UUID id,
        String email,
        String username,
        Set<Role> roles
) {
}

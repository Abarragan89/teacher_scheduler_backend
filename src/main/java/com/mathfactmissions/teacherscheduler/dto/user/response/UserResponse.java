package com.mathfactmissions.teacherscheduler.dto.user.response;

import com.mathfactmissions.teacherscheduler.model.Role;
import lombok.Builder;

import java.util.Set;

@Builder
public record UserResponse(
        String email,
        String username,
        Set<Role> roles
)
{ }

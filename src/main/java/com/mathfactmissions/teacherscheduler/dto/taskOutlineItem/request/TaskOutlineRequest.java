package com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TaskOutlineRequest(

        @NotNull(message = "missing indent level")
        Integer indentLevel,

        @NotNull(message = "missing position")
        Integer position,

        @NotNull(message = "missing taskId")
        UUID taskId,

        @NotNull(message = "missing text")
        String text
) { }

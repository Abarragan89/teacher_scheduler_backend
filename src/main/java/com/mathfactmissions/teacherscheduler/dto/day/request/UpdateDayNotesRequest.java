package com.mathfactmissions.teacherscheduler.dto.day.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UpdateDayNotesRequest(
    @NotNull(message = "Day Id required")
    UUID dayId,
    JsonNode notes
) {
}

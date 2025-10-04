package com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.response;


import lombok.Builder;

import java.util.UUID;

@Builder
public record TaskOutlineResponse(
         UUID id,
         String text,
         Boolean completed,
         Integer indentLevel,
         Integer position
) {}

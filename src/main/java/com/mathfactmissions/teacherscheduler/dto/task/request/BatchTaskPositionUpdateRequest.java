package com.mathfactmissions.teacherscheduler.dto.task.request;

import lombok.Builder;

import java.util.List;

// BatchTaskPositionUpdateRequest.java
@Builder
public record BatchTaskPositionUpdateRequest(
        List<TaskPositionUpdateDTO> tasks
) {}
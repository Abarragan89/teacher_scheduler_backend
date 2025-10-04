package com.mathfactmissions.teacherscheduler.dto.task.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateTaskRequest(
         Integer position,
         String title,
         UUID scheduleId
) {};

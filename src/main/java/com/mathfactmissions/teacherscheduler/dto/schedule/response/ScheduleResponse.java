package com.mathfactmissions.teacherscheduler.dto.schedule.response;

import com.mathfactmissions.teacherscheduler.dto.task.response.TaskResponse;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record ScheduleResponse(
        UUID id,
        List<TaskResponse> tasks
) { }

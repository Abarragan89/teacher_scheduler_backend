package com.mathfactmissions.teacherscheduler.dto.task.response;

import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.response.TaskOutlineResponse;
import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record TaskResponse(
         String title,
         Integer position,
         UUID id,
         Boolean completed,
         List<TaskOutlineResponse> outlineItems
) {}

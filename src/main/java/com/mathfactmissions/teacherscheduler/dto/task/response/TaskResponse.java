package com.mathfactmissions.teacherscheduler.dto.task.response;

import com.mathfactmissions.teacherscheduler.model.TaskOutlineItem;
import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record TaskResponse(
         String title,
         Integer position,
         UUID id,
         Boolean completed,
         List<TaskOutlineItem> outlineItems
) {}

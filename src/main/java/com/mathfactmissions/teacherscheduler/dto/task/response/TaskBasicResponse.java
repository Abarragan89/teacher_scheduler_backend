package com.mathfactmissions.teacherscheduler.dto.task.response;

import com.mathfactmissions.teacherscheduler.model.Task;
import java.util.UUID;
import lombok.Builder;

@Builder
public record TaskBasicResponse(
        UUID id,
        String title,
        Integer position,
        Boolean completed
) {
    public static TaskBasicResponse fromEntity(Task task) {
        return TaskBasicResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .position(task.getPosition())
                .completed(task.getCompleted())
                .build();
    }
}
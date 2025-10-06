package com.mathfactmissions.teacherscheduler.dto.task.response;

import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.response.TaskOutlineResponse;
import com.mathfactmissions.teacherscheduler.model.Task;
import lombok.Builder;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record TaskResponse(
        String title,
        Integer position,
        UUID id,
        Boolean completed,
        List<TaskOutlineResponse> outlineItems
) {

    public static TaskResponse fromEntity(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .position(task.getPosition())
                .completed(task.getCompleted())
                .outlineItems(
                        task.getOutlineItems() == null ? List.of() :
                                task.getOutlineItems().stream()
                                        .map(TaskOutlineResponse::fromEntity)
                                        .collect(Collectors.toList())
                )
                .build();
    }
}

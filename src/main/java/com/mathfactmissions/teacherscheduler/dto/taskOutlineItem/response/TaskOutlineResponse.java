package com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.response;

import com.mathfactmissions.teacherscheduler.model.TaskOutlineItem;
import lombok.Builder;
import java.util.UUID;

@Builder
public record TaskOutlineResponse(
        UUID id,
        String text,
        Boolean completed,
        Integer indentLevel,
        Integer position
) {

    public static TaskOutlineResponse fromEntity(TaskOutlineItem item) {
        return TaskOutlineResponse.builder()
                .id(item.getId())
                .text(item.getText())
                .completed(item.getCompleted())
                .indentLevel(item.getIndentLevel())
                .position(item.getPosition())
                .build();
    }
}

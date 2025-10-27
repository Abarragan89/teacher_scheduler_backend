package com.mathfactmissions.teacherscheduler.dto.schedule.response;

import com.mathfactmissions.teacherscheduler.dto.task.response.TaskResponse;
import com.mathfactmissions.teacherscheduler.model.Schedule;
import lombok.Builder;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record ScheduleResponse(
        UUID id,
        List<TaskResponse> tasks
) {

    public static ScheduleResponse fromEntity(Schedule schedule) {
        return ScheduleResponse.builder()
            .id(schedule.getId())
            .tasks(
                schedule.getTasks() == null ? List.of() :
                    schedule.getTasks().stream()
                        .map(TaskResponse::fromEntity)
                        .collect(Collectors.toList())
            )
            .build();
    }
}

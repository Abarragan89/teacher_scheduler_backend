package com.mathfactmissions.teacherscheduler.dto.task.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateTaskRequest {

    public Integer position;
    public String title;
    public UUID scheduleId;

}

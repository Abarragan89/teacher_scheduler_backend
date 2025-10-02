package com.mathfactmissions.teacherscheduler.dto.task.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TaskResponse {

    private String title;
    private Integer position;
    private UUID id;
    private Boolean completed;
}

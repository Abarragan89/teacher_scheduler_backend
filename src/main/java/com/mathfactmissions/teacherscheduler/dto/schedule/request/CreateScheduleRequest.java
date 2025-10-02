package com.mathfactmissions.teacherscheduler.dto.schedule.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateScheduleRequest {

    @NotNull(message = "Missing day ID.")
    private UUID dayId;

}

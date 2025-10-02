package com.mathfactmissions.teacherscheduler.dto.schedule.response;

import java.time.Instant;
import java.util.UUID;

public class ScheduleResponse {

    private UUID id;

    public ScheduleResponse(UUID id) {
        this.id = id;
//        this.createdAt = createdAt;
//        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
//    public Instant getCreatedAt() { return createdAt; }
//    public Instant getUpdatedAt() { return updatedAt; }


}

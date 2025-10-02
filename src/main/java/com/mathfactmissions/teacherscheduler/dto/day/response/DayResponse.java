package com.mathfactmissions.teacherscheduler.dto.day.response;

import com.mathfactmissions.teacherscheduler.dto.schedule.response.ScheduleResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class DayResponse {

    private UUID id;
    private LocalDate dayDate;
    private List<ScheduleResponse> schedules;

    public DayResponse(UUID id, LocalDate dayDate, List<ScheduleResponse> schedules) {
        this.id = id;
        this.dayDate = dayDate;
        this.schedules = schedules;
    }

    public UUID getId() { return id; }
    public LocalDate getDayDate() { return dayDate; }
    public List<ScheduleResponse> getSchedules() { return schedules; }

    @Override
    public String toString() {
        return "DayResponse{id=" + id + ", dayDate=" + dayDate + ", schedules=" + schedules + '}';
    }
}

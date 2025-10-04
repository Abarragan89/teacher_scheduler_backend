package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.schedule.response.ScheduleResponse;
import com.mathfactmissions.teacherscheduler.service.ScheduleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController()
@RequestMapping("/schedule")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

//    @GetMapping("/{scheduleId}")
//    public ScheduleResponse getScheduleWithTasks(@PathVariable UUID scheduleId) {
//        return scheduleService.findScheduleAndTasks(scheduleId);
//
//    }

}

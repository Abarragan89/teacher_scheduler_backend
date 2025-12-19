package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.day.request.DayRequest;
import com.mathfactmissions.teacherscheduler.dto.day.response.DayResponse;
import com.mathfactmissions.teacherscheduler.dto.schedule.request.MoveScheduleRequest;
import com.mathfactmissions.teacherscheduler.model.Day;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.DayService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.UUID;

@RestController()
@RequestMapping("/days")
public class DaysController {

    private final DayService dayService;

    @Autowired
    public DaysController(DayService dayService) {
        this.dayService = dayService;
    }

    @GetMapping("/single-day/{dayId}")
    public ResponseEntity<DayResponse> findDay(
        @PathVariable UUID dayId,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
         DayResponse day =  dayService.findSingleDay(dayId, userInfo.getId());
         return ResponseEntity.ok(day);
    }


    @PostMapping("/find-or-create")
    public ResponseEntity<DayResponse> findOrCreateDay(
        @AuthenticationPrincipal UserPrincipal userInfo,
        @RequestBody @Valid DayRequest request
    ) {

        UUID userId = userInfo.getId();

        // Call the service to find or create the day
        DayResponse day = dayService.createOrFindDay(userId, request.dayDate());
        return ResponseEntity.ok(day);
    }

    @PostMapping("/move-schedule-to-date")
    public ResponseEntity<Boolean> moveScheduleToDate(
        @RequestBody @Valid MoveScheduleRequest request,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        UUID userId = userInfo.getId();

        dayService.createOrFindDayWithSchedule(userId, request.dayDate(), request.scheduleId());
        return ResponseEntity.ok(true);
    }

}

package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.day.request.DayRequest;
import com.mathfactmissions.teacherscheduler.dto.day.response.DayResponse;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.DayService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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


    @PostMapping("/find-or-create")
    public ResponseEntity<DayResponse> findOrCreateDay(@RequestBody @Valid DayRequest request) {

        // Get the currently authenticated user ID
        UserPrincipal userInfo = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        UUID userId = userInfo.getId();

        // Call the service to find or create the day
        DayResponse day = dayService.createOrFindDay(userId, request.dayDate());
        return ResponseEntity.ok(day);
    }

}

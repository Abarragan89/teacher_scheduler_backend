package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.DayRequest;
import com.mathfactmissions.teacherscheduler.model.Day;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.DayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController()
@RequestMapping("/days")
public class DaysController {

    private final DayService dayService;

    @Autowired
    public DaysController(DayService dayService) {
        this.dayService = dayService;
    }


    @GetMapping("/get-all-days")
    public ResponseEntity<List<Day>> findAllDays() {
        List<Day>  days = dayService.findAllDays();
        return ResponseEntity.ok(days);
    }

    @PostMapping("/find-or-create")
    public ResponseEntity<Day> findOrCreateDay(@RequestBody DayRequest request) {

        UserPrincipal userInfo = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        request.setUserId(userInfo.getId());

        Day day = dayService.createOrFindDay(userInfo.getId(), request.getDayDate());

        return ResponseEntity.ok(day);
    }

}

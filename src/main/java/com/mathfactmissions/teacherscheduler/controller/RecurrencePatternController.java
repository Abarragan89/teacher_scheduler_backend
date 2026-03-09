package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.RecurrencePatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/recurrence")
@RequiredArgsConstructor
public class RecurrencePatternController {
    
    private final RecurrencePatternService recurrencePatternService;
    
    @GetMapping("/todos-in-range/{startDate}/{endDate}")
    public ResponseEntity<List<TodoResponse>> getRecurringTodosInRange(
        @PathVariable LocalDate startDate,
        @PathVariable LocalDate endDate,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        return ResponseEntity.ok(
            recurrencePatternService.getRecurringTodosInRange(
                userInfo.getId(), startDate, endDate
            )
        );
    }
    
    @GetMapping("/next-occurrences")
    public ResponseEntity<List<TodoResponse>> getNextOccurrences(
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        return ResponseEntity.ok(
            recurrencePatternService.getNextOccurrenceForEachPattern(userInfo.getId())
        );
    }
}
package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.recurringTodos.request.DeleteOccurrenceRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.RecurrencePatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
    
    // This gets recurrence todos and normal todos
    @GetMapping("/todos-for-date/{date}")
    public ResponseEntity<List<TodoResponse>> getTodosForDate(
        @PathVariable LocalDate date,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        return ResponseEntity.ok(
            recurrencePatternService.getTodosForDate(userInfo.getId(), date, userInfo.getTimeZone())
        );
    }
    
    @DeleteMapping("/delete-recurrence/{patternId}")
    public ResponseEntity<String> deleteRecurrencePattern(
        @PathVariable UUID patternId,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        
        recurrencePatternService.deleteRecurrencePattern(patternId, userInfo.getId());
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/delete-single-occurrence")
    public ResponseEntity<Void> deleteOccurrence(
        @RequestBody DeleteOccurrenceRequest request,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        recurrencePatternService.deleteOccurrence(request);
        return ResponseEntity.noContent().build();
    }
}
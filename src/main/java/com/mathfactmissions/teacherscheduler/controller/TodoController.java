package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.todo.request.CreateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.request.UpdateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.RecurrencePatternService;
import com.mathfactmissions.teacherscheduler.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/todo")
public class TodoController {
    
    public final TodoService todoService;
    public final RecurrencePatternService recurrencePatternService;
    
    public TodoController(TodoService todoService, RecurrencePatternService recurrencePatternService) {
        this.todoService = todoService;
        this.recurrencePatternService = recurrencePatternService;
    }
    
    @PostMapping("/create-list-item")
    public ResponseEntity<?> createListItem(
        @Valid @RequestBody CreateTodoRequest request,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        try {
            if (request.isRecurring()) {
                List<TodoResponse> generatedTodos = recurrencePatternService.createRecurrencePattern(request, userInfo.getId());
                return ResponseEntity.ok(generatedTodos);
            } else {
                TodoResponse newTodo = todoService.createTodoItem(request, userInfo.getId());
                return ResponseEntity.ok(newTodo);
            }
        } catch (Exception e) {
            System.out.println("=== CONTROLLER ERROR ===");
            throw e;
        }
    }
    
    @PutMapping("/update-list-item")
    public ResponseEntity<TodoResponse> updateListItem(
        @Valid @RequestBody UpdateTodoRequest request,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        
        TodoResponse updatedTodo = todoService.updateTodoItem(
            request.todoId(),
            request.todoText(),
            request.completed(),
            request.priority(),
            request.dueDate(),
            request.todoListId(),
            userInfo.getId()
        );
        
        return ResponseEntity.ok(updatedTodo);
    }
    
    @GetMapping("/get-recurring-todos-in-range/{startDate}/{endDate}")
    public ResponseEntity<List<TodoResponse>> getRecurringTodosInRange(
        @PathVariable LocalDate startDate,
        @PathVariable LocalDate endDate,
        @AuthenticationPrincipal UserPrincipal userInfo
    
    ) {
        System.out.println("start Date in controller " + startDate);
        System.out.println("End Date in controller " + endDate);
        List<TodoResponse> recurring = todoService.getRecurringTodosInRange(userInfo.getId(), startDate, endDate);
        return ResponseEntity.ok(recurring);
        
    }
    
    
    @DeleteMapping("/delete-list-item/{todoId}")
    public ResponseEntity<String> deleteListItem(@PathVariable UUID todoId) {
        try {
            boolean deleted = todoService.deleteListItem(todoId);
            
            if (!deleted) {
                return ResponseEntity.ok("Todo deleted");
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete todos");
        }
        
        return ResponseEntity.noContent().build(); // 204 No Content
    }
    
    
}

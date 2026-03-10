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
    public ResponseEntity<List<TodoResponse>> createListItem(
        @Valid @RequestBody CreateTodoRequest request,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        if (request.isRecurring()) {
            List<TodoResponse> virtualTodos = recurrencePatternService
                .createRecurrencePattern(request, userInfo.getId());
            return ResponseEntity.ok(virtualTodos);
        }
        
        return ResponseEntity.ok(List.of(todoService.createTodoItem(request, userInfo.getId())));
    }
    
    @PutMapping("/update-list-item")
    public ResponseEntity<TodoResponse> updateListItem(
        @Valid @RequestBody UpdateTodoRequest request,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        
        if (request.patternId() != null) {
            return ResponseEntity.ok(
                recurrencePatternService.updateVirtualOccurrence(request)
            );
        }
        
        TodoResponse updatedTodo = todoService.updateTodoItem(
            UUID.fromString(request.todoId()),
            request.todoText(),
            request.completed(),
            request.priority(),
            request.dueDate(),
            request.todoListId(),
            userInfo.getId()
        );
        
        return ResponseEntity.ok(updatedTodo);
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
        
        return ResponseEntity.noContent().build();
    }
    
    
}

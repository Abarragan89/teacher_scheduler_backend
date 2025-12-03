package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.todo.request.CreateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.request.UpdateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import com.mathfactmissions.teacherscheduler.service.RecurrencePatternService;
import com.mathfactmissions.teacherscheduler.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<TodoResponse> createListItem(@Valid @RequestBody CreateTodoRequest request) {
        System.out.println("=== CONTROLLER CALLED ===");
        System.out.println("Request: " + request);
        System.out.println("TodoListId: " + request.todoListId());
        System.out.println("IsRecurring: " + request.isRecurring());

        try {
            TodoResponse newTodo = todoService.createTodoItem(request);
            System.out.println("=== CONTROLLER RETURNING ===");
            return ResponseEntity.ok(newTodo);
        } catch (Exception e) {
            System.out.println("=== CONTROLLER ERROR ===");
            System.out.println("TodoListId: " + request.todoListId());
            System.out.println("IsRecurring: " + request.isRecurring());
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/update-list-item")
    public ResponseEntity<TodoResponse> updateListItem(@Valid @RequestBody UpdateTodoRequest request) {

        TodoResponse updatedTodo = todoService.updateTodoItem(
                request.todoId(),
                request.todoText(),
                request.completed(),
                request.priority(),
                request.dueDate(),
                request.todoListId()
        );

        return ResponseEntity.ok(updatedTodo);
    }

    @DeleteMapping("/delete-list-item/{todoId}")
    public ResponseEntity<Void> deleteListItem(@PathVariable UUID todoId) {

        boolean deleted = todoService.deleteListItem(todoId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build(); // 204 No Content
    }



}

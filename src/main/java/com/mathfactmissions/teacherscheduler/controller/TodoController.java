package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.todo.request.CreateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.request.UpdateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/todo")
public class TodoController {

    public final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping("/create-list-item")
    public ResponseEntity<TodoResponse> createListItem(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse newTodo =  todoService
            .createTodoItem(
                request.todoListId(),
                request.todoText(),
                request.dueDate(),
                request.priority()
            );

        return ResponseEntity.ok(newTodo);
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

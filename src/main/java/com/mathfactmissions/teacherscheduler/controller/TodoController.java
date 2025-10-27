package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.todo.request.CreateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.request.UpdateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/todo")
public class TodoController {

    public final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping("/create-list-item")
    public ResponseEntity<TodoResponse> createListItem(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse newTodo =  todoService.createTodoItem(request.todoListId(), request.todoText());

        return ResponseEntity.ok(newTodo);
    }

    @PutMapping("/update-list-item")
    public ResponseEntity<TodoResponse> updateListItem(@Valid @RequestBody UpdateTodoRequest request) {

        System.out.println("todo id " + request.todoId());
        TodoResponse updatedTodo =  todoService.updateTodoItem(
                request.todoId(),
                request.todoText(),
                request.completed(),
                request.priority()
        );

        return ResponseEntity.ok(updatedTodo);
    }

}

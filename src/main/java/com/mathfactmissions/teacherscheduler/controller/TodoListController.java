package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.todoList.request.CreateTodoListRequest;
import com.mathfactmissions.teacherscheduler.dto.todoList.request.UpdateTodoListTitleRequest;
import com.mathfactmissions.teacherscheduler.dto.todoList.response.TodoListResponse;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.TodoListService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/todo-list")
public class TodoListController {

    private final TodoListService todoListService;

    public TodoListController(
        TodoListService todoListService
    ) {
        this.todoListService = todoListService;
    }

    @GetMapping("/get-all-lists")
    public List<TodoListResponse> getAllLists() {

        UserPrincipal user = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        UUID userId = user.getId();

        return todoListService.getTodoLists(userId);
    }


    @PostMapping("/create-list")
    public TodoListResponse createList(@RequestBody @Valid CreateTodoListRequest request) {
        // Get the currently authenticated user ID
        UserPrincipal userInfo = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        UUID userId = userInfo.getId();

        TodoList newList = todoListService.createNewList(userId, request.listName());

        return TodoListResponse.fromEntity(newList);
    }

    @PutMapping("/update-list-title")
    public TodoListResponse updateListTitle(@RequestBody @Valid UpdateTodoListTitleRequest request) {
        TodoList newList = todoListService.updateListTitle(request.todoListId(), request.listName());
        return TodoListResponse.fromEntity(newList);
    }

    @DeleteMapping("/delete-list/{todoListId}")
    public ResponseEntity<Void> deleteListItem(@PathVariable UUID todoListId) {
        boolean deleted = todoListService.deleteListItem(todoListId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PutMapping("/set-default-list/{todoListId}")
    public ResponseEntity<Boolean> setDefaultList(@PathVariable UUID todoListId) {
        UserPrincipal userInfo = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        UUID userId = userInfo.getId();

        Boolean defaultList = todoListService.setDefaultList(userId, todoListId);

        return ResponseEntity.ok(defaultList);
    }
}

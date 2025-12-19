package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.todoList.request.CreateTodoListRequest;
import com.mathfactmissions.teacherscheduler.dto.todoList.request.UpdateTodoListTitleRequest;
import com.mathfactmissions.teacherscheduler.dto.todoList.response.TodoListResponse;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.TodoListService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public List<TodoListResponse> getAllLists(
    @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        try {
            return todoListService.getTodoLists(userInfo.getId());
        } catch (Exception e) {
            System.out.println("Error getting TodoLists: " + e.getMessage());
            throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Failed to load todo lists"
            );
        }
    }


    @PostMapping("/create-list")
    public TodoListResponse createList(
        @RequestBody @Valid CreateTodoListRequest request,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {

        TodoList newList = todoListService.createNewList(userInfo.getId(), request.listName());

        return TodoListResponse.fromEntity(newList);
    }

    @PutMapping("/update-list-title")
    public TodoListResponse updateListTitle(
        @RequestBody @Valid UpdateTodoListTitleRequest request,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        TodoList newList = todoListService.updateListTitle(request.todoListId(), request.listName(), userInfo.getId());
        return TodoListResponse.fromEntity(newList);
    }

    @DeleteMapping("/delete-list/{todoListId}")
    public ResponseEntity<Void> deleteListItem(
        @PathVariable UUID todoListId,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        boolean deleted = todoListService.deleteListItem(todoListId, userInfo.getId());

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

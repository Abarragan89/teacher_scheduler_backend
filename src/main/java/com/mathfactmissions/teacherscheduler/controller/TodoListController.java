package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.todoList.request.CreateTodoListRequest;
import com.mathfactmissions.teacherscheduler.dto.todoList.request.UpdateTodoListTitleRequest;
import com.mathfactmissions.teacherscheduler.dto.todoList.response.TodoListResponse;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.TodoListService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<List<TodoListResponse>> getAllLists(
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        List<TodoListResponse> lists = todoListService.getTodoLists(userInfo.getId(), userInfo.getTimeZone());
        return ResponseEntity.ok(lists);
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
    public ResponseEntity<String> deleteListItem(
        @PathVariable UUID todoListId,
        @AuthenticationPrincipal UserPrincipal userInfo
    ) {
        todoListService.deleteListItem(todoListId, userInfo.getId());
        
        return ResponseEntity.ok("List deleted successfully");
    }
    
    @PutMapping("/set-default-list/{todoListId}")
    public ResponseEntity<String> setDefaultList(@PathVariable UUID todoListId) {
        UserPrincipal userInfo = (UserPrincipal) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
        UUID userId = userInfo.getId();
        
        todoListService.setDefaultList(userId, todoListId);
        
        return ResponseEntity.ok("Default successfully set");
    }
}

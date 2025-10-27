package com.mathfactmissions.teacherscheduler.service;
import com.mathfactmissions.teacherscheduler.dto.todoList.response.TodoListResponse;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.TodoListRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TodoListService {

    private final TodoListRepository todoListRepository;
    private final UserService userService;

    public  TodoListService(
        TodoListRepository todoListRepository,
        UserService userService
    ) {
        this.todoListRepository = todoListRepository;
        this.userService = userService;
    }


    public TodoList createNewList(UUID userId, String listName) {
        // Find user
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("No user found"));

        TodoList newList = TodoList.builder()
                .user(user)
                .listName(listName)
                .build();
        return todoListRepository.save(newList);
    }


    public TodoList updateListTitle(UUID todoListId, String listName) {

        TodoList currentList = todoListRepository.findById(todoListId)
                .orElseThrow(() -> new RuntimeException("No List Found"));

        currentList.setListName(listName);
        return todoListRepository.save(currentList);
    }

    public List<TodoListResponse> getTodoLists(UUID userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("No user found"));

        List<TodoList> todoLists = todoListRepository.findAllByUser(user)
                .orElseThrow(() -> new RuntimeException("no lists found"));

        return todoLists.stream().map(TodoListResponse::fromEntity).toList();
    }


}

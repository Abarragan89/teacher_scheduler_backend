package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.todoList.response.TodoListResponse;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.TodoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoListService {

    private final TodoListRepository todoListRepository;
    private final UserService userService;

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


    public TodoList updateListTitle(UUID todoListId, String listName, UUID userId) {

        TodoList currentList = todoListRepository.findByIdAndUser_Id(todoListId, userId)
                .orElseThrow(() -> new RuntimeException("No List Found"));

        currentList.setListName(listName);
        return todoListRepository.save(currentList);
    }

    public List<TodoListResponse> getTodoLists(UUID userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("No user found"));

        List<TodoList> todoLists = todoListRepository.findAllByUserOrderByUpdatedAtDesc(user)
                .orElseThrow(() -> new RuntimeException("no lists found"));

        return todoLists.stream().map(TodoListResponse::fromEntity).toList();

    }


    public Boolean deleteListItem(UUID todoId, UUID userId) {
        if (!todoListRepository.existsByIdAndUser_Id(todoId, userId)) {
            return false; // nothing to delete
        }

        todoListRepository.deleteById(todoId);
        return true;
    }

    public Boolean setDefaultList(UUID userId, UUID todoListId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("no user found"));

        todoListRepository.findAllByUserOrderByUpdatedAtDesc(user)
            .ifPresent(lists ->
                lists.forEach(todoList -> {
                    todoList.setIsDefault(todoList.getId().equals(todoListId));
                    todoListRepository.save(todoList);
                })
            );
        return true;
    }
}

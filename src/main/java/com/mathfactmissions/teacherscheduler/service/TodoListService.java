package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.dto.todoList.response.TodoListResponse;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.TodoListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoListService {
    
    private final TodoListRepository todoListRepository;
    private final RecurrencePatternService recurrencePatternService;
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
    
    public List<TodoListResponse> getTodoLists(UUID userId, String timeZone) {
        User user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("No user found"));
        
        List<TodoList> lists = todoListRepository.findAllByUserOrderByUpdatedAtDesc(user)
            .orElse(Collections.emptyList());
        
        // Fetch next occurrences for all patterns
        List<TodoResponse> nextOccurrences = recurrencePatternService.getNextOccurrenceForEachPattern(userId, timeZone);
        
        // Group occurrences by todoListId
        Map<UUID, List<TodoResponse>> occurrencesByList = nextOccurrences.stream()
            .collect(Collectors.groupingBy(TodoResponse::todoListId));
        
        // Build response, merging next occurrences into their respective lists
        return lists.stream()
            .map(list -> {
                List<TodoResponse> regularTodos = list.getTodos().stream()
                    .map(TodoResponse::fromEntity)
                    .collect(Collectors.toList());
                
                // Merge in next occurrences for this list
                List<TodoResponse> recurring = occurrencesByList
                    .getOrDefault(list.getId(), Collections.emptyList());
                
                regularTodos.addAll(recurring);
                
                // Sort by dueDate
                regularTodos.sort(Comparator.comparing(
                    TodoResponse::dueDate,
                    Comparator.nullsLast(Comparator.naturalOrder())
                ));
                
                return TodoListResponse.builder()
                    .id(list.getId())
                    .listName(list.getListName())
                    .isDefault(list.getIsDefault())
                    .todos(regularTodos)
                    .build();
            })
            .toList();
    }
    
    
    public void deleteListItem(UUID todoId, UUID userId) {
        if (!todoListRepository.existsByIdAndUser_Id(todoId, userId)) {
            throw new RuntimeException("Todo list not found for id: " + todoId);
        }
        todoListRepository.deleteById(todoId);
    }
    
    public void setDefaultList(UUID userId, UUID todoListId) {
        User user = userService.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found for id: " + userId));
        
        List<TodoList> lists = todoListRepository.findAllByUserOrderByUpdatedAtDesc(user)
            .orElse(Collections.emptyList());
        
        lists.forEach(todoList ->
            todoList.setIsDefault(todoList.getId().equals(todoListId))
        );
        
        todoListRepository.saveAll(lists); // ✅ single batch save
    }
}

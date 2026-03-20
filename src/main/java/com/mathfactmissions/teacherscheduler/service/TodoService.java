package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.todo.request.CreateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.model.Todo;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.repository.TodoListRepository;
import com.mathfactmissions.teacherscheduler.repository.TodoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoService {
    
    public final TodoRepository todoRepository;
    public final TodoListRepository todoListRepository;
    
    @Transactional
    public TodoResponse createTodoItem(CreateTodoRequest request, UUID userId) {
        TodoList todoList = todoListRepository.findByIdAndUser_Id(request.todoListId(), userId)
            .orElseThrow(() -> new RuntimeException("No todo list found"));
        
        Todo todo = Todo.builder()
            .todoList(todoList)
            .text(request.todoText())
            .dueDate(request.dueDate())
            .priority(request.priority())
            .completed(false)
            .build();
        
        return TodoResponse.fromEntity(todoRepository.save(todo));
    }
    
    public TodoResponse updateTodoItem(
        UUID todoId,
        String todoText,
        Boolean completed,
        Integer priority,
        Instant dueDate,
        UUID todoListId,
        UUID userId
    ) {
        Todo currentTodo = todoRepository.findById(todoId)
            .orElseThrow(() -> new RuntimeException("no todo found"));
        
        TodoList todoList = currentTodo.getTodoList();
        
        if (!todoListId.equals(todoList.getId())) {
            todoList = todoListRepository.findByIdAndUser_Id(todoListId, userId)
                .orElseThrow(() -> new RuntimeException("no todo list found"));
        }
        
        Instant oldDueDate = currentTodo.getDueDate();
        
        currentTodo.setText(todoText);
        currentTodo.setCompleted(completed);
        currentTodo.setPriority(priority);
        currentTodo.setDueDate(dueDate);
        currentTodo.setTodoList(todoList);
        
        if (dueDate != null && !dueDate.equals(oldDueDate)) {
            currentTodo.setNotificationSent(false);
            currentTodo.setNotificationSentAt(null);
        }
        
        if (Boolean.TRUE.equals(completed)) {
            if (currentTodo.getCompletedAt() == null) {  // only set once, don't reset if already completed
                currentTodo.setCompletedAt(Instant.now());
            }
        } else {
            currentTodo.setCompletedAt(null);  // ✅ clear when uncompleted
        }
        return TodoResponse.fromEntity(todoRepository.save(currentTodo));
    }
    
    public boolean deleteListItem(UUID todoId) {
        if (!todoRepository.existsById(todoId)) {
            return false;
        }
        todoRepository.deleteById(todoId);
        return true;
    }
}

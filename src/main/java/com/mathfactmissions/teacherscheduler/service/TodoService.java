package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.todo.request.CreateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.model.Todo;
import com.mathfactmissions.teacherscheduler.model.TodoList;
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
    public final TodoListService todoListService;

    @Transactional
    public TodoResponse createTodoItem(CreateTodoRequest request) {

        TodoList todoList = todoListService.findById(request.todoListId())
            .orElseThrow(() -> new RuntimeException("No todo list found"));

        // Create the Todo
        Todo todo = Todo.builder()
            .todoList(todoList)
            .text(request.todoText())
            .dueDate(request.dueDate())
            .priority(request.priority())
            .completed(false)
            .build();

        Todo savedTodo = todoRepository.save(todo);
        return TodoResponse.fromEntity(savedTodo);
    }

    public TodoResponse updateTodoItem(
            UUID todoId,
            String todoText,
            Boolean completed,
            Integer priority,
            Instant dueDate,
            UUID todoListId
    ) {

        Todo currentTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("no todo found"));

        TodoList todoList = currentTodo.getTodoList();

        if (todoListId != todoList.getId()) {
            todoList = todoListService.findById(todoListId)
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
            currentTodo.setOverdueNotificationSent(false);
        }

        todoRepository.save(currentTodo);

        return TodoResponse.fromEntity(currentTodo);
    }

    public boolean deleteListItem(UUID todoId) {
        if (!todoRepository.existsById(todoId)) {
            return false; // nothing to delete
        }
            todoRepository.deleteById(todoId);
        return true;
    }
}

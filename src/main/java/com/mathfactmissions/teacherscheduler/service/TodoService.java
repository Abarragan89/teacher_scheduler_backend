package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.model.Todo;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.repository.TodoListRepository;
import com.mathfactmissions.teacherscheduler.repository.TodoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class TodoService {
    public final TodoRepository todoRepository;
    public final TodoListRepository todoListRepository;

    public TodoService(
            TodoRepository todoRepository,
            TodoListRepository todoListRepository
    ){
        this.todoRepository = todoRepository;
        this.todoListRepository = todoListRepository;
    }

    public TodoResponse createTodoItem(UUID todoId, String todoText) {

        TodoList todoList = todoListRepository.findById(todoId)
            .orElseThrow(() -> new RuntimeException("No todo list found"));

        Todo newTodo = Todo.builder()
                .todoList(todoList)
                .text(todoText)
                .priority(1)
        .build();

        todoRepository.save(newTodo);

        return TodoResponse.fromEntity(newTodo);
    }

    public TodoResponse updateTodoItem(
            UUID todoId,
            String todoText,
            Boolean completed,
            Integer priority,
            Instant dueDate
    ) {

        Todo currentTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("no todo found"));

        currentTodo.setText(todoText);
        currentTodo.setCompleted(completed);
        currentTodo.setPriority(priority);
        currentTodo.setDueDate(dueDate);

        Instant oldDueDate = currentTodo.getDueDate();
        if (dueDate != null && !dueDate.equals(oldDueDate)) {
            currentTodo.setNotificationSent(false);
            currentTodo.setNotificationSentAt(null);
            currentTodo.setOverdueNotificationSent(false);
            System.out.println("🔄 Due date updated for todo: " + todoId + " - notification flags reset");
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

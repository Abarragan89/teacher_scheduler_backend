package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.recurringTodos.request.CreateRecurrencePatternRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.request.CreateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import com.mathfactmissions.teacherscheduler.model.Todo;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.repository.TodoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class TodoService {
    public final TodoRepository todoRepository;
    public final TodoListService todoListService;
    public final RecurrencePatternService recurrencePatternService;

    public TodoService(
            TodoRepository todoRepository,
            TodoListService todoListService,
            RecurrencePatternService recurrencePatternService
    ){
        this.todoRepository = todoRepository;
        this.todoListService = todoListService;
        this.recurrencePatternService = recurrencePatternService;
    }

    @Transactional
    public TodoResponse createTodoItem(CreateTodoRequest request) {
        System.out.println("todo list id" + request.todoListId());

        RecurrencePattern pattern = null;

        // Create RecurrencePattern if this is a recurring todo
        if (request.isRecurring() && request.recurrencePattern() != null) {
            pattern = recurrencePatternService.createRecurrencePattern(request.recurrencePattern());
        }


        TodoList todoList = todoListService.findById(request.todoListId())
            .orElseThrow(() -> new RuntimeException("No todo list found"));

        // Create the Todo
        Todo todo = Todo.builder()
        .todoList(todoList)
        .text(request.todoText())
        .dueDate(request.dueDate())
        .priority(request.priority())
        .isRecurring(request.isRecurring())
        .recurrencePattern(pattern)
        .completed(false)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

        Todo savedTodo = todoRepository.save(todo);

        // Generate initial occurrences if recurring
        if (pattern != null) {
            recurrencePatternService.generateImmediateOccurrences(savedTodo, todoList,35);
        }

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

        Todo deletingTodo = todoRepository.findById(todoId)
            .orElseThrow(() -> new RuntimeException("no todo found"));


        if (deletingTodo.getIsRecurring()) {
            todoRepository.deleteByRecurringPattern(deletingTodo.getRecurrencePattern().getId());
        } else {
            todoRepository.deleteById(todoId);
        }


        return true;
    }


}

package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.todo.request.CreateTodoRequest;
import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.model.RecurrencePattern;
import com.mathfactmissions.teacherscheduler.model.Todo;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.repository.RecurrencePatternRepository;
import com.mathfactmissions.teacherscheduler.repository.TodoListRepository;
import com.mathfactmissions.teacherscheduler.repository.TodoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoService {

    public final TodoRepository todoRepository;
    public final RecurrenceEngine recurrenceEngine;
    public final TodoListRepository todoListRepository;
    public final RecurrencePatternRepository recurrencePatternRepository;

    @Transactional
    public TodoResponse createTodoItem(CreateTodoRequest request, UUID userId) {

        TodoList todoList = todoListRepository.findByIdAndUser_Id(request.todoListId(), userId)
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
            UUID todoListId,
            UUID userId
    ) {
        Todo currentTodo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("no todo found"));

        TodoList todoList = currentTodo.getTodoList();

        if (todoListId != todoList.getId()) {
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

    public List<TodoResponse> generateMissingTodosForPattern(RecurrencePattern pattern, LocalDate from, LocalDate to, String todoText) {
        ZoneId zone = pattern.getTimeZone();            // ZoneId stored in pattern
        LocalTime timeOfDay = pattern.getTimeOfDay();   // used when creating actual Instant

        // convert LocalDate range -> Instant half-open range [startInstant, endExclusive)
        Instant startInstant = startOfDayInstant(from, zone);
        Instant endExclusiveInstant = endExclusiveOfDayInstant(to, zone);

        // fetch existing dueDate instants for that pattern in the range
        Set<Instant> existingInstants = todoRepository.findDueDatesByPatternAndRange(
            pattern.getId(), startInstant, endExclusiveInstant
        );

        // convert existing instants into LocalDate in the pattern zone for easy membership checks
        Set<LocalDate> existingLocalDates = existingInstants.stream()
            .map(i -> i.atZone(zone).toLocalDate())
            .collect(Collectors.toSet());

        // compute expected LocalDate occurrences using your helper
        List<LocalDate> expectedDates = recurrenceEngine.calculateOccurrences(pattern, from, to);
        List<TodoResponse> generatedTodos = new ArrayList<>();
        // create todos only for missing dates
        for (LocalDate date : expectedDates) {
            if (existingLocalDates.contains(date)) continue;

            // build zoned date/time and convert to Instant for DB
            ZonedDateTime zdt = ZonedDateTime.of(date, timeOfDay, zone);
            Instant dueInstant = zdt.toInstant();

            Todo todo = Todo.builder()
                .todoList(pattern.getTodoList())
                .text(todoText)
                .recurrencePattern(pattern)
                .dueDate(dueInstant)
                .completed(false)
                .build();

            generatedTodos.add(TodoResponse.fromEntity(todoRepository.save(todo)));
        }
        return generatedTodos;
    }

    private Instant startOfDayInstant(LocalDate date, ZoneId zone) {
        return date.atStartOfDay(zone).toInstant();
    }

    // End-exclusive instant = start of (to + 1 day)
    private Instant endExclusiveOfDayInstant(LocalDate date, ZoneId zone) {
        return date.plusDays(1).atStartOfDay(zone).toInstant();
    }

    public List<TodoResponse> getRecurringTodosInRange(
    UUID userId,
    LocalDate startDate,
    LocalDate endDate
    ) {
        List<RecurrencePattern> patterns =
        recurrencePatternRepository.findByUserId(userId);

        List<TodoResponse> result = new ArrayList<>();

        for (RecurrencePattern pattern : patterns) {

            // 1. Calculate LocalDate occurrences
            List<LocalDate> expectedDates =
            recurrenceEngine.calculateOccurrences(pattern, startDate, endDate);

            if (expectedDates.isEmpty()) continue;

            ZoneId zone = pattern.getTimeZone();

            // 2. Convert all expected dates → Instants
            List<Instant> expectedInstants = expectedDates.stream()
                .map(date -> date.atTime(pattern.getTimeOfDay())
                .atZone(zone)
                .toInstant())
                .toList();

            // 3. Load all existing todos for this pattern in the full range
            Instant startInstant = startDate.atStartOfDay(zone).toInstant();
            Instant endInstant = endDate.plusDays(1).atStartOfDay(zone).toInstant();

            List<Todo> existingTodos =
            todoRepository.findTodosByPatternAndRange(
                pattern.getId(),
                startInstant,
                endInstant
            );

            // Map by dueDate for O(1) lookup
            Map<Instant, Todo> existingByDue =
            existingTodos.stream()
                .collect(Collectors.toMap(Todo::getDueDate, t -> t));

            // 4. Single loop: build or create each occurrence
            for (Instant dueInstant : expectedInstants) {
                Todo todo = existingByDue.get(dueInstant);

                if (todo == null) {
                    // 🔥 Create missing occurrence
                    Todo newTodo = Todo.builder()
                    .todoList(pattern.getTodoList())
                    .text(pattern.getText())
                    .priority(1)
                    .dueDate(dueInstant)
                    .recurrencePattern(pattern)
                    .completed(false)
                    .build();
                    result.add(TodoResponse.fromEntity(todoRepository.save(newTodo)));
                }
            }
        }
//    Only return the recurring todos that haven't already been made
        return result;
    }



}

package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.task.request.*;
import com.mathfactmissions.teacherscheduler.dto.task.response.TaskBasicResponse;
import com.mathfactmissions.teacherscheduler.dto.task.response.TaskResponse;
import com.mathfactmissions.teacherscheduler.model.Task;
import com.mathfactmissions.teacherscheduler.security.UserPrincipal;
import com.mathfactmissions.teacherscheduler.service.TaskService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Value("${app.public-token}")
    private String publicToken;

    private final TaskService taskService;

    public TaskController(
            TaskService taskService
    ) {
        this.taskService = taskService;
    }


    @PostMapping("/create")
    public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid CreateTaskRequest request) {
        TaskResponse newTask = taskService
                .addTask(request.scheduleId(), request.position());
        return ResponseEntity.ok(newTask);
    }

    @PutMapping("/toggle-complete")
    public ResponseEntity<TaskBasicResponse> toggleComplete(
            @RequestHeader(value = "X-Public-Token", required = true) String token,
            @RequestBody @Valid UpdateTaskRequest request
    ) {
        if (token == null || !token.equals(publicToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid token");
        }
        TaskBasicResponse updatedTask = taskService.toggleComplete(request.id(), request.completed());
        return ResponseEntity.ok(updatedTask);
    }


    @PutMapping("/update-task")
    public ResponseEntity<TaskBasicResponse> updateTaskTitle(@RequestBody @Valid UpdateTaskRequest request) {
        TaskBasicResponse updatedTask = taskService
                .updateTask(request.id(), request.title(), request.position(), request.completed());
        return ResponseEntity.ok(updatedTask);

    }

    @PutMapping("/batch-update-positions")
    public ResponseEntity<?> batchUpdateTaskPositions(@RequestBody @Valid BatchTaskPositionUpdateRequest request) {
        try {
            List<TaskPositionUpdateDTO> tasks = request.tasks();
            taskService.batchUpdateTaskPositions(tasks);
            return ResponseEntity.ok(Map.of(
                    "message", "Task positions updated successfully",
                    "updatedCount", tasks.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Batch update failed: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("delete/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId) {
            taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/move-to-later-date")
    public ResponseEntity<Void> moveTaskToLaterDate(@RequestBody @Valid MoveTaskToLaterDate request) {


        // Get the currently authenticated user ID
        UserPrincipal userInfo = (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        UUID userId = userInfo.getId();

        taskService.moveTaskToAnotherDate(
                userId,
                request.taskId(),
                request.newDate()
        );
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update-task-times")
    public ResponseEntity<TaskResponse> updateTaskTimes(
            @RequestBody
            @Valid
            UpdateTaskTimeRequest
            request
    ) {
        TaskResponse task = taskService.updateTaskTime(
                request.taskId(),
                request.startTime(),
                request.endTime()
        );

        return ResponseEntity.ok(task);
    }
}

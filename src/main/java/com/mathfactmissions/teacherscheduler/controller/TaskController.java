package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.task.request.CreateTaskRequest;
import com.mathfactmissions.teacherscheduler.dto.task.request.UpdateTaskRequest;
import com.mathfactmissions.teacherscheduler.dto.task.response.TaskBasicResponse;
import com.mathfactmissions.teacherscheduler.dto.task.response.TaskResponse;
import com.mathfactmissions.teacherscheduler.model.Task;
import com.mathfactmissions.teacherscheduler.service.TaskService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(
            TaskService taskService
    ) {
        this.taskService = taskService;
    }


    @PostMapping("/create")
    public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid CreateTaskRequest request) {
        TaskResponse newTask = taskService
                .addTask(request.scheduleId(), request.title(), request.position());
        return ResponseEntity.ok(newTask);
    }


    @PutMapping("/update-task")
    public ResponseEntity<TaskBasicResponse> updateTaskTitle(@RequestBody @Valid UpdateTaskRequest request) {
        TaskBasicResponse updatedTask = taskService
                .updateTask(request.id(), request.title(), request.position(), request.completed());
        return ResponseEntity.ok(updatedTask);

    }

    @DeleteMapping("delete/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID taskId) {
            taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}

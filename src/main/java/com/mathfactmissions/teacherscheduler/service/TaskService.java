package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.task.request.TaskPositionUpdateDTO;
import com.mathfactmissions.teacherscheduler.dto.task.response.TaskBasicResponse;
import com.mathfactmissions.teacherscheduler.dto.task.response.TaskResponse;
import com.mathfactmissions.teacherscheduler.model.Schedule;
import com.mathfactmissions.teacherscheduler.model.Task;
import com.mathfactmissions.teacherscheduler.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ScheduleService scheduleService;

    public TaskService(
            TaskRepository taskRepository,
            ScheduleService scheduleService
    ) {
        this.taskRepository = taskRepository;
        this.scheduleService = scheduleService;
    }

    public Task findById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
    }

    public TaskResponse addTask(UUID scheduleId, String taskTitle, Integer position) {

        // Find parent schedule
        Schedule schedule = scheduleService.findById(scheduleId);

        // Create the Task
        Task task = new Task();
        task.setCompleted(false);
        task.setTitle(taskTitle);
        task.setPosition(position);
        task.setSchedule(schedule);
        taskRepository.save(task);

        return TaskResponse.builder()
                .completed(task.getCompleted())
                .title(task.getTitle())
                .position(task.getPosition())
                .id(task.getId())
                .build();
    }

    public TaskBasicResponse updateTask(
            UUID id,
            String title,
            Integer position,
            Boolean completed
    ) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setTitle(title);
        task.setPosition(position);
        task.setCompleted(completed);

        // You don't have to call save() if the entity is still managed, but it’s fine to be explicit
        Task updated = taskRepository.save(task);

        return TaskBasicResponse.fromEntity(updated);
    }

    public void deleteTask(UUID taskId) {
        taskRepository.deleteById(taskId);
    }

    @Transactional
    public void batchUpdateTaskPositions(List<TaskPositionUpdateDTO> taskUpdates) {
        for (TaskPositionUpdateDTO dto : taskUpdates) {
            Task task = taskRepository.findById(dto.id())
                    .orElseThrow(() -> new RuntimeException("Task not found: " + dto.id()));

            task.setTitle(dto.title());
            task.setPosition(dto.position());
            task.setCompleted(dto.completed());
            taskRepository.save(task);
        }
    }
}

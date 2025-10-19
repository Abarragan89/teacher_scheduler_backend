package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.day.response.DayResponse;
import com.mathfactmissions.teacherscheduler.dto.task.request.TaskPositionUpdateDTO;
import com.mathfactmissions.teacherscheduler.dto.task.response.TaskBasicResponse;
import com.mathfactmissions.teacherscheduler.dto.task.response.TaskResponse;
import com.mathfactmissions.teacherscheduler.model.Day;
import com.mathfactmissions.teacherscheduler.model.Schedule;
import com.mathfactmissions.teacherscheduler.model.Task;
import com.mathfactmissions.teacherscheduler.model.TaskOutlineItem;
import com.mathfactmissions.teacherscheduler.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ScheduleService scheduleService;
    private final DayService dayService;

    public TaskService(
            TaskRepository taskRepository,
            ScheduleService scheduleService,
            DayService dayService
    ) {
        this.taskRepository = taskRepository;
        this.scheduleService = scheduleService;
        this.dayService = dayService;
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

    public void moveTaskToAnotherDate(UUID userId, UUID taskId, LocalDate newDate) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("task not found"));

         DayResponse day = dayService.createOrFindDay(userId, newDate);
         Schedule schedule = scheduleService.findById(day.schedule().id());

        Task newTask = new Task();
        newTask.setCompleted(false);
        newTask.setTitle(task.getTitle());
        newTask.setPosition(task.getPosition());

        Set<TaskOutlineItem> copiedItems = task.getOutlineItems().stream()
                .map(item -> {
                    TaskOutlineItem newItem = new TaskOutlineItem();
                    newItem.setText(item.getText());
                    newItem.setPosition(item.getPosition());
                    newItem.setCompleted(false);
                    newItem.setIndentLevel(item.getIndentLevel());
                    newItem.setTask(newTask); // attach to the *new* task
                    return newItem;
                })
                .collect(Collectors.toSet());

        newTask.setOutlineItems(copiedItems);
        newTask.setSchedule(schedule);

        taskRepository.save(newTask);
    }

    public TaskResponse updateTaskTime(UUID taskId, LocalTime startTime, LocalTime endTime) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("No task found"));

        task.setStartTime(startTime);
        task.setEndTime(endTime);
        taskRepository.save(task);

        return TaskResponse.fromEntity(task);
    }
}

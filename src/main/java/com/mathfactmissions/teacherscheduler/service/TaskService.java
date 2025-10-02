package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.task.response.TaskResponse;
import com.mathfactmissions.teacherscheduler.model.Schedule;
import com.mathfactmissions.teacherscheduler.model.Task;
import com.mathfactmissions.teacherscheduler.repository.TaskRepository;
import org.springframework.stereotype.Service;

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

    public TaskResponse addTask(UUID scheduleId, String taskTitle, Integer position) {

        // Find parent schedule
        Schedule schedule = scheduleService.findById(scheduleId);

        // Create the Task
        Task task = new Task();
        task.setCompleted(false);
        task.setTitle(taskTitle);
        task.setPosition(position);
        task.setSchedule(schedule);
        Task newTask = taskRepository.save(task);

        TaskResponse response = new TaskResponse();
        response.setCompleted(false);
        response.setTitle(taskTitle);
        response.setPosition(position);
        response.setId(newTask.getId());

        return response;
    }

}

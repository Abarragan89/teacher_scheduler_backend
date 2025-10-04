package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.response.TaskOutlineResponse;
import com.mathfactmissions.teacherscheduler.model.Task;
import com.mathfactmissions.teacherscheduler.model.TaskOutlineItem;
import com.mathfactmissions.teacherscheduler.repository.TaskOutlineItemRepository;
import com.mathfactmissions.teacherscheduler.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskOutlineItemService {

    private final TaskOutlineItemRepository taskOutlineItemRepository;
    private final TaskRepository taskRepository;

    // Constructor injection — no @Autowired needed
    public TaskOutlineItemService(
            TaskOutlineItemRepository taskOutlineItemRepository,
            TaskRepository taskRepository
    ) {
        this.taskOutlineItemRepository = taskOutlineItemRepository;
        this.taskRepository = taskRepository;
    }

    public TaskOutlineResponse addTaskOutlineItem(
            UUID taskId,
            Integer position,
            Integer indentLevel,
            String text
    ) {
        // Find parent task or throw a clear exception
        Task parentTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));

        // Create new TaskOutlineItem using setters
        TaskOutlineItem newItem = new TaskOutlineItem();
        newItem.setTask(parentTask);
        newItem.setPosition(position);
        newItem.setIndentLevel(indentLevel);
        newItem.setText(text);
        newItem.setCompleted(false); // default value

        // Save to repository
        TaskOutlineItem savedItem = taskOutlineItemRepository.save(newItem);

        // Map to DTO
        return TaskOutlineResponse.builder()
                .id(savedItem.getId())
                .position(savedItem.getPosition())
                .indentLevel(savedItem.getIndentLevel())
                .text(savedItem.getText())
                .completed(savedItem.getCompleted())
                .build();
    }
}

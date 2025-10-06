package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request.OutlineItemPositionUpdateDTO;
import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.response.TaskOutlineResponse;
import com.mathfactmissions.teacherscheduler.model.Task;
import com.mathfactmissions.teacherscheduler.model.TaskOutlineItem;
import com.mathfactmissions.teacherscheduler.repository.TaskOutlineItemRepository;
import com.mathfactmissions.teacherscheduler.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
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
        return TaskOutlineResponse.fromEntity(savedItem);
    }

    public TaskOutlineResponse updateTaskOutlineItem(
            UUID id,
            String text,
            Boolean completed,
            Integer indentLevel,
            Integer position
    ) {
        TaskOutlineItem outlineItem = taskOutlineItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Outline Task Not found"));

        outlineItem.setText(text);
        outlineItem.setCompleted(completed);
        outlineItem.setIndentLevel(indentLevel);
        outlineItem.setPosition(position);

        TaskOutlineItem updatedOutlineItem = taskOutlineItemRepository.save(outlineItem);

        return TaskOutlineResponse.fromEntity(updatedOutlineItem);
    }

    public void deleteTaskItem(UUID itemId) {
        taskOutlineItemRepository.deleteById(itemId);
    }

    @Transactional
    public void batchUpdateOutlineItemPositions(List<OutlineItemPositionUpdateDTO> itemUpdates) {
        for (OutlineItemPositionUpdateDTO dto : itemUpdates) {
            TaskOutlineItem item = taskOutlineItemRepository.findById(dto.id())
                    .orElseThrow(() -> new RuntimeException("Outline item not found: " + dto.id()));

            item.setText(dto.text());
            item.setPosition(dto.position());
            item.setIndentLevel(dto.indentLevel());
            item.setCompleted(dto.completed());

            taskOutlineItemRepository.save(item);
        }
    }
}

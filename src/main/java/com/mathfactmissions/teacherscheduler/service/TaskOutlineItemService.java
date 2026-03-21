package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request.OutlineItemPositionUpdateDTO;
import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.response.TaskOutlineResponse;
import com.mathfactmissions.teacherscheduler.model.Task;
import com.mathfactmissions.teacherscheduler.model.TaskOutlineItem;
import com.mathfactmissions.teacherscheduler.repository.TaskOutlineItemRepository;
import com.mathfactmissions.teacherscheduler.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskOutlineItemService {
    
    private final TaskOutlineItemRepository taskOutlineItemRepository;
    private final TaskRepository taskRepository;
    
    public TaskOutlineResponse addTaskOutlineItem(
        UUID taskId,
        Integer position,
        Integer indentLevel,
        String text
    ) {
        // Find parent task or throw a clear exception
        Task parentTask = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        
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
    
    public TaskOutlineResponse toggleComplete(
        UUID id,
        Boolean completed
    ) {
        TaskOutlineItem outlineItem = taskOutlineItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Outline Task Not found" + id));
        
        outlineItem.setCompleted(completed);
        TaskOutlineItem updatedOutlineItem = taskOutlineItemRepository.save(outlineItem);
        return TaskOutlineResponse.fromEntity(updatedOutlineItem);
    }
    
    
    public TaskOutlineResponse updateTaskOutlineItem(
        UUID id,
        String text,
        Boolean completed,
        Integer indentLevel,
        Integer position
    ) {
        TaskOutlineItem outlineItem = taskOutlineItemRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Outline Task Not found" + id));
        
        outlineItem.setText(text);
        outlineItem.setCompleted(completed);
        outlineItem.setIndentLevel(indentLevel);
        outlineItem.setPosition(position);
        
        TaskOutlineItem updatedOutlineItem = taskOutlineItemRepository.save(outlineItem);
        
        return TaskOutlineResponse.fromEntity(updatedOutlineItem);
    }
    
    public void deleteTaskItem(UUID itemId) {
        if (!taskOutlineItemRepository.existsById(itemId)) {
            throw new RuntimeException("Outline item not found for id: " + itemId);
        }
        taskOutlineItemRepository.deleteById(itemId);
    }
    
    @Transactional
    public void batchUpdateOutlineItemPositions(List<OutlineItemPositionUpdateDTO> itemUpdates) {
        // ✅ collect and saveAll once
        List<TaskOutlineItem> updatedItems = new ArrayList<>();
        for (OutlineItemPositionUpdateDTO dto : itemUpdates) {
            TaskOutlineItem item = taskOutlineItemRepository.findById(dto.id())
                .orElseThrow(() -> new RuntimeException("Outline item not found for id: " + dto.id()));
            item.setText(dto.text());
            item.setPosition(dto.position());
            item.setIndentLevel(dto.indentLevel());
            item.setCompleted(dto.completed());
            updatedItems.add(item);
        }
        taskOutlineItemRepository.saveAll(updatedItems);
    }
}

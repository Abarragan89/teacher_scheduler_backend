package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request.BatchOutlinePositionUpdateRequest;
import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request.OutlineItemPositionUpdateDTO;
import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request.TaskOutlineRequest;
import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request.UpdateTaskOutlineItemRequest;
import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.response.TaskOutlineResponse;
import com.mathfactmissions.teacherscheduler.service.TaskOutlineItemService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("task-outline-item")
public class TaskOutlineItemController {

    private final TaskOutlineItemService taskOutlineItemService;

    public TaskOutlineItemController(
            TaskOutlineItemService taskOutlineItemService
    ) {
        this.taskOutlineItemService = taskOutlineItemService;
    }

    @PostMapping("/create")
    public TaskOutlineResponse createTaskOutline(@Valid @RequestBody TaskOutlineRequest request
    ) {
            return taskOutlineItemService.addTaskOutlineItem(
                    request.taskId(),
                    request.position(),
                    request.indentLevel(),
                    request.text()
            );
    }

    @PutMapping("update-item")
    public TaskOutlineResponse updateTaskOutlineItem(
            @Valid
            @RequestBody
            UpdateTaskOutlineItemRequest
            request
    ){

        return taskOutlineItemService.updateTaskOutlineItem(
                request.id(),
                request.text(),
                request.completed(),
                request.indentLevel(),
                request.position()
        );
    }

    @DeleteMapping("/delete/{itemId}")
    public ResponseEntity<Void> deleteTaskOutlineItem(@PathVariable UUID itemId) {
        taskOutlineItemService.deleteTaskItem(itemId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/batch-update-positions")
    public ResponseEntity<?> batchUpdateOutlineItemPositions(@RequestBody BatchOutlinePositionUpdateRequest request) {
        try {
            List<OutlineItemPositionUpdateDTO> items = request.items();
            taskOutlineItemService.batchUpdateOutlineItemPositions(items);
            return ResponseEntity.ok(Map.of(
                    "message", "Outline item positions updated successfully",
                    "updatedCount", items.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Batch update failed: " + e.getMessage()
            ));
        }
    }

}

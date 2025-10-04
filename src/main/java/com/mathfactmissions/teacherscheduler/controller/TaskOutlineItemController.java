package com.mathfactmissions.teacherscheduler.controller;


import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request.TaskOutlineRequest;
import com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.response.TaskOutlineResponse;
import com.mathfactmissions.teacherscheduler.model.TaskOutlineItem;
import com.mathfactmissions.teacherscheduler.service.TaskOutlineItemService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

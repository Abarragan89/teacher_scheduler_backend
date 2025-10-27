package com.mathfactmissions.teacherscheduler.dto.todoList.request;

import lombok.Builder;

@Builder
public record CreateTodoListRequest(
        String listName
) {
}

package com.mathfactmissions.teacherscheduler.dto.todoList.request;

import java.util.UUID;

public record UpdateTodoListTitleRequest(
        String listName,
        UUID todoListId

) {
}

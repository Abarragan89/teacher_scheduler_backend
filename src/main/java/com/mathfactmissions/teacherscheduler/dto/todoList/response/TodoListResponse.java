package com.mathfactmissions.teacherscheduler.dto.todoList.response;

import com.mathfactmissions.teacherscheduler.dto.todo.response.TodoResponse;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import lombok.Builder;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Builder
public record TodoListResponse(
        UUID id,
        String listName,
        List<TodoResponse> todos
) {
    public static TodoListResponse fromEntity(TodoList list) {
        return TodoListResponse.builder()
            .listName(list.getListName())
            .id(list.getId())
            .todos(
                list.getTodos() == null ? List.of() :
                    list.getTodos().stream()
                        .map(TodoResponse::fromEntity)
                        .collect(Collectors.toList())
            )
            .build();

        }
}

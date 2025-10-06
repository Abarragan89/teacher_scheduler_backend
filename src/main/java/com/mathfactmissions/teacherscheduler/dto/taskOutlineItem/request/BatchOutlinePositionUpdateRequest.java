package com.mathfactmissions.teacherscheduler.dto.taskOutlineItem.request;

import lombok.Builder;

import java.util.List;

@Builder
public record BatchOutlinePositionUpdateRequest(
        List<OutlineItemPositionUpdateDTO> items
) {}

package com.mathfactmissions.teacherscheduler.repository;

import com.mathfactmissions.teacherscheduler.model.TaskOutlineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskOutlineItemRepository extends JpaRepository<TaskOutlineItem, UUID> {
}

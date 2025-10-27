package com.mathfactmissions.teacherscheduler.repository;


import com.mathfactmissions.teacherscheduler.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TodoRepository extends JpaRepository<Todo, UUID> {
}

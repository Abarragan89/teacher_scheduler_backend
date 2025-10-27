package com.mathfactmissions.teacherscheduler.repository;
import com.mathfactmissions.teacherscheduler.model.TodoList;
import com.mathfactmissions.teacherscheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TodoListRepository extends JpaRepository<TodoList, UUID> {

    Optional<List<TodoList>> findAllByUserOrderByUpdatedAtDesc(User user);
}

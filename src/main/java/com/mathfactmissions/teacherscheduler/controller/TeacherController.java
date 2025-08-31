package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.dto.CreateUserRequest;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class TeacherController {

    private final UserService userService;

    public TeacherController(UserService userService) {
            this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody CreateUserRequest dto) {
        User createdUser = userService.createUser(dto);
        return ResponseEntity.ok(createdUser);
    }

}

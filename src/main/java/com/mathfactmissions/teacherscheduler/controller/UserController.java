package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
            this.userService = userService;
    }


    @GetMapping("/allUsers")
    public List<User> getAllUsers() {
        return userService.userRepository.findAll();
    }

}

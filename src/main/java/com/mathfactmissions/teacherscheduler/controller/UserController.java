package com.mathfactmissions.teacherscheduler.controller;

import com.mathfactmissions.teacherscheduler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/users")
public class UserController {
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

//    @GetMapping("/find-all")
//    public List<User> findAllUsers() {
//        return this.userService.userRepository.findAll();
//    }
//
    
}

package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.dto.user.response.UserResponse;
import com.mathfactmissions.teacherscheduler.exception.EmailAlreadyExistsException;
import com.mathfactmissions.teacherscheduler.model.Role;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.RoleRepository;
import com.mathfactmissions.teacherscheduler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    public final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }


    public User createUser(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        String username = email.split("@")[0];

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);

        // Assign default role
        Set<Role> role = roleRepository.findByName("ROLE_USER");
        user.setRoles(role);
        // Save user
        return userRepository.save(user);
    }

    public UserResponse findOrCreateUser(String email) {
        User newUser = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(email));

        return UserResponse.builder()
                .username(newUser.getUsername())
                .email(newUser.getEmail())
                .roles(newUser.getRoles())
                .build();
    }

    public Optional<User> findByEmail (String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

}

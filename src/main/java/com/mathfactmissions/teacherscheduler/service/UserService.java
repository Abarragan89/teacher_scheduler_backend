package com.mathfactmissions.teacherscheduler.service;

import com.mathfactmissions.teacherscheduler.exception.EmailAlreadyExistsException;
import com.mathfactmissions.teacherscheduler.model.Role;
import com.mathfactmissions.teacherscheduler.model.User;
import com.mathfactmissions.teacherscheduler.repository.RoleRepository;
import com.mathfactmissions.teacherscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    public User createUser(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists: " + email);
        }
        
        Set<Role> role = roleRepository.findByName("ROLE_USER");
        if (role == null || role.isEmpty()) {
            throw new RuntimeException("Default role ROLE_USER not found in database");
        }
        
        String username = email.split("@")[0];
        
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setRoles(role);
        
        User saved = userRepository.save(user);
        System.out.println("✅ New user created: " + email);
        return saved;
    }
    
    public User findOrCreateUser(String email) {
        return userRepository.findByEmail(email)
            .orElseGet(() -> createUser(email));
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }
    
    public User save(User user) {
        return userRepository.save(user);
    }
}
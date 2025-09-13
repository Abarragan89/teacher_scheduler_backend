package com.mathfactmissions.teacherscheduler.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateUserRequest {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;


    public String getEmail() { return email;}
    public void setEmail(String email) { this.email = email; }

}

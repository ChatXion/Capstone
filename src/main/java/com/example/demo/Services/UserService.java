package com.example.demo.Services;

import org.springframework.stereotype.Service;

import com.example.demo.Repositories.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    
}

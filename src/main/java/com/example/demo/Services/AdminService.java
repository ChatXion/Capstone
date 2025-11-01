package com.example.demo.Services;

import org.springframework.stereotype.Service;

import com.example.demo.Repositories.AdminRepository;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    
}

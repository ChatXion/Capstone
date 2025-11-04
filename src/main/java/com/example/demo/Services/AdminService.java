package com.example.demo.Services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.Entities.Admin;
import com.example.demo.Repositories.AdminRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminService {
    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    
    @Transactional
    public Optional<Admin> getAdmin(Long adminId) {
        return adminRepository.findById(adminId);
    }
    
    @Transactional
    public Optional<Admin> findByEmail(String email) {
        return adminRepository.findByEmail(email);
    }
}
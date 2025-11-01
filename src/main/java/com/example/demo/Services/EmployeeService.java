package com.example.demo.Services;

import org.springframework.stereotype.Service;

import com.example.demo.Repositories.EmployeeRepository;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    
}

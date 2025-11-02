package com.example.demo.Controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.Entities.Employee;
import com.example.demo.Repositories.EmployeeRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class EmployeeHomePage {

    private final EmployeeRepository employeeRepository;

    public EmployeeHomePage(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/employee/home")
    public String employeeHome(Model model, HttpSession session) {
        // Get employee ID from session
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            // If no user in session, redirect to login
            return "redirect:/login";
        }
        
        // Fetch employee data from database
        Optional<Employee> employeeOpt = employeeRepository.findById(userId);
        
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            
            // Add employee data to model
            model.addAttribute("firstName", employee.getFirstName());
            model.addAttribute("lastName", employee.getLastName());
            model.addAttribute("employeeId", employee.getId());
            model.addAttribute("email", employee.getEmail());
            
            // Add role information if available
            if (employee.getRole() != null) {
                model.addAttribute("role", employee.getRole().getName());
            } else {
                model.addAttribute("role", "N/A");
            }
            
            // Add organization information if available
            if (employee.getOrganization() != null) {
                model.addAttribute("organization", employee.getOrganization().getName());
            } else {
                model.addAttribute("organization", "N/A");
            }
        } else {
            // Employee not found, redirect to login
            return "redirect:/login";
        }
        
        return "employee-home";
    }

    @GetMapping("/nav_user")
    public String navUser(Model model) {
        return "nav_user";
    }
}
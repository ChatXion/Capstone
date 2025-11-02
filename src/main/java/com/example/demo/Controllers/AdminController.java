package com.example.demo.Controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.Admin;
import com.example.demo.Repositories.AdminRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    private final AdminRepository adminRepository;

    public AdminController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @GetMapping("/admin/home")
    public String adminHome(Model model, HttpSession session) {
        // Get admin ID from session
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            // If no user in session, redirect to login
            return "redirect:/login";
        }
        
        // Fetch admin data from database
        Optional<Admin> adminOpt = adminRepository.findById(userId);
        
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            
            // Add admin data to model
            model.addAttribute("firstName", admin.getFirstName());
            model.addAttribute("lastName", admin.getLastName());
            model.addAttribute("adminId", admin.getId());
            model.addAttribute("email", admin.getEmail());
            model.addAttribute("role", "Administrator");
            
            // Add organization information if available
            if (admin.getOrganization() != null) {
                model.addAttribute("organization", admin.getOrganization().getName());
            } else {
                model.addAttribute("organization", "N/A");
            }
        } else {
            // Admin not found, redirect to login
            return "redirect:/login";
        }
        
        return "admin-home";
    }

    @GetMapping("/admin/create-user")
    public String createUserForm(Model model, HttpSession session) {
        // Get firstName from session for navigation
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");
        return "create-user";
    }

    @PostMapping("/admin/create-user")
    public String createUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String userType,
            @RequestParam String organization,
            @RequestParam String role,
            @RequestParam String tempPassword) {
        
        System.out.println("Creating user: " + firstName + " " + lastName + " (" + email + ")");
        
        return "redirect:/admin/home";
    }
}
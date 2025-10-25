package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    @GetMapping("/admin/home")
    public String adminHome(Model model) {
        // Placeholder data 
        model.addAttribute("firstName", "Admin");
        model.addAttribute("lastName", "User");
        model.addAttribute("adminId", "A001");
        model.addAttribute("email", "admin@company.com");
        model.addAttribute("role", "System Administrator");
        model.addAttribute("organization", "Company Co.");
        
        return "admin-home";
    }

    @GetMapping("/admin/create-user")
    public String createUserForm(Model model) {
        // Placeholder
        model.addAttribute("firstName", "Admin");
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
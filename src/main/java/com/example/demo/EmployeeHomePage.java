package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmployeeHomePage {

    @GetMapping("/employee/home")
    public String employeeHome(Model model) {
        // Placeholder data - will be replaced with actual employee data from session/database
        model.addAttribute("firstName", "John");
        model.addAttribute("lastName", "Doe");
        model.addAttribute("employeeId", "12345");
        model.addAttribute("email", "john.doe@company.com");
        model.addAttribute("role", "Developer");
        model.addAttribute("organization", "Company Co.");
        
        //System.out.println("Employee home page accessed");
        return "employee-home";
    }

    @GetMapping("/nav_user")
    public String navUser(Model model) {
        // Placeholder data - will be replaced with actual employee data from session/database
        model.addAttribute("firstName", "John");
        return "nav_user";
    }
}
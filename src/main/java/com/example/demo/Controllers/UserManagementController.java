package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class UserManagementController {

    @GetMapping("/admin/users")
    public String viewUsers(Model model) {
        // Placeholder admin 
        model.addAttribute("firstName", "Admin");
        
        // Dummy user data
        List<SystemUser> users = new ArrayList<>();
        
        users.add(new SystemUser(
            1, 
            "12345",
            "John", 
            "Doe", 
            "john.doe@company.com", 
            "555-0201",
            "Employee",
            "Developer",
            "Engineering",
            "Active",
            LocalDate.of(2023, 1, 15)
        ));
        
        users.add(new SystemUser(
            2, 
            "12346",
            "Jane", 
            "Smith", 
            "jane.smith@company.com", 
            "555-0202",
            "Manager",
            "Engineering Manager",
            "Engineering",
            "Active",
            LocalDate.of(2022, 6, 1)
        ));
        
        users.add(new SystemUser(
            3, 
            "12347",
            "Michael", 
            "Johnson", 
            "michael.j@company.com", 
            "555-0203",
            "Employee",
            "Designer",
            "Design",
            "Active",
            LocalDate.of(2024, 3, 10)
        ));
        
        users.add(new SystemUser(
            4, 
            "12348",
            "Emily", 
            "Davis", 
            "emily.davis@company.com", 
            "555-0204",
            "Employee",
            "QA Engineer",
            "Quality Assurance",
            "Active",
            LocalDate.of(2023, 9, 20)
        ));
        
        users.add(new SystemUser(
            5, 
            "12349",
            "Robert", 
            "Wilson", 
            "r.wilson@company.com", 
            "555-0205",
            "Admin",
            "System Administrator",
            "IT",
            "Active",
            LocalDate.of(2021, 11, 5)
        ));
        
        users.add(new SystemUser(
            6, 
            "12350",
            "Sarah", 
            "Martinez", 
            "sarah.m@company.com", 
            "555-0206",
            "Employee",
            "Marketing Specialist",
            "Marketing",
            "Inactive",
            LocalDate.of(2024, 2, 14)
        ));
        
        model.addAttribute("users", users);
        
        return "admin-users";
    }

    @GetMapping("/admin/users/edit")
    public String editUserForm(@RequestParam int userId, Model model) {
        // Placeholder admin data
        model.addAttribute("firstName", "Admin");
        
        SystemUser user = new SystemUser(
            userId, 
            "12345",
            "John", 
            "Doe", 
            "john.doe@company.com", 
            "555-0201",
            "Employee",
            "Developer",
            "Engineering",
            "Active",
            LocalDate.of(2023, 1, 15)
        );
        
        model.addAttribute("user", user);
        
        return "admin-edit-user";
    }

    @PostMapping("/admin/users/edit")
    public String editUser(
            @RequestParam int userId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String userType,
            @RequestParam String role,
            @RequestParam String department,
            @RequestParam String status) {
        

        System.out.println("Updating user ID: " + userId);
        System.out.println("New name: " + firstName + " " + lastName);
        
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/delete")
    public String deleteUser(@RequestParam int userId) {

        System.out.println("Deleting user ID: " + userId);
        
        return "redirect:/admin/users";
    }


    public static class SystemUser {
        private int id;
        private String employeeId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String userType;
        private String role;
        private String department;
        private String status;
        private LocalDate hireDate;

        public SystemUser(int id, String employeeId, String firstName, String lastName, 
                         String email, String phone, String userType, String role, 
                         String department, String status, LocalDate hireDate) {
            this.id = id;
            this.employeeId = employeeId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.userType = userType;
            this.role = role;
            this.department = department;
            this.status = status;
            this.hireDate = hireDate;
        }

        // Getters
        public int getId() { return id; }
        public String getEmployeeId() { return employeeId; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getUserType() { return userType; }
        public String getRole() { return role; }
        public String getDepartment() { return department; }
        public String getStatus() { return status; }
        public LocalDate getHireDate() { return hireDate; }
    }
}

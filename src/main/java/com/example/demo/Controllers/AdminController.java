package com.example.demo.Controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.Admin;
import com.example.demo.Entities.Employee;
import com.example.demo.Entities.Organization;
import com.example.demo.Entities.Role;
import com.example.demo.Repositories.EmployeeRepository;
import com.example.demo.Repositories.OrganizationRepository;
import com.example.demo.Repositories.RoleRepository;
import com.example.demo.Services.AdminService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    private final AdminService adminService;
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;

    public AdminController(AdminService adminService,
                          EmployeeRepository employeeRepository,
                          OrganizationRepository organizationRepository,
                          RoleRepository roleRepository) {
        this.adminService = adminService;
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/admin/home")
    public String adminHome(Model model, HttpSession session) {
        // Get admin ID from session
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            // If no user in session, redirect to login
            return "redirect:/login";
        }
        
        // Fetch admin data using service
        Optional<Admin> adminOpt = adminService.getAdmin(userId);
        
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
        
        // Fetch all organizations and roles for dropdowns
        List<Organization> organizations = organizationRepository.findAll();
        List<Role> roles = roleRepository.findAll();
        
        model.addAttribute("organizations", organizations);
        model.addAttribute("roles", roles);
        
        return "create-user";
    }

    @PostMapping("/admin/create-user")
    public String createUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam Double ptoBalance,
            @RequestParam Long organizationId,
            @RequestParam Long roleId) {
        
        // Create new employee
        Employee newEmployee = new Employee();
        newEmployee.setFirstName(firstName);
        newEmployee.setLastName(lastName);
        newEmployee.setEmail(email);
        newEmployee.setPassword(password);
        newEmployee.setPtoBalance(ptoBalance);
        
        // Set organization
        Optional<Organization> orgOpt = organizationRepository.findById(organizationId);
        if (orgOpt.isPresent()) {
            newEmployee.setOrganization(orgOpt.get());
        }
        
        // Set role
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isPresent()) {
            newEmployee.setRole(roleOpt.get());
        }
        
        // Save to database
        employeeRepository.save(newEmployee);
        
        System.out.println("Created user: " + firstName + " " + lastName + " (" + email + ")");
        
        return "redirect:/admin/users";
    }
}
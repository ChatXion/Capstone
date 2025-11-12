package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.Organization;
import com.example.demo.Entities.Role;
import com.example.demo.Repositories.EmployeeRepository;
import com.example.demo.Repositories.OrganizationRepository;
import com.example.demo.Repositories.RoleRepository;

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class UserManagementController {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;

    public UserManagementController(EmployeeRepository employeeRepository,
                                   OrganizationRepository organizationRepository,
                                   RoleRepository roleRepository) {
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/admin/users")
    public String viewUsers(Model model, HttpSession session) {
        // Get firstName from session for navigation
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");
        
        // Fetch all employees from database
        List<Employee> employees = employeeRepository.findAll();
        
        // Convert to display objects
        List<EmployeeDisplay> displayUsers = new ArrayList<>();
        for (Employee employee : employees) {
            EmployeeDisplay display = new EmployeeDisplay();
            display.setId(employee.getId());
            display.setFirstName(employee.getFirstName());
            display.setLastName(employee.getLastName());
            display.setEmail(employee.getEmail());
            display.setPtoBalance(employee.getPtoBalance());
            
            // Get organization
            if (employee.getOrganization() != null) {
                display.setOrganizationId(employee.getOrganization().getId());
                display.setOrganizationName(employee.getOrganization().getName());
            }
            
            // Get role
            if (employee.getRole() != null) {
                display.setRoleId(employee.getRole().getId());
                display.setRoleName(employee.getRole().getName());
            }
            
            displayUsers.add(display);
        }
        
        model.addAttribute("users", displayUsers);
        
        return "admin-users";
    }

    @GetMapping("/admin/users/edit")
    public String editUserForm(@RequestParam Long userId, Model model, HttpSession session) {
        // Get firstName from session for navigation
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");
        
        // Fetch employee from database
        Optional<Employee> employeeOpt = employeeRepository.findById(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/admin/users";
        }
        
        Employee employee = employeeOpt.get();
        
        // Create display object
        EmployeeDisplay display = new EmployeeDisplay();
        display.setId(employee.getId());
        display.setFirstName(employee.getFirstName());
        display.setLastName(employee.getLastName());
        display.setEmail(employee.getEmail());
        display.setPtoBalance(employee.getPtoBalance());
        
        if (employee.getOrganization() != null) {
            display.setOrganizationId(employee.getOrganization().getId());
            display.setOrganizationName(employee.getOrganization().getName());
        }
        
        if (employee.getRole() != null) {
            display.setRoleId(employee.getRole().getId());
            display.setRoleName(employee.getRole().getName());
        }
        
        model.addAttribute("user", display);
        
        // Fetch all organizations and roles for dropdowns
        List<Organization> organizations = organizationRepository.findAll();
        List<Role> roles = roleRepository.findAll();
        
        model.addAttribute("organizations", organizations);
        model.addAttribute("roles", roles);
        
        return "admin-edit-user";
    }

    @PostMapping("/admin/users/edit")
    public String editUser(
            @RequestParam Long userId,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam Double ptoBalance,
            @RequestParam Long organizationId,
            @RequestParam Long roleId) {
        
        // Fetch employee from database
        Optional<Employee> employeeOpt = employeeRepository.findById(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/admin/users";
        }
        
        Employee employee = employeeOpt.get();
        
        // Update employee fields
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPtoBalance(ptoBalance);
        
        // Update password only if provided
        if (password != null && !password.trim().isEmpty()) {
            employee.setPassword(password);
        }
        
        // Update organization
        Optional<Organization> orgOpt = organizationRepository.findById(organizationId);
        if (orgOpt.isPresent()) {
            employee.setOrganization(orgOpt.get());
        }
        
        // Update role
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isPresent()) {
            employee.setRole(roleOpt.get());
        }
        
        // Save to database
        employeeRepository.save(employee);
        
        System.out.println("Updated user ID: " + userId);
        System.out.println("New name: " + firstName + " " + lastName);
        
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/delete")
    public String deleteUser(@RequestParam Long userId) {
        
        // Delete employee from database
        employeeRepository.deleteById(userId);
        
        System.out.println("Deleted user ID: " + userId);
        
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/{userId}/timesheets")
    public String viewEmployeeTimesheets(@PathVariable Long userId, Model model, HttpSession session) {
        // Get admin firstName from session for navigation
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");
        
        return "redirect:/admin/view-timesheets/" + userId;
    }

    // Display class for employees - matches database columns only
    public static class EmployeeDisplay {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private Double ptoBalance;
        private Long organizationId;
        private String organizationName;
        private Long roleId;
        private String roleName;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        // Computed properties for template compatibility
        public String getEmployeeId() { return id != null ? String.valueOf(id) : null; }
        public String getPhone() { return "N/A"; }
        public String getUserType() { return "Employee"; }
        public String getDepartment() { return organizationName != null ? organizationName : "N/A"; }
        public String getRole() { return roleName; }  // Alias for roleName
        public String getStatus() { return "Active"; }
        public java.time.LocalDate getHireDate() { return null; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public Double getPtoBalance() { return ptoBalance; }
        public void setPtoBalance(Double ptoBalance) { this.ptoBalance = ptoBalance; }
        
        public Long getOrganizationId() { return organizationId; }
        public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
        
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        
        public Long getRoleId() { return roleId; }
        public void setRoleId(Long roleId) { this.roleId = roleId; }
        
        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
    }
}
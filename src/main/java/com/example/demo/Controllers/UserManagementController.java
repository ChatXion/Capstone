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
import com.example.demo.Services.RoleService; 
import com.example.demo.Services.OrganizationService; 

import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

@Controller
public class UserManagementController {

    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService; 
    private final OrganizationService organizationService; 

    public UserManagementController(EmployeeRepository employeeRepository,
            OrganizationRepository organizationRepository,
            RoleRepository roleRepository,
            RoleService roleService, 
            OrganizationService organizationService) { 
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.roleService = roleService; 
        this.organizationService = organizationService; 
    }

    @GetMapping("/admin/users")
    public String viewUsers(Model model, HttpSession session) {
        Long adminId = (Long) session.getAttribute("userId");
        String firstName = (String) session.getAttribute("firstName");
        
        if (adminId == null) {
            return "redirect:/login";
        }
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");

        // --- AUTHORIZATION SCOPE: Fetch employees for admin's organization only ---
        Organization adminOrg;
        try {
            adminOrg = organizationService.findByAdminId(adminId);
        } catch (Exception e) {
            return "redirect:/admin/home?error=NoOrganizationFound";
        }
        
        // Fetch all employees from the admin's organization
        List<Employee> employees = employeeRepository.findByOrganizationId(adminOrg.getId());

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

        // Generate filtered lists for frontend dropdowns
        List<String> organizations = displayUsers.stream()
                .map(EmployeeDisplay::getOrganizationName) 
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> roles = displayUsers.stream()
                .map(EmployeeDisplay::getRoleName) 
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        model.addAttribute("organizations", organizations);
        model.addAttribute("roles", roles);

        model.addAttribute("users", displayUsers);

        return "admin-users";
    }

    @GetMapping("/admin/users/edit")
    public String editUserForm(@RequestParam Long userId, Model model, HttpSession session) {
        Long adminId = (Long) session.getAttribute("userId");
        if (adminId == null) {
            return "redirect:/login";
        }
        
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");

        // Fetch employee from database
        Optional<Employee> employeeOpt = employeeRepository.findById(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/admin/users?error=UserNotFound";
        }

        Employee employee = employeeOpt.get();

        // --- AUTHORIZATION CHECK: Ensure the user belongs to the admin's organization ---
        Organization adminOrg;
        try {
            adminOrg = organizationService.findByAdminId(adminId);
        } catch (Exception e) {
            return "redirect:/admin/home?error=NoOrganizationFound";
        }
        
        // If the employee's organization is null or does not match the admin's organization, deny access.
        if (employee.getOrganization() == null || !employee.getOrganization().getId().equals(adminOrg.getId())) {
            return "redirect:/admin/users?error=AccessDenied";
        }
        // --- END CHECK ---

        // Create display object
        EmployeeDisplay display = new EmployeeDisplay();
        display.setId(employee.getId());
        display.setFirstName(employee.getFirstName());
        display.setLastName(employee.getLastName());
        display.setEmail(employee.getEmail());
        display.setPtoBalance(employee.getPtoBalance());
        display.setOrganizationId(employee.getOrganization().getId());
        display.setOrganizationName(employee.getOrganization().getName());

        if (employee.getRole() != null) {
            display.setRoleId(employee.getRole().getId());
            display.setRoleName(employee.getRole().getName());
        }

        model.addAttribute("user", display);

        // Fetch only roles for the dropdowns
        List<Role> roles;
        try {
            roles = roleService.findAllByAdminId(adminId); 
        } catch (Exception e) {
            roles = new ArrayList<>();
        }

        // organization list is removed from model as the dropdown is removed from the view
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
            @RequestParam Long organizationId, // Passed as hidden field from the form
            @RequestParam Long roleId,
            HttpSession session) {
        
        Long adminId = (Long) session.getAttribute("userId");
        if (adminId == null) {
            return "redirect:/login";
        }

        // Fetch employee from database
        Optional<Employee> employeeOpt = employeeRepository.findById(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/admin/users?error=UserNotFound";
        }
        Employee employee = employeeOpt.get();

        // --- AUTHORIZATION CHECK (Write): Ensure the user belongs to the admin's organization ---
        Organization adminOrg;
        try {
            adminOrg = organizationService.findByAdminId(adminId);
        } catch (Exception e) {
            return "redirect:/admin/home?error=NoOrganizationFound";
        }
        
        // 1. Target user must be in admin's organization
        if (employee.getOrganization() == null || !employee.getOrganization().getId().equals(adminOrg.getId())) {
             return "redirect:/admin/users?error=AccessDenied";
        }
        
        // 2. Organization ID check: Ensure the hidden field organizationId (from the user being edited)
        // matches the admin's organization ID. This prevents changing the organization via POST.
        if (!organizationId.equals(adminOrg.getId())) {
             return "redirect:/admin/users/edit?userId=" + userId + "&error=CannotChangeOrganization";
        }
        // --- END CHECK ---


        // Update employee fields
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPtoBalance(ptoBalance);
        
        // Update password only if provided
        if (password != null && !password.trim().isEmpty()) {
            employee.setPassword(password);
        }

        // Organization is already set correctly on employee object.

        // Set Role
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isPresent()) {
             // Further validation: Ensure the selected role belongs to the admin's organization
            if (!roleOpt.get().getOrganization().getId().equals(adminOrg.getId())) {
                return "redirect:/admin/users/edit?userId=" + userId + "&error=RoleNotValidForOrganization";
            }
            employee.setRole(roleOpt.get());
        }

        // Save to database
        employeeRepository.save(employee);

        System.out.println("Updated user ID: " + userId);
        System.out.println("New name: " + firstName + " " + lastName);

        return "redirect:/admin/users";
    }

    /**
     * Handles the request to remove an employee from their organization.
     * The method clears the employee's organization and role fields.
     */
    @PostMapping("/admin/users/remove-organization")
    public String removeUserFromOrganization(@RequestParam Long userId, HttpSession session) {
        Long adminId = (Long) session.getAttribute("userId");
        if (adminId == null) {
            return "redirect:/login";
        }

        Optional<Employee> employeeOpt = employeeRepository.findById(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/admin/users?error=UserNotFound";
        }
        Employee employee = employeeOpt.get();
        
        // --- AUTHORIZATION CHECK ---
        Organization adminOrg;
        try {
            adminOrg = organizationService.findByAdminId(adminId);
        } catch (Exception e) {
            return "redirect:/admin/home?error=NoOrganizationFound";
        }
        
        // If the employee's organization is null or does not match the admin's organization, deny access.
        if (employee.getOrganization() == null || !employee.getOrganization().getId().equals(adminOrg.getId())) {
             return "redirect:/admin/users?error=AccessDenied";
        }
        // --- END CHECK ---
        
        // Clear organization and role
        employee.setOrganization(null);
        employee.setRole(null);
        employeeRepository.save(employee);
        
        System.out.println("Removed user ID: " + userId + " from organization " + adminOrg.getName());
        
        return "redirect:/admin/users/edit?userId=" + userId + "&success=OrganizationRemoved";
    }
    
    @PostMapping("/admin/users/delete")
    public String deleteUser(@RequestParam Long userId, HttpSession session) {
        Long adminId = (Long) session.getAttribute("userId");
        if (adminId == null) {
            return "redirect:/login";
        }

        // Fetch employee from database
        Optional<Employee> employeeOpt = employeeRepository.findById(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/admin/users?error=UserNotFound";
        }
        Employee employee = employeeOpt.get();

        // --- AUTHORIZATION CHECK (Delete): Ensure the user belongs to the admin's organization ---
        Organization adminOrg;
        try {
            adminOrg = organizationService.findByAdminId(adminId);
        } catch (Exception e) {
            return "redirect:/admin/home?error=NoOrganizationFound";
        }
        
        // If the employee's organization is null or does not match the admin's organization, deny access.
        if (employee.getOrganization() == null || !employee.getOrganization().getId().equals(adminOrg.getId())) {
             return "redirect:/admin/users?error=AccessDenied";
        }
        // --- END CHECK ---
        
        // Delete employee from database
        employeeRepository.deleteById(userId);

        System.out.println("Deleted user ID: " + userId);

        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/{userId}/timesheets")
    public String viewEmployeeTimesheets(@PathVariable Long userId, Model model, HttpSession session) {
        // This controller delegates to AdminViewTimesheetsController,
        // which should handle its own authorization check.
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
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        // Computed properties for template compatibility
        public String getEmployeeId() {
            return id != null ? String.valueOf(id) : null;
        }

        public String getPhone() {
            return "N/A";
        }

        public String getUserType() {
            return "Employee";
        }

        public String getDepartment() {
            return organizationName != null ? organizationName : "N/A";
        }

        public String getRole() {
            return roleName;
        } // Alias for roleName

        public String getStatus() {
            return "Active";
        }

        public java.time.LocalDate getHireDate() {
            return null;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Double getPtoBalance() {
            return ptoBalance;
        }

        public void setPtoBalance(Double ptoBalance) {
            this.ptoBalance = ptoBalance;
        }

        public Long getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(Long organizationId) {
            this.organizationId = organizationId;
        }

        public String getOrganizationName() {
            return organizationName;
        }

        public void setOrganizationName(String organizationName) {
            this.organizationName = organizationName;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
    }
}
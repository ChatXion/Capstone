package com.example.demo.Controllers;

import java.util.ArrayList;
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
import com.example.demo.Repositories.AdminRepository;
import com.example.demo.Repositories.EmployeeRepository;
import com.example.demo.Repositories.OrganizationRepository;
import com.example.demo.Repositories.RoleRepository;
import com.example.demo.Services.AdminService;
import com.example.demo.Services.OrganizationService;
import com.example.demo.Services.RoleService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    private final AdminService adminService;
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final AdminRepository adminRepository;
    private final RoleService roleService;
    private final OrganizationService organizationService; 


    public AdminController(AdminService adminService,
            EmployeeRepository employeeRepository,
            OrganizationRepository organizationRepository,
            RoleRepository roleRepository,
            AdminRepository adminRepository,
            OrganizationService organizationService, 
            RoleService roleService) { 
        this.adminService = adminService;
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.adminRepository = adminRepository;
        this.organizationService = organizationService; 
        this.roleService = roleService; 
    }

    @GetMapping("/admin/home")
    public String adminHome(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        Optional<Admin> adminOpt = adminService.getAdmin(userId);

        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();

            model.addAttribute("firstName", admin.getFirstName());
            model.addAttribute("lastName", admin.getLastName());
            model.addAttribute("adminId", admin.getId());
            model.addAttribute("email", admin.getEmail());
            model.addAttribute("role", "Administrator");

            if (admin.getOrganization() != null) {
                model.addAttribute("organization", admin.getOrganization().getName());
            } else {
                model.addAttribute("organization", "N/A");
            }
        } else {
            return "redirect:/login";
        }

        return "admin-home";
    }

    @GetMapping("/admin/create-user")
    public String createUserForm(Model model, HttpSession session) {
        Long adminId = (Long) session.getAttribute("userId");
        if (adminId == null) {
            return "redirect:/login";
        }
        
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");

        Organization adminOrg;
        try {
            adminOrg = organizationService.findByAdminId(adminId);
        } catch (Exception e) {
            return "redirect:/admin/home?error=NoOrganizationFound";
        }

        // Only add the admin's organization to the list
        List<Organization> organizations = new ArrayList<>();
        organizations.add(adminOrg); 
        
        // Fetch only roles belonging to the admin's organization
        List<Role> roles;
        try {
            roles = roleService.findAllByAdminId(adminId);
        } catch (Exception e) {
            roles = new ArrayList<>();
        }

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
            @RequestParam Long roleId,
            HttpSession session) {
        
        Long adminId = (Long) session.getAttribute("userId");
        if (adminId == null) {
            return "redirect:/login";
        }

        Organization adminOrg;
        try {
            adminOrg = organizationService.findByAdminId(adminId);
        } catch (Exception e) {
            return "redirect:/admin/home?error=NoOrganizationFound";
        }

        // --- AUTHORIZATION CHECK (Create): Ensure the new user is created in the admin's organization ---
        if (!organizationId.equals(adminOrg.getId())) {
             return "redirect:/admin/create-user?error=CannotCreateUserInOtherOrg";
        }
        // --- END CHECK ---
        
        // Check if the role belongs to the admin's organization
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty() || !roleOpt.get().getOrganization().getId().equals(adminOrg.getId())) {
            return "redirect:/admin/create-user?error=RoleNotValidForOrganization";
        }

        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPassword(password);
        employee.setPtoBalance(ptoBalance);

        // Set Organization (validated above)
        employee.setOrganization(adminOrg); 
        
        // Set Role (validated above)
        employee.setRole(roleOpt.get());

        employeeRepository.save(employee);

        System.out.println("Created new user: " + firstName + " " + lastName + " (ID: " + employee.getId() + ")");

        return "redirect:/admin/users";
    }

    @GetMapping("/admin/roles")
    public String viewRoles(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");

        try {
            List<Role> roles = roleService.findAllByAdminId(userId);
            model.addAttribute("roles", roles);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Could not find organization for administrator.");
            model.addAttribute("roles", List.of());
        }

        return "admin-roles";
    }

    @PostMapping("/admin/roles/create")
    public String createRole(@RequestParam String roleName, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            roleService.createRole(userId, roleName);
        } catch (Exception e) {
            return "redirect:/admin/roles?error=" + e.getMessage();
        }

        return "redirect:/admin/roles";
    }

    @PostMapping("/admin/roles/edit")
    public String editRole(@RequestParam Long roleId, @RequestParam String newRoleName, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            roleService.updateRole(roleId, newRoleName, userId);
        } catch (Exception e) {
            return "redirect:/admin/roles?error=" + e.getMessage();
        }

        return "redirect:/admin/roles";
    }

    @PostMapping("/admin/roles/delete")
    public String deleteRole(@RequestParam Long roleId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            roleService.deleteRole(roleId, userId);
        } catch (IllegalStateException e) {
            return "redirect:/admin/roles?error=" + e.getMessage();
        } catch (Exception e) {
            return "redirect:/admin/roles?error=unknownError";
        }

        return "redirect:/admin/roles";
    }

}
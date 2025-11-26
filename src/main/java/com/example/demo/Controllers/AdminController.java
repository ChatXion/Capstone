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
import com.example.demo.Repositories.AdminRepository;
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
    private final AdminRepository adminRepository;

    public AdminController(AdminService adminService,
            EmployeeRepository employeeRepository,
            OrganizationRepository organizationRepository,
            RoleRepository roleRepository,
            AdminRepository adminRepository) {
        this.adminService = adminService;
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.roleRepository = roleRepository;
        this.adminRepository = adminRepository;
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
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");

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

        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPassword(password);
        employee.setPtoBalance(ptoBalance);

        Optional<Organization> orgOpt = organizationRepository.findById(organizationId);
        orgOpt.ifPresent(employee::setOrganization);

        Optional<Role> roleOpt = roleRepository.findById(roleId);
        roleOpt.ifPresent(employee::setRole);

        employeeRepository.save(employee);

        System.out.println("Created new user: " + firstName + " " + lastName + " (ID: " + employee.getId() + ")");

        return "redirect:/admin/users";
    }

}
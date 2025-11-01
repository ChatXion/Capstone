package com.example.demo.Controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.Admin;
import com.example.demo.Entities.Employee;
import com.example.demo.Repositories.AdminRepository;
import com.example.demo.Repositories.EmployeeRepository;

import jakarta.servlet.http.HttpSession;

/*
  LoginController
  - No GET /login here (served by WebController).
  - POST /login handles authentication.
*/
@Controller
public class LoginController {

    private final EmployeeRepository employeeRepository;
    private final AdminRepository adminRepository;

    public LoginController(EmployeeRepository employeeRepository,
                           AdminRepository adminRepository) {
        this.employeeRepository = employeeRepository;
        this.adminRepository = adminRepository;
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              Model model,
                              HttpSession session) {

        // Check employees
        Optional<Employee> empOpt = employeeRepository.findByEmail(email);
        if (empOpt.isPresent()) {
            Employee e = empOpt.get();
            if (e.getPassword() != null && e.getPassword().equals(password)) {
                session.setAttribute("userId", e.getId());
                session.setAttribute("firstName", e.getFirstName());
                session.setAttribute("role", "EMPLOYEE");
                return "redirect:/employee/home";
            }
            model.addAttribute("error", "Invalid email or password.");
            return "login";
        }

        // Check admins
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin a = adminOpt.get();
            if (a.getPassword() != null && a.getPassword().equals(password)) {
                session.setAttribute("userId", a.getId());
                session.setAttribute("firstName", a.getFirstName());
                session.setAttribute("role", "ADMIN");
                return "redirect:/admin/home";
            }
            model.addAttribute("error", "Invalid email or password.");
            return "login";
        }

        // Not found
        model.addAttribute("error", "Login not found.");
        return "login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
package com.example.demo.Services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.Entities.Admin;
import com.example.demo.Entities.Employee;
import com.example.demo.Repositories.AdminRepository;
import com.example.demo.Repositories.EmployeeRepository;

@Service
public class LoginService {

    private final EmployeeRepository employeeRepository;
    private final AdminRepository adminRepository;

    public LoginService(EmployeeRepository employeeRepository,
                        AdminRepository adminRepository) {
        this.employeeRepository = employeeRepository;
        this.adminRepository = adminRepository;
    }

    public record AuthResult(Long id, String firstName, String role) {}

    /**
     * Authenticate email/password against Employee then Admin.
     * Returns Optional<AuthResult> on success, empty on failure.
     * (Plain-text comparison — keep for dev only.)
     */
    public Optional<AuthResult> authenticate(String email, String password) {
        Optional<Employee> empOpt = employeeRepository.findByEmail(email);
        if (empOpt.isPresent()) {
            Employee e = empOpt.get();
            if (e.getPassword() != null && e.getPassword().equals(password)) {
                return Optional.of(new AuthResult(e.getId(), e.getFirstName(), "EMPLOYEE"));
            }
            return Optional.empty();
        }

        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin a = adminOpt.get();
            if (a.getPassword() != null && a.getPassword().equals(password)) {
                return Optional.of(new AuthResult(a.getId(), a.getFirstName(), "ADMIN"));
            }
            return Optional.empty();
        }

        return Optional.empty();
    }
}
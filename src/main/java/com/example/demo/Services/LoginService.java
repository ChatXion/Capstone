package com.example.demo.Services;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.Entities.Admin;
import com.example.demo.Entities.Employee;
import com.example.demo.Repositories.AdminRepository;
import com.example.demo.Repositories.EmployeeRepository;

@Service
public class LoginService {

    private static final Logger log = LoggerFactory.getLogger(LoginService.class);

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
     * (Plain-text comparison — dev only.)
     */
    public Optional<AuthResult> authenticate(String email, String password) {
        // employee attempt session.setAttribute("userId", ar.id()); session.setAttribute("firstName", ar.firstName());
        //stores session information, 
        Optional<Employee> empOpt = employeeRepository.findByEmail(email);
        if (empOpt.isPresent()) {
            Employee e = empOpt.get();
            boolean ok = e.getPassword() != null && e.getPassword().equals(password);
            if (ok) {
                log.info("LOGIN SUCCESS role=EMPLOYEE id={} email={} time={}", e.getId(), email, Instant.now());
                return Optional.of(new AuthResult(e.getId(), e.getFirstName(), "EMPLOYEE"));
            } else {
                log.warn("LOGIN FAILED (bad credentials) role=EMPLOYEE email={} time={}", email, Instant.now());
                return Optional.empty();
            }
        }

        // admin attempt
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin a = adminOpt.get();
            boolean ok = a.getPassword() != null && a.getPassword().equals(password);
            if (ok) {
                log.info("LOGIN SUCCESS role=ADMIN id={} email={} time={}", a.getId(), email, Instant.now());
                return Optional.of(new AuthResult(a.getId(), a.getFirstName(), "ADMIN"));
            } else {
                log.warn("LOGIN FAILED (bad credentials) role=ADMIN email={} time={}", email, Instant.now());
                return Optional.empty();
            }
        }

        log.warn("LOGIN FAILED (not found) email={} time={}", email, Instant.now());
        return Optional.empty();
    }
}
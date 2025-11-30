package com.example.demo.Services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.InviteCode;
import com.example.demo.Entities.RegistrationRequest;
import com.example.demo.Repositories.EmployeeRepository;
import com.example.demo.Repositories.InviteCodeRepository;
import com.example.demo.Repositories.RegistrationRequestRepository;

@Service
public class RegistrationService {
    private final RegistrationRequestRepository repo;
    private final InviteCodeRepository inviteCodeRepository;
    // Assume EmployeeRepository exists for creating new employees upon approval
    private final EmployeeRepository employeeRepository;
    //used in approve to create new employee
    public RegistrationService(RegistrationRequestRepository repo, 
                            InviteCodeRepository inviteCodeRepository,
                            EmployeeRepository employeeRepository) { 
        this.repo = repo;
        this.inviteCodeRepository = inviteCodeRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public RegistrationRequest create(RegistrationRequest req) {
        if (inviteCodeRepository.findByCode(req.getInvitationCode()).isEmpty()) {
            throw new IllegalArgumentException("Invalid invitation code");
        }
        if (repo.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        req.setStatus(RegistrationRequest.Status.PENDING);
        return repo.save(req);
    }

    //registration controller calls approve on registration when admin approves on admin view registrations page
    @Transactional
    public void approve(Long requestId) {
        RegistrationRequest r = repo.findById(requestId).orElseThrow();
        
        // Get the invite code to determine organization and role
        InviteCode inviteCode = inviteCodeRepository.findByCode(r.getInvitationCode())
            .orElseThrow(() -> new IllegalArgumentException("Invalid invitation code"));
        
        // Create new employee
        Employee employee = new Employee();
        employee.setFirstName(r.getFirstName());
        employee.setLastName(r.getLastName());
        employee.setEmail(r.getEmail());
        employee.setPassword("temp123"); // TODO: Generate random password and send via email
        employee.setOrganization(inviteCode.getOrganization());
        employee.setRole(inviteCode.getAssigningRole());
        employee.setPtoBalance(0.0); // Start with 0 PTO balance
        
        employeeRepository.save(employee);
        
        // Update registration status
        r.setStatus(RegistrationRequest.Status.APPROVED);
        repo.save(r);
    }

    //registration controller calls deny on registration when admin denies on admin view regirstrations page
    @Transactional
    public void deny(Long requestId, String reason) {
        RegistrationRequest r = repo.findById(requestId).orElseThrow();
        r.setStatus(RegistrationRequest.Status.REJECTED);
        repo.save(r);
        // Optionally: send rejection email with reason
    }

    // Fetch all pending registration requests, in registration controller, still sees pending registrations
    public List<RegistrationRequest> getAllPendingRegistrations() {
        return repo.findAll();
    }

    
}
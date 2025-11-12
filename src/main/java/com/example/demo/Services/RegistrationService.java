package com.example.demo.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.Entities.RegistrationRequest;
import com.example.demo.Repositories.RegistrationRequestRepository;

@Service
public class RegistrationService {
    private final RegistrationRequestRepository repo;

    public RegistrationService(RegistrationRequestRepository repo) { this.repo = repo; }

    @Transactional
    public RegistrationRequest create(RegistrationRequest req) {
        if (repo.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        req.setStatus(RegistrationRequest.Status.PENDING);
        return repo.save(req);
    }

    @Transactional
    public void approve(Long requestId) {
        RegistrationRequest r = repo.findById(requestId).orElseThrow();
        r.setStatus(RegistrationRequest.Status.APPROVED);
        repo.save(r);
        // create Employee/Admin here: hash password, save, email notification, etc.
    }
}
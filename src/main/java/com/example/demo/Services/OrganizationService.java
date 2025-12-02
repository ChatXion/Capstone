package com.example.demo.Services;

import com.example.demo.Entities.Organization;
import com.example.demo.Repositories.OrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    
    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Optional<Organization> findById(Long id) {
        return organizationRepository.findById(id);
    }

    public Organization findByAdminId(Long id){
        return organizationRepository.findByAdmins_Id(id)
            .orElseThrow(() -> new IllegalArgumentException("No organization found for admin ID: " + id));
    }
}
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

    /**
     * Retrieves an Organization by ID from the database.
     * @param id The ID of the organization to find.
     * @return An Optional containing the Organization if found.
     */
    public Optional<Organization> findById(Long id) {
        return organizationRepository.findById(id);
    }

    public Organization findByAdminId(Long id){
        return organizationRepository.findByAdmins_Id(id)
            .orElseThrow(() -> new IllegalArgumentException("No organization found for admin ID: " + id));
    }
}
package com.example.demo.Services;

import com.example.demo.Entities.Organization;
import com.example.demo.Entities.Role;
import com.example.demo.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final OrganizationService organizationService;

    public RoleService(RoleRepository roleRepository, OrganizationService organizationService) {
        this.roleRepository = roleRepository;
        this.organizationService = organizationService;
    }

    /**
     * Retrieves all Role entities by admin and organization from the database.
     */
    public List<Role> findAllByAdminId(Long adminId) {
        Organization org = organizationService.findByAdminId(adminId);

        return roleRepository.findAllByOrganization(org);
    }

    public Role findByNameAndAdminId(String name, Long adminId){

        Organization org = organizationService.findByAdminId(adminId);

        Role role = roleRepository.findByNameAndOrganization(name, org)
            .orElseThrow(() -> new IllegalArgumentException("No role found for name: " + name + " and organization: " + org.getName()));

         return role;
    }
}
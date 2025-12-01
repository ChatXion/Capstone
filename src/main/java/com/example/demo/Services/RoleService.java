package com.example.demo.Services;

import com.example.demo.Entities.Organization;
import com.example.demo.Entities.Role;
import com.example.demo.Repositories.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

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

    // New method: Find by ID (needed for update/delete)
    public Optional<Role> findById(Long roleId) {
        return roleRepository.findById(roleId);
    }
    
    /**
     * Creates a new Role entity for the given admin's organization.
     */
    @Transactional
    public Role createRole(Long adminId, String roleName) {
        Organization org = organizationService.findByAdminId(adminId);

        // Check if role name already exists in this organization
        if (roleRepository.findByNameAndOrganization(roleName, org).isPresent()) {
             throw new IllegalArgumentException("Role name already exists in organization: " + org.getName());
        }

        Role newRole = new Role();
        newRole.setName(roleName); 
        newRole.setOrganization(org);
        
        return roleRepository.save(newRole);
    }

    /**
     * Updates an existing Role's name.
     */
    @Transactional
    public Role updateRole(Long roleId, String newRoleName, Long adminId) {
        Role roleToUpdate = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
            
        Organization adminOrg = organizationService.findByAdminId(adminId);
        
        // Security check: ensure the role belongs to the admin's organization
        if (!roleToUpdate.getOrganization().getId().equals(adminOrg.getId())) {
             throw new SecurityException("Access denied. Role does not belong to your organization.");
        }
        
        // Check for duplicate name within the organization, excluding itself
        Optional<Role> duplicate = roleRepository.findByNameAndOrganization(newRoleName, adminOrg);
        if (duplicate.isPresent() && !duplicate.get().getId().equals(roleId)) {
             throw new IllegalArgumentException("Role name already exists in organization: " + adminOrg.getName());
        }

        roleToUpdate.setName(newRoleName);
        return roleRepository.save(roleToUpdate);
    }

    /**
     * Deletes a Role by its ID.
     */
    @Transactional
    public void deleteRole(Long roleId, Long adminId) {
        Role roleToDelete = roleRepository.findById(roleId)
            .orElseThrow(() -> new IllegalArgumentException("Role not found with ID: " + roleId));
            
        Organization adminOrg = organizationService.findByAdminId(adminId);
        
        // Security check: ensure the role belongs to the admin's organization
        if (!roleToDelete.getOrganization().getId().equals(adminOrg.getId())) {
             throw new SecurityException("Access denied. Role does not belong to your organization.");
        }
        
        // Check: prevent deletion if employees are still linked
        if (roleToDelete.getEmployees() != null && !roleToDelete.getEmployees().isEmpty()) {
            throw new IllegalStateException("Cannot delete role. Employees are currently assigned to this role.");
        }
        
        roleRepository.deleteById(roleId);
    }
}
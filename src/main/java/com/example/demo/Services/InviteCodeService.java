package com.example.demo.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.demo.Entities.InviteCode;
import com.example.demo.Entities.Organization;
import com.example.demo.Entities.Role;
import com.example.demo.Repositories.InviteCodeRepository;

import jakarta.transaction.Transactional;

@Service
public class InviteCodeService {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 12;
    private final InviteCodeRepository inviteCodeRepository;
    private final OrganizationService organizationService;
    private final RoleService roleService;

    public InviteCodeService(InviteCodeRepository inviteCodeRepository, OrganizationService organizationService, RoleService roleService){
        this.inviteCodeRepository = inviteCodeRepository;
        this.organizationService = organizationService;
        this.roleService = roleService;
    }

    /**
     * Generates a 12-character alpha-numeric code, formatted in 4-character chunks.
     * Example: "A1b2-c3D4-E5f6"
     */
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder();
        
        // Generate the raw 12 characters
        for (int i = 0; i < length; i++) {
            codeBuilder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        
        // Format the code into 4-4-4 chunks separated by hyphens
        return codeBuilder.insert(8, '-').insert(4, '-').toString();
    }

    /**
     * Generates a unique 12-character invitation code by repeatedly
     * generating a code and checking the database until a unique one is found.
     * @return A unique, formatted invitation code string.
     */
    public String generateUniqueCode() {
        String newCode;
        Optional<InviteCode> existingCode;
        
        // Loop until a unique code is generated
        do {
            newCode = generateRandomCode(CODE_LENGTH);
            // The Repository method findByCode is required for this check
            existingCode = inviteCodeRepository.findByCode(newCode);
        } while (existingCode.isPresent()); // Keep looping if the code exists

        return newCode;
    }

    public List<InviteCode> findAllByAdminId(Long adminId){
        Organization org = organizationService.findByAdminId(adminId);
        
        return org.getInviteCodes();
    }

    /**
     * Saves a new InviteCode entity to the database.
     * @param inviteCode The userId of the admin saving creating the code.
     * @param roleName The name of the assigning role in the invite code.
     * @param description The description for the invite code.
     * @return The saved InviteCode entity.
     */
    @Transactional
    public InviteCode createInviteCode(Long userId, String roleName, String description) {

        // Find role
        Role role = roleService.findByNameAndAdminId(roleName, userId);
            
        // Generate unique code
        String newUniqueCode = generateUniqueCode();

        // Create new entity
        InviteCode newInviteCode = new InviteCode();
        newInviteCode.setCode(newUniqueCode);
        newInviteCode.setDescription(description);
        newInviteCode.setAssigningRole(role);
        newInviteCode.setOrganization(role.getOrganization());

        return inviteCodeRepository.save(newInviteCode);
    }

    /**
     * Updates the description and/or assigned role of an existing invite code.
     * @param id The ID of the code to update.
     * @param newDescription The new description string.
     * @param newRole The new Role entity to assign.
     * @return The updated InviteCode entity.
     * @throws RuntimeException if the code ID is not found.
     */
    @Transactional
    public InviteCode editInviteCode(Long inviteCodeId, String newDescription, String roleName, Long adminId) {
        // Find the existing entity or throw an exception
        InviteCode codeToUpdate = inviteCodeRepository.findById(inviteCodeId)
            .orElseThrow(() -> new IllegalArgumentException("Invite code not found with ID: " + inviteCodeId));
        
        // 1. Look up the new Role entity using the RoleService
        Role newRole = roleService.findByNameAndAdminId(roleName, adminId);

        // Apply updates
        codeToUpdate.setDescription(newDescription);
        codeToUpdate.setAssigningRole(newRole);
        // Assuming Organization remains unchanged
        
        return inviteCodeRepository.save(codeToUpdate);
    }

    /**
     * Deletes an invite code by its ID.
     * @param id The ID of the code to delete.
     * @throws RuntimeException if the code ID is not found (though deleteById often handles this gracefully).
     */
    @Transactional
    public void deleteInviteCode(Long id) {
        // You can add a check here if necessary, but JpaRepository's deleteById is generally safe.
        inviteCodeRepository.deleteById(id);
    }
}

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

            existingCode = inviteCodeRepository.findByCode(newCode);
        } while (existingCode.isPresent()); 

        return newCode;
    }

    public List<InviteCode> findAllByAdminId(Long adminId){
        Organization org = organizationService.findByAdminId(adminId);
        
        return org.getInviteCodes();
    }

   
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

    
    @Transactional
    public InviteCode editInviteCode(Long inviteCodeId, String newDescription, String roleName, Long adminId) {
        
        InviteCode codeToUpdate = inviteCodeRepository.findById(inviteCodeId)
            .orElseThrow(() -> new IllegalArgumentException("Invite code not found with ID: " + inviteCodeId));
        
        Role newRole = roleService.findByNameAndAdminId(roleName, adminId);

        codeToUpdate.setDescription(newDescription);
        codeToUpdate.setAssigningRole(newRole);
        
        return inviteCodeRepository.save(codeToUpdate);
    }

    
    @Transactional
    public void deleteInviteCode(Long id) {
        inviteCodeRepository.deleteById(id);
    }
}

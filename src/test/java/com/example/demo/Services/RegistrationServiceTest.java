package com.example.demo.Services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Entities.InviteCode;
import com.example.demo.Entities.Organization;
import com.example.demo.Entities.RegistrationRequest;
// import com.example.demo.Entities.Role;
import com.example.demo.Repositories.InviteCodeRepository;
import com.example.demo.Repositories.OrganizationRepository;
import com.example.demo.Repositories.RegistrationRequestRepository;
// import com.example.demo.Repositories.RoleRepository;

@SpringBootTest
@Transactional
class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RegistrationRequestRepository registrationRepo;

    @Autowired
    private InviteCodeRepository inviteCodeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    // @Autowired
    // private RoleRepository roleRepository;

    @Test
    void testCreate_InvalidInviteCode_ThrowsException() {
        RegistrationRequest request = new RegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@test.com");
        request.setPhone("555-0000");
        request.setInvitationCode("INVALID_CODE");

        assertThrows(IllegalArgumentException.class, () -> {
            registrationService.create(request);
        });
    }

    @Test
    void testCreate_ValidInviteCode_Success() {
        // Create test organization
        Organization org = new Organization();
        org.setName("Test Org");
        organizationRepository.save(org);

        // Create test role
        // Role role = new Role();
        // role.setName("Test Role");
        // role.setOrganization(org);
        // roleRepository.save(role);

        // Create invite code
        InviteCode inviteCode = new InviteCode();
        inviteCode.setCode("VALID123");
        inviteCode.setOrganization(org);
        // inviteCode.setAssigningRole(role);
        inviteCodeRepository.save(inviteCode);

        // Create registration request
        RegistrationRequest request = new RegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane@test.com");
        request.setPhone("555-1234");
        request.setInvitationCode("VALID123");

        RegistrationRequest result = registrationService.create(request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("jane@test.com", result.getEmail());
        assertEquals(RegistrationRequest.Status.PENDING, result.getStatus());
    }

    @Test
    void testCreate_DuplicateEmail_ThrowsException() {
        // Create first registration
        Organization org = new Organization();
        org.setName("Test Org");
        organizationRepository.save(org);

        // Role role = new Role();
        // role.setName("Test Role");
        // role.setOrganization(org);
        // roleRepository.save(role);

        InviteCode inviteCode = new InviteCode();
        inviteCode.setCode("VALID456");
        inviteCode.setOrganization(org);
        // inviteCode.setAssigningRole(role);
        inviteCodeRepository.save(inviteCode);

        RegistrationRequest request1 = new RegistrationRequest();
        request1.setFirstName("John");
        request1.setLastName("Doe");
        request1.setEmail("duplicate@test.com");
        request1.setPhone("555-0000");
        request1.setInvitationCode("VALID456");
        registrationService.create(request1);

        // Try to create second registration with same email
        RegistrationRequest request2 = new RegistrationRequest();
        request2.setFirstName("Jane");
        request2.setLastName("Smith");
        request2.setEmail("duplicate@test.com");
        request2.setPhone("555-1111");
        request2.setInvitationCode("VALID456");

        assertThrows(IllegalArgumentException.class, () -> {
            registrationService.create(request2);
        });
    }
}
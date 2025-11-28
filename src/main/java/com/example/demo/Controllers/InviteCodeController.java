package com.example.demo.Controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.InviteCode;
import com.example.demo.Entities.Role;
import com.example.demo.Services.InviteCodeService;
import com.example.demo.Services.RoleService;

import jakarta.servlet.http.HttpSession;

@Controller
public class InviteCodeController {
    

    // Dependencies
    private final InviteCodeService inviteCodeService; 
    private final RoleService roleService;

    public InviteCodeController(
            InviteCodeService inviteCodeService, 
            RoleService roleService) {
        this.inviteCodeService = inviteCodeService;
        this.roleService = roleService;
    }


    /**
     * Handles GET request to display the "Manage Invite Codes" page.
     * Fetches real data from the database using the services.
     */
    @GetMapping("/admin/invites")
    public String manageInviteCodes(Model model, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        
        // Fetch ALL Invite Codes using the service
        List<InviteCode> inviteCodes = inviteCodeService.findAllByAdminId(userId);
        model.addAttribute("inviteCodes", inviteCodes);

        // Fetch ALL available Roles using the service
        List<Role> availableRoleEntities = roleService.findAllByAdminId(userId); 
        
        // Map Role entities to just their names (assuming Role::getName returns the role identifier string)
        List<String> availableRoles = availableRoleEntities.stream()
            .map(Role::getName) 
            .collect(Collectors.toList());
            
        model.addAttribute("availableRoles", availableRoles);

        return "admin-invites";
    }

    /**
     * Handles form submission to create a new invite code.
     * Generates a unique code and saves the entity via the service.
     */
    @PostMapping("/admin/invites/create")
    public String createInviteCode(
        @RequestParam String description, 
        @RequestParam String assignedRole,
        HttpSession session) 
    {

        Long userId = (Long) session.getAttribute("userId");

        inviteCodeService.createInviteCode(userId, assignedRole, description);

        // Redirect back to the invite codes list page
        return "redirect:/admin/invites";
    }

    /**
     * Handles form submission to edit an existing invite code.
     */
    @PostMapping("/admin/invites/edit")
    public String editInviteCode(
        @RequestParam Long id,
        @RequestParam String description, 
        @RequestParam String assignedRole,
        HttpSession session) 
    {
        
        Long userId = (Long) session.getAttribute("userId");

        // 2. Update the entity via the InviteCodeService
        inviteCodeService.editInviteCode(id, description, assignedRole, userId);

        // Redirect back to the invite codes list page
        return "redirect:/admin/invites";
    }
    
    /**
     * Handles form submission to delete an invite code.
     */
    @PostMapping("/admin/invites/delete")
    public String deleteInviteCode(@RequestParam Long codeId) {
        
        // Delete the entity via the InviteCodeService
        inviteCodeService.deleteInviteCode(codeId);

        // Redirect back to the invite codes list page
        return "redirect:/admin/invites";
    }
}

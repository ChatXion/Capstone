package com.example.demo.Controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.RegistrationRequest;
import com.example.demo.Services.RegistrationService;

import jakarta.servlet.http.HttpSession;

@Controller
    public class RegistrationController {
    private final RegistrationService service;
    public RegistrationController(RegistrationService service) { this.service = service; }

@GetMapping("/admin/registrations")
public String viewRegistrations(Model model, HttpSession session) {
    // Get firstName from session for navigation
    String firstName = (String) session.getAttribute("firstName");
    model.addAttribute("firstName", firstName != null ? firstName : "Admin");
    
    // Fetch all pending registration requests from database
    List<RegistrationRequest> registrations = service.getAllPendingRegistrations();
    
    model.addAttribute("registrations", registrations);
    
    return "admin-registrations";
}

    @ModelAttribute("registration")
    public RegistrationRequest registrationRequest() {
        return new RegistrationRequest();
    }
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registration", new RegistrationRequest()); // <- ensure object present
        return "registration";
    }
    @PostMapping("/register")
    public String submitRegistration(@ModelAttribute("registration") RegistrationRequest registration, Model model) {
        System.out.println("DEBUG: submitRegistration called -> email=" + registration.getEmail() + ", invite=" + registration.getInvitationCode());
        try {
            // actually persist the request
            RegistrationRequest saved = service.create(registration);
            System.out.println("DEBUG: saved registration id=" + saved.getId());
            return "redirect:/register?submitted";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registration", registration);
            return "registration";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
            model.addAttribute("registration", registration);
            return "registration";
        }
    }
    @PostMapping("/admin/registrations/approve")
    public String approveRegistration(@RequestParam int registrationId) {
        
        System.out.println("Approving registration ID: " + registrationId);
        service.approve((long) registrationId); // Call service to approve
        return "redirect:/admin/registrations";
    }

    @PostMapping("/admin/registrations/deny")
    public String denyRegistration(@RequestParam int registrationId, 
                                   @RequestParam(required = false) String reason) {
        
        System.out.println("Denying registration ID: " + registrationId + " - Reason: " + reason);
        service.deny((long) registrationId, reason); // Call service to deny
        return "redirect:/admin/registrations";
    }


    public static class PendingRegistration {
        private int id;
        private String invitationCode;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private LocalDate submittedDate;
        private String status;

        public PendingRegistration(int id, String invitationCode, String firstName, 
                                 String lastName, String email, String phone, 
                                 LocalDate submittedDate, String status) {
            this.id = id;
            this.invitationCode = invitationCode;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.submittedDate = submittedDate;
            this.status = status;
        }

        // Getters
        public int getId() { return id; }
        public String getInvitationCode() { return invitationCode; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public LocalDate getSubmittedDate() { return submittedDate; }
        public String getStatus() { return status; }
    }

}
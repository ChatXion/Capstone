package com.example.demo.Controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.PTORequest;
import com.example.demo.Services.EmployeeService;
import com.example.demo.Services.PTOService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PTOController {
    
    private final EmployeeService employeeService;
    private final PTOService ptoService;
    
    public PTOController(EmployeeService employeeService, PTOService ptoService) {
        this.employeeService = employeeService;
        this.ptoService = ptoService;
    }
    
    @GetMapping("/employee/pto")
    public String viewPTO(Model model, HttpSession session) {
        // Get employee ID from session
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Fetch employee data
        Optional<Employee> employeeOpt = employeeService.getEmployee(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        Employee employee = employeeOpt.get();
        model.addAttribute("firstName", employee.getFirstName());
        model.addAttribute("lastName", employee.getLastName());
        
        // Get PTO balance
        double ptoBalance = employeeService.getEmployeePTOBalance(userId);
        model.addAttribute("ptoBalance", ptoBalance);
        
        // Fetch all PTO requests for this employee
        List<PTORequest> ptoRequests = ptoService.findAllPTORequestsByEmployeeId(userId);
        
        // Convert to display objects
        List<PTORequestDisplay> displayRequests = new ArrayList<>();
        for (PTORequest request : ptoRequests) {
            PTORequestDisplay display = new PTORequestDisplay();
            display.setId(request.getId());
            display.setRequestType(request.getRequestType()); // Will return "PTO" default
            display.setStartDate(request.getStartDate());
            display.setEndDate(request.getEndDate());
            display.setHoursRequested(request.getHoursRequested());
            display.setReason(request.getReason()); // Will return "" default
            display.setApprovalStatus(request.getApprovalStatus());
            display.setSubmittedDate(request.getSubmittedDate()); // Calculated
            display.setApprovedBy(request.getApprovedBy()); // Will return "" default
            display.setRejectionReason(request.getRejectionReason()); // Will return "" default
            
            displayRequests.add(display);
        }
        
        model.addAttribute("ptoRequests", displayRequests);
        
        return "employee-pto";
    }
    
    @PostMapping("/employee/pto/create")
    public String createPTORequest(
            @RequestParam(required = false) String requestType,  // Not used, but keep for form compatibility
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String reason,  // Not used, but keep for form compatibility
            HttpSession session,
            Model model) {
        
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            // Call simplified method that only needs dates
            ptoService.createPTORequest(userId, startDate, endDate);
            return "redirect:/employee/pto";
        } catch (Exception e) {
            System.err.println("Error creating PTO request: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "redirect:/employee/pto?error=" + e.getMessage();
        }
    }
    
    @PostMapping("/employee/pto/{id}/edit")
    public String editPTORequest(
            @PathVariable Long id,
            @RequestParam(required = false) String requestType,  // Not used, but keep for form compatibility
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String reason,  // Not used, but keep for form compatibility
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            // Call simplified method with only dates
            ptoService.updatePTORequest(id, startDate, endDate, userId);
        } catch (Exception e) {
            System.err.println("Error updating PTO request: " + e.getMessage());
            return "redirect:/employee/pto?error=" + e.getMessage();
        }
        
        return "redirect:/employee/pto";
    }
    
    @PostMapping("/employee/pto/{id}/delete")
    public String deletePTORequest(
            @PathVariable Long id,
            HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        ptoService.deletePTORequest(id, userId);
        
        return "redirect:/employee/pto";
    }
    
    // Display class for PTO requests
    public static class PTORequestDisplay {
        private Long id;
        private String requestType;
        private LocalDate startDate;
        private LocalDate endDate;
        private Double hoursRequested;
        private String reason;
        private String approvalStatus;
        private LocalDate submittedDate;
        private String approvedBy;
        private String rejectionReason;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getRequestType() { return requestType; }
        public void setRequestType(String requestType) { this.requestType = requestType; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public Double getHoursRequested() { return hoursRequested; }
        public void setHoursRequested(Double hoursRequested) { this.hoursRequested = hoursRequested; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        
        public String getApprovalStatus() { return approvalStatus; }
        public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
        
        public LocalDate getSubmittedDate() { return submittedDate; }
        public void setSubmittedDate(LocalDate submittedDate) { this.submittedDate = submittedDate; }
        
        public String getApprovedBy() { return approvedBy; }
        public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
        
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }
}
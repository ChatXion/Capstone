package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.PTORequest;
import com.example.demo.Entities.Timesheet;
import com.example.demo.Services.PTOService;
import com.example.demo.Services.TimesheetService;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ApprovalsController {

    private final TimesheetService timesheetService;
    private final PTOService ptoService;

    public ApprovalsController(TimesheetService timesheetService, PTOService ptoService) {
        this.timesheetService = timesheetService;
        this.ptoService = ptoService;
    }

    @GetMapping("/admin/approvals")
    public String viewApprovals(Model model, HttpSession session) {
        // Get admin info from session
        Long userId = (Long) session.getAttribute("userId");
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");
        
        // Fetch pending timesheets from database
        List<Timesheet> pendingTimesheets = timesheetService.getAllPendingTimesheets();
        List<TimesheetApproval> timesheets = new ArrayList<>();
        
        for (Timesheet ts : pendingTimesheets) {
            if (ts.getEmployee() != null) {
                TimesheetApproval approval = new TimesheetApproval(
                    ts.getId().intValue(),
                    ts.getEmployee().getId().toString(),
                    ts.getEmployee().getFirstName() + " " + ts.getEmployee().getLastName(),
                    ts.getStartDate() != null ? ts.getStartDate() : LocalDate.now().minusDays(7),
                    ts.getEndDate() != null ? ts.getEndDate() : LocalDate.now(),
                    ts.getTotalHours(),
                    ts.getApprovalStatus(),
                    ts.getSubmittedDate() != null ? ts.getSubmittedDate() : LocalDate.now(),
                    ts.getNotes()
                );
                timesheets.add(approval);
            }
        }
        
        // Fetch pending PTO requests from database
        List<PTORequest> pendingPTORequests = ptoService.getAllPendingPTORequests();
        List<PTORequestDisplay> ptoRequests = new ArrayList<>();
        
        for (PTORequest pto : pendingPTORequests) {
            if (pto.getEmployee() != null && pto.getStartDate() != null && pto.getEndDate() != null) {
                // Calculate days from dates
                long daysBetween = ChronoUnit.DAYS.between(pto.getStartDate(), pto.getEndDate()) + 1;
                double daysRequested = daysBetween;
                
                // Estimate submitted date (e.g., 1 week before start date)
                LocalDate submittedDate = pto.getStartDate().minusDays(7);
                
                PTORequestDisplay ptoDisplay = new PTORequestDisplay(
                    pto.getId().intValue(),
                    pto.getEmployee().getId().toString(),
                    pto.getEmployee().getFirstName() + " " + pto.getEmployee().getLastName(),
                    "PTO", // Default request type since not in DB
                    pto.getStartDate(),
                    pto.getEndDate(),
                    daysRequested,
                    pto.getApprovalStatus(),
                    submittedDate,
                    "" // Empty reason since not in DB
                );
                ptoRequests.add(ptoDisplay);
            }
        }
        
        model.addAttribute("timesheets", timesheets);
        model.addAttribute("ptoRequests", ptoRequests);
        
        return "admin-approvals";
    }

    @PostMapping("/admin/approvals/timesheet/approve")
    public String approveTimesheet(@RequestParam int timesheetId, HttpSession session) {
        // Get admin name from session
        String firstName = (String) session.getAttribute("firstName");
        String approverName = firstName != null ? firstName : "Admin";
        
        try {
            // Approve the timesheet using the service
            timesheetService.approveTimesheet((long) timesheetId, approverName);
            System.out.println("Successfully approved timesheet ID: " + timesheetId + " by " + approverName);
        } catch (Exception e) {
            System.err.println("Error approving timesheet ID " + timesheetId + ": " + e.getMessage());
        }
        
        return "redirect:/admin/approvals";
    }

    @PostMapping("/admin/approvals/timesheet/deny")
    public String denyTimesheet(@RequestParam int timesheetId, 
                               @RequestParam(required = false) String reason,
                               HttpSession session) {
        // Get admin name from session
        String firstName = (String) session.getAttribute("firstName");
        String denierName = firstName != null ? firstName : "Admin";
        
        String rejectionReason = (reason != null && !reason.trim().isEmpty()) ? reason : "Not approved";
        
        try {
            // Deny the timesheet using the service
            timesheetService.denyTimesheet((long) timesheetId, rejectionReason, denierName);
            System.out.println("Successfully denied timesheet ID: " + timesheetId + " - Reason: " + rejectionReason);
        } catch (Exception e) {
            System.err.println("Error denying timesheet ID " + timesheetId + ": " + e.getMessage());
        }
        
        return "redirect:/admin/approvals";
    }

    @PostMapping("/admin/approvals/pto/approve")
    public String approvePTO(@RequestParam int ptoId, HttpSession session) {
        try {
            // Approve the PTO request using the service
            // Note: Since we don't store approved_by in the DB, we just approve without tracking approver
            ptoService.approvePTORequest((long) ptoId);
            System.out.println("Successfully approved PTO request ID: " + ptoId);
        } catch (Exception e) {
            System.err.println("Error approving PTO request ID " + ptoId + ": " + e.getMessage());
        }
        
        return "redirect:/admin/approvals";
    }

    @PostMapping("/admin/approvals/pto/deny")
    public String denyPTO(@RequestParam int ptoId, 
                         @RequestParam(required = false) String reason) {
        try {
            // Deny the PTO request using the service
            // Note: Since we don't store rejection_reason in the DB, we just deny without storing reason
            ptoService.denyPTORequest((long) ptoId);
            System.out.println("Successfully denied PTO request ID: " + ptoId);
        } catch (Exception e) {
            System.err.println("Error denying PTO request ID " + ptoId + ": " + e.getMessage());
        }
        
        return "redirect:/admin/approvals";
    }

    // Display classes matching the existing structure
    public static class TimesheetApproval {
        private int id;
        private String employeeId;
        private String employeeName;
        private LocalDate startDate;
        private LocalDate endDate;
        private double totalHours;
        private String status;
        private LocalDate submittedDate;
        private String notes;

        public TimesheetApproval(int id, String employeeId, String employeeName, 
                               LocalDate startDate, LocalDate endDate, double totalHours,
                               String status, LocalDate submittedDate, String notes) {
            this.id = id;
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalHours = totalHours;
            this.status = status;
            this.submittedDate = submittedDate;
            this.notes = notes;
        }

        // Getters
        public int getId() { return id; }
        public String getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public double getTotalHours() { return totalHours; }
        public String getStatus() { return status; }
        public LocalDate getSubmittedDate() { return submittedDate; }
        public String getNotes() { return notes; }
    }

    public static class PTORequestDisplay {
        private int id;
        private String employeeId;
        private String employeeName;
        private String requestType;
        private LocalDate startDate;
        private LocalDate endDate;
        private double daysRequested;
        private String status;
        private LocalDate submittedDate;
        private String reason;

        public PTORequestDisplay(int id, String employeeId, String employeeName, 
                         String requestType, LocalDate startDate, LocalDate endDate,
                         double daysRequested, String status, LocalDate submittedDate, 
                         String reason) {
            this.id = id;
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.requestType = requestType;
            this.startDate = startDate;
            this.endDate = endDate;
            this.daysRequested = daysRequested;
            this.status = status;
            this.submittedDate = submittedDate;
            this.reason = reason;
        }

        // Getters
        public int getId() { return id; }
        public String getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public String getRequestType() { return requestType; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public double getDaysRequested() { return daysRequested; }
        public String getStatus() { return status; }
        public LocalDate getSubmittedDate() { return submittedDate; }
        public String getReason() { return reason; }
    }
}
package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entities.Admin;
import com.example.demo.Entities.PTORequest;
import com.example.demo.Entities.Timesheet;
import com.example.demo.Repositories.AdminRepository;
import com.example.demo.Repositories.PTORequestRepository;
import com.example.demo.Repositories.TimesheetRepository;
import com.example.demo.Services.PTOService;
import com.example.demo.Services.TimesheetService;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
public class ApprovalsController {

    private final TimesheetService timesheetService;
    private final PTOService ptoService;
    private final AdminRepository adminRepository;
    private final TimesheetRepository timesheetRepository;
    private final PTORequestRepository ptoRequestRepository;

    public ApprovalsController(
            TimesheetService timesheetService,
            PTOService ptoService,
            AdminRepository adminRepository,
            TimesheetRepository timesheetRepository,
            PTORequestRepository ptoRequestRepository) {
        this.timesheetService = timesheetService;
        this.ptoService = ptoService;
        this.adminRepository = adminRepository;
        this.timesheetRepository = timesheetRepository;
        this.ptoRequestRepository = ptoRequestRepository;
    }

    @GetMapping("/admin/approvals")
    public String showApprovals(Model model, HttpSession session) {
        Long adminId = (Long) session.getAttribute("userId");
        String firstName = (String) session.getAttribute("firstName");

        if (adminId == null) {
            return "redirect:/login";
        }

        // Get admin to access their organization
        Optional<Admin> adminOpt = adminRepository.findById(adminId);
        if (adminOpt.isEmpty()) {
            return "redirect:/login";
        }
        Admin admin = adminOpt.get();
        Long organizationId = admin.getOrganization().getId();

        // 1. Fetch pending Timesheet Entities
        List<Timesheet> timesheetEntities = timesheetRepository
                .findByApprovalStatusAndEmployeeOrganizationId("pending", organizationId);

        // 2. Map Timesheet Entities to TimesheetApproval DTOs
        List<TimesheetApproval> timesheets = timesheetEntities.stream()
                .map(ts -> new TimesheetApproval(
                    ts.getId().intValue(),
                    ts.getEmployee().getId().toString(), 
                    ts.getEmployee().getFirstName() + " " + ts.getEmployee().getLastName(),
                    // StartDate and EndDate should be available as they're calculated fields
                    ts.getStartDate(),
                    ts.getEndDate(),
                    // FIX 1: ts.getTotalHours() returns a primitive double, 
                    // so remove the null check and .doubleValue().
                    ts.getTotalHours(),
                    ts.getApprovalStatus(),
                    // Null check is needed because Timesheet::getSubmittedDate can return null
                    (ts.getSubmittedDate() != null ? ts.getSubmittedDate() : LocalDate.now()),
                    ts.getNotes()
                ))
                .collect(java.util.stream.Collectors.toList());

        // 3. Fetch pending PTO Entities
        List<PTORequest> ptoEntities = ptoRequestRepository
                .findByApprovalStatusAndEmployeeOrganizationId("pending", organizationId);

        // 4. Map PTO Entities to PTORequestDisplay DTOs
        List<PTORequestDisplay> ptoRequests = ptoEntities.stream()
            .map(pto -> {
                // FIX 2: The PTORequest entity is missing getDaysRequested(), 
                // so we must calculate the days directly from the dates.
                long days = 0;
                if (pto.getStartDate() != null && pto.getEndDate() != null) {
                    days = java.time.temporal.ChronoUnit.DAYS.between(pto.getStartDate(), pto.getEndDate()) + 1;
                }
                
                return new PTORequestDisplay(
                    pto.getId().intValue(),
                    pto.getEmployee().getId().toString(),
                    pto.getEmployee().getFirstName() + " " + pto.getEmployee().getLastName(),
                    pto.getRequestType(),
                    pto.getStartDate(),
                    pto.getEndDate(),
                    // Pass calculated days, cast to double as the DTO expects
                    (double) days, 
                    pto.getApprovalStatus(),
                    // FIX 3: pto.getSubmittedDate() already returns LocalDate, 
                    // so remove the redundant .toLocalDate() call.
                    pto.getSubmittedDate(), 
                    pto.getReason()
                );
            })
            .collect(java.util.stream.Collectors.toList());


        // 5. Add the MAPPED DTO lists to the model using the CORRECT keys
        model.addAttribute("timesheets", timesheets);
        model.addAttribute("ptoRequests", ptoRequests);
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");

        return "admin-approvals";
    }

    @PostMapping("/admin/approvals/timesheet/approve")
    public String approveTimesheet(@RequestParam int timesheetId, HttpSession session) {
        String firstName = (String) session.getAttribute("firstName");
        String approverName = firstName != null ? firstName : "Admin";

        try {
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
        String firstName = (String) session.getAttribute("firstName");
        String denierName = firstName != null ? firstName : "Admin";

        String rejectionReason = (reason != null && !reason.trim().isEmpty()) ? reason : "Not approved";

        try {
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
        public int getId() {
            return id;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public double getTotalHours() {
            return totalHours;
        }

        public String getStatus() {
            return status;
        }

        public LocalDate getSubmittedDate() {
            return submittedDate;
        }

        public String getNotes() {
            return notes;
        }
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
        public int getId() {
            return id;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public String getRequestType() {
            return requestType;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public double getDaysRequested() {
            return daysRequested;
        }

        public String getStatus() {
            return status;
        }

        public LocalDate getSubmittedDate() {
            return submittedDate;
        }

        public String getReason() {
            return reason;
        }
    }
}
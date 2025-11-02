package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ApprovalsController {

    @GetMapping("/admin/approvals")
    public String viewApprovals(Model model, HttpSession session) {
        // Get firstName from session for navigation
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");
        
        // Dummy timesheet approval data
        List<TimesheetApproval> timesheets = new ArrayList<>();
        
        timesheets.add(new TimesheetApproval(
            1,
            "12345",
            "John Doe",
            LocalDate.of(2025, 10, 14),
            LocalDate.of(2025, 10, 20),
            40.0,
            "Pending",
            LocalDate.of(2025, 10, 21),
            "Regular work week"
        ));
        
        timesheets.add(new TimesheetApproval(
            2,
            "12346",
            "Jane Smith",
            LocalDate.of(2025, 10, 14),
            LocalDate.of(2025, 10, 20),
            45.5,
            "Pending",
            LocalDate.of(2025, 10, 21),
            "Includes 5.5 hours overtime"
        ));
        
        timesheets.add(new TimesheetApproval(
            3,
            "12347",
            "Michael Johnson",
            LocalDate.of(2025, 10, 14),
            LocalDate.of(2025, 10, 20),
            38.0,
            "Pending",
            LocalDate.of(2025, 10, 22),
            "Took 2 hours off on Friday"
        ));
        
        timesheets.add(new TimesheetApproval(
            4,
            "12348",
            "Emily Davis",
            LocalDate.of(2025, 10, 14),
            LocalDate.of(2025, 10, 20),
            40.0,
            "Pending",
            LocalDate.of(2025, 10, 23),
            "Standard week"
        ));
        
        // Dummy PTO request data
        List<PTORequest> ptoRequests = new ArrayList<>();
        
        ptoRequests.add(new PTORequest(
            1,
            "12349",
            "Robert Wilson",
            "Vacation",
            LocalDate.of(2025, 11, 10),
            LocalDate.of(2025, 11, 15),
            5.0,
            "Pending",
            LocalDate.of(2025, 10, 20),
            "Family trip to the beach"
        ));
        
        ptoRequests.add(new PTORequest(
            2,
            "12345",
            "John Doe",
            "Sick Leave",
            LocalDate.of(2025, 10, 28),
            LocalDate.of(2025, 10, 28),
            1.0,
            "Pending",
            LocalDate.of(2025, 10, 24),
            "Doctor's appointment"
        ));
        
        ptoRequests.add(new PTORequest(
            3,
            "12350",
            "Sarah Martinez",
            "Personal",
            LocalDate.of(2025, 11, 5),
            LocalDate.of(2025, 11, 7),
            3.0,
            "Pending",
            LocalDate.of(2025, 10, 22),
            "Moving to new apartment"
        ));
        
        ptoRequests.add(new PTORequest(
            4,
            "12347",
            "Michael Johnson",
            "Vacation",
            LocalDate.of(2025, 12, 20),
            LocalDate.of(2025, 12, 31),
            10.0,
            "Pending",
            LocalDate.of(2025, 10, 23),
            "Holiday vacation"
        ));
        
        ptoRequests.add(new PTORequest(
            5,
            "12346",
            "Jane Smith",
            "Personal",
            LocalDate.of(2025, 11, 1),
            LocalDate.of(2025, 11, 1),
            1.0,
            "Pending",
            LocalDate.of(2025, 10, 25),
            "Child's school event"
        ));
        
        model.addAttribute("timesheets", timesheets);
        model.addAttribute("ptoRequests", ptoRequests);
        
        return "admin-approvals";
    }

    @PostMapping("/admin/approvals/timesheet/approve")
    public String approveTimesheet(@RequestParam int timesheetId) {

        System.out.println("Approving timesheet ID: " + timesheetId);
        
        return "redirect:/admin/approvals";
    }

    @PostMapping("/admin/approvals/timesheet/deny")
    public String denyTimesheet(@RequestParam int timesheetId, 
                               @RequestParam(required = false) String reason) {

        System.out.println("Denying timesheet ID: " + timesheetId + " - Reason: " + reason);
        
        return "redirect:/admin/approvals";
    }

    @PostMapping("/admin/approvals/pto/approve")
    public String approvePTO(@RequestParam int ptoId) {

        System.out.println("Approving PTO request ID: " + ptoId);
        
        return "redirect:/admin/approvals";
    }

    @PostMapping("/admin/approvals/pto/deny")
    public String denyPTO(@RequestParam int ptoId, 
                         @RequestParam(required = false) String reason) {

        System.out.println("Denying PTO request ID: " + ptoId + " - Reason: " + reason);
        
        return "redirect:/admin/approvals";
    }

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


    public static class PTORequest {
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

        public PTORequest(int id, String employeeId, String employeeName, 
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
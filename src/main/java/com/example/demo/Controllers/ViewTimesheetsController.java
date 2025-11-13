package com.example.demo.Controllers;

import java.math.BigDecimal;
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
import com.example.demo.Entities.Paycode;
import com.example.demo.Entities.Timesheet;
import com.example.demo.Entities.TimesheetEntry;
import com.example.demo.Services.EmployeeService;
import com.example.demo.Services.PayCodeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewTimesheetsController {

    private final EmployeeService employeeService;
    private final PayCodeService payCodeService;

    public ViewTimesheetsController(EmployeeService employeeService, PayCodeService payCodeService) {
        this.employeeService = employeeService;
        this.payCodeService = payCodeService;
    }

    @GetMapping("/employee/timesheets")
    public String viewAllTimesheets(Model model, HttpSession session) {
        // Get employee ID from session
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        // Fetch employee data using service
        Optional<Employee> employeeOpt = employeeService.getEmployee(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/login";
        }

        Employee employee = employeeOpt.get();
        model.addAttribute("firstName", employee.getFirstName());
        model.addAttribute("lastName", employee.getLastName());

        // Fetch all timesheets for this employee using service
        List<Timesheet> timesheets = employeeService.findAllTimesheets(userId);

        // Convert to display objects
        List<TimesheetDisplay> displayTimesheets = new ArrayList<>();
        for (Timesheet timesheet : timesheets) {
            TimesheetDisplay display = new TimesheetDisplay();
            display.setId(timesheet.getId());
            display.setWeek(timesheet.getWeek());
            display.setApprovalStatus(timesheet.getApprovalStatus());
            display.setApprovedBy(timesheet.getApprovedBy());
            display.setRejectionReason(timesheet.getRejectionReason());

            // Calculate total hours from entries
            double totalHours = 0;
            if (timesheet.getEntries() != null) {
                for (TimesheetEntry entry : timesheet.getEntries()) {
                    totalHours += entry.getHoursWorked();
                }
            }
            display.setTotalHours(totalHours);

            // Get date range from entries
            if (timesheet.getEntries() != null && !timesheet.getEntries().isEmpty()) {
                LocalDate minDate = null;
                LocalDate maxDate = null;

                for (TimesheetEntry entry : timesheet.getEntries()) {
                    if (entry.getDate() != null) {
                        if (minDate == null || entry.getDate().isBefore(minDate)) {
                            minDate = entry.getDate();
                        }
                        if (maxDate == null || entry.getDate().isAfter(maxDate)) {
                            maxDate = entry.getDate();
                        }
                    }
                }

                display.setStartDate(minDate);
                display.setEndDate(maxDate);
            }

            displayTimesheets.add(display);
        }

        model.addAttribute("timesheets", displayTimesheets);

        return "employee-timesheets";
    }

    @GetMapping("/employee/timesheets/{id}")
    public String viewTimesheetDetails(@PathVariable Long id, Model model, HttpSession session) {
        // Get employee ID from session
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        // Fetch employee data using service
        Optional<Employee> employeeOpt = employeeService.getEmployee(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/login";
        }

        Employee employee = employeeOpt.get();
        model.addAttribute("firstName", employee.getFirstName());

        // Fetch the specific timesheet using service
        Optional<Timesheet> timesheetOpt = employeeService.getTimesheet(userId, id);

        if (timesheetOpt.isEmpty()) {
            // Timesheet not found or doesn't belong to this employee
            return "redirect:/employee/timesheets";
        }

        Timesheet timesheet = timesheetOpt.get();

        // Create display object
        TimesheetDisplay display = new TimesheetDisplay();
        display.setId(timesheet.getId());
        display.setWeek(timesheet.getWeek());
        display.setApprovalStatus(timesheet.getApprovalStatus());
        display.setApprovedBy(timesheet.getApprovedBy());
        display.setRejectionReason(timesheet.getRejectionReason());

        // Get entries
        List<TimesheetEntryDisplay> entryDisplays = new ArrayList<>();
        double totalHours = 0;

        if (timesheet.getEntries() != null) {
            for (TimesheetEntry entry : timesheet.getEntries()) {
                TimesheetEntryDisplay entryDisplay = new TimesheetEntryDisplay();
                entryDisplay.setId(entry.getId());
                entryDisplay.setDate(entry.getDate());
                entryDisplay.setHoursWorked(entry.getHoursWorked());

                // CRITICAL FIX: Always set payCode fields, even if null
                if (entry.getPaycode() != null) {
                    entryDisplay.setPayCodeId(entry.getPaycode().getId());
                    entryDisplay.setPayCodeName(entry.getPaycode().getName());
                    entryDisplay.setPayCodeCode(entry.getPaycode().getCode());
                    entryDisplay.setHourlyRate(BigDecimal.valueOf(entry.getPaycode().getHourlyRate()));
                } else {
                    // Set default values if payCode is null
                    entryDisplay.setPayCodeName("N/A");
                    entryDisplay.setPayCodeCode("N/A");
                    entryDisplay.setHourlyRate(java.math.BigDecimal.ZERO);
                }

                entryDisplays.add(entryDisplay);
                totalHours += entry.getHoursWorked();
            }
        }

        display.setTotalHours(totalHours);
        display.setEntries(entryDisplays);

        model.addAttribute("timesheet", display);

        // Get employee's organization ID and fetch pay codes if timesheet is pending
        if ("pending".equals(timesheet.getApprovalStatus())) {
            Long organizationId = null;
            if (employee.getOrganization() != null) {
                organizationId = employee.getOrganization().getId();
            }

            if (organizationId != null) {
                List<Paycode> payCodes = payCodeService.getPaycodesByOrganization(organizationId);
                model.addAttribute("payCodes", payCodes);
            }
        }

        return "employee-timesheet-details";
    }

    @PostMapping("/employee/timesheets/{id}/entry/{entryId}/edit")
    public String editTimesheetEntry(
            @PathVariable Long id,
            @PathVariable Long entryId,
            @RequestParam LocalDate date,
            @RequestParam double hoursWorked,
            @RequestParam Long payCodeId,
            HttpSession session) {

        // Get employee ID from session
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        // Verify timesheet belongs to employee and is pending
        Optional<Timesheet> timesheetOpt = employeeService.getTimesheet(userId, id);
        if (timesheetOpt.isEmpty() || !"pending".equals(timesheetOpt.get().getApprovalStatus())) {
            return "redirect:/employee/timesheets/" + id;
        }

        // Update the entry
        employeeService.updateTimesheetEntry(entryId, date, hoursWorked, payCodeId);

        return "redirect:/employee/timesheets/" + id;
    }

    @PostMapping("/employee/timesheets/{id}/entry/{entryId}/delete")
    public String deleteTimesheetEntry(
            @PathVariable Long id,
            @PathVariable Long entryId,
            HttpSession session) {

        // Get employee ID from session
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        // Verify timesheet belongs to employee and is pending
        Optional<Timesheet> timesheetOpt = employeeService.getTimesheet(userId, id);
        if (timesheetOpt.isEmpty() || !"pending".equals(timesheetOpt.get().getApprovalStatus())) {
            return "redirect:/employee/timesheets/" + id;
        }

        // Delete the entry
        employeeService.deleteTimesheetEntry(entryId);

        return "redirect:/employee/timesheets/" + id;
    }

    // Display class for timesheets list
    public static class TimesheetDisplay {
        private Long id;
        private int week;
        private LocalDate startDate;
        private LocalDate endDate;
        private double totalHours;
        private String approvalStatus;
        private String approvedBy;
        private String rejectionReason;
        private List<TimesheetEntryDisplay> entries;

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public int getWeek() {
            return week;
        }

        public void setWeek(int week) {
            this.week = week;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public double getTotalHours() {
            return totalHours;
        }

        public void setTotalHours(double totalHours) {
            this.totalHours = totalHours;
        }

        public String getApprovalStatus() {
            return approvalStatus;
        }

        public void setApprovalStatus(String approvalStatus) {
            this.approvalStatus = approvalStatus;
        }

        public String getApprovedBy() {
            return approvedBy;
        }

        public void setApprovedBy(String approvedBy) {
            this.approvedBy = approvedBy;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }

        public void setRejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
        }

        public List<TimesheetEntryDisplay> getEntries() {
            return entries;
        }

        public void setEntries(List<TimesheetEntryDisplay> entries) {
            this.entries = entries;
        }
    }

    // Display class for timesheet entries - FIXED VERSION
    public static class TimesheetEntryDisplay {
        private Long id;
        private LocalDate date;
        private double hoursWorked;
        private Long payCodeId;
        private String payCodeName;
        private String payCodeCode;
        private java.math.BigDecimal hourlyRate;

        // Default constructor with safe initialization
        public TimesheetEntryDisplay() {
            this.payCodeName = "";
            this.payCodeCode = "";
            this.hourlyRate = java.math.BigDecimal.ZERO;
        }

        // Getters and Setters - All explicitly public with null safety
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public double getHoursWorked() {
            return hoursWorked;
        }

        public void setHoursWorked(double hoursWorked) {
            this.hoursWorked = hoursWorked;
        }

        public Long getPayCodeId() {
            return payCodeId;
        }

        public void setPayCodeId(Long payCodeId) {
            this.payCodeId = payCodeId;
        }

        public String getPayCodeName() {
            return payCodeName != null ? payCodeName : "N/A";
        }

        public void setPayCodeName(String payCodeName) {
            this.payCodeName = payCodeName != null ? payCodeName : "N/A";
        }

        public String getPayCodeCode() {
            return payCodeCode != null ? payCodeCode : "N/A";
        }

        public void setPayCodeCode(String payCodeCode) {
            this.payCodeCode = payCodeCode != null ? payCodeCode : "N/A";
        }

        public java.math.BigDecimal getHourlyRate() {
            return hourlyRate != null ? hourlyRate : java.math.BigDecimal.ZERO;
        }

        public void setHourlyRate(java.math.BigDecimal hourlyRate) {
            this.hourlyRate = hourlyRate != null ? hourlyRate : java.math.BigDecimal.ZERO;
        }

        private Long paycodeId;
        private String paycodeName;
        private String paycodeCode;

        public Long getPaycodeId() {
            return paycodeId;
        }

        public void setPaycodeId(Long paycodeId) {
            this.paycodeId = paycodeId;
        }

        public String getPaycodeName() {
            return paycodeName;
        }

        public void setPaycodeName(String paycodeName) {
            this.paycodeName = paycodeName;
        }

        public String getPaycodeCode() {
            return paycodeCode;
        }

        public void setPaycodeCode(String paycodeCode) {
            this.paycodeCode = paycodeCode;
        }
    }
}
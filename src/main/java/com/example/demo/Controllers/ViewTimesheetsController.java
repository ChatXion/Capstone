package com.example.demo.Controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.Timesheet;
import com.example.demo.Entities.TimesheetEntry;
import com.example.demo.Repositories.EmployeeRepository;
import com.example.demo.Services.EmployeeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewTimesheetsController {

    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;

    public ViewTimesheetsController(EmployeeRepository employeeRepository,
                                   EmployeeService employeeService) {
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
    }

    @GetMapping("/employee/timesheets")
    public String viewAllTimesheets(Model model, HttpSession session) {
        // Get employee ID from session
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Fetch employee data for navigation
        Optional<Employee> employeeOpt = employeeRepository.findById(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        Employee employee = employeeOpt.get();
        model.addAttribute("firstName", employee.getFirstName());
        model.addAttribute("lastName", employee.getLastName());
        
        // Fetch all timesheets for this employee
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
        
        // Fetch employee data for navigation
        Optional<Employee> employeeOpt = employeeRepository.findById(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        Employee employee = employeeOpt.get();
        model.addAttribute("firstName", employee.getFirstName());
        
        // Fetch the specific timesheet
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
                entryDisplay.setDate(entry.getDate());
                entryDisplay.setHoursWorked(entry.getHoursWorked());
                
                if (entry.getPayCode() != null) {
                    entryDisplay.setPayCodeName(entry.getPayCode().getName());
                    entryDisplay.setPayCodeCode(entry.getPayCode().getCode());
                    entryDisplay.setHourlyRate(entry.getPayCode().getHourlyRate());
                }
                
                entryDisplays.add(entryDisplay);
                totalHours += entry.getHoursWorked();
            }
        }
        
        display.setTotalHours(totalHours);
        display.setEntries(entryDisplays);
        
        model.addAttribute("timesheet", display);
        
        return "employee-timesheet-details";
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
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public int getWeek() { return week; }
        public void setWeek(int week) { this.week = week; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public double getTotalHours() { return totalHours; }
        public void setTotalHours(double totalHours) { this.totalHours = totalHours; }
        
        public String getApprovalStatus() { return approvalStatus; }
        public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
        
        public String getApprovedBy() { return approvedBy; }
        public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
        
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
        
        public List<TimesheetEntryDisplay> getEntries() { return entries; }
        public void setEntries(List<TimesheetEntryDisplay> entries) { this.entries = entries; }
    }
    
    // Display class for timesheet entries
    public static class TimesheetEntryDisplay {
        private LocalDate date;
        private double hoursWorked;
        private String payCodeName;
        private String payCodeCode;
        private java.math.BigDecimal hourlyRate;
        
        // Getters and Setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public double getHoursWorked() { return hoursWorked; }
        public void setHoursWorked(double hoursWorked) { this.hoursWorked = hoursWorked; }
        
        public String getPayCodeName() { return payCodeName; }
        public void setPayCodeName(String payCodeName) { this.payCodeName = payCodeName; }
        
        public String getPayCodeCode() { return payCodeCode; }
        public void setPayCodeCode(String payCodeCode) { this.payCodeCode = payCodeCode; }
        
        public java.math.BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(java.math.BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    }
}
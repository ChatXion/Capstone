package com.example.demo.Controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.PayCode;
import com.example.demo.Services.EmployeeService;
import com.example.demo.Services.PayCodeService;
import com.example.demo.Services.TimesheetService;

import jakarta.servlet.http.HttpSession;

@Controller
public class TimesheetController {

    private final EmployeeService employeeService;
    private final PayCodeService payCodeService;
    private final TimesheetService timesheetService;

    public TimesheetController(EmployeeService employeeService, 
                              PayCodeService payCodeService,
                              TimesheetService timesheetService) {
        this.employeeService = employeeService;
        this.payCodeService = payCodeService;
        this.timesheetService = timesheetService;
    }

    @GetMapping("/timesheet")
    public String timesheetForm(Model model, HttpSession session) {
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
        
        // Get firstName from session for navigation
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "User");
        
        // Get pay codes for this employee's organization
        Long organizationId = null;
        if (employee.getOrganization() != null) {
            organizationId = employee.getOrganization().getId();
        }
        
        if (organizationId != null) {
            List<PayCode> payCodes = payCodeService.getPayCodesByOrganization(organizationId);
            model.addAttribute("payCodes", payCodes);
        }
        
        // Create a new form object
        TimesheetEntryForm form = new TimesheetEntryForm();
        model.addAttribute("timesheetEntry", form);
        
        return "timesheet";
    }

    @PostMapping("/timesheet")
    public String timesheetSubmit(@ModelAttribute("timesheetEntry") TimesheetEntryForm timesheetEntry, 
                                  HttpSession session,
                                  Model model) {
        // Get employee ID from session
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        // Get firstName from session for navigation
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "User");
        
        // Fetch employee data
        Optional<Employee> employeeOpt = employeeService.getEmployee(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/login";
        }
        
        Employee employee = employeeOpt.get();
        
        // Get pay codes for the form (needed if we return to form on error)
        Long organizationId = null;
        if (employee.getOrganization() != null) {
            organizationId = employee.getOrganization().getId();
        }
        
        List<PayCode> payCodes = null;
        if (organizationId != null) {
            payCodes = payCodeService.getPayCodesByOrganization(organizationId);
            model.addAttribute("payCodes", payCodes);
        }
        
        // Validate the form data
        if (timesheetEntry.getWeek() == null || timesheetEntry.getDate() == null || 
            timesheetEntry.getHours() == null || timesheetEntry.getPayCodeId() == null) {
            model.addAttribute("error", "All fields are required.");
            return "timesheet";
        }
        
        try {
            // Save the timesheet entry using the service
            timesheetService.createTimesheetEntry(
                userId, 
                timesheetEntry.getWeek(),
                timesheetEntry.getDate(), 
                timesheetEntry.getHours(), 
                timesheetEntry.getPayCodeId()
            );
            
            System.out.println("Timesheet submitted - Week: " + timesheetEntry.getWeek() + 
                             ", Date: " + timesheetEntry.getDate() + 
                             ", Hours: " + timesheetEntry.getHours() +
                             ", PayCode ID: " + timesheetEntry.getPayCodeId());
            
            return "redirect:/employee/timesheets";
            
        } catch (Exception e) {
            System.err.println("Error creating timesheet entry: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error creating timesheet entry: " + e.getMessage());
            return "timesheet";
        }
    }
    
    // Form object to hold timesheet entry data
    public static class TimesheetEntryForm {
        private Integer week;
        private LocalDate date;
        private Double hours;
        private Long payCodeId;
        
        public TimesheetEntryForm() {
        }
        
        public Integer getWeek() {
            return week;
        }
        
        public void setWeek(Integer week) {
            this.week = week;
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public void setDate(LocalDate date) {
            this.date = date;
        }
        
        public Double getHours() {
            return hours;
        }
        
        public void setHours(Double hours) {
            this.hours = hours;
        }
        
        public Long getPayCodeId() {
            return payCodeId;
        }
        
        public void setPayCodeId(Long payCodeId) {
            this.payCodeId = payCodeId;
        }
    }
}
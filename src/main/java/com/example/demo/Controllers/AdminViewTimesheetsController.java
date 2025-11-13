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
import com.example.demo.Services.TimesheetService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminViewTimesheetsController {

    private final EmployeeService employeeService;
    private final PayCodeService PaycodeService;
    private final TimesheetService timesheetService;

    public AdminViewTimesheetsController(EmployeeService employeeService, 
                                        PayCodeService PayCodeService,
                                        TimesheetService timesheetService) {
        this.employeeService = employeeService;
        this.PaycodeService = PayCodeService;
        this.timesheetService = timesheetService;
    }

    @GetMapping("/admin/view-timesheets/{employeeId}")
    public String viewEmployeeTimesheets(@PathVariable Long employeeId, Model model, HttpSession session) {
        // Get admin firstName from session for navigation
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");
        
        // Fetch employee data
        Optional<Employee> employeeOpt = employeeService.getEmployee(employeeId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/admin/users";
        }
        
        Employee employee = employeeOpt.get();
        model.addAttribute("employeeFirstName", employee.getFirstName());
        model.addAttribute("employeeLastName", employee.getLastName());
        model.addAttribute("employeeId", employeeId);
        
        // Fetch all timesheets for this employee
        List<Timesheet> timesheets = employeeService.findAllTimesheets(employeeId);
        
        // Convert to display objects (reusing ViewTimesheetsController.TimesheetDisplay)
        List<ViewTimesheetsController.TimesheetDisplay> displayTimesheets = new ArrayList<>();
        for (Timesheet timesheet : timesheets) {
            ViewTimesheetsController.TimesheetDisplay display = new ViewTimesheetsController.TimesheetDisplay();
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
        
        return "admin-view-timesheets";
    }
    
    @GetMapping("/admin/view-timesheets/{employeeId}/{timesheetId}")
    public String viewEmployeeTimesheetDetails(@PathVariable Long employeeId, 
                                              @PathVariable Long timesheetId, 
                                              Model model, 
                                              HttpSession session) {
        // Get admin firstName from session for navigation
        String firstName = (String) session.getAttribute("firstName");
        String adminFirstName = firstName != null ? firstName : "Admin";
        model.addAttribute("firstName", adminFirstName);
        
        // Fetch employee data
        Optional<Employee> employeeOpt = employeeService.getEmployee(employeeId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/admin/users";
        }
        
        Employee employee = employeeOpt.get();
        model.addAttribute("employeeFirstName", employee.getFirstName());
        model.addAttribute("employeeLastName", employee.getLastName());
        model.addAttribute("employeeId", employeeId);
        
        // Fetch the specific timesheet
        Optional<Timesheet> timesheetOpt = employeeService.getTimesheet(employeeId, timesheetId);
        
        if (timesheetOpt.isEmpty()) {
            return "redirect:/admin/view-timesheets/" + employeeId;
        }
        
        Timesheet timesheet = timesheetOpt.get();
        
        // Create display object
        ViewTimesheetsController.TimesheetDisplay display = new ViewTimesheetsController.TimesheetDisplay();
        display.setId(timesheet.getId());
        display.setWeek(timesheet.getWeek());
        display.setApprovalStatus(timesheet.getApprovalStatus());
        display.setApprovedBy(timesheet.getApprovedBy());
        display.setRejectionReason(timesheet.getRejectionReason());
        
        // Get entries
        List<ViewTimesheetsController.TimesheetEntryDisplay> entryDisplays = new ArrayList<>();
        double totalHours = 0;
        
        if (timesheet.getEntries() != null) {
            for (TimesheetEntry entry : timesheet.getEntries()) {
                ViewTimesheetsController.TimesheetEntryDisplay entryDisplay = new ViewTimesheetsController.TimesheetEntryDisplay();
                entryDisplay.setId(entry.getId());
                entryDisplay.setDate(entry.getDate());
                entryDisplay.setHoursWorked(entry.getHoursWorked());
                
                if (entry.getPaycode() != null) {
                    entryDisplay.setPaycodeId(entry.getPaycode().getId());
                    entryDisplay.setPaycodeName(entry.getPaycode().getName());
                    entryDisplay.setPaycodeCode(entry.getPaycode().getCode());
                    entryDisplay.setHourlyRate(BigDecimal.valueOf(entry.getPaycode().getHourlyRate()));
                }
                
                entryDisplays.add(entryDisplay);
                totalHours += entry.getHoursWorked();
            }
        }
        
        display.setTotalHours(totalHours);
        display.setEntries(entryDisplays);
        
        model.addAttribute("timesheet", display);
        
        // Get pay codes if timesheet is pending (for editing)
        if ("pending".equals(timesheet.getApprovalStatus())) {
            Long organizationId = null;
            if (employee.getOrganization() != null) {
                organizationId = employee.getOrganization().getId();
            }
            
            if (organizationId != null) {
                List<Paycode> Paycodes = PaycodeService.getPaycodesByOrganization(organizationId);
                model.addAttribute("Paycodes", Paycodes);
            }
        }
        
        return "admin-view-timesheet-details";
    }
    
    @PostMapping("/admin/view-timesheets/{employeeId}/{timesheetId}/entry/{entryId}/edit")
    public String editTimesheetEntry(
            @PathVariable Long employeeId,
            @PathVariable Long timesheetId,
            @PathVariable Long entryId,
            @RequestParam LocalDate date,
            @RequestParam double hoursWorked,
            @RequestParam Long PaycodeId,
            HttpSession session) {
        
        // Verify timesheet belongs to employee and is pending
        Optional<Timesheet> timesheetOpt = employeeService.getTimesheet(employeeId, timesheetId);
        if (timesheetOpt.isEmpty() || !"pending".equals(timesheetOpt.get().getApprovalStatus())) {
            return "redirect:/admin/view-timesheets/" + employeeId + "/" + timesheetId;
        }
        
        // Update the entry
        employeeService.updateTimesheetEntry(entryId, date, hoursWorked, PaycodeId);
        
        return "redirect:/admin/view-timesheets/" + employeeId + "/" + timesheetId;
    }
    
    @PostMapping("/admin/view-timesheets/{employeeId}/{timesheetId}/entry/{entryId}/delete")
    public String deleteTimesheetEntry(
            @PathVariable Long employeeId,
            @PathVariable Long timesheetId,
            @PathVariable Long entryId,
            HttpSession session) {
        
        // Verify timesheet belongs to employee and is pending
        Optional<Timesheet> timesheetOpt = employeeService.getTimesheet(employeeId, timesheetId);
        if (timesheetOpt.isEmpty() || !"pending".equals(timesheetOpt.get().getApprovalStatus())) {
            return "redirect:/admin/view-timesheets/" + employeeId + "/" + timesheetId;
        }
        
        // Delete the entry
        employeeService.deleteTimesheetEntry(entryId);
        
        return "redirect:/admin/view-timesheets/" + employeeId + "/" + timesheetId;
    }
    
    @PostMapping("/admin/view-timesheets/{employeeId}/{timesheetId}/approve")
    public String approveTimesheet(
            @PathVariable Long employeeId,
            @PathVariable Long timesheetId,
            HttpSession session) {
        
        // Get admin name from session
        String adminFirstName = (String) session.getAttribute("firstName");
        String approvedBy = adminFirstName != null ? adminFirstName : "Admin";
        
        // Approve the timesheet
        timesheetService.approveTimesheet(timesheetId, approvedBy);
        
        return "redirect:/admin/view-timesheets/" + employeeId;
    }
    
    @PostMapping("/admin/view-timesheets/{employeeId}/{timesheetId}/deny")
    public String denyTimesheet(
            @PathVariable Long employeeId,
            @PathVariable Long timesheetId,
            @RequestParam String rejectionReason,
            HttpSession session) {
        
        // Get admin name from session
        String adminFirstName = (String) session.getAttribute("firstName");
        String deniedBy = adminFirstName != null ? adminFirstName : "Admin";
        
        // Deny the timesheet
        timesheetService.denyTimesheet(timesheetId, rejectionReason, deniedBy);
        
        return "redirect:/admin/view-timesheets/" + employeeId;
    }
}
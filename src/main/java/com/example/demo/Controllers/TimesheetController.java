package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.PayCode;
import com.example.demo.Entities.Timesheet;
import com.example.demo.Entities.TimesheetEntry;
import com.example.demo.Repositories.EmployeeRepository;
import com.example.demo.Repositories.PayCodeRepository;
import com.example.demo.Repositories.TimesheetRepository;
import com.example.demo.Repositories.TimesheetEntryRepository;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class TimesheetController {

    // --- DTO Classes for Form Binding ---

    /**
     * DTO for the form itself
     */
    public static class TimesheetFormDTO {
        private String payPeriodStart;
        private List<TimesheetEntryDTO> entries = new ArrayList<>();
        public String getPayPeriodStart() { return payPeriodStart; }
        public void setPayPeriodStart(String payPeriodStart) { this.payPeriodStart = payPeriodStart; }
        public List<TimesheetEntryDTO> getEntries() { return entries; }
        public void setEntries(List<TimesheetEntryDTO> entries) { this.entries = entries; }
    }

    /**
     * DTO for a single row in the form
     */
    public static class TimesheetEntryDTO {
        private Long payCodeId;
        private Map<String, Double> hours;
        public Long getPayCodeId() { return payCodeId; }
        public void setPayCodeId(Long payCodeId) { this.payCodeId = payCodeId; }
        public Map<String, Double> getHours() { return hours; }
        public void setHours(Map<String, Double> hours) { this.hours = hours; }
    }

    /**
     * Safe DTO for sending PayCode data to the template.
     * This avoids the "LazyInitializationException" and "Incomplete Chunk" errors.
     */
    public static class PayCodeDTO {
        private Long id;
        private String name;
        private String code;

        public PayCodeDTO(Long id, String name, String code) {
            this.id = id;
            this.name = name;
            this.code = code;
        }
        // Getters are needed for the JSON serializer
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getCode() { return code; }
    }


    // --- Injected Repositories ---
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PayCodeRepository payCodeRepository;
    @Autowired private TimesheetRepository timesheetRepository;
    @Autowired private TimesheetEntryRepository timesheetEntryRepository;


    /**
     * GET /timesheet
     * Displays the new timesheet grid page.
     * Fetches all pay codes and converts them to DTOs.
     */
    @GetMapping("/timesheet")
    public String timesheetForm(Model model, HttpSession session) {
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "User");

        model.addAttribute("timesheetForm", new TimesheetFormDTO());

        // --- THIS IS THE FIX ---
        // 1. Fetch all PayCode entities
        Iterable<PayCode> payCodeEntities = payCodeRepository.findAll();
        
        // 2. Convert them to "safe" DTOs
        List<PayCodeDTO> payCodeDTOs = new ArrayList<>();
        for (PayCode pc : payCodeEntities) {
            payCodeDTOs.add(new PayCodeDTO(pc.getId(), pc.getName(), pc.getCode()));
        }

        // 3. Add the DTO list to the model.
        // The template (timesheet.html) is expecting a model attribute named "payCodes".
        model.addAttribute("payCodes", payCodeDTOs);
        // --- END OF FIX ---
        
        return "timesheet"; 
    }

    /**
     * POST /timesheet
     * Handles the submission of the entire timesheet grid.
     */
    @PostMapping("/timesheet")
    public String timesheetSubmit(@ModelAttribute TimesheetFormDTO timesheetForm, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login"; 
        }

        Optional<Employee> employeeOpt = employeeRepository.findById(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/login"; 
        }
        Employee employee = employeeOpt.get();

        LocalDate payPeriodStartDate;
        try {
            payPeriodStartDate = LocalDate.parse(timesheetForm.getPayPeriodStart());
        } catch (DateTimeParseException e) {
            return "redirect:/timesheet?error=invalidDate";
        }

        Timesheet newTimesheet = new Timesheet();
        newTimesheet.setEmployee(employee);
        newTimesheet.setOrganization(employee.getOrganization());
        newTimesheet.setApprovalStatus("pending");  // FIXED: Changed from "Pending" to "pending"
        // You can calculate and set the week number here if needed
        // newTimesheet.setWeek(yourWeekCalculationLogic);
        
        Timesheet savedTimesheet = timesheetRepository.save(newTimesheet);

        for (TimesheetEntryDTO entryDTO : timesheetForm.getEntries()) {
            
            if (entryDTO.getPayCodeId() == null || entryDTO.getHours() == null) {
                continue; // Skip empty rows
            }

            PayCode payCode = payCodeRepository.findById(entryDTO.getPayCodeId()).orElse(null);
            if (payCode == null) {
                continue; // Skip if paycode ID is invalid
            }

            for (Map.Entry<String, Double> dayEntry : entryDTO.getHours().entrySet()) {
                String dayName = dayEntry.getKey(); 
                Double hours = dayEntry.getValue();
                
                if (hours != null && hours > 0) {
                    TimesheetEntry newEntry = new TimesheetEntry();
                    newEntry.setTimesheet(savedTimesheet);
                    newEntry.setPayCode(payCode);
                    newEntry.setHoursWorked(hours);
                    newEntry.setDate(calculateDateFromDay(payPeriodStartDate, dayName));
                    
                    timesheetEntryRepository.save(newEntry);
                }
            }
        }

        return "redirect:/employee/home"; // Redirect on success
    }

    /**
     * Helper function to determine the LocalDate for an entry
     */
    private LocalDate calculateDateFromDay(LocalDate startDate, String dayName) {
        int daysToAdd = 0;
        switch (dayName.toLowerCase()) {
            case "sun": daysToAdd = 0; break;
            case "mon": daysToAdd = 1; break;
            case "tue": daysToAdd = 2; break;
            case "wed": daysToAdd = 3; break;
            case "thu": daysToAdd = 4; break;
            case "fri": daysToAdd = 5; break;
            case "sat": daysToAdd = 6; break;
        }
        return startDate.plusDays(daysToAdd);
    }
}
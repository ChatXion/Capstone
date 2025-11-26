package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.Paycode;
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

        public String getPayPeriodStart() {
            return payPeriodStart;
        }

        public void setPayPeriodStart(String payPeriodStart) {
            this.payPeriodStart = payPeriodStart;
        }

        public List<TimesheetEntryDTO> getEntries() {
            return entries;
        }

        public void setEntries(List<TimesheetEntryDTO> entries) {
            this.entries = entries;
        }
    }

    /**
     * DTO for a single row in the form
     */
    public static class TimesheetEntryDTO {
        private Long paycodeId;
        private Map<String, Double> hours;

        public Long getPaycodeId() {
            return paycodeId;
        }

        public void setPaycodeId(Long paycodeId) {
            this.paycodeId = paycodeId;
        }

        public Map<String, Double> getHours() {
            return hours;
        }

        public void setHours(Map<String, Double> hours) {
            this.hours = hours;
        }
    }

    /**
     * Safe DTO for sending Paycode data to the template.
     * This avoids the "LazyInitializationException" and "Incomplete Chunk" errors.
     */
    public static class PaycodeDTO {
        private Long id;
        private String name;
        private String code;

        public PaycodeDTO(Long id, String name, String code) {
            this.id = id;
            this.name = name;
            this.code = code;
        }

        // Getters are needed for the JSON serializer
        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }
    }

    // --- Injected Repositories ---
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PayCodeRepository paycodeRepository;
    @Autowired
    private TimesheetRepository timesheetRepository;
    @Autowired
    private TimesheetEntryRepository timesheetEntryRepository;

    /**
     * GET /timesheet
     * Displays the new timesheet grid page.
     * Fetches all pay codes and converts them to DTOs.
     */
    @GetMapping("/timesheet")
    public String timesheetForm(Model model, HttpSession session) {
        // Get user info from session
        Long userId = (Long) session.getAttribute("userId");
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "User");

        // Redirect to login if not authenticated
        if (userId == null) {
            return "redirect:/login";
        }

        // Get the employee to access their organization and role
        Optional<Employee> employeeOpt = employeeRepository.findById(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/login";
        }
        Employee employee = employeeOpt.get();

        model.addAttribute("timesheetForm", new TimesheetFormDTO());

        // Filter pay codes by employee's organization
        Long organizationId = employee.getOrganization().getId();

        // Get role prefix for filtering
        String roleName = employee.getRole().getName();
        String rolePrefix = getRolePrefixFromRoleName(roleName);

        Iterable<Paycode> paycodeEntities = paycodeRepository.findByOrganizationId(organizationId);

        // Convert to DTOs and filter by role
        List<PaycodeDTO> paycodeDTOs = new ArrayList<>();
        for (Paycode pc : paycodeEntities) {
            // Only include paycodes that match the employee's role
            if (pc.getCode().startsWith(rolePrefix)) {
                paycodeDTOs.add(new PaycodeDTO(pc.getId(), pc.getName(), pc.getCode()));
            }
        }

        model.addAttribute("paycodes", paycodeDTOs);

        return "timesheet";
    }

    /**
     * Helper method to map role names to paycode prefixes
     */
    private String getRolePrefixFromRoleName(String roleName) {
        switch (roleName.toLowerCase()) {
            case "engineering":
                return "ENG";
            case "finance":
                return "FIN";
            case "hr":
                return "HR";
            case "marketing":
                return "MKT";
            case "sales":
                return "SAL";
            default:
                return ""; // No match - will show no paycodes
        }
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

        // Calculate week number
        int weekNumber = calculateWeekNumber(payPeriodStartDate);

        // Check if a pending timesheet already exists for this week and employee
        Timesheet existingTimesheet = timesheetRepository.findByEmployeeIdAndWeekAndApprovalStatus(
                employee.getId(), weekNumber, "pending");

        Timesheet targetTimesheet;
        if (existingTimesheet != null) {
            // Use existing pending timesheet
            targetTimesheet = existingTimesheet;
        } else {
            // Create new timesheet
            Timesheet newTimesheet = new Timesheet();
            newTimesheet.setEmployee(employee);
            newTimesheet.setApprovalStatus("pending");
            newTimesheet.setWeek(weekNumber);
            targetTimesheet = timesheetRepository.save(newTimesheet);
        }

        // Process entries
        for (TimesheetEntryDTO entryDTO : timesheetForm.getEntries()) {
            if (entryDTO.getPaycodeId() == null || entryDTO.getHours() == null) {
                continue;
            }

            Paycode paycode = paycodeRepository.findById(entryDTO.getPaycodeId()).orElse(null);
            if (paycode == null) {
                continue;
            }

            for (Map.Entry<String, Double> dayEntry : entryDTO.getHours().entrySet()) {
                String dayName = dayEntry.getKey();
                Double hours = dayEntry.getValue();

                if (hours != null && hours > 0) {
                    LocalDate entryDate = calculateDateFromDay(payPeriodStartDate, dayName);

                    // Check if an entry already exists for this date and paycode
                    TimesheetEntry existingEntry = timesheetEntryRepository
                            .findByTimesheetIdAndDateAndPaycodeId(
                                    targetTimesheet.getId(),
                                    entryDate,
                                    paycode.getId());

                    if (existingEntry != null) {
                        // Add hours to existing entry
                        existingEntry.setHoursWorked(existingEntry.getHoursWorked() + hours);
                        timesheetEntryRepository.save(existingEntry);
                    } else {
                        // Create new entry
                        TimesheetEntry newEntry = new TimesheetEntry();
                        newEntry.setTimesheet(targetTimesheet);
                        newEntry.setPaycode(paycode);
                        newEntry.setHoursWorked(hours);
                        newEntry.setDate(entryDate);
                        timesheetEntryRepository.save(newEntry);
                    }
                }
            }
        }

        return "redirect:/employee/home";
    }

    /**
     * Calculate week number from the pay period start date
     * Uses week of year as the week number
     */
    private int calculateWeekNumber(LocalDate startDate) {
        // Get the week of the year (1-52/53)
        return startDate.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear());
    }

    /**
     * Helper function to determine the LocalDate for an entry
     */
    private LocalDate calculateDateFromDay(LocalDate startDate, String dayName) {
        int daysToAdd = 0;
        switch (dayName.toLowerCase()) {
            case "sun":
                daysToAdd = 0;
                break;
            case "mon":
                daysToAdd = 1;
                break;
            case "tue":
                daysToAdd = 2;
                break;
            case "wed":
                daysToAdd = 3;
                break;
            case "thu":
                daysToAdd = 4;
                break;
            case "fri":
                daysToAdd = 5;
                break;
            case "sat":
                daysToAdd = 6;
                break;
        }
        return startDate.plusDays(daysToAdd);
    }
}
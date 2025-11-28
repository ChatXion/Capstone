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

import com.example.demo.Entities.Admin;
import com.example.demo.Entities.Employee;
import com.example.demo.Entities.Paycode;
import com.example.demo.Entities.Timesheet;
import com.example.demo.Entities.TimesheetEntry;
import com.example.demo.Repositories.AdminRepository;
import com.example.demo.Repositories.TimesheetRepository;
import com.example.demo.Services.EmployeeService;
import com.example.demo.Services.PayCodeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ViewTimesheetsController {

    private final EmployeeService employeeService;
    private final PayCodeService payCodeService;
    private final AdminRepository adminRepository;
    private final TimesheetRepository timesheetRepository;

    public ViewTimesheetsController(
            EmployeeService employeeService,
            PayCodeService payCodeService,
            AdminRepository adminRepository,
            TimesheetRepository timesheetRepository) {
        this.employeeService = employeeService;
        this.payCodeService = payCodeService;
        this.adminRepository = adminRepository;
        this.timesheetRepository = timesheetRepository;
    }

    @GetMapping("/employee/timesheets")
    public String viewEmployeeTimesheets(Model model, HttpSession session) {
        Long employeeId = (Long) session.getAttribute("userId");
        String firstName = (String) session.getAttribute("firstName");

        if (employeeId == null) {
            return "redirect:/login";
        }

        // Fetch all timesheets associated with the logged-in employee ID
        List<Timesheet> timesheetEntities = timesheetRepository.findByEmployeeId(employeeId);

        // NOTE: The employee-timesheets.html template expects a list named
        // 'timesheets'.
        model.addAttribute("timesheets", timesheetEntities);
        model.addAttribute("firstName", firstName != null ? firstName : "Employee");

        return "employee-timesheets";
    }

    @GetMapping("/admin/view-timesheets")
    public String viewAllTimesheets(Model model, HttpSession session) {
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

        // Get all timesheets for employees in the admin's organization
        Iterable<Timesheet> timesheets = timesheetRepository.findByEmployeeOrganizationId(organizationId);

        // Build DTOs for each timesheet
        List<TimesheetDTO> timesheetDTOs = new ArrayList<>();
        for (Timesheet ts : timesheets) {
            TimesheetDTO dto = new TimesheetDTO();
            dto.setTimesheetId(ts.getId());
            dto.setWeek(ts.getWeek());
            dto.setApprovalStatus(ts.getApprovalStatus());
            dto.setApprovedBy(ts.getApprovedBy());
            dto.setRejectionReason(ts.getRejectionReason());

            Employee emp = ts.getEmployee();
            dto.setEmployeeId(emp.getId());
            dto.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());

            // Calculate total hours
            double totalHours = 0.0;
            for (TimesheetEntry entry : ts.getEntries()) {
                totalHours += entry.getHoursWorked();
            }
            dto.setTotalHours(totalHours);

            timesheetDTOs.add(dto);
        }

        model.addAttribute("timesheets", timesheetDTOs);
        model.addAttribute("firstName", firstName != null ? firstName : "Admin");

        return "admin-view-timesheets";
    }

    @GetMapping("/employee/timesheets/{id}")
    public String viewTimesheetDetails(@PathVariable Long id, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        Optional<Employee> employeeOpt = employeeService.getEmployee(userId);
        if (employeeOpt.isEmpty()) {
            return "redirect:/login";
        }

        Employee employee = employeeOpt.get();
        model.addAttribute("firstName", employee.getFirstName());

        Optional<Timesheet> timesheetOpt = employeeService.getTimesheet(userId, id);

        if (timesheetOpt.isEmpty()) {
            return "redirect:/employee/timesheets";
        }

        Timesheet timesheet = timesheetOpt.get();

        TimesheetDisplay display = new TimesheetDisplay();
        display.setId(timesheet.getId());
        display.setWeek(timesheet.getWeek());
        display.setApprovalStatus(timesheet.getApprovalStatus());
        display.setApprovedBy(timesheet.getApprovedBy());
        display.setRejectionReason(timesheet.getRejectionReason());

        List<TimesheetEntryDisplay> entryDisplays = new ArrayList<>();
        double totalHours = 0;

        if (timesheet.getEntries() != null) {
            for (TimesheetEntry entry : timesheet.getEntries()) {
                TimesheetEntryDisplay entryDisplay = new TimesheetEntryDisplay();
                entryDisplay.setId(entry.getId());
                entryDisplay.setDate(entry.getDate());
                entryDisplay.setHoursWorked(entry.getHoursWorked());

                if (entry.getPaycode() != null) {
                    entryDisplay.setPayCodeId(entry.getPaycode().getId());
                    entryDisplay.setPayCodeName(entry.getPaycode().getName());
                    entryDisplay.setPayCodeCode(entry.getPaycode().getCode());
                    entryDisplay.setHourlyRate(BigDecimal.valueOf(entry.getPaycode().getHourlyRate()));
                } else {
                    entryDisplay.setPayCodeName("N/A");
                    entryDisplay.setPayCodeCode("N/A");
                    entryDisplay.setHourlyRate(BigDecimal.ZERO);
                }

                entryDisplays.add(entryDisplay);
                totalHours += entry.getHoursWorked();
            }
        }

        display.setTotalHours(totalHours);
        display.setEntries(entryDisplays);

        model.addAttribute("timesheet", display);

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

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        Optional<Timesheet> timesheetOpt = employeeService.getTimesheet(userId, id);
        if (timesheetOpt.isEmpty() || !"pending".equals(timesheetOpt.get().getApprovalStatus())) {
            return "redirect:/employee/timesheets/" + id;
        }

        employeeService.updateTimesheetEntry(entryId, date, hoursWorked, payCodeId);

        return "redirect:/employee/timesheets/" + id;
    }

    @PostMapping("/employee/timesheets/{id}/entry/{entryId}/delete")
    public String deleteTimesheetEntry(
            @PathVariable Long id,
            @PathVariable Long entryId,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        Optional<Timesheet> timesheetOpt = employeeService.getTimesheet(userId, id);
        if (timesheetOpt.isEmpty() || !"pending".equals(timesheetOpt.get().getApprovalStatus())) {
            return "redirect:/employee/timesheets/" + id;
        }

        employeeService.deleteTimesheetEntry(entryId);

        return "redirect:/employee/timesheets/" + id;
    }

    // DTO class for timesheet list view
    public static class TimesheetDTO {
        private Long timesheetId;
        private Long employeeId;
        private String employeeName;
        private Integer week;
        private double totalHours;
        private String approvalStatus;
        private String approvedBy;
        private String rejectionReason;

        public Long getTimesheetId() {
            return timesheetId;
        }

        public void setTimesheetId(Long timesheetId) {
            this.timesheetId = timesheetId;
        }

        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public Integer getWeek() {
            return week;
        }

        public void setWeek(Integer week) {
            this.week = week;
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

    // Display class for timesheet entries
    public static class TimesheetEntryDisplay {
        private Long id;
        private LocalDate date;
        private double hoursWorked;
        private Long payCodeId;
        private String payCodeName;
        private String payCodeCode;
        private BigDecimal hourlyRate;

        public TimesheetEntryDisplay() {
            this.payCodeName = "";
            this.payCodeCode = "";
            this.hourlyRate = BigDecimal.ZERO;
        }

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

        public BigDecimal getHourlyRate() {
            return hourlyRate != null ? hourlyRate : BigDecimal.ZERO;
        }

        public void setHourlyRate(BigDecimal hourlyRate) {
            this.hourlyRate = hourlyRate != null ? hourlyRate : BigDecimal.ZERO;
        }
    }
}
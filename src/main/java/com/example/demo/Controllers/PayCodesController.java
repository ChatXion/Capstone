package com.example.demo.Controllers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.Entities.Employee;
import com.example.demo.Entities.PayCode;
import com.example.demo.Services.EmployeeService;
import com.example.demo.Services.PayCodeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PayCodesController {

    private final EmployeeService employeeService;
    private final PayCodeService payCodeService;

    public PayCodesController(EmployeeService employeeService,
                             PayCodeService payCodeService) {
        this.employeeService = employeeService;
        this.payCodeService = payCodeService;
    }

    @GetMapping("/employee/paycodes")
    public String viewPayCodes(Model model, HttpSession session) {
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
        
        // Get employee's organization ID
        Long organizationId = null;
        if (employee.getOrganization() != null) {
            organizationId = employee.getOrganization().getId();
        }
        
        // Fetch pay codes for this organization using service
        List<PayCode> payCodes = new ArrayList<>();
        if (organizationId != null) {
            payCodes = payCodeService.getPayCodesByOrganization(organizationId);
        }
        
        // Convert to display objects
        List<PayCodeDisplay> displayPayCodes = new ArrayList<>();
        for (PayCode payCode : payCodes) {
            PayCodeDisplay display = new PayCodeDisplay();
            display.setCode(payCode.getCode());
            display.setName(payCode.getName());
            display.setDescription(payCode.getDescription());
            display.setHourlyRate(payCode.getHourlyRate());
            
            // Determine category based on code pattern
            String category = determineCategory(payCode.getCode(), payCode.getName());
            display.setCategory(category);
            
            displayPayCodes.add(display);
        }
        
        model.addAttribute("payCodes", displayPayCodes);
        model.addAttribute("organizationName", 
            employee.getOrganization() != null ? employee.getOrganization().getName() : "Your Organization");
        
        return "employee-paycodes";
    }
    
    private String determineCategory(String code, String name) {
        if (code.startsWith("HR") || name.contains("HR") || name.contains("Human Resources")) {
            return "Human Resources";
        } else if (code.startsWith("ENG") || name.contains("Engineering")) {
            return "Engineering";
        } else if (code.startsWith("FIN") || name.contains("Finance")) {
            return "Finance";
        } else if (code.startsWith("MKT") || name.contains("Marketing")) {
            return "Marketing";
        } else {
            return "General";
        }
    }
    
    // Display class for pay codes
    public static class PayCodeDisplay {
        private String code;
        private String name;
        private String description;
        private BigDecimal hourlyRate;
        private String category;
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public BigDecimal getHourlyRate() { return hourlyRate; }
        public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}
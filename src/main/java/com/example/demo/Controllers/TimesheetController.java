package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpSession;

@Controller
public class TimesheetController {

    @GetMapping("/timesheet")
    public String timesheetForm(Model model, HttpSession session) {
        // Create a simple form object for the timesheet entry
        TimesheetEntryForm form = new TimesheetEntryForm();
        model.addAttribute("timesheetEntry", form);
        
        // Get firstName from session for navigation
        String firstName = (String) session.getAttribute("firstName");
        model.addAttribute("firstName", firstName != null ? firstName : "User");
        
        return "timesheet";
    }

    @PostMapping("/timesheet")
    public String timesheetSubmit(@ModelAttribute TimesheetEntryForm timesheetEntry) {
        // Add logic to save the timesheet data
        System.out.println("Timesheet submitted - Date: " + timesheetEntry.getDate() + ", Hours: " + timesheetEntry.getHours());
        return "redirect:/employee/home";
    }
    
    // Simple form object to hold timesheet entry data
    public static class TimesheetEntryForm {
        private String date;
        private Double hours;
        
        public TimesheetEntryForm() {
        }
        
        public String getDate() {
            return date;
        }
        
        public void setDate(String date) {
            this.date = date;
        }
        
        public Double getHours() {
            return hours;
        }
        
        public void setHours(Double hours) {
            this.hours = hours;
        }
    }
}
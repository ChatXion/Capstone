package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TimesheetController {

    @GetMapping("/timesheet")
    public String timesheetForm(Model model) {
        model.addAttribute("timesheet", new Timesheet());
        return "timesheet";
    }

    @PostMapping("/timesheet")
    public String timesheetSubmit(Timesheet timesheet) {
        // Add logic to save the timesheet data
        return "redirect:/employee-home";
    }
}
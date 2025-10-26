package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.Timesheet;

@Controller
public class TimesheetController {

    @GetMapping("/timesheet")
    public String timesheetForm(Model model) {
        model.addAttribute("timesheet", new Timesheet());
        model.addAttribute("firstName", "John");
        return "timesheet";
    }

    @PostMapping("/timesheet")
    public String timesheetSubmit(Timesheet timesheet) {
        // Add logic to save the timesheet data
        return "redirect:/employee/home";
    }
}
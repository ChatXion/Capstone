package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.Entities.RegistrationRequest;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        //System.out.println("Home controller hit");
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @ModelAttribute("registration")
    public RegistrationRequest registrationRequest() {
        return new RegistrationRequest();
    }

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }
}


package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        System.out.println("Home controller hit");
        return "home";
    }

    @GetMapping("/login")
    public String test() {
        System.out.println("Test controller hit");
        return "home";
    }
}

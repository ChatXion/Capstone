package com.example.demo.Controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Services.LoginService;

import jakarta.servlet.http.HttpSession;

/*
  LoginController
  - POST /login delegates authentication to LoginService.
*/
@Controller
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              Model model,
                              HttpSession session) {

        Optional<LoginService.AuthResult> result = loginService.authenticate(email, password);
        if (result.isPresent()) {
            LoginService.AuthResult ar = result.get();
            session.setAttribute("userId", ar.id());
            session.setAttribute("firstName", ar.firstName());
            session.setAttribute("role", ar.role());
            return ar.role().equals("ADMIN") ? "redirect:/admin/home" : "redirect:/employee/home";
        }

        model.addAttribute("error", "Invalid email or password.");
        return "login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
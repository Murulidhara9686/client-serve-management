package com.csm.controller;

import com.csm.model.Role;
import com.csm.model.User;
import com.csm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String loggedout,
                            @RequestParam(required = false) String registered,
                            Model model) {
        if (error      != null) model.addAttribute("error",      "Invalid email or password.");
        if (loggedout  != null) model.addAttribute("loggedout",  "You have been signed out.");
        if (registered != null) model.addAttribute("registered", "Account created! You can now sign in.");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user",  new User());
        model.addAttribute("roles", new Role[]{Role.CUSTOMER, Role.EMPLOYEE});
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Email already registered.");
            model.addAttribute("roles", new Role[]{Role.CUSTOMER, Role.EMPLOYEE});
            return "auth/register";
        }
        if (user.getPassword() == null || user.getPassword().length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters.");
            model.addAttribute("roles", new Role[]{Role.CUSTOMER, Role.EMPLOYEE});
            return "auth/register";
        }
        userService.register(user);
        return "redirect:/?registered=true";
    }

    @GetMapping("/access-denied")
    public String accessDenied() { return "auth/access-denied"; }
}

package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.service.UserService;
import com.example.demo.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Controller
public class AuthController {
    public static final String SESSION_USER_ID = "reviewmeUserId";
    public static final String SESSION_USERNAME = "reviewmeUsername";
    public static final String SESSION_ROLE = "reviewmeRole";
    private final UserService userService;
    private final LoginAttemptService loginAttemptService;
    public AuthController(UserService userService, LoginAttemptService loginAttemptService) {
        this.userService = userService;
        this.loginAttemptService = loginAttemptService;
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(required = false) String next, Model model) {
        model.addAttribute("next", next == null ? "" : next); return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        @RequestParam(required = false) String next, HttpSession session, Model model,
                        HttpServletRequest request) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase(java.util.Locale.ROOT);
        String attemptKey = request.getRemoteAddr() + "|" + normalizedUsername;
        if (loginAttemptService.isBlocked(attemptKey)) {
            model.addAttribute("error", "Too many failed login attempts. Please wait 15 minutes before trying again.");
            model.addAttribute("next", next == null ? "" : next);
            return "login";
        }
        var user = userService.authenticate(username, password);
        if (user.isPresent()) {
            loginAttemptService.recordSuccess(attemptKey);
            request.changeSessionId();
            session.setAttribute(SESSION_USER_ID, user.get().getId());
            session.setAttribute(SESSION_USERNAME, user.get().getUsername());
            session.setAttribute(SESSION_ROLE, user.get().getRole());
            return next != null && next.startsWith("/") && !next.startsWith("//") ? "redirect:" + next : "redirect:/";
        }
        loginAttemptService.recordFailure(attemptKey);
        model.addAttribute("error", "Invalid username or password.");
        model.addAttribute("next", next == null ? "" : next); return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password, Model model) {
        AuthRequest request = new AuthRequest(); request.setUsername(username); request.setPassword(password);
        Map<String, String> result = userService.register(request);
        if ("User registered successfully".equals(result.get("message"))) {
            model.addAttribute("success", "Account created. Please log in."); return "login";
        }
        model.addAttribute("error", result.get("message")); return "register";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) { session.invalidate(); return "redirect:/"; }
}

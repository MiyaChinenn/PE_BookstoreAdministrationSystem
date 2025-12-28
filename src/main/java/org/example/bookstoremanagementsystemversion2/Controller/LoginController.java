package org.example.bookstoremanagementsystemversion2.Controller;

import java.util.HashMap;
import java.util.Map;

import org.example.bookstoremanagementsystemversion2.DAO.UserDAO;
import org.example.bookstoremanagementsystemversion2.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        
        // Add user info to model for navigation
        model.addAttribute("isLoggedIn", username != null);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("isAdmin", "admin".equals(role));
        
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(Model model, @RequestParam(required = false) String redirect) {
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData, HttpSession session) {
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Username and password are required"
                ));
            }
            
            // USE THE EXISTING LOGIN METHOD IN UserDAO
            User user = userDAO.login(username, password);
            
            if (user != null) {
                // Set session attributes
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole());
                session.setAttribute("userId", user.getUsername()); // Use username as userId since User model doesn't have userId field
                
                // Determine redirect URL
                String redirectUrl = "/";
                if ("admin".equals(user.getRole())) {
                    redirectUrl = "/admin";
                }
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("redirectUrl", redirectUrl);
                response.put("role", user.getRole());
                response.put("username", user.getUsername());
                response.put("needCartSync", true);
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid username or password"
                ));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Server error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/logout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        try {
            session.invalidate();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error during logout"
            ));
        }
    }

    @GetMapping("/logout")
    public String logoutPage(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/api/session-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSessionStatus(HttpSession session) {
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        
        Map<String, Object> status = new HashMap<>();
        status.put("isLoggedIn", username != null);
        status.put("username", username);
        status.put("role", role);
        status.put("isAdmin", "admin".equals(role));
        
        return ResponseEntity.ok(status);
    }
}
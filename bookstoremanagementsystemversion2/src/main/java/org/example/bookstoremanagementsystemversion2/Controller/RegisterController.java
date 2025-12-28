package org.example.bookstoremanagementsystemversion2.Controller;

import java.util.HashMap;
import java.util.Map;

import org.example.bookstoremanagementsystemversion2.DAO.UserDAO;
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
public class RegisterController {

    @Autowired
    private UserDAO userDAO;

    @GetMapping("/register")
    public String registerPage(Model model) {
        return "register";
    }

    // JSON API endpoint for register
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> registerData, HttpSession session) {
        try {
            String username = registerData.get("username");
            String password = registerData.get("password");
            String firstName = registerData.get("firstName");
            String lastName = registerData.get("lastName");
            String phoneNumber = registerData.get("phoneNumber");
            
            // Validation
            if (username == null || password == null || firstName == null || lastName == null ||
                username.trim().isEmpty() || password.trim().isEmpty() || firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "All fields are required"
                ));
            }
            
            if (password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Password must be at least 6 characters long"
                ));
            }
            
            // Check if username already exists
            userDAO.getAllUsers();
            boolean userExists = userDAO.getUsersList().stream()
                .anyMatch(u -> username.equals(u.getUsername()));
            
            if (userExists) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Username already exists"
                ));
            }
            
            // Create new user using existing method
            boolean success = userDAO.createUser(username, firstName, lastName, phoneNumber, password, "customer");
            
            if (success) {
                // Auto-login after successful registration
                session.setAttribute("username", username);
                session.setAttribute("role", "customer");
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Registration successful! You are now logged in.");
                response.put("redirectUrl", "/");
                response.put("role", "customer");
                response.put("username", username);
                response.put("needCartSync", true); // Signal frontend to sync guest cart
                response.put("isNewUser", true); // Signal that this is a new registration
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Registration failed. Please try again."
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

    // Keep existing form-based registration for backward compatibility
    @PostMapping("/processRegister")
    public String processRegister(@RequestParam String username,
                                 @RequestParam String firstName,
                                 @RequestParam String lastName,
                                 @RequestParam String phoneNumber,
                                 @RequestParam String password,
                                 Model model) {
        try {
            // Input validation
            if (username == null || username.trim().isEmpty()) {
                model.addAttribute("error", "Username is required!");
                return "register";
            }
            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "Password is required!");
                return "register";
            }
            if (firstName == null || firstName.trim().isEmpty()) {
                model.addAttribute("error", "First name is required!");
                return "register";
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                model.addAttribute("error", "Last name is required!");
                return "register";
            }

            // Use the existing createUser method from UserDAO
            boolean success = userDAO.createUser(
                username.trim(), 
                firstName.trim(), 
                lastName.trim(), 
                phoneNumber != null ? phoneNumber.trim() : "", 
                password, 
                "customer"
            );
            
            if (success) {
                model.addAttribute("success", "Registration successful! Please login.");
                return "login";
            } else {
                model.addAttribute("error", "Registration failed!");
                return "register";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Registration error: " + e.getMessage());
            return "register";
        }
    }
}
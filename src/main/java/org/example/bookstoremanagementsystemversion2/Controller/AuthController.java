package org.example.bookstoremanagementsystemversion2.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.example.bookstoremanagementsystemversion2.Model.User;
import org.example.bookstoremanagementsystemversion2.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            
            // Validate input
            if (username == null || username.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Username and password are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Find user by username
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Validate password
                if (user.getPassword().equals(password)) {
                    response.put("success", true);
                    response.put("message", "Login successful");
                    response.put("username", username);
                    response.put("firstName", user.getFirstName());
                    response.put("lastName", user.getLastName());
                    response.put("role", user.getRole());
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("message", "Invalid password");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            } else {
                response.put("success", false);
                response.put("message", "User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> registerRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = registerRequest.get("username");
            String firstName = registerRequest.get("firstName");
            String lastName = registerRequest.get("lastName");
            String phoneNumber = registerRequest.get("phoneNumber");
            String password = registerRequest.get("password");
            
            // Validate input
            if (username == null || username.trim().isEmpty() ||
                firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "All fields are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if user already exists
            if (userRepository.findByUsername(username).isPresent()) {
                response.put("success", false);
                response.put("message", "Username already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // Create new user with default role "customer"
            User newUser = new User(username, firstName, lastName, phoneNumber, password, "customer");
            User savedUser = userRepository.save(newUser);
            
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("username", savedUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
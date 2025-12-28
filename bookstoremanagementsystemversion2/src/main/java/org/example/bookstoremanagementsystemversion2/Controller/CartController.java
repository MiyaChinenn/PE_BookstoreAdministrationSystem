package org.example.bookstoremanagementsystemversion2.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        
        // Add user info to model for navigation
        model.addAttribute("isLoggedIn", username != null);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("isAdmin", "admin".equals(role));
        
        // Check if user just logged in (for guest cart transfer)
        String justLoggedIn = (String) session.getAttribute("justLoggedIn");
        if ("true".equals(justLoggedIn)) {
            model.addAttribute("triggerCartTransfer", true);
            session.removeAttribute("justLoggedIn"); // Remove flag after use
        }
        
        return "cart";
    }
}
package org.example.bookstoremanagementsystemversion2.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.example.bookstoremanagementsystemversion2.DAO.BookDAO;
import org.example.bookstoremanagementsystemversion2.DAO.OrderDAO;
import org.example.bookstoremanagementsystemversion2.DAO.OrderDetailDAO;
import org.example.bookstoremanagementsystemversion2.DAO.ShoppingCartDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);
    
    @Autowired
    private BookDAO bookDAO;
    
    @Autowired
    private ShoppingCartDAO shoppingCartDAO;
    
    @Autowired
    private OrderDAO orderDAO;
    
    @Autowired
    private OrderDetailDAO orderDetailDAO;

    @GetMapping("/checkout")
    public String checkoutPage(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        
        logger.info("Checkout page accessed - username: {}, role: {}", username, role);
        
        if (username == null || username.trim().isEmpty()) {
            logger.info("User not logged in, redirecting to login");
            return "redirect:/login?redirect=/checkout";
        }
        
        // Add all session info to model
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("isAdmin", "admin".equals(role));
        
        logger.info("Checkout page loaded successfully for user: {}", username);
        
        return "checkout";
    }

    @PostMapping("/processCheckout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processCheckout(@RequestBody Map<String, Object> checkoutData, HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Please login first"));
        }

        try {
            // Extract checkout data
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> items = (java.util.List<Map<String, Object>>) checkoutData.get("items");
            
            if (items == null || items.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Cart is empty"));
            }
            
            // Calculate totals
            double subtotal = items.stream()
                .mapToDouble(item -> {
                    double price = ((Number) item.get("price")).doubleValue();
                    int quantity = ((Number) item.get("quantity")).intValue();
                    return price * quantity;
                })
                .sum();
            
            double tax = subtotal * 0.08;
            double total = subtotal + tax;
            int totalItems = items.stream().mapToInt(item -> ((Number) item.get("quantity")).intValue()).sum();
            
            // Generate order ID (will be replaced by database auto-generated ID)
            String orderDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            // Create order in database first
            int orderId;
            try {
                orderId = orderDAO.createOrderWithTotals(username, total, totalItems);
                if (orderId <= 0) {
                    logger.error("Failed to create order in database for user: {}", username);
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to create order"));
                }
                logger.info("Created order in database: orderId={} for user: {}", orderId, username);
            } catch (Exception e) {
                logger.error("Error creating order for user {}: {}", username, e.getMessage());
                return ResponseEntity.status(500).body(Map.of("success", false, "message", "Database error creating order"));
            }
            
            // Process the order: create order details, reduce book stock and clear cart
            try {
                // 1. Create order details for each item
                for (Map<String, Object> item : items) {
                    int bookId = ((Number) item.get("bookId")).intValue();
                    int quantity = ((Number) item.get("quantity")).intValue();
                    double price = ((Number) item.get("price")).doubleValue();
                    
                    try {
                        orderDetailDAO.createOrderDetails(orderId, bookId, quantity, price);
                        logger.info("Created order detail: orderId={}, bookId={}, quantity={}, price={}", orderId, bookId, quantity, price);
                    } catch (Exception e) {
                        logger.error("Failed to create order detail for orderId={}, bookId={}: {}", orderId, bookId, e.getMessage());
                        // Continue with other items even if one fails
                    }
                }
                
                // 2. Reduce book stock for each item
                for (Map<String, Object> item : items) {
                    int bookId = ((Number) item.get("bookId")).intValue();
                    int quantity = ((Number) item.get("quantity")).intValue();
                    
                    boolean stockReduced = bookDAO.reduceBookStock(bookId, quantity);
                    if (!stockReduced) {
                        logger.warn("Failed to reduce stock for book ID: {}, quantity: {}", bookId, quantity);
                        // Note: Order is already created, so we log but continue
                    } else {
                        logger.info("Reduced stock for book ID {}: quantity reduced = {}", bookId, quantity);
                    }
                }
                
                // 3. Clear user's cart from database
                boolean cartCleared = shoppingCartDAO.clearCart(username);
                if (cartCleared) {
                    logger.info("Cart cleared for user: {}", username);
                } else {
                    logger.warn("Failed to clear cart for user: {}", username);
                }
                
            } catch (Exception e) {
                logger.error("Error processing order items for user {}: {}", username, e.getMessage());
                // Order is already created in database, so we continue
            }
            
            // Format order ID for display
            String displayOrderId = "#ORD" + String.format("%05d", orderId);
            
            // Store order details in session for the success page
            session.setAttribute("orderId", displayOrderId);
            session.setAttribute("orderDate", orderDate);
            session.setAttribute("totalItems", totalItems);
            session.setAttribute("totalAmount", String.format("%.2f", total));
            
            logger.info("Order processed successfully for user {}: orderId={}, total=${}", username, orderId, total);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order placed successfully",
                "redirectUrl", "/orderSuccess",
                "orderId", displayOrderId
            ));
            
        } catch (Exception e) {
            logger.error("Error processing checkout for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Error processing order"));
        }
    }

    @GetMapping("/orderSuccess")
    public String orderSuccessPage(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        // Get order details from session
        model.addAttribute("orderId", session.getAttribute("orderId"));
        model.addAttribute("orderDate", session.getAttribute("orderDate"));
        model.addAttribute("totalItems", session.getAttribute("totalItems"));
        model.addAttribute("totalAmount", session.getAttribute("totalAmount"));
        model.addAttribute("isLoggedIn", username != null);
        model.addAttribute("username", username);
        
        return "orderSuccess";
    }
}
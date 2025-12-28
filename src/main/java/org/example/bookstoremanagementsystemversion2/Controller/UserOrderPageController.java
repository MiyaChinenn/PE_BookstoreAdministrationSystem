package org.example.bookstoremanagementsystemversion2.Controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.bookstoremanagementsystemversion2.DAO.OrderDAO;
import org.example.bookstoremanagementsystemversion2.DAO.OrderDetailDAO;
import org.example.bookstoremanagementsystemversion2.Model.Order;
import org.example.bookstoremanagementsystemversion2.Model.OrderDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserOrderPageController {

    @Autowired
    private OrderDAO orderDAO;
    
    @Autowired
    private OrderDetailDAO orderDetailDAO;

    @GetMapping("/my-orders")
    public String myOrdersPage(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        
        if (username == null) {
            return "redirect:/login?redirect=/my-orders";
        }
        
        // Add user info to model for navigation
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("isAdmin", "admin".equals(role));
        
        // Initialize with empty list to prevent null pointer exceptions
        List<Order> orders = new ArrayList<>();
        
        // Fetch user's orders
        try {
            if (orderDAO != null) {
                orderDAO.fetchOrders(username);
                List<Order> fetchedOrders = orderDAO.getOrdersList();
                if (fetchedOrders != null) {
                    orders = fetchedOrders;
                }
            } else {
                System.err.println("OrderDAO is null!");
                model.addAttribute("errorMessage", "System error: OrderDAO not available.");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching orders for user " + username + ": " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load orders. Please try again.");
        } catch (Exception e) {
            System.err.println("Unexpected error fetching orders for user " + username + ": " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Unexpected error occurred. Please try again.");
        }
        
        model.addAttribute("orders", orders);
        return "my-orders";
    }

    @GetMapping("/orders")
    public String ordersPage(Model model, HttpSession session) {
        // Redirect to my-orders for consistency
        return "redirect:/my-orders";
    }

    @GetMapping("/order-details/{orderId}")
    public String orderDetailsPage(@PathVariable Integer orderId, @RequestParam(required = false) String from, Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        
        if (username == null) {
            return "redirect:/login?redirect=/order-details/" + orderId;
        }
        
        // Add user info to model for navigation
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("isAdmin", "admin".equals(role));
        model.addAttribute("orderId", orderId);
        
        // Determine back link based on source
        String backLink = "/my-orders";
        String backText = "← Back to My Orders";
        
        if ("admin".equals(from)) {
            backLink = "/admin";
            backText = "← Back to Admin Dashboard";
        }
        
        model.addAttribute("backLink", backLink);
        model.addAttribute("backText", backText);
        
        // Fetch order details
        try {
            if (orderDAO == null) {
                System.err.println("OrderDAO is null!");
                model.addAttribute("errorMessage", "System error: OrderDAO not available.");
                model.addAttribute("orderDetails", new ArrayList<>()); // Ensure non-null
                return "order-details";
            }
            
            if (orderDetailDAO == null) {
                System.err.println("OrderDetailDAO is null!");
                model.addAttribute("errorMessage", "System error: OrderDetailDAO not available.");
                model.addAttribute("orderDetails", new ArrayList<>()); // Ensure non-null
                return "order-details";
            }
            
            Order order = orderDAO.getOrderById(orderId);
            if (order != null) {
                // Check if user is admin or if this is their order
                if (!"admin".equals(role) && !username.equals(order.getUsername())) {
                    return "redirect:/my-orders?error=unauthorized";
                }
                
                model.addAttribute("order", order);
                
                // Fetch order details with book info
                List<OrderDetails> orderDetails = orderDetailDAO.getOrderDetailsWithBookInfo(orderId);
                model.addAttribute("orderDetails", orderDetails != null ? orderDetails : new ArrayList<>());
            } else {
                System.err.println("Order not found: " + orderId);
                model.addAttribute("errorMessage", "Order not found.");
                model.addAttribute("orderDetails", new ArrayList<>()); // Ensure non-null
            }
        } catch (SQLException e) {
            System.err.println("Error fetching order details for order " + orderId + ": " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load order details.");
            model.addAttribute("orderDetails", new ArrayList<>()); // Ensure non-null
        } catch (Exception e) {
            System.err.println("Unexpected error fetching order details for order " + orderId + ": " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Unexpected error occurred.");
            model.addAttribute("orderDetails", new ArrayList<>()); // Ensure non-null
        }
        
        return "order-details";
    }
}

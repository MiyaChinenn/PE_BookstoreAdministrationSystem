package org.example.bookstoremanagementsystemversion2.Controller;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.example.bookstoremanagementsystemversion2.DAO.OrderDAO;
import org.example.bookstoremanagementsystemversion2.DAO.OrderDetailDAO;
import org.example.bookstoremanagementsystemversion2.DAO.ShoppingCartDAO;
import org.example.bookstoremanagementsystemversion2.Model.Order;
import org.example.bookstoremanagementsystemversion2.Model.OrderDetails;
import org.example.bookstoremanagementsystemversion2.Model.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// REST Controller for managing orders
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderDAO orderDAO;
    
    @Autowired
    private OrderDetailDAO orderDetailDAO;
    
    @Autowired
    private ShoppingCartDAO shoppingCartDAO;

    // GET endpoint to fetch orders by username
    @GetMapping({"", "/"})
    public List<Order> getOrdersByUsername(@RequestParam String username) {
        try{
            orderDAO.fetchOrders(username);
            return orderDAO.getOrdersList();
        } catch(SQLException ex) {
            System.err.println("GetOrders _ SQL: " + ex.getMessage());
            ex.printStackTrace();
        } catch(Exception ex){
            System.err.println("getOrders _ Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
        return null;
    }

    // GET endpoint to fetch all orders (for admin)
    @GetMapping("/all")
    public List<Order> getAllOrders() {
        try{
            orderDAO.fetchAllOrders();
            return orderDAO.getOrdersList();
        } catch(SQLException ex) {
            System.err.println("GetAllOrders _ SQL: " + ex.getMessage());
            ex.printStackTrace();
        } catch(Exception ex){
            System.err.println("getAllOrders _ Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
        return null;
    }

    // GET endpoint to fetch order by ID
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable int id) {
        try{
            return orderDAO.getOrderById(id);
        } catch(SQLException ex) {
            System.err.println("GetOrderById _ SQL: " + ex.getMessage());
            ex.printStackTrace();
        } catch(Exception ex){
            System.err.println("getOrderById _ Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
        return null;
    }

    // GET endpoint to fetch order details with book info
    @GetMapping("/{id}/details")
    public List<OrderDetails> getOrderDetailsWithBookInfo(@PathVariable int id) {
        try{
            return orderDetailDAO.getOrderDetailsWithBookInfo(id);
        } catch(SQLException ex) {
            System.err.println("GetOrderDetails _ SQL: " + ex.getMessage());
            ex.printStackTrace();
        } catch(Exception ex){
            System.err.println("getOrderDetails _ Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
        return null;
    }

    @PostMapping({"", "/"})
    public boolean createOrder(@RequestBody Order info) {
        try{
            return orderDAO.createOrder(info.getUsername(), info.getOrderDate());
        } catch(SQLException ex) {
            System.err.println("CreateOrder _ SQL: " + ex.getMessage());
            ex.printStackTrace();
        } catch(Exception ex){
            System.err.println("createOrder _ Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
        return false;
    }

    // NEW: Checkout endpoint - Convert cart to order
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            if (username == null || username.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Username is required"));
            }

            // Get cart items
            shoppingCartDAO.fetchCartItems(username);
            List<ShoppingCart> cartItems = shoppingCartDAO.getItems();
            
            if (cartItems == null || cartItems.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Cart is empty"));
            }

            // Calculate totals
            double totalAmount = cartItems.stream().mapToDouble(item -> item.getQuantity() * item.getPrice()).sum();
            int totalItems = cartItems.stream().mapToInt(ShoppingCart::getQuantity).sum();

            // Create order
            int orderId = orderDAO.createOrderWithTotals(username, totalAmount, totalItems);
            
            if (orderId > 0) {
                // Create order details
                for (ShoppingCart cartItem : cartItems) {
                    orderDetailDAO.createOrderDetails(orderId, cartItem.getBookId(), cartItem.getQuantity(), cartItem.getPrice());
                }
                
                // Clear cart
                shoppingCartDAO.clearCart(username);
                
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "Order placed successfully", 
                    "orderId", orderId,
                    "totalAmount", totalAmount,
                    "totalItems", totalItems
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to create order"));
            }
            
        } catch(Exception ex) {
            System.err.println("Checkout _ Exception: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Checkout failed: " + ex.getMessage()));
        }
    }

    @PutMapping({"", "/"})
    public boolean updateOrder(@RequestBody Order info) {
        try{
            boolean result = orderDAO.updateOrder(info);
            return result;
        } catch(SQLException ex) {
            System.err.println("UpdateOrder _ SQL: " + ex.getMessage());
            ex.printStackTrace();
        } catch(Exception ex){
            System.err.println("updateOrder _ Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
        return false;
    }

    // NEW: Update order status (for admin)
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable int id, @RequestBody Map<String, String> request) {
        try {
            String newStatus = request.get("status");
            if (newStatus == null || newStatus.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Status is required"));
            }

            Order order = orderDAO.getOrderById(id);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Order not found"));
            }

            order.setStatus(newStatus);
            boolean result = orderDAO.updateOrder(order);
            
            if (result) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Order status updated successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to update order status"));
            }
            
        } catch(Exception ex) {
            System.err.println("UpdateOrderStatus _ Exception: " + ex.getMessage());
            ex.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to update status: " + ex.getMessage()));
        }
    }
}

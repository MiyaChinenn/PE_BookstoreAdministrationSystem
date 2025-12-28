package org.example.bookstoremanagementsystemversion2.Controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.bookstoremanagementsystemversion2.DAO.BookDAO;
import org.example.bookstoremanagementsystemversion2.DAO.UserDAO;
import org.example.bookstoremanagementsystemversion2.Model.Book;
import org.example.bookstoremanagementsystemversion2.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private UserDAO userDAO;
    
    @Autowired
    private BookDAO bookDAO;

    @GetMapping("/admin")
    public String adminDashboard(Model model, HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            String role = (String) session.getAttribute("role");
            
            // Temporary bypass for testing
            if (username == null) {
                username = "admin";
                role = "admin";
                session.setAttribute("username", username);
                session.setAttribute("role", role);
            }
            
            // Get all users from database
            userDAO.getAllUsers();
            List<User> allUsers = userDAO.getUsersList();
            
            if (allUsers == null) {
                allUsers = new ArrayList<>();
            }
            
            // Separate admin and customer users
            List<User> adminUsers = allUsers.stream()
                .filter(user -> "admin".equals(user.getRole()))
                .collect(Collectors.toList());
                
            List<User> customerUsers = allUsers.stream()
                .filter(user -> "customer".equals(user.getRole()))
                .collect(Collectors.toList());
            
            // Get all books from database  
            bookDAO.getAllBooks();
            List<Book> books = bookDAO.getBooksList();
            
            if (books == null) {
                books = new ArrayList<>();
            }
            
            // Add data to model
            model.addAttribute("adminUsers", adminUsers);
            model.addAttribute("customerUsers", customerUsers);
            model.addAttribute("books", books);
            model.addAttribute("adminName", username);
            
            return "admin";
            
        } catch (Exception e) {
            System.err.println("=== ADMIN CONTROLLER ERROR ===");
            e.printStackTrace();
            model.addAttribute("error", "Database error: " + e.getMessage());
            model.addAttribute("adminUsers", new ArrayList<>());
            model.addAttribute("customerUsers", new ArrayList<>());
            model.addAttribute("books", new ArrayList<>());
            model.addAttribute("adminName", "Admin");
            return "admin";
        }
    }

    @PostMapping("/admin/addBook")
    public String addBook(@RequestParam String name,
                         @RequestParam String type,
                         @RequestParam double price,
                         @RequestParam String publisher,
                         @RequestParam int quantity,
                         RedirectAttributes redirectAttributes) {
        try {
            Book newBook = new Book();
            newBook.setName(name);
            newBook.setType(type);
            newBook.setPrice(price);
            newBook.setPublisher(publisher);
            newBook.setQuantity(quantity);
            
            boolean success = bookDAO.createBook(newBook);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Book added successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to add book.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error adding book: " + e.getMessage());
        }
        
        return "redirect:/admin";
    }

    @PostMapping("/admin/updateStock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStock(@RequestBody Map<String, Object> requestData, HttpSession session) {
        try {
            // Check if user is admin
            String role = (String) session.getAttribute("role");
            if (!"admin".equals(role)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied"
                ));
            }
            
            // Extract data
            Object bookIdObj = requestData.get("bookId");
            Object quantityObj = requestData.get("quantity");
            
            if (bookIdObj == null || quantityObj == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Missing required fields"
                ));
            }
            
            int bookId = Integer.parseInt(bookIdObj.toString());
            int quantity = Integer.parseInt(quantityObj.toString());
            
            // Update stock in database
            boolean updated = bookDAO.updateBookStock(bookId, quantity);
            
            if (updated) {
                // BUG FIX: Refresh the books list after update
                bookDAO.getAllBooks();
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Stock updated successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to update stock - book not found"
                ));
            }
            
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid number format"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Server error: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/admin/updateBook")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateBook(@RequestBody Map<String, Object> requestData, HttpSession session) {
        try {
            // Check if user is admin
            String role = (String) session.getAttribute("role");
            if (!"admin".equals(role)) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied"
                ));
            }
            
            // Extract data
            Object bookIdObj = requestData.get("bookId");
            Object nameObj = requestData.get("name");
            Object priceObj = requestData.get("price");
            Object quantityObj = requestData.get("quantity");
            
            if (bookIdObj == null || nameObj == null || priceObj == null || quantityObj == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Missing required fields"
                ));
            }
            
            int bookId = Integer.parseInt(bookIdObj.toString());
            String name = nameObj.toString();
            double price = Double.parseDouble(priceObj.toString());
            int quantity = Integer.parseInt(quantityObj.toString());
            
            // Update book in database
            boolean updated = bookDAO.updateBookDetails(bookId, name, price, quantity);
            
            if (updated) {
                // BUG FIX: Refresh the books list after update
                bookDAO.getAllBooks();
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Book updated successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to update book - book not found"
                ));
            }
            
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid number format"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Server error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/admin/deleteBook/{bookId}")
    public String deleteBook(@PathVariable int bookId, RedirectAttributes redirectAttributes) {
        try {
            boolean success = bookDAO.deleteBook(bookId);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Book deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to delete book.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error deleting book: " + e.getMessage());
        }
        
        return "redirect:/admin";
    }
}
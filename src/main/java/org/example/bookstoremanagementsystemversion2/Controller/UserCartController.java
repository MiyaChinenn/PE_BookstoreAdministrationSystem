package org.example.bookstoremanagementsystemversion2.Controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.example.bookstoremanagementsystemversion2.DAO.BookDAO;
import org.example.bookstoremanagementsystemversion2.DAO.ShoppingCartDAO;
import org.example.bookstoremanagementsystemversion2.Model.Book;
import org.example.bookstoremanagementsystemversion2.Model.ShoppingCart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping({"/api/user-cart", "/api/cart"}) // Support both endpoints
@CrossOrigin(origins = "*")
public class UserCartController {

    private static final Logger logger = LoggerFactory.getLogger(UserCartController.class);

    @Autowired
    private ShoppingCartDAO shoppingCartDAO;
    
    @Autowired
    private BookDAO bookDAO;

    // In-memory cart storage for guest users (when not logged in)
    private final Map<String, List<CartItem>> guestCarts = new ConcurrentHashMap<>();

    // GET /api/cart and /api/user-cart - Get cart items (supports both guest and user carts)
    @GetMapping
    public ResponseEntity<?> getCart(HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            // Guest user - return in-memory cart
            return getGuestCart(session);
        } else {
            // Logged-in user - return database cart
            return getUserCartFromDatabase(session);
        }
    }

    // POST /api/cart/add and /api/user-cart/add - Add item to cart (supports both guest and user carts)
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request, HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        try {
            // Convert Map to AddToCartRequest for consistency
            AddToCartRequest cartRequest = new AddToCartRequest();
            cartRequest.setBookId(Integer.parseInt(request.get("bookId").toString()));
            cartRequest.setQuantity(request.containsKey("quantity") ? 
                Integer.parseInt(request.get("quantity").toString()) : 1);
            if (request.containsKey("price")) {
                cartRequest.setPrice(Double.parseDouble(request.get("price").toString()));
            }
            
            if (username == null) {
                // Guest user - use in-memory cart, return String response
                return addToGuestCart(cartRequest, session);
            } else {
                // Logged-in user - use database cart, return JSON response for compatibility
                ResponseEntity<String> result = addToUserCartDatabase(cartRequest, session);
                if (result.getStatusCode().is2xxSuccessful()) {
                    return ResponseEntity.ok(Map.of("success", true, "message", result.getBody()));
                } else {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", result.getBody()));
                }
            }
        } catch (Exception e) {
            logger.error("Error in addToCart: {}", e.getMessage());
            if (username == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding item to cart");
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error adding item to cart"));
            }
        }
    }

    // PUT /api/user-cart/update - Update cart item quantity
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateCartItem(@RequestBody Map<String, Object> request, HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Please login first"));
        }

        try {
            int bookId = Integer.parseInt(request.get("bookId").toString());
            int quantity = Integer.parseInt(request.get("quantity").toString());
            double price = Double.parseDouble(request.get("price").toString());
            
            // Check book stock before updating
            Book book = bookDAO.getBookByIdSafe(bookId);
            if (book == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Book not found"));
            }
            
            if (quantity > book.getQuantity()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, 
                    "message", "Quantity exceeds available stock. Available: " + book.getQuantity()));
            }
            
            ShoppingCart item = new ShoppingCart();
            item.setUsername(username);
            item.setBookId(bookId);
            item.setQuantity(quantity);
            item.setPrice(price);
            
            boolean success = shoppingCartDAO.updateItem(item);
            
            if (success) {
                logger.info("Updated cart item for user {}: bookId={}, quantity={}", username, bookId, quantity);
                return ResponseEntity.ok(Map.of("success", true, "message", "Cart updated"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to update cart"));
            }
            
        } catch (Exception e) {
            logger.error("Error updating cart item for user {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error updating cart"));
        }
    }

    // DELETE /api/user-cart/remove - Remove item from cart
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeFromCart(@RequestParam Integer bookId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Please login first"));
        }

        try {
            boolean success;
            try {
                success = shoppingCartDAO.deleteItem(username, bookId);
            } catch (SQLException e) {
                logger.error("Error deleting cart item for user {}: {}", username, e.getMessage());
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error removing item"));
            }
            
            if (success) {
                logger.info("Removed cart item for user {}: bookId={}", username, bookId);
                return ResponseEntity.ok(Map.of("success", true, "message", "Item removed from cart"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to remove item"));
            }
            
        } catch (NumberFormatException e) {
            logger.error("Error removing cart item for user {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error removing item"));
        }
    }

    // POST /api/user-cart/sync - Sync guest cart to user cart on login
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncGuestCart(@RequestBody List<Map<String, Object>> guestCart, HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Please login first"));
        }

        try {
            int syncedItems = 0;
            
            for (Map<String, Object> guestItem : guestCart) {
                try {
                    // Validate guest item data
                    if (guestItem.get("bookId") == null || guestItem.get("quantity") == null || guestItem.get("price") == null) {
                        logger.warn("Skipping guest cart item with missing data: {}", guestItem);
                        continue;
                    }
                    
                    int bookId = Integer.parseInt(guestItem.get("bookId").toString());
                    int quantity = Integer.parseInt(guestItem.get("quantity").toString());
                    double price = Double.parseDouble(guestItem.get("price").toString());
                    
                    // Basic validation
                    if (bookId <= 0 || quantity <= 0 || price < 0) {
                        logger.warn("Skipping guest cart item with invalid data: bookId={}, quantity={}, price={}", bookId, quantity, price);
                        continue;
                    }
                    
                    // Check if item already exists in user's cart
                    try {
                        shoppingCartDAO.fetchCartItems(username);
                    } catch (SQLException e) {
                        logger.error("Error fetching cart items for sync for user {}: {}", username, e.getMessage());
                        continue; // Skip this item and continue with next
                    }
                    List<ShoppingCart> existingItems = shoppingCartDAO.getItems();
                    
                    ShoppingCart existingItem = existingItems.stream()
                        .filter(cartItem -> cartItem.getBookId().equals(bookId))
                        .findFirst()
                        .orElse(null);
                    
                    if (existingItem != null) {
                        // Update existing item quantity
                        int newQuantity = existingItem.getQuantity() + quantity;
                        existingItem.setQuantity(newQuantity);
                        if (shoppingCartDAO.updateItem(existingItem)) {
                            syncedItems++;
                            logger.info("Updated existing cart item for user {}: bookId={}, newQuantity={}", username, bookId, newQuantity);
                        }
                    } else {
                        // Add new item
                        if (shoppingCartDAO.addItem(username, bookId, quantity, price)) {
                            syncedItems++;
                            logger.info("Added new cart item for user {}: bookId={}, quantity={}, price={}", username, bookId, quantity, price);
                        }
                    }
                    
                } catch (NumberFormatException e) {
                    logger.warn("Skipping guest cart item with invalid number format: {}", guestItem);
                } catch (Exception e) {
                    logger.error("Error processing guest cart item {}: {}", guestItem, e.getMessage());
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Guest cart synced successfully",
                "syncedItems", syncedItems
            ));
            
        } catch (Exception e) {
            logger.error("Error syncing guest cart for user {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error syncing guest cart"));
        }
    }

    // POST /api/user-cart/clear - Clear user's cart (used after successful checkout)
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearUserCart(HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Please login first"));
        }

        try {
            boolean success;
            try {
                success = shoppingCartDAO.clearCart(username);
            } catch (Exception e) {
                logger.error("Error clearing cart for user {}: {}", username, e.getMessage());
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error clearing cart"));
            }
            
            if (success) {
                logger.info("Cleared cart for user: {}", username);
                return ResponseEntity.ok(Map.of("success", true, "message", "Cart cleared successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Failed to clear cart"));
            }
        } catch (NumberFormatException e) {
            logger.error("Error processing clear cart request for user {}: {}", username, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Error clearing cart"));
        }
    }

    // Additional endpoints for frontend compatibility (consolidated from CartApiController)
    
    // GET /api/cart/count - Get cart item count (supports both guest and user carts)
    @GetMapping("/count")
    public ResponseEntity<Integer> getCartCount(HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            // Guest user - count in-memory cart
            return getGuestCartCount(session);
        } else {
            // Logged-in user - count database cart
            return getUserCartCount(session);
        }
    }
    
    // POST /api/cart/transfer - Transfer guest cart to user cart
    @PostMapping("/transfer")
    public ResponseEntity<String> transferGuestCart(@RequestBody List<GuestCartItem> guestItems, HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please login");
        }

        try {
            int syncedItems = 0;
            
            for (GuestCartItem guestItem : guestItems) {
                try {
                    // Validate guest item data
                    if (guestItem.getBookId() == null || guestItem.getQuantity() == null || guestItem.getPrice() == null) {
                        logger.warn("Skipping guest cart item with missing data: {}", guestItem);
                        continue;
                    }
                    
                    int bookId = guestItem.getBookId();
                    int quantity = guestItem.getQuantity();
                    double price = guestItem.getPrice();
                    
                    // Basic validation
                    if (bookId <= 0 || quantity <= 0 || price < 0) {
                        logger.warn("Skipping guest cart item with invalid data: bookId={}, quantity={}, price={}", bookId, quantity, price);
                        continue;
                    }
                    
                    // Check if item already exists in user's cart
                    try {
                        shoppingCartDAO.fetchCartItems(username);
                    } catch (SQLException e) {
                        logger.error("Error fetching cart items for transfer for user {}: {}", username, e.getMessage());
                        continue; // Skip this item and continue with next
                    }
                    List<ShoppingCart> existingItems = shoppingCartDAO.getItems();
                    
                    ShoppingCart existingItem = existingItems.stream()
                        .filter(cartItem -> cartItem.getBookId().equals(bookId))
                        .findFirst()
                        .orElse(null);
                    
                    if (existingItem != null) {
                        // Update existing item quantity
                        int newQuantity = existingItem.getQuantity() + quantity;
                        existingItem.setQuantity(newQuantity);
                        if (shoppingCartDAO.updateItem(existingItem)) {
                            syncedItems++;
                            logger.info("Updated existing cart item for user {}: bookId={}, newQuantity={}", username, bookId, newQuantity);
                        }
                    } else {
                        // Add new item
                        if (shoppingCartDAO.addItem(username, bookId, quantity, price)) {
                            syncedItems++;
                            logger.info("Added new cart item for user {}: bookId={}, quantity={}, price={}", username, bookId, quantity, price);
                        }
                    }
                    
                } catch (NumberFormatException e) {
                    logger.warn("Skipping guest cart item with invalid number format: {}", guestItem);
                } catch (Exception e) {
                    logger.error("Error processing guest cart item {}: {}", guestItem, e.getMessage());
                }
            }
            
            logger.info("Transferred {} guest items to cart for user {}", syncedItems, username);
            return ResponseEntity.ok("Guest cart transferred successfully");
            
        } catch (Exception e) {
            logger.error("Error transferring guest cart for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error transferring cart");
        }
    }

    // Helper methods for guest cart operations
    private ResponseEntity<String> addToGuestCart(AddToCartRequest request, HttpSession session) {
        try {
            String sessionId = session.getId();
            
            // Validate book exists and has stock
            bookDAO.getAllBooks();
            List<Book> books = bookDAO.getBooksList();
            
            Book book = books.stream()
                .filter(b -> request.getBookId().equals(b.getBookId()))
                .findFirst()
                .orElse(null);

            if (book == null) {
                return ResponseEntity.badRequest().body("Book not found");
            }

            if (book.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body("Book is out of stock");
            }

            // Add to guest cart
            List<CartItem> cart = guestCarts.computeIfAbsent(sessionId, k -> new ArrayList<>());
            
            // Check if item already exists in cart
            CartItem existingItem = cart.stream()
                .filter(item -> item.getBookId().equals(request.getBookId()))
                .findFirst()
                .orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
                logger.info("Updated quantity for book {} in guest cart for session {}", request.getBookId(), sessionId);
            } else {
                CartItem newItem = new CartItem();
                newItem.setBookId(request.getBookId());
                newItem.setName(book.getName());
                newItem.setPrice(request.getPrice() != null ? request.getPrice() : book.getPrice());
                newItem.setQuantity(request.getQuantity());
                newItem.setAuthor(book.getPublisher()); // Use publisher as author
                cart.add(newItem);
                logger.info("Added new item {} to guest cart for session {}", book.getName(), sessionId);
            }

            return ResponseEntity.ok("Item added to cart successfully");
            
        } catch (Exception e) {
            logger.error("Error adding item to guest cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding item to cart");
        }
    }
    
    private ResponseEntity<String> addToUserCartDatabase(AddToCartRequest request, HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        try {
            int bookId = request.getBookId();
            int quantity = request.getQuantity();
            Double requestPrice = request.getPrice();
            double price = requestPrice != null ? requestPrice : 0.0;
            
            // Get book price if not provided
            if (price == 0.0) {
                Book book = bookDAO.getBookByIdSafe(bookId);
                if (book != null) {
                    price = book.getPrice();
                }
            }
            
            // Check book stock first
            Book book = bookDAO.getBookByIdSafe(bookId);
            if (book == null) {
                return ResponseEntity.badRequest().body("Book not found");
            }
            
            // Check if item already exists in cart
            try {
                shoppingCartDAO.fetchCartItems(username);
            } catch (SQLException e) {
                logger.error("Error fetching cart items for user {}: {}", username, e.getMessage());
                return ResponseEntity.badRequest().body("Error accessing cart");
            }
            List<ShoppingCart> existingItems = shoppingCartDAO.getItems();
            
            ShoppingCart existingItem = existingItems.stream()
                .filter(cartItem -> cartItem.getBookId() != null && cartItem.getBookId().equals(bookId))
                .findFirst()
                .orElse(null);
            
            int currentCartQuantity = 0;
            if (existingItem != null && existingItem.getQuantity() != null) {
                currentCartQuantity = existingItem.getQuantity();
            }
            int totalRequestedQuantity = currentCartQuantity + quantity;
            
            // Check if total quantity exceeds available stock
            if (totalRequestedQuantity > book.getQuantity()) {
                int availableToAdd = book.getQuantity() - currentCartQuantity;
                String message;
                if (availableToAdd <= 0) {
                    message = "This book is already at maximum quantity in your cart (Stock: " + book.getQuantity() + ")";
                } else {
                    message = "Not enough stock. You can only add " + availableToAdd + " more (Stock: " + book.getQuantity() + ", In cart: " + currentCartQuantity + ")";
                }
                return ResponseEntity.badRequest().body(message);
            }
            
            boolean success;
            if (existingItem != null) {
                // Update existing item
                existingItem.setQuantity(totalRequestedQuantity);
                success = shoppingCartDAO.updateItem(existingItem);
                logger.info("Updated cart item for user {}: bookId={}, newQuantity={}", username, bookId, existingItem.getQuantity());
            } else {
                // Add new item
                success = shoppingCartDAO.addItem(username, bookId, quantity, price);
                logger.info("Added new cart item for user {}: bookId={}, quantity={}, price={}", username, bookId, quantity, price);
            }
            
            if (success) {
                return ResponseEntity.ok("Item added to cart successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to add item to cart");
            }
            
        } catch (Exception e) {
            logger.error("Error adding item to cart for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding item to cart");
        }
    }
    
    private ResponseEntity<List<CartItem>> getGuestCart(HttpSession session) {
        String sessionId = session.getId();
        List<CartItem> cart = guestCarts.getOrDefault(sessionId, new ArrayList<>());
        logger.info("Retrieved guest cart for session: {} with {} items", sessionId, cart.size());
        return ResponseEntity.ok(cart);
    }
    
    private ResponseEntity<Integer> getGuestCartCount(HttpSession session) {
        String sessionId = session.getId();
        List<CartItem> cart = guestCarts.getOrDefault(sessionId, new ArrayList<>());
        int totalItems = cart.stream().mapToInt(CartItem::getQuantity).sum();
        logger.info("Guest cart count for session {}: {}", sessionId, totalItems);
        return ResponseEntity.ok(totalItems);
    }
    
    private ResponseEntity<Integer> getUserCartCount(HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        try {
            try {
                shoppingCartDAO.fetchCartItems(username);
            } catch (SQLException e) {
                logger.error("Error fetching cart items for count for user {}: {}", username, e.getMessage());
                return ResponseEntity.ok(0);
            }
            List<ShoppingCart> cartItems = shoppingCartDAO.getItems();
            int count = cartItems.stream().mapToInt(ShoppingCart::getQuantity).sum();
            
            logger.info("Cart count for user {}: {}", username, count);
            return ResponseEntity.ok(count);
        } catch (NumberFormatException e) {
            logger.error("Error getting cart count for user {}: {}", username, e.getMessage());
            return ResponseEntity.ok(0);
        }
    }

    // Helper method to get user cart from database
    private ResponseEntity<List<Map<String, Object>>> getUserCartFromDatabase(HttpSession session) {
        String username = (String) session.getAttribute("username");
        
        if (username == null) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        try {
            // Fetch cart items from database
            try {
                shoppingCartDAO.fetchCartItems(username);
            } catch (SQLException e) {
                logger.error("Error fetching cart items for user {}: {}", username, e.getMessage());
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            List<ShoppingCart> cartItems = shoppingCartDAO.getItems();
            
            if (cartItems.isEmpty()) {
                logger.info("No cart items found for user: {}", username);
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            // Convert to frontend format with book details
            List<Map<String, Object>> cartResponse = new ArrayList<>();
            
            // Get all books for reference
            try {
                bookDAO.getAllBooks();
            } catch (SQLException e) {
                logger.error("Error loading books for user {}: {}", username, e.getMessage());
                return ResponseEntity.ok(new ArrayList<>());
            }
            List<Book> allBooks = bookDAO.getBooksList();
            
            logger.info("Total books loaded: {}, cart items to process: {}", allBooks.size(), cartItems.size());
            
            for (ShoppingCart item : cartItems) {
                Integer cartBookId = item.getBookId();
                logger.info("Processing cart item with bookId: {}", cartBookId);
                
                // Find book details
                Book book = null;
                for (Book b : allBooks) {
                    if (b.getBookId() != null && b.getBookId().equals(cartBookId)) {
                        book = b;
                        break;
                    }
                }
                
                if (book != null) {
                    logger.info("Book found: '{}' (ID: {})", book.getName(), book.getBookId());
                    
                    Map<String, Object> cartItem = new HashMap<>();
                    cartItem.put("bookId", item.getBookId());
                    cartItem.put("name", book.getName() != null ? book.getName() : "Unknown Book");
                    cartItem.put("price", item.getPrice());
                    cartItem.put("quantity", item.getQuantity());
                    
                    logger.info("Cart item created - Name: '{}', BookId: {}", 
                               cartItem.get("name"), cartItem.get("bookId"));
                    cartResponse.add(cartItem);
                } else {
                    logger.error("Book NOT FOUND for bookId: {}", cartBookId);
                    
                    // Still add item but with fallback data to prevent cart from breaking
                    Map<String, Object> cartItem = new HashMap<>();
                    cartItem.put("bookId", item.getBookId());
                    cartItem.put("name", "Unknown Book (ID: " + cartBookId + ")");
                    cartItem.put("price", item.getPrice());
                    cartItem.put("quantity", item.getQuantity());
                    cartResponse.add(cartItem);
                }
            }
            
            logger.info("Successfully processed {} cart items for user: {}", cartResponse.size(), username);
            return ResponseEntity.ok(cartResponse);
            
        } catch (NumberFormatException e) {
            logger.error("Error parsing cart data for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // Inner classes for request/response objects (copied from CartApiController)
    public static class CartItem {
        private Integer bookId;
        private String name;
        private Double price;
        private Integer quantity;
        private String author;

        // Getters and setters
        public Integer getBookId() { return bookId; }
        public void setBookId(Integer bookId) { this.bookId = bookId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
    }

    public static class AddToCartRequest {
        private Integer bookId;
        private Integer quantity = 1;
        private Double price;

        // Getters and setters
        public Integer getBookId() { return bookId; }
        public void setBookId(Integer bookId) { this.bookId = bookId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }

    public static class GuestCartItem {
        private Integer bookId;
        private String name;
        private Double price;
        private Integer quantity;
        private String author;

        // Getters and setters
        public Integer getBookId() { return bookId; }
        public void setBookId(Integer bookId) { this.bookId = bookId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
    }
}

package org.example.bookstoremanagementsystemversion2.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "shoppingcart")
@IdClass(ShoppingCartId.class)
public class ShoppingCart {
    @Id
    @Column(name = "username")
    private String username;
    
    @Id
    @Column(name = "bookId")
    private Integer bookId; // Changed from int to Integer
    
    @Column(name = "quantity")
    private Integer quantity; // Changed from int to Integer
    
    @Column(name = "price")
    private Double price; // Changed from double to Double
    
    // Constructors
    public ShoppingCart() {}
    
    public ShoppingCart(String username, Integer bookId, Integer quantity, Double price) {
        this.username = username;
        this.bookId = bookId;
        this.quantity = quantity;
        this.price = price;
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public Integer getBookId() { return bookId; } // Changed return type
    public void setBookId(Integer bookId) { this.bookId = bookId; } // Changed parameter type
    
    public Integer getQuantity() { return quantity; } // Changed return type
    public void setQuantity(Integer quantity) { this.quantity = quantity; } // Changed parameter type
    
    public Double getPrice() { return price; } // Changed return type
    public void setPrice(Double price) { this.price = price; } // Changed parameter type
}
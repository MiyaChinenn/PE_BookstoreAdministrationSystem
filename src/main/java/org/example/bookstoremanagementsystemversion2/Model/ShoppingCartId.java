package org.example.bookstoremanagementsystemversion2.Model;

import java.io.Serializable;
import java.util.Objects;

public class ShoppingCartId implements Serializable {
    private String username;
    private int bookId;
    
    public ShoppingCartId() {}
    
    public ShoppingCartId(String username, int bookId) {
        this.username = username;
        this.bookId = bookId;
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingCartId that = (ShoppingCartId) o;
        return bookId == that.bookId && Objects.equals(username, that.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(username, bookId);
    }
}
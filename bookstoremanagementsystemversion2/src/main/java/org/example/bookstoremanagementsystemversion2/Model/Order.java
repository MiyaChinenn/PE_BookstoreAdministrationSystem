package org.example.bookstoremanagementsystemversion2.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderId")
    private Integer orderId;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "orderDate")
    private LocalDateTime orderDate;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "totalAmount")
    private Double totalAmount;
    
    @Column(name = "totalItems")
    private Integer totalItems;
    
    // Default constructor (required by JPA)
    public Order() {
        this.orderDate = LocalDateTime.now();
        this.status = "Pending";
    }
    
    // Constructor with parameters
    public Order(String username, Double totalAmount, Integer totalItems) {
        this.username = username;
        this.totalAmount = totalAmount;
        this.totalItems = totalItems;
        this.orderDate = LocalDateTime.now();
        this.status = "Pending";
    }
    
    // Getters and Setters
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", username='" + username + '\'' +
                ", orderDate=" + orderDate +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                ", totalItems=" + totalItems +
                '}';
    }
}

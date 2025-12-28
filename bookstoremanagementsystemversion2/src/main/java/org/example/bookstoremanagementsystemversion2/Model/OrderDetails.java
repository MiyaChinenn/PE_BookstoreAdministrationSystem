package org.example.bookstoremanagementsystemversion2.Model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "orderdetails")
public class OrderDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderDetailId")
    private Integer orderDetailId;
    
    @Column(name = "orderId", nullable = false)
    private Integer orderId;
    
    @Column(name = "bookId", nullable = false)
    private Integer bookId;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "price", nullable = false)
    private Double price;
    
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    // FIX: Add missing fields for book info (not mapped to database - using @Transient)
    @Transient
    private String bookName;
    
    @Transient
    private String bookPublisher;
    
    // Constructors
    public OrderDetails() {}
    
    public OrderDetails(Integer orderId, Integer bookId, Integer quantity, Double price) {
        this.orderId = orderId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = BigDecimal.valueOf(quantity * price);
    }
    
    // Getters and Setters
    public Integer getOrderDetailId() { return orderDetailId; }
    public void setOrderDetailId(Integer orderDetailId) { this.orderDetailId = orderDetailId; }
    
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }
    
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    
    // FIX: Add missing getters and setters for book info
    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }
    
    public String getBookPublisher() { return bookPublisher; }
    public void setBookPublisher(String bookPublisher) { this.bookPublisher = bookPublisher; }
    
    // Helper method to get subtotal as double if needed
    public Double getSubtotalAsDouble() {
        return subtotal != null ? subtotal.doubleValue() : 0.0;
    }
    
    @Override
    public String toString() {
        return "OrderDetails{" +
                "orderDetailId=" + orderDetailId +
                ", orderId=" + orderId +
                ", bookId=" + bookId +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + subtotal +
                ", bookName='" + bookName + '\'' +
                ", bookPublisher='" + bookPublisher + '\'' +
                '}';
    }
}

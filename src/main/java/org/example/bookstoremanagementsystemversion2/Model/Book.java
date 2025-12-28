package org.example.bookstoremanagementsystemversion2.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookId")
    private Integer bookId;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "price")
    private Double price;
    
    @Column(name = "publisher")
    private String publisher;
    
    @Column(name = "quantity")
    private Integer quantity;

    // Default constructor
    public Book() {}

    // Constructor with existing database fields only
    public Book(String type, String name, Double price, String publisher, Integer quantity) {
        this.type = type;
        this.name = name;
        this.price = price;
        this.publisher = publisher;
        this.quantity = quantity;
    }

    // Getters and setters for existing fields only
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", publisher='" + publisher + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
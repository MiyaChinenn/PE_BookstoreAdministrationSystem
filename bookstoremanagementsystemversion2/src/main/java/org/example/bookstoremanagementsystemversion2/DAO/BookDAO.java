package org.example.bookstoremanagementsystemversion2.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.bookstoremanagementsystemversion2.Model.Book;
import org.example.bookstoremanagementsystemversion2.Model.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BookDAO {
    private List<Book> books;
    private final Database database;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    public BookDAO(Database database) {
        this.database = database;
    }

    // Public method to get book by ID (for use by other controllers)
    public Book getBookById(Integer bookId) throws SQLException {
        if (bookId == null) {
            System.err.println("Error: bookId cannot be null");
            return null;
        }
        
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        Book temp = null;
        try {
            String sql = "SELECT bookId, type, name, price, publisher, quantity FROM books WHERE bookId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, bookId);
            rs = stm.executeQuery();
            if (rs.next()) {
                temp = new Book();
                temp.setBookId(rs.getInt("bookId"));
                temp.setType(rs.getString("type"));
                temp.setName(rs.getString("name"));
                temp.setPrice(rs.getDouble("price"));
                temp.setPublisher(rs.getString("publisher"));
                temp.setQuantity(rs.getInt("quantity"));
            }
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
        return temp;
    }

    // Safe wrapper method for external controllers (handles exceptions internally)
    public Book getBookByIdSafe(Integer bookId) {
        try {
            return getBookById(bookId);
        } catch (SQLException e) {
            System.err.println("Error getting book by ID " + bookId + ": " + e.getMessage());
            return null;
        }
    }

    public void fetchBooks(Integer bookId, String type, String name, String publisher) throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            books = new ArrayList<>();
            String sql = "SELECT bookId, type, name, price, publisher, quantity FROM books WHERE 1=1";
            List<Object> params = new ArrayList<>();
            
            if (bookId != null && bookId > 0) {
                sql += " AND bookId = ?";
                params.add(bookId);
            }
            if (type != null && !type.trim().isEmpty()) {
                sql += " AND type LIKE ?";
                params.add("%" + type + "%");
            }
            if (name != null && !name.trim().isEmpty()) {
                sql += " AND name LIKE ?";
                params.add("%" + name + "%");
            }
            if (publisher != null && !publisher.trim().isEmpty()) {
                sql += " AND publisher LIKE ?";
                params.add("%" + publisher + "%");
            }
            
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stm.setObject(i + 1, params.get(i));
            }
            
            rs = stm.executeQuery();
            while (rs.next()) {
                Book temp = new Book();
                temp.setBookId(rs.getInt("bookId"));
                temp.setType(rs.getString("type"));
                temp.setName(rs.getString("name"));
                temp.setPrice(rs.getDouble("price"));
                temp.setPublisher(rs.getString("publisher"));
                temp.setQuantity(rs.getInt("quantity"));
                books.add(temp);
            }
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public boolean createBook(Book book) throws SQLException {
        if (book == null) return false;
        
        Connection con = null;
        PreparedStatement stm = null;
        try {
            if (book.getType() == null || book.getType().trim().isEmpty() || 
                book.getName() == null || book.getName().trim().isEmpty() || 
                book.getPrice() < 0 || book.getQuantity() < 0) {
                return false;
            }
            
            String publisher = book.getPublisher();
            if (publisher == null) publisher = "";
            
            String sql = "INSERT INTO books (type, name, price, publisher, quantity) VALUES (?, ?, ?, ?, ?)";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, book.getType());
            stm.setString(2, book.getName());
            stm.setDouble(3, book.getPrice());
            stm.setString(4, publisher);
            stm.setInt(5, book.getQuantity());
            
            boolean result = stm.executeUpdate() > 0;
            
            // BUG FIX: Refresh the books list after creating
            if (result) {
                getAllBooks();
            }
            
            return result;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public boolean createBook(String type, String name, double price, String publisher, int quantity) throws SQLException {
        Book book = new Book();
        book.setType(type);
        book.setName(name);
        book.setPrice(price);
        book.setPublisher(publisher);
        book.setQuantity(quantity);
        return createBook(book);
    }

    public boolean updateBook(Book info) throws SQLException {
        if (info == null || info.getBookId() <= 0) return false;
        
        Connection con = null;
        PreparedStatement stm = null;
        try {
            Book target = getBookById(info.getBookId());
            if (target == null) {
                return false;
            }
            
            if (info.getName() == null || info.getName().trim().isEmpty()) {
                return false;
            }
            if (info.getPrice() < 0 || info.getQuantity() < 0) {
                return false;
            }
            
            if (info.getType() != null && !info.getType().trim().isEmpty()) {
                target.setType(info.getType());
            }
            if (info.getName() != null && !info.getName().trim().isEmpty()) {
                target.setName(info.getName());
            }
            if (info.getPrice() >= 0) {
                target.setPrice(info.getPrice());
            }
            if (info.getPublisher() != null) {
                target.setPublisher(info.getPublisher());
            }
            if (info.getQuantity() >= 0) {
                target.setQuantity(info.getQuantity());
            }
            
            String sql = "UPDATE books SET type = ?, name = ?, price = ?, publisher = ?, quantity = ? WHERE bookId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, target.getType());
            stm.setString(2, target.getName());
            stm.setDouble(3, target.getPrice());
            stm.setString(4, target.getPublisher());
            stm.setInt(5, target.getQuantity());
            stm.setInt(6, target.getBookId());
            
            boolean result = stm.executeUpdate() > 0;
            
            // BUG FIX: Refresh the books list after updating
            if (result) {
                getAllBooks();
            }
            
            return result;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public boolean deleteBook(int bookId) throws SQLException {
        if (bookId <= 0) return false;
        
        Connection con = null;
        PreparedStatement stm = null;
        try {
            String sql = "DELETE FROM books WHERE bookId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, bookId);
            
            boolean result = stm.executeUpdate() > 0;
            
            // BUG FIX: Refresh the books list after deleting
            if (result) {
                getAllBooks();
            }
            
            return result;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public void getAllBooks() throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            books = new ArrayList<>();
            String sql = "SELECT bookId, type, name, price, publisher, quantity FROM books ORDER BY name";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            rs = stm.executeQuery();
            while (rs.next()) {
                Book temp = new Book();
                temp.setBookId(rs.getInt("bookId"));
                temp.setType(rs.getString("type"));
                temp.setName(rs.getString("name"));
                temp.setPrice(rs.getDouble("price"));
                temp.setPublisher(rs.getString("publisher"));
                temp.setQuantity(rs.getInt("quantity"));
                books.add(temp);
            }
            System.out.println("BookDAO: Loaded " + books.size() + " books");
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public List<Book> getBooksList() {
        return books != null ? books : new ArrayList<>();
    }

    public List<Book> getBooks() {
        if (books == null) {
            try {
                getAllBooks();
            } catch (SQLException e) {
                System.err.println("Error loading books: " + e.getMessage());
                books = new ArrayList<>();
            }
        }
        return books;
    }

    // BUG FIX: Add missing updateBookStock method
    public boolean updateBookStock(int bookId, int newQuantity) {
        String sql = "UPDATE books SET quantity = ? WHERE bookId = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, newQuantity, bookId);
            System.out.println("Updated book stock: bookId=" + bookId + ", quantity=" + newQuantity + ", rowsAffected=" + rowsAffected);
            
            // BUG FIX: Refresh the books list after stock update
            if (rowsAffected > 0) {
                getAllBooks();
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating book stock: " + e.getMessage());
            return false;
        }
    }

    // BUG FIX: Add missing updateBookDetails method
    public boolean updateBookDetails(int bookId, String name, double price, int quantity) {
        String sql = "UPDATE books SET name = ?, price = ?, quantity = ? WHERE bookId = ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, name, price, quantity, bookId);
            System.out.println("Updated book details: bookId=" + bookId + ", rowsAffected=" + rowsAffected);
            
            // BUG FIX: Refresh the books list after details update
            if (rowsAffected > 0) {
                getAllBooks();
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating book details: " + e.getMessage());
            return false;
        }
    }

    // BUG FIX: Add missing reduceBookStock method for checkout
    public boolean reduceBookStock(int bookId, int quantityToReduce) {
        String sql = "UPDATE books SET quantity = quantity - ? WHERE bookId = ? AND quantity >= ?";
        try {
            int rowsAffected = jdbcTemplate.update(sql, quantityToReduce, bookId, quantityToReduce);
            System.out.println("Reduced book stock: bookId=" + bookId + ", reduced=" + quantityToReduce + ", rowsAffected=" + rowsAffected);
            
            // BUG FIX: Refresh the books list after stock reduction
            if (rowsAffected > 0) {
                getAllBooks();
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error reducing book stock: " + e.getMessage());
            return false;
        }
    }
}
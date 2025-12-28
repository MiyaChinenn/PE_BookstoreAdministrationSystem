package org.example.bookstoremanagementsystemversion2.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.bookstoremanagementsystemversion2.Model.Database;
import org.example.bookstoremanagementsystemversion2.Model.ShoppingCart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ShoppingCartDAO {
    private List<ShoppingCart> items;
    private final Database database;

    @Autowired
    public ShoppingCartDAO(Database database) {
        this.database = database;
    }

    public void fetchCartItems(String username) throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            items = new ArrayList<>();
            String sql = "SELECT username, bookId, quantity, price FROM shoppingcart WHERE username = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, username);
            rs = stm.executeQuery();
            while (rs.next()) {
                ShoppingCart item = new ShoppingCart();
                item.setUsername(rs.getString("username"));
                item.setBookId(rs.getInt("bookId"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPrice(rs.getDouble("price"));
                items.add(item);
            }
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public boolean addItem(String username, int bookId, int quantity, double price) throws SQLException, Exception {
        Connection con = null;
        PreparedStatement stm = null;
        try {
            if (username == null || username.isEmpty() || quantity <= 0 || price < 0) {
                throw new Exception("INVALID PARAMETER");
            }
            String sql = "INSERT INTO shoppingcart (username, bookId, quantity, price) VALUES (?, ?, ?, ?)";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, username);
            stm.setInt(2, bookId);
            stm.setInt(3, quantity);
            stm.setDouble(4, price);
            return stm.executeUpdate() > 0;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public boolean updateItem(ShoppingCart updatedItem) throws SQLException , Exception {
        Connection con = null;
        PreparedStatement stm = null;
        try {
            if(updatedItem.getUsername() == null || updatedItem.getUsername().isEmpty())
                throw new Exception("INVALID PARAMETER");
            if(updatedItem.getQuantity() <= 0)
                throw new Exception("INVALID PARAMETER");
            if(updatedItem.getPrice() < 0)
                throw new Exception("INVALID PARAMETER");
            String sql = "UPDATE shoppingcart SET quantity = ?, price = ? WHERE username = ? AND bookId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, updatedItem.getQuantity());
            stm.setDouble(2, updatedItem.getPrice());
            stm.setString(3, updatedItem.getUsername());
            stm.setInt(4, updatedItem.getBookId());
            return stm.executeUpdate() > 0;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public boolean deleteItem(String username, int bookId) throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        try {
            String sql = "DELETE FROM shoppingcart WHERE username = ? AND bookId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, username);
            stm.setInt(2, bookId);
            return stm.executeUpdate() > 0;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    // NEW: Clear entire cart for a user (for checkout process)
    public boolean clearCart(String username) throws SQLException, Exception {
        Connection con = null;
        PreparedStatement stm = null;
        try {
            if (username == null || username.isEmpty()) {
                throw new Exception("INVALID PARAMETER");
            }
            String sql = "DELETE FROM shoppingcart WHERE username = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, username);
            return stm.executeUpdate() >= 0; // Returns true even if cart was already empty
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public List<ShoppingCart> getItems() {
        return items;
    }
}


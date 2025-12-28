package org.example.bookstoremanagementsystemversion2.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.example.bookstoremanagementsystemversion2.Model.Database;
import org.example.bookstoremanagementsystemversion2.Model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OrderDAO {
    private List<Order> ordersList;
    private final Database database;

    private JdbcTemplate jdbcTemplate;

    public OrderDAO(Database database) {
        this.database = database;
    }
    
    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Order getOrderById(int id) throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        Order order = null;
        try {
            // All SQL queries use proper column names matching database schema
            String sql = "SELECT orderId, username, orderDate, status, totalAmount, totalItems FROM orders WHERE orderId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, id);
            rs = stm.executeQuery();
            if (rs.next()) {
                order = new Order();
                order.setOrderId(rs.getInt("orderId"));
                order.setUsername(rs.getString("username"));
                // FIX: Handle both DATETIME and DATE formats
                Timestamp timestamp = rs.getTimestamp("orderDate");
                if (timestamp != null) {
                    order.setOrderDate(timestamp.toLocalDateTime());
                }
                order.setStatus(rs.getString("status"));
                order.setTotalAmount(rs.getDouble("totalAmount"));
                order.setTotalItems(rs.getInt("totalItems"));
            }
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
        return order;
    }

    public void fetchOrders(String username) throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            ordersList = new ArrayList<>();
            String sql = "SELECT orderId, username, orderDate, status, totalAmount, totalItems FROM orders WHERE username LIKE ? ORDER BY orderDate DESC";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, "%" + username + "%");
            rs = stm.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("orderId"));
                order.setUsername(rs.getString("username"));
                // FIX: Handle both DATETIME and DATE formats
                Timestamp timestamp = rs.getTimestamp("orderDate");
                if (timestamp != null) {
                    order.setOrderDate(timestamp.toLocalDateTime());
                }
                order.setStatus(rs.getString("status"));
                order.setTotalAmount(rs.getDouble("totalAmount"));
                order.setTotalItems(rs.getInt("totalItems"));
                ordersList.add(order);
            }
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public void fetchAllOrders() throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            ordersList = new ArrayList<>();
            String sql = "SELECT orderId, username, orderDate, status, totalAmount, totalItems FROM orders ORDER BY orderDate DESC";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            rs = stm.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt("orderId"));
                order.setUsername(rs.getString("username"));
                // FIX: Handle both DATETIME and DATE formats
                Timestamp timestamp = rs.getTimestamp("orderDate");
                if (timestamp != null) {
                    order.setOrderDate(timestamp.toLocalDateTime());
                }
                order.setStatus(rs.getString("status"));
                order.setTotalAmount(rs.getDouble("totalAmount"));
                order.setTotalItems(rs.getInt("totalItems"));
                ordersList.add(order);
            }
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public boolean createOrder(String username, LocalDateTime orderDate) throws SQLException, Exception {
        Connection con = null;
        PreparedStatement stm = null;
        try {
            if (username == null || username.isEmpty() || orderDate == null) {
                throw new Exception("INVALID PARAMETER");
            }
            String sql = "INSERT INTO orders (username, orderDate, status, totalAmount, totalItems) VALUES (?, ?, ?, ?, ?)";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, username);
            stm.setTimestamp(2, Timestamp.valueOf(orderDate));
            stm.setString(3, "Pending");
            stm.setDouble(4, 0.0);
            stm.setInt(5, 0);
            return stm.executeUpdate() > 0;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    // FIXED: Use correct column names matching database schema
    public int createOrderWithTotals(String username, double totalAmount, int totalItems) {
        String sql = "INSERT INTO orders (username, totalAmount, totalItems, orderDate, status) VALUES (?, ?, ?, NOW(), 'PENDING')";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            int rowsAffected = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, username);
                ps.setDouble(2, totalAmount);
                ps.setInt(3, totalItems);
                return ps;
            }, keyHolder);
            
            if (rowsAffected > 0) {
                Number generatedId = keyHolder.getKey();
                System.out.println("Order created successfully: orderId=" + generatedId + ", total=" + totalAmount);
                return generatedId != null ? generatedId.intValue() : -1;
            }
            return -1;
        } catch (Exception e) {
            System.err.println("Error creating order: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public boolean updateOrder(Order info) throws Exception {
        Connection con = null;
        PreparedStatement stm = null;
        Order target = getOrderById(info.getOrderId());
        try {
            if (target == null)
                throw new Exception("Order not found");
            if (info.getUsername() == null || info.getUsername().isEmpty())
                throw new Exception("INVALID PARAMETER");
            
            target.setUsername(info.getUsername());
            if (info.getOrderDate() != null) target.setOrderDate(info.getOrderDate());
            if (info.getStatus() != null) target.setStatus(info.getStatus());
            target.setTotalAmount(info.getTotalAmount());
            target.setTotalItems(info.getTotalItems());
            
            String sql = "UPDATE orders SET username = ?, orderDate = ?, status = ?, totalAmount = ?, totalItems = ? WHERE orderId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, target.getUsername());
            stm.setTimestamp(2, Timestamp.valueOf(target.getOrderDate()));
            stm.setString(3, target.getStatus());
            stm.setDouble(4, target.getTotalAmount());
            stm.setInt(5, target.getTotalItems());
            stm.setInt(6, target.getOrderId());
            return stm.executeUpdate() > 0;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public List<Order> getOrdersList() {
        return ordersList;
    }
}

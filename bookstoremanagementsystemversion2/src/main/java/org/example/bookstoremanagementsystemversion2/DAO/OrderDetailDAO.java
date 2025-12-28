package org.example.bookstoremanagementsystemversion2.DAO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import org.example.bookstoremanagementsystemversion2.Model.OrderDetails;
import org.example.bookstoremanagementsystemversion2.Model.Database;

@Repository
public class OrderDetailDAO {
    private List<OrderDetails> orderDetailsList;
    private final Database database;

    public OrderDetailDAO(Database database) {
        this.database = database;
    }

    public void getOrderDetailsById(int orderId) throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        List<OrderDetails> temp = null;
        try {
            String sql = "SELECT orderDetailId, orderId, bookId, quantity, price, subtotal FROM orderdetails WHERE orderId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, orderId);
            rs = stm.executeQuery();
            while (rs.next()) {
                OrderDetails cached = new OrderDetails();
                cached.setOrderDetailId(rs.getInt("orderDetailId"));
                cached.setOrderId(rs.getInt("orderId"));
                cached.setBookId(rs.getInt("bookId"));
                cached.setQuantity(rs.getInt("quantity"));
                cached.setPrice(rs.getDouble("price"));
                // FIX: Consistent BigDecimal handling
                cached.setSubtotal(rs.getBigDecimal("subtotal"));
                if(temp == null)
                    temp = new ArrayList<OrderDetails>();
                temp.add(cached);
            }
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
        orderDetailsList = temp;
    }

    public List<OrderDetails> getOrderDetailsWithBookInfo(int orderId) throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        List<OrderDetails> details = new ArrayList<>();
        try {
            String sql = """
                SELECT od.orderDetailId, od.orderId, od.bookId, od.quantity, od.price, od.subtotal,
                       b.name as bookName, b.publisher as bookPublisher
                FROM orderdetails od 
                JOIN books b ON od.bookId = b.bookId 
                WHERE od.orderId = ?
                ORDER BY od.bookId
                """;
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, orderId);
            rs = stm.executeQuery();
            while (rs.next()) {
                OrderDetails detail = new OrderDetails();
                detail.setOrderDetailId(rs.getInt("orderDetailId"));
                detail.setOrderId(rs.getInt("orderId"));
                detail.setBookId(rs.getInt("bookId"));
                detail.setQuantity(rs.getInt("quantity"));
                detail.setPrice(rs.getDouble("price"));
                // FIX: Consistent BigDecimal handling
                detail.setSubtotal(rs.getBigDecimal("subtotal"));
                detail.setBookName(rs.getString("bookName"));
                detail.setBookPublisher(rs.getString("bookPublisher"));
                details.add(detail);
            }
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
        return details;
    }

    public OrderDetails fetchOrderDetails(Integer orderId, Integer bookId) throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        OrderDetails temp = null;
        try {
            String sql = "SELECT orderDetailId, orderId, bookId, quantity, price, subtotal FROM orderdetails WHERE orderId = ? AND bookId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, orderId);
            stm.setInt(2, bookId);
            rs = stm.executeQuery();
            if (rs.next()) {
                temp = new OrderDetails();
                temp.setOrderDetailId(rs.getInt("orderDetailId"));
                temp.setOrderId(rs.getInt("orderId"));
                temp.setBookId(rs.getInt("bookId"));
                temp.setQuantity(rs.getInt("quantity"));
                temp.setPrice(rs.getDouble("price"));
                // FIX: Consistent BigDecimal handling
                temp.setSubtotal(rs.getBigDecimal("subtotal"));
            }
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
        return temp;
    }

    public boolean createOrderDetails(int orderId, int bookId, int quantity, double price) throws SQLException, Exception {
        Connection con = null;
        PreparedStatement stm = null;
        try {
            if (orderId <= 0 || bookId <= 0 || quantity < 0 || price < 0) {
                throw new Exception("INVALID PARAMETER");
            }
            String sql = "INSERT INTO orderdetails (orderId, bookId, quantity, price) VALUES (?, ?, ?, ?)";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, orderId);
            stm.setInt(2, bookId);
            stm.setInt(3, quantity);
            stm.setDouble(4, price);
            return stm.executeUpdate() > 0;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public boolean clearOrderDetails(int orderId) throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        try {
            String sql = "DELETE FROM orderdetails WHERE orderId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, orderId);
            return stm.executeUpdate() >= 0;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public boolean updateOrderDetails(OrderDetails info) throws Exception {
        Connection con = null;
        PreparedStatement stm = null;
        try {
            getOrderDetailsById(info.getOrderId());
            OrderDetails target = fetchOrderDetails(info.getOrderId(), info.getBookId());
            if (target == null) throw new Exception("Cannot update this order detail");
            if (info.getQuantity() < 0 || info.getPrice() < 0)
                throw new Exception("INVALID PARAMETER");
            if (info.getBookId() > 0) target.setBookId(info.getBookId());
            if (info.getQuantity() != target.getQuantity()) target.setQuantity(info.getQuantity());
            if (!info.getPrice().equals(target.getPrice())) target.setPrice(info.getPrice());
            
            String sql = "UPDATE orderdetails SET quantity = ?, price = ? WHERE orderId = ? AND bookId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, target.getQuantity());
            stm.setDouble(2, target.getPrice());
            stm.setInt(3, target.getOrderId());
            stm.setInt(4, target.getBookId());
            return stm.executeUpdate() > 0;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public void fetchOrderDetails(int orderId) throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            orderDetailsList = new ArrayList<>();
            String sql = "SELECT orderDetailId, orderId, bookId, quantity, price, subtotal FROM orderdetails WHERE orderId = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, orderId);
            rs = stm.executeQuery();
            while (rs.next()) {
                OrderDetails orderDetail = new OrderDetails();
                orderDetail.setOrderDetailId(rs.getInt("orderDetailId"));
                orderDetail.setOrderId(rs.getInt("orderId"));
                orderDetail.setBookId(rs.getInt("bookId"));
                orderDetail.setQuantity(rs.getInt("quantity"));
                orderDetail.setPrice(rs.getDouble("price"));
                // FIX: Consistent BigDecimal handling
                orderDetail.setSubtotal(rs.getBigDecimal("subtotal"));
                orderDetailsList.add(orderDetail);
            }
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public boolean createOrderDetail(OrderDetails info) throws SQLException, Exception {
        Connection con = null;
        PreparedStatement stm = null;
        try {
            if (info.getOrderId() == null || info.getBookId() == null || 
                info.getQuantity() == null || info.getPrice() == null) {
                throw new Exception("INVALID PARAMETER");
            }
            String sql = "INSERT INTO orderdetails (orderId, bookId, quantity, price) VALUES (?, ?, ?, ?)";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setInt(1, info.getOrderId());
            stm.setInt(2, info.getBookId());
            stm.setInt(3, info.getQuantity());
            stm.setDouble(4, info.getPrice());
            return stm.executeUpdate() > 0;
        } finally {
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public List<OrderDetails> getOrderDetailsList() {
        return orderDetailsList;
    }
}


package org.example.bookstoremanagementsystemversion2.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.bookstoremanagementsystemversion2.Model.Book;
import org.example.bookstoremanagementsystemversion2.Model.Database;
import org.example.bookstoremanagementsystemversion2.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAO{
    private List<Book> books;
    private final Database database;
    private List<User> usersList;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDAO(Database database, JdbcTemplate jdbcTemplate) {
        this.database = database;
        this.jdbcTemplate = jdbcTemplate;
    }

    private User getByUsername(String name) throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        User temp = null;
        try {
            String sql = "SELECT username, firstName, lastName, phoneNumber, password FROM users WHERE username = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, name);
            rs = stm.executeQuery();
            if (rs.next()) {
                temp = new User();
                temp.setUsername(rs.getString("username"));
                temp.setFirstName(rs.getString("firstName"));
                temp.setLastName(rs.getString("lastName"));
                temp.setPhoneNumber(rs.getString("phoneNumber"));
                temp.setPassword("*********"); // handle securely if needed
            }
        } finally{
            if( rs != null ){
                rs.close();
            }
            if( stm != null ){
                stm.close();
            }
            if( con != null ){
                con.close();
            }
        }
        return temp;
    }

    public User login(String username, String password) throws SQLException{
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        User temp = null;
        try{
            String sql = "SELECT username, firstName, lastName, phoneNumber, role FROM users WHERE username = ? AND password = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, username);
            stm.setString(2, password);
            rs = stm.executeQuery();
            if (rs.next()) {
                temp = new User();
                temp.setUsername(rs.getString("username"));
                temp.setFirstName(rs.getString("firstName"));
                temp.setLastName(rs.getString("lastName"));
                temp.setPhoneNumber(rs.getString("phoneNumber"));
                temp.setRole(rs.getString("role")); // Include role
                temp.setPassword("*********"); // handle securely if needed
            }
        } finally{
            if( rs != null ){
                rs.close();
            }
            if( stm != null ){
                stm.close();
            }
            if( con != null ){
                con.close();
            }
        }
        return temp;
    }

    public boolean createUser(String username, String firstName, String lastName, String phoneNumber, String password) throws SQLException, Exception {
        return createUser(username, firstName, lastName, phoneNumber, password, "customer");
    }

    public boolean createUser(String username, String firstName, String lastName, String phoneNumber, String password, String role) throws SQLException, Exception {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try{
            // Basic validation
            if (username == null || username.isEmpty())
                throw new Exception("INVALID PARAMETER");
            if(firstName == null || firstName.isEmpty())
                throw new Exception("INVALID PARAMETER");
            if(lastName == null || lastName.isEmpty())
                throw new Exception("INVALID PARAMETER");
            if( phoneNumber == null || phoneNumber.isEmpty())
                phoneNumber = "";
            if( password == null || password.isEmpty()) 
                throw new Exception("INVALID PARAMETER");
            String sql = "INSERT INTO users (username, firstName, lastName, phoneNumber, password, role) VALUES (?, ?, ?, ?, ?, ?)";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, username);
            stm.setString(2, firstName);
            stm.setString(3, lastName);
            stm.setString(4, phoneNumber);
            stm.setString(5, password); // You should hash this in real applications!
            stm.setString(6, role);

            return stm.executeUpdate() > 0;
        } finally{
            if( rs != null ){
                rs.close();
            }
            if (stm != null) {
                stm.close();
            }
            if (con != null) {
                con.close();
            }
        }
    }

    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, firstName, lastName, phoneNumber, password, role) VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, 
                user.getUsername(),
                user.getFirstName(), 
                user.getLastName(),
                user.getPhoneNumber(),
                user.getPassword(),
                user.getRole()
            );
            
            return rowsAffected > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User info) throws Exception {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            // Get existing user
            User target = getByUsername(info.getUsername());
            if (target == null) {
                throw new Exception("Cannot update this user");
            }
            // Update fields if valid and different
            if (info.getFirstName() != null && info.getFirstName().isEmpty() == false) {
                target.setFirstName(info.getFirstName());
            }
            if (info.getLastName() != null && info.getLastName().isEmpty() == false) {
                target.setLastName(info.getLastName());
            }
            if (info.getPhoneNumber() != null && info.getPhoneNumber().isEmpty() == false) {
                target.setPhoneNumber(info.getPhoneNumber());
            }
            if (info.getPassword() != null && info.getPassword().isEmpty() == false) {
                target.setPassword(info.getPassword()); // hash this in production
            }
            // Build and execute update
            String sql = "UPDATE users SET firstName = ?, lastName = ?, phoneNumber = ?, password = ? WHERE username = ?";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            stm.setString(1, target.getFirstName());
            stm.setString(2, target.getLastName());
            stm.setString(3, target.getPhoneNumber());
            stm.setString(4, target.getPassword());
            stm.setString(5, target.getUsername());

            int result = stm.executeUpdate();
            return result > 0;

        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public List<Book> getBooks() {
        return books;
    }

    public void getAllUsers() throws SQLException {
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            usersList = new ArrayList<>();
            String sql = "SELECT username, firstName, lastName, phoneNumber, role FROM users ORDER BY role, username";
            con = database.getConnection();
            stm = con.prepareStatement(sql);
            rs = stm.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setPhoneNumber(rs.getString("phoneNumber"));
                user.setRole(rs.getString("role"));
                usersList.add(user);
            }
        } finally {
            if (rs != null) rs.close();
            if (stm != null) stm.close();
            if (con != null) con.close();
        }
    }

    public List<User> getUsersList() {
        return usersList;
    }
}



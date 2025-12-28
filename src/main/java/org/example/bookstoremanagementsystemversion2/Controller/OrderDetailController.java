package org.example.bookstoremanagementsystemversion2.Controller;

import java.sql.SQLException;
import java.util.List;

import org.example.bookstoremanagementsystemversion2.DAO.OrderDetailDAO;
import org.example.bookstoremanagementsystemversion2.Model.OrderDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// REST Controller for managing employees
@RestController
@RequestMapping("/orderdetail")
public class OrderDetailController {

    @Autowired
    private OrderDetailDAO orderDetailDAO;

    // GET endpoint to fetch all employees
    @GetMapping({"", "/"})
    public List<OrderDetails> getOrderDetails(@RequestParam int orderId) {
        try{
            orderDetailDAO.getOrderDetailsById(orderId);
            return orderDetailDAO.getOrderDetailsList();
        } catch(SQLException ex) {
            System.err.println("GetProductServlet _ SQL: " + ex.getMessage());
            ex.printStackTrace();
        } catch(Exception ex){
            System.err.println("getBooks _ Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
        return null;
    }

    @PostMapping({"", "/"})
    public boolean addOrderDetails(@RequestBody OrderDetails info) {
        try{
            return orderDetailDAO.createOrderDetails(info.getOrderId(), info.getBookId(), info.getQuantity(), info.getPrice());
        } catch(SQLException ex) {
            System.err.println("GetProductServlet _ SQL: " + ex.getMessage());
            ex.printStackTrace();
        } catch(Exception ex){
            System.err.println("getBooks _ Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
        return false;
    }

    @PutMapping({"", "/"})
    public boolean updateOrderDetails(@RequestBody OrderDetails info) {
        try{
            boolean result = orderDetailDAO.updateOrderDetails(info);
            return result;
        } catch(SQLException ex) {
            System.err.println("GetProductServlet _ SQL: " + ex.getMessage());
            ex.printStackTrace();
        } catch(Exception ex){
            System.err.println("getBooks _ Exception: " + ex.getMessage() );
            ex.printStackTrace();
        }
        return false;
    }
}

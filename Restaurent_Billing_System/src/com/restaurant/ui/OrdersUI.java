package com.restaurant.ui;
import com.restaurent.db.DBConnection; 
import javax.swing.*;
import java.sql.*;

public class OrdersUI extends JFrame {

    public OrdersUI() {

        setTitle("Orders");
        setSize(400, 300);

        JTextArea area = new JTextArea();
        add(new JScrollPane(area));

        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM orders");

            StringBuilder sb = new StringBuilder();

            while (rs.next()) {
                sb.append("ID: ").append(rs.getInt("id"))
                  .append(" | ₹").append(rs.getDouble("final_total"))
                  .append(" | ").append(rs.getDate("order_date"))
                  .append("\n");
            }

            area.setText(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        setVisible(true);
    }
}
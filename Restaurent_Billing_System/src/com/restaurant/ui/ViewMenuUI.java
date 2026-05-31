package com.restaurant.ui;
import java.sql.*;
import com.restaurent.db.DBConnection;
import javax.swing.*;
import java.sql.*;
import com.restaurent.db.DBConnection;

public class ViewMenuUI extends JPanel {

    public ViewMenuUI() {

        JTextArea area = new JTextArea();
        area.setEditable(false);

        add(new JScrollPane(area));

        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM menu");

            StringBuilder sb = new StringBuilder("MENU:\n\n");

            while (rs.next()) {
                sb.append(rs.getString("name"))
                  .append(" - ₹")
                  .append(rs.getDouble("price"))
                  .append("\n");
            }

            area.setText(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
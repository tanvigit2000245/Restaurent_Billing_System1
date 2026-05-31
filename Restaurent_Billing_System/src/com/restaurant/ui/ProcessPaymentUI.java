package com.restaurant.ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.restaurent.db.DBConnection;

public class ProcessPaymentUI extends JFrame {

    JTextField tableField;
    JComboBox<String> methodBox;

    public ProcessPaymentUI() {

        setTitle("Process Payment");
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tableField = new JTextField();
        methodBox = new JComboBox<>(new String[]{"Cash", "UPI", "Card"});

        JButton payBtn = new JButton("Confirm Payment");

        add(new JLabel("Table Number:"));
        add(tableField);

        add(new JLabel("Payment Method:"));
        add(methodBox);

        add(new JLabel());
        add(payBtn);

        payBtn.addActionListener(e -> processPayment());

        setVisible(true);
    }

    void processPayment() {
        try {
            int tableNo = Integer.parseInt(tableField.getText().trim());
            String method = methodBox.getSelectedItem().toString();

            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE orders SET payment_status='PAID', payment_method=? " +
                    "WHERE table_no=? AND payment_status='PENDING'"
            );

            ps.setString(1, method);
            ps.setInt(2, tableNo);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                PreparedStatement freeTable = con.prepareStatement(
                        "UPDATE tables SET status='FREE' WHERE table_no=?"
                );

                freeTable.setInt(1, tableNo);
                freeTable.executeUpdate();

                JOptionPane.showMessageDialog(this,
                        "Payment successful!\nTable " + tableNo + " is now FREE.");

            } else {
                JOptionPane.showMessageDialog(this, "No pending bill found for this table.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Payment failed.");
        }
    }
}
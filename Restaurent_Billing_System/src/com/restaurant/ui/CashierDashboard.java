package com.restaurant.ui;

import javax.swing.*;
import java.awt.*;

public class CashierDashboard extends JFrame {

    public CashierDashboard() {

        setTitle("Cashier Dashboard");
        setSize(600, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("💳 Cashier Panel", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setBorder(BorderFactory.createEmptyBorder(25, 0, 20, 0));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        centerPanel.setBackground(new Color(245, 247, 250));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton generateBillBtn = createButton("🧾 Generate Bill");
       
        JButton historyBtn = createButton("📜 Order History");
        JButton logoutBtn = createButton("🚪 Logout");

        centerPanel.add(generateBillBtn);
       
        centerPanel.add(historyBtn);
        centerPanel.add(logoutBtn);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);

        generateBillBtn.addActionListener(e -> new GenerateBillPanel());
       
        historyBtn.addActionListener(e -> new CashierOrderHistoryPanel());

        logoutBtn.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        setVisible(true);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
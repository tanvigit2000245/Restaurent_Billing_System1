package com.restaurant.ui;

import com.restaurent.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MenuManagementUI extends JPanel {

    private JPanel cardPanel;

    private final Color maroon = new Color(122, 31, 46);
    private final Color bg = new Color(245, 243, 240);
    private final Color border = new Color(220, 215, 210);

    public MenuManagementUI() {
        setLayout(new BorderLayout());
        setBackground(bg);

        add(headerPanel(), BorderLayout.NORTH);

        cardPanel = new JPanel(new GridLayout(0, 3, 22, 22));
        cardPanel.setBackground(bg);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        JScrollPane scroll = new JScrollPane(cardPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
        add(bottomPanel(), BorderLayout.SOUTH);

        loadMenu();
    }

    private JPanel headerPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(bg);
        header.setBorder(BorderFactory.createEmptyBorder(25, 30, 15, 30));

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleBox.setOpaque(false);

     JLabel icon = new JLabel("📋");
     icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        icon.setForeground(maroon);

        JLabel title = new JLabel("Menu Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(35, 35, 35));

        titleBox.add(icon);
        titleBox.add(title);

        header.add(titleBox, BorderLayout.WEST);
        return header;
    }

    private JPanel bottomPanel() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(bg);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 30, 18, 30));

        JButton addBtn = createButton("+ Add Item", new Color(76, 175, 80));
        JButton refreshBtn = createButton("Refresh", new Color(33, 150, 243));

        addBtn.addActionListener(e -> addItem());
        refreshBtn.addActionListener(e -> loadMenu());

        btnPanel.add(addBtn);
        btnPanel.add(refreshBtn);

        return btnPanel;
    }

    private JPanel createCard(String name, double price) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel imgLabel = new JLabel();
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imgLabel.setPreferredSize(new Dimension(150, 105));
        imgLabel.setMaximumSize(new Dimension(150, 105));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);

        String path = "images/" + name.toLowerCase() + ".png";
        ImageIcon icon = new ImageIcon(path);

        if (icon.getIconWidth() > 0) {
            Image img = icon.getImage().getScaledInstance(130, 95, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(img));
        } else {
            imgLabel.setText("No Image");
            imgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            imgLabel.setForeground(new Color(120, 120, 120));
        }

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(new Color(35, 35, 35));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel priceLabel = new JLabel("₹ " + String.format("%.2f", price));
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        priceLabel.setForeground(new Color(80, 80, 80));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton deleteBtn = createButton("Delete", new Color(244, 67, 54));
        deleteBtn.setPreferredSize(new Dimension(120, 36));
        deleteBtn.setMaximumSize(new Dimension(120, 36));
        deleteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteBtn.addActionListener(e -> deleteItem(name));

        card.add(imgLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(priceLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(deleteBtn);

        return card;
    }

    private void loadMenu() {
        try {
            cardPanel.removeAll();

            Connection con = DBConnection.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM menu");

            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");
                cardPanel.add(createCard(name, price));
            }

            cardPanel.revalidate();
            cardPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading menu items.");
        }
    }

    private void addItem() {
        try {
            JTextField nameField = new JTextField();
            JTextField priceField = new JTextField();

            Object[] message = {
                    "Item Name:", nameField,
                    "Price:", priceField
            };

            int option = JOptionPane.showConfirmDialog(
                    this,
                    message,
                    "Add Menu Item",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Item name cannot be empty.");
                    return;
                }

                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO menu(name, price) VALUES (?, ?)"
                );

                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.executeUpdate();

                loadMenu();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input.");
        }
    }

    private void deleteItem(String name) {
        try {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Delete " + name + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) return;

            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM menu WHERE name=?"
            );

            ps.setString(1, name);
            ps.executeUpdate();

            loadMenu();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting item.");
        }
    }

    private JButton createButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
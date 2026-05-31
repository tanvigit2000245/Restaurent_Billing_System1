package com.restaurant.ui;

import com.restaurent.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class WaiterOrderPanel extends JPanel {

    private JPanel itemsPanel;
    private JLabel totalLabel;
    private JComboBox<Integer> tableCombo;
    private JTextField searchField;
    private java.util.List<JPanel> itemCards = new ArrayList<>();
    private java.util.List<String> itemNames = new ArrayList<>();
    
    
    private double total = 0;
    private Map<String, Integer> qtyMap = new HashMap<>();
    private Map<String, Double> priceMap = new HashMap<>();

    private final Color bg = new Color(245, 243, 240);
    private final Color maroon = new Color(122, 31, 46);

    public WaiterOrderPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(bg);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        add(topPanel(), BorderLayout.NORTH);
        add(menuScrollPanel(), BorderLayout.CENTER);
        add(bottomPanel(), BorderLayout.SOUTH);

        loadTables();
        loadMenuItems();
    }

    private JPanel topPanel() {

        JPanel top = new JPanel(new BorderLayout(20, 0));
        top.setBackground(bg);

        JLabel title = new JLabel("Create Order");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        right.setBackground(bg);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(220, 38));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        tableCombo = new JComboBox<>();
        tableCombo.setPreferredSize(new Dimension(130, 38));

        right.add(new JLabel("Search:"));
        right.add(searchField);

        right.add(new JLabel("Table No:"));
        right.add(tableCombo);

        top.add(title, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                searchItems();
            }
        });

        return top;
    }

    private JScrollPane menuScrollPanel() {
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(bg);

        JScrollPane scroll = new JScrollPane(itemsPanel);
        scroll.setBorder(BorderFactory.createTitledBorder("Menu Items"));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        return scroll;
    }

    private JPanel bottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        totalLabel = new JLabel("Total: ₹0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        totalLabel.setForeground(maroon);

        JButton saveBtn = button("Save Order");
        saveBtn.addActionListener(e -> saveOrder());

        bottom.add(totalLabel, BorderLayout.WEST);
        bottom.add(saveBtn, BorderLayout.EAST);

        return bottom;
    }

    private void loadTables() {
        tableCombo.removeAllItems();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT table_no FROM tables WHERE status='FREE' ORDER BY table_no"
             );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableCombo.addItem(rs.getInt("table_no"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to load tables.");
        }
    }

    private void loadMenuItems() {
        itemsPanel.removeAll();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT name, price FROM menu ORDER BY name"
             );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                double price = rs.getDouble("price");

                qtyMap.put(name, 0);
                priceMap.put(name, price);

                JPanel row = itemRow(name, price);

                itemCards.add(row);
                itemNames.add(name.toLowerCase());

                itemsPanel.add(row);
                itemsPanel.add(Box.createVerticalStrut(12));
            }

            itemsPanel.revalidate();
            itemsPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to load menu.");
        }
    }

    private JPanel itemRow(String name, double price) {
        JPanel row = new JPanel(new BorderLayout(20, 0));
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 220, 215)),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel itemLabel = new JLabel(name + "   ₹" + money(price));
        itemLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        qtyPanel.setOpaque(false);

        JButton minus = smallButton("-");
        JLabel qtyLabel = new JLabel("0");
        qtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton plus = smallButton("+");

        minus.addActionListener(e -> {
            int q = qtyMap.get(name);
            if (q > 0) {
                qtyMap.put(name, q - 1);
                qtyLabel.setText(String.valueOf(q - 1));
                calculateTotal();
            }
        });

        plus.addActionListener(e -> {
            int q = qtyMap.get(name);
            qtyMap.put(name, q + 1);
            qtyLabel.setText(String.valueOf(q + 1));
            calculateTotal();
        });

        qtyPanel.add(minus);
        qtyPanel.add(qtyLabel);
        qtyPanel.add(plus);

        row.add(itemLabel, BorderLayout.WEST);
        row.add(qtyPanel, BorderLayout.EAST);

        return row;
    }

    private void calculateTotal() {
        total = 0;

        for (String item : qtyMap.keySet()) {
            total += qtyMap.get(item) * priceMap.get(item);
        }

        totalLabel.setText("Total: ₹" + money(total));
    }

    private void saveOrder() {
        try {
            if (tableCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "No free table available.");
                return;
            }

            if (total <= 0) {
                JOptionPane.showMessageDialog(this, "Please select at least one item.");
                return;
            }

            int tableNo = (int) tableCombo.getSelectedItem();

            StringBuilder details = new StringBuilder();

            for (String item : qtyMap.keySet()) {
                int qty = qtyMap.get(item);
                if (qty > 0) {
                    details.append(item).append(" x").append(qty).append(", ");
                }
            }

            try (Connection con = DBConnection.getConnection()) {

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO orders(table_no, order_details, final_total, payment_status) " +
                                "VALUES (?, ?, ?, 'PENDING')"
                );

                ps.setInt(1, tableNo);
                ps.setString(2, details.toString());
                ps.setDouble(3, total);
                ps.executeUpdate();

                PreparedStatement tablePs = con.prepareStatement(
                        "UPDATE tables SET status='OCCUPIED' WHERE table_no=?"
                );

                tablePs.setInt(1, tableNo);
                tablePs.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Order saved successfully.");

            resetOrder();
            loadTables();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to save order.");
        }
    }

    private void resetOrder() {
        total = 0;
        qtyMap.clear();
        priceMap.clear();
        totalLabel.setText("Total: ₹0.00");
        loadMenuItems();
    }

    private JButton button(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(maroon);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(160, 42));
        return btn;
    }

    private JButton smallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setPreferredSize(new Dimension(48, 34));
        btn.setFocusPainted(false);
        return btn;
    }

    private String money(double value) {
        return String.format("%.2f", value);
    }
    private void searchItems() {

        String text = searchField.getText().trim().toLowerCase();

        itemsPanel.removeAll();

        java.util.List<JPanel> matched = new ArrayList<>();
        java.util.List<JPanel> others = new ArrayList<>();

        for (int i = 0; i < itemCards.size(); i++) {

            JPanel panel = itemCards.get(i);
            String name = itemNames.get(i);

            // reset all cards first
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(225, 220, 215)),
                    BorderFactory.createEmptyBorder(14, 18, 14, 18)
            ));

            if (!text.isEmpty() && name.contains(text)) {
                panel.setBackground(new Color(255, 248, 210));
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 170, 0), 2),
                        BorderFactory.createEmptyBorder(14, 18, 14, 18)
                ));
                matched.add(panel);
            } else {
                others.add(panel);
            }
        }

        if (text.isEmpty()) {
            for (JPanel p : itemCards) {
                itemsPanel.add(p);
                itemsPanel.add(Box.createVerticalStrut(12));
            }
        } else {
            for (JPanel p : matched) {
                itemsPanel.add(p);
                itemsPanel.add(Box.createVerticalStrut(12));
            }

            for (JPanel p : others) {
                itemsPanel.add(p);
                itemsPanel.add(Box.createVerticalStrut(12));
            }
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }}
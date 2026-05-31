package com.restaurant.ui;

import com.restaurent.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TableOrdersPanel extends JPanel {

    private final Color maroon = new Color(122, 31, 46);
    private final Color bgPage = new Color(245, 243, 240);
    private final Color border = new Color(220, 215, 210);
    private JPanel grid;
    
    public TableOrdersPanel() {
        setLayout(new BorderLayout());
        setBackground(bgPage);

        // This adds the true Maroon header featuring the Refresh button
        add(header(), BorderLayout.NORTH);
        add(scrollContent(), BorderLayout.CENTER);
    }

    private JPanel header() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(maroon);
        header.setPreferredSize(new Dimension(0, 95));
        header.setBorder(BorderFactory.createEmptyBorder(18, 35, 18, 35));

        JLabel title = new JLabel("Table Orders");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(Color.WHITE);
        refreshBtn.setForeground(maroon);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorderPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        refreshBtn.setPreferredSize(new Dimension(120, 42));

        refreshBtn.addActionListener(e -> loadOrders());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        right.setOpaque(false);
        right.add(refreshBtn);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JScrollPane scrollContent() {
        grid = new JPanel(new GridLayout(0, 2, 20, 20));
        grid.setBackground(bgPage);
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        loadOrders();

        JScrollPane scroll = new JScrollPane(
                grid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        scroll.setBorder(null);
        scroll.getViewport().setBackground(bgPage);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        return scroll;
    }
    
    private void loadOrders() {
        grid.removeAll();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT t.table_no, t.status, o.order_details, o.final_total " +
                     "FROM tables t " +
                     "LEFT JOIN orders o ON t.table_no = o.table_no " +
                     "AND o.payment_status='PENDING' " +
                     "AND o.id = (" +
                     "   SELECT MAX(id) FROM orders " +
                     "   WHERE table_no=t.table_no AND payment_status='PENDING'" +
                     ") " +
                     "ORDER BY t.table_no"
             );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                grid.add(buildCard(
                        rs.getInt("table_no"),
                        rs.getString("status"),
                        rs.getString("order_details"),
                        rs.getDouble("final_total")
                ));
            }

            grid.revalidate();
            grid.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to load table orders.");
        }
    }
    
    private JPanel buildCard(int tableNo, String status, String items, double total) {
        boolean occupied = "OCCUPIED".equalsIgnoreCase(status);
        boolean hasOrder = items != null && !items.trim().isEmpty();

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createLineBorder(new Color(225,225,225), 1),
                 BorderFactory.createEmptyBorder(22, 24, 22, 24)
        ));
        card.setPreferredSize(new Dimension(420, 160));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel tableLbl = new JLabel("Table " + tableNo);
        tableLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        tableLbl.setForeground(new Color(35, 35, 35));

        JLabel statusLbl = new JLabel(occupied ? "Occupied" : "Available", JLabel.CENTER);
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLbl.setOpaque(true);
        statusLbl.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        statusLbl.setBackground(occupied ? new Color(255, 235, 238) : new Color(232, 245, 233));
        statusLbl.setForeground(occupied ? maroon : new Color(46, 125, 50));

        top.add(tableLbl, BorderLayout.WEST);
        top.add(statusLbl, BorderLayout.EAST);

        JLabel itemLbl = new JLabel(
                "<html><div style='width:260px;'>" +
                        (hasOrder ? items : "No active order") +
                        "</div></html>"
        );
        itemLbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        itemLbl.setForeground(new Color(95, 95, 95));

        JLabel totalLbl = new JLabel(hasOrder ? "₹" + String.format("%.2f", total) : "₹0.00");
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLbl.setForeground(hasOrder ? maroon : new Color(130, 130, 130));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        bottom.add(itemLbl, BorderLayout.CENTER);
        bottom.add(totalLbl, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        
        return card;
    }
}
package com.restaurant.ui;

import com.restaurent.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CashierOrderHistoryPanel extends JPanel {

    private JComboBox<String> dateCombo;
    private DefaultTableModel paidModel, pendingModel;

    private final Color bg = new Color(245, 243, 240);
    private final Color maroon = new Color(122, 31, 46);

    public CashierOrderHistoryPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(bg);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        add(topPanel(), BorderLayout.NORTH);
        add(tablesPanel(), BorderLayout.CENTER);

        loadPaymentDates();
        loadPendingOrders();
    }

    private JPanel topPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);

        JLabel title = new JLabel("Order History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        filterPanel.setBackground(bg);

        dateCombo = new JComboBox<>();
        dateCombo.setPreferredSize(new Dimension(190, 38));

        JButton refreshBtn = button("Refresh");

        filterPanel.add(new JLabel("Payment Date:"));
        filterPanel.add(dateCombo);
        filterPanel.add(refreshBtn);

        panel.add(title, BorderLayout.WEST);
        panel.add(filterPanel, BorderLayout.EAST);

        dateCombo.addActionListener(e -> loadPaidOrdersByDate());

        refreshBtn.addActionListener(e -> {
            loadPaymentDates();
            loadPendingOrders();
        });

        return panel;
    }

    private JPanel tablesPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 18));
        panel.setBackground(bg);

        paidModel = createModel();
        pendingModel = createModel();

        panel.add(tableBox("Paid Orders", paidModel));
        panel.add(tableBox("Pending Orders", pendingModel));

        return panel;
    }

    private DefaultTableModel createModel() {
        return new DefaultTableModel(
                new String[]{
                        "Table No", "Items", "Food Total", "GST",
                        "Tax", "Discount", "Grand Total", "Status", "Method"
                },
                0
        );
    }

    private JScrollPane tableBox(String title, DefaultTableModel model) {
        JTable table = new JTable(model);

        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(420);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);
        table.getColumnModel().getColumn(7).setPreferredWidth(100);
        table.getColumnModel().getColumn(8).setPreferredWidth(110);

        JScrollPane scroll = new JScrollPane(
                table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        scroll.setBorder(BorderFactory.createTitledBorder(title));
        return scroll;
    }

    private void loadPaymentDates() {
        dateCombo.removeAllItems();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT DISTINCT DATE(Order_date) AS payment_date " +
                             "FROM orders " +
                             "WHERE payment_status='PAID' " +
                             "AND Order_date IS NOT NULL " +
                             "ORDER BY payment_date DESC"
             );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                dateCombo.addItem(rs.getString("payment_date"));
            }

            if (dateCombo.getItemCount() > 0) {
                dateCombo.setSelectedIndex(0);
                loadPaidOrdersByDate();
            } else {
                paidModel.setRowCount(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to load payment dates.");
        }
    }

    private void loadPaidOrdersByDate() {
        if (dateCombo.getSelectedItem() == null) return;

        paidModel.setRowCount(0);
        String selectedDate = dateCombo.getSelectedItem().toString();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT table_no, order_details, final_total, gst, other_tax, " +
                             "discount, grand_total, payment_status, payment_method " +
                             "FROM orders " +
                             "WHERE payment_status='PAID' " +
                             "AND DATE(Order_date)=? " +
                             "ORDER BY id DESC"
             )) {

            ps.setString(1, selectedDate);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                paidModel.addRow(rowData(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to load paid orders.");
        }
    }

    private void loadPendingOrders() {
        pendingModel.setRowCount(0);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT table_no, order_details, final_total, gst, other_tax, " +
                             "discount, grand_total, payment_status, payment_method " +
                             "FROM orders " +
                             "WHERE payment_status='PENDING' " +
                             "ORDER BY id DESC"
             );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                pendingModel.addRow(rowData(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to load pending orders.");
        }
    }

    private Object[] rowData(ResultSet rs) throws SQLException {
        return new Object[]{
                rs.getInt("table_no"),
                cleanItems(rs.getString("order_details")),
                "₹" + money(rs.getDouble("final_total")),
                "₹" + money(rs.getDouble("gst")),
                "₹" + money(rs.getDouble("other_tax")),
                "₹" + money(rs.getDouble("discount")),
                "₹" + money(rs.getDouble("grand_total")),
                rs.getString("payment_status"),
                safeText(rs.getString("payment_method"))
        };
    }

    private String cleanItems(String items) {
        if (items == null) return "";
        return items.trim();
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }

    private JButton button(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(maroon);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(120, 38));
        return btn;
    }

    private String money(double value) {
        return String.format("%.2f", value);
    }
}
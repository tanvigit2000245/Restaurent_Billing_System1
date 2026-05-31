package com.restaurant.ui;

import com.restaurent.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ReportsUI extends JPanel {

    private JLabel salesLbl, ordersLbl, pendingLbl, paidLbl;
    private DefaultTableModel topItemsModel, paymentModel;

    private final Color bg = new Color(245, 243, 240);
    private final Color maroon = new Color(122, 31, 46);

    public ReportsUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(bg);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        add(topCards(), BorderLayout.NORTH);
        add(centerTables(), BorderLayout.CENTER);

        loadReports();
        new Timer(3000, e -> loadReports()).start();
    }

    private JPanel topCards() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 18, 0));
        panel.setBackground(bg);

        salesLbl = card("₹0.00", "Today's Sales");
        ordersLbl = card("0", "Orders Today");
        pendingLbl = card("0", "Pending Bills");
        paidLbl = card("0", "Paid Bills");

        panel.add(wrapCard(salesLbl));
        panel.add(wrapCard(ordersLbl));
        panel.add(wrapCard(pendingLbl));
        panel.add(wrapCard(paidLbl));

        return panel;
    }

    private JLabel card(String value, String title) {
        JLabel lbl = new JLabel(
                "<html><center><b style='font-size:24px;'>" + value +
                "</b><br><span style='font-size:12px;'>" + title +
                "</span></center></html>",
                SwingConstants.CENTER
        );
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lbl.setForeground(new Color(35, 35, 35));
        return lbl;
    }

    private JPanel wrapCard(JLabel label) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 220, 215)),
                BorderFactory.createEmptyBorder(22, 18, 22, 18)
        ));
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    private JPanel centerTables() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(bg);

        topItemsModel = new DefaultTableModel(new String[]{"Item", "Qty Sold"}, 0);
        paymentModel = new DefaultTableModel(new String[]{"Payment Method", "Count"}, 0);

        panel.add(tableBox("Top Selling Items", topItemsModel));
        panel.add(tableBox("Payment Report", paymentModel));

        return panel;
    }

    private JPanel tableBox(String title, DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));

        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createTitledBorder(title));
        box.add(new JScrollPane(table), BorderLayout.CENTER);

        return box;
    }

    private void loadReports() {
        loadSummary();
        loadTopItems();
        loadPaymentReport();
    }

    private void loadSummary() {
        try (Connection con = DBConnection.getConnection()) {

            salesLbl.setText(cardText("₹" + money(getDouble(con,
                    "SELECT COALESCE(SUM(grand_total),0) FROM orders WHERE payment_status='PAID'")), "Total Sales"));

            ordersLbl.setText(cardText(String.valueOf(getInt(con,
                    "SELECT COUNT(*) FROM orders")), "Total Orders"));

            pendingLbl.setText(cardText(String.valueOf(getInt(con,
                    "SELECT COUNT(*) FROM orders WHERE payment_status='PENDING'")), "Pending Bills"));

            paidLbl.setText(cardText(String.valueOf(getInt(con,
                    "SELECT COUNT(*) FROM orders WHERE payment_status='PAID'")), "Paid Bills"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTopItems() {
        topItemsModel.setRowCount(0);

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT order_details FROM orders")) {

            java.util.Map<String, Integer> map = new java.util.HashMap<>();

            while (rs.next()) {
                String details = rs.getString("order_details");
                if (details == null) continue;

                details = details.replace(";", ",");

                for (String item : details.split(",")) {
                    item = item.trim();
                    if (item.isEmpty()) continue;

                    int qty = 1;

                    if (item.matches(".*\\sx\\d+$")) {
                        int index = item.lastIndexOf("x");
                        qty = Integer.parseInt(item.substring(index + 1).trim());
                        item = item.substring(0, index).trim();
                    }

                    map.put(item, map.getOrDefault(item, 0) + qty);
                }
            }

            map.entrySet().stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .limit(10)
                    .forEach(e -> topItemsModel.addRow(new Object[]{e.getKey(), e.getValue()}));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPaymentReport() {
        paymentModel.setRowCount(0);

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT payment_method, COUNT(*) total FROM orders " +
                     "WHERE payment_status='PAID' GROUP BY payment_method"
             )) {

            while (rs.next()) {
                paymentModel.addRow(new Object[]{
                        rs.getString("payment_method"),
                        rs.getInt("total")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getInt(Connection con, String sql) throws Exception {
        ResultSet rs = con.createStatement().executeQuery(sql);
        return rs.next() ? rs.getInt(1) : 0;
    }

    private double getDouble(Connection con, String sql) throws Exception {
        ResultSet rs = con.createStatement().executeQuery(sql);
        return rs.next() ? rs.getDouble(1) : 0;
    }

    private String cardText(String value, String title) {
        return "<html><center><b style='font-size:24px;'>" + value +
                "</b><br><span style='font-size:12px;'>" + title +
                "</span></center></html>";
    }

    private String money(double value) {
        return String.format("%.2f", value);
    }
}
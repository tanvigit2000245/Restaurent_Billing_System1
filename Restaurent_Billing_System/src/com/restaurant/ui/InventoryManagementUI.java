package com.restaurant.ui;

import com.restaurent.db.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InventoryManagementUI extends JPanel {

    private DefaultTableModel model;
    private JTable table;

    private final Color bg = new Color(245, 243, 240);
    private final Color maroon = new Color(122, 31, 46);

    public InventoryManagementUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(bg);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        add(topPanel(), BorderLayout.NORTH);
        add(tablePanel(), BorderLayout.CENTER);

        loadInventory();
    }

    private JPanel topPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);

        JLabel title = new JLabel("Inventory Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setBackground(bg);

        JButton addBtn = button("Add Item");
        JButton updateBtn = button("Update Stock");
        JButton deleteBtn = button("Delete");
        JButton refreshBtn = button("Refresh");

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);

        panel.add(title, BorderLayout.WEST);
        panel.add(btnPanel, BorderLayout.EAST);

        addBtn.addActionListener(e -> addItem());
        updateBtn.addActionListener(e -> updateStock());
        deleteBtn.addActionListener(e -> deleteItem());
        refreshBtn.addActionListener(e -> loadInventory());

        return panel;
    }

    private JScrollPane tablePanel() {
        model = new DefaultTableModel(
                new String[]{"ID", "Item Name", "Stock Qty", "Unit", "Min Stock", "Status"},
                0
        );

        table = new JTable(model);
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Inventory Stock"));

        return scroll;
    }

    private void loadInventory() {
        model.setRowCount(0);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT * FROM inventory ORDER BY ingredient_name"
             );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int stock = rs.getInt("stock_qty");
                int min = rs.getInt("min_stock");

                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("ingredient_name"),
                        stock,
                        rs.getString("unit"),
                        min,
                        stock <= min ? "LOW STOCK" : "AVAILABLE"
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to load inventory.");
        }
    }

    private void addItem() {
        JTextField name = new JTextField();
        JTextField stock = new JTextField();
        JTextField unit = new JTextField("pcs");
        JTextField min = new JTextField("5");

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Item Name:"));
        panel.add(name);
        panel.add(new JLabel("Stock Qty:"));
        panel.add(stock);
        panel.add(new JLabel("Unit:"));
        panel.add(unit);
        panel.add(new JLabel("Min Stock:"));
        panel.add(min);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Inventory Item", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "INSERT INTO inventory(ingredient_name, stock_qty, unit, min_stock) VALUES (?, ?, ?, ?)"
                 )) {

                ps.setString(1, name.getText().trim());
                ps.setInt(2, Integer.parseInt(stock.getText().trim()));
                ps.setString(3, unit.getText().trim());
                ps.setInt(4, Integer.parseInt(min.getText().trim()));

                ps.executeUpdate();
                loadInventory();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Invalid input.");
            }
        }
    }

    private void updateStock() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String itemName = model.getValueAt(row, 1).toString();

        String qtyText = JOptionPane.showInputDialog(this, "Enter new stock for " + itemName + ":");

        if (qtyText == null || qtyText.trim().isEmpty()) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE inventory SET stock_qty=? WHERE id=?"
             )) {

            ps.setInt(1, Integer.parseInt(qtyText.trim()));
            ps.setInt(2, id);

            ps.executeUpdate();
            loadInventory();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid stock value.");
        }
    }

    private void deleteItem() {
        int row = table.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an item first.");
            return;
        }

        int id = Integer.parseInt(model.getValueAt(row, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete selected inventory item?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM inventory WHERE id=?"
             )) {

            ps.setInt(1, id);
            ps.executeUpdate();
            loadInventory();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to delete item.");
        }
    }

    private JButton button(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(maroon);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(130, 38));
        return btn;
    }
}
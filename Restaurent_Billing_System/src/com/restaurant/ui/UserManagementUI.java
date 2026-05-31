package com.restaurant.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import com.restaurent.db.DBConnection;

public class UserManagementUI extends JPanel {

    private DefaultTableModel waiterModel, cashierModel;
    private JTable waiterTable, cashierTable;

    private final Color maroon = new Color(122, 31, 46);
    private final Color bg = new Color(245, 243, 240);

    public UserManagementUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(bg);
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        add(topPanel(), BorderLayout.NORTH);

        JPanel tables = new JPanel(new GridLayout(1, 2, 25, 0));
        tables.setBackground(bg);

        waiterModel = new DefaultTableModel(new String[]{"ID", "Username"}, 0);
        cashierModel = new DefaultTableModel(new String[]{"ID", "Username"}, 0);

        waiterTable = createTable(waiterModel);
        cashierTable = createTable(cashierModel);

        tables.add(tableBox("Waiters", waiterTable));
        tables.add(tableBox("Cashiers", cashierTable));

        add(tables, BorderLayout.CENTER);

        loadUsers();
    }

    private JPanel topPanel() {

        JPanel top = new JPanel(new BorderLayout(20, 10));
        top.setBackground(bg);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5));

        // ===== TITLE =====
        JLabel title = new JLabel("👨‍💼 User Management");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 30));
        title.setForeground(new Color(35, 35, 35));

        // ===== BUTTON PANEL =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(bg);

        JButton addBtn = button("+ Add User", new Color(76, 175, 80));
        JButton deleteBtn = button("Delete User", new Color(244, 67, 54));
        JButton refreshBtn = button("Refresh", new Color(33, 150, 243));

        // responsive button size
        Dimension btnSize = new Dimension(140, 40);

        addBtn.setPreferredSize(btnSize);
        deleteBtn.setPreferredSize(btnSize);
        refreshBtn.setPreferredSize(btnSize);

        buttons.add(addBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);

        // ===== LEFT + RIGHT =====
        top.add(title, BorderLayout.WEST);
        top.add(buttons, BorderLayout.EAST);

        // ===== ACTIONS =====
        addBtn.addActionListener(e -> addUser());
        deleteBtn.addActionListener(e -> deleteUser());
        refreshBtn.addActionListener(e -> loadUsers());

        return top;
    }
    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        return table;
    }

    private JPanel tableBox(String title, JTable table) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createTitledBorder(title));

        JScrollPane scroll = new JScrollPane(table);
        box.add(scroll, BorderLayout.CENTER);

        return box;
    }

    private JButton button(String text, Color color) {

        JButton btn = new JButton(text);

        btn.setBackground(color);
        btn.setForeground(Color.WHITE);

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);

        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));

        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private void loadUsers() {
        waiterModel.setRowCount(0);
        cashierModel.setRowCount(0);

        try {
            Connection con = DBConnection.getConnection();
            ResultSet rs = con.createStatement().executeQuery(
                    "SELECT id, username, role FROM users WHERE role IN ('CASHIER','WAITER')"
            );

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("username")
                };

                if (rs.getString("role").equalsIgnoreCase("WAITER")) {
                    waiterModel.addRow(row);
                } else if (rs.getString("role").equalsIgnoreCase("CASHIER")) {
                    cashierModel.addRow(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addUser() {
        JTextField username = new JTextField();
        JTextField password = new JTextField();
        JComboBox<String> role = new JComboBox<>(new String[]{"CASHIER", "WAITER"});

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.add(new JLabel("Username:"));
        panel.add(username);
        panel.add(new JLabel("Password:"));
        panel.add(password);
        panel.add(new JLabel("Role:"));
        panel.add(role);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add User", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Connection con = DBConnection.getConnection();
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users(username, password, role) VALUES (?, ?, ?)"
                );

                ps.setString(1, username.getText().trim());
                ps.setString(2, password.getText().trim());
                ps.setString(3, role.getSelectedItem().toString());

                ps.executeUpdate();
                loadUsers();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteUser() {
        String username = JOptionPane.showInputDialog(this, "Enter username to delete:");

        if (username == null || username.trim().isEmpty()) return;

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM users WHERE username=? AND role!='ADMIN'"
            );

            ps.setString(1, username.trim());
            ps.executeUpdate();

            loadUsers();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
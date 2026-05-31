package com.restaurant.ui;

import com.restaurent.db.DBConnection;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JComboBox<String> roleCombo;

    public LoginFrame() {
        setTitle("RestroFlow – Restaurant Billing System");
        setSize(420, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        Color maroon = new Color(122, 31, 46);
        Color borderColor = new Color(210, 205, 200);
        Color textMuted = new Color(120, 115, 110);

        Border fieldBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        );

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(32, 32, 32, 32)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JLabel icon = new JLabel("🍽️", JLabel.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 30));
        gbc.insets = new Insets(0, 0, 8, 0);
        card.add(icon, gbc);

        JLabel title = new JLabel("RestroFlow", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(title, gbc);

        JLabel subtitle = new JLabel("Restaurant Billing System", JLabel.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);
        gbc.insets = new Insets(0, 0, 22, 0);
        card.add(subtitle, gbc);

        JTextField userField = new JTextField(18);
        JPasswordField passField = new JPasswordField(18);
        roleCombo = new JComboBox<>(new String[]{"ADMIN", "CASHIER", "WAITER"});

        addInputGroup(card, "Username", userField, fieldBorder, textMuted, gbc);
        addInputGroup(card, "Password", passField, fieldBorder, textMuted, gbc);
        addInputGroup(card, "Select Role", roleCombo, null, textMuted, gbc);

        roleCombo.setBackground(new Color(250, 248, 246));
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton loginBtn = new JButton("Sign in");
        loginBtn.setBackground(maroon);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginBtn.setPreferredSize(new Dimension(280, 42));
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.insets = new Insets(10, 0, 16, 0);
        card.add(loginBtn, gbc);

        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(new Color(245, 243, 240));
        page.add(card);

        add(page);

        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            attemptLogin(username, password);
        });

        setVisible(true);
    }

    private void addInputGroup(JPanel panel, String label, JComponent field,
                               Border border, Color labelColor, GridBagConstraints gbc) {

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(labelColor);

        gbc.insets = new Insets(6, 0, 4, 0);
        panel.add(lbl, gbc);

        if (border != null) {
            field.setBorder(border);
        }

        field.setBackground(new Color(250, 248, 246));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        gbc.insets = new Insets(0, 0, 12, 0);
        panel.add(field, gbc);
    }

    private void attemptLogin(String username, String password) {

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        String selectedRole = roleCombo.getSelectedItem().toString();

        //String query = "SELECT role FROM users WHERE username=? AND password=? AND role=?";
        String query = "SELECT username, role FROM users WHERE username=? AND password=? AND role=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, selectedRole);

            ResultSet rs = ps.executeQuery();

//            if (rs.next()) {
//                new MainDashboard(selectedRole);
//                dispose();
            if (rs.next()) {
                String loggedInName = rs.getString("username");
                new MainDashboard(selectedRole, loggedInName);
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid credentials for selected role.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Database Error! Check DBConnection and MySQL.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
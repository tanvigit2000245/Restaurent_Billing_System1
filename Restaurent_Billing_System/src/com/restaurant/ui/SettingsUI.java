package com.restaurant.ui;

import com.restaurent.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SettingsUI extends JPanel {

    private JTextField gstField, serviceField, nameField, phoneField, addressField, footerField;
    private JCheckBox cashBox, upiBox, cardBox;

    private final Color bg = new Color(245, 243, 240);
    private final Color maroon = new Color(122, 31, 46);

    public SettingsUI() {
        setLayout(new BorderLayout());
        setBackground(bg);
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        add(formPanel(), BorderLayout.CENTER);
        loadSettings();
    }

    private JPanel formPanel() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 215, 210)),
                BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("⚙ Settings");
        title.setFont(new Font("Dialog", Font.BOLD, 30));
        title.setForeground(new Color(35, 35, 35));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        gstField = field();
        serviceField = field();
        nameField = field();
        phoneField = field();
        addressField = field();
        footerField = field();

        addRow(card, gbc, 1, "GST Rate %", gstField);
        addRow(card, gbc, 2, "Service Charge %", serviceField);
        addRow(card, gbc, 3, "Restaurant Name", nameField);
        addRow(card, gbc, 4, "Phone", phoneField);
        addRow(card, gbc, 5, "Address", addressField);
        addRow(card, gbc, 6, "Bill Footer Message", footerField);

        JLabel paymentLbl = new JLabel("Payment Methods");
        paymentLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        card.add(paymentLbl, gbc);

        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        paymentPanel.setOpaque(false);

        cashBox = new JCheckBox("Cash");
        upiBox = new JCheckBox("UPI");
        cardBox = new JCheckBox("Card");

        paymentPanel.add(cashBox);
        paymentPanel.add(upiBox);
        paymentPanel.add(cardBox);

        gbc.gridx = 1;
        card.add(paymentPanel, gbc);

        JButton saveBtn = new JButton("Save Settings");
        saveBtn.setBackground(maroon);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        saveBtn.setPreferredSize(new Dimension(180, 42));

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        card.add(saveBtn, gbc);

        saveBtn.addActionListener(e -> saveSettings());

        return card;
    }

    private JTextField field() {
        JTextField f = new JTextField(22);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        return f;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String label, JTextField field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));

        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void loadSettings() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM settings WHERE id=1");
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                gstField.setText(String.valueOf(rs.getDouble("gst_rate")));
                serviceField.setText(String.valueOf(rs.getDouble("service_charge")));
                nameField.setText(rs.getString("restaurant_name"));
                phoneField.setText(rs.getString("phone"));
                addressField.setText(rs.getString("address"));
                footerField.setText(rs.getString("footer_message"));

                cashBox.setSelected(rs.getBoolean("cash_enabled"));
                upiBox.setSelected(rs.getBoolean("upi_enabled"));
                cardBox.setSelected(rs.getBoolean("card_enabled"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unable to load settings.");
        }
    }

    private void saveSettings() {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE settings SET gst_rate=?, service_charge=?, restaurant_name=?, phone=?, address=?, " +
                             "footer_message=?, cash_enabled=?, upi_enabled=?, card_enabled=? WHERE id=1"
             )) {

            ps.setDouble(1, Double.parseDouble(gstField.getText().trim()));
            ps.setDouble(2, Double.parseDouble(serviceField.getText().trim()));
            ps.setString(3, nameField.getText().trim());
            ps.setString(4, phoneField.getText().trim());
            ps.setString(5, addressField.getText().trim());
            ps.setString(6, footerField.getText().trim());
            ps.setBoolean(7, cashBox.isSelected());
            ps.setBoolean(8, upiBox.isSelected());
            ps.setBoolean(9, cardBox.isSelected());

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Settings saved successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid input or database error.");
        }
    }
}
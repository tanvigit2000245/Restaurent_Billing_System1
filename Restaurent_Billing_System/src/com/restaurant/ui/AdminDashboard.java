package com.restaurant.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AdminDashboard extends JFrame {
    private final Color maroon = new Color(122, 31, 46), bgPage = new Color(245, 243, 240),
                        iconBg = new Color(245, 232, 234), border = new Color(210, 205, 200),
                        textMain = new Color(33, 28, 28);

    public AdminDashboard(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            JOptionPane.showMessageDialog(null, "Access Denied!");
            return;
        }

        setTitle("Admin Dashboard – RestroFlow");
        setSize(780, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(bgPage);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(maroon);
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 16, 24));

        JPanel headerText = new JPanel(new GridLayout(2, 1));
        headerText.setOpaque(false);
        headerText.add(createLbl("RestroFlow", 11, new Color(255, 255, 255, 130), false));
        headerText.add(createLbl("Admin Dashboard", 20, Color.WHITE, true));

        JLabel avatar = new JLabel("👤", SwingConstants.CENTER);
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(255, 255, 255, 40));
        avatar.setPreferredSize(new Dimension(42, 42));

        header.add(headerText, BorderLayout.WEST);
        header.add(avatar, BorderLayout.EAST);
        mainPanel.add(header, BorderLayout.NORTH);

        // Grid
        JPanel grid = new JPanel(new GridLayout(2, 3, 15, 15));
        grid.setBackground(bgPage);
        grid.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        grid.add(createCard("🍽️", "Manage Menu", iconBg, Color.WHITE, border, textMain, e -> new MenuManagementUI()));
        grid.add(createCard("📋", "View Orders", iconBg, Color.WHITE, border, textMain, e -> new OrdersUI()));
        grid.add(createCard("📊", "Reports", iconBg, Color.WHITE, border, textMain, e -> new ReportsUI()));
        grid.add(createCard("👥", "Users", iconBg, Color.WHITE, border, textMain, e -> new UserManagementUI()));
        grid.add(createCard("⚙️", "Settings", iconBg, Color.WHITE, border, textMain, e -> new SettingsUI()));
        grid.add(createCard("🚪", "Logout", new Color(255,255,255,40), maroon, maroon, Color.WHITE, e -> { new LoginFrame(); dispose(); }));

        mainPanel.add(grid, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private JLabel createLbl(String txt, int size, Color c, boolean bold) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(c);
        return l;
    }

    private JPanel createCard(String emoji, String txt, Color iBg, Color bg, Color brd, Color tCol, ActionListener al) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(bg);
        c.setCursor(new Cursor(Cursor.HAND_CURSOR));
        c.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(brd, 1),
            BorderFactory.createEmptyBorder(20, 10, 20, 10)
        ));

        // Refined Icon Label
        JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        icon.setOpaque(true);
        icon.setBackground(iBg);
        
        // Force the icon container to be a perfect square
        Dimension d = new Dimension(56, 56);
        icon.setPreferredSize(d);
        icon.setMinimumSize(d);
        icon.setMaximumSize(d);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = createLbl(txt, 14, tCol, true);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        c.add(Box.createVerticalGlue());
        c.add(icon);
        c.add(Box.createVerticalStrut(12)); // Spacing between icon and text
        c.add(label);
        c.add(Box.createVerticalGlue());

        Color hov = (bg == maroon) ? maroon.darker() : new Color(242, 240, 238);
        c.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { c.setBackground(hov); }
            public void mouseExited(MouseEvent e)  { c.setBackground(bg); }
            public void mouseClicked(MouseEvent e) { al.actionPerformed(null); }
        });
        return c;
    }
}
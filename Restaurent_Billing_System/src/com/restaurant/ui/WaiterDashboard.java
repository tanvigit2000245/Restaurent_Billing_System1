package com.restaurant.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class WaiterDashboard extends JFrame {

    // ── Shared Theme Colors ─────────────────────────────────────────
    private final Color maroon = new Color(122, 31, 46), bgPage = new Color(245, 243, 240),
                        bgAction = new Color(250, 248, 246), border = new Color(210, 205, 200),
                        textMuted = new Color(120, 115, 110), textMain = new Color(33, 28, 28);

    public WaiterDashboard() {
        setTitle("Waiter Dashboard – RestroFlow");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(bgPage);

        // ── Header ──────────────────────────────────────────
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(maroon);
        header.setBorder(BorderFactory.createEmptyBorder(22, 24, 18, 24));
        header.add(createLabel("RestroFlow", 12, new Color(255, 255, 255, 140), false));
        header.add(createLabel("Waiter Panel", 22, Color.WHITE, true));
        mainPanel.add(header, BorderLayout.NORTH);

        // ── Center – Action Cards ────────────────────────────
        JPanel center = new JPanel();
        center.setBackground(bgPage);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        center.add(makeActionCard("🪑", "Create Order", "Start a new table order", e -> new WaiterOrderPanel()));
        center.add(Box.createVerticalStrut(12));
        center.add(makeActionCard("📋", "View Table Orders", "Check all active orders", e -> new TableOrdersPanel()));
        mainPanel.add(center, BorderLayout.CENTER);

        // ── Footer – Logout ──────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(bgPage);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, border),
            BorderFactory.createEmptyBorder(12, 24, 16, 24)
        ));

        JButton logout = new JButton("Logout");
        logout.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logout.setForeground(maroon);
        logout.setBackground(bgPage);
        logout.setBorder(BorderFactory.createLineBorder(maroon, 1));
        logout.setFocusPainted(false);
        logout.setPreferredSize(new Dimension(90, 32));
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> { new LoginFrame(); dispose(); });

        footer.add(logout);
        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    // ── Helper: Label Generator ─────────────────────────────────────
    private JLabel createLabel(String text, int size, Color color, boolean isBold) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", isBold ? Font.BOLD : Font.PLAIN, size));
        lbl.setForeground(color);
        return lbl;
    }

    // ── Helper: Action Card Generator ───────────────────────────────
    private JPanel makeActionCard(String emoji, String title, String sub, ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(14, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        card.setBackground(bgAction);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border, 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        icon.setPreferredSize(new Dimension(44, 44));
        icon.setOpaque(true);
        icon.setBackground(new Color(245, 232, 234));
        card.add(icon, BorderLayout.WEST);

        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        text.add(createLabel(title, 14, textMain, true));
        text.add(createLabel(sub, 12, textMuted, false));
        card.add(text, BorderLayout.CENTER);

        card.add(createLabel("›", 20, textMuted, false), BorderLayout.EAST);
        
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { action.actionPerformed(null); }
            public void mouseEntered(MouseEvent e) { card.setBackground(new Color(240, 235, 232)); }
            public void mouseExited(MouseEvent e)  { card.setBackground(bgAction); }
        });

        return card;
    }
}
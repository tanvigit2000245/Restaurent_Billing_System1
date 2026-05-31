package com.restaurant.ui;

import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private String role;
    private String userName;

    private final Color maroon = new Color(122, 31, 46);
    private final Color bgPage = new Color(238, 233, 228);
    private final Color sidebarBg = new Color(45, 45, 45);

    public MainDashboard(String role, String userName) {
        this.role = role;
        this.userName = userName;
        setTitle("RestroFlow Dashboard");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.add(createSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(createPage("Dashboard", homePanel()), "HOME");

        if (role.equalsIgnoreCase("ADMIN")) {
            contentPanel.add(createPage("Menu Management", new MenuManagementUI()), "MENU");
            contentPanel.add(createPage("User Management", new UserManagementUI()), "USERS");
            contentPanel.add(createPage("Reports", new ReportsUI()), "REPORTS");
            contentPanel.add(createPage("Inventory Management", new InventoryManagementUI()), "INVENTORY");
            contentPanel.add(createPage("Settings", new SettingsUI()), "SETTINGS");
        }

        if (role.equalsIgnoreCase("WAITER")) {
            contentPanel.add(createPage("Create Order", new WaiterOrderPanel()), "CREATE_ORDER");
            
            // FIX: Add TableOrdersPanel directly WITHOUT wrapping it in createPage().
            // This prevents the duplicate top header layout framework from showing up!
            contentPanel.add(new TableOrdersPanel(), "TABLE_ORDERS");
        }

        if (role.equalsIgnoreCase("CASHIER")) {
            contentPanel.add(createPage("Generate Bill", new GenerateBillPanel()), "GENERATE_BILL");
            contentPanel.add(createPage("Order History", new CashierOrderHistoryPanel()), "ORDER_HISTORY");
        }

        root.add(contentPanel, BorderLayout.CENTER);
        add(root);
        setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(250, 650));
        sidebar.setBackground(sidebarBg);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(25, 18, 25, 18));

        JLabel appName = new JLabel("RestroFlow");
        appName.setForeground(Color.WHITE);
        appName.setFont(new Font("Segoe UI", Font.BOLD, 28));
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel(role.toUpperCase() + " PANEL");
        roleLabel.setForeground(new Color(220, 220, 220));
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(appName);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(roleLabel);
        sidebar.add(Box.createVerticalStrut(40));

        sidebar.add(sideButton("⌂  Dashboard", "HOME"));

        if (role.equalsIgnoreCase("ADMIN")) {
            sidebar.add(sideButton("▣  Manage Menu", "MENU"));
            sidebar.add(sideButton("◉  Manage Users", "USERS"));
            sidebar.add(sideButton("▥  Reports", "REPORTS"));
            sidebar.add(sideButton("▤  Inventory", "INVENTORY"));
            sidebar.add(sideButton("⚙  Settings", "SETTINGS"));
        }

        if (role.equalsIgnoreCase("WAITER")) {
            sidebar.add(sideButton("▤  Create Order", "CREATE_ORDER"));
            sidebar.add(sideButton("☷  Table Orders", "TABLE_ORDERS"));
        }

        if (role.equalsIgnoreCase("CASHIER")) {
            sidebar.add(sideButton("▤  Generate Bill", "GENERATE_BILL"));
            sidebar.add(sideButton("☰  Order History", "ORDER_HISTORY"));
        }

        sidebar.add(Box.createVerticalGlue());

        JButton logout = sideButton("↩  Logout", "LOGOUT");
        logout.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        sidebar.add(logout);
        return sidebar;
    }

    private JButton sideButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(new Font("Segoe UI Symbol", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(sidebarBg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(65, 65, 65));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(sidebarBg);
            }
        });

        if (!cardName.equals("LOGOUT")) {
            btn.addActionListener(e -> cardLayout.show(contentPanel, cardName));
        }

        return btn;
    }

    private JPanel createPage(String titleText, JPanel body) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(bgPage);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(maroon);
        header.setBorder(BorderFactory.createEmptyBorder(20, 35, 20, 35));

        JLabel title = new JLabel(titleText);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        header.add(title, BorderLayout.WEST);

        page.add(header, BorderLayout.NORTH);
        page.add(body, BorderLayout.CENTER);

        return page;
    }

    private JPanel homePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgPage);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel card = new JPanel(new BorderLayout(35, 0));
        card.setPreferredSize(new Dimension(900, 330));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 220, 215)),
                BorderFactory.createEmptyBorder(45, 55, 45, 55)
        ));

        String subtitleText;

        if (role.equalsIgnoreCase("ADMIN")) {
            subtitleText = "Manage menu items, users, reports, and restaurant settings.";
        } else if (role.equalsIgnoreCase("WAITER")) {
            subtitleText = "Create orders, manage table orders, and serve customers faster.";
        } else if (role.equalsIgnoreCase("CASHIER")) {
            subtitleText = "Generate bills, process payments, and view order history.";
        } else {
            subtitleText = "Use the sidebar to continue.";
        }

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome back, " + userName);
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        title.setForeground(new Color(30, 30, 30));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "<html><div style='width:520px;'>" + subtitleText + "</div></html>"
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        subtitle.setForeground(new Color(90, 90, 90));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(18));
        textPanel.add(subtitle);
        textPanel.add(Box.createVerticalGlue());

        JPanel logoPanel = new JPanel(new GridBagLayout());
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(220, 220));

        JLabel logo = new JLabel("🍽");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 145));
        logo.setForeground(new Color(225, 220, 215));
        logoPanel.add(logo);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(logoPanel, BorderLayout.EAST);

        panel.add(card);
        return panel;
    }
}
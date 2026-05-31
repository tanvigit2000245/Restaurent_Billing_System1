package com.restaurant.ui;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import com.restaurent.db.DBConnection;

public class GenerateBillPanel extends JPanel {
    private JComboBox<Integer> tableCombo;
    private JTextField gstField, taxField, discountField;
    private JTable itemTable;
    private DefaultTableModel model;
    private JTextArea billPreview;
    private JLabel foodTotalLbl, gstLbl, taxLbl, discountLbl, grandTotalLbl;

    private int tableNo = 0;
    private double baseAmount = 0, grandTotal = 0;

    private final Color maroon = new Color(122, 31, 46);
    private final Color bg = new Color(245, 243, 240);

    private JButton printBtn;
    private String finalBillText = "";
    
    public GenerateBillPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(bg);
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        add(formPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 15, 0));
        center.setBackground(bg);
        center.add(tablePanel());
        center.add(rightPanel());

        add(center, BorderLayout.CENTER);
        loadPendingTables();
    }

    private JPanel formPanel() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 12));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel fields = new JPanel(new GridLayout(2, 4, 12, 8));
        fields.setBackground(Color.WHITE);

        tableCombo = new JComboBox<>();
        gstField = field("5");
        taxField = field("0");
        discountField = field("0");

        fields.add(label("Table No"));
        fields.add(label("GST %"));
        fields.add(label("Tax ₹"));
        fields.add(label("Discount ₹"));

        fields.add(tableCombo);
        fields.add(gstField);
        fields.add(taxField);
        fields.add(discountField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttons.setBackground(Color.WHITE);

        JButton load = button("Load Order");
        JButton generate = button("Generate Bill");
        JButton pay = button("Pay");
        printBtn = button("Print Bill");
        JButton refresh = button("Refresh Tables");

        printBtn.setEnabled(false);
        buttons.add(load);
        buttons.add(generate);
        buttons.add(pay);
        buttons.add(printBtn);
        buttons.add(refresh);

        p.add(fields);
        p.add(buttons);

        load.addActionListener(e -> loadOrder());
        generate.addActionListener(e -> generateBill());
        pay.addActionListener(e -> processPayment());
        refresh.addActionListener(e -> loadPendingTables());
        printBtn.addActionListener(e -> printBill());
        return p;
    }

    private JScrollPane tablePanel() {
        model = new DefaultTableModel(new String[]{"Item", "Qty", "Price ₹", "Total ₹"}, 0);
        itemTable = new JTable(model);
        itemTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        itemTable.setRowHeight(34);
        itemTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));

        JScrollPane sp = new JScrollPane(itemTable);
        sp.setBorder(BorderFactory.createTitledBorder("Order Items"));
        return sp;
    }

    private JPanel rightPanel() {
        JPanel p = new JPanel(new BorderLayout(12, 12));
        p.setBackground(bg);

        JPanel summary = new JPanel(new GridLayout(5, 1, 8, 8));
        summary.setBackground(Color.WHITE);
        summary.setBorder(BorderFactory.createTitledBorder("Bill Summary"));

        foodTotalLbl = summaryLabel("Food Total: ₹0.00");
        gstLbl = summaryLabel("GST: ₹0.00");
        taxLbl = summaryLabel("Tax: ₹0.00");
        discountLbl = summaryLabel("Discount: ₹0.00");
        grandTotalLbl = summaryLabel("Grand Total: ₹0.00");
        grandTotalLbl.setForeground(maroon);
        grandTotalLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));

        summary.add(foodTotalLbl);
        summary.add(gstLbl);
        summary.add(taxLbl);
        summary.add(discountLbl);
        summary.add(grandTotalLbl);

        billPreview = new JTextArea();
        billPreview.setEditable(false);
        billPreview.setFont(new Font("Monospaced", Font.PLAIN, 13));
        billPreview.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane preview = new JScrollPane(billPreview);
        preview.setBorder(BorderFactory.createTitledBorder("Generated Bill Preview"));

        p.add(summary, BorderLayout.NORTH);
        p.add(preview, BorderLayout.CENTER);

        return p;
    }

    private void loadPendingTables() {
        tableCombo.removeAllItems();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT DISTINCT table_no FROM orders WHERE payment_status='PENDING' ORDER BY table_no"
             );
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tableCombo.addItem(rs.getInt("table_no"));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unable to load table numbers.");
        }
    }

    private void loadOrder() {
        try {
            if (tableCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "No pending table found.");
                return;
            }

            tableNo = (int) tableCombo.getSelectedItem();

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "SELECT order_details, final_total FROM orders " +
                         "WHERE table_no=? AND payment_status='PENDING' " +
                         "ORDER BY id DESC LIMIT 1"
                 )) {

                ps.setInt(1, tableNo);
                ResultSet rs = ps.executeQuery();

                model.setRowCount(0);
                billPreview.setText("");
                baseAmount = 0;

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "No pending order found.");
                    return;
                }

                String details = rs.getString("order_details");
                details = details.replace(";", ",");

                for (String line : details.split(",")) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    OrderItem item = parseOrderItem(line);

                    double price = getItemPrice(item.name);
                    double totalPrice = price * item.qty;

                    model.addRow(new Object[]{
                            item.name,
                            item.qty,
                            money(price),
                            money(totalPrice)
                    });

                    baseAmount += totalPrice;
                }

                if (baseAmount == 0) {
                    baseAmount = rs.getDouble("final_total");
                }

                grandTotal = baseAmount;
                updateSummary(0, 0, 0, grandTotal);
                billPreview.setText("Order loaded. Click Generate Bill.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading order.");
        }
    }
    private double getItemPrice(String itemName) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT price FROM menu WHERE LOWER(TRIM(name)) = LOWER(TRIM(?)) LIMIT 1"
             )) {

            ps.setString(1, itemName);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("price");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void generateBill() {
        try {
            if (baseAmount <= 0) {
                JOptionPane.showMessageDialog(this, "Load order first.");
                return;
            }

            double gstPercent = Double.parseDouble(gstField.getText().trim());
            double tax = Double.parseDouble(taxField.getText().trim());
            double discount = Double.parseDouble(discountField.getText().trim());

            double gst = baseAmount * gstPercent / 100;
            grandTotal = baseAmount + gst + tax - discount;

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "UPDATE orders SET gst=?, other_tax=?, discount=?, grand_total=? " +
                         "WHERE table_no=? AND payment_status='PENDING'"
                 )) {

                ps.setDouble(1, gst);
                ps.setDouble(2, tax);
                ps.setDouble(3, discount);
                ps.setDouble(4, grandTotal);
                ps.setInt(5, tableNo);
                ps.executeUpdate();
            }

            updateSummary(gst, tax, discount, grandTotal);
            showBillPreview(gstPercent, gst, tax, discount);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid bill input.");
        }
    }

    private void showBillPreview(double gstPercent, double gst, double tax, double discount) {
        StringBuilder sb = new StringBuilder();

        sb.append("=========== RESTROFLOW ===========\n");
        sb.append("Restaurant Billing System\n");
        sb.append("Phone: 9876543210\n");
        sb.append("Address: Your Restaurant Address\n");
        sb.append("----------------------------------\n");
        sb.append("Table No: ").append(tableNo).append("\n");
        sb.append("Date: ").append(java.time.LocalDate.now()).append("\n");
        sb.append("----------------------------------\n");
        sb.append(String.format("%-12s %3s %8s %8s\n", "Item", "Qty", "Price", "Total"));
        sb.append("----------------------------------\n");

        for (int i = 0; i < model.getRowCount(); i++) {
            sb.append(String.format("%-12s %3s %8s %8s\n",
                    model.getValueAt(i, 0),
                    model.getValueAt(i, 1),
                    model.getValueAt(i, 2),
                    model.getValueAt(i, 3)));
        }

        sb.append("----------------------------------\n");
        sb.append("Food Total   ₹").append(money(baseAmount)).append("\n");
        sb.append("GST ").append(gstPercent).append("%      ₹").append(money(gst)).append("\n");
        sb.append("Tax          ₹").append(money(tax)).append("\n");
        sb.append("Discount     ₹").append(money(discount)).append("\n");
        sb.append("----------------------------------\n");
        sb.append("GRAND TOTAL  ₹").append(money(grandTotal)).append("\n");
        sb.append("----------------------------------\n");
        sb.append("Thank you for visiting!\n");
        sb.append("==================================");

        finalBillText = sb.toString();
        billPreview.setText(finalBillText);
        billPreview.setCaretPosition(0);
        printBtn.setEnabled(true);
    }

    private void processPayment() {
        try {
            if (tableNo == 0 || grandTotal <= 0) {
                JOptionPane.showMessageDialog(this, "Generate bill first.");
                return;
            }

            String method = (String) JOptionPane.showInputDialog(
                    this,
                    "Select Payment Method",
                    "Payment",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Cash", "UPI", "Card"},
                    "Cash"
            );

            if (method == null) return;

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                		 "UPDATE orders SET payment_status='PAID', " +
                				 "payment_method=?, " +
                				 "Order_date=CURDATE() " +
                				 "WHERE table_no=? AND payment_status='PENDING'"
                 )) {

                ps.setString(1, method);
                ps.setInt(2, tableNo);

                if (ps.executeUpdate() > 0) {
                	PreparedStatement inv = con.prepareStatement(
                		    "UPDATE inventory i " +
                		    "JOIN recipe_items r ON i.ingredient_name = r.ingredient_name " +
                		    "SET i.stock_qty = i.stock_qty - (? * r.qty_used) " +
                		    "WHERE LOWER(TRIM(r.menu_item)) = LOWER(TRIM(?))"
                		);

                		for (int i = 0; i < model.getRowCount(); i++) {

                		    String itemName = model.getValueAt(i, 0).toString();
                		    int qty = Integer.parseInt(model.getValueAt(i, 1).toString());

                		    inv.setInt(1, qty);
                		    inv.setString(2, itemName);
                		    inv.executeUpdate();
                		}
                    try (PreparedStatement free = con.prepareStatement(
                            "UPDATE tables SET status='FREE' WHERE table_no=?"
                    )) {
                        free.setInt(1, tableNo);
                        free.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(this, "Payment successful via " + method);

                    int print = JOptionPane.showConfirmDialog(
                            this,
                            "Do you want to print the bill?",
                            "Print Bill",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (print == JOptionPane.YES_OPTION) {
                        printBill();
                    }

                    clearBill();
                    loadPendingTables();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Payment failed.");
        }
    }

    private void updateSummary(double gst, double tax, double discount, double total) {
        foodTotalLbl.setText("Food Total: ₹" + money(baseAmount));
        gstLbl.setText("GST: ₹" + money(gst));
        taxLbl.setText("Tax: ₹" + money(tax));
        discountLbl.setText("Discount: ₹" + money(discount));
        grandTotalLbl.setText("Grand Total: ₹" + money(total));
    }

    private void clearBill() {
        model.setRowCount(0);
        billPreview.setText("");
        baseAmount = grandTotal = tableNo = 0;
        updateSummary(0, 0, 0, 0);
    }

    private JTextField field(String text) {
        JTextField f = new JTextField(text);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        return f;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return l;
    }

    private JLabel summaryLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        return l;
    }

    private JButton button(String text) {
        JButton b = new JButton(text);
        b.setBackground(maroon);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(145, 38));
        return b;
    }

    private String money(double value) {
        return String.format("%.2f", value);
    }
    private OrderItem parseOrderItem(String text) {
        text = text.trim();

        int qty = 1;
        String name = text;

        if (text.matches(".*\\sx\\d+$")) {
            int index = text.lastIndexOf("x");
            name = text.substring(0, index).trim();
            qty = Integer.parseInt(text.substring(index + 1).trim());
        }

        return new OrderItem(name, qty);
    }

    private static class OrderItem {
        String name;
        int qty;

        OrderItem(String name, int qty) {
            this.name = name;
            this.qty = qty;
        }
    }
    private void printBill() {

        JFrame billFrame = new JFrame("RestroFlow Bill");
        billFrame.setSize(420, 650);
        billFrame.setLocationRelativeTo(this);

        JTextArea area = new JTextArea(finalBillText);

        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 15));
        area.setMargin(new Insets(20,20,20,20));
        area.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(area);

        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(new Color(122,31,46));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);

        closeBtn.addActionListener(e -> billFrame.dispose());

        JPanel bottom = new JPanel();
        bottom.add(closeBtn);

        billFrame.add(scroll, BorderLayout.CENTER);
        billFrame.add(bottom, BorderLayout.SOUTH);

        billFrame.setVisible(true);
      } }
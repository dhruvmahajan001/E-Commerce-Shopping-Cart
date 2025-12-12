import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

class CheckoutSummaryPage extends JDialog {

    public CheckoutSummaryPage(JFrame parent, List<CartItem> items, double subtotal, int discount, double total,
                               String coupon, java.util.function.Consumer<String> onConfirm, Runnable onCancel,
                               Color appBG, Color panelBG, Color textColor, Color primaryBlue, Color hoverBlue) {

        super(parent, "Checkout Summary", true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(appBG);
        setSize(650, 750);
        setLocationRelativeTo(parent);

        // ========================= HEADER =========================
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(255, 87, 87));
        header.setPreferredSize(new Dimension(0, 60));

        JLabel titleLbl = new JLabel("Checkout Summary", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(Color.WHITE);

        header.add(titleLbl, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ========================= CENTER PANEL =========================
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // ========================= ORDER SUMMARY =========================
        JTextArea itemsList = new JTextArea();
        itemsList.setEditable(false);
        itemsList.setBackground(new Color(255, 250, 245));
        itemsList.setForeground(textColor);
        itemsList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemsList.setBorder(new EmptyBorder(10, 10, 10, 10));

        StringBuilder sb = new StringBuilder("ORDER ITEMS:\n\n");
        for (CartItem ci : items) {
            sb.append(String.format("• %s x%d = ₹%.2f\n", ci.p.name, ci.q, ci.total()));
        }

        sb.append("\n").append("─".repeat(40)).append("\n");
        sb.append(String.format("Subtotal: ₹%.2f\n", subtotal));
        sb.append(String.format("Discount: -₹%d %s\n", discount, coupon.isEmpty() ? "" : "(" + coupon + ")"));
        sb.append(String.format("TOTAL: ₹%.2f\n", total));

        itemsList.setText(sb.toString());

        JScrollPane scrollPane = new JScrollPane(itemsList);
        scrollPane.setBorder(new LineBorder(new Color(255, 200, 150), 1));
        scrollPane.setPreferredSize(new Dimension(0, 200));

        // ========================= FORM PANEL =========================
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 13);

        // ---------- CUSTOMER DETAILS ----------
        JLabel custTitle = new JLabel("Customer Details");
        custTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        custTitle.setForeground(textColor);
        formPanel.add(custTitle);

        JTextField firstName = new JTextField(20);
        formPanel.add(makeLabeledField("First Name:", firstName, labelFont, inputFont, textColor));

        JTextField lastName = new JTextField(20);
        formPanel.add(makeLabeledField("Last Name:", lastName, labelFont, inputFont, textColor));

        JTextField phone = new JTextField(20);
        formPanel.add(makeLabeledField("Phone Number:", phone, labelFont, inputFont, textColor));

        JTextField email = new JTextField(20);
        formPanel.add(makeLabeledField("Email:", email, labelFont, inputFont, textColor));

        formPanel.add(Box.createVerticalStrut(15));

        // ---------- SHIPPING ADDRESS ----------
        JLabel addressTitle = new JLabel("Shipping Address");
        addressTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        addressTitle.setForeground(textColor);
        formPanel.add(addressTitle);

        JTextField addr1 = new JTextField(25);
        formPanel.add(makeLabeledField("Address Line 1:", addr1, labelFont, inputFont, textColor));

        JTextField addr2 = new JTextField(25);
        formPanel.add(makeLabeledField("Address Line 2:", addr2, labelFont, inputFont, textColor));

        JTextField city = new JTextField(20);
        formPanel.add(makeLabeledField("City:", city, labelFont, inputFont, textColor));

        JTextField state = new JTextField(20);
        formPanel.add(makeLabeledField("State:", state, labelFont, inputFont, textColor));

        JTextField pincode = new JTextField(10);
        formPanel.add(makeLabeledField("Pincode:", pincode, labelFont, inputFont, textColor));

        formPanel.add(Box.createVerticalStrut(15));

        // ---------- PAYMENT OPTIONS ----------
        JLabel payTitle = new JLabel("Payment Method");
        payTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        payTitle.setForeground(textColor);
        formPanel.add(payTitle);

        JRadioButton cod = new JRadioButton("Cash on Delivery (COD)");
        JRadioButton upi = new JRadioButton("UPI Payment");
        JRadioButton card = new JRadioButton("Debit/Credit Card");

        cod.setOpaque(false);
        upi.setOpaque(false);
        card.setOpaque(false);

        cod.setForeground(textColor);
        upi.setForeground(textColor);
        card.setForeground(textColor);

        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cod);
        paymentGroup.add(upi);
        paymentGroup.add(card);

        cod.setSelected(true);

        formPanel.add(cod);
        formPanel.add(upi);
        formPanel.add(card);

        formPanel.add(Box.createVerticalStrut(20));

        // Add everything
        centerPanel.add(scrollPane, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ========================= BUTTON PANEL =========================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setOpaque(false);

        BlueButton confirmBtn = new BlueButton("Confirm Order", primaryBlue, hoverBlue, Color.WHITE);
        BlueButton cancelBtn = new BlueButton("Cancel", new Color(120, 120, 120), new Color(150, 150, 150), Color.WHITE);

        // ---------- CONFIRM ACTION ----------
        confirmBtn.addActionListener(e -> {

            if (firstName.getText().trim().isEmpty() ||
                lastName.getText().trim().isEmpty() ||
                phone.getText().trim().isEmpty() ||
                addr1.getText().trim().isEmpty() ||
                city.getText().trim().isEmpty() ||
                state.getText().trim().isEmpty() ||
                pincode.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(this,
                        "Please fill all required fields!",
                        "Incomplete Details",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String finalAddress =
                    "Name: " + firstName.getText() + " " + lastName.getText() + "\n" +
                    "Phone: " + phone.getText() + "\n" +
                    "Email: " + email.getText() + "\n\n" +
                    "Address:\n" +
                    addr1.getText() + "\n" +
                    addr2.getText() + "\n" +
                    city.getText() + ", " + state.getText() + " - " + pincode.getText() + "\n\n" +
                    "Payment Mode: " +
                    (cod.isSelected() ? "Cash on Delivery" :
                     upi.isSelected() ? "UPI Payment" :
                     "Debit/Credit Card");

            onConfirm.accept(finalAddress);
            dispose();
        });

        cancelBtn.addActionListener(e -> {
            onCancel.run();
            dispose();
        });

        btnPanel.add(confirmBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    // ===========================================================
    // HELPER METHOD FOR LABELED TEXT FIELDS
    // ===========================================================
    private JPanel makeLabeledField(String label, JTextField field, Font labelFont, Font inputFont, Color textColor) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(labelFont);
        lbl.setForeground(textColor);

        field.setFont(inputFont);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(255, 200, 150), 1),
                new EmptyBorder(5, 5, 5, 5)
        ));

        panel.add(lbl, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }
}

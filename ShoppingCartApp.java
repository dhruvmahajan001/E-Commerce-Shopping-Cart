import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

class Product {
    int id; String name; String cat; double price; String imagePath;
    Product(int id, String name, String cat, double price, String imagePath){ 
        this.id=id; this.name=name; this.cat=cat; this.price=price; this.imagePath=imagePath;
    }
    public String toString(){ return "["+cat+"] "+id+" - "+name+" (₹"+price+")"; }
}

class CartItem {
    Product p; int q;
    CartItem(Product p,int q){ this.p=p; this.q=q; }
    double total(){ return p.price*q; }
}

class BlueButton extends JButton {
    private final Color base, hover, text;
    public BlueButton(String textLabel, Color base, Color hover, Color text){
        super(textLabel);
        this.base = base; this.hover = hover; this.text = text;
        setOpaque(false); setForeground(text); setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR)); setBorder(new EmptyBorder(8,16,8,16));
        setContentAreaFilled(false); setBackground(base);
        addMouseListener(new MouseAdapter(){ 
            public void mouseEntered(MouseEvent e){ setBackground(hover); repaint(); } 
            public void mouseExited(MouseEvent e){ setBackground(base); repaint(); }
        });
    }
    @Override protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground()); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
        super.paintComponent(g2); g2.dispose();
    }
    @Override public void updateUI(){ super.updateUI(); setContentAreaFilled(false); }
    @Override public boolean isOpaque(){ return false; }
}

class GlassPanel extends JPanel {
    private Color base; private int arc=20;
    public GlassPanel(Color base,int arc){ this.base=base; this.arc=arc; setOpaque(false); }
    @Override protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(), h=getHeight();
        if(w>0 && h>0){
            g2.setColor(new Color(0,0,0,36));
            g2.fillRoundRect(6,6,Math.max(0,w-12),Math.max(0,h-12),arc,arc);
            Color c1 = new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.min(140, base.getAlpha()));
            GradientPaint gp = new GradientPaint(0,0,c1,0,h,new Color(255,255,255,30));
            g2.setPaint(gp);
            g2.fillRoundRect(0,0,Math.max(0,w-12),Math.max(0,h-12),arc,arc);
            g2.setColor(new Color(255,255,255,110));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0,0,Math.max(0,w-13),Math.max(0,h-13),arc,arc);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

class ProductCard extends JPanel {
    private Product product;
    private final Color cardBG = new Color(255, 250, 245);
    private final Color hoverBG = new Color(255, 240, 225);
    private final Color textColor = new Color(120, 40, 31);
    private final Color priceColor = new Color(255, 107, 53);
    
    public ProductCard(Product p, Runnable onAddToCart, Runnable onBuyNow) {
        this.product = p;
        setLayout(new BorderLayout(8, 8));
        setBackground(cardBG);
        setBorder(new CompoundBorder(
            new LineBorder(new Color(255, 200, 150), 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
        setPreferredSize(new Dimension(220, 280));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setPreferredSize(new Dimension(200, 160));
        imagePanel.setBorder(new LineBorder(new Color(255, 200, 150), 1));
        
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        try {
            ImageIcon icon = new ImageIcon(p.imagePath);
            if (icon.getIconWidth() > 0) {
                Image scaled = icon.getImage().getScaledInstance(180, 140, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaled));
            } else {
                throw new Exception("Invalid image");
            }
        } catch (Exception e) {
            String emoji = getCategoryEmoji(p.cat);
            imageLabel.setText("<html><div style='text-align:center; font-size:48px;'>" + emoji + "</div></html>");
            imageLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        }
        
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        add(imagePanel, BorderLayout.NORTH);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel("<html><div style='width:180px; text-align:center;'>" + p.name + "</div></html>");
        nameLabel.setForeground(textColor);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel priceLabel = new JLabel("₹" + String.format("%,.0f", p.price));
        priceLabel.setForeground(priceColor);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(priceLabel);
        
        add(infoPanel, BorderLayout.CENTER);
        
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        btnPanel.setOpaque(false);
        
        JButton addBtn = createCardButton("Add", new Color(255, 107, 53));
        JButton buyBtn = createCardButton("Buy", new Color(255, 140, 90));
        
        addBtn.addActionListener(e -> onAddToCart.run());
        buyBtn.addActionListener(e -> onBuyNow.run());
        
        btnPanel.add(addBtn);
        btnPanel.add(buyBtn);
        
        add(btnPanel, BorderLayout.SOUTH);
        
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                setBackground(hoverBG);
                repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                setBackground(cardBG);
                repaint();
            }
        });
    }
    
    private JButton createCardButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            Color original = bg;
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(original);
            }
        });
        
        return btn;
    }
    
    private String getCategoryEmoji(String category) {
        switch (category.toLowerCase()) {
            case "electronics": return "📱";
            case "clothing": return "👕";
            case "groceries": return "🛒";
            case "books": return "📚";
            case "accessories": return "👜";
            default: return "📦";
        }
    }
}

class BackgroundScrollPane extends JScrollPane {
    private Image backgroundImage;
    private float opacity = 0.12f;
    
    public BackgroundScrollPane(Component view, String imagePath) {
        super(view);
        try {
            backgroundImage = new ImageIcon(imagePath).getImage();
        } catch (Exception e) {
            backgroundImage = null;
        }
        getViewport().setOpaque(false);
        setOpaque(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(new Color(255, 243, 230));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        if (backgroundImage != null && backgroundImage.getWidth(null) > 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            
            int imgW = backgroundImage.getWidth(null);
            int imgH = backgroundImage.getHeight(null);
            
            double scaleX = (double) getWidth() / imgW;
            double scaleY = (double) getHeight() / imgH;
            double scale = Math.min(scaleX, scaleY) * 0.6;
            
            int scaledW = (int) (imgW * scale);
            int scaledH = (int) (imgH * scale);
            int x = (getWidth() - scaledW) / 2;
            int y = (getHeight() - scaledH) / 2;
            
            g2.drawImage(backgroundImage, x, y, scaledW, scaledH, null);
        } else {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 300));
            g2.setColor(new Color(255, 107, 53));
            
            FontMetrics fm = g2.getFontMetrics();
            String emoji = "🛒";
            int x = (getWidth() - fm.stringWidth(emoji)) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 50;
            
            g2.drawString(emoji, x, y);
        }
        
        g2.dispose();
        super.paintComponent(g);
    }
}

class Splash extends JWindow {
    private float opacityValue = 0f;
    private javax.swing.Timer fadeInTimer, fadeOutTimer;
    public Splash(Runnable onFinish) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, screen.width, screen.height);
        setAlwaysOnTop(true);
        setOpacity(0f);
        
        Color bgColor = new Color(255, 87, 87);
        Color lighterBg = new Color(255, 140, 105);
        
        JPanel bg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, bgColor, 0, getHeight(), lighterBg));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bg.setLayout(null);
        
        int cx = screen.width / 2;
        int cy = screen.height / 2;
        
        ImageIcon icon = null;
        try { icon = new ImageIcon("cart.png"); } catch (Exception ignored) {}
        
        JLabel iconLabel = new JLabel();
        if (icon != null && icon.getIconWidth() > 0)
            iconLabel.setIcon(new ImageIcon(icon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH)));
        else {
            iconLabel.setText("🛒");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 120));
            iconLabel.setForeground(Color.WHITE);
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        }
        iconLabel.setBounds(cx - 90, cy - 200, 180, 180);
        
        JLabel title = new JLabel("E-Commerce Shopping Cart", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setBounds(cx - 360, cy + 10, 720, 48);
        
        JProgressBar bar = new JProgressBar();
        bar.setBounds(cx - 220, cy + 90, 440, 20);
        bar.setBackground(new Color(255, 120, 120));
        bar.setForeground(new Color(255, 193, 7));
        
        JLabel loadingTxt = new JLabel("Loading...", SwingConstants.CENTER);
        loadingTxt.setForeground(Color.WHITE);
        loadingTxt.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        loadingTxt.setBounds(cx - 100, cy + 120, 200, 26);
        
        bg.add(iconLabel); bg.add(title); bg.add(bar); bg.add(loadingTxt);
        setContentPane(bg);
        
        fadeInTimer = new javax.swing.Timer(20, e -> {
            opacityValue += 0.04f;
            if (opacityValue >= 1f) { opacityValue = 1f; fadeInTimer.stop(); }
            setOpacity(Math.max(0f, Math.min(1f, opacityValue)));
        });
        
        fadeOutTimer = new javax.swing.Timer(20, e -> {
            opacityValue -= 0.04f;
            if (opacityValue <= 0f) {
                opacityValue = 0f;
                fadeOutTimer.stop();
                dispose();
                onFinish.run();
            }
            setOpacity(Math.max(0f, opacityValue));
        });
        
        fadeInTimer.start();
        
        new javax.swing.Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int v = bar.getValue();
                if (v < 100) bar.setValue(v + 1);
                else {
                    ((javax.swing.Timer) e.getSource()).stop();
                    new javax.swing.Timer(350, ev -> fadeOutTimer.start()).start();
                }
            }
        }).start();
        
        bg.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (fadeInTimer.isRunning()) fadeInTimer.stop();
                fadeOutTimer.start();
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (fadeInTimer.isRunning()) fadeInTimer.stop();
                fadeOutTimer.start();
            }
        });
    }
}

public class ShoppingCartApp extends JFrame {
    DefaultListModel<Product> electronics = new DefaultListModel<>();
    DefaultListModel<Product> clothing = new DefaultListModel<>();
    DefaultListModel<Product> groceries = new DefaultListModel<>();
    DefaultListModel<Product> books = new DefaultListModel<>();
    DefaultListModel<Product> accessories = new DefaultListModel<>();
    java.util.List<CartItem> cart = new ArrayList<>();
    
    JTable table = new JTable(new DefaultTableModel(new Object[]{"ID","Name","Qty","Price","Line"},0){
        public boolean isCellEditable(int r,int c){return false;}
    });
    JLabel subLbl = new JLabel("Subtotal: ₹0.00"), disLbl = new JLabel("Discount: -₹0.00"), totLbl = new JLabel("Total: ₹0.00");
    
    DefaultListModel<String> couponNames = new DefaultListModel<>();
    Map<String, Integer> couponAmount = new LinkedHashMap<>();
    String appliedCoupon = "";
    int appliedAmount = 0;
    
    DecimalFormat df = new DecimalFormat("#,##0.00");
    
    final Color APP_BG = new Color(255, 245, 235);
    final Color PANEL_BG = new Color(255, 250, 245);
    final Color LIST_BG = new Color(255, 243, 230);
    final Color LIST_BORDER = new Color(255, 200, 150);
    final Color TEXT_COLOR = new Color(120, 40, 31);
    final Color PRIMARY_BLUE = new Color(255, 107, 53);
    final Color SELECTION_BG = new Color(255, 179, 135);
    final Color HOVER_BLUE = new Color(255, 140, 90);
    final Color HEADER_BG = new Color(255, 87, 87);
    final Color GOLDEN_ACCENT = new Color(255, 193, 7);
    final Border ROUNDED_CARD = new LineBorder(new Color(255, 200, 150), 1, true);
    final Border SOFT_SHADOW = new MatteBorder(4, 4, 6, 4, new Color(255, 180, 120, 80));
    
    Map<String,String> users = new LinkedHashMap<>();
    File usersFile = new File("users.dat");
    String currentUser = null;
    
    private JPanel cards;
    private static final String CARD_LOGIN = "LOGIN", CARD_REGISTER = "REGISTER", CARD_MAIN = "MAIN";
    private JLayeredPane layered;
    private JPanel overlayPanel;
    private GlassPanel overlayContent;
    public ShoppingCartApp(){
        super("E-Commerce Shopping Cart");
        loadUsers();
        if(!users.containsKey("dhruv")) users.put("dhruv","Dhruv@123");
        
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(false);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);
        
        seedProducts(); 
        seedCoupons();
        setGlobalFont(new Font("Segoe UI", Font.PLAIN,13));
        
        layered = new JLayeredPane(); layered.setLayout(null);
        getContentPane().add(layered, BorderLayout.CENTER);
        getContentPane().setBackground(APP_BG);
        cards = new JPanel(new CardLayout()); 
        cards.setBackground(APP_BG);
        cards.add(buildLoginPage(), CARD_LOGIN);
        cards.add(buildRegisterPage(), CARD_REGISTER);
        cards.add(buildMainPage(), CARD_MAIN);
        layered.add(cards, JLayeredPane.DEFAULT_LAYER);
        /*setContentPane(cards);
        switchTo(CARD_LOGIN);*/
        overlayPanel = new JPanel(null); overlayPanel.setOpaque(false); overlayPanel.setVisible(false);
        layered.add(overlayPanel, JLayeredPane.PALETTE_LAYER);
        overlayContent = new GlassPanel(new Color(18,30,50,220), 18); overlayContent.setLayout(new GridBagLayout()); overlayContent.setVisible(false);
        overlayPanel.add(overlayContent);

        addComponentListener(new ComponentAdapter(){ @Override public void componentResized(ComponentEvent e){
            Dimension d = getContentPane().getSize(); cards.setBounds(0,0,d.width,d.height); overlayPanel.setBounds(0,0,d.width,d.height);
            if(overlayContent.isVisible()){ Dimension c = overlayContent.getPreferredSize(); overlayContent.setBounds((d.width-c.width)/2,(d.height-c.height)/2,c.width,c.height); }
        }@Override public void componentShown(ComponentEvent e){ Dimension d = getContentPane().getSize(); cards.setBounds(0,0,d.width,d.height); overlayPanel.setBounds(0,0,d.width,d.height); }});

        // Attempt auto-login using last_user.dat
        String last = loadLastUser();
        if(last != null && users.containsKey(last)){
            currentUser = last;
            loadCartForUser(currentUser);
            switchTo(CARD_MAIN);
            refresh();
        } else {
            switchTo(CARD_LOGIN);
        }
        
        addWindowListener(new WindowAdapter(){
            @Override public void windowClosing(WindowEvent e){
                saveUsers();
                if(currentUser != null) saveCartForUser(currentUser);
                saveLastUser(currentUser);
            }
        });
         pack();
    }
    
    private void switchTo(String key){ 
        CardLayout cl=(CardLayout)cards.getLayout(); 
        cl.show(cards,key); 
    }
    
    // Validation methods
    private boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        return username.matches("^[A-za-z0-9]+$");
    }
    
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isWhitespace(c)) {
                hasSpecial = true;
            }
        }
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecial;
    }
    
    private String getPasswordRequirements() {
        return "<html><div style='width:400px;'>" +
               "Password requirements:<br>" +
               "• At least 6 characters<br>" +
               "• At least one uppercase letter (A-Z)<br>" +
               "• At least one lowercase letter (a-z)<br>" +
               "• At least one number (0-9)<br>" +
               "• At least one special character (!@#$%^&*...)" +
               "</div></html>";
    }
    
    private String getUsernameRequirements() {
        return "<html><div style='width:400px;'>" +
               "Username requirements:<br>" +
               "• At least one uppercase letter (A-Z)<br>" +
               "• At least one lowercase letter (a-z)<br>"+
               "• Numbers (0-9)<br>" +
               "• No spaces, or special characters" +
               "</div></html>";
    }
    
    private JPanel buildLoginPage(){
        JPanel page = new JPanel(new BorderLayout()); page.setBackground(APP_BG);
        JPanel header = new JPanel(new BorderLayout()); 
        header.setBackground(HEADER_BG); 
        header.setPreferredSize(new Dimension(0,80));
        
        JLabel title = new JLabel("E-Commerce Shopping Cart", SwingConstants.CENTER); 
        title.setForeground(Color.WHITE); 
        title.setFont(new Font("Segoe UI",Font.BOLD,28)); 
        header.add(title, BorderLayout.CENTER);
        page.add(header, BorderLayout.NORTH);
        
        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);
        GlassPanel glass = new GlassPanel(new Color(255,255,255,18),20); 
        glass.setLayout(new GridBagLayout()); 
        glass.setPreferredSize(new Dimension(560,380));
        
        GridBagConstraints ig=new GridBagConstraints(); 
        ig.insets=new Insets(10,12,8,12); ig.gridx=0; ig.gridy=0; ig.gridwidth=2; ig.anchor=GridBagConstraints.CENTER;
        
        JLabel formTitle=new JLabel("Login"); 
        formTitle.setFont(new Font("Segoe UI",Font.BOLD,22)); 
        formTitle.setForeground(TEXT_COLOR); 
        glass.add(formTitle,ig);
        
        ig.gridy++; ig.gridwidth=1; ig.anchor=GridBagConstraints.WEST;
        JLabel uL=new JLabel("Username:"); uL.setForeground(TEXT_COLOR); 
        JTextField userField=new JTextField(18);
        
        // Login username hint
        userField.setForeground(Color.GRAY);
        userField.setText("Enter your username");
        userField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (userField.getText().equals("Enter your username")) {
                    userField.setText("");
                    userField.setForeground(TEXT_COLOR);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (userField.getText().isEmpty()) {
                    userField.setForeground(Color.GRAY);
                    userField.setText("Enter your username");
                }
            }
        });
        
        glass.add(uL,ig); ig.gridx=1; glass.add(userField,ig);
        
        ig.gridy++; ig.gridx=0; 
        JLabel pL=new JLabel("Password:"); pL.setForeground(TEXT_COLOR); 
        JPasswordField passField=new JPasswordField(18);
        
        // Login password hint
        passField.setForeground(Color.GRAY);
        passField.setEchoChar((char)0);
        passField.setText("Enter your password");
        passField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(passField.getPassword()).equals("Enter your password")) {
                    passField.setText("");
                    passField.setEchoChar('•');
                    passField.setForeground(TEXT_COLOR);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (passField.getPassword().length == 0) {
                    passField.setForeground(Color.GRAY);
                    passField.setEchoChar((char)0);
                    passField.setText("Enter your password");
                }
            }
        });
        
        glass.add(pL,ig); ig.gridx=1; glass.add(passField,ig);
        
        ig.gridy++; ig.gridx=0; ig.gridwidth=2; ig.anchor=GridBagConstraints.CENTER;
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER,12,0)); btnRow.setOpaque(false);
        BlueButton loginBtn=new BlueButton("Login", PRIMARY_BLUE, HOVER_BLUE, Color.WHITE);
        BlueButton regBtn=new BlueButton("Register", new Color(255,255,255,40), new Color(255,255,255,60), TEXT_COLOR);
        btnRow.add(loginBtn); btnRow.add(regBtn); glass.add(btnRow,ig);
        
        ig.gridy++; 
        JLabel tip=new JLabel("Tip: Register for New User"); 
        tip.setForeground(TEXT_COLOR); 
        glass.add(tip,ig);
        
        center.add(glass); page.add(center, BorderLayout.CENTER);
        
        loginBtn.addActionListener(e -> {
            String u = userField.getText().trim();
            if (u.equals("Enter your username")) {
                u = "";
            }
            
            String p = new String(passField.getPassword());
            if (p.equals("Enter your password")) {
                p = "";
            }
            
            if(u.isEmpty() || p.isEmpty()){ 
                JOptionPane.showMessageDialog(this,"Enter both username and password.","Missing",JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            String stored = users.get(u);
            if (stored != null && stored.equals(p)) {
                currentUser = u;
                saveLastUser(u);
                loadCartForUser(u);
                switchTo(CARD_MAIN);
                refresh();
            } else {
                JOptionPane.showMessageDialog(this,"Invalid credentials.","Login Failed",JOptionPane.ERROR_MESSAGE);
            }
        });
        
        regBtn.addActionListener(e -> switchTo(CARD_REGISTER));
        
        return page;
    }
    
    private JPanel buildRegisterPage(){
        JPanel page=new JPanel(new BorderLayout()); page.setBackground(APP_BG);
        JPanel header=new JPanel(new BorderLayout()); 
        header.setBackground(HEADER_BG); 
        header.setPreferredSize(new Dimension(0,80));
        
        JLabel title=new JLabel("Create your account",SwingConstants.CENTER); 
        title.setForeground(Color.WHITE); 
        title.setFont(new Font("Segoe UI",Font.BOLD,26)); 
        header.add(title,BorderLayout.CENTER); 
        page.add(header,BorderLayout.NORTH);
        
        JPanel center=new JPanel(new GridBagLayout()); center.setOpaque(false);
        GlassPanel glass=new GlassPanel(new Color(255,255,255,18),20); 
        glass.setPreferredSize(new Dimension(560,450)); 
        glass.setLayout(new GridBagLayout());
        
        GridBagConstraints ig=new GridBagConstraints(); 
        ig.insets=new Insets(10,12,8,12); ig.gridx=0; ig.gridy=0; ig.gridwidth=2; ig.anchor=GridBagConstraints.CENTER;
        
        JLabel formTitle=new JLabel("Register"); 
        formTitle.setFont(new Font("Segoe UI",Font.BOLD,20)); 
        formTitle.setForeground(TEXT_COLOR); 
        glass.add(formTitle,ig);
        
        ig.gridy++; ig.gridwidth=1; ig.anchor=GridBagConstraints.WEST;
        JLabel nuL=new JLabel("Username:"); nuL.setForeground(TEXT_COLOR); 
        JTextField nuField=new JTextField(18);
        
        // Add placeholder/hint for username
        nuField.setForeground(Color.GRAY);
        nuField.setText("lowercase & numbers only");
        nuField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (nuField.getText().equals("lowercase & numbers only")) {
                    nuField.setText("");
                    nuField.setForeground(TEXT_COLOR);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (nuField.getText().isEmpty()) {
                    nuField.setForeground(Color.GRAY);
                    nuField.setText("lowercase & numbers only");
                }
            }
        });
        
        glass.add(nuL,ig); ig.gridx=1; glass.add(nuField,ig);
        
        ig.gridy++; ig.gridx=0; 
        JLabel pwL=new JLabel("Password:"); pwL.setForeground(TEXT_COLOR); 
        JPasswordField pwField=new JPasswordField(18);
        
        // Add placeholder/hint for password
        pwField.setForeground(Color.GRAY);
        pwField.setEchoChar((char)0);
        pwField.setText("Min 6 chars, A-z, 0-9, !@#");
        pwField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(pwField.getPassword()).equals("Min 6 chars, A-z, 0-9, !@#")) {
                    pwField.setText("");
                    pwField.setEchoChar('•');
                    pwField.setForeground(TEXT_COLOR);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (pwField.getPassword().length == 0) {
                    pwField.setForeground(Color.GRAY);
                    pwField.setEchoChar((char)0);
                    pwField.setText("Min 6 chars, A-z, 0-9, !@#");
                }
            }
        });
        
        glass.add(pwL,ig); ig.gridx=1; glass.add(pwField,ig);
        
        ig.gridy++; ig.gridx=0; 
        JLabel cpwL=new JLabel("Confirm:"); cpwL.setForeground(TEXT_COLOR); 
        JPasswordField cpwField=new JPasswordField(18);
        
        // Add placeholder/hint for confirm password
        cpwField.setForeground(Color.GRAY);
        cpwField.setEchoChar((char)0);
        cpwField.setText("Re-enter password");
        cpwField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(cpwField.getPassword()).equals("Re-enter password")) {
                    cpwField.setText("");
                    cpwField.setEchoChar('•');
                    cpwField.setForeground(TEXT_COLOR);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (cpwField.getPassword().length == 0) {
                    cpwField.setForeground(Color.GRAY);
                    cpwField.setEchoChar((char)0);
                    cpwField.setText("Re-enter password");
                }
            }
        });
        
        glass.add(cpwL,ig); ig.gridx=1; glass.add(cpwField,ig);
        
        ig.gridy++; ig.gridx=0; ig.gridwidth=2; ig.anchor=GridBagConstraints.CENTER;
        JLabel note=new JLabel("<html><center>Password: 6+ chars, upper, lower, number, special</center></html>"); 
        note.setForeground(TEXT_COLOR); 
        note.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        glass.add(note,ig);
        
        ig.gridy++; ig.gridwidth=2; ig.anchor=GridBagConstraints.CENTER;
        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.CENTER,12,0)); btnRow.setOpaque(false);
        BlueButton regBtn=new BlueButton("Register", PRIMARY_BLUE, HOVER_BLUE, Color.WHITE);
        BlueButton backBtn=new BlueButton("Back to Login", new Color(255,255,255,40), new Color(255,255,255,60), TEXT_COLOR);
        btnRow.add(regBtn); btnRow.add(backBtn); glass.add(btnRow,ig);
        
        center.add(glass); page.add(center,BorderLayout.CENTER);
        
        regBtn.addActionListener(e -> {
            // Get text and handle placeholders
            String un = nuField.getText().trim();
            if (un.equals("lowercase & numbers only")) {
                un = "";
            }
            
            String pw = new String(pwField.getPassword());
            if (pw.equals("Min 6 chars, A-z, 0-9, !@#")) {
                pw = "";
            }
            
            String cpw = new String(cpwField.getPassword());
            if (cpw.equals("Re-enter password")) {
                cpw = "";
            }
            
            // Username validation
            if(un.isEmpty()) { 
                JOptionPane.showMessageDialog(this,
                    "Username cannot be empty.\n\n" + getUsernameRequirements(),
                    "Invalid Username",
                    JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            
            if(!isValidUsername(un)) {
                JOptionPane.showMessageDialog(this,
                    "Invalid username format.\n\n" + getUsernameRequirements(),
                    "Invalid Username",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Password validation
            if(pw.isEmpty()) { 
                JOptionPane.showMessageDialog(this,
                    "Password cannot be empty.\n\n" + getPasswordRequirements(),
                    "Invalid Password",
                    JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            
            if(!isValidPassword(pw)) {
                JOptionPane.showMessageDialog(this,
                    "Password does not meet requirements.\n\n" + getPasswordRequirements(),
                    "Weak Password",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Confirm password match
            if(!pw.equals(cpw)) { 
                JOptionPane.showMessageDialog(this,
                    "Passwords do not match.",
                    "Mismatch",
                    JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            
            // Check duplicate username
            if(users.containsKey(un)) { 
                JOptionPane.showMessageDialog(this,
                    "Username already exists. Please choose another.",
                    "Duplicate Username",
                    JOptionPane.WARNING_MESSAGE); 
                return; 
            }
            
            // Registration successful
            users.put(un, pw); 
            saveUsers(); 
            JOptionPane.showMessageDialog(this,
                "Registration successful!\n\nUsername: " + un + "\nYou can now login.",
                "Registered",
                JOptionPane.INFORMATION_MESSAGE);
            switchTo(CARD_LOGIN);
        });
        
        backBtn.addActionListener(e -> switchTo(CARD_LOGIN));
        
        return page;
    }
    
    private JPanel buildMainPage(){
        JPanel root=new JPanel(new BorderLayout()); root.setBackground(APP_BG);
        
        JPanel header=new JPanel(new BorderLayout()); 
        header.setOpaque(true); 
        header.setBackground(HEADER_BG); 
        header.setPreferredSize(new Dimension(0,72));
        
        header.add(new JLabel("   "), BorderLayout.WEST);
        
        JLabel title=new JLabel("E-Commerce Shopping Cart", SwingConstants.CENTER); 
        title.setFont(new Font("Segoe UI",Font.BOLD,20)); 
        title.setForeground(Color.WHITE); 
        title.setBorder(new EmptyBorder(10,14,10,14)); 
        header.add(title,BorderLayout.CENTER);
        
        JButton logout=new JButton("Logout"); 
        logout.addActionListener(e -> { 
            saveCartForUser(currentUser);
            currentUser=null; 
            switchTo(CARD_LOGIN); 
        }); 
        logout.setForeground(TEXT_COLOR); 
        logout.setFocusPainted(false);
        logout.setBackground(GOLDEN_ACCENT);
        logout.setBorderPainted(false);
        logout.setOpaque(true);
        logout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel rightWrap=new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
        rightWrap.setOpaque(false); 
        rightWrap.add(logout); 
        header.add(rightWrap, BorderLayout.EAST);
        
        root.add(header, BorderLayout.NORTH);
        
        JTabbedPane tabs=new JTabbedPane(); 
        tabs.setFont(tabs.getFont().deriveFont(Font.PLAIN,14f));
        tabs.setForeground(TEXT_COLOR);
        tabs.setBackground(APP_BG);
        
        tabs.addTab("📱 Electronics", buildCategoryPanel(electronics,"Electronics"));
        tabs.addTab("👕 Clothing", buildCategoryPanel(clothing,"Clothing"));
        tabs.addTab("🛒 Groceries", buildCategoryPanel(groceries,"Groceries"));
        tabs.addTab("📚 Books", buildCategoryPanel(books,"Books"));
        tabs.addTab("👜 Accessories", buildCategoryPanel(accessories,"Accessories"));
        tabs.addTab("🛍️ My Cart", buildCartPanel());
        tabs.addTab("🎫 Coupons", buildCouponsPanel());
        
        tabs.setBorder(new EmptyBorder(6,6,6,6));
        
        root.add(tabs, BorderLayout.CENTER);
        root.add(buildTotalsPanel(), BorderLayout.SOUTH);
        
        return root;
    }
    
    JPanel buildCategoryPanel(DefaultListModel<Product> model, String titleText) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(APP_BG);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_COLOR);
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(255, 200, 150), 2, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("🔍 Search:"));
        searchPanel.add(searchField);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(searchPanel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel gridContainer = new JPanel(new GridLayout(0, 4, 15, 15));
        gridContainer.setBackground(APP_BG);
        
        for (int i = 0; i < model.size(); i++) {
            Product p = model.get(i);
            ProductCard card = new ProductCard(
                p,
                () -> {
                    String qtyStr = JOptionPane.showInputDialog(this, 
                        "Add to cart: " + p.name + "\n\nEnter quantity:", "1");
                    try {
                        int qty = Integer.parseInt(qtyStr.trim());
                        if (qty > 0 && qty <= 50) {
                            addToCart(p, qty);
                            refresh();
                            msg("Added " + qty + "x " + p.name + " to cart!");
                        }
                    } catch (Exception ex) { }
                },
                () -> showBuyNowDialog(p)
            );
            gridContainer.add(card);
        }
        
        JScrollPane scrollPane = new JScrollPane(gridContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(APP_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = searchField.getText().toLowerCase();
                gridContainer.removeAll();
                
                for (int i = 0; i < model.size(); i++) {
                    Product p = model.get(i);
                    if (p.name.toLowerCase().contains(query) || 
                        String.valueOf(p.id).contains(query)) {
                        
                        ProductCard card = new ProductCard(p,
                            () -> {
                                String qtyStr = JOptionPane.showInputDialog(ShoppingCartApp.this, 
                                    "Add to cart: " + p.name + "\n\nEnter quantity:", "1");
                                try {
                                    int qty = Integer.parseInt(qtyStr.trim());
                                    if (qty > 0 && qty <= 50) {
                                        addToCart(p, qty);
                                        refresh();
                                    }
                                } catch (Exception ex) { }
                            },
                            () -> showBuyNowDialog(p)
                        );
                        gridContainer.add(card);
                    }
                }
                
                gridContainer.revalidate();
                gridContainer.repaint();
            }
        });
        
        return mainPanel;
    }
    
    private void showBuyNowDialog(Product p){
        JDialog dialog = new JDialog(this, "Buy Now", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(PANEL_BG);
        
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(PANEL_BG);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints ig = new GridBagConstraints();
        ig.insets = new Insets(10,10,10,10); 
        ig.gridx = 0; 
        ig.gridy = 0; 
        ig.gridwidth = 2; 
        ig.anchor = GridBagConstraints.CENTER;
        
        JLabel title = new JLabel("Buy Now - " + p.name);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_COLOR);
        main.add(title, ig);
        
        ig.gridy++; 
        ig.gridwidth = 1; 
        ig.anchor = GridBagConstraints.WEST;
        
        JLabel priceL = new JLabel("Price:");
        priceL.setForeground(TEXT_COLOR);
        JLabel priceV = new JLabel("₹" + df.format(p.price));
        priceV.setForeground(TEXT_COLOR);
        main.add(priceL, ig); 
        ig.gridx = 1; 
        main.add(priceV, ig);
        
        ig.gridy++; 
        ig.gridx = 0;
        JLabel qtyL = new JLabel("Quantity:");
        qtyL.setForeground(TEXT_COLOR);
        JSpinner qtySpin = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        main.add(qtyL, ig); 
        ig.gridx = 1; 
        main.add(qtySpin, ig);
        
        ig.gridy++; 
        ig.gridx = 0;
        JLabel totalL = new JLabel("Total:");
        totalL.setForeground(TEXT_COLOR);
        JLabel totalV = new JLabel("₹" + df.format(p.price));
        totalV.setForeground(TEXT_COLOR);
        main.add(totalL, ig); 
        ig.gridx = 1; 
        main.add(totalV, ig);
        
        qtySpin.addChangeListener(ev -> totalV.setText("₹" + df.format(p.price * (Integer)qtySpin.getValue())));
        
        ig.gridy++; 
        ig.gridx = 0; 
        ig.gridwidth = 2; 
        ig.anchor = GridBagConstraints.CENTER;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        
        BlueButton buyBtn = new BlueButton("Confirm Purchase", PRIMARY_BLUE, HOVER_BLUE, Color.WHITE);
        BlueButton cancelBtn = new BlueButton("Cancel", new Color(120, 120, 120), new Color(150, 150, 150), Color.WHITE);
        
        btnRow.add(buyBtn); 
        btnRow.add(cancelBtn); 
        main.add(btnRow, ig);
        
        buyBtn.addActionListener(ev -> {
            int q = (Integer)qtySpin.getValue();
            long ts = System.currentTimeMillis();
            String file = "instant_receipt_" + ts + ".txt";
            try(PrintWriter pw = new PrintWriter(new FileWriter(file))){
                pw.println("=== Instant Purchase Receipt " + ts + " ===");
                pw.println("Item: " + p.name);
                pw.println("Quantity: " + q);
                pw.println("Price per item: ₹" + df.format(p.price));
                pw.println("Total: ₹" + df.format(p.price * q));
                pw.println("\n--- No coupons applied ---");
                pw.println("\nThank you for your purchase!");
            } catch(Exception ex){
                msg("Failed to create receipt");
            }
            msg("Purchase complete!\nSaved: " + file);
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(ev -> dialog.dispose());
        
        dialog.add(main, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void openCheckoutPage() {
        if (cart.isEmpty()) {
            msg("Cart is empty");
            return;
        }
        
        double sub = 0;
        for (CartItem ci : cart) sub += ci.total();
        int discount = (appliedAmount > sub) ? (int) sub : appliedAmount;
        double total = sub - discount;
        
        final double finalSub = sub;
        final int finalDiscount = discount;
        final double finalTotal = total;
        final String finalAppliedCoupon = appliedCoupon;
        
        CheckoutSummaryPage win = new CheckoutSummaryPage(
            this,
            new ArrayList<>(cart),
            finalSub,
            finalDiscount,
            finalTotal,
            finalAppliedCoupon,
            (String addressText) -> {
                saveReceiptForCheckout(finalSub, finalDiscount, finalTotal, finalAppliedCoupon, addressText);
                saveHistoryEntry(currentUser, new ArrayList<>(cart), finalSub, finalDiscount, finalTotal, finalAppliedCoupon, addressText);
                cart.clear();
                appliedCoupon = "";
                appliedAmount = 0;
                refresh();
                msg("Order confirmed and Checked Out.\nAddress: " + addressText);
            },
            () -> { },
            APP_BG,
            PANEL_BG,
            TEXT_COLOR,
            PRIMARY_BLUE,
            HOVER_BLUE
        );
        
        win.setVisible(true);
    }
    
    private void showQuantityRemovalDialog(CartItem item) {
        JDialog dialog = new JDialog(this, "Remove from Cart", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(PANEL_BG);
        
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(PANEL_BG);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints ig = new GridBagConstraints();
        ig.insets = new Insets(10, 10, 10, 10);
        ig.gridx = 0;
        ig.gridy = 0;
        ig.gridwidth = 2;
        ig.anchor = GridBagConstraints.CENTER;
        
        JLabel title = new JLabel("Remove - " + item.p.name);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_COLOR);
        main.add(title, ig);
        
        ig.gridy++;
        ig.gridwidth = 1;
        ig.anchor = GridBagConstraints.WEST;
        
        JLabel currentQtyL = new JLabel("Current Quantity:");
        currentQtyL.setForeground(TEXT_COLOR);
        JLabel currentQtyV = new JLabel(String.valueOf(item.q));
        currentQtyV.setForeground(TEXT_COLOR);
        currentQtyV.setFont(new Font("Segoe UI", Font.BOLD, 14));
        main.add(currentQtyL, ig);
        ig.gridx = 1;
        main.add(currentQtyV, ig);
        
        ig.gridy++;
        ig.gridx = 0;
        JLabel removeQtyL = new JLabel("Quantity to Remove:");
        removeQtyL.setForeground(TEXT_COLOR);
        JSpinner qtySpin = new JSpinner(new SpinnerNumberModel(1, 1, item.q, 1));
        qtySpin.setPreferredSize(new Dimension(100, 25));
        main.add(removeQtyL, ig);
        ig.gridx = 1;
        main.add(qtySpin, ig);
        
        ig.gridy++;
        ig.gridx = 0;
        JLabel newQtyL = new JLabel("Remaining:");
        newQtyL.setForeground(TEXT_COLOR);
        JLabel newQtyV = new JLabel(String.valueOf(item.q - 1));
        newQtyV.setForeground(new Color(255, 107, 53));
        newQtyV.setFont(new Font("Segoe UI", Font.BOLD, 14));
        main.add(newQtyL, ig);
        ig.gridx = 1;
        main.add(newQtyV, ig);
        
        qtySpin.addChangeListener(ev -> {
            int removeQty = (Integer) qtySpin.getValue();
            int remaining = item.q - removeQty;
            
            if (remaining == 0) {
                newQtyV.setText("Item will be removed");
                newQtyV.setForeground(new Color(200, 50, 50));
            } else {
                newQtyV.setText(String.valueOf(remaining));
                newQtyV.setForeground(new Color(255, 107, 53));
            }
        });
        
        ig.gridy++;
        ig.gridx = 0;
        ig.gridwidth = 2;
        ig.anchor = GridBagConstraints.CENTER;
        
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        
        BlueButton removeBtn = new BlueButton("Remove", PRIMARY_BLUE, HOVER_BLUE, Color.WHITE);
        BlueButton removeAllBtn = new BlueButton("Remove All", new Color(200, 50, 50), new Color(220, 70, 70), Color.WHITE);
        BlueButton cancelBtn = new BlueButton("Cancel", new Color(120, 120, 120), new Color(150, 150, 150), Color.WHITE);
        
        btnRow.add(removeBtn);
        btnRow.add(removeAllBtn);
        btnRow.add(cancelBtn);
        main.add(btnRow, ig);
        
        removeBtn.addActionListener(ev -> {
            int removeQty = (Integer) qtySpin.getValue();
            
            if (removeQty >= item.q) {
                cart.removeIf(ci -> ci.p.id == item.p.id);
                msg("Removed all " + item.p.name + " from cart");
            } else {
                item.q -= removeQty;
                msg("Removed " + removeQty + "x " + item.p.name + " from cart");
            }
            
            refresh();
            dialog.dispose();
        });
        
        removeAllBtn.addActionListener(ev -> {
            cart.removeIf(ci -> ci.p.id == item.p.id);
            msg("Removed all " + item.p.name + " from cart");
            refresh();
            dialog.dispose();
        });
        
        cancelBtn.addActionListener(ev -> dialog.dispose());
        
        dialog.add(main, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    JPanel buildCouponsPanel(){
        JPanel wrapper=new JPanel(new BorderLayout(8,8)); 
        wrapper.setBackground(PANEL_BG); 
        wrapper.setBorder(new CompoundBorder(SOFT_SHADOW, ROUNDED_CARD));
        
        JLabel head=new JLabel("  Available Coupons"); 
        head.setOpaque(true); 
        head.setBackground(new Color(255, 220, 200)); 
        head.setForeground(TEXT_COLOR); 
        head.setFont(head.getFont().deriveFont(Font.BOLD,14f));
        head.setBorder(new CompoundBorder(new MatteBorder(0,0,1,0,new Color(255, 200, 150)), new EmptyBorder(8,10,8,10)));
        
        JList<String> list = new JList<>(couponNames); 
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
        list.setBackground(LIST_BG); 
        list.setForeground(TEXT_COLOR); 
        list.setSelectionBackground(SELECTION_BG); 
        list.setSelectionForeground(TEXT_COLOR); 
        list.setBorder(new LineBorder(LIST_BORDER,1,true));
        
        JScrollPane sp=new JScrollPane(list); 
        sp.getViewport().setBackground(LIST_BG); 
        sp.setBorder(new LineBorder(new Color(255, 200, 150),1,true));
        
        BlueButton applyBtn=new BlueButton("Apply Selected", PRIMARY_BLUE, HOVER_BLUE, Color.WHITE);
        BlueButton clearBtn=new BlueButton("Clear Coupon", PRIMARY_BLUE, HOVER_BLUE, Color.WHITE);
        
        applyBtn.addActionListener(e->{ 
            String name=list.getSelectedValue(); 
            if(name==null){ msg("Select a coupon"); return; } 
            appliedCoupon=name; 
            appliedAmount=couponAmount.getOrDefault(name,0); 
            msg("Applied: "+appliedCoupon+" (₹"+appliedAmount+" off)"); 
            refreshTotals(); 
        });
        
        clearBtn.addActionListener(e->{ appliedCoupon=""; appliedAmount=0; refreshTotals(); });
        
        JTextArea info=new JTextArea("Tip: Only one flat-amount coupon can be applied at a time."); 
        info.setEditable(false); 
        info.setLineWrap(true); 
        info.setWrapStyleWord(true); 
        info.setBackground(PANEL_BG); 
        info.setForeground(TEXT_COLOR); 
        info.setBorder(new EmptyBorder(6,6,6,6));
        
        JPanel left=new JPanel(new BorderLayout(6,6)); 
        left.setOpaque(false); 
        left.add(head,BorderLayout.NORTH); 
        left.add(sp,BorderLayout.CENTER);
        
        JPanel btns=new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        btns.setOpaque(false); 
        btns.add(applyBtn); 
        btns.add(clearBtn); 
        left.add(btns,BorderLayout.SOUTH);
        
        JPanel right=new JPanel(new BorderLayout()); 
        right.setOpaque(false); 
        right.add(new JScrollPane(info),BorderLayout.CENTER);
        
        JSplitPane split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,right); 
        split.setResizeWeight(0.6); 
        split.setBorder(null);
        
        wrapper.add(split, BorderLayout.CENTER); 
        return wrapper;
    }
    
    JPanel buildCartPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(APP_BG);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel cartTitle = new JLabel("🛍️ Shopping Cart");
        cartTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        cartTitle.setForeground(TEXT_COLOR);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(cartTitle, BorderLayout.WEST);
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(PANEL_BG);
        tablePanel.setBorder(new CompoundBorder(SOFT_SHADOW, ROUNDED_CARD));
        
        table.setBackground(LIST_BG);
        table.setForeground(TEXT_COLOR);
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(TEXT_COLOR);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(255, 220, 200));
        table.getTableHeader().setForeground(TEXT_COLOR);
        
        table.setOpaque(false);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setOpaque(false);
        table.setDefaultRenderer(Object.class, renderer);
        
        BackgroundScrollPane sp = new BackgroundScrollPane(table, "cart_background.jpg");
        sp.setBorder(new LineBorder(new Color(255, 200, 150), 1, true));
        
        tablePanel.add(sp, BorderLayout.CENTER);
        
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controls.setOpaque(false);
        
        BlueButton removeBtn = new BlueButton("Remove Selected", PRIMARY_BLUE, HOVER_BLUE, Color.WHITE);
        BlueButton clearBtn = new BlueButton("Clear Cart", new Color(255, 140, 90), new Color(255, 160, 110), Color.WHITE);
        BlueButton checkout = new BlueButton("Checkout →", new Color(255, 87, 87), new Color(255, 120, 120), Color.WHITE);
        
        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { 
                msg("Select a cart item to remove"); 
                return; 
            }
            
            int pid = Integer.parseInt(table.getValueAt(row, 0).toString());
            CartItem selectedItem = null;
            
            for (CartItem ci : cart) {
                if (ci.p.id == pid) {
                    selectedItem = ci;
                    break;
                }
            }
            
            if (selectedItem == null) return;
            
            showQuantityRemovalDialog(selectedItem);
        });
        
        clearBtn.addActionListener(e -> { cart.clear(); refresh(); });
        checkout.addActionListener(e -> openCheckoutPage());
        
        controls.add(removeBtn);
        controls.add(clearBtn);
        controls.add(Box.createHorizontalStrut(20));
        controls.add(checkout);
        
        tablePanel.add(controls, BorderLayout.SOUTH);
        
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        return mainPanel;
    }
    
    JPanel buildTotalsPanel(){
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(HEADER_BG);
        container.setBorder(new EmptyBorder(10,10,10,10));
        
        JButton historyBtn = new JButton("📜 View Past Purchases");
        historyBtn.setForeground(Color.WHITE);
        historyBtn.setBackground(new Color(255, 140, 90));
        historyBtn.setFocusPainted(false);
        historyBtn.setBorderPainted(false);
        historyBtn.setOpaque(true);
        historyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        historyBtn.addActionListener(e -> {
            if(currentUser != null) {
                new HistoryWindow(this, currentUser).setVisible(true);
            } else {
                msg("Please login first");
            }
        });
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        leftPanel.add(historyBtn);
        
        JPanel totalsPanel = new JPanel(new GridLayout(1,3,6,6));
        totalsPanel.setOpaque(false);
        
        subLbl.setForeground(Color.WHITE);
        disLbl.setForeground(Color.WHITE);
        totLbl.setForeground(Color.WHITE);
        subLbl.setFont(subLbl.getFont().deriveFont(Font.BOLD));
        disLbl.setFont(disLbl.getFont().deriveFont(Font.BOLD));
        totLbl.setFont(totLbl.getFont().deriveFont(Font.BOLD));
        
        totalsPanel.add(subLbl); 
        totalsPanel.add(disLbl); 
        totalsPanel.add(totLbl);
        
        container.add(leftPanel, BorderLayout.WEST);
        container.add(totalsPanel, BorderLayout.EAST);
        
        return container;
    }
    
    void seedProducts(){
        seedCategory("Electronics", 100, 1001, 899, 49999, electronics);
        seedCategory("Clothing",   100, 2001, 299,  2199, clothing);
        seedCategory("Groceries",  100, 3001,  49,   599, groceries);
        seedCategory("Books",      100, 4001, 199,  1499, books);
        seedCategory("Accessories",100, 5001, 149,  2999, accessories);
    }
    
    void seedCategory(String cat, int count, int startId, int minPrice, int maxPrice, DefaultListModel<Product> model){
        Random rnd = new Random(42 + startId);
        for(int i=0;i<count;i++){
            int id = startId + i;
            int price = minPrice + rnd.nextInt(Math.max(1, maxPrice - minPrice + 1));
            String name = cat+" Item "+(i+1);
            String imagePath = "images/" + cat.toLowerCase() + "/" + id + ".jpg";
            model.addElement(new Product(id, name, cat, price, imagePath));
        }
    }
    
    void seedCoupons(){
        addCoupon("WELCOME50", 50);
        addCoupon("SAVE100",   100);
        addCoupon("FEST200",   200);
        addCoupon("NEW300",    300);
        addCoupon("BIG500",    500);
    }
    
    void addCoupon(String name, int amount){ 
        couponNames.addElement(name); 
        couponAmount.put(name, amount); 
    }
    
    void addToCart(Product p,int q){
        for(CartItem ci: cart){ 
            if(ci.p.id==p.id){ 
                ci.q = Math.max(1, ci.q+q); 
                return; 
            } 
        }
        cart.add(new CartItem(p,q));
    }
    
    void refresh(){
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        m.setRowCount(0);
        for(CartItem ci: cart)
            m.addRow(new Object[]{ci.p.id, ci.p.name, ci.q, "₹"+df.format(ci.p.price), "₹"+df.format(ci.total())});
        refreshTotals();
    }
    
    void refreshTotals(){
        double sub=0; 
        for(CartItem ci: cart) sub+=ci.total();
        int discount = (appliedAmount > sub) ? (int)sub : appliedAmount;
        double total = sub - discount;
        subLbl.setText("Subtotal: ₹"+df.format(sub));
        disLbl.setText("Discount: -₹"+df.format(discount) + (appliedCoupon.isEmpty()?"":" ("+appliedCoupon+")"));
        totLbl.setText("Total: ₹"+df.format(total));
    }
    
    void msg(String s){ JOptionPane.showMessageDialog(this,s); }
    
    void loadUsers(){
        users.clear();
        users.put("Dhruv","Dhruv@123");
        try(BufferedReader br = new BufferedReader(new FileReader(usersFile))){
            String line;
            while((line = br.readLine()) != null){
                line = line.trim();
                if(line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(":",2);
                if(parts.length == 2) users.put(parts[0], parts[1]);
            }
        }catch(Exception ignore){}
    }
    
    void saveUsers(){
        try(PrintWriter pw = new PrintWriter(new FileWriter(usersFile))){
            pw.println("# username:password");
            for(Map.Entry<String,String> e: users.entrySet()){
                pw.println(e.getKey()+":"+e.getValue());
            }
        }catch(Exception ignore){}
    }
    
    String loadLastUser(){
        try(BufferedReader br = new BufferedReader(new FileReader("last_user.dat"))){
            return br.readLine();
        }catch(Exception e){ return null; }
    }
    
    void saveLastUser(String user){
        try(PrintWriter pw = new PrintWriter(new FileWriter("last_user.dat"))){
            if(user != null) pw.println(user);
        }catch(Exception ignore){}
    }
    
    void loadCartForUser(String user){
        cart.clear();
        File f = new File("cart_"+user+".dat");
        if(!f.exists()) return;
        try(BufferedReader br = new BufferedReader(new FileReader(f))){
            String line;
            while((line = br.readLine()) != null){
                String[] parts = line.split(",");
                if(parts.length == 3){
                    int id = Integer.parseInt(parts[0]);
                    int qty = Integer.parseInt(parts[2]);
                    Product p = findProductById(id);
                    if(p != null) cart.add(new CartItem(p, qty));
                }
            }
        }catch(Exception ignore){}
        refresh();
    }
    
    void saveCartForUser(String user){
        try(PrintWriter pw = new PrintWriter(new FileWriter("cart_"+user+".dat"))){
            for(CartItem ci: cart){
                pw.println(ci.p.id+","+ci.p.name+","+ci.q);
            }
        }catch(Exception ignore){}
    }
    
    Product findProductById(int id){
        DefaultListModel<Product>[] models = new DefaultListModel[]{electronics, clothing, groceries, books, accessories};
        for(DefaultListModel<Product> m : models){
            for(int i=0; i<m.size(); i++){
                Product p = m.get(i);
                if(p.id == id) return p;
            }
        }
        return null;
    }
    void saveCartOnLogout(){ if(currentUser!=null) saveCartForUser(currentUser); }
    void saveReceiptForCheckout(double sub, int discount, double total, String coupon, String address){
        long ts = System.currentTimeMillis();
        String file = "receipt_"+ts+".txt";
        try(PrintWriter pw = new PrintWriter(new FileWriter(file))){
            pw.println("=== Receipt "+ts+" ===");
            for(CartItem ci: cart) 
                pw.println(ci.p.name+" x"+ci.q+" = ₹"+df.format(ci.total()));
            pw.println("-----------------");
            pw.println("Subtotal: ₹"+df.format(sub));
            pw.println("Discount: -₹"+df.format(discount) + (coupon.isEmpty()?"":" ("+coupon+")"));
            pw.println("Total: ₹"+df.format(total));
            pw.println("\nShipping Address:");
            pw.println(address);
            pw.println("\nThank you for shopping with us!");
        }catch(Exception ex){ msg("Failed to write receipt"); }
    }
    
    void saveHistoryEntry(String user, List<CartItem> cartSnapshot, double sub, int discount, double total, String coupon, String address){
        File f = new File("history_"+user+".dat");
        try(PrintWriter pw = new PrintWriter(new FileWriter(f, true))){
            long ts = System.currentTimeMillis();
            pw.println("TIMESTAMP:"+ts);
            pw.println("DATE:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ts)));
            
            StringBuilder itemsList = new StringBuilder();
            for(int i=0; i<cartSnapshot.size(); i++){
                CartItem ci = cartSnapshot.get(i);
                if(i > 0) itemsList.append(", ");
                itemsList.append(ci.p.name).append(" x").append(ci.q);
            }
            pw.println("ITEMS:"+itemsList.toString());
            
            pw.println("SUBTOTAL:₹"+df.format(sub));
            pw.println("DISCOUNT:₹"+discount + (coupon.isEmpty()?"":" ("+coupon+")"));
            pw.println("TOTAL:₹"+df.format(total));
            pw.println("ADDRESS:"+address.replaceAll("\n", " "));
            pw.println("--------");
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    private void setGlobalFont(Font f){
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while(keys.hasMoreElements()){
            Object key = keys.nextElement();
            Object val = UIManager.get(key);
            if(val instanceof Font) UIManager.put(key, f);
        }
    }
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            ShoppingCartApp app = new ShoppingCartApp();
            Splash splash = new Splash(() -> {
                app.setVisible(true);
            });
            splash.setVisible(true);
            splash.requestFocusInWindow();
        });
    }
}

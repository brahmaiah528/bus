package gui;

import model.User;
import monitor.PassValidityMonitor;
import service.AuthService;
import service.BusPassService;
import service.PassengerService;
import service.RouteService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

/**
 * Desktop Login & User Registration Window.
 * Provides realistic authentication for Admin, Student, and Faculty accounts.
 */
public class LoginFrame extends Frame implements ActionListener {
    private final AuthService authService;
    private final PassengerService passengerService;
    private final RouteService routeService;
    private final BusPassService passService;
    private final PassValidityMonitor monitor;

    // Login Form Fields
    private TextField tfLoginUsername, tfLoginPassword;
    private Button btnLogin, btnShowRegisterTab;

    // Register Form Fields
    private TextField tfRegUsername, tfRegPassword, tfRegFullName, tfRegEmail, tfRegPassengerId;
    private Choice choiceRegRole;
    private Button btnRegister, btnShowLoginTab;

    // Container with CardLayout
    private Panel pnlCardDeck;
    private CardLayout cardLayout;
    private Label lblError;

    public LoginFrame(AuthService authService, PassengerService passengerService, 
                      RouteService routeService, BusPassService passService, PassValidityMonitor monitor) {
        super("Campus Transport Portal - Authentication & Sign In");
        this.authService = authService;
        this.passengerService = passengerService;
        this.routeService = routeService;
        this.passService = passService;
        this.monitor = monitor;

        initUI();
    }

    private void initUI() {
        setSize(480, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 247, 250));

        // Top Header
        Panel pnlHeader = new Panel(new BorderLayout());
        pnlHeader.setBackground(new Color(24, 43, 94));
        pnlHeader.setPreferredSize(new Dimension(480, 75));

        Label lblTitle = new Label("  COLLEGE TRANSPORT PORTAL", Label.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.CENTER);

        Label lblSubtitle = new Label("Secure Bus Pass Management & Issuance System", Label.CENTER);
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSubtitle.setForeground(new Color(200, 215, 245));
        pnlHeader.add(lblSubtitle, BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        // Center Card Deck (Login Card vs Register Card)
        cardLayout = new CardLayout();
        pnlCardDeck = new Panel(cardLayout);

        Panel pnlLoginCard = createLoginCard();
        Panel pnlRegisterCard = createRegisterCard();

        pnlCardDeck.add(pnlLoginCard, "LOGIN");
        pnlCardDeck.add(pnlRegisterCard, "REGISTER");

        add(pnlCardDeck, BorderLayout.CENTER);

        // Bottom Status/Error Banner
        lblError = new Label("Default Admin: admin / admin123  |  Student: aarav / student123", Label.CENTER);
        lblError.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblError.setBackground(new Color(230, 235, 245));
        lblError.setForeground(new Color(60, 60, 60));
        lblError.setPreferredSize(new Dimension(480, 30));
        add(lblError, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (monitor != null) monitor.stopMonitoring();
                dispose();
                System.exit(0);
            }
        });
    }

    private Panel createLoginCard() {
        Panel card = new Panel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);

        Panel pnlInner = new Panel(new GridLayout(6, 1, 6, 6));
        pnlInner.setBackground(Color.WHITE);

        Label lblCardTitle = new Label("Sign In to Your Account", Label.CENTER);
        lblCardTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblCardTitle.setForeground(new Color(24, 43, 94));
        pnlInner.add(lblCardTitle);

        Panel pnlUser = new Panel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        pnlUser.setBackground(Color.WHITE);
        pnlUser.add(new Label("Username:"));
        tfLoginUsername = new TextField(20);
        tfLoginUsername.setText("admin");
        pnlUser.add(tfLoginUsername);
        pnlInner.add(pnlUser);

        Panel pnlPass = new Panel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        pnlPass.setBackground(Color.WHITE);
        pnlPass.add(new Label("Password:"));
        tfLoginPassword = new TextField(20);
        tfLoginPassword.setEchoChar('*');
        tfLoginPassword.setText("admin123");
        pnlPass.add(tfLoginPassword);
        pnlInner.add(pnlPass);

        Panel pnlBtn = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        pnlBtn.setBackground(Color.WHITE);
        btnLogin = new Button("   Sign In   ");
        btnLogin.setBackground(new Color(34, 139, 34));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnLogin.addActionListener(this);
        pnlBtn.add(btnLogin);
        pnlInner.add(pnlBtn);

        Panel pnlSwitch = new Panel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        pnlSwitch.setBackground(Color.WHITE);
        pnlSwitch.add(new Label("Don't have an account?"));
        btnShowRegisterTab = new Button("Register New Account");
        btnShowRegisterTab.addActionListener(this);
        pnlSwitch.add(btnShowRegisterTab);
        pnlInner.add(pnlSwitch);

        card.add(pnlInner, BorderLayout.CENTER);
        return card;
    }

    private Panel createRegisterCard() {
        Panel card = new Panel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);

        Panel pnlInner = new Panel(new GridLayout(8, 1, 4, 4));
        pnlInner.setBackground(Color.WHITE);

        Label lblCardTitle = new Label("Create New User Account", Label.CENTER);
        lblCardTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCardTitle.setForeground(new Color(24, 43, 94));
        pnlInner.add(lblCardTitle);

        Panel p1 = new Panel(new FlowLayout(FlowLayout.CENTER));
        p1.setBackground(Color.WHITE);
        p1.add(new Label("Username: "));
        tfRegUsername = new TextField(18);
        p1.add(tfRegUsername);
        pnlInner.add(p1);

        Panel p2 = new Panel(new FlowLayout(FlowLayout.CENTER));
        p2.setBackground(Color.WHITE);
        p2.add(new Label("Password: "));
        tfRegPassword = new TextField(18);
        tfRegPassword.setEchoChar('*');
        p2.add(tfRegPassword);
        pnlInner.add(p2);

        Panel p3 = new Panel(new FlowLayout(FlowLayout.CENTER));
        p3.setBackground(Color.WHITE);
        p3.add(new Label("Full Name:"));
        tfRegFullName = new TextField(18);
        p3.add(tfRegFullName);
        pnlInner.add(p3);

        Panel p4 = new Panel(new FlowLayout(FlowLayout.CENTER));
        p4.setBackground(Color.WHITE);
        p4.add(new Label("Email ID: "));
        tfRegEmail = new TextField(18);
        p4.add(tfRegEmail);
        pnlInner.add(p4);

        Panel p5 = new Panel(new FlowLayout(FlowLayout.CENTER));
        p5.setBackground(Color.WHITE);
        p5.add(new Label("User Role:"));
        choiceRegRole = new Choice();
        choiceRegRole.add("STUDENT");
        choiceRegRole.add("FACULTY");
        choiceRegRole.add("ADMIN");
        p5.add(choiceRegRole);
        pnlInner.add(p5);

        Panel p6 = new Panel(new FlowLayout(FlowLayout.CENTER));
        p6.setBackground(Color.WHITE);
        btnRegister = new Button("Register & Create Account");
        btnRegister.setBackground(new Color(0, 102, 204));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRegister.addActionListener(this);
        p6.add(btnRegister);
        pnlInner.add(p6);

        Panel p7 = new Panel(new FlowLayout(FlowLayout.CENTER));
        p7.setBackground(Color.WHITE);
        p7.add(new Label("Already registered?"));
        btnShowLoginTab = new Button("Back to Sign In");
        btnShowLoginTab.addActionListener(this);
        p7.add(btnShowLoginTab);
        pnlInner.add(p7);

        card.add(pnlInner, BorderLayout.CENTER);
        return card;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnShowRegisterTab) {
            cardLayout.show(pnlCardDeck, "REGISTER");
            lblError.setText("Fill in all details to create a new user profile.");
            lblError.setForeground(Color.BLACK);
        } else if (src == btnShowLoginTab) {
            cardLayout.show(pnlCardDeck, "LOGIN");
            lblError.setText("Default Admin: admin / admin123  |  Student: aarav / student123");
            lblError.setForeground(Color.BLACK);
        } else if (src == btnLogin) {
            handleLogin();
        } else if (src == btnRegister) {
            handleRegister();
        }
    }

    private void handleLogin() {
        String u = tfLoginUsername.getText().trim();
        String p = tfLoginPassword.getText().trim();

        try {
            User user = authService.login(u, p);
            if (user != null) {
                lblError.setText("Login Successful! Welcome, " + user.getFullName());
                lblError.setForeground(new Color(0, 128, 0));

                // Launch main dashboard
                EventQueue.invokeLater(() -> {
                    MainFrame mainFrame = new MainFrame(passengerService, routeService, passService, monitor, user, this);
                    mainFrame.setVisible(true);
                    this.setVisible(false); // Hide login window
                });

            } else {
                lblError.setText("Invalid username or password entered.");
                lblError.setForeground(Color.RED);
            }
        } catch (Exception ex) {
            lblError.setText("Authentication error: " + ex.getMessage());
            lblError.setForeground(Color.RED);
        }
    }

    private void handleRegister() {
        String u = tfRegUsername.getText().trim();
        String p = tfRegPassword.getText().trim();
        String name = tfRegFullName.getText().trim();
        String email = tfRegEmail.getText().trim();
        String role = choiceRegRole.getSelectedItem();

        try {
            User newUser = authService.register(u, p, name, email, role, null);
            lblError.setText("Registration complete for '" + newUser.getUsername() + "'! Please Sign In.");
            lblError.setForeground(new Color(0, 128, 0));
            tfLoginUsername.setText(u);
            tfLoginPassword.setText(p);
            cardLayout.show(pnlCardDeck, "LOGIN");
        } catch (IllegalArgumentException ex) {
            lblError.setText("Validation: " + ex.getMessage());
            lblError.setForeground(Color.RED);
        } catch (SQLException ex) {
            lblError.setText("Database error: " + ex.getMessage());
            lblError.setForeground(Color.RED);
        }
    }
}

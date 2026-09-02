package gui;

import model.User;
import monitor.PassValidityMonitor;
import service.BusPassService;
import service.PassengerService;
import service.RouteService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main Application Window for Bus Pass Management System.
 * Displays logged-in user profile, role badge, and logout control.
 */
public class MainFrame extends Frame implements ActionListener {
    private final PassengerService passengerService;
    private final RouteService routeService;
    private final BusPassService passService;
    private final PassValidityMonitor monitor;
    private final User currentUser;
    private final LoginFrame parentLoginFrame;

    // CardLayout container
    private Panel pnlCardContainer;
    private CardLayout cardLayout;

    // Sub-panels
    private PassengerPanel passengerPanel;
    private RoutePanel routePanel;
    private PassPanel passPanel;
    private SearchPanel searchPanel;
    private ExpiryPanel expiryPanel;

    // Navigation buttons
    private Button btnNavPassengers, btnNavRoutes, btnNavPasses, btnNavSearch, btnNavExpiry, btnLogout;
    private Label lblSystemStatus;

    public MainFrame(PassengerService passengerService, RouteService routeService, 
                     BusPassService passService, PassValidityMonitor monitor, 
                     User currentUser, LoginFrame parentLoginFrame) {
        super("Bus Pass Management System - Logged in as " + (currentUser != null ? currentUser.getFullName() : "Admin"));
        this.passengerService = passengerService;
        this.routeService = routeService;
        this.passService = passService;
        this.monitor = monitor;
        this.currentUser = currentUser;
        this.parentLoginFrame = parentLoginFrame;

        initUI();
        initWindowEvents();
    }

    private void initUI() {
        setSize(1080, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));
        setBackground(new Color(235, 238, 242));

        // --- Top Header Bar ---
        Panel pnlTop = new Panel(new BorderLayout());
        pnlTop.setBackground(new Color(24, 43, 94));
        pnlTop.setPreferredSize(new Dimension(1080, 62));

        Label lblAppName = new Label("  COLLEGE BUS PASS MANAGEMENT SYSTEM");
        lblAppName.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblAppName.setForeground(Color.WHITE);
        pnlTop.add(lblAppName, BorderLayout.WEST);

        // User Profile Pill in Header
        Panel pnlUserBadge = new Panel(new FlowLayout(FlowLayout.RIGHT, 15, 16));
        pnlUserBadge.setBackground(new Color(24, 43, 94));

        String userDisplay = (currentUser != null) ? 
                ("User: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")") : "Role: ADMINISTRATOR";
        Label lblUser = new Label(userDisplay);
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblUser.setForeground(new Color(255, 215, 0)); // Gold text

        btnLogout = new Button("Sign Out");
        btnLogout.setBackground(new Color(178, 34, 34));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnLogout.addActionListener(this);

        pnlUserBadge.add(lblUser);
        pnlUserBadge.add(btnLogout);
        pnlTop.add(pnlUserBadge, BorderLayout.EAST);

        add(pnlTop, BorderLayout.NORTH);

        // --- Left Sidebar Navigation ---
        Panel pnlSidebar = new Panel(new GridLayout(8, 1, 6, 8));
        pnlSidebar.setBackground(new Color(45, 52, 70));
        pnlSidebar.setPreferredSize(new Dimension(210, 560));

        Label lblMenu = new Label("  DASHBOARD MENU", Label.LEFT);
        lblMenu.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblMenu.setForeground(new Color(180, 195, 220));
        pnlSidebar.add(lblMenu);

        btnNavPassengers = createNavButton("1. Passengers");
        btnNavRoutes = createNavButton("2. Bus Routes");
        btnNavPasses = createNavButton("3. Issue / Renew Pass");
        btnNavSearch = createNavButton("4. Search Routes");
        btnNavExpiry = createNavButton("5. Expiry Monitor");

        pnlSidebar.add(btnNavPassengers);
        pnlSidebar.add(btnNavRoutes);
        pnlSidebar.add(btnNavPasses);
        pnlSidebar.add(btnNavSearch);
        pnlSidebar.add(btnNavExpiry);
        pnlSidebar.add(new Label("")); // Spacer
        pnlSidebar.add(new Label("")); // Spacer

        add(pnlSidebar, BorderLayout.WEST);

        // --- Center Card Deck ---
        cardLayout = new CardLayout();
        pnlCardContainer = new Panel(cardLayout);

        passengerPanel = new PassengerPanel(this, passengerService);
        routePanel = new RoutePanel(this, routeService);
        passPanel = new PassPanel(this, passService, passengerService, routeService);
        searchPanel = new SearchPanel(this, routeService);
        expiryPanel = new ExpiryPanel(this, passService);

        pnlCardContainer.add(passengerPanel, "PASSENGERS");
        pnlCardContainer.add(routePanel, "ROUTES");
        pnlCardContainer.add(passPanel, "PASSES");
        pnlCardContainer.add(searchPanel, "SEARCH");
        pnlCardContainer.add(expiryPanel, "EXPIRY");

        add(pnlCardContainer, BorderLayout.CENTER);

        // --- Bottom Status Bar ---
        Panel pnlBottom = new Panel(new BorderLayout());
        pnlBottom.setBackground(new Color(220, 225, 235));
        pnlBottom.setPreferredSize(new Dimension(1080, 28));

        lblSystemStatus = new Label(" Status: Authenticated | Connected to SQLite Database (buspass.db) | Web Server on http://localhost:8080");
        lblSystemStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        pnlBottom.add(lblSystemStatus, BorderLayout.WEST);

        Label lblCopyright = new Label("B.Tech CSE Project | Core Java OOP & AWT  ");
        lblCopyright.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblCopyright.setForeground(new Color(100, 100, 100));
        pnlBottom.add(lblCopyright, BorderLayout.EAST);

        add(pnlBottom, BorderLayout.SOUTH);

        if (monitor != null) {
            monitor.setAlertListener((expiredCount, soonCount) -> {
                expiryPanel.updateStatusCounts(expiredCount, soonCount);
            });
        }
    }

    private Button createNavButton(String title) {
        Button btn = new Button(title);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(new Color(60, 70, 92));
        btn.setForeground(Color.WHITE);
        btn.addActionListener(this);
        return btn;
    }

    private void initWindowEvents() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnNavPassengers) {
            cardLayout.show(pnlCardContainer, "PASSENGERS");
            passengerPanel.loadPassengers();
        } else if (src == btnNavRoutes) {
            cardLayout.show(pnlCardContainer, "ROUTES");
            routePanel.loadRoutes();
        } else if (src == btnNavPasses) {
            cardLayout.show(pnlCardContainer, "PASSES");
            passPanel.loadPasses();
        } else if (src == btnNavSearch) {
            cardLayout.show(pnlCardContainer, "SEARCH");
        } else if (src == btnNavExpiry) {
            cardLayout.show(pnlCardContainer, "EXPIRY");
            expiryPanel.loadPassStatusData();
        } else if (src == btnLogout) {
            handleLogout();
        }
    }

    private void handleLogout() {
        dispose();
        if (parentLoginFrame != null) {
            parentLoginFrame.setVisible(true);
        }
    }

    private void exitApplication() {
        if (monitor != null) {
            monitor.stopMonitoring();
        }
        dispose();
        System.exit(0);
    }
}

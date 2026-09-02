package gui;

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
 * Built with Java AWT (Frame, CardLayout, BorderLayout, FlowLayout).
 * Hosts dashboard navigation and module panels.
 */
public class MainFrame extends Frame implements ActionListener {
    private final PassengerService passengerService;
    private final RouteService routeService;
    private final BusPassService passService;
    private final PassValidityMonitor monitor;

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
    private Button btnNavPassengers, btnNavRoutes, btnNavPasses, btnNavSearch, btnNavExpiry, btnNavExit;
    private Label lblSystemStatus;

    public MainFrame(PassengerService passengerService, RouteService routeService, 
                     BusPassService passService, PassValidityMonitor monitor) {
        super("Bus Pass Management System - College Transport Administration");
        this.passengerService = passengerService;
        this.routeService = routeService;
        this.passService = passService;
        this.monitor = monitor;

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
        pnlTop.setPreferredSize(new Dimension(1080, 60));

        Label lblAppName = new Label("  COLLEGE BUS PASS MANAGEMENT SYSTEM (AWT)");
        lblAppName.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblAppName.setForeground(Color.WHITE);
        pnlTop.add(lblAppName, BorderLayout.WEST);

        Label lblSub = new Label("Transport Department | Java Desktop Edition  ");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSub.setForeground(new Color(200, 215, 245));
        pnlTop.add(lblSub, BorderLayout.EAST);

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
        btnNavExit = createNavButton("6. Exit System");
        btnNavExit.setBackground(new Color(160, 40, 40));
        btnNavExit.setForeground(Color.WHITE);

        pnlSidebar.add(btnNavPassengers);
        pnlSidebar.add(btnNavRoutes);
        pnlSidebar.add(btnNavPasses);
        pnlSidebar.add(btnNavSearch);
        pnlSidebar.add(btnNavExpiry);
        pnlSidebar.add(new Label("")); // Spacer
        pnlSidebar.add(btnNavExit);

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

        lblSystemStatus = new Label(" System Status: Ready | Connected to SQLite Database (buspass.db)");
        lblSystemStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        pnlBottom.add(lblSystemStatus, BorderLayout.WEST);

        Label lblCopyright = new Label("B.Tech CSE Project | Core Java OOP & AWT  ");
        lblCopyright.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblCopyright.setForeground(new Color(100, 100, 100));
        pnlBottom.add(lblCopyright, BorderLayout.EAST);

        add(pnlBottom, BorderLayout.SOUTH);

        // Connect background monitor alert listener to GUI
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
        // Window closing adapter
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
        } else if (src == btnNavExit) {
            exitApplication();
        }
    }

    private void exitApplication() {
        if (monitor != null) {
            monitor.stopMonitoring();
        }
        dispose();
        System.out.println("Application closed cleanly.");
        System.exit(0);
    }
}

package gui;

import model.BusRoute;
import service.RouteService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * AWT Panel for Route Searching.
 * Demonstrates:
 * - Java Collections Framework (ArrayList, HashMap)
 * - Generics (List<BusRoute>, Iterator<BusRoute>)
 * - Multi-attribute case-insensitive search
 */
public class SearchPanel extends Panel implements ActionListener {
    private final RouteService routeService;
    private final Frame parentFrame;

    private TextField tfSearchQuery;
    private Button btnSearch, btnReset;
    private TextArea taSearchResults;
    private Label lblResultCount;

    public SearchPanel(Frame parent, RouteService routeService) {
        this.parentFrame = parent;
        this.routeService = routeService;
        initUI();
        performSearch(""); // Load all routes initially
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));

        // Header
        Panel pnlHeader = new Panel(new FlowLayout(FlowLayout.LEFT));
        pnlHeader.setBackground(new Color(33, 64, 154));
        Label lblTitle = new Label(" Search Bus Routes (Collection Iterator & Generics)");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Search Bar Panel
        Panel pnlSearchControl = new Panel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        pnlSearchControl.setBackground(Color.WHITE);

        Label lblPrompt = new Label("Search (Route # / Destination / Boarding Point):");
        lblPrompt.setFont(new Font("SansSerif", Font.BOLD, 12));
        pnlSearchControl.add(lblPrompt);

        tfSearchQuery = new TextField(25);
        tfSearchQuery.addActionListener(this); // Trigger search on Enter
        pnlSearchControl.add(tfSearchQuery);

        btnSearch = new Button("Search Catalogue");
        btnSearch.setBackground(new Color(0, 102, 204));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(this);
        pnlSearchControl.add(btnSearch);

        btnReset = new Button("View All");
        btnReset.addActionListener(this);
        pnlSearchControl.add(btnReset);

        add(pnlSearchControl, BorderLayout.NORTH);

        // Results Display
        Panel pnlResults = new Panel(new BorderLayout(5, 5));
        pnlResults.setBackground(Color.WHITE);

        taSearchResults = new TextArea(18, 70);
        taSearchResults.setEditable(false);
        taSearchResults.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pnlResults.add(taSearchResults, BorderLayout.CENTER);

        lblResultCount = new Label(" Showing search results...");
        lblResultCount.setBackground(new Color(230, 235, 245));
        pnlResults.add(lblResultCount, BorderLayout.SOUTH);

        // Wrap layout
        Panel pnlContainer = new Panel(new BorderLayout(10, 10));
        pnlContainer.add(pnlSearchControl, BorderLayout.NORTH);
        pnlContainer.add(pnlResults, BorderLayout.CENTER);

        add(pnlContainer, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSearch || e.getSource() == tfSearchQuery) {
            performSearch(tfSearchQuery.getText().trim());
        } else if (e.getSource() == btnReset) {
            tfSearchQuery.setText("");
            performSearch("");
        }
    }

    private void performSearch(String query) {
        // Calls RouteService which employs Iterator<BusRoute>
        List<BusRoute> results = routeService.searchRoutes(query);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-10s | %-20s | %-20s | %-20s | %-10s | %-10s%n",
                "ROUTE #", "SOURCE", "DESTINATION", "BOARDING POINT", "FARE", "AVAILABILITY"));
        sb.append("========================================================================================================\n");

        if (results.isEmpty()) {
            sb.append("\n  No bus routes found matching: '").append(query).append("'\n");
        } else {
            for (BusRoute r : results) {
                sb.append(String.format("%-10s | %-20s | %-20s | %-20s | Rs.%-7.1f | %-10s%n",
                        r.getRouteNumber(),
                        truncate(r.getSource(), 20),
                        truncate(r.getDestination(), 20),
                        truncate(r.getBoardingPoint(), 20),
                        r.getFare(),
                        (r.isAvailable() ? "ACTIVE" : "SUSPENDED")));
            }
        }

        taSearchResults.setText(sb.toString());
        lblResultCount.setText(" Found " + results.size() + " matching route(s) in in-memory collection.");
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 2) + "..";
    }
}

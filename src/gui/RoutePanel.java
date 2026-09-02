package gui;

import model.BusRoute;
import service.RouteService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

/**
 * AWT Panel for Bus Route Management.
 * Supports Route creation, status toggling, viewing, and deletion.
 */
public class RoutePanel extends Panel implements ActionListener {
    private final RouteService routeService;
    private final Frame parentFrame;

    private TextField tfRouteNumber, tfSource, tfDestination, tfBoardingPoint, tfFare;
    private Checkbox chkAvailable;
    private Button btnAddRoute, btnClear, btnRefresh, btnDelete;
    private TextArea taRouteList;
    private Label lblStatus;

    public RoutePanel(Frame parent, RouteService routeService) {
        this.parentFrame = parent;
        this.routeService = routeService;
        initUI();
        loadRoutes();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));

        // Header
        Panel pnlHeader = new Panel(new FlowLayout(FlowLayout.LEFT));
        pnlHeader.setBackground(new Color(33, 64, 154));
        Label lblTitle = new Label(" Bus Route Management & Catalog");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Center Split
        Panel pnlCenter = new Panel(new GridLayout(1, 2, 15, 15));

        // --- Left: Form ---
        Panel pnlForm = new Panel(new BorderLayout(5, 5));
        pnlForm.setBackground(Color.WHITE);

        Panel pnlInputs = new Panel(new GridLayout(6, 2, 8, 10));
        pnlInputs.setBackground(Color.WHITE);

        pnlInputs.add(new Label(" Route Number:"));
        tfRouteNumber = new TextField(12);
        pnlInputs.add(tfRouteNumber);

        pnlInputs.add(new Label(" Origin/Source:"));
        tfSource = new TextField(20);
        pnlInputs.add(tfSource);

        pnlInputs.add(new Label(" Destination:"));
        tfDestination = new TextField("College Campus", 20);
        pnlInputs.add(tfDestination);

        pnlInputs.add(new Label(" Key Boarding Point:"));
        tfBoardingPoint = new TextField(20);
        pnlInputs.add(tfBoardingPoint);

        pnlInputs.add(new Label(" Base Fare (Rs):"));
        tfFare = new TextField("30.0", 10);
        pnlInputs.add(tfFare);

        pnlInputs.add(new Label(" Active / Available:"));
        chkAvailable = new Checkbox("Route Operating", true);
        pnlInputs.add(chkAvailable);

        pnlForm.add(pnlInputs, BorderLayout.CENTER);

        // Buttons
        Panel pnlButtons = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnAddRoute = new Button("Add Route");
        btnAddRoute.setBackground(new Color(34, 139, 34));
        btnAddRoute.setForeground(Color.WHITE);
        btnAddRoute.addActionListener(this);

        btnClear = new Button("Clear");
        btnClear.addActionListener(this);

        btnDelete = new Button("Delete Route");
        btnDelete.setBackground(new Color(178, 34, 34));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(this);

        pnlButtons.add(btnAddRoute);
        pnlButtons.add(btnClear);
        pnlButtons.add(btnDelete);
        pnlForm.add(pnlButtons, BorderLayout.SOUTH);

        pnlCenter.add(pnlForm);

        // --- Right: Route List ---
        Panel pnlList = new Panel(new BorderLayout(5, 5));
        pnlList.setBackground(Color.WHITE);

        Panel pnlListTop = new Panel(new BorderLayout());
        Label lblListTitle = new Label(" Active Network Bus Routes", Label.LEFT);
        lblListTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRefresh = new Button("Refresh");
        btnRefresh.addActionListener(this);
        pnlListTop.add(lblListTitle, BorderLayout.WEST);
        pnlListTop.add(btnRefresh, BorderLayout.EAST);
        pnlList.add(pnlListTop, BorderLayout.NORTH);

        taRouteList = new TextArea(15, 45);
        taRouteList.setEditable(false);
        taRouteList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pnlList.add(taRouteList, BorderLayout.CENTER);

        pnlCenter.add(pnlList);

        add(pnlCenter, BorderLayout.CENTER);

        // Status
        lblStatus = new Label("Ready.");
        lblStatus.setBackground(new Color(230, 235, 245));
        add(lblStatus, BorderLayout.SOUTH);
    }

    public void loadRoutes() {
        List<BusRoute> list = routeService.getAllRoutes();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-8s | %-16s | %-16s | %-16s | %-8s | %-8s%n", 
                "ROUTE", "SOURCE", "DESTINATION", "BOARDING", "FARE", "STATUS"));
        sb.append("----------------------------------------------------------------------------------------\n");
        for (BusRoute r : list) {
            sb.append(String.format("%-8s | %-16s | %-16s | %-16s | Rs.%-5.1f | %-8s%n",
                    r.getRouteNumber(),
                    truncate(r.getSource(), 16),
                    truncate(r.getDestination(), 16),
                    truncate(r.getBoardingPoint(), 16),
                    r.getFare(),
                    (r.isAvailable() ? "ACTIVE" : "SUSPENDED")));
        }
        taRouteList.setText(sb.toString());
        lblStatus.setText(" Loaded " + list.size() + " routes from network catalogue.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnAddRoute) {
            handleAddRoute();
        } else if (src == btnClear) {
            clearForm();
        } else if (src == btnRefresh) {
            loadRoutes();
        } else if (src == btnDelete) {
            handleDelete();
        }
    }

    private void handleAddRoute() {
        try {
            String rNum = tfRouteNumber.getText().trim();
            String src = tfSource.getText().trim();
            String dest = tfDestination.getText().trim();
            String boarding = tfBoardingPoint.getText().trim();
            boolean avail = chkAvailable.getState();

            double fare;
            try {
                fare = Double.parseDouble(tfFare.getText().trim());
            } catch (NumberFormatException ex) {
                showDialog("Input Error", "Fare must be a valid numeric amount.");
                return;
            }

            boolean ok = routeService.addRoute(rNum, src, dest, boarding, fare, avail);
            if (ok) {
                showDialog("Success", "Route " + rNum + " created successfully!");
                clearForm();
                loadRoutes();
            }
        } catch (IllegalArgumentException ex) {
            showDialog("Validation Warning", ex.getMessage());
        } catch (SQLException ex) {
            showDialog("Database Error", "Failed to insert route: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        String rNum = tfRouteNumber.getText().trim();
        if (rNum.isEmpty()) {
            showDialog("Prompt", "Enter Route Number in the Route field to delete.");
            return;
        }
        try {
            boolean ok = routeService.deleteRoute(rNum);
            if (ok) {
                showDialog("Deleted", "Route " + rNum + " deleted successfully.");
                clearForm();
                loadRoutes();
            } else {
                showDialog("Not Found", "Route " + rNum + " not found.");
            }
        } catch (SQLException ex) {
            showDialog("Error", "Could not delete route: " + ex.getMessage());
        }
    }

    private void clearForm() {
        tfRouteNumber.setText("");
        tfSource.setText("");
        tfDestination.setText("College Campus");
        tfBoardingPoint.setText("");
        tfFare.setText("30.0");
        chkAvailable.setState(true);
        tfRouteNumber.requestFocus();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 2) + "..";
    }

    private void showDialog(String title, String message) {
        Dialog dlg = new Dialog(parentFrame, title, true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.setBackground(new Color(240, 240, 240));

        Label lbl = new Label(message, Label.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dlg.add(lbl, BorderLayout.CENTER);

        Button btnOk = new Button("OK");
        btnOk.addActionListener(ev -> dlg.dispose());
        Panel pnlBtn = new FlowLayout(FlowLayout.CENTER) == null ? new Panel() : new Panel(new FlowLayout(FlowLayout.CENTER));
        pnlBtn.add(btnOk);
        dlg.add(pnlBtn, BorderLayout.SOUTH);

        dlg.setSize(420, 150);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.setVisible(true);
    }
}

package gui;

import exception.InvalidRouteException;
import exception.PassValidityExpiredException;
import model.BusPass;
import model.BusRoute;
import model.Passenger;
import service.BusPassService;
import service.PassengerService;
import service.RouteService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.List;

/**
 * AWT Panel for Issuing, Renewing, and Cancelling Bus Passes.
 * Handles:
 * - Workflow 1: Pass Issuance (Polymorphic fee calculation + custom InvalidRouteException)
 * - Workflow 2: Pass Renewal (Validity extension + custom PassValidityExpiredException)
 * - Workflow 3: Pass Cancellation
 */
public class PassPanel extends Panel implements ActionListener, ItemListener {
    private final BusPassService passService;
    private final PassengerService passengerService;
    private final RouteService routeService;
    private final Frame parentFrame;

    // Controls for Issue Section
    private TextField tfPassengerId, tfRouteNumber, tfEstimatedFee;
    private Choice choicePassType;
    private Button btnEstimateFee, btnIssuePass;

    // Controls for Renewal / Cancellation / Route Change
    private TextField tfPassIdToManage, tfNewRouteNumber;
    private Choice choiceRenewDuration;
    private Button btnRenewPass, btnCancelPass, btnChangeRoute;

    // Table view
    private TextArea taPassList;
    private Button btnRefreshPasses;
    private Label lblStatus;

    public PassPanel(Frame parent, BusPassService passService, PassengerService passengerService, RouteService routeService) {
        this.parentFrame = parent;
        this.passService = passService;
        this.passengerService = passengerService;
        this.routeService = routeService;
        initUI();
        loadPasses();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));

        // Header
        Panel pnlHeader = new Panel(new FlowLayout(FlowLayout.LEFT));
        pnlHeader.setBackground(new Color(33, 64, 154));
        Label lblTitle = new Label(" Bus Pass Operations (Issue, Renew, Cancel)");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Center: Left (Operations) & Right (Pass Database)
        Panel pnlCenter = new Panel(new GridLayout(1, 2, 15, 15));

        // --- Left: Operations Panel (Cards / Sections) ---
        Panel pnlOps = new Panel(new GridLayout(2, 1, 10, 10));
        pnlOps.setBackground(new Color(245, 247, 250));

        // Section 1: Issue New Pass
        Panel pnlIssue = new Panel(new BorderLayout(5, 5));
        pnlIssue.setBackground(Color.WHITE);

        Label lblIssueHdr = new Label(" 1. Issue New Bus Pass", Label.LEFT);
        lblIssueHdr.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblIssueHdr.setBackground(new Color(230, 240, 255));
        pnlIssue.add(lblIssueHdr, BorderLayout.NORTH);

        Panel pnlIssueForm = new Panel(new GridLayout(4, 2, 6, 6));
        pnlIssueForm.setBackground(Color.WHITE);

        pnlIssueForm.add(new Label(" Passenger ID:"));
        tfPassengerId = new TextField(12);
        pnlIssueForm.add(tfPassengerId);

        pnlIssueForm.add(new Label(" Route Number:"));
        tfRouteNumber = new TextField(12);
        pnlIssueForm.add(tfRouteNumber);

        pnlIssueForm.add(new Label(" Duration / Type:"));
        choicePassType = new Choice();
        choicePassType.add("MONTHLY");
        choicePassType.add("SEMESTER");
        choicePassType.addItemListener(this);
        pnlIssueForm.add(choicePassType);

        pnlIssueForm.add(new Label(" Estimated Fee:"));
        tfEstimatedFee = new TextField("Click Estimate", 10);
        tfEstimatedFee.setEditable(false);
        pnlIssueForm.add(tfEstimatedFee);

        pnlIssue.add(pnlIssueForm, BorderLayout.CENTER);

        Panel pnlIssueBtns = new Panel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        btnEstimateFee = new Button("Calculate Fee");
        btnEstimateFee.addActionListener(this);
        btnIssuePass = new Button("Issue Pass");
        btnIssuePass.setBackground(new Color(34, 139, 34));
        btnIssuePass.setForeground(Color.WHITE);
        btnIssuePass.addActionListener(this);
        pnlIssueBtns.add(btnEstimateFee);
        pnlIssueBtns.add(btnIssuePass);
        pnlIssue.add(pnlIssueBtns, BorderLayout.SOUTH);

        pnlOps.add(pnlIssue);

        // Section 2: Manage Existing Pass (Renew / Cancel / Change Route)
        Panel pnlManage = new Panel(new BorderLayout(5, 5));
        pnlManage.setBackground(Color.WHITE);

        Label lblManageHdr = new Label(" 2. Renew, Cancel, or Change Route", Label.LEFT);
        lblManageHdr.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblManageHdr.setBackground(new Color(255, 243, 224));
        pnlManage.add(lblManageHdr, BorderLayout.NORTH);

        Panel pnlManageForm = new Panel(new GridLayout(3, 2, 6, 6));
        pnlManageForm.setBackground(Color.WHITE);

        pnlManageForm.add(new Label(" Pass ID (e.g. PASS-1001):"));
        tfPassIdToManage = new TextField(15);
        pnlManageForm.add(tfPassIdToManage);

        pnlManageForm.add(new Label(" Renewal Duration:"));
        choiceRenewDuration = new Choice();
        choiceRenewDuration.add("MONTHLY");
        choiceRenewDuration.add("SEMESTER");
        pnlManageForm.add(choiceRenewDuration);

        pnlManageForm.add(new Label(" Change to Route Number:"));
        tfNewRouteNumber = new TextField(12);
        pnlManageForm.add(tfNewRouteNumber);

        pnlManage.add(pnlManageForm, BorderLayout.CENTER);

        Panel pnlManageBtns = new Panel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        btnRenewPass = new Button("Renew Pass");
        btnRenewPass.setBackground(new Color(0, 102, 204));
        btnRenewPass.setForeground(Color.WHITE);
        btnRenewPass.addActionListener(this);

        btnChangeRoute = new Button("Update Route");
        btnChangeRoute.addActionListener(this);

        btnCancelPass = new Button("Cancel Pass");
        btnCancelPass.setBackground(new Color(178, 34, 34));
        btnCancelPass.setForeground(Color.WHITE);
        btnCancelPass.addActionListener(this);

        pnlManageBtns.add(btnRenewPass);
        pnlManageBtns.add(btnChangeRoute);
        pnlManageBtns.add(btnCancelPass);
        pnlManage.add(pnlManageBtns, BorderLayout.SOUTH);

        pnlOps.add(pnlManage);

        pnlCenter.add(pnlOps);

        // --- Right: Pass Registry Display ---
        Panel pnlList = new Panel(new BorderLayout(5, 5));
        pnlList.setBackground(Color.WHITE);

        Panel pnlListTop = new Panel(new BorderLayout());
        Label lblListTitle = new Label(" Issued Bus Passes Database", Label.LEFT);
        lblListTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRefreshPasses = new Button("Refresh");
        btnRefreshPasses.addActionListener(this);
        pnlListTop.add(lblListTitle, BorderLayout.WEST);
        pnlListTop.add(btnRefreshPasses, BorderLayout.EAST);
        pnlList.add(pnlListTop, BorderLayout.NORTH);

        taPassList = new TextArea(15, 45);
        taPassList.setEditable(false);
        taPassList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pnlList.add(taPassList, BorderLayout.CENTER);

        pnlCenter.add(pnlList);

        add(pnlCenter, BorderLayout.CENTER);

        // Status Label
        lblStatus = new Label("Ready.");
        lblStatus.setBackground(new Color(230, 235, 245));
        add(lblStatus, BorderLayout.SOUTH);
    }

    public void loadPasses() {
        try {
            List<BusPass> list = passService.getAllPasses();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-10s | %-8s | %-7s | %-8s | %-9s | %-11s | %-11s | %-9s%n",
                    "PASS ID", "PASSENGER", "ROUTE", "TYPE", "FEE (Rs)", "ISSUED", "EXPIRY", "STATUS"));
            sb.append("-------------------------------------------------------------------------------------------------\n");
            for (BusPass p : list) {
                sb.append(String.format("%-10s | %-8s | %-7s | %-8s | %-9.2f | %-11s | %-11s | %-9s%n",
                        p.getPassId(),
                        p.getPassengerId(),
                        p.getRouteNumber(),
                        p.getPassType(),
                        p.getFee(),
                        p.getIssueDate(),
                        p.getExpiryDate(),
                        p.getStatus()));
            }
            taPassList.setText(sb.toString());
            lblStatus.setText(" Total Passes in Record: " + list.size());
        } catch (SQLException e) {
            showDialog("Database Error", "Failed to retrieve pass records: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnEstimateFee) {
            estimateFee();
        } else if (src == btnIssuePass) {
            handleIssuePass();
        } else if (src == btnRenewPass) {
            handleRenewPass();
        } else if (src == btnCancelPass) {
            handleCancelPass();
        } else if (src == btnChangeRoute) {
            handleChangeRoute();
        } else if (src == btnRefreshPasses) {
            loadPasses();
        }
    }

    private void estimateFee() {
        String pId = tfPassengerId.getText().trim();
        String rNum = tfRouteNumber.getText().trim();
        String type = choicePassType.getSelectedItem();

        if (pId.isEmpty() || rNum.isEmpty()) {
            showDialog("Prompt", "Please enter Passenger ID and Route Number first.");
            return;
        }

        try {
            Passenger p = passengerService.getPassenger(pId);
            if (p == null) {
                showDialog("Not Found", "Passenger ID '" + pId + "' is not registered.");
                return;
            }

            BusRoute r = routeService.getRouteByNumber(rNum);
            if (r == null) {
                showDialog("Not Found", "Route '" + rNum + "' does not exist.");
                return;
            }

            // Polymorphic method execution
            double fee = p.calculatePassFee(r.getFare(), type);
            tfEstimatedFee.setText("Rs. " + String.format("%.2f", fee) + " (" + p.getPassengerType() + ")");

        } catch (SQLException ex) {
            showDialog("Error", "Error checking fee: " + ex.getMessage());
        }
    }

    private void handleIssuePass() {
        String pId = tfPassengerId.getText().trim();
        String rNum = tfRouteNumber.getText().trim();
        String type = choicePassType.getSelectedItem();

        try {
            BusPass pass = passService.issuePass(pId, rNum, type);
            showDialog("Pass Issued Successfully", 
                    "Pass #" + pass.getPassId() + " generated!\n" +
                    "Passenger: " + pass.getPassengerId() + "\n" +
                    "Route: " + pass.getRouteNumber() + " | Fee: Rs. " + pass.getFee() + "\n" +
                    "Valid from " + pass.getIssueDate() + " to " + pass.getExpiryDate());
            
            tfPassengerId.setText("");
            tfRouteNumber.setText("");
            tfEstimatedFee.setText("");
            loadPasses();

        } catch (InvalidRouteException ex) {
            // User-defined checked exception handled
            showDialog("Invalid Route Exception", "Custom Exception Caught:\n" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // Built-in exception handled
            showDialog("Validation Warning", ex.getMessage());
        } catch (SQLException ex) {
            showDialog("Database Error", "Pass issuance failed: " + ex.getMessage());
        }
    }

    private void handleRenewPass() {
        String passId = tfPassIdToManage.getText().trim();
        String duration = choiceRenewDuration.getSelectedItem();

        if (passId.isEmpty()) {
            showDialog("Prompt", "Please enter Pass ID to renew.");
            return;
        }

        try {
            BusPass renewed = passService.renewPass(passId, duration);
            showDialog("Pass Renewed",
                    "Pass #" + renewed.getPassId() + " successfully renewed!\n" +
                    "New Expiry Date: " + renewed.getExpiryDate() + "\n" +
                    "Renewal Fee: Rs. " + renewed.getFee());
            loadPasses();

        } catch (PassValidityExpiredException ex) {
            // User-defined checked exception handled
            showDialog("Pass Validity Exception", "Custom Exception Caught:\n" + ex.getMessage());
        } catch (InvalidRouteException ex) {
            showDialog("Route Exception", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showDialog("Validation Warning", ex.getMessage());
        } catch (SQLException ex) {
            showDialog("Database Error", "Renewal failed: " + ex.getMessage());
        }
    }

    private void handleCancelPass() {
        String passId = tfPassIdToManage.getText().trim();
        if (passId.isEmpty()) {
            showDialog("Prompt", "Please enter Pass ID to cancel.");
            return;
        }

        try {
            boolean cancelled = passService.cancelPass(passId);
            if (cancelled) {
                showDialog("Cancelled", "Pass #" + passId + " has been marked CANCELLED.");
                loadPasses();
            } else {
                showDialog("Error", "Pass #" + passId + " not found.");
            }
        } catch (SQLException ex) {
            showDialog("Database Error", "Cancellation failed: " + ex.getMessage());
        }
    }

    private void handleChangeRoute() {
        String passId = tfPassIdToManage.getText().trim();
        String newRoute = tfNewRouteNumber.getText().trim();

        if (passId.isEmpty() || newRoute.isEmpty()) {
            showDialog("Prompt", "Please provide both Pass ID and New Route Number.");
            return;
        }

        try {
            boolean updated = passService.changePassRoute(passId, newRoute);
            if (updated) {
                showDialog("Route Updated", "Pass #" + passId + " successfully transferred to route " + newRoute);
                loadPasses();
            }
        } catch (InvalidRouteException ex) {
            showDialog("Invalid Route", ex.getMessage());
        } catch (SQLException | IllegalArgumentException ex) {
            showDialog("Error", ex.getMessage());
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == choicePassType) {
            tfEstimatedFee.setText("Click Estimate");
        }
    }

    private void showDialog(String title, String message) {
        Dialog dlg = new Dialog(parentFrame, title, true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.setBackground(new Color(240, 240, 240));

        TextArea ta = new TextArea(message, 5, 35, TextArea.SCROLLBARS_NONE);
        ta.setEditable(false);
        ta.setBackground(new Color(240, 240, 240));
        ta.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dlg.add(ta, BorderLayout.CENTER);

        Button btnOk = new Button("OK");
        btnOk.addActionListener(ev -> dlg.dispose());
        Panel pnlBtn = new Panel(new FlowLayout(FlowLayout.CENTER));
        pnlBtn.add(btnOk);
        dlg.add(pnlBtn, BorderLayout.SOUTH);

        dlg.setSize(440, 180);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.setVisible(true);
    }
}

package gui;

import model.BusPass;
import service.BusPassService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

/**
 * AWT Panel for monitoring Expired and Expiring Bus Passes.
 * Integrates with PassValidityMonitor background thread alerts.
 */
public class ExpiryPanel extends Panel implements ActionListener {
    private final BusPassService passService;
    private final Frame parentFrame;

    private TextArea taExpiredList;
    private TextArea taExpiringSoonList;
    private Button btnRefresh;
    private Label lblMonitorStatus;

    public ExpiryPanel(Frame parent, BusPassService passService) {
        this.parentFrame = parent;
        this.passService = passService;
        initUI();
        loadPassStatusData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));

        // Header
        Panel pnlHeader = new Panel(new BorderLayout());
        pnlHeader.setBackground(new Color(33, 64, 154));

        Label lblTitle = new Label(" Pass Expiry & Validity Audit Monitor");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        btnRefresh = new Button("Refresh Expiry Audit");
        btnRefresh.addActionListener(this);
        pnlHeader.add(btnRefresh, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        // Center Split (Two viewports: Expired Passes vs Expiring Within 7 Days)
        Panel pnlCenter = new Panel(new GridLayout(2, 1, 10, 10));

        // --- Section 1: Passes Expiring Soon (Within 7 Days) ---
        Panel pnlSoon = new Panel(new BorderLayout(5, 5));
        pnlSoon.setBackground(Color.WHITE);

        Label lblSoonTitle = new Label(" Passes Expiring Soon (Within Next 7 Days)", Label.LEFT);
        lblSoonTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblSoonTitle.setBackground(new Color(255, 243, 205));
        pnlSoon.add(lblSoonTitle, BorderLayout.NORTH);

        taExpiringSoonList = new TextArea(8, 60);
        taExpiringSoonList.setEditable(false);
        taExpiringSoonList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pnlSoon.add(taExpiringSoonList, BorderLayout.CENTER);

        pnlCenter.add(pnlSoon);

        // --- Section 2: Already Expired Passes ---
        Panel pnlExpired = new Panel(new BorderLayout(5, 5));
        pnlExpired.setBackground(Color.WHITE);

        Label lblExpiredTitle = new Label(" Expired Passes (Requires Renewal or Action)", Label.LEFT);
        lblExpiredTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblExpiredTitle.setBackground(new Color(248, 215, 218));
        pnlExpired.add(lblExpiredTitle, BorderLayout.NORTH);

        taExpiredList = new TextArea(8, 60);
        taExpiredList.setEditable(false);
        taExpiredList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pnlExpired.add(taExpiredList, BorderLayout.CENTER);

        pnlCenter.add(pnlExpired);

        add(pnlCenter, BorderLayout.CENTER);

        // Footer
        lblMonitorStatus = new Label(" Background Monitor: Active");
        lblMonitorStatus.setBackground(new Color(230, 235, 245));
        add(lblMonitorStatus, BorderLayout.SOUTH);
    }

    public void updateStatusCounts(int expiredCount, int expiringSoonCount) {
        lblMonitorStatus.setText(" [Background Thread Alert] Expired: " + expiredCount + " | Expiring in 7 Days: " + expiringSoonCount);
    }

    public void loadPassStatusData() {
        try {
            // 1. Passes Expiring in 7 Days
            List<BusPass> expiringSoon = passService.getPassesExpiringSoon(7);
            StringBuilder sbSoon = new StringBuilder();
            sbSoon.append(String.format("%-10s | %-10s | %-8s | %-9s | %-12s | %-12s | %-8s%n",
                    "PASS ID", "PASSENGER", "ROUTE", "TYPE", "ISSUED", "EXPIRY", "FEE"));
            sbSoon.append("--------------------------------------------------------------------------------------\n");
            if (expiringSoon.isEmpty()) {
                sbSoon.append("No passes expiring in the next 7 days.\n");
            } else {
                for (BusPass p : expiringSoon) {
                    sbSoon.append(String.format("%-10s | %-10s | %-8s | %-9s | %-12s | %-12s | Rs.%.2f%n",
                            p.getPassId(), p.getPassengerId(), p.getRouteNumber(), p.getPassType(),
                            p.getIssueDate(), p.getExpiryDate(), p.getFee()));
                }
            }
            taExpiringSoonList.setText(sbSoon.toString());

            // 2. Already Expired Passes
            List<BusPass> expired = passService.getExpiredPasses();
            StringBuilder sbExp = new StringBuilder();
            sbExp.append(String.format("%-10s | %-10s | %-8s | %-9s | %-12s | %-12s | %-10s%n",
                    "PASS ID", "PASSENGER", "ROUTE", "TYPE", "ISSUED", "EXPIRY", "STATUS"));
            sbExp.append("--------------------------------------------------------------------------------------\n");
            if (expired.isEmpty()) {
                sbExp.append("No expired passes found in system.\n");
            } else {
                for (BusPass p : expired) {
                    sbExp.append(String.format("%-10s | %-10s | %-8s | %-9s | %-12s | %-12s | %-10s%n",
                            p.getPassId(), p.getPassengerId(), p.getRouteNumber(), p.getPassType(),
                            p.getIssueDate(), p.getExpiryDate(), p.getStatus()));
                }
            }
            taExpiredList.setText(sbExp.toString());

            lblMonitorStatus.setText(" Last Checked: " + java.time.LocalTime.now().withNano(0) +
                    " | Expired Count: " + expired.size() + " | Expiring Soon Count: " + expiringSoon.size());

        } catch (SQLException e) {
            taExpiredList.setText("Database error: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRefresh) {
            loadPassStatusData();
        }
    }
}

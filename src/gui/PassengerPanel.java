package gui;

import model.Passenger;
import service.PassengerService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.util.List;

/**
 * AWT Panel for Passenger Registration and Management.
 * Demonstrates:
 * - AWT UI controls (TextField, Button, Choice, TextArea, Panel, Label)
 * - ActionListener and ItemListener
 * - Input validation & Exception handling with AWT Dialogs
 */
public class PassengerPanel extends Panel implements ActionListener, ItemListener {
    private final PassengerService passengerService;
    private final Frame parentFrame;

    private TextField tfPassengerId, tfName, tfPhone, tfEmail, tfValidityDays;
    private Choice choiceType;
    private Button btnRegister, btnClear, btnRefreshList, btnDelete;
    private TextArea taPassengerList;
    private Label lblStatus;

    public PassengerPanel(Frame parent, PassengerService passengerService) {
        this.parentFrame = parent;
        this.passengerService = passengerService;
        initUI();
        loadPassengers();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));

        // Title Header
        Panel pnlHeader = new Panel(new FlowLayout(FlowLayout.LEFT));
        pnlHeader.setBackground(new Color(33, 64, 154));
        Label lblTitle = new Label(" Passenger Registration & Management");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle);
        add(pnlHeader, BorderLayout.NORTH);

        // Form & Table Split
        Panel pnlCenter = new Panel(new GridLayout(1, 2, 15, 15));

        // --- Left Side: Form Panel ---
        Panel pnlForm = new Panel(new BorderLayout(5, 5));
        pnlForm.setBackground(Color.WHITE);

        Panel pnlInputs = new Panel(new GridLayout(6, 2, 8, 10));
        pnlInputs.setBackground(Color.WHITE);

        pnlInputs.add(new Label(" Passenger ID:"));
        tfPassengerId = new TextField(15);
        pnlInputs.add(tfPassengerId);

        pnlInputs.add(new Label(" Full Name:"));
        tfName = new TextField(20);
        pnlInputs.add(tfName);

        pnlInputs.add(new Label(" Passenger Type:"));
        choiceType = new Choice();
        choiceType.add("STUDENT");
        choiceType.add("FACULTY");
        choiceType.addItemListener(this);
        pnlInputs.add(choiceType);

        pnlInputs.add(new Label(" Phone (10 digits):"));
        tfPhone = new TextField(12);
        pnlInputs.add(tfPhone);

        pnlInputs.add(new Label(" Email Address:"));
        tfEmail = new TextField(25);
        pnlInputs.add(tfEmail);

        pnlInputs.add(new Label(" Validity (Days):"));
        tfValidityDays = new TextField("180", 6);
        pnlInputs.add(tfValidityDays);

        pnlForm.add(pnlInputs, BorderLayout.CENTER);

        // Button bar
        Panel pnlButtons = new Panel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnRegister = new Button("Register Passenger");
        btnRegister.setBackground(new Color(34, 139, 34));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.addActionListener(this);

        btnClear = new Button("Clear Form");
        btnClear.addActionListener(this);

        btnDelete = new Button("Delete Selected ID");
        btnDelete.setBackground(new Color(178, 34, 34));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(this);

        pnlButtons.add(btnRegister);
        pnlButtons.add(btnClear);
        pnlButtons.add(btnDelete);
        pnlForm.add(pnlButtons, BorderLayout.SOUTH);

        pnlCenter.add(pnlForm);

        // --- Right Side: Registered Passenger List ---
        Panel pnlList = new Panel(new BorderLayout(5, 5));
        pnlList.setBackground(Color.WHITE);

        Panel pnlListTop = new Panel(new BorderLayout());
        Label lblListTitle = new Label(" Registered Passengers Database", Label.LEFT);
        lblListTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRefreshList = new Button("Refresh");
        btnRefreshList.addActionListener(this);
        pnlListTop.add(lblListTitle, BorderLayout.WEST);
        pnlListTop.add(btnRefreshList, BorderLayout.EAST);
        pnlList.add(pnlListTop, BorderLayout.NORTH);

        taPassengerList = new TextArea(15, 45);
        taPassengerList.setEditable(false);
        taPassengerList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pnlList.add(taPassengerList, BorderLayout.CENTER);

        pnlCenter.add(pnlList);

        add(pnlCenter, BorderLayout.CENTER);

        // Bottom Status Label
        lblStatus = new Label("Ready.");
        lblStatus.setBackground(new Color(230, 235, 245));
        lblStatus.setForeground(new Color(30, 30, 30));
        add(lblStatus, BorderLayout.SOUTH);
    }

    public void loadPassengers() {
        try {
            List<Passenger> list = passengerService.getAllPassengers();
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%-10s | %-18s | %-8s | %-12s | %-20s%n", "ID", "NAME", "TYPE", "PHONE", "EMAIL"));
            sb.append("--------------------------------------------------------------------------------\n");
            for (Passenger p : list) {
                sb.append(String.format("%-10s | %-18s | %-8s | %-12s | %-20s%n",
                        p.getPassengerId(),
                        truncate(p.getName(), 18),
                        p.getPassengerType(),
                        p.getPhone(),
                        truncate(p.getEmail(), 20)));
            }
            taPassengerList.setText(sb.toString());
            lblStatus.setText(" Loaded " + list.size() + " passenger records.");
        } catch (SQLException e) {
            showDialog("Database Error", "Failed to load passengers: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == btnRegister) {
            handleRegister();
        } else if (src == btnClear) {
            clearForm();
        } else if (src == btnRefreshList) {
            loadPassengers();
        } else if (src == btnDelete) {
            handleDelete();
        }
    }

    private void handleRegister() {
        try {
            String id = tfPassengerId.getText().trim();
            String name = tfName.getText().trim();
            String type = choiceType.getSelectedItem();
            String phone = tfPhone.getText().trim();
            String email = tfEmail.getText().trim();

            int validity;
            try {
                validity = Integer.parseInt(tfValidityDays.getText().trim());
            } catch (NumberFormatException ex) {
                showDialog("Input Error", "Validity Days must be a valid positive integer.");
                return;
            }

            boolean success = passengerService.registerPassenger(id, name, phone, email, type, validity);
            if (success) {
                showDialog("Success", "Passenger '" + name + "' (" + type + ") registered successfully!");
                clearForm();
                loadPassengers();
            }

        } catch (IllegalArgumentException ex) {
            showDialog("Validation Warning", ex.getMessage());
        } catch (SQLException ex) {
            showDialog("Database Error", "Registration failed: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        String id = tfPassengerId.getText().trim();
        if (id.isEmpty()) {
            showDialog("Prompt", "Please enter the Passenger ID in the ID field to delete.");
            return;
        }
        try {
            boolean deleted = passengerService.deletePassenger(id);
            if (deleted) {
                showDialog("Deleted", "Passenger " + id + " deleted successfully.");
                clearForm();
                loadPassengers();
            } else {
                showDialog("Not Found", "Passenger with ID " + id + " does not exist.");
            }
        } catch (SQLException ex) {
            showDialog("Error", "Could not delete passenger: " + ex.getMessage());
        }
    }

    private void clearForm() {
        tfPassengerId.setText("");
        tfName.setText("");
        tfPhone.setText("");
        tfEmail.setText("");
        tfValidityDays.setText(choiceType.getSelectedItem().equals("STUDENT") ? "180" : "365");
        tfPassengerId.requestFocus();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == choiceType) {
            if ("STUDENT".equals(choiceType.getSelectedItem())) {
                tfValidityDays.setText("180");
            } else {
                tfValidityDays.setText("365");
            }
        }
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
        Panel pnlBtn = new Panel(new FlowLayout(FlowLayout.CENTER));
        pnlBtn.add(btnOk);
        dlg.add(pnlBtn, BorderLayout.SOUTH);

        dlg.setSize(420, 150);
        dlg.setLocationRelativeTo(parentFrame);
        dlg.setVisible(true);
    }
}

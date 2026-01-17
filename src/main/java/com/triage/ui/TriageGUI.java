package com.triage.ui;

import com.triage.model.Patient;
import com.triage.model.Symptom;
import com.triage.model.TriageResult;
import com.triage.service.RuleGenerationService;
import com.triage.service.TriageService;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

@Component
public class TriageGUI extends JFrame {

    private final TriageService triageService;
    private final RuleGenerationService ruleGenerationService;

    private JComboBox<String> symptomDropdown;
    private DefaultListModel<String> selectedSymptomsModel;
    private JTextField ageField;
    private JTextArea resultArea;
    private JLabel statusLabel;

    public TriageGUI(TriageService triageService, RuleGenerationService ruleGenerationService) {
        this.triageService = triageService;
        this.ruleGenerationService = ruleGenerationService;

        setTitle("Irish Children's Triage System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Setup
        JPanel setupPanel = createSetupPanel();
        tabbedPane.addTab("Setup", setupPanel);

        // Tab 2: Simulation
        JPanel simulationPanel = createSimulationPanel();
        tabbedPane.addTab("Triage Simulation", simulationPanel);

        add(tabbedPane);

        // Initial status check
        if (ruleGenerationService.areRulesOutdated()) {
            statusLabel.setText("Status: Rules match PDF? No (Outdated or Missing)");
        } else {
            statusLabel.setText("Status: Rules match PDF? Yes");
        }

        // Populate symptoms if ready
        refreshSymptoms();
    }

    private JPanel createSetupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new GridBagLayout());

        JButton generateButton = new JButton("Generate Rules from PDF");
        generateButton.setFont(new Font("Arial", Font.BOLD, 16));

        statusLabel = new JLabel("Status: Checking...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        generateButton.addActionListener(e -> {
            try {
                statusLabel.setText("Status: Generating rules...");
                // Run in background to not freeze UI
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        ruleGenerationService.generateRules();
                        triageService.init(); // Re-initialize engine
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            statusLabel.setText("Status: Rules Generated Successfully.");
                            JOptionPane.showMessageDialog(TriageGUI.this, "Rules generated successfully!", "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                            refreshSymptoms();
                        } catch (Exception ex) {
                            statusLabel.setText("Status: Error generating rules.");
                            JOptionPane.showMessageDialog(TriageGUI.this, "Error: " + ex.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            ex.printStackTrace();
                        }
                    }
                };
                worker.execute();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        centerPanel.add(generateButton, gbc);

        gbc.gridy = 1;
        centerPanel.add(statusLabel, gbc);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSimulationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Input
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        inputPanel.add(new JLabel("Patient Age:"));
        ageField = new JTextField();
        inputPanel.add(ageField);

        inputPanel.add(new JLabel("Symptom:"));
        symptomDropdown = new JComboBox<>();
        inputPanel.add(symptomDropdown);

        JButton addSymptomButton = new JButton("Add Symptom");
        addSymptomButton.addActionListener(e -> {
            String selected = (String) symptomDropdown.getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                if (!selectedSymptomsModel.contains(selected)) {
                    selectedSymptomsModel.addElement(selected);
                }
            }
        });
        inputPanel.add(new JLabel("")); // spacer
        inputPanel.add(addSymptomButton);

        panel.add(inputPanel, BorderLayout.NORTH);

        // Center: Lists
        selectedSymptomsModel = new DefaultListModel<>();
        JList<String> symptomList = new JList<>(selectedSymptomsModel);
        panel.add(new JScrollPane(symptomList), BorderLayout.CENTER);

        // Bottom: Actions & Result
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton clearButton = new JButton("Clear Symptoms");
        clearButton.addActionListener(e -> {
            selectedSymptomsModel.clear();
            ageField.setText("");
            resultArea.setText("");
            resultArea.setBackground(Color.WHITE);
        });

        JButton triageButton = new JButton("Triage Patient");
        triageButton.setFont(new Font("Arial", Font.BOLD, 14));
        triageButton.addActionListener(e -> performTriage());

        JButton saveButton = new JButton("Save Output");
        saveButton.addActionListener(e -> saveOutput());

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(clearButton);
        buttonPanel.add(triageButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(exitButton);

        bottomPanel.add(buttonPanel, BorderLayout.NORTH);

        resultArea = new JTextArea(3, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.BOLD, 16));
        resultArea.setBorder(BorderFactory.createTitledBorder("Triage Result"));

        bottomPanel.add(resultArea, BorderLayout.CENTER);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshSymptoms() {
        triageService.init(); // Ensure initialized
        Set<String> symptoms = triageService.getAvailableSymptoms();
        symptomDropdown.removeAllItems();
        for (String s : symptoms) {
            symptomDropdown.addItem(s);
        }
    }

    private void performTriage() {
        try {
            String ageStr = ageField.getText().trim();
            if (ageStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter age.", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            int age = Integer.parseInt(ageStr);

            Patient patient = new Patient("GUI_Pat", age);
            for (int i = 0; i < selectedSymptomsModel.size(); i++) {
                patient.addSymptom(new Symptom(selectedSymptomsModel.get(i)));
            }

            TriageResult result = triageService.triage(patient);

            resultArea.setText(result.toString());

            // Color coding
            String color = result.getColor();
            if (color != null) {
                switch (color.toLowerCase()) {
                    case "red":
                        resultArea.setBackground(Color.RED);
                        break;
                    case "orange":
                        resultArea.setBackground(Color.ORANGE);
                        break;
                    case "yellow":
                        resultArea.setBackground(Color.YELLOW);
                        break;
                    case "green":
                        resultArea.setBackground(Color.GREEN);
                        break;
                    case "blue":
                        resultArea.setBackground(Color.CYAN);
                        break;
                    default:
                        resultArea.setBackground(Color.WHITE);
                }
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Age. Must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Triage Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void saveOutput() {
        String result = resultArea.getText();
        if (result == null || result.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No result to save.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try (FileWriter fw = new FileWriter("triage_result.txt", true)) {
            fw.write("Date: " + java.time.LocalDateTime.now() + "\n");
            fw.write("Patient Age: " + ageField.getText() + "\n");
            fw.write("Symptoms: " + selectedSymptomsModel.toString() + "\n");
            fw.write("Result: " + result + "\n");
            fw.write("--------------------------------------------------\n");
            JOptionPane.showMessageDialog(this, "Saved to triage_result.txt", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

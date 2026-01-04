package com.triage;

import com.triage.model.Patient;
import com.triage.model.Symptom;
import com.triage.model.TriageResult;
import com.triage.rules.TriageEngine;
import com.triage.rules.TriageRuleGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class Main {
    private static TriageEngine engine;
    private static final String PDF_PATH = "spec/national-emergency-medicine-programme-irish-childrens-triage-system-icts.pdf";
    private static final String DRL_PATH = "generated_rules/triage.drl";

    public static void main(String[] args) {
        // CLI argument support for headless generation (backwards compatibility)
        if (args.length > 0 && args[0].equalsIgnoreCase("generate")) {
            generateRules(null, false);
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Irish Children's Triage System (Console) ===");

        // Try to init engine on startup
        initEngine(false);

        // Initial check for outdated rules
        checkRulesStatus();

        boolean running = true;
        while (running) {
            System.out.println("\nMenu:");
            System.out.println("1. Triage Patient");
            System.out.println("2. Generate Rules from PDF");
            System.out.println("3. List Available Symptoms");
            System.out.println("4. Exit");
            System.out.print("Select option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    triagePatient(scanner);
                    break;
                case "2":
                    generateRules(scanner, true);
                    // Re-init engine after generation
                    initEngine(true);
                    break;
                case "3":
                    listSymptoms();
                    break;
                case "4":
                    running = false;
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
        scanner.close();
    }

    private static void checkRulesStatus() {
        File pdf = new File(PDF_PATH);
        File drl = new File(DRL_PATH);

        if (!pdf.exists()) {
            System.out.println("WARNING: Source PDF not found at " + PDF_PATH);
            return;
        }

        if (!drl.exists()) {
            System.out.println("NOTE: Rules file not found. Please generate rules (Option 2).");
            return;
        }

        if (pdf.lastModified() > drl.lastModified()) {
            System.out.println("WARNING: Rules are outdated relative to the PDF. Please regenerate rules (Option 2).");
        }
    }

    private static void initEngine(boolean forceMsg) {
        try {
            engine = new TriageEngine();
            engine.init();
            if (forceMsg)
                System.out.println("Triage Engine Initialized.");
        } catch (Exception e) {
            System.out.println("Engine not initialized (Rules might be missing). Please generate rules first.");
            if (forceMsg)
                System.out.println("Error detail: " + e.getMessage());
            engine = null;
        }
    }

    private static void generateRules(Scanner scanner, boolean interactive) {
        if (interactive && scanner != null) {
            File pdf = new File(PDF_PATH);
            File drl = new File(DRL_PATH);
            if (pdf.exists() && drl.exists() && pdf.lastModified() < drl.lastModified()) {
                System.out.print("Rules appear to be up-to-date. Regenerate anyway? (y/n): ");
                String resp = scanner.nextLine().trim();
                if (!resp.equalsIgnoreCase("y")) {
                    System.out.println("Cancelled generation.");
                    return;
                }
            }
        }

        System.out.println("Generating rules via CLI...");
        TriageRuleGenerator generator = new TriageRuleGenerator();
        try {
            generator.generate(PDF_PATH, DRL_PATH);
            System.out.println("Rules generated successfully to " + DRL_PATH);
        } catch (Exception e) {
            System.err.println("Error generating rules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void listSymptoms() {
        File drlFile = new File(DRL_PATH);
        if (!drlFile.exists()) {
            System.out.println("Rules file not found. Generate rules first.");
            return;
        }

        Set<String> symptoms = new TreeSet<>();
        try (Scanner fileScanner = new Scanner(drlFile)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.startsWith("Symptom( name == \"")) {
                    int start = "Symptom( name == \"".length();
                    int end = line.lastIndexOf("\"");
                    if (end > start) {
                        symptoms.add(line.substring(start, end));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading rules file: " + e.getMessage());
            return;
        }

        System.out.println("\n--- Available Symptoms ---");
        for (String s : symptoms) {
            System.out.println("- " + s);
        }
        System.out.println("--------------------------");
    }

    private static void triagePatient(Scanner scanner) {
        if (engine == null) {
            System.out.println("Error: Engine is not initialized. Please generate rules first (Option 2).");
            return;
        }

        try {
            System.out.print("Enter Patient Age: ");
            String ageStr = scanner.nextLine().trim();
            int age = Integer.parseInt(ageStr);

            Patient patient = new Patient("Console_Pat", age);

            System.out.println("Enter Symptoms (comma separated, e.g. 'Stridor, Shock'): ");
            String symptomsStr = scanner.nextLine().trim();
            if (!symptomsStr.isEmpty()) {
                String[] parts = symptomsStr.split(",");
                for (String p : parts) {
                    patient.addSymptom(new Symptom(p.trim()));
                }
            }

            TriageResult result = engine.executeTriage(patient);
            System.out.println(">>> Triage Result: " + result.toString());

        } catch (NumberFormatException e) {
            System.out.println("Invalid Age entered.");
        } catch (Exception e) {
            System.out.println("Error during triage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

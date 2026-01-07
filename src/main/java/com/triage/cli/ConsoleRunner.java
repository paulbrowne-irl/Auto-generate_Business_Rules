package com.triage.cli;

import com.triage.model.Patient;
import com.triage.model.Symptom;
import com.triage.model.TriageResult;
import com.triage.service.RuleGenerationService;
import com.triage.service.TriageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Scanner;
import java.util.Set;

@Component
public class ConsoleRunner implements CommandLineRunner {

    private final TriageService triageService;
    private final RuleGenerationService ruleService;

    public ConsoleRunner(TriageService triageService, RuleGenerationService ruleService) {
        this.triageService = triageService;
        this.ruleService = ruleService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Headless generation check
        if (args.length > 0 && args[0].equalsIgnoreCase("generate")) {
            ruleService.generateRules();
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Irish Children's Triage System (Spring Boot Console) ===");

        triageService.init();
        if (triageService.isInitialized()) {
            System.out.println("Triage Engine Initialized.");
        } else {
            System.out.println("Engine not initialized (Rules might be missing). Please generate rules.");
        }

        if (ruleService.areRulesOutdated()) {
            System.out.println("WARNING: Rules are outdated relative to the PDF. Please regenerate rules.");
        }

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
                    generateRules(scanner);
                    triageService.init(); // Re-init
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
        // scanner.close(); // Spring Boot likely keeps running or System.in closed;
        // careful closing system.in
    }

    private void generateRules(Scanner scanner) {
        if (!ruleService.areRulesOutdated()) {
            System.out.print("Rules appear to be up-to-date. Regenerate anyway? (y/n): ");
            String resp = scanner.nextLine().trim();
            if (!resp.equalsIgnoreCase("y")) {
                System.out.println("Cancelled generation.");
                return;
            }
        }

        System.out.println("Generating rules...");
        try {
            ruleService.generateRules();
            System.out.println("Rules generated successfully.");
        } catch (Exception e) {
            System.err.println("Error generating rules: " + e.getMessage());
        }
    }

    private void listSymptoms() {
        Set<String> symptoms = triageService.getAvailableSymptoms();
        if (symptoms.isEmpty()) {
            System.out.println("No symptoms found or rules missing.");
            return;
        }
        System.out.println("\n--- Available Symptoms ---");
        for (String s : symptoms) {
            System.out.println("- " + s);
        }
        System.out.println("--------------------------");
    }

    private void triagePatient(Scanner scanner) {
        if (!triageService.isInitialized()) {
            System.out.println("Error: Engine is not initialized. Please generate rules first.");
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

            TriageResult result = triageService.triage(patient);
            System.out.println(">>> Triage Result: " + result.toString());

        } catch (NumberFormatException e) {
            System.out.println("Invalid Age entered.");
        } catch (Exception e) {
            System.out.println("Error during triage: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

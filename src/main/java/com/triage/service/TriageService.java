package com.triage.service;

import com.triage.model.Patient;
import com.triage.model.TriageResult;
import com.triage.rules.TriageEngine;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

@Service
public class TriageService {

    private TriageEngine engine;
    private final RuleGenerationService ruleService;

    public TriageService(RuleGenerationService ruleService) {
        this.ruleService = ruleService;
    }

    public void init() {
        try {
            engine = new TriageEngine();
            engine.init();
        } catch (Exception e) {
            // Log or handle silent failure - engine might be null if rules are missing
            engine = null;
        }
    }

    public boolean isInitialized() {
        return engine != null;
    }

    public TriageResult triage(Patient patient) {
        if (engine == null) {
            throw new IllegalStateException("Triage Engine not initialized. Please generate rules.");
        }
        return engine.executeTriage(patient);
    }

    public Set<String> getAvailableSymptoms() {
        Set<String> symptoms = new TreeSet<>();
        File drlFile = new File(ruleService.getDrlPath());

        if (!drlFile.exists()) {
            return symptoms;
        }

        try (Scanner fileScanner = new Scanner(drlFile)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                // Match: Symptom( name == "..." )
                if (line.startsWith("Symptom( name == \"")) {
                    int start = "Symptom( name == \"".length();
                    int end = line.lastIndexOf("\"");
                    if (end > start) {
                        symptoms.add(line.substring(start, end));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // Should not happen due to check above
        }
        return symptoms;
    }
}

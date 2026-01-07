package com.triage.controller;

import com.triage.model.Patient;
import com.triage.model.TriageResult;
import com.triage.service.TriageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api")
public class TriageController {

    private final TriageService triageService;

    public TriageController(TriageService triageService) {
        this.triageService = triageService;
    }

    @GetMapping("/symptoms")
    public ResponseEntity<Set<String>> getSymptoms() {
        return ResponseEntity.ok(triageService.getAvailableSymptoms());
    }

    @PostMapping("/triage")
    public ResponseEntity<?> triagePatient(@RequestBody Patient patient) {
        if (!triageService.isInitialized()) {
            return ResponseEntity.status(503).body("Triage engine not initialized. Please generate rules.");
        }
        try {
            TriageResult result = triageService.triage(patient);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing triage: " + e.getMessage());
        }
    }
}

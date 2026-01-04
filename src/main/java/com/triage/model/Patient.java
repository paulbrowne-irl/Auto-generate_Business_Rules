package com.triage.model;

import java.util.ArrayList;
import java.util.List;

public class Patient {
    private String id;
    private int age; // in years, or months? Let's assume years for simplicity, or handle both.
    private List<Symptom> symptoms;

    public Patient(String id, int age) {
        this.id = id;
        this.age = age;
        this.symptoms = new ArrayList<>();
    }

    public void addSymptom(Symptom symptom) {
        this.symptoms.add(symptom);
    }

    public List<Symptom> getSymptoms() {
        return symptoms;
    }

    public int getAge() {
        return age;
    }

    // Getters and setters
    public String getId() { return id; }
}

package com.triage.model;

import java.util.ArrayList;
import java.util.List;

public class Patient {
    private String id;
    private int age;
    private List<Symptom> symptoms;

    public Patient() {
        this.symptoms = new ArrayList<>();
    }

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

    public void setSymptoms(List<Symptom> symptoms) {
        this.symptoms = symptoms;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

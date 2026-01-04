package com.triage.model;

public class Symptom {
    private String name;
    private String stringValue;
    private Double numericValue;

    public Symptom(String name) {
        this.name = name;
    }

    public Symptom(String name, String stringValue) {
        this.name = name;
        this.stringValue = stringValue;
    }

    public Symptom(String name, Double numericValue) {
        this.name = name;
        this.numericValue = numericValue;
    }

    public String getName() { return name; }
    public String getStringValue() { return stringValue; }
    public Double getNumericValue() { return numericValue; }

    @Override
    public String toString() {
        if (numericValue != null) return name + ": " + numericValue;
        if (stringValue != null) return name + ": " + stringValue;
        return name;
    }
}

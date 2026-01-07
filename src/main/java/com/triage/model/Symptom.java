package com.triage.model;

public class Symptom {
    private String name;
    private String stringValue;
    private Double numericValue;

    public Symptom() {
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Double getNumericValue() {
        return numericValue;
    }

    public void setNumericValue(Double numericValue) {
        this.numericValue = numericValue;
    }

    @Override
    public String toString() {
        if (numericValue != null)
            return name + ": " + numericValue;
        if (stringValue != null)
            return name + ": " + stringValue;
        return name;
    }
}

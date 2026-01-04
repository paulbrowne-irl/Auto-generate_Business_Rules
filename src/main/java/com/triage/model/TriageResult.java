package com.triage.model;

public class TriageResult {
    private String color; // Red, Orange, Yellow, Green, Blue
    private int priority; // 1=Red, 5=Blue

    public TriageResult() {
    }

    public TriageResult(String color) {
        setColor(color);
    }

    public void setColor(String color) {
        this.color = color;
        switch (color.toLowerCase()) {
            case "red":
                priority = 1;
                break;
            case "orange":
                priority = 2;
                break;
            case "yellow":
                priority = 3;
                break;
            case "green":
                priority = 4;
                break;
            case "blue":
                priority = 5;
                break;
            default:
                priority = 5;
        }
    }

    public String getColor() {
        return color;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "Triage Result: " + color + " (Priority " + priority + ")";
    }
}

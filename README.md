# Irish Children's Triage System (ICTS) - GUI Edition

## Overview

A Java-based desktop application (Swing) that digitizes the National Emergency Medicine Programme Irish Children’s Triage System (ICTS). It parses PDF guidelines to generate executable business rules and provides a user-friendly GUI for triaging patients.

## Features

*   **PDF Rule Generation**: Automatically extracts triage rules from the ICTS PDF (`spec/national-emergency-medicine-programme-irish-childrens-triage-system-icts.pdf`).
*   **GUI Interface**: User-friendly Swing interface for:
    *   Generating rules.
    *   Simulating patient triage interactively.
    *   Visualizing results (Color-coded).
*   **Drools Rule Engine**: Uses Apache KIE (Drools) to execute the generated rules logic.

## Prerequisites

*   **Java 21**
*   **Maven**

## Getting Started

### 1. Build the Application
```bash
mvn clean package
```

### 2. Run the Application
Start the application from the command line (or double-click the jar if associated):
```bash
java -jar target/triage-system-1.0-SNAPSHOT.jar
```
*Note: This will launch a GUI window.*

### 3. Usage

#### Setup Tab
- Click **"Generate Rules from PDF"** to parse the PDF and create the rules.
- Check the status at the bottom.

#### Triage Simulation Tab
1.  **Patient Age**: Enter the age.
2.  **Symptom**: Select a symptom from the dropdown. Click **"Add Symptom"**.
3.  **Triage Patient**: Click to see the result (Priority and Color).
4.  **Save Output**: Save the current result to `triage_result.txt`.

## Project Structure

*   `src/main/java/com/triage`:
    *   `TriageApplication.java`: Spring Boot entry point (GUI Mode).
    *   `ui/TriageGUI.java`: Main Swing Window.
    *   `service/`: Backend logic.
    *   `model/`: Domain models.
    *   `rules/`: PDF Parser and Engine.
*   `spec/`: Source PDF.

# Irish Children's Triage System (ICTS) - Rule Engine

Book example of Spec driven development

This project is a Java-based application that digitizes the **National Emergency Medicine Programme Irish Children's Triage System (ICTS)**. It parses the official PDF guidelines to generate executable business rules (Drools/DRL) and provides a command-line interface for triaging patients.

## Features

*   **PDF Rule Generation**: Automatically extracts triage rules from the ICTS PDF (`national-emergency-medicine-programme-irish-childrens-triage-system-icts.pdf`). It identifies flowcharts, symptoms, and triage categories (Red, Orange, Yellow, Green, Blue) using a coordinate-based parsing strategy.
*   **Drools Rule Engine**: Uses Apache KIE (Drools) to execute the generated rules logic.
*   **Interactive Triage CLI**: Allows users (e.g., triage nurses) to input patient details and symptoms to receive a calculated triage priority.

## Prerequisites

*   **Java 21** (Required for compatibility with recent libraries)
*   **Maven** (For building the project)

## Getting Started

### 1. Build the Project
Compile the code and copy dependencies:
```bash
mvn clean compile dependency:copy-dependencies
```

### 2. Run the Application
A helper script `run.bat` is provided for convenience.

**Generate Rules**
First, you must parse the PDF to generate the `.drl` file:
```bash
run.bat generate
```
*Output: `generated_rules/triage.drl`*

**Interactive Mode**
Run the triage system to evaluate a patient:
```bash
run.bat
```

### Usage Example

```text
> run.bat
...
--- Triage System Interactive Mode ---

Enter Patient Age (or 'exit'): 10
Enter symptoms (type 'done' to finish):
> Airway compromise
> done

>>> Result: Triage Result: Red (Priority 1)
```

## Project Structure

*   `src/main/java/com/triage/model`: Domain models (`Patient`, `Symptom`, `TriageResult`).
*   `src/main/java/com/triage/rules`:
    *   `TriageRuleGenerator.java`: Logic to parse the PDF and write the DRL file.
    *   `TriageEngine.java`: Wrapper around the KIE session to execute rules.
*   `src/main/java/com/triage/Main.java`: CLI entry point.
*   `spec/`: Contains the source PDF.
*   `generated_rules/`: Destination for the generated Drools file.

## Technical Details

The PDF parser uses **Apache PDFBox** with a custom `PDFTextStripper` to retain X/Y coordinates. This generic text is then processed to strictly identify:
1.  **Flowchart Titles**: Based on specific header coordinates.
2.  **Symptoms**: Based on bullet point indentation.
3.  **Categories**: Based on the column position of the numbers 1-5.

The rules are then executed using **Drools 7.74.1** (with MVEL 2.5.2 for Java 21+ support).

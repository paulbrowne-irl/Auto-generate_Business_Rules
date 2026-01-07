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

### 1. Build the Application
```bash
mvn clean package
```

### 2. Run the Application
You can run the application as a standard Java JAR. This starts both the **Console Interface** and the **REST API** (on port 8080).
```bash
java -jar target/triage-system-1.0-SNAPSHOT.jar
```

### 3. Usage

#### Console Interface
Follow the on-screen menu to:
1.  **Triage Patient**: Input age and symptoms interactively.
2.  **Generate Rules**: Parse the PDF to create/update the `.drl` file.
3.  **List Symptoms**: View all available symptoms from the rules.

#### REST API
**Endpoint:** `POST /api/triage`
**Body:**
```json
{
  "age": 10,
  "symptoms": [
    { "name": "Airway compromise" }
  ]
}
```

**Endpoint:** `GET /api/symptoms`
- Returns a list of all valid symptoms found in the rules.

## Project Structure

*   `src/main/java/com/triage`:
    *   `TriageApplication.java`: Spring Boot entry point.
    *   `controller/TriageController.java`: REST API endpoints.
    *   `cli/ConsoleRunner.java`: Interactive command-line logic.
    *   `service/`:
        *   `RuleGenerationService.java`: PDF parsing logic.
        *   `TriageService.java`: Rules Engine wrapper.
    *   `model/`: Domain models (`Patient`, `Symptom`, `TriageResult`).
*   `spec/`: Contains the source PDF.
*   `generated_rules/`: Destination for the generated Drools file.

## Technical Details

The PDF parser uses **Apache PDFBox** with a custom `PDFTextStripper` to retain X/Y coordinates. This generic text is then processed to strictly identify:
1.  **Flowchart Titles**: Based on specific header coordinates.
2.  **Symptoms**: Based on bullet point indentation.
3.  **Categories**: Based on the column position of the numbers 1-5.

The rules are then executed using **Drools 7.74.1** (with MVEL 2.5.2 for Java 21+ support).

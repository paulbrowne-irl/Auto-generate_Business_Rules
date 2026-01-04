# Irish Children’s Triage System (ICTS) - Specification

## Overview

A Java-based  application that digitizes the National Emergency Medicine Programme Irish Children’s Triage System (ICTS). It parses PDF guidelines to generate executable business rules and provides a user-friendly GUI for triaging patients.

**Purpose:** Automate the triage process using rule-based logic derived directly from official medical guidelines, assisting healthcare professionals in determining clinical priority.

---

## What Users Can Do

1.  **Generate Rules:** Parse the official ICTS PDF to create executable Drools rules.
2.  **Simulate Triage:** Input a patient's age and symptoms.
3.  **Select Symptoms:** Choose from a dynamic list of symptoms extracted from the guidelines.
4.  **View Results:** Instantly see the Triage Category (Colour) and Priority.

---

## User Interface

### Layout Mockup

**Tab 1: Setup**
```
┌─────────────────────────────────────────┐
│  Setup  │  Triage Simulation            │
│                                         │
│  [ Generate Rules from PDF ]            │
│                                         │
│  Status: Rules Generated Successfully.  │
│                                         │
└─────────────────────────────────────────┘
```

**Tab 2: Triage Simulation**
```
┌─────────────────────────────────────────┐
│  Setup  │  Triage Simulation            │
│                                         │
│  ┌─ Patient Details ─────────────────┐  │
│  │ Age: [ 10 ]                       │  │
│  │ Symptom: [ Airway compromise  ▼ ] │  │
│  │          [ Add Symptom ]          │  │
│  └───────────────────────────────────┘  │
│                                         │
│  Symptoms List:                         │
│  ┌───────────────────────────────────┐  │
│  │ - Airway compromise               │  │
│  │                                   │  │
│  └───────────────────────────────────┘  │
│                                         │
│  [ Clear Symptoms ]  [ Triage Patient ] │
│                                         │
│  Result: Red (Priority 1)               │
│                                         │
└─────────────────────────────────────────┘
```

---

## Features

### Feature 1: Rule Generation

**Input:**
- Source PDF file: `national-emergency-medicine-programme-irish-childrens-triage-system-icts.pdf`

**Behavior:**
- checks to see if the Drools (`.drl`) file already exists, and it has a new timestamp than the source pdf file. If it does, it skips the rule generation step.
- Parses text and coordinates to identify Flowcharts, Symptoms, and Categories.
- Generates a Drools (`.drl`) file containing executable rules.
- Updates the application state with the new rules.

**Output:**
- `generated_rules/triage.drl` file.
- Success/Error status message in the GUI.

---

### Feature 2: Patient Triage

**Input:**
- **Age:** Numeric input for patient age.
- **Symptoms:** Dropdown selection populated dynamically from the generated rules. Users can add multiple symptoms.

**Behavior:**
- Engine matches patient data against 700+ generated rules.
- Determines the highest priority (lowest category number) triggered.
- If no specific rule matches, defaults to "Blue".

**Output:**
- **Triage Result:** Displayed as Text (e.g., "Red") and color-coded visually.

---

### Feature 3: Dynamic Symptom List

**Behavior:**
- Reads the generated DRL file.
- Extracts unique symptom names used in `Symptom( name == "..." )` patterns.
- Populates the symptom dropdown to ensure users can only select valid, rule-triggering symptoms.

---

## Technical Architecture

### Core Components

1.  **PDF Parser (`TriageRuleGenerator`)**:
    - library: `Apache PDFBox`
    - Logic: Custom `PDFTextStripper` using X/Y coordinates to structure unstructured PDF content.

2.  **Rules Engine (`TriageEngine`)**:
    - Library: `Drools (Apache KIE)`
    - Logic: Stateful/Stateless session execution against the generated DRL.

3.  **User Interface (`Console')**:
    - Interface: simple console application.

4.  **Data Model**:
    - `Patient`: Holds ID, Age, List of Symptoms.
    - `Symptom`: Wrapper for symptom strings.
    - `TriageResult`: Encapsulates Color and Priority.

---

## Success Criteria

**Test 1: Rule Generation**
- Click "Generate Rules".
- Verify success message.
- Verify `generated_rules/triage.drl` exists and contains ~761 rules.

**Test 2: High Priority Triage**
- Input Age: 5.
- Add Symptom: "Airway compromise".
- Click Triage.
- **Result:** RED (Priority 1).

**Test 3: Lower Priority Triage**
- Input Age: 8.
- Add Symptom: "Minor limb injury".
- Click Triage.
- **Result:** GREEN or BLUE (depending on specific rule match).

**Test 4: User interface Responsiveness**
- Verify dropdown is populated.
- Verify "Clear Symptoms" resets the list and result.

---

## API & Data

**Input Data Source:**
- PDF File located in `spec/` directory.

**Output Data:**
- Drools Rule File: Text-based DRL format.

**Rules Format:**
```drools
rule "Rule_1_FlowchartName"
    when
        $p : Patient( $s : symptoms )
        Symptom( name == "Specific Symptom" ) from $s
    then
        insert(new TriageResult("Red"));
end
```

---

## Technical Requirements

**Language:** Java 21
**Build System:** Maven
**Dependencies:**
- `org.drools:drools-core`
- `org.drools:drools-compiler`
- `org.drools:drools-mvel`
- `org.apache.pdfbox:pdfbox`
- `org.slf4j:slf4j-simple`

**Environment:** Desktop (Windows/Linux/macOS)
**Logging** slf4j-simple

package com.triage.rules;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TriageRuleGenerator {

    static class TriageRule {
        String flowchart;
        String symptom;
        int category;

        public TriageRule(String flowchart, String symptom, int category) {
            this.flowchart = flowchart;
            this.symptom = symptom.replace("", "").trim();
            this.category = category;
        }
    }

    private List<TriageRule> rules = new ArrayList<>();

    // Parser State
    private String currentFlowchart = null;
    private List<String> currentSymptoms = new ArrayList<>();
    private Integer currentCategory = null;

    public void generate(String pdfPath, String outputDrlPath) throws IOException {
        PDDocument document = PDDocument.load(new File(pdfPath));
        if (document.isEncrypted()) {
            throw new IOException("PDF is encrypted");
        }

        // Custom Stripper to parse line by line with coordinates
        PDFTextStripper stripper = new PDFTextStripper() {
            @Override
            protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
                if (!textPositions.isEmpty()) {
                    float x = textPositions.get(0).getXDirAdj();
                    float y = textPositions.get(0).getYDirAdj();
                    processLine(text.trim(), x, y);
                }
            }
        };
        stripper.setSortByPosition(true);
        stripper.getText(document);

        // Flush any remaining rules
        flushBlock();

        document.close();

        writeDrl(outputDrlPath);
    }

    private void processLine(String text, float x, float y) {
        // Skip Header/Footer
        if (y > 750 || y < 50)
            return;

        // Detect Title (Flowchart Name)
        // Consistent at X ~ 48.19, Y ~ 63.17. Relaxed range: X < 60, Y < 100
        if (x < 60 && y < 100 && text.length() > 5 && !text.startsWith("A =") && !text.startsWith("Page")) {
            // New Flowchart implies flushing previous block and resetting
            flushBlock();
            currentFlowchart = text;
            currentSymptoms.clear();
            currentCategory = null;
            System.out.println("Flowchart found: " + currentFlowchart);
            return;
        }

        // Detect Symptom (Starts with bullet)
        // The bullet comes across as '' or similar. We check startsWith or just X
        // position column
        if (text.startsWith("") || (x > 50 && x < 100 && text.length() > 2)) {
            // It's a symptom line.
            // If it starts with bullet, remove it.
            String symptom = text.replace("", "").trim();
            if (!symptom.isEmpty()) {
                currentSymptoms.add(symptom);
            }
        }

        // Detect Category Number (1-5) on the right side (X > 490)
        if (x > 490 && text.matches("[1-5]")) {
            currentCategory = Integer.parseInt(text);
        }

        // Detect "No" which signifies end of a block
        if (text.equalsIgnoreCase("No") && x > 180 && x < 250) {
            flushBlock();
            currentSymptoms.clear();
            currentCategory = null;
        }
    }

    private void flushBlock() {
        if (currentFlowchart != null && currentCategory != null && !currentSymptoms.isEmpty()) {
            for (String sym : currentSymptoms) {
                rules.add(new TriageRule(currentFlowchart, sym, currentCategory));
            }
        }
    }

    private void writeDrl(String outputPath) throws IOException {
        StringBuilder drl = new StringBuilder();
        drl.append("package generated_rules;\n\n");
        drl.append("import com.triage.model.Patient;\n");
        drl.append("import com.triage.model.Symptom;\n");
        drl.append("import com.triage.model.TriageResult;\n\n");

        int ruleId = 0;
        for (TriageRule rule : rules) {
            ruleId++;
            drl.append("rule \"Rule_").append(ruleId).append("_").append(escape(rule.flowchart)).append("\"\n");
            drl.append("    when\n");
            // Match if patient has a symptom with this name
            // We loosely match string values for now
            drl.append("        $p : Patient( $s : symptoms )\n");
            drl.append("        Symptom( name == \"").append(escape(rule.symptom)).append("\" ) from $s\n");
            drl.append("    then\n");
            // Wait, Patient doesn't have addTriageResult yet, and TriageResult constructor
            // needs updating or usage.
            // Let's assume we insert TriageResult into working memory or add to a list.
            // For simplicity, let's insert a TriageResult object into memory.
            // Or better, set it on a global or helper.
            // Re-reading usage: "execute the rules and return the triage level".
            // So we can insert TriageResult into KIE session.
            drl.append("        insert(new TriageResult(\"").append(getColor(rule.category)).append("\"));\n");
            drl.append("end\n\n");
        }

        File outFile = new File(outputPath);
        if (outFile.getParentFile() != null)
            outFile.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(outFile);
        writer.write(drl.toString());
        writer.close();
        System.out.println("Generated " + rules.size() + " rules to " + outputPath);
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"").replace("\n", " ");
    }

    private String getColor(int cat) {
        switch (cat) {
            case 1:
                return "Red";
            case 2:
                return "Orange";
            case 3:
                return "Yellow";
            case 4:
                return "Green";
            default:
                return "Blue";
        }
    }

    public static void main(String[] args) {
        try {
            new TriageRuleGenerator().generate(
                    "spec/national-emergency-medicine-programme-irish-childrens-triage-system-icts.pdf",
                    "generated_rules/triage.drl");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

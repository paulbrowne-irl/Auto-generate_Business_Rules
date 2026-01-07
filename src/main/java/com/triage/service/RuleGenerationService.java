package com.triage.service;

import com.triage.rules.TriageRuleGenerator;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class RuleGenerationService {

    private static final String PDF_PATH = "spec/national-emergency-medicine-programme-irish-childrens-triage-system-icts.pdf";
    private static final String DRL_PATH = "generated_rules/triage.drl";

    public boolean areRulesOutdated() {
        File pdf = new File(PDF_PATH);
        File drl = new File(DRL_PATH);

        if (!pdf.exists()) {
            return false; // Cannot generate without PDF
        }
        if (!drl.exists()) {
            return true; // Missing rules
        }
        return pdf.lastModified() > drl.lastModified();
    }

    public void generateRules() throws IOException {
        File pdf = new File(PDF_PATH);
        if (!pdf.exists()) {
            throw new IOException("Source PDF not found at " + PDF_PATH);
        }

        TriageRuleGenerator generator = new TriageRuleGenerator();
        generator.generate(PDF_PATH, DRL_PATH);
    }

    public String getDrlPath() {
        return DRL_PATH;
    }
}

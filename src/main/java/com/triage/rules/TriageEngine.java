package com.triage.rules;

import com.triage.model.Patient;
import com.triage.model.TriageResult;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TriageEngine {

    private KieContainer kContainer;

    public void init() {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        File drlFile = new File("generated_rules/triage.drl");
        if (!drlFile.exists()) {
            throw new RuntimeException("Rules file not found: " + drlFile.getAbsolutePath());
        }

        kfs.write(ResourceFactory.newFileResource(drlFile));
        KieBuilder kb = ks.newKieBuilder(kfs).buildAll();

        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }

        kContainer = ks.newKieContainer(kb.getKieModule().getReleaseId());
    }

    public TriageResult executeTriage(Patient patient) {
        KieSession kSession = kContainer.newKieSession();
        kSession.insert(patient);

        List<TriageResult> results = new ArrayList<>();
        // We can use a global or query, or just insert an empty result and let rules
        // update it?
        // My rules use: insert(new TriageResult("Color"));

        kSession.fireAllRules();

        // Collect objects
        for (Object obj : kSession.getObjects()) {
            if (obj instanceof TriageResult) {
                results.add((TriageResult) obj);
            }
        }
        kSession.dispose();

        // Determine highest priority (lowest number)
        TriageResult finalResult = null;
        for (TriageResult r : results) {
            if (finalResult == null || r.getPriority() < finalResult.getPriority()) {
                finalResult = r;
            }
        }

        return finalResult != null ? finalResult : new TriageResult("Blue"); // Default
    }
}

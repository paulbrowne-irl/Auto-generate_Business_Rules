package com.triage;

import com.triage.ui.TriageGUI;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import java.awt.EventQueue;

@SpringBootApplication
public class TriageApplication {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(TriageApplication.class);
        builder.headless(false); // Important for GUI
        ConfigurableApplicationContext context = builder.run(args);

        // Launch GUI
        EventQueue.invokeLater(() -> {
            TriageGUI gui = context.getBean(TriageGUI.class);
            gui.setVisible(true);
        });
    }
}

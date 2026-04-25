package com.researchtool.console;

import javax.swing.*;
import java.awt.*;

/**
 * Offline local console emulator for educational workflows.
 */
public final class ResearchConsole {
    private static JFrame frame;

    private ResearchConsole() {
    }

    public static void open() {
        if (frame != null) {
            frame.toFront();
            return;
        }
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Research Console (Local)");
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JTextArea out = new JTextArea();
            out.setEditable(false);
            JTextField in = new JTextField();
            out.append("Research console started. Commands: help, list\n");

            in.addActionListener(e -> {
                String cmd = in.getText().trim();
                out.append("> " + cmd + "\n");
                switch (cmd) {
                    case "help" -> out.append("Available: help, list\n");
                    case "list" -> out.append("No real server commands. This is a local emulator.\n");
                    default -> out.append("Unknown command in local research console.\n");
                }
                in.setText("");
            });

            frame.add(new JScrollPane(out), BorderLayout.CENTER);
            frame.add(in, BorderLayout.SOUTH);
            frame.setSize(520, 300);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

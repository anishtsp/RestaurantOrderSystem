package com.restaurantops.gui.panels;

import com.restaurantops.core.RestaurantEngine;
import com.restaurantops.util.LoggerService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DiagnosticsPanel extends JPanel {

    private final RestaurantEngine engine;
    private final LoggerService logger;

    private JTextArea logArea;

    private JLabel lblEngineStarted;
    private JLabel lblStationsPaused;

    private Timer autoRefreshTimer;

    public DiagnosticsPanel() {

        this.engine = RestaurantEngine.getInstance();
        this.logger = engine.getLogger();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMainArea(), BorderLayout.CENTER);

        refreshStatus();
        refreshLogs();
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Diagnostics");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JCheckBox autoRefresh = new JCheckBox("Auto Refresh (1s)");
        autoRefresh.addActionListener(e -> toggleAutoRefresh(autoRefresh.isSelected()));

        p.add(title, BorderLayout.WEST);
        p.add(autoRefresh, BorderLayout.EAST);

        return p;
    }

    private void toggleAutoRefresh(boolean enabled) {
        if (enabled) {
            autoRefreshTimer = new Timer(1000, e -> {
                refreshStatus();
                refreshLogs();
            });
            autoRefreshTimer.start();
        } else {
            if (autoRefreshTimer != null) autoRefreshTimer.stop();
        }
    }

    private JSplitPane buildMainArea() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.6);

        split.setLeftComponent(buildLogsPanel());
        split.setRightComponent(buildStatusPanel());

        return split;
    }

    /* --------------------------
            LOG VIEWER
       -------------------------- */

    private JPanel buildLogsPanel() {
        JPanel p = new JPanel(new BorderLayout(5, 5));

        JLabel lbl = new JLabel("System Logs");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14));

        JButton refresh = new JButton("Refresh Logs");
        refresh.addActionListener(e -> refreshLogs());

        p.add(lbl, BorderLayout.NORTH);
        p.add(new JScrollPane(logArea), BorderLayout.CENTER);
        p.add(refresh, BorderLayout.SOUTH);

        return p;
    }

    private void refreshLogs() {
        var logs = engine.getLogger().getLogs();

        StringBuilder sb = new StringBuilder();
        for (String log : logs) {
            sb.append(log).append("\n");
        }

        logArea.setText(sb.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength()); // auto-scroll to bottom
    }


    /* --------------------------
            STATUS PANEL
       -------------------------- */

    private JPanel buildStatusPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("System Status");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        lblEngineStarted = addStatusRow(p, "Engine Started:");
        lblStationsPaused = addStatusRow(p, "Stations Paused:");

        p.add(Box.createVerticalStrut(20));
        p.add(buildControlButtons());

        return p;
    }

    private JLabel addStatusRow(JPanel parent, String label) {
        JPanel row = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel(label);
        JLabel value = new JLabel("-");

        lbl.setPreferredSize(new Dimension(140, 20));
        row.add(lbl, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);

        parent.add(row);
        parent.add(Box.createVerticalStrut(8));

        return value;
    }

    private JPanel buildControlButtons() {
        JPanel p = new JPanel(new GridLayout(2, 1, 5, 10));

        JButton pause = new JButton("Pause Stations");
        JButton resume = new JButton("Resume Stations");

        pause.addActionListener(e -> {
            engine.pauseStationsForIdle();
            refreshStatus();
        });

        resume.addActionListener(e -> {
            engine.resumeStationsOnActivity();
            refreshStatus();
        });

        p.add(pause);
        p.add(resume);

        return p;
    }

    private void refreshStatus() {
        lblEngineStarted.setText(engine.isStarted() ? "YES" : "NO");
        lblStationsPaused.setText(engine.isStationsPaused() ? "YES" : "NO");

        lblEngineStarted.setForeground(engine.isStarted() ? new Color(0, 180, 0) : Color.RED);
        lblStationsPaused.setForeground(engine.isStationsPaused() ? Color.RED : new Color(0, 180, 0));
    }
}

package com.restaurantops.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public class LoggerService {

    private final List<String> logs = new LinkedList<>();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public synchronized void log(String msg) {
        String entry = "[" + LocalDateTime.now().format(fmt) + "] " + msg;

        // PREVIOUS (incorrect): newest first
        // logs.add(0, entry);

        // FIXED: append to bottom (normal order)
        logs.add(entry);
    }

    public synchronized List<String> getLogs() {
        return new LinkedList<>(logs);
    }

    public synchronized void clear() {
        logs.clear();
    }
}

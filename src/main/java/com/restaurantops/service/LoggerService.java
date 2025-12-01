package com.restaurantops.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoggerService {

    private final List<String> logs = Collections.synchronizedList(new ArrayList<>());

    public void log(String msg) {
        logs.add(msg);
    }

    public List<String> getLogs() {
        return logs;
    }
}

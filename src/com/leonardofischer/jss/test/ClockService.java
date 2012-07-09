package com.leonardofischer.jss.test;

import com.leonardofischer.jss.Service;

public class ClockService extends Service {

    ClockApp clock;

    public void start(String[] args) {
        clock = new ClockApp();
        clock.run();
    }

    public void stop(String[] args) {
        clock.keepRunning = false;
    }

    public String status(String[] args) {
        return clock.date;
    }

    public static void main(String[] args) {
        ClockService clockService = new ClockService();
        clockService.parseArgs(args);
    }
}

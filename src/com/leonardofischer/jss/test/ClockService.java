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

    public void onServiceNotRunning() {
        printErrorMessage("The ClockService is not running, " + "please start it with the 'iniciar' command");
    }

    // new version of the main, with some customization
    public static void main(String[] args) {
        ClockService clockService = new ClockService();
        // 'iniciar' is the portuguese word for 'start'
        clockService.setStartCommand("iniciar");
        // Go to 'How Java Simple Services Work' if you want to know
        // why setting a port here
        clockService.getServiceController().setPort(9876);
        clockService.parseArgs(args);
    }
}

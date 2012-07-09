package com.leonardofischer.jss.test;

import java.util.Date;

public class ClockApp {

    public boolean keepRunning = true;

    public String date = "Not executed yet";

    public void run() {
        try {
            while (keepRunning) {
                date = (new Date()).toString();
                System.out.println(date);
                Thread.sleep(1000);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ClockApp clock = new ClockApp();
        clock.run();
    }
}

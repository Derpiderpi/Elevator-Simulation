package com.elevatorsim.model;

public class SimClock {
    private static int currentTime;
    
    //constructor
    public SimClock() {
        currentTime = 0;
    }
    
    //increment the currentTime
    public static void tick() {
        currentTime++;
    }
    
    //getter method for current time
    public static int getTime() {
        return currentTime;
    }
}

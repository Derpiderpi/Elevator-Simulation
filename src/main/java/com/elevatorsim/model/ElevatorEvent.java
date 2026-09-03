package com.elevatorsim.model;

public class ElevatorEvent {
    private int destination;
    private int expectedArrival;
    
    // Constructor
    public ElevatorEvent(int destination, int expectedArrival) {
        this.destination = destination;
        this.expectedArrival = expectedArrival;
    }
    
    // Get the destination floor
    public int getDestination() {
        return destination;
    }
    
    // Get expected time of arrival
    public int getExpectedArrival() {
        return expectedArrival;
    }
    
    // Set the destination
    public void setDestincation(int destination) {
        this.destination = destination;
    }
    
    // Set the expected arrival time
    public void setExpectedArrival(int expectedArrival) {
        this.expectedArrival = expectedArrival;
    }
}

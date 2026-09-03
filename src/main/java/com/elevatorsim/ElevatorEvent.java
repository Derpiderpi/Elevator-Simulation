package com.elevatorsim;

public class ElevatorEvent {
    private int destination;
    private int expectedArrival;
    private final int originFloor;
    private final int departureTick;

    // Constructor
    public ElevatorEvent(int originFloor, int destination, int departureTick, int expectedArrival) {
        this.originFloor = originFloor;
        this.destination = destination;
        this.departureTick = departureTick;
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

    // Get the floor the elevator departed from for this leg
    public int getOriginFloor() {
        return originFloor;
    }

    // Get the simulated tick this leg was scheduled at (departure)
    public int getDepartureTick() {
        return departureTick;
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

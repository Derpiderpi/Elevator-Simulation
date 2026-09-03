package com.elevatorsim;

public class PassengerArrival {
    private int numPassengers;
    private int destinationFloor;
    private int timePeriod;
    private int expectedTimeOfArrival;
    
    //constructor
    public PassengerArrival(int numPassengers, int destinationFloor, int timePeriod, int expectedTimeOfArrival){
        this.numPassengers = numPassengers;
        this.destinationFloor = destinationFloor;
        this.timePeriod = timePeriod;
        this.expectedTimeOfArrival = expectedTimeOfArrival;
    }
    
    //setter method for number of passengers
    public void setNumPassengers(int numPassengers) {
        this.numPassengers = numPassengers;
    }
    
    //setter method for destination floor
    public void setDestinationFloor(int destinationFloor) {
        this.destinationFloor = destinationFloor;
    }
    
    //setter method for time period
    public void setTimePeriod(int timePeriod) {
        this.timePeriod = timePeriod;
    }
    
    //setter method for expected time of arrival
    public void setExpectedTimeOfArrival(int expectedTimeOfArrival) {
        this.expectedTimeOfArrival = expectedTimeOfArrival;
    }
    
    //getter method for number of passengers
    public int getNumPassengers() {
        return numPassengers;
    }
    
    //getter method for destination floor
    public int getDestinationFloor() {
        return destinationFloor;
    }
    
    //getter method for time period
    public int getTimePeriod() {
        return timePeriod;
    }
    
    //getter method for expected time of arrival
    public int getExpectedTimeOfArrival() {
        return expectedTimeOfArrival;
    }
}
package com.elevatorsim.model;

public class BuildingFloor {
    private int[] totalDestinationRequests;
    private int[] arrivedPassengers;
    private int[] passengerRequests;
    private int approachingElevator;
    
    //constructor
    public BuildingFloor() {
        totalDestinationRequests = new int[10];
        arrivedPassengers = new int[10];
        passengerRequests = new int[10];
        approachingElevator = -1; // -1: No elevator is going to that floor
    }
    
    //setter method for total destination requests on every floor
    public void setTotalNumDestRequests(int floor, int numOfPassenRequest) {
        totalDestinationRequests[floor] += numOfPassenRequest;
    }
    
    //setter method for current arrived passengers and total arrived passengers on every floor
    public void setArrivedPassengers(int floor, int arrivedPassenger) {
        arrivedPassengers[floor] += arrivedPassenger;
    }
    
    //setter method for current passenger requests on every floor
    public void setPassengerRequests(int floor, int numOfPassenRequest) {
        if (numOfPassenRequest != 0 &&passengerRequests[floor]>0) {
            passengerRequests[floor] += numOfPassenRequest;
            return;
        }
        passengerRequests[floor] = numOfPassenRequest;
    }
    
    //setter method for approaching elevator
    public void setApproachingElevator(int approachingElevator) {
        this.approachingElevator = approachingElevator;
    }
    
    //getter method for total destination requests on every floor
    public int getTotalNumOfRequest(int floor) {
        return totalDestinationRequests[floor];
    }
    
    //getter method for current arrived passengers from every floor
    public int getArrivedPassenger(int floor) {
        return arrivedPassengers[floor];
    }
    
    //getter method for current passenger requests on every floor
    public int getPassengerRequests(int floor) {
        return passengerRequests[floor];
    }
    
    //getter method for approaching elevator
    public int getApproachingElevator() {
        return approachingElevator;
    }
}
package com.elevatorsim;

public class BuildingManager {
    private BuildingFloor[] floors;
    private boolean goUp;
    
    // Constructor
    public BuildingManager() {
        floors = new BuildingFloor[10];
        for (int i=0; i<10; i++) {
            floors[i] = new BuildingFloor();
        }
        goUp = false;
    }
    
    // Update passenger request by setter method in Building floor 
    public synchronized void updatePassengerRequest(int start, int dest, int numOfPassenRequest) {
        floors[start].setPassengerRequests(dest, numOfPassenRequest);
        floors[start].setTotalNumDestRequests(dest, numOfPassenRequest);
    }
    
    // Update arrived passengers by setter method in Building floor
    public synchronized void updateArrivedPassengers(int floor, int start, int arrivedPassenger) {
        floors[floor].setArrivedPassengers(start, arrivedPassenger);
    }
    
    // Update approaching elevator by setter method in Building floor
    public synchronized void updateApproachingElevator(int floor, int approachingElevator) {
        floors[floor].setApproachingElevator(approachingElevator);
    }
    
    // Getter method for passenger request
    public synchronized int getPassengerRequest(int start, int dest) {
        return floors[start].getPassengerRequests(dest);
    }

    // Get a floor's pending pickup request count broken down by destination floor (index = destination)
    public synchronized int[] getPassengerRequestsByDestination(int floor) {
        int[] counts = new int[10];
        for (int dest = 0; dest < 10; dest++) {
            counts[dest] = floors[floor].getPassengerRequests(dest);
        }
        return counts;
    }
    
    // Getter method for approaching elevator
    public synchronized int getApproachingElevator(int floor) {
        return floors[floor].getApproachingElevator();
    }
    
    // Check if there are passengers request for elevator; return the destination floor if there is, return -1 if no requests.
    public synchronized int detectPassengers(int elevatorID) {
        for (int floor=0; floor<10; floor++) {
            for (int dest=0; dest<10; dest++) {
                if (floors[floor].getPassengerRequests(dest) > 0 && floors[floor].getApproachingElevator() == -1) {
                    floors[floor].setApproachingElevator(elevatorID);
                    return floor;
                }
            }
        }
        return -1;
    }
    
    // Return true if the elevator goes up, else goes down
    public synchronized boolean checkUpDown() {
        return goUp;
    }
    
    // Load passengers in a floor and save their destination information in an array
    public synchronized int[] pickUpPassengers(int floor) {
        int[] destinations = new int[10];
        goUp = false;
        for (int dest=floor; dest<10; dest++) {
            if (floors[floor].getPassengerRequests(dest) > 0) {
                destinations[dest] = floors[floor].getPassengerRequests(dest);
                floors[floor].setPassengerRequests(dest, 0);
                goUp = true;
            }
        }
        if (!goUp) {
            for (int dest=floor; dest>=0; dest--) {
                if (floors[floor].getPassengerRequests(dest) > 0) {
                    destinations[dest] = floors[floor].getPassengerRequests(dest);
                    floors[floor].setPassengerRequests(dest, 0);
                }
            }
        }
        floors[floor].setApproachingElevator(-1);
        return destinations;
    }
    
    // Get total number of requests of a floor
    public synchronized int getTotalRequests(int floor) {
        int totalRequest = 0;
       for (int dest=0; dest<10; dest++) {
           totalRequest += floors[floor].getTotalNumOfRequest(dest);
       }
       return totalRequest;
    }
    
    // Get current number of requests of a floor
    public int getCurrentRequest(int floor) {
        int currentRequest = 0;
        for (int dest=0; dest<10; dest++) {
            currentRequest += floors[floor].getPassengerRequests(dest);
        }
        return currentRequest;
    }
    
    // Get total number of arrived passengers of a floor
    public int getTotalArrivedPassengers(int floor) {
        int totalArrived = 0;
        for (int start=0; start<10; start++) {
            totalArrived += floors[floor].getArrivedPassenger(start);
        }
        return totalArrived;
    }
}

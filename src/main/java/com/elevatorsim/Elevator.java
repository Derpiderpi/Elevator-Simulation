package com.elevatorsim;

import java.util.ArrayList;

public class Elevator implements Runnable {
    private int elevatorID;
    private volatile int currentfloor;
    private volatile int numPassengers;
    private int totalLoadedPassengers;
    private int totalUnloadedPassengers;
    ArrayList<ElevatorEvent> moveQueue;
    private BuildingManager manager;
    private int[] passengerDestinations;
    private int[] unloadedPassengers;
    // Read-only view of moveQueue's head for the GUI to poll cross-thread; reassigned wherever moveQueue's head changes
    private volatile ElevatorEvent activeLeg;
    
    // Constructor
    public Elevator(int elevatorID, BuildingManager manager) {
        this.elevatorID = elevatorID;
        this.manager = manager;
        currentfloor = 0;
        numPassengers = 0;
        totalLoadedPassengers = 0;
        totalUnloadedPassengers = 0;
        passengerDestinations = new int[10];
        moveQueue = new ArrayList<>();
        unloadedPassengers = new int[10];
    }
    
    // Keep detecting (going to a floor if passengers request), loading (update elevator and destination information)
    // and unloading passengers (update elevator and destination information)
    public void run() {
        while (true && !Thread.interrupted()) {
            if (numPassengers == 0 && moveQueue.isEmpty()) { // detecting
                int dest = manager.detectPassengers(elevatorID);
                if (dest != -1) {
                    moveQueue.add(new ElevatorEvent(currentfloor, dest, SimClock.getTime(), SimClock.getTime() + Math.abs(currentfloor-dest)*3 + 5));
                    activeLeg = moveQueue.get(0);
                    System.out.println(" Time: " + SimClock.getTime() + " Elevator " + elevatorID + " is heading to floor " + 
                           moveQueue.get(0).getDestination() + " to pick up passengers.");
                }
            }
            if (numPassengers == 0 && !moveQueue.isEmpty()) { // Loading passengers
                ElevatorEvent event = moveQueue.get(0);
                if (SimClock.getTime() == event.getExpectedArrival()) { // Check if the elevator arrive the destination floor
                    currentfloor = event.getDestination();
                    passengerDestinations = manager.pickUpPassengers(currentfloor);
                    if (manager.checkUpDown()) {
                        for (int dest=0; dest<passengerDestinations.length; dest++) { // Going up
                            if (passengerDestinations[dest] > 0) {
                                incPassengers(passengerDestinations[dest]);
                                moveQueue.add(new ElevatorEvent(currentfloor, dest, SimClock.getTime(), SimClock.getTime() + Math.abs(currentfloor-dest)*3+5*moveQueue.size()));
                                System.out.println(" Time: " + SimClock.getTime() + " Elevator " + elevatorID + " finished loading " + 
                                passengerDestinations[dest] + " passengers at floor " + currentfloor+ " to floor " + dest);
                            }
                        }
                    } else {
                        for (int dest=4; dest>=0; dest--) { // Going down
                            if (passengerDestinations[dest] > 0) {
                                incPassengers(passengerDestinations[dest]);
                                moveQueue.add(new ElevatorEvent(currentfloor, dest, SimClock.getTime(), SimClock.getTime() + Math.abs(currentfloor-dest)*3+5*moveQueue.size()));
                                System.out.println(" Time: " + SimClock.getTime() + " Elevator " + elevatorID + " finished loading " + 
                                passengerDestinations[dest] + " passengers at floor " + currentfloor+ " to floor " + dest);
                            }
                        }
                    }
                    moveQueue.remove(0);
                    activeLeg = moveQueue.isEmpty() ? null : moveQueue.get(0);
                }
            }
            if (numPassengers > 0 && !moveQueue.isEmpty()) { // Unloading passengers
                ElevatorEvent event = moveQueue.get(0);
                if (SimClock.getTime() == event.getExpectedArrival()) {
                    moveQueue.remove(0);
                    activeLeg = moveQueue.isEmpty() ? null : moveQueue.get(0);
                    int arrivedPassengers = passengerDestinations[event.getDestination()];
                    manager.updateArrivedPassengers(event.getDestination(), currentfloor, arrivedPassengers);
                    decPassengers(arrivedPassengers);
                    currentfloor = event.getDestination();
                    updateUnloadedPassengers(currentfloor, arrivedPassengers);
                    System.out.println(" Time: " + SimClock.getTime() + " Elevator " + elevatorID + " finished unloading " + 
                    arrivedPassengers + " passengers at floor " + currentfloor);
                }
            }
        }
    }
    
    // Update the number of passengers and total loaded passengers
    private void incPassengers(int num) {
        numPassengers += num;
        totalLoadedPassengers += num;
    }
    
    // Update the number of passengers and total unloaded passengers
    private void decPassengers(int num) {
        numPassengers -= num;
        totalUnloadedPassengers += num;
    }
    
    private void updateUnloadedPassengers(int floor, int num) {
        unloadedPassengers[floor] += num;
    }
    
    // Get the elevator ID
    public int getElevatorID() {
        return elevatorID;
    }
    
    // Get total loaded passengers
    public int getTotalLoadedPassengers() {
        return totalLoadedPassengers;
    }
    
    // Get total unloaded passengers
    public int getTotalUnloadedPassengers() {
        return totalUnloadedPassengers;
    }
    
    // Get current destination of the elevator
    public int getCurrentDestination() {
        if (moveQueue.size() == 0) {
            return -1;
        }
        return moveQueue.get(0).getDestination();
    }
    
    // Get current passengers on the elevator
    public int getCurrentPassengers() {
        return numPassengers;
    }
    
    public int getUnloadedPassengersFloor(int floor) {
        return unloadedPassengers[floor];
    }

    // Get the elevator's current floor
    public int getCurrentFloor() {
        return currentfloor;
    }

    // Get the in-flight move event (origin/destination/departure/arrival), or null if idle
    public ElevatorEvent getActiveLeg() {
        return activeLeg;
    }
}

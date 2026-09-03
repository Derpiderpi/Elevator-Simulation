package com.elevatorsim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ElevatorSimulation {
    private BuildingManager manager;
    private Elevator e1;
    private Elevator e2;
    private Elevator e3;
    private Elevator[] elevators = new Elevator[3];
    private Thread elevator1;
    private Thread elevator2;
    private Thread elevator3;
    private int totalSimTime;
    private volatile int rateOfSim;
    ArrayList<ArrayList<PassengerArrival>> PassArrival;
    
    // Constructor
    public ElevatorSimulation() {
        manager = new BuildingManager();
        e1 = new Elevator(0, manager);
        e2 = new Elevator(1, manager);
        e3 = new Elevator(2, manager);

        elevator1 = new Thread(e1);
        elevator2 = new Thread(e2);
        elevator3 = new Thread(e3);
        elevators[0] = e1;
        elevators[1] = e2;
        elevators[2] = e3;

        totalSimTime = 0;
        rateOfSim = 0;
        PassArrival = new ArrayList<>();  
    }
    
    // Runs the main simulation loop including incrementing the simulated time and managing passenger arrival behavior. 
    // The simulation ends when the current simulation time is greater than the total simulation time defined in ElevatorConfig.txt
    public void start() {
        try {
            readFile("ElevatorConfig.txt");
            elevator1.start();
            elevator2.start();
            elevator3.start();
            Thread.sleep(totalSimTime);
            while (SimClock.getTime() < totalSimTime) {
                SimClock.tick();
                for (ArrayList<PassengerArrival> i:PassArrival) {
                    for (PassengerArrival pa:i) {
                        if (SimClock.getTime() == pa.getExpectedTimeOfArrival()) {
                            manager.updatePassengerRequest(PassArrival.indexOf(i), pa.getDestinationFloor(), pa.getNumPassengers());
                            pa.setExpectedTimeOfArrival(pa.getTimePeriod() + SimClock.getTime());
                            System.out.println("Time: "+ SimClock.getTime() + " | " + pa.getNumPassengers() + " passengers entering floor " + 
                                    PassArrival.indexOf(i) + ", requesting to floor " + pa.getDestinationFloor());
                            
                        }
                    }
                }
                Thread.sleep(rateOfSim);
            }
            elevator1.interrupt();
            elevator2.interrupt();
            elevator3.interrupt();
            printBuildingState();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Get the elevator instances, for read-only cross-thread polling by the GUI
    public Elevator[] getElevators() {
        return elevators;
    }

    // Get the building manager, for read-only cross-thread polling by the GUI
    public BuildingManager getManager() {
        return manager;
    }

    // Get the configured real-time milliseconds per simulated tick, or 0 before readFile() has run
    public int getRateOfSimMs() {
        return rateOfSim;
    }

    // Reads the ElevatorConfig.txt file and save it to PassengerArrival object
    private void readFile(String filePath) throws IOException {
        String[] line;
        Scanner sc = new Scanner(new File(filePath));
        totalSimTime = Integer.parseInt(sc.nextLine());
        rateOfSim = Integer.parseInt(sc.nextLine());
        while (sc.hasNextLine()) {
            line = sc.nextLine().split(";");
            ArrayList<PassengerArrival> pa = new ArrayList<> ();
            for (String i:line) {
                String[] s = i.split("\\s+");
                    pa.add(new PassengerArrival(Integer.parseInt(s[0]), Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[2])));
            }
            PassArrival.add(pa);
        }
        sc.close();
    }
    
    
    // Prints out the final state of the elevators and building floors
    public void printBuildingState() {   
        System.out.println();
        for (int floor=0; floor<10; floor++) {
            System.out.println("Floor# | Total Requests  | Total Exits | Current Request | Current Approaching");
            System.out.println(floor + "           " + manager.getTotalRequests(floor) +"                "+manager.getTotalArrivedPassengers(floor) 
            + "                 " + manager.getCurrentRequest(floor)+"                "+manager.getApproachingElevator(floor));
        }
        System.out.println();
        
        for (int i=0; i<elevators.length; i++) {
            System.out.println("Elevator "+ elevators[i].getElevatorID() + ": " + 
        elevators[i].getTotalLoadedPassengers() + " passengers entered the elevator throughout the simulation, " + 
            elevators[i].getTotalUnloadedPassengers() + " passengers exited the elevator, " +
        "\n            Current has " + elevators[i].getCurrentPassengers() + " passengers going to floor " + 
            elevators[i].getCurrentDestination());
            for (int r=0; r<10; r++) {
                System.out.println("         - " + elevators[i].getUnloadedPassengersFloor(r) + " passengers exited on floor " + r);
            }
        }
    }
}

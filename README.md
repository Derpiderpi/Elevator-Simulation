# ElevatorSimulation

## Introduction
This project simulates the situation in a ten-story building with three elevators. The project is written in Java using knowledge focus on Multithreading.

## Project Layout
The project is built with Maven. Source lives under `src/main/java/com/elevatorsim`:
- `com.elevatorsim.model` — the simulation itself (`BuildingFloor`, `BuildingManager`, `Elevator`, `ElevatorEvent`, `ElevatorSimulation`, `PassengerArrival`, `SimClock`), unchanged in behavior from the original flat-file version.
- `com.elevatorsim` — `Simulator`, the original console-only entry point.
- `com.elevatorsim.gui` — a JavaFX visualization (`ElevatorApp` and friends) that runs the simulation in the background and animates the building, elevators, and passengers as it progresses.

## Running

**JavaFX animation** (floors, elevators, and passengers rendered and animated live):
```
mvn clean javafx:run
```
Click "Start Simulation" in the window that opens.

**Original console-only mode** (prints the same log lines as before, straight to stdout):
```
mvn -q compile exec:java
```

Both modes read `ElevatorConfig.txt` from the current working directory (a sample is included at the project root).

## Input File
Setting should be provided seperately in a text file named "ElevatorConfig.txt", in the following format:

- The 1st line represents the total time of the simulation (in number of simulated seconds).
- The 2nd line represents the rate of each simulated seconds (in milliseconds)
- Lines 3 – 12 represent the passenger arrival rate of each floor in the building. The general format to define passenger behavior is [num_passengers floor_destination rate_of_occurrence]. Each floor can have many behaviors defined and each one will be separated by a semicolon (;).

Here is an example of ElevatorConfig.txt:
```
1000
100
2 9 100;5 2 300
3 8 500;1 4 200
5 6 200;2 1 500;3 9 600
4 0 200
2 3 300;6 2 100;4 0 40
2 3 300;6 2 100;4 0 40
4 5 200
5 0 200;2 1 500;3 3 600
3 9 500;1 4 200
2 4 100;5 2 300
```

## Elevator Rules
- Each elevator will run in its own thread.
- Each elevator will check the state of the building and passengers without sleeping.
- All elevators start on the 0th floor.
- An elevator will only head to a floor to load passengers if it currently does not have any passengers and is not moving towards a floor.
- By default, an elevator will load all passengers requesting to go up first. If an elevator reaches a floor to load passengers and there are no passengers that want to go up, then the elevator will load all passengers that want to go down.
- The elevator must drop off current passengers to the floor that it is closest to. For example, if the elevator on floor 2 has two passengers requesting to go to floor 3 and floor 4, then it will stop at floor 3 first and then floor 4 (not 4 and then 3).
- If an elevator has no passengers to drop off, then it will continuously check all floors to see if any passengers are waiting for an elevator and no other elevator is approaching that floor for passenger pickup.
- No two elevators will approach a floor for passenger pickup at the same time.
- If an elevator detects that there are passengers waiting on a floor, and no other elevator is currently approaching a floor for passenger pickup, then it will go towards that floor to load passengers.
- An elevator takes 5 simulated seconds to load / unload passengers.
- An elevator takes 3 simulated seconds to traverse one floor.
- An elevator does not have a max capacity of passengers.

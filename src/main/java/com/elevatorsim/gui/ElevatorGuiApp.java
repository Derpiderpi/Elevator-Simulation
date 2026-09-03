package com.elevatorsim.gui;

import com.elevatorsim.Elevator;
import com.elevatorsim.ElevatorEvent;
import com.elevatorsim.ElevatorSimulation;
import com.elevatorsim.SimClock;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// JavaFX entry point. Owns the ElevatorSimulation's lifecycle (constructs it and runs it on a
// background thread) and drives a single AnimationTimer that polls simulation state each frame
// to animate elevator car positions, passenger spheres, floor waiting-passenger spheres, and
// passenger-transfer (pickup/dropoff) animations within BuildingView's 3D scene.
public class ElevatorGuiApp extends Application {

    private final ElevatorSimulation simulation = new ElevatorSimulation();
    private final PassengerTransitManager transitManager = new PassengerTransitManager(BuildingView.SHAFT_COUNT);

    // Tracks the wall-clock time of the last observed SimClock tick change, so the interpolator
    // is self-calibrating rather than relying on a fixed "simulation start" timestamp.
    private int lastObservedTick = -1;
    private long lastTickChangeMs = System.currentTimeMillis();

    @Override
    public void start(Stage stage) {
        BuildingView buildingView = new BuildingView();
        Scene scene = new Scene(buildingView);
        stage.setTitle("Elevator Simulation");
        stage.setScene(scene);
        stage.show();

        Thread simThread = new Thread(simulation::start, "sim-driver");
        simThread.setDaemon(true);
        simThread.start();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render(buildingView);
            }
        };
        timer.start();
    }

    private void render(BuildingView buildingView) {
        int rateOfSimMs = simulation.getRateOfSimMs();
        if (rateOfSimMs <= 0) {
            // ElevatorSimulation.readFile() hasn't populated rateOfSim yet; nothing to render.
            return;
        }

        long nowMs = System.currentTimeMillis();
        int currentTick = SimClock.getTime();
        if (currentTick != lastObservedTick) {
            lastObservedTick = currentTick;
            lastTickChangeMs = nowMs;
        }

        Elevator[] elevators = simulation.getElevators();
        for (int i = 0; i < elevators.length; i++) {
            Elevator elevator = elevators[i];

            transitManager.observePickup(i, elevator.getLastPickup(), nowMs);
            transitManager.observeDropoff(i, elevator.getLastDropoff(), nowMs);

            ElevatorEvent activeLeg = elevator.getActiveLeg();
            double floorPosition = ElevatorInterpolator.computeFloorPosition(
                    activeLeg, elevator.getCurrentFloor(), currentTick, lastTickChangeMs, nowMs, rateOfSimMs);

            int[] ridingCounts = elevator.getRidingDestinationCounts();
            int[] suppression = transitManager.getPickupSuppression(i);
            int[] visibleRidingByDest = new int[10];
            for (int d = 0; d < 10; d++) {
                visibleRidingByDest[d] = Math.max(0, ridingCounts[d] - suppression[d]);
            }
            buildingView.updateElevator(i, floorPosition, visibleRidingByDest, elevator.getRidingOriginFloor());
        }
        transitManager.tick(nowMs);

        for (int floor = 0; floor < BuildingView.FLOOR_COUNT; floor++) {
            buildingView.updateFloorWaiting(floor, simulation.getManager().getPassengerRequestsByDestination(floor));
        }

        buildingView.updateTransits(transitManager.getActiveTransits(), nowMs);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package com.elevatorsim.gui;

import com.elevatorsim.model.ElevatorSimulation;
import com.elevatorsim.model.SimClock;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

// JavaFX entry point. Builds the building view, runs the existing
// ElevatorSimulation on a background thread untouched, and polls its
// state ~15 times/sec on the FX Application Thread to keep the view in
// sync and animated.
public class ElevatorApp extends Application {

    private static final long POLL_INTERVAL_NANOS = 65_000_000L;

    private boolean started = false;

    @Override
    public void start(Stage stage) {
        ElevatorSimulation simulation = new ElevatorSimulation();
        BuildingView buildingView = new BuildingView(simulation.getElevators(), simulation.getManager());

        Button startButton = new Button("Start Simulation");
        Label clockLabel = new Label("Time: 0");
        clockLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        Label statusLabel = new Label("Idle");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

        startButton.setOnAction(e -> {
            if (started) {
                return;
            }
            started = true;
            startButton.setDisable(true);
            statusLabel.setText("Running");

            Thread simulationThread = new Thread(() -> {
                simulation.start();
                Platform.runLater(() -> statusLabel.setText("Finished"));
            }, "elevator-simulation");
            simulationThread.setDaemon(true);
            simulationThread.start();
        });

        HBox topBar = new HBox(18, startButton, clockLabel, statusLabel);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(buildingView);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(scrollPane);

        stage.setTitle("Elevator Simulation");
        stage.setScene(new Scene(root, 640, 700));
        stage.show();

        AnimationTimer pollTimer = new AnimationTimer() {
            private long lastPoll = 0;

            @Override
            public void handle(long now) {
                if (now - lastPoll < POLL_INTERVAL_NANOS) {
                    return;
                }
                lastPoll = now;
                clockLabel.setText("Time: " + SimClock.getTime());
                buildingView.refresh();
            }
        };
        pollTimer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

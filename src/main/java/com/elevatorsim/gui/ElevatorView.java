package com.elevatorsim.gui;

import com.elevatorsim.model.Elevator;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

// The animated car for a single Elevator. Every refresh() it compares the
// model's live currentFloor/currentPassengers against what was last shown
// and (a) glides the car to its new floor with a TranslateTransition and
// (b) fades rider dots in/out to match the new passenger count. It only
// ever reads from the Elevator instance - it never touches simulation
// state.
class ElevatorView {

    private static final Color[] CAR_COLORS = {
        Color.web("#3f7cac"), Color.web("#c1440e"), Color.web("#2e8b57"),
    };
    private static final int MAX_VISIBLE_RIDERS = 6;
    private static final Duration FADE_DURATION = Duration.millis(220);
    private static final double MS_PER_FLOOR = 220;
    private static final double MAX_MOVE_MS = 1600;

    private final Elevator elevator;
    private final double rowHeight;
    private final double carHeight;
    private final double topPad;
    private final int numFloors;

    private final StackPane node;
    private final FlowPane riderDots = new FlowPane(2, 2);
    private final Label overflowLabel = new Label();

    private int lastFloor;
    private TranslateTransition activeMove;

    ElevatorView(Elevator elevator, double shaftX, double rowHeight, double carWidth, double carHeight,
                 double topPad, int numFloors) {
        this.elevator = elevator;
        this.rowHeight = rowHeight;
        this.carHeight = carHeight;
        this.topPad = topPad;
        this.numFloors = numFloors;

        Rectangle body = new Rectangle(carWidth, carHeight);
        body.setArcWidth(10);
        body.setArcHeight(10);
        body.setFill(CAR_COLORS[elevator.getElevatorID() % CAR_COLORS.length]);
        body.setStroke(Color.web("#202020"));
        body.setStrokeWidth(1.5);

        Label idLabel = new Label("E" + elevator.getElevatorID());
        idLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold;");

        riderDots.setPrefWrapLength(carWidth - 12);
        riderDots.setAlignment(Pos.CENTER);
        overflowLabel.setStyle("-fx-text-fill: white; -fx-font-size: 9px;");

        HBox riderRow = new HBox(3, riderDots, overflowLabel);
        riderRow.setAlignment(Pos.CENTER);

        VBox content = new VBox(3, idLabel, riderRow);
        content.setAlignment(Pos.CENTER);

        node = new StackPane(body, content);
        node.setPrefSize(carWidth, carHeight);
        node.setClip(new Rectangle(carWidth, carHeight));
        node.setLayoutX(shaftX);
        lastFloor = elevator.getCurrentFloor();
        node.setLayoutY(floorToY(lastFloor));
    }

    Node getNode() {
        return node;
    }

    // Reads the elevator's live state and animates any change since the
    // last call.
    void refresh() {
        int currentFloor = elevator.getCurrentFloor();
        if (currentFloor != lastFloor) {
            animateTo(currentFloor);
        }
        reconcileRiders(elevator.getCurrentPassengers());
    }

    private double floorToY(int floor) {
        double margin = (rowHeight - carHeight) / 2.0;
        return topPad + (numFloors - 1 - floor) * rowHeight + margin;
    }

    private void animateTo(int newFloor) {
        if (activeMove != null) {
            activeMove.stop();
        }
        double committedY = node.getLayoutY() + node.getTranslateY();
        node.setLayoutY(committedY);
        node.setTranslateY(0);

        double targetY = floorToY(newFloor);
        double delta = targetY - committedY;
        int floorsMoved = Math.max(1, Math.abs(newFloor - lastFloor));
        Duration duration = Duration.millis(Math.min(MAX_MOVE_MS, MS_PER_FLOOR * floorsMoved));

        TranslateTransition move = new TranslateTransition(duration, node);
        move.setToY(delta);
        move.setInterpolator(Interpolator.EASE_BOTH);
        move.setOnFinished(e -> {
            node.setLayoutY(targetY);
            node.setTranslateY(0);
        });
        move.play();
        activeMove = move;
        lastFloor = newFloor;
    }

    private void reconcileRiders(int passengerCount) {
        int target = Math.min(passengerCount, MAX_VISIBLE_RIDERS);
        int current = riderDots.getChildren().size();
        if (target > current) {
            for (int i = current; i < target; i++) {
                addRiderDot();
            }
        } else if (target < current) {
            List<Node> toRemove = new ArrayList<>(riderDots.getChildren().subList(target, current));
            for (Node dot : toRemove) {
                removeRiderDot(dot);
            }
        }
        overflowLabel.setText(passengerCount > MAX_VISIBLE_RIDERS ? "+" + (passengerCount - MAX_VISIBLE_RIDERS) : "");
    }

    private void addRiderDot() {
        Node dot = PassengerToken.create(Color.web("#fff4e0"));
        dot.setOpacity(0);
        riderDots.getChildren().add(dot);
        FadeTransition fade = new FadeTransition(FADE_DURATION, dot);
        fade.setToValue(1);
        fade.play();
    }

    private void removeRiderDot(Node dot) {
        FadeTransition fade = new FadeTransition(FADE_DURATION, dot);
        fade.setToValue(0);
        fade.setOnFinished(e -> riderDots.getChildren().remove(dot));
        fade.play();
    }
}

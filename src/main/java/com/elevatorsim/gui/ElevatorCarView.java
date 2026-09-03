package com.elevatorsim.gui;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

// Renders a single elevator car: a colored rectangle with its current passenger count.
// Vertical position is set continuously by the caller as the simulation's animation timer
// recomputes each elevator's interpolated floor position every frame.
public class ElevatorCarView extends StackPane {

    private final Label passengerLabel;

    public ElevatorCarView(String fillColor) {
        Rectangle body = new Rectangle(BuildingView.CAR_WIDTH, BuildingView.CAR_HEIGHT);
        body.setArcWidth(8);
        body.setArcHeight(8);
        body.setFill(Color.web(fillColor));
        body.setStroke(Color.web("#111827"));

        passengerLabel = new Label("0");
        passengerLabel.setTextFill(Color.WHITE);
        passengerLabel.setStyle("-fx-font-weight: bold;");

        getChildren().addAll(body, passengerLabel);
        setPickOnBounds(false);
    }

    public void updatePosition(double floorPosition) {
        double y = BuildingView.TOP_MARGIN
                + (BuildingView.FLOOR_COUNT - 1 - floorPosition) * BuildingView.FLOOR_HEIGHT
                + (BuildingView.FLOOR_HEIGHT - BuildingView.CAR_HEIGHT) / 2.0;
        setLayoutY(y);
    }

    public void updatePassengerCount(int passengerCount) {
        passengerLabel.setText(Integer.toString(passengerCount));
    }
}

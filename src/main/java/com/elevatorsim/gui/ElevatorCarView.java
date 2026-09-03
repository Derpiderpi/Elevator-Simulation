package com.elevatorsim.gui;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

// Renders a single elevator car: a colored rectangle with one circle per riding passenger.
// Vertical position is set continuously by the caller as the simulation's animation timer
// recomputes each elevator's interpolated floor position every frame. Uses Pane (rather than
// StackPane) so the body and the passenger-circle grid can be positioned by explicit
// coordinates, matching BuildingView's layout style and avoiding StackPane's auto-centering.
public class ElevatorCarView extends Pane {

    private static final int MAX_RIDING_DISPLAY = 12;
    private static final int RIDING_GRID_COLUMNS = 6;
    private static final double RIDING_CIRCLE_RADIUS = 5;
    private static final double RIDING_GRID_SPACING = 15;
    private static final double RIDING_GRID_X0 = 8;
    private static final double RIDING_GRID_Y0 = 9;

    private final Circle[] passengerCircles = new Circle[MAX_RIDING_DISPLAY];

    public ElevatorCarView(String fillColor) {
        setPrefSize(BuildingView.CAR_WIDTH, BuildingView.CAR_HEIGHT);

        Rectangle body = new Rectangle(0, 0, BuildingView.CAR_WIDTH, BuildingView.CAR_HEIGHT);
        body.setArcWidth(8);
        body.setArcHeight(8);
        body.setFill(Color.web(fillColor));
        body.setStroke(Color.web("#111827"));
        getChildren().add(body);

        for (int i = 0; i < MAX_RIDING_DISPLAY; i++) {
            double cx = RIDING_GRID_X0 + (i % RIDING_GRID_COLUMNS) * RIDING_GRID_SPACING;
            double cy = RIDING_GRID_Y0 + (i / RIDING_GRID_COLUMNS) * RIDING_GRID_SPACING;
            Circle circle = new Circle(cx, cy, RIDING_CIRCLE_RADIUS, Color.WHITE);
            circle.setStroke(Color.web("#111827"));
            circle.setVisible(false);
            passengerCircles[i] = circle;
            getChildren().add(circle);
        }

        setPickOnBounds(false);
    }

    public void updatePosition(double floorPosition) {
        double y = BuildingView.TOP_MARGIN
                + (BuildingView.FLOOR_COUNT - 1 - floorPosition) * BuildingView.FLOOR_HEIGHT
                + (BuildingView.FLOOR_HEIGHT - BuildingView.CAR_HEIGHT) / 2.0;
        setLayoutY(y);
    }

    public void updateRidingCount(int ridingCount) {
        for (int i = 0; i < passengerCircles.length; i++) {
            passengerCircles[i].setVisible(i < ridingCount);
        }
    }
}

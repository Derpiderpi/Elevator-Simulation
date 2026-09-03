package com.elevatorsim.gui;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

// Renders the building's 10 floors and 3 elevator shafts, floor call-pending indicators,
// and hosts one ElevatorCarView per shaft. Layout is computed once in the constructor;
// per-frame updates only move/relabel the existing car and indicator nodes.
public class BuildingView extends Pane {

    public static final int FLOOR_COUNT = 10;
    public static final int SHAFT_COUNT = 3;
    public static final double FLOOR_HEIGHT = 50;
    public static final double SHAFT_WIDTH = 130;
    public static final double LEFT_MARGIN = 80;
    public static final double TOP_MARGIN = 20;
    public static final double CAR_WIDTH = 96;
    public static final double CAR_HEIGHT = 36;

    private static final String[] CAR_COLORS = {"#3B82F6", "#EF4444", "#10B981"};
    private static final String INDICATOR_IDLE_COLOR = "#4B5563";
    private static final String INDICATOR_PENDING_COLOR = "#F59E0B";

    private final ElevatorCarView[] carViews = new ElevatorCarView[SHAFT_COUNT];
    private final Circle[] callIndicators = new Circle[FLOOR_COUNT];

    public BuildingView() {
        double width = LEFT_MARGIN + SHAFT_COUNT * SHAFT_WIDTH;
        double height = TOP_MARGIN + FLOOR_COUNT * FLOOR_HEIGHT + TOP_MARGIN;
        setPrefSize(width, height);
        setStyle("-fx-background-color: #111827;");

        for (int floor = 0; floor < FLOOR_COUNT; floor++) {
            double rowTopY = TOP_MARGIN + (FLOOR_COUNT - 1 - floor) * FLOOR_HEIGHT;

            Line separator = new Line(LEFT_MARGIN, rowTopY + FLOOR_HEIGHT, width, rowTopY + FLOOR_HEIGHT);
            separator.setStroke(Color.web("#374151"));
            getChildren().add(separator);

            Text label = new Text(10, rowTopY + FLOOR_HEIGHT / 2.0 + 4, "Floor " + floor);
            label.setFill(Color.web("#D1D5DB"));
            getChildren().add(label);

            Circle indicator = new Circle(LEFT_MARGIN - 20, rowTopY + FLOOR_HEIGHT / 2.0, 6);
            indicator.setFill(Color.web(INDICATOR_IDLE_COLOR));
            callIndicators[floor] = indicator;
            getChildren().add(indicator);
        }

        for (int shaft = 0; shaft < SHAFT_COUNT; shaft++) {
            double shaftX = LEFT_MARGIN + shaft * SHAFT_WIDTH;

            Line shaftLine = new Line(shaftX, TOP_MARGIN, shaftX, height - TOP_MARGIN);
            shaftLine.setStroke(Color.web("#374151"));
            getChildren().add(shaftLine);

            ElevatorCarView car = new ElevatorCarView(CAR_COLORS[shaft % CAR_COLORS.length]);
            car.setLayoutX(shaftX + (SHAFT_WIDTH - CAR_WIDTH) / 2.0);
            car.updatePosition(0);
            carViews[shaft] = car;
            getChildren().add(car);
        }

        Line rightBorder = new Line(width, TOP_MARGIN, width, height - TOP_MARGIN);
        rightBorder.setStroke(Color.web("#374151"));
        getChildren().add(rightBorder);
    }

    public void updateElevator(int shaftIndex, double floorPosition, int passengerCount) {
        carViews[shaftIndex].updatePosition(floorPosition);
        carViews[shaftIndex].updatePassengerCount(passengerCount);
    }

    public void updateFloorCallState(int floor, boolean pending) {
        callIndicators[floor].setFill(Color.web(pending ? INDICATOR_PENDING_COLOR : INDICATOR_IDLE_COLOR));
    }
}

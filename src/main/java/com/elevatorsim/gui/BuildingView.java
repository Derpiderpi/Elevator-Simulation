package com.elevatorsim.gui;

import java.util.List;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

// Renders the building's 10 floors and 3 elevator shafts, one circle per waiting passenger
// per floor, and hosts one ElevatorCarView per shaft. Layout is computed once in the
// constructor for the persistent floor/car nodes; per-frame updates toggle their visibility
// and rebuild the short-lived passenger-transfer animation layer on top.
public class BuildingView extends Pane {

    public static final int FLOOR_COUNT = 10;
    public static final int SHAFT_COUNT = 3;
    public static final double FLOOR_HEIGHT = 50;
    public static final double SHAFT_WIDTH = 130;
    public static final double LEFT_MARGIN = 150;
    public static final double TOP_MARGIN = 20;
    public static final double CAR_WIDTH = 96;
    public static final double CAR_HEIGHT = 36;

    private static final int MAX_WAITING_DISPLAY = 12;
    private static final int WAITING_GRID_COLUMNS = 6;
    private static final double WAITING_CIRCLE_RADIUS = 4;
    private static final double WAITING_GRID_SPACING = 11;
    private static final double WAITING_GRID_X0 = 70;
    private static final double WAITING_AREA_CENTER_X =
            WAITING_GRID_X0 + (WAITING_GRID_COLUMNS - 1) * WAITING_GRID_SPACING / 2.0;

    private static final double TRANSIT_CIRCLE_RADIUS = 4;
    private static final int TRANSIT_FAN_COLUMNS = 6;
    private static final double TRANSIT_FAN_SPACING = 9;

    private static final String[] CAR_COLORS = {"#3B82F6", "#EF4444", "#10B981"};
    private static final String WAITING_CIRCLE_COLOR = "#F59E0B";

    private final ElevatorCarView[] carViews = new ElevatorCarView[SHAFT_COUNT];
    private final Circle[][] waitingCircles = new Circle[FLOOR_COUNT][MAX_WAITING_DISPLAY];
    private final Group transitLayer = new Group();

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

            double gridY0 = rowTopY + FLOOR_HEIGHT / 2.0 - WAITING_GRID_SPACING / 2.0;
            for (int i = 0; i < MAX_WAITING_DISPLAY; i++) {
                double cx = WAITING_GRID_X0 + (i % WAITING_GRID_COLUMNS) * WAITING_GRID_SPACING;
                double cy = gridY0 + (i / WAITING_GRID_COLUMNS) * WAITING_GRID_SPACING;
                Circle circle = new Circle(cx, cy, WAITING_CIRCLE_RADIUS, Color.web(WAITING_CIRCLE_COLOR));
                circle.setVisible(false);
                waitingCircles[floor][i] = circle;
                getChildren().add(circle);
            }
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

        // Added last so passenger-transfer animations render above floors and cars.
        getChildren().add(transitLayer);
    }

    public void updateElevator(int shaftIndex, double floorPosition, int passengerCount) {
        carViews[shaftIndex].updatePosition(floorPosition);
        carViews[shaftIndex].updateRidingCount(passengerCount);
    }

    public void updateFloorWaitingCount(int floor, int waitingCount) {
        Circle[] circles = waitingCircles[floor];
        for (int i = 0; i < circles.length; i++) {
            circles[i].setVisible(i < waitingCount);
        }
    }

    // Rebuilds the transient transfer-animation layer from the currently active transits.
    // Rebuilding per frame is acceptable here since transits are short-lived and few, unlike
    // the persistent floor/car circle pools above which stay fixed-size to avoid node churn.
    public void updateTransits(List<PassengerTransit> activeTransits, long nowMs) {
        transitLayer.getChildren().clear();
        for (PassengerTransit transit : activeTransits) {
            double progress = transit.computeProgress(nowMs);
            double floorCenterY = TOP_MARGIN + (FLOOR_COUNT - 1 - transit.getFloor()) * FLOOR_HEIGHT + FLOOR_HEIGHT / 2.0;
            double shaftCenterX = LEFT_MARGIN + transit.getShaftIndex() * SHAFT_WIDTH + SHAFT_WIDTH / 2.0;

            double fromX = transit.getKind() == PassengerTransit.Kind.PICKUP ? WAITING_AREA_CENTER_X : shaftCenterX;
            double toX = transit.getKind() == PassengerTransit.Kind.PICKUP ? shaftCenterX : WAITING_AREA_CENTER_X;
            double centerX = fromX + (toX - fromX) * progress;

            String color = CAR_COLORS[transit.getShaftIndex() % CAR_COLORS.length];
            int renderedCount = Math.min(transit.getCount(), MAX_WAITING_DISPLAY);
            for (int i = 0; i < renderedCount; i++) {
                double offsetX = (i % TRANSIT_FAN_COLUMNS - (TRANSIT_FAN_COLUMNS - 1) / 2.0) * TRANSIT_FAN_SPACING;
                double offsetY = (i / TRANSIT_FAN_COLUMNS) * TRANSIT_FAN_SPACING;
                Circle circle = new Circle(centerX + offsetX, floorCenterY + offsetY, TRANSIT_CIRCLE_RADIUS, Color.web(color));
                transitLayer.getChildren().add(circle);
            }
        }
    }
}

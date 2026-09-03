package com.elevatorsim.gui;

import com.elevatorsim.model.BuildingManager;
import com.elevatorsim.model.Elevator;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

// Draws the whole building: 10 floor rows (floor 9 at the top, floor 0 at
// the bottom), a waiting area per floor, and one animated shaft per
// elevator. refresh() is called on every poll from ElevatorApp and simply
// forwards to the per-floor / per-elevator views, which each read the
// model directly.
class BuildingView extends Pane {

    private static final int NUM_FLOORS = 10;
    private static final double ROW_HEIGHT = 56;
    private static final double FLOOR_LABEL_WIDTH = 36;
    private static final double WAITING_AREA_WIDTH = 100;
    private static final double SHAFT_WIDTH = 84;
    private static final double SHAFT_GAP = 12;
    private static final double CAR_MARGIN = 8;
    private static final double TOP_PAD = 14;
    private static final double LEFT_PAD = 14;

    private final FloorWaitingArea[] waitingAreas = new FloorWaitingArea[NUM_FLOORS];
    private final ElevatorView[] elevatorViews;
    private final BuildingManager manager;

    BuildingView(Elevator[] elevators, BuildingManager manager) {
        this.manager = manager;

        double shaftsX = LEFT_PAD + FLOOR_LABEL_WIDTH + WAITING_AREA_WIDTH;
        double width = shaftsX + elevators.length * SHAFT_WIDTH + (elevators.length - 1) * SHAFT_GAP + LEFT_PAD;
        double height = TOP_PAD * 2 + NUM_FLOORS * ROW_HEIGHT;
        setPrefSize(width, height);
        setStyle("-fx-background-color: #f6f5f0;");

        for (int i = 0; i < elevators.length; i++) {
            double shaftX = shaftsX + i * (SHAFT_WIDTH + SHAFT_GAP);
            Rectangle shaftBg = new Rectangle(shaftX, TOP_PAD, SHAFT_WIDTH, NUM_FLOORS * ROW_HEIGHT);
            shaftBg.setFill(Color.web("#e4e3db"));
            shaftBg.setStroke(Color.web("#bbbbbb"));
            getChildren().add(shaftBg);
        }

        for (int floor = 0; floor < NUM_FLOORS; floor++) {
            double y = rowTopY(floor);

            Line separator = new Line(LEFT_PAD, y + ROW_HEIGHT, width - LEFT_PAD, y + ROW_HEIGHT);
            separator.setStroke(Color.web("#cccccc"));

            Label floorLabel = new Label("F" + floor);
            floorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
            floorLabel.setLayoutX(LEFT_PAD);
            floorLabel.setLayoutY(y + ROW_HEIGHT / 2 - 8);
            floorLabel.setPrefWidth(FLOOR_LABEL_WIDTH);

            FloorWaitingArea waitingArea = new FloorWaitingArea();
            waitingArea.setLayoutX(LEFT_PAD + FLOOR_LABEL_WIDTH + 4);
            waitingArea.setLayoutY(y + ROW_HEIGHT / 2 - 10);
            waitingArea.setPrefWidth(WAITING_AREA_WIDTH - 8);
            waitingAreas[floor] = waitingArea;

            getChildren().addAll(separator, floorLabel, waitingArea);
        }

        elevatorViews = new ElevatorView[elevators.length];
        for (int i = 0; i < elevators.length; i++) {
            double shaftX = shaftsX + i * (SHAFT_WIDTH + SHAFT_GAP);
            double carWidth = SHAFT_WIDTH - 2 * CAR_MARGIN;
            double carHeight = ROW_HEIGHT - 2 * CAR_MARGIN;
            elevatorViews[i] = new ElevatorView(elevators[i], shaftX + CAR_MARGIN, ROW_HEIGHT, carWidth, carHeight,
                    TOP_PAD, NUM_FLOORS);
            getChildren().add(elevatorViews[i].getNode());
        }
    }

    private double rowTopY(int floor) {
        return TOP_PAD + (NUM_FLOORS - 1 - floor) * ROW_HEIGHT;
    }

    void refresh() {
        for (int floor = 0; floor < NUM_FLOORS; floor++) {
            waitingAreas[floor].update(manager.getCurrentRequest(floor));
        }
        for (ElevatorView elevatorView : elevatorViews) {
            elevatorView.refresh();
        }
    }
}

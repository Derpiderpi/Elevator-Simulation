package com.elevatorsim.gui;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

// The crowd of passengers currently waiting at one floor. update() is
// called every poll with BuildingManager.getCurrentRequest(floor) and
// reconciles the visible dots to that count, fading new arrivals in and
// boarded passengers out.
class FloorWaitingArea extends HBox {

    private static final int MAX_VISIBLE = 6;
    private static final Color DOT_COLOR = Color.web("#e2924a");
    private static final Duration FADE_DURATION = Duration.millis(220);

    private final FlowPane dots = new FlowPane(3, 3);
    private final Label overflowLabel = new Label();

    FloorWaitingArea() {
        setSpacing(4);
        setAlignment(Pos.CENTER_LEFT);
        dots.setPrefWrapLength(64);
        overflowLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #666666;");
        getChildren().addAll(dots, overflowLabel);
    }

    void update(int waitingCount) {
        int target = Math.min(waitingCount, MAX_VISIBLE);
        int current = dots.getChildren().size();
        if (target > current) {
            for (int i = current; i < target; i++) {
                addDot();
            }
        } else if (target < current) {
            List<Node> toRemove = new ArrayList<>(dots.getChildren().subList(target, current));
            for (Node dot : toRemove) {
                removeDot(dot);
            }
        }
        overflowLabel.setText(waitingCount > MAX_VISIBLE ? "+" + (waitingCount - MAX_VISIBLE) : "");
    }

    private void addDot() {
        Node dot = PassengerToken.create(DOT_COLOR);
        dot.setOpacity(0);
        dots.getChildren().add(dot);
        FadeTransition fade = new FadeTransition(FADE_DURATION, dot);
        fade.setToValue(1);
        fade.play();
    }

    private void removeDot(Node dot) {
        FadeTransition fade = new FadeTransition(FADE_DURATION, dot);
        fade.setToValue(0);
        fade.setOnFinished(e -> dots.getChildren().remove(dot));
        fade.play();
    }
}

package com.elevatorsim.gui;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

// A single small dot representing one passenger, reused by the floor
// waiting areas and the elevator cars.
final class PassengerToken {

    private PassengerToken() {
    }

    static Circle create(Color color) {
        Circle dot = new Circle(5, color);
        dot.setStroke(Color.web("#222222"));
        dot.setStrokeWidth(0.75);
        return dot;
    }
}

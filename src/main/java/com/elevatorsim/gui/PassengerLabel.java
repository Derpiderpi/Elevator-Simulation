package com.elevatorsim.gui;

import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

// Shared factory for the small destination-floor digit labels drawn inside passenger
// circles (floor waiting grid, elevator riding grid, and the transit fan). Uses a fixed
// empirical X offset rather than per-frame getLayoutBounds() measurement, since digits
// 0-9 in a bold font are near-uniform width.
final class PassengerLabel {

    static final double FONT_SIZE = 9;
    private static final double X_OFFSET = FONT_SIZE * 0.30;

    private PassengerLabel() {
    }

    static Text create() {
        Text text = new Text();
        text.setFont(Font.font("System", FontWeight.BOLD, FONT_SIZE));
        text.setFill(Color.web(BuildingView.PASSENGER_LABEL_COLOR));
        text.setMouseTransparent(true);
        text.setTextOrigin(VPos.CENTER);
        return text;
    }

    static void position(Text text, double centerX, double centerY) {
        text.setX(centerX - X_OFFSET);
        text.setY(centerY);
    }
}

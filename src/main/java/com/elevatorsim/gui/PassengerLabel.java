package com.elevatorsim.gui;

import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

// Shared factory for the small destination-floor digit labels drawn over passenger spheres
// (floor waiting pool, elevator riding pool, and the transit fan) and over the floor-number
// markers. Labels live in a 2D overlay Pane stacked on top of the 3D SubScene, since JavaFX
// Text does not billboard automatically inside a 3D scene graph; they are repositioned every
// frame from their anchor node's live screen projection via Node.localToScreen, so they track
// both moving geometry (a translating car, an in-flight transit) and camera movement (orbit,
// zoom) uniformly.
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

    // Projects anchor's local origin through its full 3D transform chain and the scene's
    // camera to a screen point, then converts that into overlay's local coordinate space.
    // Leaves `text` at its last position (a one-frame no-op, self-correcting next frame) if
    // the anchor isn't currently part of a shown, laid-out scene.
    static void position(Text text, Node anchor, Pane overlay) {
        Point2D screenPoint = anchor.localToScreen(0, 0, 0);
        if (screenPoint == null) {
            return;
        }
        Point2D local = overlay.screenToLocal(screenPoint);
        if (local == null) {
            return;
        }
        text.setX(local.getX() - X_OFFSET);
        text.setY(local.getY());
    }
}

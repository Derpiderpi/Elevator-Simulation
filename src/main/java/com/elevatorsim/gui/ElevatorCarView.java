package com.elevatorsim.gui;

import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;

// Renders a single elevator car as a 3D box with one sphere per riding passenger, each labeled
// with its destination floor and colored by travel direction. Vertical position is set
// continuously by the caller as the simulation's animation timer recomputes each elevator's
// interpolated floor position every frame. The car is a Group translated as a whole
// (translateX fixed once by the caller to its shaft's center, translateY updated every frame
// by updatePosition); passenger spheres are children positioned at fixed local offsets around
// the car's own center, so they move with the car automatically. Digit labels can't live
// inside the 3D group (Text doesn't billboard), so they are separate Text nodes added to a
// shared 2D overlay Pane and repositioned every frame from each sphere's live screen
// projection.
public class ElevatorCarView extends Group {

    static final double CAR_DEPTH = 60;

    private static final int MAX_RIDING_DISPLAY = 12;
    private static final int RIDING_GRID_COLUMNS = 6;
    private static final double RIDING_SPHERE_RADIUS = 6;
    private static final double RIDING_GRID_SPACING = 14;
    private static final double RIDING_GRID_X0 = -(RIDING_GRID_COLUMNS - 1) * RIDING_GRID_SPACING / 2.0;
    private static final double RIDING_GRID_Z0 = -RIDING_GRID_SPACING / 2.0;

    private final Pane overlay;
    private final Sphere[] passengerSpheres = new Sphere[MAX_RIDING_DISPLAY];
    private final Text[] passengerLabels = new Text[MAX_RIDING_DISPLAY];

    public ElevatorCarView(String fillColor, Pane overlay) {
        this.overlay = overlay;

        Box body = new Box(BuildingView.CAR_WIDTH, BuildingView.CAR_HEIGHT, CAR_DEPTH);
        body.setMaterial(new PhongMaterial(Color.web(fillColor)));
        getChildren().add(body);

        for (int i = 0; i < MAX_RIDING_DISPLAY; i++) {
            double x = RIDING_GRID_X0 + (i % RIDING_GRID_COLUMNS) * RIDING_GRID_SPACING;
            double z = RIDING_GRID_Z0 + (i / RIDING_GRID_COLUMNS) * RIDING_GRID_SPACING;

            Sphere sphere = new Sphere(RIDING_SPHERE_RADIUS);
            sphere.setTranslateX(x);
            sphere.setTranslateZ(z);
            sphere.setVisible(false);
            passengerSpheres[i] = sphere;
            getChildren().add(sphere);

            Text digit = PassengerLabel.create();
            digit.setVisible(false);
            passengerLabels[i] = digit;
            overlay.getChildren().add(digit);
        }
    }

    public void updatePosition(double floorPosition) {
        double y = BuildingView.TOP_MARGIN
                + (BuildingView.FLOOR_COUNT - 1 - floorPosition) * BuildingView.FLOOR_HEIGHT
                + BuildingView.FLOOR_HEIGHT / 2.0;
        setTranslateY(y);
    }

    public void updateRidingCounts(int[] countsByDestination, int originFloor) {
        int slot = 0;
        for (int dest = 0; dest < 10 && slot < MAX_RIDING_DISPLAY; dest++) {
            PhongMaterial material = dest > originFloor ? BuildingView.UP_MATERIAL : BuildingView.DOWN_MATERIAL;
            for (int n = 0; n < countsByDestination[dest] && slot < MAX_RIDING_DISPLAY; n++, slot++) {
                passengerSpheres[slot].setMaterial(material);
                passengerSpheres[slot].setVisible(true);
                passengerLabels[slot].setText(Integer.toString(dest));
                passengerLabels[slot].setVisible(true);
            }
        }
        for (; slot < MAX_RIDING_DISPLAY; slot++) {
            passengerSpheres[slot].setVisible(false);
            passengerLabels[slot].setVisible(false);
        }
        // Repositions every visible label from its sphere's current screen projection, both so
        // a newly shown label starts correctly placed and so labels keep tracking the car as it
        // moves and the camera orbits/zooms, since this runs every rendered frame.
        for (int i = 0; i < MAX_RIDING_DISPLAY; i++) {
            if (passengerSpheres[i].isVisible()) {
                PassengerLabel.position(passengerLabels[i], passengerSpheres[i], overlay);
            }
        }
    }
}

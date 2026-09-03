package com.elevatorsim.gui;

import java.util.List;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

// Renders the building's 10 floors and 3 elevator shafts as a 3D scene (floor slabs and shaft
// columns as Box geometry, one sphere per waiting passenger per floor labeled with their
// destination floor and colored by travel direction), hosts one ElevatorCarView per shaft, and
// lets the user orbit/zoom the camera with the mouse. Layout is computed once in the
// constructor for the persistent floor/car nodes; per-frame updates toggle their visibility and
// rebuild the short-lived passenger-transfer animation layer on top. Digit labels are 2D Text
// nodes in an overlay Pane stacked on top of the 3D SubScene (JavaFX Text does not billboard
// inside a 3D scene graph), repositioned every frame from their anchor node's live screen
// projection via Node.localToScreen so they track both moving geometry and camera movement.
public class BuildingView extends StackPane {

    public static final int FLOOR_COUNT = 10;
    public static final int SHAFT_COUNT = 3;
    public static final double FLOOR_HEIGHT = 50;
    public static final double SHAFT_WIDTH = 130;
    public static final double LEFT_MARGIN = 150;
    public static final double TOP_MARGIN = 20;
    public static final double CAR_WIDTH = 96;
    public static final double CAR_HEIGHT = 36;

    private static final double BUILDING_DEPTH = 200;
    private static final double FLOOR_SLAB_THICKNESS = 10;
    private static final double COLUMN_THICKNESS = 6;

    // Direction-based passenger sphere colors, and the digit-label color that reads clearly
    // against both (see design.md for the contrast/consistency rationale).
    public static final String UP_COLOR = "#22C55E";
    public static final String DOWN_COLOR = "#F97316";
    public static final String PASSENGER_LABEL_COLOR = "#1F2937";

    // Shared, stateless materials reused across every waiting/riding/transit sphere, so
    // direction changes are a material swap rather than a per-sphere allocation.
    static final PhongMaterial UP_MATERIAL = new PhongMaterial(Color.web(UP_COLOR));
    static final PhongMaterial DOWN_MATERIAL = new PhongMaterial(Color.web(DOWN_COLOR));

    private static final int MAX_WAITING_DISPLAY = 12;
    private static final int WAITING_GRID_COLUMNS = 6;
    private static final double WAITING_SPHERE_RADIUS = 6;
    private static final double WAITING_GRID_SPACING = 14;
    private static final double WAITING_GRID_X0 = 68;
    private static final double WAITING_GRID_Z0 = -WAITING_GRID_SPACING / 2.0;
    private static final double WAITING_AREA_CENTER_X =
            WAITING_GRID_X0 + (WAITING_GRID_COLUMNS - 1) * WAITING_GRID_SPACING / 2.0;

    private static final double TRANSIT_SPHERE_RADIUS = 6;
    private static final int TRANSIT_FAN_COLUMNS = 6;
    private static final double TRANSIT_FAN_SPACING = 14;

    private static final String[] CAR_COLORS = {"#3B82F6", "#EF4444", "#10B981"};

    private static final double INITIAL_YAW_DEGREES = 25;
    private static final double INITIAL_PITCH_DEGREES = -18;
    private static final double INITIAL_DISTANCE = 950;

    private final Pane overlay = new Pane();
    private final ElevatorCarView[] carViews = new ElevatorCarView[SHAFT_COUNT];
    private final Sphere[][] waitingSpheres = new Sphere[FLOOR_COUNT][MAX_WAITING_DISPLAY];
    private final Text[][] waitingLabels = new Text[FLOOR_COUNT][MAX_WAITING_DISPLAY];
    private final Group transitLayer = new Group();
    private final Pane transitLabelLayer = new Pane();
    private final Group[] floorLabelAnchors = new Group[FLOOR_COUNT];
    private final Text[] floorLabelTexts = new Text[FLOOR_COUNT];

    private final CameraController cameraController =
            new CameraController(INITIAL_YAW_DEGREES, INITIAL_PITCH_DEGREES, INITIAL_DISTANCE);
    private final Rotate cameraYaw = new Rotate(INITIAL_YAW_DEGREES, Rotate.Y_AXIS);
    private final Rotate cameraPitch = new Rotate(INITIAL_PITCH_DEGREES, Rotate.X_AXIS);
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    private double lastDragX;
    private double lastDragY;

    public BuildingView() {
        double width = LEFT_MARGIN + SHAFT_COUNT * SHAFT_WIDTH;
        double height = TOP_MARGIN + FLOOR_COUNT * FLOOR_HEIGHT + TOP_MARGIN;
        setPrefSize(width, height);

        Group world = new Group();

        for (int floor = 0; floor < FLOOR_COUNT; floor++) {
            double rowTopY = TOP_MARGIN + (FLOOR_COUNT - 1 - floor) * FLOOR_HEIGHT;
            double rowCenterY = rowTopY + FLOOR_HEIGHT / 2.0;

            Box slab = new Box(SHAFT_COUNT * SHAFT_WIDTH, FLOOR_SLAB_THICKNESS, BUILDING_DEPTH);
            slab.setMaterial(new PhongMaterial(Color.web("#374151")));
            slab.setTranslateX(LEFT_MARGIN + SHAFT_COUNT * SHAFT_WIDTH / 2.0);
            slab.setTranslateY(rowTopY + FLOOR_HEIGHT);
            world.getChildren().add(slab);

            Group floorLabelAnchor = new Group();
            floorLabelAnchor.setTranslateX(10);
            floorLabelAnchor.setTranslateY(rowCenterY);
            world.getChildren().add(floorLabelAnchor);

            Text label = PassengerLabel.create();
            label.setText("Floor " + floor);
            overlay.getChildren().add(label);
            floorLabelAnchors[floor] = floorLabelAnchor;
            floorLabelTexts[floor] = label;

            for (int i = 0; i < MAX_WAITING_DISPLAY; i++) {
                double x = WAITING_GRID_X0 + (i % WAITING_GRID_COLUMNS) * WAITING_GRID_SPACING;
                double z = WAITING_GRID_Z0 + (i / WAITING_GRID_COLUMNS) * WAITING_GRID_SPACING;

                Sphere sphere = new Sphere(WAITING_SPHERE_RADIUS);
                sphere.setTranslateX(x);
                sphere.setTranslateY(rowCenterY);
                sphere.setTranslateZ(z);
                sphere.setVisible(false);
                waitingSpheres[floor][i] = sphere;
                world.getChildren().add(sphere);

                Text digit = PassengerLabel.create();
                digit.setVisible(false);
                waitingLabels[floor][i] = digit;
                overlay.getChildren().add(digit);
            }
        }

        for (int boundary = 0; boundary <= SHAFT_COUNT; boundary++) {
            double x = LEFT_MARGIN + boundary * SHAFT_WIDTH;

            Box column = new Box(COLUMN_THICKNESS, FLOOR_COUNT * FLOOR_HEIGHT, BUILDING_DEPTH);
            column.setMaterial(new PhongMaterial(Color.web("#374151")));
            column.setTranslateX(x);
            column.setTranslateY(TOP_MARGIN + FLOOR_COUNT * FLOOR_HEIGHT / 2.0);
            world.getChildren().add(column);
        }

        for (int shaft = 0; shaft < SHAFT_COUNT; shaft++) {
            double shaftCenterX = LEFT_MARGIN + shaft * SHAFT_WIDTH + SHAFT_WIDTH / 2.0;

            ElevatorCarView car = new ElevatorCarView(CAR_COLORS[shaft % CAR_COLORS.length], overlay);
            car.setTranslateX(shaftCenterX);
            car.updatePosition(0);
            carViews[shaft] = car;
            world.getChildren().add(car);
        }

        world.getChildren().add(transitLayer);

        AmbientLight ambientLight = new AmbientLight(Color.web("#606060"));
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(width / 2.0);
        pointLight.setTranslateY(-200);
        pointLight.setTranslateZ(-400);
        world.getChildren().addAll(ambientLight, pointLight);

        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setTranslateZ(-cameraController.getDistance());
        Group cameraPitchGroup = new Group(camera);
        cameraPitchGroup.getTransforms().add(cameraPitch);
        Group cameraYawGroup = new Group(cameraPitchGroup);
        cameraYawGroup.getTransforms().add(cameraYaw);
        cameraYawGroup.setTranslateX(width / 2.0);
        cameraYawGroup.setTranslateY(height / 2.0);

        Group subSceneRoot = new Group(world, cameraYawGroup);
        SubScene subScene = new SubScene(subSceneRoot, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#111827"));
        subScene.setCamera(camera);

        overlay.setPrefSize(width, height);
        overlay.setMouseTransparent(true);
        overlay.getChildren().add(transitLabelLayer);

        getChildren().addAll(subScene, overlay);

        subScene.setOnMousePressed(event -> {
            lastDragX = event.getSceneX();
            lastDragY = event.getSceneY();
        });
        subScene.setOnMouseDragged(event -> {
            cameraController.drag(event.getSceneX() - lastDragX, event.getSceneY() - lastDragY);
            lastDragX = event.getSceneX();
            lastDragY = event.getSceneY();
            applyCameraState();
        });
        subScene.setOnScroll(this::handleScroll);
    }

    private void handleScroll(ScrollEvent event) {
        cameraController.zoom(event.getDeltaY());
        applyCameraState();
    }

    private void applyCameraState() {
        cameraYaw.setAngle(cameraController.getYawDegrees());
        cameraPitch.setAngle(cameraController.getPitchDegrees());
        camera.setTranslateZ(-cameraController.getDistance());
    }

    public void updateElevator(int shaftIndex, double floorPosition, int[] ridingCountsByDestination, int ridingOriginFloor) {
        carViews[shaftIndex].updatePosition(floorPosition);
        carViews[shaftIndex].updateRidingCounts(ridingCountsByDestination, ridingOriginFloor);
    }

    public void updateFloorWaiting(int floor, int[] countsByDestination) {
        PassengerLabel.position(floorLabelTexts[floor], floorLabelAnchors[floor], overlay);

        Sphere[] spheres = waitingSpheres[floor];
        Text[] labels = waitingLabels[floor];
        int slot = 0;
        for (int dest = 0; dest < FLOOR_COUNT && slot < MAX_WAITING_DISPLAY; dest++) {
            PhongMaterial material = dest > floor ? UP_MATERIAL : DOWN_MATERIAL;
            for (int n = 0; n < countsByDestination[dest] && slot < MAX_WAITING_DISPLAY; n++, slot++) {
                spheres[slot].setMaterial(material);
                spheres[slot].setVisible(true);
                labels[slot].setText(Integer.toString(dest));
                labels[slot].setVisible(true);
                PassengerLabel.position(labels[slot], spheres[slot], overlay);
            }
        }
        for (; slot < MAX_WAITING_DISPLAY; slot++) {
            spheres[slot].setVisible(false);
            labels[slot].setVisible(false);
        }
    }

    // Rebuilds the transient transfer-animation layer from the currently active transits.
    // Rebuilding per frame is acceptable here since transits are short-lived and few, unlike
    // the persistent floor/car sphere pools above which stay fixed-size to avoid node churn.
    public void updateTransits(List<PassengerTransit> activeTransits, long nowMs) {
        transitLayer.getChildren().clear();
        transitLabelLayer.getChildren().clear();
        for (PassengerTransit transit : activeTransits) {
            double progress = transit.computeProgress(nowMs);
            double floorCenterY = TOP_MARGIN + (FLOOR_COUNT - 1 - transit.getFloor()) * FLOOR_HEIGHT + FLOOR_HEIGHT / 2.0;
            double shaftCenterX = LEFT_MARGIN + transit.getShaftIndex() * SHAFT_WIDTH + SHAFT_WIDTH / 2.0;

            double fromX = transit.getKind() == PassengerTransit.Kind.PICKUP ? WAITING_AREA_CENTER_X : shaftCenterX;
            double toX = transit.getKind() == PassengerTransit.Kind.PICKUP ? shaftCenterX : WAITING_AREA_CENTER_X;
            double centerX = fromX + (toX - fromX) * progress;

            int[] counts = transit.getCountsByDestination();
            int originFloor = transit.getOriginFloor();
            int slot = 0;
            for (int dest = 0; dest < FLOOR_COUNT && slot < MAX_WAITING_DISPLAY; dest++) {
                PhongMaterial material = dest > originFloor ? UP_MATERIAL : DOWN_MATERIAL;
                for (int n = 0; n < counts[dest] && slot < MAX_WAITING_DISPLAY; n++, slot++) {
                    double offsetX = (slot % TRANSIT_FAN_COLUMNS - (TRANSIT_FAN_COLUMNS - 1) / 2.0) * TRANSIT_FAN_SPACING;
                    double offsetZ = (slot / TRANSIT_FAN_COLUMNS - 0.5) * TRANSIT_FAN_SPACING;
                    double x = centerX + offsetX;
                    double z = offsetZ;

                    Sphere sphere = new Sphere(TRANSIT_SPHERE_RADIUS);
                    sphere.setMaterial(material);
                    sphere.setTranslateX(x);
                    sphere.setTranslateY(floorCenterY);
                    sphere.setTranslateZ(z);
                    transitLayer.getChildren().add(sphere);

                    Text digit = PassengerLabel.create();
                    digit.setText(Integer.toString(dest));
                    transitLabelLayer.getChildren().add(digit);
                    PassengerLabel.position(digit, sphere, overlay);
                }
            }
        }
    }
}

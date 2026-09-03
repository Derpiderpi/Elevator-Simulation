package com.elevatorsim.gui;

// Accumulates orbit-camera state (yaw, pitch, distance) from mouse-drag and scroll input
// deltas. Deliberately has no javafx.* imports, so it can be unit-tested headlessly, mirroring
// ElevatorInterpolator/PassengerTransit's JavaFX-free style; the view layer is responsible for
// applying the resulting yaw/pitch/distance to actual Rotate/Translate nodes.
public final class CameraController {

    public static final double MIN_PITCH_DEGREES = -80;
    public static final double MAX_PITCH_DEGREES = 10;
    public static final double MIN_DISTANCE = 200;
    public static final double MAX_DISTANCE = 2000;

    private static final double YAW_DEGREES_PER_PIXEL = 0.3;
    private static final double PITCH_DEGREES_PER_PIXEL = 0.3;
    private static final double DISTANCE_PER_SCROLL_UNIT = 1.0;

    private double yawDegrees;
    private double pitchDegrees;
    private double distance;

    public CameraController(double initialYawDegrees, double initialPitchDegrees, double initialDistance) {
        this.yawDegrees = initialYawDegrees;
        this.pitchDegrees = clampPitch(initialPitchDegrees);
        this.distance = clampDistance(initialDistance);
    }

    // Applies a mouse-drag delta (in pixels, current position minus press/last position) to
    // yaw and pitch. Yaw accumulates without bound (orbiting all the way around is fine);
    // pitch is clamped so the camera can't flip past looking straight up or down.
    public void drag(double deltaX, double deltaY) {
        yawDegrees += deltaX * YAW_DEGREES_PER_PIXEL;
        pitchDegrees = clampPitch(pitchDegrees - deltaY * PITCH_DEGREES_PER_PIXEL);
    }

    // Applies a scroll delta (JavaFX ScrollEvent.getDeltaY() convention: positive scrolls up,
    // which zooms in / decreases distance) clamped to [MIN_DISTANCE, MAX_DISTANCE].
    public void zoom(double scrollDeltaY) {
        distance = clampDistance(distance - scrollDeltaY * DISTANCE_PER_SCROLL_UNIT);
    }

    public double getYawDegrees() {
        return yawDegrees;
    }

    public double getPitchDegrees() {
        return pitchDegrees;
    }

    public double getDistance() {
        return distance;
    }

    private static double clampPitch(double pitch) {
        return Math.max(MIN_PITCH_DEGREES, Math.min(MAX_PITCH_DEGREES, pitch));
    }

    private static double clampDistance(double distance) {
        return Math.max(MIN_DISTANCE, Math.min(MAX_DISTANCE, distance));
    }
}

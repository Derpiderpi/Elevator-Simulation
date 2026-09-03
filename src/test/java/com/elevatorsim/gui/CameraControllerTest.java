package com.elevatorsim.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CameraControllerTest {

    private static final double DELTA = 1e-9;

    @Test
    void dragAccumulatesYawAcrossRepeatedDrags() {
        CameraController camera = new CameraController(0, 0, 500);
        camera.drag(10, 0);
        camera.drag(10, 0);
        assertEquals(20 * 0.3, camera.getYawDegrees(), DELTA);
    }

    @Test
    void yawIsUnboundedAndCanExceedAFullTurn() {
        CameraController camera = new CameraController(0, 0, 500);
        camera.drag(2000, 0);
        assertTrue(camera.getYawDegrees() > 360.0);
    }

    @Test
    void pitchClampsAtUpperBoundInsteadOfFlipping() {
        CameraController camera = new CameraController(0, 0, 500);
        camera.drag(0, -10_000);
        assertEquals(CameraController.MAX_PITCH_DEGREES, camera.getPitchDegrees(), DELTA);
    }

    @Test
    void pitchClampsAtLowerBoundInsteadOfFlipping() {
        CameraController camera = new CameraController(0, 0, 500);
        camera.drag(0, 10_000);
        assertEquals(CameraController.MIN_PITCH_DEGREES, camera.getPitchDegrees(), DELTA);
    }

    @Test
    void constructorClampsAnOutOfRangeInitialPitch() {
        CameraController camera = new CameraController(0, 999, 500);
        assertEquals(CameraController.MAX_PITCH_DEGREES, camera.getPitchDegrees(), DELTA);
    }

    @Test
    void zoomInDecreasesDistanceButClampsAtMinimum() {
        CameraController camera = new CameraController(0, 0, CameraController.MIN_DISTANCE + 5);
        camera.zoom(1000);
        assertEquals(CameraController.MIN_DISTANCE, camera.getDistance(), DELTA);
    }

    @Test
    void zoomOutIncreasesDistanceButClampsAtMaximum() {
        CameraController camera = new CameraController(0, 0, CameraController.MAX_DISTANCE - 5);
        camera.zoom(-1000);
        assertEquals(CameraController.MAX_DISTANCE, camera.getDistance(), DELTA);
    }

    @Test
    void constructorClampsAnOutOfRangeInitialDistance() {
        CameraController camera = new CameraController(0, 0, 999_999);
        assertEquals(CameraController.MAX_DISTANCE, camera.getDistance(), DELTA);
    }

    @Test
    void zoomWithinRangeAppliesDeltaExactly() {
        CameraController camera = new CameraController(0, 0, 500);
        camera.zoom(50);
        assertEquals(450, camera.getDistance(), DELTA);
    }
}

## Why

The GUI currently renders the building as a flat 2D `Pane` (floors as
horizontal rows, shafts as vertical columns, passengers as flat circles).
This makes it hard to read the building as a physical space — every floor
looks the same distance from the viewer, and there is no way to look at
the building from a different angle. Rendering the sandbox as an actual
3D scene, with a camera the user can orbit and zoom, gives a much more
legible and inspectable view of elevator movement without changing any
simulation behavior.

## What Changes

- Replace the 2D `Pane`-based `BuildingView` with a JavaFX 3D scene
  (`SubScene` + `PerspectiveCamera`) embedded in the existing application
  window, using JavaFX's built-in 3D shape/material/transform APIs
  (`Box`, `Sphere`, `PhongMaterial`, `Rotate`, `Translate`) — no new
  Maven dependency, since `javafx-controls` already pulls in
  `javafx-graphics`, which includes 3D support.
- Render each floor as a stacked 3D slab (`Box`) and each elevator shaft
  as a 3D column, laid out at fixed positions in 3D world space instead
  of 2D screen space.
- Render each elevator car as a 3D box that translates vertically along
  its shaft, using the same floor-position interpolation math that
  already drives the 2D car (`ElevatorInterpolator` is unchanged).
- Render waiting and riding passengers as 3D spheres (`Sphere` +
  `PhongMaterial`) instead of 2D circles, keeping the existing
  destination-floor digit label and up/down direction coloring. Digit
  labels are drawn as a 2D overlay layer positioned each frame by
  projecting each sphere's 3D world position to 2D screen coordinates
  (JavaFX `Text` does not billboard automatically inside a 3D scene
  graph), rather than as `Text` nodes embedded in the 3D scene.
- Reimplement the passenger pickup/dropoff transfer animation
  (`PassengerTransit`/`PassengerTransitManager` logic is unchanged) to
  move spheres through 3D space between a floor's waiting position and
  the elevator car, instead of across a 2D pane.
- Add basic camera controls: dragging the mouse orbits the camera around
  the building's vertical axis, and the scroll wheel zooms the camera
  in/out, so the user can view the building from different angles.
- Preserve all existing animation timing, passenger counts/colors/labels,
  the 12-passenger display cap per location, and the console-only entry
  point (`com.elevatorsim.Simulator`) exactly as they are today. This
  change is scoped to the rendering dimension and camera; it does not
  touch simulation, scheduling, or animation-timing logic.

## Capabilities

### New Capabilities

(none)

### Modified Capabilities

- `elevator-gui-animation`: "Building Visualization", "Passenger Load
  Display", "Floor Call Indication", and "Passenger Transfer Animation"
  change from flat 2D shapes to 3D geometry (slabs/columns/boxes/spheres)
  positioned in a 3D scene; a new "Camera Orbit and Zoom Controls"
  requirement is added for user-driven camera movement.

## Impact

- `com.elevatorsim.gui.BuildingView`: reworked from a 2D `Pane` to a 3D
  scene graph (`Group` of `Box`/`Sphere` nodes) hosted inside a
  `SubScene`, plus a 2D overlay `Pane` for digit labels and camera-drag
  bookkeeping.
- `com.elevatorsim.gui.ElevatorCarView`: reworked from a 2D `Pane` with a
  `Rectangle` body to a 3D `Box`-based car with `Sphere` passenger
  markers, still positioned via the existing `updatePosition`/
  `updateRidingCounts` interface used by `ElevatorGuiApp`.
- `com.elevatorsim.gui.PassengerLabel`: reworked to position a `Text`
  node from a projected 3D-to-2D screen coordinate instead of a fixed 2D
  layout coordinate.
- New `com.elevatorsim.gui.CameraController` (or similarly named) class
  encapsulating orbit/zoom mouse-input handling, keeping
  `ElevatorGuiApp`'s per-frame render loop and simulation wiring
  unchanged.
- `com.elevatorsim.gui.ElevatorGuiApp`: swaps the `Scene(buildingView)`
  root for the new 3D-capable view; the simulation thread,
  `AnimationTimer`, and per-frame polling of `ElevatorSimulation` are
  unchanged.
- No change to `com.elevatorsim.Elevator`, `BuildingManager`,
  `Simulator`, `SimClock`, `PassengerTransit`, `PassengerTransitManager`,
  or `ElevatorInterpolator` — all simulation and animation-timing logic
  is reused as-is.
- No new Maven dependency; `pom.xml` is unchanged.

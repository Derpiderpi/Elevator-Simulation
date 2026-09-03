## Context

Today `BuildingView`/`ElevatorCarView` are 2D `Pane`s: floors and shafts
are laid out with fixed pixel `x`/`y` math (`TOP_MARGIN`, `FLOOR_HEIGHT`,
`LEFT_MARGIN`, `SHAFT_WIDTH`), and passengers are `Circle`+`Text` pairs
positioned by that same pixel math, pooled in fixed-size arrays and
toggled visible/invisible per frame (see `BuildingView.java`,
`ElevatorCarView.java`, `PassengerLabel.java`). `ElevatorInterpolator`
and `PassengerTransit`/`PassengerTransitManager` are deliberately
JavaFX-free and return unitless values (a floor-position `double`, a
`[0,1]` progress) — all pixel/coordinate mapping happens in the view
layer. See proposal.md - Why / Impact for motivation and scope.

## Goals / Non-Goals

**Goals:**
- Replace the *rendering* of floors, shafts, cars, and passengers with
  real 3D geometry (`Box`, `Sphere`) positioned in 3D world space, driven
  by the same interpolation/progress values the 2D view already
  consumes.
- Give the user mouse-drag orbit and scroll-wheel zoom over that scene.
- Keep every existing spec requirement (counts, caps, colors, digit
  labels, transfer animation) observably identical from a "what does the
  user see happen" standpoint — only the rendering dimension changes.

**Non-Goals:**
- Realistic lighting/materials, shadows, or textures — a single
  `AmbientLight` + `PointLight` for basic Phong shading readability is
  enough; this is a visualization, not a renderer showcase.
- Making passenger spheres or cars mouse-interactive/selectable.
- Persisting camera position across runs, or supporting more than one
  camera/viewport.
- Any change to `Elevator`, `BuildingManager`, `Simulator`, `SimClock`,
  `ElevatorInterpolator`, `PassengerTransit`, or
  `PassengerTransitManager` — all reused unchanged.

## Decisions

**`SubScene` + `PerspectiveCamera` inside the existing `Scene`, not a
top-level 3D `Scene`.** `Application.start()` keeps building one JavaFX
`Scene` for the `Stage`; that scene's root becomes a `StackPane`
containing the 3D `SubScene` (the building geometry) with a transparent,
mouse-transparent 2D `Pane` overlay on top (the digit labels). A
`StackPane` lets both layers fill the same area and resize together
without manual size binding beyond `SubScene.width/height`.

**World coordinates reuse the existing 2D layout formulas, plus a Z
axis.** `floorY(floor) = TOP_MARGIN + (FLOOR_COUNT - 1 - floor) *
FLOOR_HEIGHT` and `shaftX(shaft) = LEFT_MARGIN + shaft * SHAFT_WIDTH`
carry over unchanged (same sign convention: JavaFX's Y axis increases
downward, so floor 0 sits at the largest Y, i.e., visually at the
bottom). A new fixed `DEPTH` constant gives floor slabs, shaft columns,
and cars a Z-extent so orbiting the camera actually reveals a 3D shape
instead of a flat plane. This keeps `ElevatorInterpolator`'s output (a
floor-position double) and `PassengerTransit`'s output (a `[0,1]`
progress) directly reusable: the view layer just adds a `z` term where
today it only computes `x`/`y`.

**Digit labels stay 2D `Text` nodes, positioned via
`Node.localToScreen(x, y, z)`.** JavaFX `Text` does not billboard
automatically inside a 3D scene graph, and embedding it as a 3D node
would make it rotate out of legibility as the camera orbits. Instead,
each passenger sphere's label is a `Text` in the 2D overlay `Pane`,
repositioned every frame by projecting that sphere's current 3D local
position through `Node.localToScreen(double, double, double)` (the
JavaFX 9+ API that resolves a local 3D point to a 2D screen point
through the full transform chain, including the scene's camera). This
keeps the same pooled-node/toggle-visibility pattern `PassengerLabel`
already uses, just with a per-frame position recompute instead of a
one-time layout.

**Camera is an orbit/zoom rig built from `Rotate` + `Translate`, not a
free-fly camera.** A `cameraPivot` `Group` is placed at the building's
center, holding a yaw `Rotate` (around the world Y axis) and a pitch
`Rotate` (around the local X axis, clamped to avoid flipping past
straight-down/up); the `PerspectiveCamera` itself is a child translated
back along Z by a `distance` value. Mouse-drag deltas update
yaw/pitch; scroll deltas update `distance`, clamped to a min/max range
so the user can't zoom through or arbitrarily far from the building.
This is the standard JavaFX orbit-camera pattern and keeps all the
interesting math (angle/zoom accumulation and clamping) in one small,
headlessly-testable class (`CameraController`) that takes input deltas
and produces yaw/pitch/distance — mirroring how `ElevatorInterpolator`
and `PassengerTransit` keep their math JavaFX-free and unit-testable,
with only the final `Rotate`/`Translate` property writes touching
JavaFX types.

**Passenger/car pooling strategy is unchanged.** `BuildingView` and
`ElevatorCarView` keep fixed-size arrays of pre-created `Sphere`+`Text`
pairs (up to the existing 12-per-location cap) toggled visible/invisible
per frame, exactly like today's `Circle`+`Text` pools — only the node
types and position math change, not the pooling/update strategy or the
`updateElevator`/`updateFloorWaiting`/`updateTransits` method contracts
`ElevatorGuiApp` calls into.

**Transit layer stays a rebuilt-per-frame `Group`.** `updateTransits`
keeps clearing and rebuilding a small `Group` of transiting
sphere+label pairs each frame (as `transitLayer` does today), since
transits are short-lived and few; this becomes a 3D `Group` of `Sphere`
nodes plus corresponding 2D overlay labels, positioned by interpolating
`fromX/Y/Z` to `toX/Y/Z` using `PassengerTransit.computeProgress`
exactly as the 2D version interpolates `fromX` to `toX`.

## Risks / Trade-offs

- [Risk] No display server is available in this environment to visually
  verify the 3D rendering or camera controls. → Mitigation: same
  approach as the original GUI change — verify via compilation, unit
  tests on the pure-Java camera math (`CameraController`), and a
  `mvn javafx:run` smoke test that resolves JavaFX/loads the app and
  fails only at `Unable to open DISPLAY`; visual confirmation is left to
  the user.
- [Risk] `Node.localToScreen(x, y, z)` can return `null` (or a stale
  value) if the node isn't yet part of a shown, laid-out scene. →
  Mitigation: guard for a `null` result by skipping that label for the
  current frame only (it's recomputed every frame from the sphere's
  live position, so a skipped frame just means a one-frame flicker, not
  a stuck label).
- [Risk] Free camera orbiting means floors/cars can occlude each other
  or a label can be viewed edge-on, unlike the always-fully-visible 2D
  layout. → Mitigation: accepted trade-off of true 3D viewing — data is
  never lost, only temporarily harder to see from a given angle; the
  user can orbit back to a clearer view.
- [Risk] Reworking `BuildingView`/`ElevatorCarView` internals could
  accidentally change the `updateElevator`/`updateFloorWaiting`/
  `updateTransits` contract `ElevatorGuiApp` depends on. → Mitigation:
  keep those method signatures and semantics identical; `ElevatorGuiApp`
  should need no changes beyond how it constructs the root `Scene`.

## Migration Plan

Not applicable — purely a rendering-layer replacement in the GUI, no
deployment or data migration involved; the console-only entry point
(`com.elevatorsim.Simulator`) is untouched and unaffected.

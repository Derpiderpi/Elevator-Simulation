## MODIFIED Requirements

### Requirement: Passenger Load Display
The GUI SHALL display each elevator's current riding passengers as one
circle per passenger, up to a maximum of 12 simultaneously displayed
circles. Each circle SHALL be labeled with a single digit denoting that
passenger's destination floor and colored according to whether that
passenger is traveling up or down relative to the floor where their
current ride began. The GUI SHALL update the displayed circles when the
passenger count or destination breakdown changes.

#### Scenario: Passenger count changes
- **WHEN** an elevator's current passenger count changes
- **THEN** the number of rendered passenger circles for that elevator
  updates to match the new count within one rendered frame, up to the
  12-circle display maximum, each labeled with its destination floor
  digit and colored by direction of travel

#### Scenario: Passengers board without an in-progress transit
- **WHEN** an elevator's passenger count increases but no pickup transit
  animation is currently in progress for that elevator
- **THEN** the elevator's rendered circle count reflects the full new
  passenger count immediately, labeled and colored as above

#### Scenario: Riding passengers traveling in different directions
- **WHEN** an elevator's current riders have destinations both above and
  below the floor where they boarded
- **THEN** the circles for riders with a destination above the boarding
  floor and the circles for riders with a destination below it are
  rendered in two visually distinct direction colors

### Requirement: Floor Call Indication
The GUI SHALL visually mark a floor's pending passenger pickup requests
as one circle per waiting passenger, up to a maximum of 12 simultaneously
displayed circles, rather than a single binary indicator. Each circle
SHALL be labeled with a single digit denoting that passenger's
destination floor and colored according to whether that destination is
above or below the waiting floor.

#### Scenario: Floor has an outstanding request
- **WHEN** a floor has one or more pending passenger pickup requests
- **THEN** that floor renders one circle per pending request, up to the
  12-circle display maximum, each labeled with its destination floor
  digit and colored by direction of travel from that floor

#### Scenario: Floor's pending requests are fully served
- **WHEN** a floor's pending passenger pickup request count reaches zero
- **THEN** no waiting-passenger circles are rendered for that floor

#### Scenario: Waiting passengers traveling in different directions
- **WHEN** a floor has waiting passengers with destinations both above
  and below that floor
- **THEN** the circles for passengers heading up and the circles for
  passengers heading down are rendered in two visually distinct
  direction colors

### Requirement: Passenger Transfer Animation
The GUI SHALL animate passenger circles moving between a floor and an
elevator car when a pickup or dropoff occurs, over a fixed real-time
duration, rather than instantaneously appearing, disappearing, or
recounting at either location. Each traveling circle SHALL retain its
destination-floor digit label and its direction color throughout the
animation.

#### Scenario: Passengers are picked up
- **WHEN** an elevator picks up passengers at a floor
- **THEN** circles representing those passengers, each labeled with its
  destination floor digit and colored by direction of travel from the
  pickup floor, visibly travel from the floor's waiting position to the
  elevator car over a fixed animation duration, and the elevator's
  resting passenger-circle count does not include those passengers until
  the travel animation completes

#### Scenario: Passengers are dropped off
- **WHEN** an elevator drops off passengers at a floor
- **THEN** circles representing those passengers, each labeled with its
  destination floor digit and colored by the direction of travel from
  the floor where that ride began, visibly travel from the elevator car
  to the floor over a fixed animation duration before disappearing,
  without affecting the floor's waiting-passenger circle count

#### Scenario: No double-counting during a pickup transit
- **WHEN** a pickup transit animation is in progress for an elevator
- **THEN** the total number of passenger circles rendered at the floor's
  waiting position plus in transit plus resting in the elevator does not
  exceed the elevator's actual current passenger count at any rendered
  frame, and suppression of resting circles is applied per destination
  so that a destination group with no in-progress transit is never
  reduced by a different destination's in-flight pickup

package com.elevatorsim;

// Immutable record of a single pickup or dropoff event at a floor, for GUI polling.
// countsByDestination is indexed by destination floor (size 10). originFloor is the floor
// this batch boarded at: for a pickup it equals floor itself; for a dropoff, floor is the
// arrival floor, so originFloor is carried separately.
public final class PassengerTransfer {
    private final int floor;
    private final int originFloor;
    private final int[] countsByDestination;
    private final int tick;

    public PassengerTransfer(int floor, int originFloor, int[] countsByDestination, int tick) {
        this.floor = floor;
        this.originFloor = originFloor;
        this.countsByDestination = countsByDestination.clone();
        this.tick = tick;
    }

    public int getFloor() {
        return floor;
    }

    public int getOriginFloor() {
        return originFloor;
    }

    public int[] getCountsByDestination() {
        return countsByDestination.clone();
    }

    public int getCount() {
        int sum = 0;
        for (int c : countsByDestination) {
            sum += c;
        }
        return sum;
    }

    public int getTick() {
        return tick;
    }
}

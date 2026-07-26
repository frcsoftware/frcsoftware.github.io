/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// [robotTrackerFull]
class RobotTracker {
    // [mutableField]
    private Point position;
    // [/mutableField]

    // [robotTrackerConstructor]
    public RobotTracker(Point startPosition) {
        this.position = startPosition;
    }
    // [/robotTrackerConstructor]

    // [constructorChaining]
    public RobotTracker() {
        this(Point.ORIGIN);
    }
    // [/constructorChaining]

    // [stateMutation]
    public void move(Point delta) {
        this.position = this.position.plus(delta);
    }
    // [/stateMutation]

    // [localVariableChain]
    public double distanceTo(Point target) {
        Point diff = target.minus(this.position);
        return diff.norm();
    }
    // [/localVariableChain]

    // [getPosition]
    public Point getPosition() {
        return this.position;
    }
    // [/getPosition]

    // [useStaticConstant]
    public void reset() {
        this.position = Point.ORIGIN;
    }
    // [/useStaticConstant]
}
// [/robotTrackerFull]

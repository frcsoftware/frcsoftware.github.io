/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// [pointFull]
class Point {
    // [finalFields]
    private final double x;
    private final double y;
    // [/finalFields]
    // [staticConstant]
    public static final Point ORIGIN = new Point(0, 0);
    // [/staticConstant]

    // [constructor]
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    // [/constructor]

    // [getters]
    public double getX() { return this.x; }
    public double getY() { return this.y; }
    // [/getters]

    // [plus]
    public Point plus(Point other) {
        return new Point(this.x + other.x, this.y + other.y);
    }
    // [/plus]

    // [minus]
    public Point minus(Point other) {
        return new Point(this.x - other.x, this.y - other.y);
    }
    // [/minus]

    // [norm]
    public double norm() {
        double sumOfSquares = this.x * this.x + this.y * this.y;
        return Math.sqrt(sumOfSquares);
    }
    // [/norm]
}
// [/pointFull]

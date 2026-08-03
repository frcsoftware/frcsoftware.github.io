/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.simulation;

import java.util.ArrayList;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Pose3d;
import org.wpilib.math.geometry.Rotation3d;
import org.wpilib.math.geometry.Transform3d;
import org.wpilib.networktables.NetworkTable;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.networktables.StructArrayPublisher;
import org.wpilib.system.Timer;

public class FuelSim {
  public enum Mode {
    INTAKE,
    SHOOT,
    OUTTAKE,
    NONE
  }

  private static final double SHOT_DT = 0.1, SHOT_VELOCITY_X = -2, SHOT_VELOCITY_Z = 5.5;
  private static final Transform3d
      FIRST_ROW_TRANSFORM = new Transform3d(-0.22, 0, 0.35, Rotation3d.kZero),
      SECOND_ROW_TRANSFORM = new Transform3d(-0.08, 0, 0.3, Rotation3d.kZero),
      THIRD_ROW_TRANSFORM = new Transform3d(0.06, 0, 0.25, Rotation3d.kZero);

  private static final NetworkTable table = NetworkTableInstance.getDefault().getTable("Test");
  private static final StructArrayPublisher<Pose3d>
      row1FuelPub = table.getStructArrayTopic("Row1Fuel", Pose3d.struct).publish(),
      row2FuelPub = table.getStructArrayTopic("Row2Fuel", Pose3d.struct).publish(),
      row3FuelPub = table.getStructArrayTopic("Row3Fuel", Pose3d.struct).publish(),
      parabolaFuelPub = table.getStructArrayTopic("ParabolaFuel", Pose3d.struct).publish();
  private static final Timer modeChangeTimer = new Timer();
  private static Mode mode = Mode.NONE;
  private static int rowsOfFuel = 3;

  private static Pose3d[] basicParabola(Pose3d start) {
    var poses = new ArrayList<Pose3d>();
    poses.add(start);
    double t = 0;
    while (poses.getLast().getZ() > 0) {
      double x = start.getX() + SHOT_VELOCITY_X * t;
      double y = start.getY();
      double z = start.getZ() + SHOT_VELOCITY_Z * t - 0.5 * 9.8 * t * t;
      poses.add(new Pose3d(x, y, z, Rotation3d.kZero));
      t += SHOT_DT;
    }
    return poses.toArray(Pose3d[]::new);
  }

  private static Pose3d[] fuelRow(Pose3d base) {
    var offset = new Transform3d(0, 0.15, 0, Rotation3d.kZero);
    var poses = new Pose3d[3];
    poses[0] = base.plus(offset.inverse());
    poses[1] = base;
    poses[2] = base.plus(offset);
    return poses;
  }

  public static void setMode(Mode mode) {
    if (mode == FuelSim.mode) return;
    if (mode == Mode.SHOOT && rowsOfFuel == 0) return;
    FuelSim.mode = mode;
    modeChangeTimer.restart();
  }

  public static void update(Pose2d robotPose) {
    var root = new Pose3d(robotPose);
    var timeSinceModeSet = modeChangeTimer.get();

    if (mode == Mode.SHOOT && timeSinceModeSet < 3.5) {
      parabolaFuelPub.set(basicParabola(root.plus(new Transform3d(0.2, 0, 0.5, Rotation3d.kZero))));
    } else {
      parabolaFuelPub.set(new Pose3d[0]);
    }

    if (mode == Mode.INTAKE) {
      rowsOfFuel = Math.min(3, (int) (timeSinceModeSet + 0.2));
    } else if (mode != Mode.NONE) {
      rowsOfFuel = Math.min(3, (int) (3.2 - timeSinceModeSet));
    }

    row1FuelPub.set(rowsOfFuel >= 1 ? fuelRow(root.plus(FIRST_ROW_TRANSFORM)) : new Pose3d[0]);
    row2FuelPub.set(rowsOfFuel >= 2 ? fuelRow(root.plus(SECOND_ROW_TRANSFORM)) : new Pose3d[0]);
    row3FuelPub.set(rowsOfFuel >= 3 ? fuelRow(root.plus(THIRD_ROW_TRANSFORM)) : new Pose3d[0]);
  }
}

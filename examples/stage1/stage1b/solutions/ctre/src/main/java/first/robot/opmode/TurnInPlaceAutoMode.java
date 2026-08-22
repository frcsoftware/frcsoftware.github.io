/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.PeriodicOpMode;

@Autonomous
public class TurnInPlaceAutoMode extends PeriodicOpMode {
  private final Robot robot;

  public TurnInPlaceAutoMode(Robot robot) {
    this.robot = robot;
  }

  @Override
  public void start() {
    Scheduler.getDefault().schedule(robot.drivetrain.rotateInPlace(90.0, () -> 0.2));
  }
}

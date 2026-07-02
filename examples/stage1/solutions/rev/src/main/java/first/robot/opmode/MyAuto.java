/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.system.Timer;

@Autonomous(name = "My Auto", group = "Group 1")
public class MyAuto extends PeriodicOpMode {
  private final Robot robot;
  private Timer timer = new Timer();

  public MyAuto(Robot robot) {
    this.robot = robot;
  }

  @Override
  public void start() {
    timer.restart();
  }

  @Override
  public void periodic() {
    if (timer.hasElapsed(4)) {
      robot.drivetrain.arcadeDrive(0.0, 0.0); // Stop the robot after 4 seconds
    } else {
      robot.drivetrain.arcadeDrive(0.5, 0.0); // Drive forward at half speed with no rotation
    }
  }
}

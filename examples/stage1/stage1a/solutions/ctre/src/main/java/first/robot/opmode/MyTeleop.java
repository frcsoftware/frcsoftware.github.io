/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

import first.robot.Robot;
import org.wpilib.driverstation.NiDsXboxController;
import org.wpilib.opmode.PeriodicOpMode;
package first.robot.opmode;
import org.wpilib.opmode.Teleop;

// [FullDrivetrain]
@Teleop
public class MyTeleop extends PeriodicOpMode {
  private final Robot robot;
  // [Controller]
  private final NiDsXboxController xboxController = new NiDsXboxController(0);

  // [/Controller]

  /** The Robot instance is passed into the opmode via the constructor. */
  public MyTeleop(Robot robot) {
    this.robot = robot;
  }

  // [DriveSimPeriodic]
  @Override
  public void periodic() {
    /* Called periodically (set time interval) while the robot is enabled. */
    robot.drivetrain.arcadeDrive(-xboxController.getLeftY(), xboxController.getRightX());
    // [/DriveSimPeriodic]
    // [/FullDrivetrain]

    if (xboxController.getRightBumperButton()) {
      // shoot
      robot.intakeLauncher.setThrottle(0.9);
      robot.feeder.setThrottle(0.75);

    } else if (xboxController.getLeftBumperButton()) {
      // intake
      robot.intakeLauncher.setThrottle(0.8);
      robot.feeder.setThrottle(-1.0);

    } else if (xboxController.getAButton()) {
      // outake
      robot.intakeLauncher.setThrottle(-0.8);
      robot.feeder.setThrottle(1.0);

    } else {
      // stop
      robot.intakeLauncher.setThrottle(0.0);
      robot.feeder.setThrottle(0.0);
    }
  }
}

/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.driverstation.NiDsXboxController;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop
public class MyTeleop extends PeriodicOpMode {
  private final Robot robot;
  private final NiDsXboxController xboxController = new NiDsXboxController(0);

  public MyTeleop(Robot robot) {
    this.robot = robot;
  }

  @Override
  public void periodic() {
    robot.drivetrain.arcadeDrive(-xboxController.getLeftY(), xboxController.getRightX());

    if (xboxController.getRightBumperButton()) {
      // shoot
      robot.intakeLauncher.setThrottle(0.9);
      robot.feeder.setThrottle(0.75);
    } else if (xboxController.getLeftBumperButton()) {
      // intake
      robot.intakeLauncher.setThrottle(0.8);
      robot.feeder.setThrottle(-1.0);
    } else if (xboxController.getAButton()) {
      // outtake
      robot.intakeLauncher.setThrottle(-0.8);
      robot.feeder.setThrottle(1.0);
    } else {
      // stop
      robot.intakeLauncher.setThrottle(0.0);
      robot.feeder.setThrottle(0.0);
    }
  }
}

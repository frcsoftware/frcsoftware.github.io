// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.opmode;

import first.robot.Robot;
import org.wpilib.driverstation.NiDsXboxController;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop
public class MyTeleop extends PeriodicOpMode {
  private final Robot robot;
  private final NiDsXboxController xboxController = new NiDsXboxController(0);

  /** The Robot instance is passed into the opmode via the constructor. */
  public MyTeleop(Robot robot) {
    this.robot = robot;
  }

  @Override
  public void periodic() {
    /* Called periodically (set time interval) while the robot is enabled. */
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

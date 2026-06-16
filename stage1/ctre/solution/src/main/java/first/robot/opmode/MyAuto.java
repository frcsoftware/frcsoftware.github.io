// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot.opmode;

import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.system.Timer;

import first.robot.Robot;

@Autonomous(name = "My Auto", group = "Group 1")
public class MyAuto extends PeriodicOpMode {
  private final Robot robot;
  private double startTime;


  /** The Robot instance is passed into the opmode via the constructor. */
  public MyAuto(Robot robot) {
    this.robot = robot;
  }

  @Override
  public void start() {
    startTime = Timer.getTimestamp(); // Set the start time when auto starts
  }

  /*
   * This method runs periodically, using the same period as the Robot instance.
   *
   * Additional periodic methods may be configured with addPeriodic(),
   * which can have periods that differ from the main Robot instance.
   */
  @Override
  public void periodic() {
    if (Timer.getTimestamp() - startTime < 4.0) { // Drive for 4 seconds after the start of auto
      robot.drivetrain.arcadeDrive(0.5, 0.0); // Drive forward at half speed with no rotation
    } else {
      robot.drivetrain.arcadeDrive(0.0, 0.0); //Stop the drivetrain after 4 seconds
    }
  }
}

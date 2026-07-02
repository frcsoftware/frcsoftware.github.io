// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import first.robot.simulation.DrivetrainSim;
import first.robot.simulation.SingleFlywheelSim;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.framework.OpModeRobot;
import org.wpilib.hardware.imu.OnboardIMU;
import org.wpilib.hardware.imu.OnboardIMU.MountOrientation;

public class Robot extends OpModeRobot {

  private SparkMax leftLeader = new SparkMax(0, 0, MotorType.kBrushless);
  private SparkMax leftFollower = new SparkMax(0, 1, MotorType.kBrushless);
  private SparkMax rightLeader = new SparkMax(0, 2, MotorType.kBrushless);
  private SparkMax rightFollower = new SparkMax(0, 3, MotorType.kBrushless);

  private OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);

  private DrivetrainSim drivetrainSim = new DrivetrainSim(leftLeader, rightLeader);
  public SparkMax intakeLauncher = new SparkMax(0, 4, MotorType.kBrushless);
  public SparkMax feeder = new SparkMax(0, 5, MotorType.kBrushless);

  private SingleFlywheelSim intakeLauncherSim =
      new SingleFlywheelSim(intakeLauncher, "IntakeLauncher");
  private SingleFlywheelSim feederSim = new SingleFlywheelSim(feeder, "Feeder");

  public final DifferentialDrive drivetrain =
      new DifferentialDrive(leftLeader::setThrottle, rightLeader::setThrottle);

  public Robot() {

    var leftConfig = new SparkMaxConfig().inverted(true);
    leftLeader.configure(
        leftConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    leftFollower.configure(
        leftConfig.follow(leftLeader),
        ResetMode.kResetSafeParameters,
        PersistMode.kPersistParameters);

    var rightConfig = new SparkMaxConfig().inverted(false);
    rightLeader.configure(
        rightConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    rightFollower.configure(
        rightConfig.follow(rightLeader),
        ResetMode.kResetSafeParameters,
        PersistMode.kPersistParameters);
  }

  @Override
  public void simulationPeriodic() {
    drivetrainSim.periodic();
    intakeLauncherSim.periodic();
    feederSim.periodic();
  }
}

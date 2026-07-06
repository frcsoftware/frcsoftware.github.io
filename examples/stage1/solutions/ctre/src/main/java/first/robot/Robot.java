// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import first.robot.simulation.DrivetrainSim;
import first.robot.simulation.SingleFlywheelSim;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.framework.OpModeRobot;
import org.wpilib.hardware.imu.OnboardIMU;
import org.wpilib.hardware.imu.OnboardIMU.MountOrientation;

/**
 * The methods in this class are called automatically as described in the OpModeRobot documentation.
 * OpMode classes anywhere in the package (or sub-packages) where this class is located are
 * automatically registered to display in the Driver Station. If you change the name of this class
 * or the package after creating this project, you must also update the Main.java file in the
 * project.
 */
public class Robot extends OpModeRobot {

  private final int leftLeaderID = 0;
  public TalonFX leftLeader = new TalonFX(leftLeaderID, CANBus.systemcore(0));
  private TalonFX leftFollower = new TalonFX(1, CANBus.systemcore(0));

  private final int rightLeaderID = 2;
  public TalonFX rightLeader = new TalonFX(rightLeaderID, CANBus.systemcore(0));
  private TalonFX rightFollower = new TalonFX(3, CANBus.systemcore(0));

  public TalonFX intakeLauncher = new TalonFX(4, CANBus.systemcore(0));
  public TalonFX feeder = new TalonFX(5, CANBus.systemcore(0));

  private OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);

  public final DifferentialDrive drivetrain =
      new DifferentialDrive(leftLeader::setThrottle, rightLeader::setThrottle);

  private DrivetrainSim drivetrainSim = new DrivetrainSim(leftLeader, rightLeader);
  private SingleFlywheelSim intakeLauncherSim =
      new SingleFlywheelSim(intakeLauncher, "intakeLauncher");
  private SingleFlywheelSim feederSim = new SingleFlywheelSim(feeder, "feeder");

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {
    var leftConfig = new TalonFXConfiguration();
    leftConfig.MotorOutput.withInverted(InvertedValue.Clockwise_Positive);
    leftLeader.getConfigurator().apply(leftConfig);

    var rightConfig = new TalonFXConfiguration();
    rightConfig.MotorOutput.withInverted(InvertedValue.CounterClockwise_Positive);
    rightLeader.getConfigurator().apply(rightConfig);

    leftFollower.setControl(new Follower(leftLeaderID, MotorAlignmentValue.Aligned));
    rightFollower.setControl(new Follower(rightLeaderID, MotorAlignmentValue.Aligned));
  }

  @Override
  public void simulationPeriodic() {
    drivetrainSim.periodic();
    intakeLauncherSim.periodic();
    feederSim.periodic();
  }
}

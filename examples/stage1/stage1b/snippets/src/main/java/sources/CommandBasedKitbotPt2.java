/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package sources;

import static org.wpilib.units.Units.Seconds;

import java.util.function.DoubleSupplier;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Scheduler;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.hardware.imu.OnboardIMU;
import org.wpilib.hardware.imu.OnboardIMU.MountOrientation;
import org.wpilib.opmode.PeriodicOpMode;

class CommandBasedKitbotPt2 {
  // [drivetrainDef]
  public class Drivetrain implements Mechanism {
    private static final int leftLeaderID = 0, rightLeaderID = 2;
    private final TalonFX leftLeader = new TalonFX(leftLeaderID, CANBus.systemcore(0)),
        leftFollower = new TalonFX(1, CANBus.systemcore(0)),
        rightLeader = new TalonFX(rightLeaderID, CANBus.systemcore(0)),
        rightFollower = new TalonFX(3, CANBus.systemcore(0));

    private final OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);
    private final DifferentialDrive differentialDrive =
        new DifferentialDrive(leftLeader::setThrottle, rightLeader::setThrottle);

    private final DrivetrainSim drivetrainSim = new DrivetrainSim(leftLeader, rightLeader);

    public void periodic() {
      drivetrainSim.periodic();
    }
  }
  // [/drivetrainDef]

  // [opModeSkeleton]
  class MyOpModeName extends PeriodicOpMode {
    private final Robot robot;

    public MyOpModeName(Robot robot) {
      this.robot = robot;
    }

    @Override
    public void start() {}
  }
  // [/opModeSkeleton]

  class AutoModeExampleCode extends PeriodicOpMode {
    private final Command myAutoCommand = null;

    // [startMethod]
    @Override
    public void start() {
      Scheduler.getDefault().schedule(myAutoCommand);
    }
    // [/startMethod]

    public AutoModeExampleCode(Robot robot) {
      DoubleSupplier forwardThrottle = null, rotationThrottle = null;

      var driveCmd =
          // [driveCommand]
          robot.drivetrain.arcadeDrive(forwardThrottle, rotationThrottle);
      // [/driveCommand]

      var driveCmdWithTimeout =
          // [4SecDriveCommand]
          robot.drivetrain
              .arcadeDrive(forwardThrottle, rotationThrottle)
              .withTimeout(Seconds.of(4));
      // [/4SecDriveCommand]
    }
  }

  class Robot {
    DriveCommandExamples drivetrain = new DriveCommandExamples();
  }

  class DriveCommandExamples implements Mechanism {
    private final OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);
    private final DifferentialDrive differentialDrive = new DifferentialDrive(throttle -> {}, throttle -> {});

    Command arcadeDrive(DoubleSupplier forwardThrottle, DoubleSupplier rotationThrottle) {
      return run(coroutine -> {
            // [arcadeDriveBody]
            while (true) {
              differentialDrive.arcadeDrive(
                  forwardThrottle.getAsDouble(), rotationThrottle.getAsDouble());
              coroutine.yield();
            }
            // [/arcadeDriveBody]
          })
          .named("Drive");
    }

    Command rotateInPlaceHint(double angleDegrees) {
      return run(coroutine -> {
            // [rotateInPlaceBody]
            double targetAngle = imu.getRotation2d().getDegrees() + angleDegrees;
            while (true) {
              // What to add here?
              coroutine.yield();
            }
            // [/rotateInPlaceBody]
          })
          .named("RotateInPlace");
    }
  }

  static class TalonFX {
    TalonFX(int id, CANBus bus) {}

    void setThrottle(double throttle) {}
  }

  static class CANBus {
    static CANBus systemcore(int busNumber) {
      return new CANBus();
    }
  }

  static class DrivetrainSim {
    DrivetrainSim(TalonFX leftLeader, TalonFX rightLeader) {}

    void periodic() {}
  }
}

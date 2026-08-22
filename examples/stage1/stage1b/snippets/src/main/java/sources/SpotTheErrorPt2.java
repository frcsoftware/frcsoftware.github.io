/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package sources;

import java.util.function.DoubleSupplier;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.command3.Scheduler;
import org.wpilib.command3.button.CommandXboxController;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.framework.OpModeRobot;
import org.wpilib.hardware.imu.OnboardIMU;

public class SpotTheErrorPt2 {
  private final DifferentialDrive differentialDrive = null;
  private final OnboardIMU imu = null;

  class ArcadeDriveBug implements Mechanism {
    // [arcadeDriveBug]
    Command arcadeDrive(DoubleSupplier forwardThrottle, DoubleSupplier rotationThrottle) {
      double forward = forwardThrottle.getAsDouble();
      double rotation = rotationThrottle.getAsDouble();
      return run(coroutine -> {
            while (true) {
              differentialDrive.arcadeDrive(forward, rotation);
              coroutine.yield();
            }
          })
          .named("Drive");
    }
    // [/arcadeDriveBug]
  }

  class ArcadeDriveFix implements Mechanism {
    // [arcadeDriveFix]
    Command arcadeDrive(DoubleSupplier forwardThrottle, DoubleSupplier rotationThrottle) {
      return run(coroutine -> {
            while (true) {
              differentialDrive.arcadeDrive(
                  forwardThrottle.getAsDouble(), rotationThrottle.getAsDouble());
              coroutine.yield();
            }
          })
          .named("Drive");
    }
    // [/arcadeDriveFix]
  }

  // [robotPeriodicBug]
  public class Robot extends OpModeRobot {
    private final CommandXboxController xbox = new CommandXboxController(0);

    public Robot() {
      xbox.a().whileTrue(printHelloWorld());
    }

    @Override
    public void robotPeriodic() {}

    private Command printHelloWorld() {
      return Command.noRequirements(coroutine -> {
            while (true) {
              System.out.println("Hello World!");
              coroutine.yield();
            }
          })
          .named("Hello World!");
    }
  }
  // [/robotPeriodicBug]

  class RobotFixed extends OpModeRobot {
    private final Drivetrain drivetrain = new Drivetrain();

    // [robotPeriodicFix]
    @Override
    public void robotPeriodic() {
      Scheduler.getDefault().run();
    }
    // [/robotPeriodicFix]
  }

  class RotateInPlaceBug implements Mechanism {
    // [rotateInPlaceBug]
    Command rotateInPlace(double angleDegrees) {
      double targetAngle = imu.getRotation2d().getDegrees() + angleDegrees;
      return run(coroutine -> {
            while (imu.getRotation2d().getDegrees() < targetAngle) {
              differentialDrive.arcadeDrive(0.0, 0.2);
              coroutine.yield();
            }
          })
          .named("RotateInPlace");
    }
    // [/rotateInPlaceBug]
  }

  class RotateInPlaceFix implements Mechanism {
    // [rotateInPlaceFix]
    Command rotateInPlace(double angleDegrees) {
      return run(coroutine -> {
            double targetAngle = imu.getRotation2d().getDegrees() + angleDegrees;
            while (imu.getRotation2d().getDegrees() < targetAngle) {
              differentialDrive.arcadeDrive(0.0, 0.2);
              coroutine.yield();
            }
          })
          .named("RotateInPlace");
    }
    // [/rotateInPlaceFix]
  }

  class Drivetrain implements Mechanism {
    void periodic() {}

    Command arcadeDrive(DoubleSupplier forwardThrottle, DoubleSupplier rotationThrottle) {
      return null;
    }
  }
}

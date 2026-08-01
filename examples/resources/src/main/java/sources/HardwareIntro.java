/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
import static org.wpilib.units.Units.Rotations;

import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.hardware.discrete.DigitalInput;
import org.wpilib.hardware.imu.OnboardIMU;
import org.wpilib.hardware.imu.OnboardIMU.MountOrientation;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.units.Units;
import org.wpilib.units.measure.Angle;

public class HardwareIntro {
  // [intake]
  public class Intake implements Mechanism {
    // Placeholder for TalonFX, SparkMax or SparkFlex
    private final ExampleMotor motor = new ExampleMotor(0, CANBus.systemcore(0));

    public Command runIntake() {
      return run(coroutine -> {
            while (true) {
              motor.set(0.8);
              coroutine.yield();
            }
          })
          .named("Run Intake");
    }

    public Command stopIntake() {
      return run(coroutine -> {
            while (true) {
              motor.set(0.0);
              coroutine.yield();
            }
          })
          .named("Stop Intake");
    }
  }
  // [/intake]

  // [turret]
  public class Turret implements Mechanism {
    // Placeholder for magnetic encoder
    private final ExampleEncoder encoder = new ExampleEncoder(1, CANBus.systemcore(0));

    public Rotation2d getEncoderPosition() {
      double position = encoder.getAbsolutePosition().getValue().in(Rotations) * ENCODER_MECHANISM_RATIO;
      return Rotation2d.fromRotations(position);
    }
  }
  // [/turret]

  // [climber]
  public class Climber implements Mechanism {
    private final DigitalInput limitSwitch = new DigitalInput(2);

    public boolean isLimitSwitchPressed() {
      return !limitSwitch.get();
    }
  }
  // [/climber]

  // [indexer]
  public class Indexer implements Mechanism {
    private final DigitalInput beamBrake = new DigitalInput(3);

    public boolean isBeamBrakeTripped() {
      return !beamBrake.get();
    }
  }
  // [/indexer]

  // [drivetrain]
  public class Drivetrain implements Mechanism {
    private final OnboardIMU imu = new OnboardIMU(MountOrientation.FLAT);

    public Rotation2d getHeading() {
      return imu.getRotation2d();
    }
  }
  // [/drivetrain]

  private static final double ENCODER_MECHANISM_RATIO = 1.0;

  static class CANBus {
    static CANBus systemcore(int busNumber) {
      return new CANBus();
    }
  }

  static class ExampleMotor {
    ExampleMotor(int deviceId, CANBus bus) {}

    void set(double percent) {}
  }

  static class ExampleEncoder {
    ExampleEncoder(int deviceId, CANBus bus) {}

    PositionSignal getAbsolutePosition() {
      return new PositionSignal();
    }

    static class PositionSignal {
      Angle getValue() {
        return Angle.ofRelativeUnits(0.0, Units.Rotations);
      }
    }
  }
}

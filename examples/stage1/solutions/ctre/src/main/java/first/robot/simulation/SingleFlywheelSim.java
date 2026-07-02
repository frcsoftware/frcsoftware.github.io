/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.simulation;

import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.RadiansPerSecond;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.sim.ChassisReference;
import com.ctre.phoenix6.sim.TalonFXSimState;
import com.ctre.phoenix6.sim.TalonFXSimState.MotorType;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
import org.wpilib.networktables.DoublePublisher;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.simulation.FlywheelSim;

public class SingleFlywheelSim {

  private final TalonFX talonMotor;
  private final TalonFXSimState talonMotorSim;
  private double motorPosition = 0.0;

  private final double gearRatio = 1.0;
  private final FlywheelSim flywheelSim =
      new FlywheelSim(
          Models.flywheelFromPhysicalConstants(DCMotor.getKrakenX60(1), 0.001, gearRatio),
          DCMotor.getKrakenX60(1));

  private final double kBusVoltage = 12.0;

  private final DoublePublisher motorVoltagePub;
  private final DoublePublisher motorVelocityPub;
  private final DoublePublisher motorCurrentPub;
  private final DoublePublisher motorPositionPub;

  public SingleFlywheelSim(TalonFX talonMotor, String name) {
    this.talonMotor = talonMotor;
    this.talonMotorSim =
        new TalonFXSimState(talonMotor, ChassisReference.CounterClockwise_Positive);
    this.talonMotorSim.setMotorType(MotorType.KrakenX60);

    var table = NetworkTableInstance.getDefault().getTable(name);
    motorVoltagePub = table.getDoubleTopic("MotorVoltage").publish();
    motorVelocityPub = table.getDoubleTopic("MotorVelocity").publish();
    motorCurrentPub = table.getDoubleTopic("MotorStatorCurrent").publish();
    motorPositionPub = table.getDoubleTopic("MotorPosition").publish();
  }

  public void periodic() {
    double motorVoltage = talonMotor.getThrottle() * kBusVoltage;

    flywheelSim.setInputVoltage(motorVoltage);
    flywheelSim.update(0.02);

    double motorVelo = flywheelSim.getAngularVelocity() * gearRatio;
    motorPosition += motorVelo * 0.02 * gearRatio;

    talonMotorSim.setSupplyVoltage(12.0);
    talonMotorSim.setRawRotorPosition(Radians.of(motorPosition));
    talonMotorSim.setRotorVelocity(RadiansPerSecond.of(motorVelo));

    motorVoltagePub.set(motorVoltage);
    motorVelocityPub.set(talonMotor.getVelocity().getValueAsDouble());
    motorCurrentPub.set(talonMotor.getStatorCurrent().getValueAsDouble());
    motorPositionPub.set(talonMotor.getPosition().getValueAsDouble());
  }
}

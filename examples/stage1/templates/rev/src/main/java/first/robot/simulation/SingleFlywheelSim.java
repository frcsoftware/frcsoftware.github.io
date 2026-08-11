/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.simulation;

import com.revrobotics.spark.SparkMax;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
import org.wpilib.networktables.DoublePublisher;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.simulation.FlywheelSim;

public class SingleFlywheelSim {

  private final SparkMax motor;

  private final FlywheelSim m_flywheelSim;

  private final DoublePublisher motorVoltagePub;
  private final DoublePublisher motorVelocityPub;
  private final DoublePublisher currentPub;
  private final DoublePublisher motorPositionPub;
  private double rotorPositionRad;

  private static final double kBusVoltage = 12.0;

  private final String name;

  public SingleFlywheelSim(SparkMax motor, String name) {
    this.name = name;
    this.motor = motor;
    var gearbox = DCMotor.getNEO(1);
    this.m_flywheelSim =
        new FlywheelSim(Models.flywheelFromPhysicalConstants(gearbox, 0.001, 1.0), gearbox);

    var table = NetworkTableInstance.getDefault().getTable(this.name);
    this.motorVoltagePub = table.getDoubleTopic("MotorVoltage").publish();
    this.motorVelocityPub = table.getDoubleTopic("MotorVelocity").publish();
    this.currentPub = table.getDoubleTopic("Current").publish();
    this.motorPositionPub = table.getDoubleTopic("MotorPosition").publish();

    // Voltage and current properties aren't included since they default to volts and amps already
    this.motorVelocityPub.getTopic().setProperty("unit", "\"RadiansPerSecond\"");
    this.motorPositionPub.getTopic().setProperty("unit", "\"Radians\"");
  }

  public void periodic() {
    double motorVoltage = motor.getThrottle() * kBusVoltage;

    m_flywheelSim.setInputVoltage(motorVoltage);
    m_flywheelSim.update(0.02);

    double radPerSec = m_flywheelSim.getAngularVelocity();
    rotorPositionRad += radPerSec * 0.02;

    motorVoltagePub.set(motorVoltage);
    motorVelocityPub.set(radPerSec);
    currentPub.set(m_flywheelSim.getCurrentDraw());
    motorPositionPub.set(rotorPositionRad);
  }
}

// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

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
  private final DoublePublisher rotorVelocityPub;
  private final DoublePublisher currentPub;
  private final DoublePublisher rotorPositionPub;
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
    this.rotorVelocityPub = table.getDoubleTopic("RotorVelocity").publish();
    this.currentPub = table.getDoubleTopic("Current").publish();
    this.rotorPositionPub = table.getDoubleTopic("RotorPosition").publish();
  }

  public void periodic() {
    double motorVoltage = motor.getThrottle() * kBusVoltage;

    m_flywheelSim.setInputVoltage(motorVoltage);
    m_flywheelSim.update(0.02);

    double radPerSec = m_flywheelSim.getAngularVelocity();
    rotorPositionRad += radPerSec * 0.02;

    motorVoltagePub.set(motorVoltage);
    rotorVelocityPub.set(radPerSec);
    currentPub.set(m_flywheelSim.getCurrentDraw());
    rotorPositionPub.set(rotorPositionRad);
  }
}

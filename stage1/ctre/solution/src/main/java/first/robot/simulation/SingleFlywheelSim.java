package first.robot.simulation;

import com.ctre.phoenix6.hardware.TalonFX;
import org.wpilib.math.system.DCMotor;
import org.wpilib.math.system.Models;
import org.wpilib.networktables.DoublePublisher;
import org.wpilib.networktables.NetworkTableInstance;
import org.wpilib.simulation.FlywheelSim;

public class SingleFlywheelSim {

  private final TalonFX talonMotor;
  private double motorPosition = 0.0;

  private final double gearRatio = 1.0;
  private final FlywheelSim flywheelSim = new FlywheelSim(
      Models.flywheelFromPhysicalConstants(DCMotor.getKrakenX60(1), 0.001, gearRatio), DCMotor.getKrakenX60(1));

  private final double kBusVoltage = 12.0;

  private final DoublePublisher motorVoltagePub;
  private final DoublePublisher motorVelocityPub;
  private final DoublePublisher motorCurrentPub;
  private final DoublePublisher motorPositionPub;

  public SingleFlywheelSim(TalonFX talonMotor, String name) {
    this.talonMotor = talonMotor;

    var table = NetworkTableInstance.getDefault().getTable(name);
    motorVoltagePub = table.getDoubleTopic("MotorVoltage").publish();
    motorVelocityPub = table.getDoubleTopic("MotorVelocity").publish();
    motorCurrentPub = table.getDoubleTopic("MotorCurrent").publish();
    motorPositionPub = table.getDoubleTopic("MotorPosition").publish();
  }

  public void periodic() {
    double motorVoltage = talonMotor.getThrottle() * kBusVoltage;

    flywheelSim.setInputVoltage(motorVoltage);
    flywheelSim.update(0.02);

    double motorVelo = flywheelSim.getAngularVelocity();
    motorPosition += motorVelo * 0.02;

    motorVoltagePub.set(motorVoltage);
    motorVelocityPub.set(motorVelo);
    motorCurrentPub.set(flywheelSim.getCurrentDraw());
    motorPositionPub.set(motorPosition);
  }
}

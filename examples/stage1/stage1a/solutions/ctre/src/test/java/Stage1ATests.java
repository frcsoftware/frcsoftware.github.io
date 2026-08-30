/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
import static org.junit.jupiter.api.Assertions.*;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import first.robot.Robot;
import first.robot.opmode.MyAuto;
import first.robot.opmode.MyTeleop;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.wpilib.drive.DifferentialDrive;
import org.wpilib.driverstation.NiDsXboxController;
import org.wpilib.hardware.hal.HAL;
import org.wpilib.simulation.NiDsXboxControllerSim;
import org.wpilib.simulation.SimHooks;

// Run this with: ./gradlew :stage1-templates-ctre:test --tests Stage1ATests
// Students run with: ./gradlew test --tests Stage1ATests
public class Stage1ATests {

  private static final double THROTTLE_TOLERANCE = 0.05;

  @BeforeAll
  static void initializeSimulation() {
    HAL.initialize(500, 0);
  }

  /** Reads a (possibly private) field from the Robot, failing the test if it is missing. */
  private static Field findField(String name) {
    try {
      Field field = Robot.class.getDeclaredField(name);
      field.setAccessible(true);
      return field;
    } catch (NoSuchFieldException e) {
      fail("Robot is missing field \"" + name + "\"");
      return null; // unreachable
    }
  }

  /** Reads the value of a Robot field, failing the test if it is not accessible. */
  private static Object readField(Robot robot, String name) {
    try {
      return findField(name).get(robot);
    } catch (IllegalAccessException e) {
      fail("Robot does not have field \"" + name + "\".");
      return null; // unreachable
    }
  }

  /** Fetches a TalonFX motor field from the robot. */
  private static TalonFX motor(Robot robot, String name) {
    return (TalonFX) readField(robot, name);
  }

  /** Returns the duty cycle currently being commanded to the motor. */
  private static double appliedOutput(TalonFX talon, String name) {
    var control = talon.getAppliedControl();
    assertInstanceOf(DutyCycleOut.class, control, name + " should be driven with a duty cycle");
    return ((DutyCycleOut) control).Output;
  }

  /** Verifies a motor field exists, is constructed, and is on the expected CAN ID. */
  private static void checkMotorField(Robot robot, String name, int expectedId) {
    var obj = readField(robot, name);
    assertNotNull(obj, name + " should be constructed");
    assertTrue(
        obj instanceof TalonFX talon && talon.getDeviceID() == expectedId,
        name + " should be a TalonFX with ID " + expectedId);
  }

  /** Verifies a field exists and holds a DifferentialDrive. */
  private static void checkDriveField(Robot robot, String name) {
    Object drive = readField(robot, name);
    assertNotNull(drive, name + " should be constructed");
    assertInstanceOf(DifferentialDrive.class, drive, name + " should be a DifferentialDrive");
  }

  @Test
  void checkFields() {
    var robot = new Robot();

    checkMotorField(robot, "leftLeader", 0);
    checkMotorField(robot, "leftFollower", 1);
    checkMotorField(robot, "rightLeader", 2);
    checkMotorField(robot, "rightFollower", 3);
    checkMotorField(robot, "intakeLauncher", 4);
    checkMotorField(robot, "feeder", 5);
    checkDriveField(robot, "drivetrain");

    // Motor controller inversion configuration
    var leftConfig = new TalonFXConfiguration();
    motor(robot, "leftLeader").getConfigurator().refresh(leftConfig);
    assertEquals(
        InvertedValue.Clockwise_Positive,
        leftConfig.MotorOutput.Inverted,
        "leftLeader should be inverted (Clockwise_Positive)");

    var rightConfig = new TalonFXConfiguration();
    motor(robot, "rightLeader").getConfigurator().refresh(rightConfig);
    assertEquals(
        InvertedValue.CounterClockwise_Positive,
        rightConfig.MotorOutput.Inverted,
        "rightLeader should not be inverted (CounterClockwise_Positive)");
  }

  @Test
  void checkTeleop() {
    var robot = new Robot();
    var opmode = new MyTeleop(robot);

    var controller = new NiDsXboxController(0);
    var sim = new NiDsXboxControllerSim(controller);

    var leftLeader = motor(robot, "leftLeader");
    var rightLeader = motor(robot, "rightLeader");
    var intakeLauncher = motor(robot, "intakeLauncher");
    var feeder = motor(robot, "feeder");

    // No inputs pressed: everything should stop.
    sim.setLeftY(0.0);
    sim.setRightX(0.0);
    sim.setRightBumperButton(false);
    sim.setLeftBumperButton(false);
    sim.setAButton(false);
    sim.notifyNewData();
    opmode.periodic();

    assertEquals(
        0.0,
        appliedOutput(intakeLauncher, "intakeLauncher"),
        THROTTLE_TOLERANCE,
        "intakeLauncher should stop");
    assertEquals(0.0, appliedOutput(feeder, "feeder"), THROTTLE_TOLERANCE, "feeder should stop");
    assertEquals(
        0.0, appliedOutput(leftLeader, "leftLeader"), THROTTLE_TOLERANCE, "leftLeader should stop");
    assertEquals(
        0.0,
        appliedOutput(rightLeader, "rightLeader"),
        THROTTLE_TOLERANCE,
        "rightLeader should stop");

    // Left stick forward: drivetrain drives straight (arcadeDrive squares the 0.5 input).
    sim.setLeftY(-0.5);
    sim.setRightX(0.0);
    sim.notifyNewData();
    opmode.periodic();

    assertEquals(
        0.25,
        appliedOutput(leftLeader, "leftLeader"),
        THROTTLE_TOLERANCE,
        "leftLeader should drive forward at half speed");
    assertEquals(
        0.25,
        appliedOutput(rightLeader, "rightLeader"),
        THROTTLE_TOLERANCE,
        "rightLeader should drive forward at half speed");

    // Right stick right: the robot should turn in place (sides spin opposite ways).
    sim.setLeftY(0.0);
    sim.setRightX(0.5);
    sim.notifyNewData();
    opmode.periodic();

    assertEquals(
        -0.25,
        appliedOutput(leftLeader, "leftLeader"),
        THROTTLE_TOLERANCE,
        "leftLeader should turn at half speed");
    assertEquals(
        0.25,
        appliedOutput(rightLeader, "rightLeader"),
        THROTTLE_TOLERANCE,
        "rightLeader should turn at half speed");

    // Right bumper: shoot.
    sim.setRightBumperButton(true);
    sim.setLeftBumperButton(false);
    sim.setAButton(false);
    sim.notifyNewData();
    opmode.periodic();

    assertEquals(
        0.9,
        appliedOutput(intakeLauncher, "intakeLauncher"),
        THROTTLE_TOLERANCE,
        "shooting should spin up the intakeLauncher");
    assertEquals(
        0.75,
        appliedOutput(feeder, "feeder"),
        THROTTLE_TOLERANCE,
        "shooting should feed the shooter");

    // Left bumper: intake.
    sim.setRightBumperButton(false);
    sim.setLeftBumperButton(true);
    sim.notifyNewData();
    opmode.periodic();

    assertEquals(
        0.8,
        appliedOutput(intakeLauncher, "intakeLauncher"),
        THROTTLE_TOLERANCE,
        "intaking should spin up the intakeLauncher");
    assertEquals(
        -1.0,
        appliedOutput(feeder, "feeder"),
        THROTTLE_TOLERANCE,
        "intaking should run the feeder in reverse");

    // A button: outtake.
    sim.setLeftBumperButton(false);
    sim.setAButton(true);
    sim.notifyNewData();
    opmode.periodic();

    assertEquals(
        -0.8,
        appliedOutput(intakeLauncher, "intakeLauncher"),
        THROTTLE_TOLERANCE,
        "outtaking should reverse the intakeLauncher");
    assertEquals(
        1.0,
        appliedOutput(feeder, "feeder"),
        THROTTLE_TOLERANCE,
        "outtaking should run the feeder forward");
  }

  @Test
  void checkAuto() {
    var robot = new Robot();
    var opmode = new MyAuto(robot);

    var leftLeader = motor(robot, "leftLeader");
    var rightLeader = motor(robot, "rightLeader");

    // Drive forward at half speed for the first four seconds (arcadeDrive squares the 0.5 input).
    opmode.start();
    opmode.periodic();

    assertEquals(
        0.25,
        appliedOutput(leftLeader, "leftLeader"),
        THROTTLE_TOLERANCE,
        "auto should drive forward");
    assertEquals(
        0.25,
        appliedOutput(rightLeader, "rightLeader"),
        THROTTLE_TOLERANCE,
        "auto should drive forward");

    // After four seconds the drivetrain should stop.
    SimHooks.stepTiming(4.1);
    opmode.periodic();

    assertEquals(
        0.0,
        appliedOutput(leftLeader, "leftLeader"),
        THROTTLE_TOLERANCE,
        "auto should stop after 4 seconds");
    assertEquals(
        0.0,
        appliedOutput(rightLeader, "rightLeader"),
        THROTTLE_TOLERANCE,
        "auto should stop after 4 seconds");
  }
}

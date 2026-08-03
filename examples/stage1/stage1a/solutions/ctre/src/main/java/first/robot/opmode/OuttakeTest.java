/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package first.robot.opmode;

import first.robot.Robot;
import first.robot.simulation.FuelSim;
import org.wpilib.opmode.PeriodicOpMode;
import org.wpilib.opmode.Teleop;

@Teleop
public class OuttakeTest extends PeriodicOpMode {
  public OuttakeTest(Robot robot) {
    FuelSim.setMode(FuelSim.Mode.OUTTAKE);
  }
}

/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.wpilib.command3.button;

import org.wpilib.command3.Trigger;

/**
 * A shim for the `CommandXboxController` class, which isn't yet present in wpilib beta 6 (but is in
 * beta 7).
 */
public class CommandXboxController {
  public CommandXboxController(int port) {}

  public double getLeftY() {
    return 0;
  }

  public Trigger a() {
    return new Trigger(() -> false);
  }

  public Trigger leftBumper() {
    return new Trigger(() -> false);
  }
}

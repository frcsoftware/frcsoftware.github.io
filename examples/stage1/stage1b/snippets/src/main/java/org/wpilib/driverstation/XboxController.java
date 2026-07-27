/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.wpilib.driverstation;

/**
 * A shim for the `CommandXboxController` class, which isn't yet present in wpilib beta 6 (but is in
 * beta 7).
 */
public class XboxController {
  public XboxController(int id) {}

  public boolean getAButton() {
    return false;
  }
}

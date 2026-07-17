/*
 * Copyright 2026 FRCSoftware
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.wpilib.command3;

import java.util.List;
import java.util.function.Consumer;
import org.wpilib.annotation.NoDiscard;
import org.wpilib.units.measure.Time;

/**
 * A "shim" for the `Mechanism` interface. In WPILib 2027 alpha 6, Mechanisms were still classes;
 * however, alpha 7 changes them to interfaces. In an effort to not rewrite any docs, this shim will
 * exist for now until alpha 7 releases.
 */
public interface Mechanism {
  /**
   * Returns the scheduler under which this subsystem and its default commands are registered. The
   * scheduler is also used to fetch running commands for the subsystem.
   *
   * @return The registered scheduler.
   */
  default Scheduler getRegisteredScheduler() {
    return Scheduler.getDefault();
  }

  /**
   * Gets the name of this mechanism. This will default to the name of this mechanism's class.
   *
   * @return The name of the mechanism.
   */
  @NoDiscard
  default String getName() {
    return getClass().getSimpleName();
  }

  /**
   * Sets the default command to run on the mechanism when no other command is scheduled. The
   * default command's priority is effectively the minimum allowable priority for any command
   * requiring a mechanism. For this reason, it's recommended that a default command have a priority
   * less than {@link Command#DEFAULT_PRIORITY} so it doesn't prevent low-priority commands from
   * running.
   *
   * <p>The default command is initially an idle command that only owns the mechanism without doing
   * anything. This command has the lowest possible priority to allow any other command to run.
   *
   * @param defaultCommand the new default command
   */
  default void setDefaultCommand(Command defaultCommand) {
    getRegisteredScheduler().setDefaultCommand(this, defaultCommand);
  }

  /**
   * Gets the default command that was set by the latest call to {@link
   * #setDefaultCommand(Command)}.
   *
   * @return The currently configured default command
   */
  default Command getDefaultCommand() {
    return getRegisteredScheduler().getDefaultCommandFor(this);
  }

  /**
   * Starts building a command that requires this mechanism.
   *
   * @param commandBody The main function body of the command.
   * @return The command builder, for further configuration.
   */
  default NeedsNameBuilderStage run(Consumer<Coroutine> commandBody) {
    return new StagedCommandBuilder().requiring(this).executing(commandBody);
  }

  /**
   * Starts building a command that requires this mechanism. The given function will be called
   * repeatedly in an infinite loop. Useful for building commands that don't need state or multiple
   * stages of logic.
   *
   * @param loopBody The body of the infinite loop.
   * @return The command builder, for further configuration.
   */
  default NeedsNameBuilderStage runRepeatedly(Runnable loopBody) {
    return run(coroutine -> {
      while (true) {
        loopBody.run();
        coroutine.yield();
      }
    });
  }

  /**
   * Returns a command that idles this mechanism until another command claims it. The idle command
   * has {@link Command#LOWEST_PRIORITY the lowest priority} and can be interrupted by any other
   * command.
   *
   * <p>The {@link #getDefaultCommand() default command} for every mechanism is an idle command
   * unless a different default command has been configured.
   *
   * @return A new idle command.
   */
  default Command idle() {
    return run(Coroutine::park).withPriority(Command.LOWEST_PRIORITY).named(getName() + "[IDLE]");
  }

  /**
   * Returns a command that idles this mechanism for the given duration of time.
   *
   * @param duration How long the mechanism should idle for.
   * @return A new idle command.
   */
  default Command idleFor(Time duration) {
    return idle().withTimeout(duration);
  }

  /**
   * Gets all running commands that require this mechanism. Commands are returned in the order in
   * which they were scheduled. The returned list is read-only. Every command in the list will have
   * been scheduled by the previous entry in the list or by intermediate commands that do not
   * require the mechanism.
   *
   * @return The currently running commands that require the mechanism.
   */
  @NoDiscard
  default List<Command> getRunningCommands() {
    return getRegisteredScheduler().getRunningCommandsFor(this);
  }
}

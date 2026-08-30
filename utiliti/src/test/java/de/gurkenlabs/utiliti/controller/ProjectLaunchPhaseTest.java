package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ProjectLaunchPhaseTest {
  @Test
  void onlyPreparationPhasesKeepLaunchProgressVisible() {
    assertTrue(ProjectLaunchPhase.SAVING.isLaunching());
    assertTrue(ProjectLaunchPhase.RESOLVING_MODEL.isLaunching());
    assertTrue(ProjectLaunchPhase.BUILDING.isLaunching());
    assertTrue(ProjectLaunchPhase.STARTING_GAME.isLaunching());
    assertTrue(ProjectLaunchPhase.ATTACHING_DEBUGGER.isLaunching());
    assertFalse(ProjectLaunchPhase.RUNNING.isLaunching());
    assertFalse(ProjectLaunchPhase.FAILED.isLaunching());
    assertFalse(ProjectLaunchPhase.CANCELLED.isLaunching());
  }
}

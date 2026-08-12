package de.gurkenlabs.utiliti.controller;

/** User-visible phases of preparing and starting an external game project. */
public enum ProjectLaunchPhase {
  IDLE(""),
  SAVING("Saving..."),
  RESOLVING_MODEL("Resolving..."),
  BUILDING("Building..."),
  STARTING_GAME("Starting..."),
  ATTACHING_DEBUGGER("Attaching debugger..."),
  RUNNING("Project is running"),
  PAUSED("Paused in debugger"),
  STOPPING("Stopping..."),
  FAILED("Project launch failed"),
  CANCELLED("Project launch cancelled");

  private final String displayText;

  ProjectLaunchPhase(String displayText) {
    this.displayText = displayText;
  }

  public String displayText() {
    return this.displayText;
  }

  public boolean isLaunching() {
    return switch (this) {
      case SAVING, RESOLVING_MODEL, BUILDING, STARTING_GAME, ATTACHING_DEBUGGER -> true;
      default -> false;
    };
  }
}

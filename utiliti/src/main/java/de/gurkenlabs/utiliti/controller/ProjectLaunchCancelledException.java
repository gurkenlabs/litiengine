package de.gurkenlabs.utiliti.controller;

import java.io.IOException;

/** Indicates that a pending project launch was deliberately cancelled. */
public final class ProjectLaunchCancelledException extends IOException {
  public ProjectLaunchCancelledException() {
    super("Project launch was cancelled.");
  }
}

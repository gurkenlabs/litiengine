package de.gurkenlabs.utiliti.controller.debug;

import java.util.Objects;

/** A persistent source breakpoint for one runtime script. */
public record ScriptBreakpoint(String project, String scriptId, String source, int line, boolean enabled) {
  public ScriptBreakpoint {
    project = Objects.requireNonNullElse(project, "");
    scriptId = Objects.requireNonNullElse(scriptId, "");
    source = Objects.requireNonNullElse(source, "");
    if (line < 1) throw new IllegalArgumentException("Breakpoint line must be positive.");
  }
}

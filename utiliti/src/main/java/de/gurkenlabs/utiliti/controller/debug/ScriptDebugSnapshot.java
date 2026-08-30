package de.gurkenlabs.utiliti.controller.debug;

import java.util.List;

/** Immutable paused-state data safe to hand from the JDI event thread to Swing. */
public record ScriptDebugSnapshot(String thread, List<Frame> frames) {
  public ScriptDebugSnapshot {
    frames = frames == null ? List.of() : List.copyOf(frames);
  }

  public record Frame(String className, String method, String source, int line, List<Variable> variables) {
    public Frame {
      variables = variables == null ? List.of() : List.copyOf(variables);
    }

    @Override
    public String toString() {
      return className + "." + method + " (" + source + ":" + line + ")";
    }
  }

  public record Variable(String name, String type, String value, String reference) {
    public Variable(String name, String type, String value) {
      this(name, type, value, null);
    }

    public boolean expandable() {
      return this.reference != null && !this.reference.isBlank();
    }
  }
}

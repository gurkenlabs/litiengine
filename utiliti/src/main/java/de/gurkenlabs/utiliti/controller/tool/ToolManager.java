package de.gurkenlabs.utiliti.controller.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ToolManager {
  private static ToolManager instance;

  private final List<Tool> tools;
  private final List<Runnable> listeners;
  private Tool activeTool;

  private ToolManager() {
    this.tools = new ArrayList<>();
    this.listeners = new ArrayList<>();
  }

  public static ToolManager instance() {
    if (instance == null) {
      instance = new ToolManager();
    }
    return instance;
  }

  public void register(Tool tool) {
    tools.add(tool);
    if (activeTool == null) {
      activeTool = tool;
    }
  }

  public List<Tool> getTools() {
    return Collections.unmodifiableList(tools);
  }

  public Tool getActiveTool() {
    return activeTool;
  }

  public void setActiveTool(Tool tool) {
    if (activeTool != null && activeTool.equals(tool)) {
      return;
    }
    if (activeTool != null) {
      activeTool.deactivated();
    }
    activeTool = tool;
    if (activeTool != null) {
      activeTool.activated();
    }
    for (Runnable listener : listeners) {
      listener.run();
    }
  }

  public void addListener(Runnable listener) {
    listeners.add(listener);
  }

  public void removeListener(Runnable listener) {
    listeners.remove(listener);
  }

  public static void reset() {
    instance = null;
  }
}

package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.utiliti.mcp.McpServer.ActionState;
import de.gurkenlabs.utiliti.mcp.McpServer.ActionStatus;
import de.gurkenlabs.utiliti.model.Style;
import org.junit.jupiter.api.Test;

class StatusBarTest {
  @Test
  void formatsCurrentAndEstimatedMaximumFps() {
    assertEquals("60 FPS  |  240 MAX", StatusBar.formatFps(60, 240));
  }

  @Test
  void colorsFpsByDistanceFromConfiguredCap() {
    assertEquals(de.gurkenlabs.utiliti.model.Style.COLOR_GREEN, StatusBar.fpsColor(60, 60));
    assertEquals(de.gurkenlabs.utiliti.model.Style.COLOR_GREEN, StatusBar.fpsColor(59, 60));
    assertEquals(de.gurkenlabs.utiliti.model.Style.COLOR_GREEN, StatusBar.fpsColor(61, 60));
    assertEquals(StatusBar.FPS_WARNING_COLOR, StatusBar.fpsColor(58, 60));
    assertEquals(StatusBar.FPS_WARNING_COLOR, StatusBar.fpsColor(54, 60));
    assertEquals(de.gurkenlabs.utiliti.model.Style.COLOR_RED, StatusBar.fpsColor(53, 60));
  }

  @Test
  void describesMcpActionInTooltip() {
    assertEquals("MCP Server listening on port 8080. Click to view connected clients.",
        StatusBar.mcpTooltip(
        8080, new ActionStatus(ActionState.IDLE, null)));
    assertEquals("MCP action in progress: Set Tile. Click to view connected clients.",
        StatusBar.mcpTooltip(
        8080, new ActionStatus(ActionState.RUNNING, "set-tile")));
    assertEquals("MCP action completed: Save Project. Click to view connected clients.",
        StatusBar.mcpTooltip(
        8080, new ActionStatus(ActionState.SUCCEEDED, "save-project")));
    assertEquals("MCP action failed: Load Project. Click to view connected clients.",
        StatusBar.mcpTooltip(
        8080, new ActionStatus(ActionState.FAILED, "load-project")));
  }

  @Test
  void colorsMcpActionState() {
    assertEquals(Style.mutedText(), StatusBar.mcpColor(
        false, new ActionStatus(ActionState.IDLE, null)));
    assertEquals(Style.COLOR_GREEN, StatusBar.mcpColor(
        true, new ActionStatus(ActionState.IDLE, null)));
    assertEquals(Style.COLOR_ORANGE, StatusBar.mcpColor(
        true, new ActionStatus(ActionState.RUNNING, "set-tile")));
    assertEquals(Style.COLOR_RED, StatusBar.mcpColor(
        true, new ActionStatus(ActionState.FAILED, "set-tile")));
  }
}

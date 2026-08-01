package de.gurkenlabs.utiliti.mcp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.json.JsonArray;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class McpTilesetHandlerTest {

  @Test
  void registersTileGeometryInspectionTools() {
    JsonArray tools = McpToolHandler.getToolsList().getJsonArray("tools");
    Set<String> names = tools.stream().map(value -> value.asJsonObject().getString("name"))
        .collect(Collectors.toSet());

    assertTrue(names.contains("render-tileset"));
    assertTrue(names.contains("find-tile-usage"));
    assertTrue(names.contains("render-tile-context"));
    assertTrue(names.contains("preview-tile-edits"));
  }

  @Test
  void geometryToolNamesAreHandledByTilesetHandler() {
    assertTrue(McpTilesetHandler.handles("render-tileset"));
    assertTrue(McpTilesetHandler.handles("find-tile-usage"));
    assertTrue(McpTilesetHandler.handles("render-tile-context"));
    assertTrue(McpTilesetHandler.handles("preview-tile-edits"));
  }

  @Test
  void semanticRegistryExposesSnakeCaseAliases() {
    JsonArray tools = McpSemanticToolRegistry.getSemanticToolsList().getJsonArray("tools");
    Set<String> names = tools.stream().map(value -> value.asJsonObject().getString("name"))
        .collect(Collectors.toSet());

    assertTrue(names.containsAll(Set.of(
        "render_tileset", "find_tile_usage", "render_tile_context", "preview_tile_edits")));
  }
}

package de.gurkenlabs.utiliti.mcp;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.utiliti.controller.Editor;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
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

  @Test
  void previewTileEditsRestoresDuplicateCoordinates() throws Exception {
    Editor editor = Editor.instance();
    java.lang.reflect.Field gameFileField = Editor.class.getDeclaredField("gameFile");
    gameFileField.setAccessible(true);
    ResourceBundle previous = (ResourceBundle) gameFileField.get(editor);
    ResourceBundle project = new ResourceBundle();
    TmxMap map = new TmxMap();
    map.setName("preview-map");
    map.setWidth(4);
    map.setHeight(4);
    map.setTileWidth(16);
    map.setTileHeight(16);
    TileLayer layer = new TileLayer(4, 4);
    layer.setName("ground");
    layer.setTile(1, 1, 7);
    map.addLayer(layer);
    project.getMaps().add(map);
    gameFileField.set(editor, project);
    try {
      JsonObject response = McpTilesetHandler.handle("preview-tile-edits", Json.createObjectBuilder()
          .add("mapId", "preview-map").add("layer", "ground")
          .add("edits", Json.createArrayBuilder()
              .add(Json.createObjectBuilder().add("x", 1).add("y", 1).add("gid", 2))
              .add(Json.createObjectBuilder().add("x", 1).add("y", 1).add("gid", 3)))
          .build());

      assertTrue(response.getBoolean("success"), response::toString);
      assertEquals(7, layer.getTile(1, 1).getGridId());
    } finally {
      gameFileField.set(editor, previous);
    }
  }
}

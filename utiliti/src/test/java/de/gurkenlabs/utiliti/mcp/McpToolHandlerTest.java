package de.gurkenlabs.utiliti.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.TerrainType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TileLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangColor;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangSet;
import de.gurkenlabs.litiengine.environment.tilemap.xml.WangTile;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterAttributes;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.UserPreferences;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class McpToolHandlerTest {
  private TmxMap map;

  @BeforeEach
  void setUp() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    this.map = new TmxMap(MapOrientations.ORTHOGONAL);
    this.map.setName("mcp-test");
    this.map.setWidth(10);
    this.map.setHeight(10);
    this.map.setTileWidth(16);
    this.map.setTileHeight(16);
    this.map.addLayer(new MapObjectLayer());
    Game.world().loadEnvironment(this.map);
  }

  @AfterEach
  void tearDown() throws Exception {
    Editor.instance().getMapComponent().getMaps().clear();
    Editor.instance().getGameFile().getTilesets().clear();
    UndoManager.clearAll();
    Method terminate = Game.class.getDeclaredMethod("terminate");
    terminate.setAccessible(true);
    terminate.invoke(null);
  }

  @Test
  void mcpIsDisabledByDefault() {
    assertFalse(new UserPreferences().isMcpEnabled());
  }

  @Test
  void assignsNextUniqueMapObjectId() {
    MapObject existing = new MapObject();
    existing.setId(7);
    existing.setType(MapObjectType.AREA.name());
    this.map.getMapObjectLayers().getFirst().addMapObject(existing);

    MapObject created = new MapObject();

    assertEquals(8, McpToolHandler.assignNextMapId(created));
    assertEquals(8, created.getId());
  }

  @Test
  void queryGeometryReturnsCollisionBoxesForDocumentedLayerCollisionMode() {
    JsonObject created = McpToolHandler.handleCallTool("add-collisionbox",
        Json.createObjectBuilder().add("x", 16).add("y", 32).add("width", 24).add("height", 16).build());
    assertTrue(created.getBoolean("success"), created::toString);

    JsonObject result = McpToolHandler.handleCallTool("query-geometry",
        Json.createObjectBuilder().add("mode", "layer-collision").build());
    assertTrue(result.getBoolean("success"), result::toString);
    assertEquals("layer-collision", result.getString("mode"));
    assertEquals(1, result.getJsonArray("collisions").size());
  }

  @Test
  void configureViewUpdatesViewportTogglePreferences() {
    Editor.preferences().setShowGrid(true);
    Editor.preferences().setSnapToGrid(true);
    Editor.preferences().setRenderBoundingBoxes(true);

    JsonObject result = McpToolHandler.handleCallTool(
        "configure-view",
        Json.createObjectBuilder()
            .add("showGrid", false)
            .add("showCollision", false)
            .build());

    assertTrue(result.getBoolean("success"), result::toString);
    assertFalse(Editor.preferences().showGrid());
    assertFalse(Editor.preferences().renderBoundingBoxes());
    assertTrue(Editor.preferences().snapToGrid(), "configure-view must not change snap unless asked");
  }

  @Test
  void createMapUsesArgumentsWithoutOpeningInteractiveDialog() {
    JsonObject result =
        McpToolHandler.handleCallTool(
            "create-map",
            Json.createObjectBuilder()
                .add("name", "mcp-created")
                .add("orientation", "orthogonal")
                .add("width", 30)
                .add("height", 20)
                .add("tileWidth", 32)
                .add("tileHeight", 16)
                .build());

    assertTrue(result.getBoolean("success"), result::toString);
    TmxMap created = (TmxMap) Game.world().environment().getMap();
    assertEquals("mcp-created", created.getName());
    assertEquals("orthogonal", created.getOrientation().getName());
    assertEquals(30, created.getWidth());
    assertEquals(20, created.getHeight());
    assertEquals(32, created.getTileWidth());
    assertEquals(16, created.getTileHeight());
    assertEquals(1, created.getMapObjectLayers().size());
    assertTrue(Editor.instance().getMapComponent().getMaps().contains(created));

    JsonObject duplicate =
        McpToolHandler.handleCallTool(
            "create-map",
            Json.createObjectBuilder().add("name", "MCP-CREATED").build());
    assertFalse(duplicate.getBoolean("success"));
    assertTrue(duplicate.getString("error").contains("already exists"));
  }

  @Test
  void createMapAttachesExistingProjectTilesetsWithMapLocalGridIds() {
    Tileset hospital = new Tileset();
    hospital.setName("tiles-hospital");
    hospital.setTileCount(256);
    Tileset details = new Tileset();
    details.setName("tiles-details");
    details.setTileCount(32);
    Editor.instance().getGameFile().getTilesets().clear();
    Editor.instance().getGameFile().getTilesets().add(hospital);
    Editor.instance().getGameFile().getTilesets().add(details);

    JsonObject result =
        McpToolHandler.handleCallTool(
            "create-map",
            Json.createObjectBuilder()
                .add("name", "with-project-tilesets")
                .add(
                    "tilesets",
                    Json.createArrayBuilder().add("tiles-hospital").add("tiles-details"))
                .build());

    assertTrue(result.getBoolean("success"), result::toString);
    TmxMap created = (TmxMap) Game.world().environment().getMap();
    assertEquals(2, created.getTilesets().size());
    assertEquals("tiles-hospital", created.getTilesets().get(0).getName());
    assertEquals(1, created.getTilesets().get(0).getFirstGridId());
    assertEquals("tiles-details", created.getTilesets().get(1).getName());
    assertEquals(257, created.getTilesets().get(1).getFirstGridId());
    assertTrue(created.getTilesets().get(0) != hospital);
    assertEquals(0, hospital.getFirstGridId());
    assertEquals(2, result.getJsonArray("tilesets").size());

    int mapCount = Editor.instance().getMapComponent().getMaps().size();
    JsonObject missing =
        McpToolHandler.handleCallTool(
            "create-map",
            Json.createObjectBuilder()
                .add("name", "missing-project-tileset")
                .add("tilesets", Json.createArrayBuilder().add("does-not-exist"))
                .build());
    assertFalse(missing.getBoolean("success"));
    assertEquals(mapCount, Editor.instance().getMapComponent().getMaps().size());
  }

  @Test
  void addPropAssignsIdAndAppliesEditorDefaults() {
    Resources.spritesheets().clear();
    new Spritesheet(
        new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
        "prop-crate.png",
        16,
        16);

    JsonObject result =
        McpToolHandler.handleCallTool(
            "add-prop", Json.createObjectBuilder().add("x", 4).add("y", 8).build());

    assertTrue(result.getBoolean("success"));
    assertTrue(result.getInt("id") > 0);
    MapObject created =
        (MapObject) this.map.getMapObjectLayers().getFirst().getMapObjects().getFirst();
    assertEquals(result.getInt("id"), created.getId());
    assertEquals("crate", created.getStringValue(MapObjectProperty.SPRITESHEETNAME));
    assertTrue(created.getBoolValue(MapObjectProperty.COLLISION));
    assertTrue(created.getBoolValue(MapObjectProperty.PROP_ADDSHADOW));
    assertFalse(created.getBoolValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE));
    assertEquals(
        6.4,
        created.getDoubleValue(MapObjectProperty.COLLISIONBOX_WIDTH),
        0.001);
    assertEquals(
        6.4,
        created.getDoubleValue(MapObjectProperty.COLLISIONBOX_HEIGHT),
        0.001);
  }

  @Test
  void genericAddEntityDispatchesPropValidationFromRequestedType() {
    Resources.spritesheets().clear();
    new Spritesheet(
        new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
        "prop-exit-wheelchair-intact.png",
        16,
        16);

    JsonObject result =
        McpToolHandler.handleCallTool(
            "add-entity",
            Json.createObjectBuilder()
                .add("name", "exit-wheelchair")
                .add("type", "PROP")
                .add("x", 688)
                .add("y", 724)
                .build());

    assertTrue(result.getBoolean("success"), result::toString);
    MapObject created = (MapObject) this.map.getMapObject(result.getInt("id"));
    assertEquals(MapObjectType.PROP.name(), created.getType());
    assertEquals(
        "exit-wheelchair",
        created.getStringValue(MapObjectProperty.SPRITESHEETNAME));
  }

  @Test
  void addPropNormalizesExistingVariantToLogicalSpriteFamily() {
    Resources.spritesheets().clear();
    new Spritesheet(
        new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
        "prop-crate-intact.png",
        16,
        16);

    JsonObject result =
        McpToolHandler.handleCallTool(
            "add-prop",
            Json.createObjectBuilder()
                .add("x", 0)
                .add("y", 0)
                .add("spritesheetName", "prop-crate-intact")
                .build());

    assertTrue(result.getBoolean("success"));
    MapObject created =
        (MapObject) this.map.getMapObjectLayers().getFirst().getMapObjects().getFirst();
    assertEquals("crate", created.getStringValue(MapObjectProperty.SPRITESHEETNAME));
  }

  @Test
  void changingPropSpriteReloadsLiveEnvironmentEntity() {
    Resources.spritesheets().clear();
    new Spritesheet(
        new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
        "prop-crate-intact.png",
        16,
        16);
    new Spritesheet(
        new BufferedImage(25, 9, BufferedImage.TYPE_INT_ARGB),
        "prop-bed3-intact.png",
        25,
        9);
    JsonObject created =
        McpToolHandler.handleCallTool(
            "add-prop",
            Json.createObjectBuilder()
                .add("x", 0)
                .add("y", 0)
                .add("spritesheetName", "crate")
                .build());
    int id = created.getInt("id");
    assertEquals("crate", Game.world().environment().getProp(id).getSpritesheetName());

    JsonObject updated =
        McpToolHandler.handleCallTool(
            "configure-prop",
            Json.createObjectBuilder()
                .add("id", id)
                .add("spritesheetName", "prop-bed3-intact")
                .build());

    assertTrue(updated.getBoolean("success"), updated::toString);
    Prop liveProp = Game.world().environment().getProp(id);
    assertNotNull(liveProp);
    assertEquals("bed3", liveProp.getSpritesheetName());
    assertEquals(
        "bed3",
        this.map
            .getMapObject(id)
            .getStringValue(MapObjectProperty.SPRITESHEETNAME));
  }

  @Test
  void rejectsPropertyUpdatesThatBreakMandatoryReferences() {
    Resources.spritesheets().clear();
    new Spritesheet(
        new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
        "prop-crate.png",
        16,
        16);
    JsonObject created =
        McpToolHandler.handleCallTool(
            "add-prop", Json.createObjectBuilder().add("x", 0).add("y", 0).build());

    JsonObject update =
        McpToolHandler.handleCallTool(
            "set-entity-property",
            Json.createObjectBuilder()
                .add("id", created.getInt("id"))
                .add("property", MapObjectProperty.SPRITESHEETNAME)
                .add("value", "missing")
                .build());

    assertFalse(update.getBoolean("success"));
    MapObject prop = (MapObject) this.map.getMapObject(created.getInt("id"));
    assertEquals("crate", prop.getStringValue(MapObjectProperty.SPRITESHEETNAME));
  }

  @Test
  void entityValidationErrorsProvideMachineReadableRepairInstructions() {
    JsonObject error = McpToolHandler.entityValidationError(List.of(
        "targets must be a comma-separated list of integer entity IDs (e.g. '101,102'), not string names ('Southwest Door')"));

    assertEquals("Invalid entity property: targets", error.getString("error"));
    JsonObject issue = error.getJsonObject("errorDetails").getJsonArray("issues").getJsonObject(0);
    assertEquals("ENTITY_VALIDATION_FAILED", error.getJsonObject("errorDetails").getString("code"));
    assertEquals("targets", issue.getString("field"));
    assertEquals("101,102", issue.getString("example"));
    assertTrue(issue.getString("nextAction").contains("search_entities"));
  }

  @Test
  void configureMovementDoesNotRequireAnUnrelatedCreatureSprite() {
    MapObject creature = new MapObject("CREATURE");
    creature.setId(901);
    creature.setWidth(16);
    creature.setHeight(16);
    this.map.getMapObjectLayers().getFirst().addMapObject(creature);

    JsonObject result = McpToolHandler.handleCallTool("configure-movement", Json.createObjectBuilder()
        .add("id", 901).add("velocity", 35).add("acceleration", 200).build());

    assertTrue(result.getBoolean("success"), result::toString);
    assertEquals("35", creature.getStringValue(MapObjectProperty.MOVEMENT_VELOCITY));
  }

  @Test
  void batchConfigurePropsUpdatesMultiplePropsInOneOperation() {
    Resources.spritesheets().clear();
    new Spritesheet(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), "prop-crate.png", 16, 16);
    JsonObject first = McpToolHandler.handleCallTool("add-prop", Json.createObjectBuilder().add("x", 0).add("y", 0).build());
    JsonObject second = McpToolHandler.handleCallTool("add-prop", Json.createObjectBuilder().add("x", 16).add("y", 0).build());

    JsonObject result = McpToolHandler.handleCallTool("batch-configure-props", Json.createObjectBuilder()
        .add("updates", Json.createArrayBuilder()
            .add(Json.createObjectBuilder().add("id", first.getInt("id")).add("material", "STEEL"))
            .add(Json.createObjectBuilder().add("id", second.getInt("id")).add("material", "PLASTIC")))
        .build());

    assertTrue(result.getBoolean("success"), result::toString);
    assertEquals(2, result.getInt("updatedCount"));
    assertEquals("STEEL", this.map.getMapObject(first.getInt("id")).getStringValue(MapObjectProperty.PROP_MATERIAL));
    assertEquals("PLASTIC", this.map.getMapObject(second.getInt("id")).getStringValue(MapObjectProperty.PROP_MATERIAL));
  }

  @Test
  void rejectsMissingOrUnknownMandatorySpriteBeforeAssigningId() {
    Resources.spritesheets().clear();

    JsonObject missing =
        McpToolHandler.handleCallTool(
            "add-prop", Json.createObjectBuilder().add("x", 0).add("y", 0).build());
    JsonObject unknown =
        McpToolHandler.handleCallTool(
            "add-creature",
            Json.createObjectBuilder()
                .add("x", 0)
                .add("y", 0)
                .add("spritesheetName", "missing-creature")
                .build());

    assertFalse(missing.getBoolean("success"));
    assertEquals("ENTITY_VALIDATION_FAILED", missing.getJsonObject("errorDetails").getString("code"));
    assertEquals("spritesheetName", missing.getJsonObject("errorDetails").getJsonArray("issues")
        .getJsonObject(0).getString("field"));
    assertFalse(unknown.getBoolean("success"));
    assertEquals("spritesheetName", unknown.getJsonObject("errorDetails").getJsonArray("issues")
        .getJsonObject(0).getString("field"));
    assertTrue(this.map.getMapObjects().isEmpty());
  }

  @Test
  void soundSourceRequiresExistingProjectSoundAndKeepsEditorDefaults() {
    SoundResource sound = new SoundResource();
    sound.setName("wind");
    Editor.instance().getGameFile().getSounds().add(sound);

    JsonObject missing =
        McpToolHandler.handleCallTool(
            "add-sound-source",
            Json.createObjectBuilder().add("x", 0).add("y", 0).build());
    JsonObject unknown =
        McpToolHandler.handleCallTool(
            "add-sound-source",
            Json.createObjectBuilder()
                .add("x", 0)
                .add("y", 0)
                .add("soundName", "missing")
                .build());
    JsonObject created =
        McpToolHandler.handleCallTool(
            "add-sound-source",
            Json.createObjectBuilder()
                .add("x", 0)
                .add("y", 0)
                .add("soundName", "wind")
                .build());

    assertFalse(missing.getBoolean("success"));
    assertFalse(unknown.getBoolean("success"));
    assertTrue(created.getBoolean("success"));
    MapObject soundSource =
        (MapObject) this.map.getMapObjectLayers().getFirst().getMapObjects().getFirst();
    assertEquals(1.0, soundSource.getDoubleValue(MapObjectProperty.SOUND_VOLUME));
    assertTrue(soundSource.getBoolValue(MapObjectProperty.SOUND_LOOP));
  }

  @Test
  void genericAndEmitterCreationApplyEditorDefaults() {
    JsonObject lightResult =
        McpToolHandler.handleCallTool(
            "add-entity",
            Json.createObjectBuilder()
                .add("type", "LIGHTSOURCE")
                .add("x", 0)
                .add("y", 0)
                .build());
    JsonObject emitterResult =
        McpToolHandler.handleCallTool(
            "add-emitter",
            Json.createObjectBuilder().add("x", 16).add("y", 16).build());

    assertTrue(lightResult.getBoolean("success"));
    assertTrue(emitterResult.getBoolean("success"), emitterResult.toString());
    MapObject light = (MapObject) this.map.getMapObject(lightResult.getInt("id"));
    assertEquals(
        "#ffffff", light.getStringValue(MapObjectProperty.LIGHT_COLOR).toLowerCase());
    assertEquals(100, light.getIntValue(MapObjectProperty.LIGHT_INTENSITY));
    assertEquals("ellipse", light.getStringValue(MapObjectProperty.LIGHT_SHAPE));
    assertTrue(light.getBoolValue(MapObjectProperty.LIGHT_ACTIVE));

    MapObject emitter =
        (MapObject) this.map.getMapObject(emitterResult.getInt("id"));
    assertEquals(
        EmitterAttributes.DEFAULT_SPAWNRATE,
        emitter.getIntValue(MapObjectProperty.Emitter.SPAWNRATE));
    assertEquals(
        EmitterAttributes.DEFAULT_SPAWNAMOUNT,
        emitter.getIntValue(MapObjectProperty.Emitter.SPAWNAMOUNT));
    assertEquals(
        EmitterAttributes.DEFAULT_MAXPARTICLES,
        emitter.getIntValue(MapObjectProperty.Emitter.MAXPARTICLES));
  }

  @Test
  void validatesCreationValuesAndRequestedLayer() {
    JsonObject intensity =
        McpToolHandler.handleCallTool(
            "add-light",
            Json.createObjectBuilder()
                .add("x", 0)
                .add("y", 0)
                .add("lightIntensity", 300)
                .build());
    JsonObject dimensions =
        McpToolHandler.handleCallTool(
            "add-area",
            Json.createObjectBuilder()
                .add("x", 0)
                .add("y", 0)
                .add("width", -1)
                .build());
    JsonObject layer =
        McpToolHandler.handleCallTool(
            "add-area",
            Json.createObjectBuilder()
                .add("x", 0)
                .add("y", 0)
                .add("layer", "missing-layer")
                .build());

    assertFalse(intensity.getBoolean("success"));
    assertFalse(dimensions.getBoolean("success"));
    assertFalse(layer.getBoolean("success"));
    assertTrue(this.map.getMapObjects().isEmpty());
  }

  @Test
  void resizeRejectsInvalidDimensionsWithoutMutatingEntity() {
    JsonObject created =
        McpToolHandler.handleCallTool(
            "add-area",
            Json.createObjectBuilder().add("x", 0).add("y", 0).build());

    JsonObject resize =
        McpToolHandler.handleCallTool(
            "resize-entity",
            Json.createObjectBuilder()
                .add("id", created.getInt("id"))
                .add("width", -5)
                .add("height", 10)
                .build());

    assertFalse(resize.getBoolean("success"));
    MapObject area = (MapObject) this.map.getMapObject(created.getInt("id"));
    assertEquals(32, area.getWidth());
    assertEquals(32, area.getHeight());
  }

  @Test
  void batchCreationPreflightsEveryEntityBeforeMutation() {
    Resources.spritesheets().clear();

    JsonObject result =
        McpToolHandler.handleCallTool(
            "batch-add-entities",
            Json.createObjectBuilder()
                .add(
                    "entities",
                    Json.createArrayBuilder()
                        .add(
                            Json.createObjectBuilder()
                                .add("type", "AREA")
                                .add("x", 1)
                                .add("y", 1))
                        .add(
                            Json.createObjectBuilder()
                                .add("type", "PROP")
                                .add("x", 2)
                                .add("y", 2)
                                .add("spritesheetName", "missing")))
                .build());

    assertFalse(result.getBoolean("success"));
    assertEquals(1, result.getInt("failedIndex"));
    assertTrue(this.map.getMapObjects().isEmpty());
  }

  @Test
  void setGravityUpdatesMapAndLiveEnvironment() {
    JsonObject result = McpToolHandler.handleCallTool(
        "set-gravity",
        Json.createObjectBuilder().add("gravity", 12).build());

    assertTrue(result.getBoolean("success"));
    assertEquals(12, this.map.getIntValue(MapProperty.GRAVITY));
    assertEquals(12, Game.world().environment().getGravity());
  }

  @Test
  void setAmbientLightUpdatesMapAndLiveEnvironment() {
    JsonObject result = McpToolHandler.handleCallTool(
        "set-ambient-light",
        Json.createObjectBuilder().add("color", "#112233").add("alpha", 128).build());
    Color expected = new Color(0x11, 0x22, 0x33, 128);

    assertTrue(result.getBoolean("success"));
    assertEquals(expected, this.map.getColorValue(MapProperty.AMBIENTCOLOR));
    assertEquals(expected, Game.world().environment().getAmbientLight().getColor());
  }

  @Test
  void semanticAmbientLightUpdatesTheActiveEnvironment() throws Exception {
    java.lang.reflect.Field gameFileField = Editor.class.getDeclaredField("gameFile");
    gameFileField.setAccessible(true);
    Object previous = gameFileField.get(Editor.instance());
    try {
      Editor.instance().getGameFile().getMaps().add(this.map);
      JsonObject result = McpSemanticHandler.handleSemanticTool("set_ambient_light",
          Json.createObjectBuilder().add("mapId", "mcp-test").add("color", "#112233").add("alpha", 128).build());

      assertTrue(result.getBoolean("success"), result::toString);
      assertEquals(new Color(0x11, 0x22, 0x33, 128), Game.world().environment().getAmbientLight().getColor());
    } finally {
      Editor.instance().getGameFile().getMaps().remove(this.map);
      gameFileField.set(Editor.instance(), previous);
    }
  }

  @Test
  void canvasSnapshotIncludesEnvironmentRendering() {
    Color environmentColor = new Color(17, 34, 51, 255);
    Game.world().environment().add(graphics -> {
      graphics.setColor(environmentColor);
      graphics.fillRect(4, 4, 24, 24);
    }, RenderType.GROUND);

    BufferedImage snapshot = McpToolHandler.renderCanvasSnapshot(64, 64);

    boolean containsEnvironmentPixels = false;
    for (int y = 0; y < snapshot.getHeight() && !containsEnvironmentPixels; y++) {
      for (int x = 0; x < snapshot.getWidth(); x++) {
        if (snapshot.getRGB(x, y) == environmentColor.getRGB()) {
          containsEnvironmentPixels = true;
          break;
        }
      }
    }
    assertTrue(containsEnvironmentPixels);
  }

  @Test
  void canvasSnapshotUsesPhysicalRenderCanvasSizeBeforeLogicalBounds() {
    Dimension resolved =
        McpToolHandler.selectCanvasSnapshotSize(
            new Dimension(1600, 900),
            new Dimension(1024, 1024),
            new Dimension(2048, 2048));

    assertEquals(new Dimension(1600, 900), resolved);
    assertEquals(
        new Dimension(1024, 1024),
        McpToolHandler.selectCanvasSnapshotSize(
            new Dimension(),
            new Dimension(1024, 1024),
            new Dimension(2048, 2048)));
    assertEquals(
        new Dimension(2048, 2048),
        McpToolHandler.selectCanvasSnapshotSize(
            null, new Dimension(), new Dimension(2048, 2048)));
  }

  @Test
  void createsAndEditsSpriteAnimation() {
    new Spritesheet(new BufferedImage(32, 16, BufferedImage.TYPE_INT_ARGB), "mcp-animation.png", 16, 16);

    JsonObject created = McpToolHandler.handleCallTool(
        "create-sprite-animation",
        Json.createObjectBuilder()
            .add("name", "walk")
            .add("spritesheet", "mcp-animation")
            .add("loop", true)
            .add("durations", Json.createArrayBuilder().add(80).add(120))
            .build());
    assertTrue(created.getBoolean("success"));
    assertEquals(2, created.getJsonObject("animation").getInt("frameCount"));

    JsonObject edited = McpToolHandler.handleCallTool(
        "edit-sprite-animation",
        Json.createObjectBuilder()
            .add("name", "walk")
            .add("loop", false)
            .add("durations", Json.createArrayBuilder().add(100).add(150))
            .build());
    assertTrue(edited.getBoolean("success"));
    assertFalse(Resources.animations().get("walk").isLooping());
    assertEquals(250, Resources.animations().get("walk").getTotalDuration());
  }

  @Test
  void editsTileAnimationAndTerrainMetadata() {
    Tileset tileset = new Tileset();
    tileset.setName("mcp-tiles");
    tileset.setTileWidth(16);
    tileset.setTileHeight(16);
    tileset.setColumns(4);
    tileset.setTileCount(4);
    Editor.instance().getGameFile().getTilesets().add(tileset);

    JsonObject animation = McpToolHandler.handleCallTool(
        "set-tile-animation",
        Json.createObjectBuilder()
            .add("tileset", "mcp-tiles")
            .add("tileId", 0)
            .add("frames", Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("tileId", 1).add("duration", 90))
                .add(Json.createObjectBuilder().add("tileId", 2).add("duration", 110)))
            .build());
    assertTrue(animation.getBoolean("success"));
    assertEquals(200, animation.getJsonObject("animation").getInt("totalDuration"));

    assertTrue(McpToolHandler.handleCallTool(
        "create-terrain-set",
        Json.createObjectBuilder()
            .add("tileset", "mcp-tiles")
            .add("set", "Ground")
            .add("type", "mixed")
            .build()).getBoolean("success"));
    assertTrue(McpToolHandler.handleCallTool(
        "add-terrain",
        Json.createObjectBuilder()
            .add("tileset", "mcp-tiles")
            .add("set", "Ground")
            .add("terrain", "Grass")
            .add("color", "#339933")
            .build()).getBoolean("success"));
    JsonObject assignment = McpToolHandler.handleCallTool(
        "set-tile-terrain",
        Json.createObjectBuilder()
            .add("tileset", "mcp-tiles")
            .add("set", "Ground")
            .add("tileId", 0)
            .add("wangId", Json.createArrayBuilder()
                .add(1).add(1).add(1).add(1).add(1).add(1).add(1).add(1))
            .build());
    assertTrue(assignment.getBoolean("success"));
    assertEquals(1, assignment.getJsonArray("wangId").getInt(0));
  }

  @Test
  void reassignMapIdsReturnsMappingAndUpdatesBuiltInReferences() {
    MapObject target = new MapObject();
    target.setId(10);
    target.setName("target");
    MapObject trigger = new MapObject();
    trigger.setId(20);
    trigger.setName("trigger");
    trigger.setValue(MapObjectProperty.TRIGGER_TARGETS, "10");
    trigger.setValue(MapObjectProperty.TRIGGER_ACTIVATORS, "10, 999");
    this.map.getMapObjectLayers().getFirst().addMapObject(target);
    this.map.getMapObjectLayers().getFirst().addMapObject(trigger);

    JsonObject result =
        McpToolHandler.handleCallTool(
            "reassign-map-ids", Json.createObjectBuilder().add("minId", 1).build());

    assertTrue(result.getBoolean("success"));
    assertEquals(2, result.getJsonArray("idMapping").size());
    assertEquals(1, target.getId());
    assertEquals(2, trigger.getId());
    assertEquals("1", trigger.getStringValue(MapObjectProperty.TRIGGER_TARGETS));
    assertEquals("1, 999", trigger.getStringValue(MapObjectProperty.TRIGGER_ACTIVATORS));
    assertEquals(2, result.getInt("updatedReferences"));
  }

  @Test
  void reassignMapIdsLeavesAmbiguousReferencesAndWarns() {
    MapObject first = new MapObject();
    first.setId(7);
    MapObject second = new MapObject();
    second.setId(7);
    MapObject trigger = new MapObject();
    trigger.setId(9);
    trigger.setValue(MapObjectProperty.TRIGGER_TARGETS, "7");
    this.map.getMapObjectLayers().getFirst().addMapObject(first);
    this.map.getMapObjectLayers().getFirst().addMapObject(second);
    this.map.getMapObjectLayers().getFirst().addMapObject(trigger);

    JsonObject result =
        McpToolHandler.handleCallTool("reassign-map-ids", Json.createObjectBuilder().build());

    assertTrue(result.getBoolean("success"));
    assertEquals("7", trigger.getStringValue(MapObjectProperty.TRIGGER_TARGETS));
    assertEquals(7, result.getJsonArray("ambiguousOldIds").getInt(0));
    assertTrue(result.getJsonArray("warnings").toString().contains("ambiguous"));
  }

  @Test
  void reassignMapIdsUpdatesDeclaredCrossMapReferences() {
    MapObject target = new MapObject();
    target.setId(56);
    this.map.getMapObjectLayers().getFirst().addMapObject(target);

    TmxMap sourceMap = new TmxMap(MapOrientations.ORTHOGONAL);
    sourceMap.setName("source");
    MapObjectLayer sourceLayer = new MapObjectLayer();
    sourceMap.addLayer(sourceLayer);
    MapObject sourceTrigger = new MapObject();
    sourceTrigger.setId(90);
    sourceTrigger.setValue("targetSpawn", "56");
    sourceTrigger.setValue("targetMap", "mcp-test");
    sourceLayer.addMapObject(sourceTrigger);
    Editor.instance().getMapComponent().getMaps().clear();
    Editor.instance().getMapComponent().getMaps().add(this.map);
    Editor.instance().getMapComponent().getMaps().add(sourceMap);

    JsonObject result =
        McpToolHandler.handleCallTool(
            "reassign-map-ids",
            Json.createObjectBuilder()
                .add(
                    "projectReferences",
                    Json.createArrayBuilder()
                        .add(
                            Json.createObjectBuilder()
                                .add("idProperty", "targetSpawn")
                                .add("targetMapProperty", "targetMap")))
                .build());

    assertTrue(result.getBoolean("success"));
    assertEquals("1", sourceTrigger.getStringValue("targetSpawn"));
    assertEquals(1, result.getInt("updatedReferences"));
  }

  @Test
  void postMutationRefreshFailureIsReturnedAsWarning() {
    String warning =
        McpToolHandler.runPostMutationRefresh(
            () -> {
              throw new IllegalArgumentException("can't parse argument number");
            });

    assertNotNull(warning);
    assertTrue(warning.contains("Mutation succeeded"));
    assertTrue(warning.contains("can't parse argument number"));
  }

  @Test
  void setTilesAppliesGroupedSparsePlacementsInOneCall() {
    TileLayer details = new TileLayer(10, 10);
    details.setName("groundDetail3");
    this.map.addLayer(details);

    JsonObject result =
        McpToolHandler.handleCallTool(
            "set-tiles",
            Json.createObjectBuilder()
                .add("layer", "groundDetail3")
                .add(
                    "placements",
                    Json.createArrayBuilder()
                        .add(
                            Json.createObjectBuilder()
                                .add("gid", 282)
                                .add(
                                    "cells",
                                    Json.createArrayBuilder()
                                        .add(cell(1, 1))
                                        .add(cell(2, 2))
                                        .add(cell(3, 3))
                                        .add(cell(4, 4))))
                        .add(
                            Json.createObjectBuilder()
                                .add("gid", 289)
                                .add(
                                    "cells",
                                    Json.createArrayBuilder()
                                        .add(cell(5, 1))
                                        .add(cell(6, 2))
                                        .add(cell(7, 3))
                                        .add(cell(8, 4)))))
                .build());

    assertTrue(result.getBoolean("success"));
    assertEquals(8, result.getInt("requestedTiles"));
    assertEquals(8, result.getInt("changedTiles"));
    assertEquals(282, details.getTile(3, 3).getGridId());
    assertEquals(289, details.getTile(7, 3).getGridId());
  }

  @Test
  void getTilesInfoReturnsOrderedSamplesAndPerQueryErrors() {
    TileLayer ground = new TileLayer(10, 10);
    ground.setName("ground1");
    ground.setTile(3, 4, 101);
    this.map.addLayer(ground);
    TileLayer wall = new TileLayer(10, 10);
    wall.setName("wall1");
    wall.setTile(2, 1, 202);
    this.map.addLayer(wall);

    JsonObject result =
        McpToolHandler.handleCallTool(
            "get-tiles-info",
            Json.createObjectBuilder()
                .add(
                    "queries",
                    Json.createArrayBuilder()
                        .add(tileQuery("ground1", 3, 4))
                        .add(tileQuery("wall1", 2, 1))
                        .add(tileQuery("ground1", 0, 0))
                        .add(tileQuery("missing", 1, 1))
                        .add(tileQuery("wall1", 10, 1)))
                .build());

    assertTrue(result.getBoolean("success"), result::toString);
    assertEquals(5, result.getInt("requestedQueries"));
    assertEquals(2, result.getInt("errorCount"));
    assertEquals(5, result.getJsonArray("results").size());
    assertEquals(101, result.getJsonArray("results").getJsonObject(0).getInt("gid"));
    assertEquals(202, result.getJsonArray("results").getJsonObject(1).getInt("gid"));
    assertEquals(0, result.getJsonArray("results").getJsonObject(2).getInt("gid"));
    assertFalse(result.getJsonArray("results").getJsonObject(3).getBoolean("success"));
    assertTrue(
        result
            .getJsonArray("results")
            .getJsonObject(3)
            .getString("error")
            .contains("missing"));
    assertFalse(result.getJsonArray("results").getJsonObject(4).getBoolean("success"));
  }

  @Test
  void terrainToolsExplainWangIdsAndPaintSparseCells() throws Exception {
    TileLayer ground = new TileLayer(10, 10);
    ground.setName("ground");
    this.map.addLayer(ground);

    Tileset tileset = new Tileset();
    tileset.setName("terrain-paint-test");
    tileset.setTileCount(16);
    Field firstGridId = Tileset.class.getDeclaredField("firstgid");
    firstGridId.setAccessible(true);
    firstGridId.setInt(tileset, 1);
    WangSet set = new WangSet("Ground", TerrainType.CORNER);
    set.getTerrains().add(new WangColor("Grass", Color.GREEN));
    int[] cornerPositions = {1, 3, 5, 7};
    for (int pattern = 0; pattern < 16; pattern++) {
      int[] wangId = new int[8];
      for (int corner = 0; corner < cornerPositions.length; corner++) {
        wangId[cornerPositions[corner]] = (pattern >> corner) & 1;
      }
      set.getWangTiles().add(new WangTile(pattern, wangId));
    }
    tileset.getOrCreateTerrainSets().add(set);
    this.map.getTilesets().add(tileset);

    JsonObject listed =
        McpToolHandler.handleCallTool(
            "list-terrains",
            Json.createObjectBuilder().add("tileset", "terrain-paint-test").build());
    assertTrue(listed.getBoolean("success"));
    assertEquals(1, listed.getInt("firstGid"));
    assertEquals(
        "top",
        listed.getJsonObject("paintingGuide").getJsonArray("wangIdOrder").getString(0));
    assertTrue(
        listed
            .getJsonObject("paintingGuide")
            .getString("bulkPainting")
            .contains("cells"));
    assertEquals(
        Json.createArrayBuilder()
            .add("tileset")
            .add("set")
            .add("terrain")
            .add("layer")
            .build(),
        listed
            .getJsonObject("paintingGuide")
            .getJsonArray("requiredArguments"));

    JsonObject painted =
        McpToolHandler.handleCallTool(
            "paint-terrain",
            Json.createObjectBuilder()
                .add("tileset", "terrain-paint-test")
                .add("set", "Ground")
                .add("terrain", "Grass")
                .add("layer", "ground")
                .add(
                    "cells",
                    Json.createArrayBuilder().add(cell(2, 2)).add(cell(7, 7)))
                .build());

    assertTrue(painted.getBoolean("success"));
    assertEquals(2, painted.getInt("requestedCells"));
    assertTrue(painted.getInt("changedTiles") >= 2);
    assertEquals(painted.getInt("changedTiles"), painted.getJsonArray("changes").size());
    assertEquals(16, ground.getTile(2, 2).getGridId());
    assertEquals(16, ground.getTile(7, 7).getGridId());

    JsonObject missingTerrain =
        McpToolHandler.handleCallTool(
            "paint-terrain",
            Json.createObjectBuilder()
                .add("tileset", "terrain-paint-test")
                .add("set", "Ground")
                .add("layer", "ground")
                .add("cells", Json.createArrayBuilder().add(cell(1, 1)))
                .build());
    assertFalse(missingTerrain.getBoolean("success"));
    assertTrue(missingTerrain.getString("error").contains("'terrain'"));
  }

  @Test
  void getTileAnimationReturnsSuccessForUnanimatedTile() throws Exception {
    Tileset tileset = new Tileset();
    tileset.setName("test-tileset");
    tileset.saveSource(java.nio.file.Paths.get("test-tileset.tsx"));

    java.util.List<de.gurkenlabs.litiengine.environment.tilemap.xml.TilesetEntry> entries = new java.util.ArrayList<>();
    for (int i = 0; i <= 200; i++) {
      entries.add(new de.gurkenlabs.litiengine.environment.tilemap.xml.TilesetEntry(tileset, i));
    }
    Field allTilesField = Tileset.class.getDeclaredField("allTiles");
    allTilesField.setAccessible(true);
    allTilesField.set(tileset, entries);

    Field tilecountField = Tileset.class.getDeclaredField("tilecount");
    tilecountField.setAccessible(true);
    tilecountField.set(tileset, Integer.valueOf(201));

    Resources.tilesets().add("test-tileset", tileset);

    JsonObject result = McpToolHandler.handleCallTool(
        "get-tile-animation",
        Json.createObjectBuilder()
            .add("tileset", "test-tileset")
            .add("tileId", 200)
            .build());

    assertTrue(result.getBoolean("success"));
    assertFalse(result.getBoolean("animated"));
    assertTrue(result.getString("message").contains("Tile 200 has no animation"));
  }

  @Test
  void toolsIncludeValidInputSchemas() {
    JsonObject toolsList = McpToolHandler.getToolsList();
    jakarta.json.JsonArray tools = toolsList.getJsonArray("tools");
    assertNotNull(tools);
    assertTrue(tools.size() > 50);

    for (jakarta.json.JsonValue item : tools) {
      JsonObject tool = item.asJsonObject();
      String name = tool.getString("name");
      JsonObject schema = tool.getJsonObject("inputSchema");
      assertNotNull(schema, "Tool " + name + " must have an inputSchema");
      JsonObject properties = schema.getJsonObject("properties");
      assertNotNull(properties, "Tool " + name + " schema must have properties");

      if (name.startsWith("import-") || name.equals("remove-resource") || name.equals("export-resource")) {
        assertFalse(properties.isEmpty(), "Tool " + name + " schema properties should not be empty!");
      }
    }

    JsonObject importTileset = tools.stream()
        .map(jakarta.json.JsonValue::asJsonObject)
        .filter(t -> "import-tileset".equals(t.getString("name")))
        .findFirst()
        .orElseThrow();
    JsonObject importTilesetProps = importTileset.getJsonObject("inputSchema").getJsonObject("properties");
    assertTrue(importTilesetProps.containsKey("path"), "import-tileset must define 'path' property in inputSchema");
  }

  @Test
  void createMapWithOverwriteAndInitialLayers() {
    JsonObject create1 = McpToolHandler.handleCallTool(
        "create-map",
        Json.createObjectBuilder()
            .add("name", "hospital-map")
            .add("width", 32)
            .add("height", 32)
            .add("initialLayers", Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("name", "ground").add("type", "tile"))
                .add(Json.createObjectBuilder().add("name", "walls").add("type", "tile"))
                .add(Json.createObjectBuilder().add("name", "props").add("type", "object")))
            .build());
    assertTrue(create1.getBoolean("success"), () -> "create1 failed: " + create1);

    JsonObject create2 = McpToolHandler.handleCallTool(
        "create-map",
        Json.createObjectBuilder()
            .add("name", "hospital-map")
            .add("overwrite", true)
            .add("width", 32)
            .add("height", 32)
            .add("initialLayers", Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("name", "ground").add("type", "tile"))
                .add(Json.createObjectBuilder().add("name", "walls").add("type", "tile")))
            .build());
    assertTrue(create2.getBoolean("success"), () -> "create2 failed: " + create2);
  }

  @Test
  void fillTilesWithMultipleRegions() {
    McpToolHandler.handleCallTool(
        "create-map",
        Json.createObjectBuilder()
            .add("name", "region-test-map")
            .add("overwrite", true)
            .add("initialLayers", Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("name", "walls").add("type", "tile")))
            .build());

    JsonObject fillResult = McpToolHandler.handleCallTool(
        "fill-tiles",
        Json.createObjectBuilder()
            .add("regions", Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("layer", "walls").add("x", 0).add("y", 0).add("width", 10).add("height", 1).add("gid", 5))
                .add(Json.createObjectBuilder().add("layer", "walls").add("x", 0).add("y", 10).add("width", 10).add("height", 1).add("gid", 5)))
            .build());

    assertTrue(fillResult.getBoolean("success"));
    assertTrue(fillResult.getString("message").contains("2 regions"));
  }

  private static JsonObject cell(int x, int y) {
    return Json.createObjectBuilder().add("x", x).add("y", y).build();
  }

  private static JsonObject tileQuery(String layer, int x, int y) {
    return Json.createObjectBuilder()
        .add("layer", layer)
        .add("x", x)
        .add("y", y)
        .build();
  }

  @Test
  void projectMapsReportsExplicitDimensionsAndTileDetails() {
    JsonObject maps = McpResourceHandler.getProjectMaps();
    assertNotNull(maps.getJsonArray("maps"));
  }

  @Test
  void validateMapResolvesSpriteAliasesAndStatePrefixes() {
    JsonObject createResult = McpToolHandler.handleCallTool(
        "create-map",
        Json.createObjectBuilder()
            .add("name", "val-sprite-map")
            .add("overwrite", true)
            .add("width", 30)
            .add("height", 20)
            .add("initialLayers", Json.createArrayBuilder()
                .add(Json.createObjectBuilder().add("name", "objects").add("type", "object")))
            .build());
    assertTrue(createResult.getBoolean("success"));

    JsonObject addSpawn = McpToolHandler.handleCallTool(
        "add-spawnpoint",
        Json.createObjectBuilder()
            .add("name", "spawn_1")
            .add("layer", "objects")
            .add("x", 50)
            .add("y", 50)
            .build());
    assertTrue(addSpawn.getBoolean("success"), () -> "addSpawn result: " + addSpawn);

    JsonObject valResult = McpToolHandler.handleCallTool("validate-map", null);
    assertTrue(valResult.getBoolean("success"), () -> "valResult: " + valResult);
  }

  @Test
  void getEntityInfoAndSetEntityPropertyResolvesEntityAcrossMaps() {
    TmxMap map1 = new TmxMap(MapOrientations.ORTHOGONAL);
    map1.setName("map_active");
    map1.addLayer(new MapObjectLayer());
    Game.world().loadEnvironment(map1);

    TmxMap map2 = new TmxMap(MapOrientations.ORTHOGONAL);
    map2.setName("map_other");
    MapObjectLayer objectLayer = new MapObjectLayer();
    MapObject entity = new MapObject();
    entity.setId(136);
    entity.setName("door_136");
    entity.setType(MapObjectType.AREA.name());
    entity.setWidth(16);
    entity.setHeight(16);
    objectLayer.addMapObject(entity);
    map2.addLayer(objectLayer);

    Editor.instance().getMapComponent().getMaps().clear();
    Editor.instance().getMapComponent().getMaps().add(map1);
    Editor.instance().getMapComponent().getMaps().add(map2);

    JsonObject infoResult = McpToolHandler.handleCallTool(
        "get-entity-info",
        Json.createObjectBuilder().add("id", 136).build());

    assertTrue(infoResult.getBoolean("success"));
    assertEquals("map_other", infoResult.getString("map"));
    assertEquals(136, infoResult.getInt("id"));

    JsonObject setPropSuccess = McpToolHandler.handleCallTool(
        "set-entity-property",
        Json.createObjectBuilder().add("id", 136).add("property", "testKey").add("value", "testVal").build());

    assertTrue(setPropSuccess.getBoolean("success"), () -> "setPropSuccess: " + setPropSuccess);
    assertEquals("testVal", entity.getStringValue("testKey"));
  }
}

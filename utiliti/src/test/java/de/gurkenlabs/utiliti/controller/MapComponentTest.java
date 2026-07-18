package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.xml.GroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;

class MapComponentTest {

  @Test
  void sortedMapsAcceptsImmutableLists() {
    List<?> maps = assertDoesNotThrow(() -> MapComponent.sortedMaps(List.of()));

    assertTrue(maps.isEmpty());
  }

  @Test
  void mapRenameRejectsBlankAndDuplicateNames() {
    MapComponent component = new MapComponent();
    TmxMap first = new TmxMap(MapOrientations.ORTHOGONAL);
    first.setName("first");
    TmxMap second = new TmxMap(MapOrientations.ORTHOGONAL);
    second.setName("second");
    component.getMaps().addAll(List.of(first, second));

    assertTrue(!component.renameMap(first, "  "));
    assertTrue(!component.renameMap(first, "second"));
    assertTrue(component.renameMap(first, " renamed "));
    assertEquals("renamed", first.getName());
  }

  @Test
  void hiddenOrTransparentAncestorSuppressesNestedLayer() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    GroupLayer group = new GroupLayer();
    MapObjectLayer child = new MapObjectLayer();
    group.addLayer(child);
    map.addLayer(group);

    assertTrue(MapComponent.isLayerEffectivelyVisible(map, child));
    group.setVisible(false);
    assertTrue(!MapComponent.isLayerEffectivelyVisible(map, child));
    group.setVisible(true);
    group.setOpacity(0);
    assertTrue(!MapComponent.isLayerEffectivelyVisible(map, child));
  }

  @Test
  void synchronizationRebuildsActiveEntitiesAndInvalidatesInactiveCache() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    try {
      MapComponent component = new MapComponent();
      TmxMap map = mapWithCollisionBox("synchronized");
      Environment original = new Environment(map);
      original.init();
      Game.world().loadEnvironment(original);
      assertEquals(1, original.getEntities().size());

      map.removeLayer(map.getMapObjectLayers().getFirst());
      component.synchronizeEnvironmentEntities(map);

      Environment rebuilt = Game.world().environment();
      assertNotSame(original, rebuilt);
      assertSame(rebuilt, component.getCachedEnvironmentForTest(map));
      assertEquals(0, rebuilt.getEntities().size());

      TmxMap other = mapWithCollisionBox("other");
      Game.world().loadEnvironment(other);
      component.synchronizeEnvironmentEntities(map);

      assertSame(other, Game.world().environment().getMap());
      assertNull(component.getCachedEnvironmentForTest(map));
    } finally {
      Method terminate = Game.class.getDeclaredMethod("terminate");
      terminate.setAccessible(true);
      terminate.invoke(null);
    }
  }

  @Test
  void calculatesPaddedFitForLandscapeMap() {
    float zoom = MapComponent.calculateFitZoom(
      new Dimension(10000, 5000), new Dimension(800, 600), 1);

    assertEquals(0.0736f, zoom, 0.0001f);
  }

  @Test
  void calculatesPaddedFitForPortraitMapAtScaledUi() {
    float zoom = MapComponent.calculateFitZoom(
      new Dimension(500, 1000), new Dimension(800, 600), 1.5f);

    assertEquals(0.504f, zoom, 0.0001f);
  }

  @Test
  void fittedZoomHasSafePositiveFloor() {
    float zoom = MapComponent.calculateFitZoom(
      new Dimension(1000000, 1000000), new Dimension(800, 600), 1);

    assertEquals(0.01f, zoom, 0.0001f);
  }

  @Test
  void mouseEventsBeforeToolRegistrationAreIgnored() {
    ToolManager.reset();
    try {
      MapComponent component = new MapComponent();

      assertDoesNotThrow(() -> component.handleMouseMoved(null));
      assertDoesNotThrow(() -> component.handleMousePressed(null));
      assertDoesNotThrow(() -> component.handleMouseDragged(null));
      assertDoesNotThrow(() -> component.handleMouseReleased(null));
    } finally {
      ToolManager.reset();
    }
  }

  @Test
  void convertsPhysicalCanvasCoordinatesUsingCameraRenderScale() {
    ICamera camera = mock(ICamera.class);
    when(camera.getRenderScale()).thenReturn(2f);
    when(camera.getMapLocation(any(Point2D.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Point2D location = MapComponent.toMapLocation(new Point2D.Double(200, 100), camera);

    assertEquals(100, location.getX());
    assertEquals(50, location.getY());
  }

  @Test
  void choosesRenderableDefaultsForGenericSpriteEntities() {
    Spritesheet prop = mock(Spritesheet.class);
    Spritesheet creature = mock(Spritesheet.class);
    when(prop.getName()).thenReturn("prop-crate-intact");
    when(creature.getName()).thenReturn("lepus-idle");

    assertEquals("crate", MapComponent.getDefaultSpriteName(MapObjectType.PROP, List.of(prop, creature)));
    assertEquals("lepus", MapComponent.getDefaultSpriteName(MapObjectType.CREATURE, List.of(prop, creature)));
  }

  private static TmxMap mapWithCollisionBox(String name) {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    map.setName(name);
    map.setWidth(1);
    map.setHeight(1);
    map.setTileWidth(16);
    map.setTileHeight(16);
    MapObject object = new MapObject();
    object.setId(1);
    object.setType(MapObjectType.COLLISIONBOX.name());
    object.setWidth(8);
    object.setHeight(8);
    MapObjectLayer layer = new MapObjectLayer();
    layer.addMapObject(object);
    map.addLayer(layer);
    return map;
  }
}

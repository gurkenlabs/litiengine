package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
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
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
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
  void mapsMouseSideButtonsToInspectorNavigation() {
    assertEquals(-1, MapComponent.inspectorNavigationDirection(4));
    assertEquals(1, MapComponent.inspectorNavigationDirection(5));
    assertEquals(0, MapComponent.inspectorNavigationDirection(1));
  }

  @Test
  void consumesMouseSideButtonEvents() {
    MapComponent component = new MapComponent();
    MouseEvent press = mock(MouseEvent.class);
    when(press.getButton()).thenReturn(4);
    when(press.getWhen()).thenReturn(123L);
    MouseEvent release = mock(MouseEvent.class);
    when(release.getButton()).thenReturn(4);

    assertTrue(component.handleInspectorNavigationMousePressed(press));
    assertTrue(component.handleInspectorNavigationMouseReleased(release));
    verify(press).consume();
    verify(release).consume();
  }

  @Test
  void onlyPrimaryButtonDragsCanTransformMapObjects() {
    MouseEvent event = mock(MouseEvent.class);
    when(event.getModifiersEx()).thenReturn(InputEvent.BUTTON1_DOWN_MASK);
    assertTrue(MapComponent.isPrimaryButtonDown(event));

    when(event.getModifiersEx()).thenReturn(InputEvent.BUTTON3_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
    assertTrue(!MapComponent.isPrimaryButtonDown(event));
  }

  @Test
  void altArrowDoesNotTransformMapObjects() {
    assertTrue(MapComponent.shouldHandleArrowTransform(0));
    assertTrue(!MapComponent.shouldHandleArrowTransform(InputEvent.ALT_DOWN_MASK));
  }

  @Test
  void findsNestedLayers() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    GroupLayer group = new GroupLayer();
    MapObjectLayer child = new MapObjectLayer();
    group.addLayer(child);
    map.addLayer(group);

    assertTrue(MapComponent.containsLayer(map, child));
  }

  @Test
  void findsOverlappingVisibleMapObjectsAtLocation() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    MapObject first = mapObject(1, 0, 0, 10, 10);
    MapObject second = mapObject(2, 5, 5, 10, 10);
    MapObject outside = mapObject(3, 20, 20, 10, 10);
    MapObjectLayer layer = new MapObjectLayer();
    layer.addMapObject(first);
    layer.addMapObject(second);
    layer.addMapObject(outside);
    map.addLayer(layer);

    assertEquals(List.of(first, second), MapComponent.mapObjectsAt(map, new Point2D.Double(7, 7)));
  }

  @Test
  void doesNotPromoteOverlappingCollisionBoxToEntity() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    MapObject collisionBox = mapObject(1, 0, 0, 100, 100);
    collisionBox.setType(MapObjectType.COLLISIONBOX.name());
    MapObject creature = mapObject(2, 20, 20, 16, 16);
    creature.setType(MapObjectType.CREATURE.name());
    MapObjectLayer layer = new MapObjectLayer();
    layer.addMapObject(collisionBox);
    layer.addMapObject(creature);
    map.addLayer(layer);

    assertSame(collisionBox, MapComponent.resolveParentEntity(collisionBox));
  }

  @Test
  void excludesObjectsOnHiddenLayersAtLocation() {
    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    MapObject mapObject = mapObject(1, 0, 0, 10, 10);
    MapObjectLayer layer = new MapObjectLayer();
    layer.addMapObject(mapObject);
    layer.setVisible(false);
    map.addLayer(layer);

    assertTrue(MapComponent.mapObjectsAt(map, new Point2D.Double(5, 5)).isEmpty());
  }

  @Test
  void ignoresTransparentPixelsWhenFindingSpriteObjects() {
    BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(1, 0, 0xffffffff);
    new Spritesheet(image, "prop-transparent-hit-intact.png", 2, 1);

    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    MapObject prop = mapObject(1, 0, 0, 2, 1);
    prop.setType(MapObjectType.PROP.name());
    prop.setValue(MapObjectProperty.SPRITESHEETNAME, "transparent-hit");
    MapObject collisionBox = mapObject(2, 0, 0, 2, 1);
    collisionBox.setType(MapObjectType.COLLISIONBOX.name());
    MapObjectLayer layer = new MapObjectLayer();
    layer.addMapObject(prop);
    layer.addMapObject(collisionBox);
    map.addLayer(layer);

    assertEquals(
      List.of(collisionBox), MapComponent.mapObjectsAt(map, new Point2D.Double(0.5, 0.5)));
    assertEquals(
      List.of(prop, collisionBox), MapComponent.mapObjectsAt(map, new Point2D.Double(1.5, 0.5)));
  }

  @Test
  void previewHitTestingDoesNotCombinePixelsFromDifferentAnimationFrames() {
    BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(1, 0, 0xffffffff);
    new Spritesheet(image, "prop-multiframe-hit-intact.png", 1, 1);

    TmxMap map = new TmxMap(MapOrientations.ORTHOGONAL);
    MapObject prop = mapObject(1, 0, 0, 1, 1);
    prop.setType(MapObjectType.PROP.name());
    prop.setValue(MapObjectProperty.SPRITESHEETNAME, "multiframe-hit");
    MapObject collisionBox = mapObject(2, 0, 0, 1, 1);
    collisionBox.setType(MapObjectType.COLLISIONBOX.name());
    MapObjectLayer layer = new MapObjectLayer();
    layer.addMapObject(prop);
    layer.addMapObject(collisionBox);
    map.addLayer(layer);

    assertEquals(
      List.of(collisionBox), MapComponent.mapObjectsAt(map, new Point2D.Double(0.5, 0.5)));
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

  private static MapObject mapObject(int id, float x, float y, float width, float height) {
    MapObject mapObject = new MapObject();
    mapObject.setId(id);
    mapObject.setType(MapObjectType.PROP.name());
    mapObject.setX(x);
    mapObject.setY(y);
    mapObject.setWidth(width);
    mapObject.setHeight(height);
    return mapObject;
  }

  @Test
  void remapIdReferencesPreservesExactWhitespace() {
    java.util.Map<Integer, Integer> mapping = java.util.Map.of(10, 100, 20, 200);
    MapComponent.IdReferenceRemap result =
        MapComponent.remapIdReferences("  10 ,  20  , 30 ", mapping, null);
    assertEquals("  100 ,  200  , 30 ", result.value());
    assertEquals(2, result.replacements());
  }

  @Test
  void reloadEnvironmentRebuildsCachedEnvironment() throws Exception {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
    try {
      MapComponent component = new MapComponent();
      TmxMap map = mapWithCollisionBox("reloaded");
      Environment original = new Environment(map);
      original.init();
      Game.world().loadEnvironment(original);

      component.loadEnvironment(map);
      Environment firstCached = component.getCachedEnvironmentForTest(map);

      component.reloadEnvironment();
      Environment reloadedCached = component.getCachedEnvironmentForTest(map);

      assertNotSame(firstCached, reloadedCached);
      assertSame(reloadedCached, Game.world().environment());
    } finally {
      Method terminate = Game.class.getDeclaredMethod("terminate");
      terminate.setAccessible(true);
      terminate.invoke(null);
    }
  }
}

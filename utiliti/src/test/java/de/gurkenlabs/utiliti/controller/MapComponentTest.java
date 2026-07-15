package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.List;
import org.junit.jupiter.api.Test;

class MapComponentTest {

  @Test
  void sortedMapsAcceptsImmutableLists() {
    List<?> maps = assertDoesNotThrow(() -> MapComponent.sortedMaps(List.of()));

    assertTrue(maps.isEmpty());
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
}

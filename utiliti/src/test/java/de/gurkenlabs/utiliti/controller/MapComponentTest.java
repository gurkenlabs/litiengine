package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Dimension;
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
}

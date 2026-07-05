package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MapComponentTest {

  @Test
  void sortedMapsAcceptsImmutableLists() {
    List<?> maps = assertDoesNotThrow(() -> MapComponent.sortedMaps(List.of()));

    assertTrue(maps.isEmpty());
  }
}

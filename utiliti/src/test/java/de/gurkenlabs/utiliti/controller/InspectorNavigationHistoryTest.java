package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import java.util.Set;
import org.junit.jupiter.api.Test;

class InspectorNavigationHistoryTest {
  @Test
  void navigatesAcrossInspectorTargetTypes() {
    IMap map = mock(IMap.class);
    IMapObject object = mock(IMapObject.class);
    ILayer layer = mock(ILayer.class);
    SpritesheetResource sprite = mock(SpritesheetResource.class);
    InspectorNavigationTarget mapTarget = InspectorNavigationTarget.map(map);
    InspectorNavigationTarget objectTarget = InspectorNavigationTarget.object(map, object);
    InspectorNavigationTarget layerTarget = InspectorNavigationTarget.layer(map, layer);
    InspectorNavigationTarget spriteTarget = InspectorNavigationTarget.sprite(map, sprite);
    InspectorNavigationHistory<InspectorNavigationTarget> history =
      new InspectorNavigationHistory<>(mapTarget);
    history.record(objectTarget);
    history.record(layerTarget);
    history.record(spriteTarget);

    assertSame(layerTarget, history.goBack(entry -> true));
    assertSame(objectTarget, history.goBack(entry -> true));
    assertSame(mapTarget, history.goBack(entry -> true));
    assertSame(objectTarget, history.goForward(entry -> true));
    assertSame(layerTarget, history.goForward(entry -> true));
    assertSame(spriteTarget, history.goForward(entry -> true));
  }

  @Test
  void navigatesBackAndForwardIncludingMapProperties() {
    IMapObject first = mock(IMapObject.class);
    IMapObject second = mock(IMapObject.class);
    InspectorNavigationHistory<IMapObject> history = new InspectorNavigationHistory<>(null);
    history.record(first);
    history.record(second);

    assertSame(first, history.goBack(entry -> true));
    assertNull(history.goBack(entry -> true));
    assertSame(first, history.goForward(entry -> true));
    assertSame(second, history.goForward(entry -> true));
  }

  @Test
  void newFocusClearsForwardHistory() {
    IMapObject first = mock(IMapObject.class);
    IMapObject discarded = mock(IMapObject.class);
    IMapObject replacement = mock(IMapObject.class);
    InspectorNavigationHistory<IMapObject> history = new InspectorNavigationHistory<>(null);
    history.record(first);
    history.record(discarded);
    history.goBack(entry -> true);

    history.record(replacement);

    assertFalse(history.canGoForward(entry -> true));
    assertSame(first, history.goBack(entry -> true));
    assertSame(replacement, history.goForward(entry -> true));
  }

  @Test
  void skipsObjectsThatNoLongerExist() {
    IMapObject first = mock(IMapObject.class);
    IMapObject deleted = mock(IMapObject.class);
    IMapObject current = mock(IMapObject.class);
    InspectorNavigationHistory<IMapObject> history = new InspectorNavigationHistory<>(first);
    history.record(deleted);
    history.record(current);
    Set<IMapObject> existing = Set.of(first, current);

    assertTrue(history.canGoBack(entry -> entry == null || existing.contains(entry)));
    assertSame(first, history.goBack(entry -> entry == null || existing.contains(entry)));
  }
}

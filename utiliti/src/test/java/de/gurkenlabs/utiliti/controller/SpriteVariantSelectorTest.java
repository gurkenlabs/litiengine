package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpriteVariantSelectorTest {

  @Test
  void selectBaseCreatureSpriteNamesPrefersMoveOverDeadFallback() {
    List<Spritesheet> sheets = List.of(
      sheet("goblin-dead-down"),
      sheet("goblin-move-down"));

    assertEquals("goblin-move-down", SpriteVariantSelector.selectBaseCreatureSpriteNames(sheets).get("goblin"));
  }

  @Test
  void selectBaseCreatureSpriteNamesPrefersWalkOverDeadFallback() {
    List<Spritesheet> sheets = List.of(
      sheet("zombie11-dead-right"),
      sheet("zombie11-walk-right"));

    assertEquals("zombie11-walk-right", SpriteVariantSelector.selectBaseCreatureSpriteNames(sheets).get("zombie11"));
  }

  @Test
  void selectBasePropSpriteNamesPrefersIntactOverDamagedOrDestroyed() {
    List<Spritesheet> sheets = List.of(
      sheet("prop-barrel1-destroyed"),
      sheet("prop-barrel1-damaged"),
      sheet("prop-barrel1-intact"));

    // Maps base identifier "barrel1" to representative intact sprite "prop-barrel1-intact"
    assertEquals("prop-barrel1-intact", SpriteVariantSelector.selectBasePropSpriteNames(sheets).get("barrel1"));
  }

  @Test
  void selectBasePropSpriteNamesExtractsBaseIdentifierCorrectly() {
    List<Spritesheet> sheets = List.of(
      sheet("prop-door2-closed"),
      sheet("prop-chest-open"));

    var map = SpriteVariantSelector.selectBasePropSpriteNames(sheets);
    assertEquals("prop-door2-closed", map.get("door2"));
    assertEquals("prop-chest-open", map.get("chest"));
  }

  @Test
  void selectCreatureSpriteNameMatchesDirectionAndState() {
    List<Spritesheet> sheets = List.of(
      sheet("zombie9-idle-down"),
      sheet("zombie9-walk-left"),
      sheet("zombie9-walk-right"),
      sheet("zombie9-dead"));

    assertEquals("zombie9-walk-left", SpriteVariantSelector.selectCreatureSpriteName("zombie9", de.gurkenlabs.litiengine.Direction.LEFT, false, sheets));
    assertEquals("zombie9-walk-right", SpriteVariantSelector.selectCreatureSpriteName("zombie9", de.gurkenlabs.litiengine.Direction.RIGHT, false, sheets));
    assertEquals("zombie9-dead", SpriteVariantSelector.selectCreatureSpriteName("zombie9", de.gurkenlabs.litiengine.Direction.LEFT, true, sheets));
  }

  @Test
  void selectPropSpriteNameMatchesState() {
    List<Spritesheet> sheets = List.of(
      sheet("prop-barrel1-intact"),
      sheet("prop-barrel1-damaged"),
      sheet("prop-barrel1-destroyed"));

    assertEquals("prop-barrel1-intact", SpriteVariantSelector.selectPropSpriteName("barrel1", de.gurkenlabs.litiengine.entities.PropState.INTACT, sheets));
    assertEquals("prop-barrel1-damaged", SpriteVariantSelector.selectPropSpriteName("barrel1", de.gurkenlabs.litiengine.entities.PropState.DAMAGED, sheets));
    assertEquals("prop-barrel1-destroyed", SpriteVariantSelector.selectPropSpriteName("barrel1", de.gurkenlabs.litiengine.entities.PropState.DESTROYED, sheets));
  }

  @Test
  void previewRenderingToleratesMissingCreatureSpritesheetName() {
    MapObject mapObject = new MapObject(MapObjectType.CREATURE.name());

    assertNull(SpriteVariantSelector.getPreviewSpritesheet(null, mapObject));
    assertNull(SpriteVariantSelector.getEntityIcon(null, mapObject, 16));
  }

  @Test
  void previewRenderingToleratesMissingPropSpritesheetName() {
    MapObject mapObject = new MapObject(MapObjectType.PROP.name());

    assertNull(SpriteVariantSelector.getPreviewSpritesheet(null, mapObject));
    assertNull(SpriteVariantSelector.getEntityIcon(null, mapObject, 16));
  }

  private static Spritesheet sheet(String name) {
    return new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), name + ".png", 1, 1);
  }
}

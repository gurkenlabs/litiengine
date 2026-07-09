package de.gurkenlabs.utiliti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

  private static Spritesheet sheet(String name) {
    return new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), name + ".png", 1, 1);
  }
}

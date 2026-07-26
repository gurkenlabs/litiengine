package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.jupiter.api.Test;

class PropPanelTest {

  @Test
  void spritesheetClearRemovesCachedPropSpriteItems() {
    Resources.spritesheets().clear();
    new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "prop-crate.png", 1, 1);
    PropPanel panel = new PropPanel();
    panel.bind(null);

    assertEquals(1, panel.getSpriteItemCountForTest());
    Resources.spritesheets().clear();
    assertEquals(0, panel.getSpriteItemCountForTest());
  }

  @Test
  void animationPickerIncludesAllPropStates() {
    List<Spritesheet> sheets = List.of(
        new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "prop-crate-intact.png", 1, 1),
        new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "prop-crate-damaged.png", 1, 1),
        new Spritesheet(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "prop-crate-destroyed.png", 1, 1));

    assertEquals(List.of("prop-crate-damaged", "prop-crate-destroyed", "prop-crate-intact"),
        List.copyOf(PropPanel.getAnimationSpriteNames("crate", sheets).keySet()));

    Resources.spritesheets().remove("prop-crate-intact");
    Resources.spritesheets().remove("prop-crate-damaged");
    Resources.spritesheets().remove("prop-crate-destroyed");
  }
}

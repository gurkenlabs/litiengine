package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
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

  @Test
  void stateVariantsKeepHyphenatedPropFamilyNames() {
    assertEquals(
        "emergency-light-west",
        PropPanel.getIdentifierBySpriteName("prop-emergency-light-west-intact"));
  }

  @Test
  void bindingLegacyFullSpriteReferenceSelectsItsLogicalFamily() {
    Resources.spritesheets().clear();
    new Spritesheet(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
        "prop-crate-intact.png",
        1,
        1);
    new Spritesheet(
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
        "prop-bed3-intact.png",
        1,
        1);
    MapObject mapObject = new MapObject();
    mapObject.setType(MapObjectType.PROP.name());
    mapObject.setValue(MapObjectProperty.SPRITESHEETNAME, "prop-bed3-intact");
    PropPanel panel = new PropPanel();

    panel.bind(mapObject);

    assertEquals("bed3", panel.getSelectedSpriteForTest());
  }
}

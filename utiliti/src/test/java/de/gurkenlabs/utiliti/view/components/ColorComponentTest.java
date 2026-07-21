package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JSlider;
import org.junit.jupiter.api.Test;

class ColorComponentTest {

  @Test
  void alphaSliderUsesPercentageAndUpdatesColorAlpha() {
    ColorComponent component = new ColorComponent(Color.WHITE);
    component.setColor(new Color(10, 20, 30, 120));

    assertEquals(47, component.getAlphaPercentage());
    assertEquals(120, component.getAlpha());

    find(component, JSlider.class).setValue(25);

    assertEquals(25, component.getAlphaPercentage());
    assertEquals(64, component.getColor().getAlpha());
  }

  @Test
  void setColorNotifiesListenersOnce() {
    ColorComponent component = new ColorComponent(Color.WHITE);
    int[] notifications = {0};
    component.addActionListener(_ -> notifications[0]++);

    component.setColor(new Color(10, 20, 30, 120));

    assertEquals(1, notifications[0]);
  }

  private static <T extends Component> T find(Container root, Class<T> type) {
    for (Component component : root.getComponents()) {
      if (type.isInstance(component)) {
        return type.cast(component);
      }
      if (component instanceof Container container) {
        try {
          return find(container, type);
        } catch (java.util.NoSuchElementException ignored) {
          // Continue with the remaining children.
        }
      }
    }
    throw new java.util.NoSuchElementException(type.getSimpleName());
  }
}

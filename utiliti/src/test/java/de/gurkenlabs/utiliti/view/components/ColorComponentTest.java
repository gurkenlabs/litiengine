package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

class ColorComponentTest {

  @Test
  void colorSwatchOpensChooserWithCurrentColorAndAppliesSelection() {
    Color current = new Color(10, 20, 30, 120);
    Color selected = new Color(40, 50, 60, 180);
    AtomicReference<Color> chooserColor = new AtomicReference<>();
    ColorComponent component = new ColorComponent(Color.WHITE, null, color -> {
      chooserColor.set(color);
      return selected;
    });
    component.setColor(current);
    JButton swatch = findSwatchButton(component);

    swatch.doClick();

    assertEquals(current, chooserColor.get());
    assertEquals(selected, component.getColor());
    assertTrue(swatch.isFocusable());
  }

  @Test
  void cancelingColorChooserKeepsCurrentColorAndDoesNotNotifyListeners() {
    Color current = new Color(10, 20, 30, 120);
    ColorComponent component = new ColorComponent(Color.WHITE, null, color -> null);
    component.setColor(current);
    int[] notifications = {0};
    component.addActionListener(_ -> notifications[0]++);

    findSwatchButton(component).doClick();

    assertEquals(current, component.getColor());
    assertEquals(0, notifications[0]);
  }

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
  void alphaSpinnerUpdatesSliderAndNotifiesListenersOnce() {
    ColorComponent component = new ColorComponent(Color.WHITE);
    component.setColor(new Color(10, 20, 30, 255));
    int[] notifications = {0};
    component.addActionListener(_ -> notifications[0]++);

    find(component, JSpinner.class).setValue(25);

    assertEquals(25, find(component, JSlider.class).getValue());
    assertEquals(64, component.getAlpha());
    assertEquals(1, notifications[0]);
  }

  @Test
  void textFieldAppliesEnteredColor() {
    ColorComponent component = new ColorComponent(Color.WHITE);
    JTextField colorField = find(component, JTextField.class);

    colorField.setText("#80102030");
    colorField.postActionEvent();

    assertEquals(new Color(16, 32, 48, 128), component.getColor());
    assertEquals(50, component.getAlphaPercentage());
  }

  @Test
  void setColorNotifiesListenersOnce() {
    ColorComponent component = new ColorComponent(Color.WHITE);
    int[] notifications = {0};
    component.addActionListener(_ -> notifications[0]++);

    component.setColor(new Color(10, 20, 30, 120));

    assertEquals(1, notifications[0]);
  }

  @Test
  void setColorSynchronizesAllControls() {
    ColorComponent component = new ColorComponent(Color.WHITE);
    Color color = new Color(10, 20, 30, 128);

    component.setColor(color);

    assertEquals(color, component.getColor());
    assertEquals("#800a141e", component.getHexColor());
    assertEquals(color, findSwatchButton(component).getBackground());
    assertEquals(50, find(component, JSlider.class).getValue());
    assertEquals(50, find(component, JSpinner.class).getValue());
  }

  @Test
  void removedListenerIsNotNotified() {
    ColorComponent component = new ColorComponent(Color.WHITE);
    int[] notifications = {0};
    ActionListener listener = _ -> notifications[0]++;
    component.addActionListener(listener);
    component.removeActionListener(listener);

    component.setColor(Color.RED);

    assertEquals(0, notifications[0]);
  }

  @Test
  void nullColorDoesNotChangeStateOrNotifyListeners() {
    ColorComponent component = new ColorComponent(Color.WHITE);
    component.setColor(Color.RED);
    int[] notifications = {0};
    component.addActionListener(_ -> notifications[0]++);

    component.setColor(null);

    assertEquals(Color.RED, component.getColor());
    assertEquals(0, notifications[0]);
  }

  @Test
  void separateAlphaFieldKeepsRgbHexAndAlphaPercentageIndependent() {
    ColorComponent component = new ColorComponent(Color.WHITE);
    component.setSeparateAlphaField(true);
    component.setColor(new Color(10, 20, 30, 128));

    assertEquals("#0a141e", component.getHexColor());
    assertEquals(50, component.getAlphaPercentage());
    assertEquals(128, component.getColor().getAlpha());
  }

  @Test
  void customClearActionOverridesDefaultClearBehavior() {
    ColorComponent component = new ColorComponent(Color.WHITE);
    component.setColor(Color.RED);
    int[] clears = {0};
    component.setClearAction(() -> clears[0]++);

    findDeleteButton(component).doClick();

    assertEquals(1, clears[0]);
    assertEquals(Color.RED, component.getColor());
  }

  @Test
  void clearButtonRestoresConfiguredClearColor() {
    Color clearColor = new Color(1, 2, 3, 4);
    ColorComponent component = new ColorComponent(clearColor);
    component.setColor(Color.RED);

    findDeleteButton(component).doClick();

    assertEquals(clearColor, component.getColor());
  }

  @Test
  void componentEnabledStatePropagatesToControls() {
    ColorComponent component = new ColorComponent(Color.WHITE);

    component.setEnabled(false);

    assertFalse(component.isEnabled());
    for (Component child : component.getComponents()) {
      assertFalse(child.isEnabled());
    }

    component.setEnabled(true);

    assertTrue(component.isEnabled());
    for (Component child : component.getComponents()) {
      assertTrue(child.isEnabled());
    }
  }

  @Test
  void eyedropperSelectionPreservesCurrentAlpha() {
    ColorComponent component = new ColorComponent(Color.WHITE);
    component.setColor(new Color(10, 20, 30, 120));

    component.samplePickedColor(() -> new Color(40, 50, 60));

    assertEquals(new Color(40, 50, 60, 120), component.getColor());
  }

  @Test
  void deniedScreenCaptureKeepsCurrentColorAndDoesNotEscapeEventHandler() {
    Color current = new Color(10, 20, 30, 120);
    ColorComponent component = new ColorComponent(Color.WHITE);
    component.setColor(current);
    int[] notifications = {0};
    component.addActionListener(_ -> notifications[0]++);

    assertDoesNotThrow(() -> component.samplePickedColor(() -> {
      throw new SecurityException("Screen capture denied");
    }));

    assertEquals(current, component.getColor());
    assertEquals(0, notifications[0]);
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

  private static JButton findDeleteButton(Container root) {
    for (Component component : root.getComponents()) {
      if (component instanceof JButton button && button.getIcon() != null) {
        String name = button.getAccessibleContext().getAccessibleName();
        if (name != null && name.equals(de.gurkenlabs.litiengine.resources.Resources.strings()
            .get("colorComponent_clearColor"))) {
          return button;
        }
      }
      if (component instanceof Container container) {
        try {
          return findDeleteButton(container);
        } catch (java.util.NoSuchElementException ignored) {
          // Continue with remaining children.
        }
      }
    }
    throw new java.util.NoSuchElementException("delete button");
  }

  private static JButton findSwatchButton(Container root) {
    for (Component component : root.getComponents()) {
      if (component instanceof JButton button && button.getIcon() == null) {
        return button;
      }
    }
    throw new java.util.NoSuchElementException("color swatch button");
  }
}

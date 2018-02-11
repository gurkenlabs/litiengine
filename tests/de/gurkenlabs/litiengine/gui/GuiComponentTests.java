package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class GuiComponentTests {

  @Test
  public void testInitializaion() {
    TestComponent component = new TestComponent(10, 20, 100, 50);

    assertNotNull(component);
    assertEquals(10.0, component.getX(), 0.0001);
    assertEquals(20.0, component.getY(), 0.0001);
    assertEquals(100.0, component.getWidth(), 0.0001);
    assertEquals(50.0, component.getHeight(), 0.0001);

    assertTrue(component.isEnabled());

    assertFalse(component.isVisible());
    assertFalse(component.isHovered());
    assertFalse(component.isSelected());
  }

  @Test
  public void ensureThatDefaultAppearanceIsSet() {
    TestComponent component = new TestComponent(10, 20, 100, 50);

    assertNotNull(component);
    assertNotNull(component.getAppearance());
    assertNotNull(component.getAppearanceDisabled());
    assertNotNull(component.getAppearanceHovered());

    assertEquals(GuiProperties.getDefaultAppearance(), component.getAppearance());
    assertEquals(GuiProperties.getDefaultAppearanceDisabled(), component.getAppearanceDisabled());
    assertEquals(GuiProperties.getDefaultAppearanceHovered(), component.getAppearanceHovered());
  }

  private class TestComponent extends GuiComponent {

    protected TestComponent(double x, double y, double width, double height) {
      super(x, y, width, height);
    }
  }
}

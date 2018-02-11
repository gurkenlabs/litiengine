package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.MouseEvent;

import javax.swing.JLabel;

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

  @Test
  public void testEventRegistration() {
    TestComponent component = new TestComponent(0, 0, 100, 50);
    component.setVisible(true);
    
    final Object pressed = new Object();
    final Object clicked = new Object();
    final Object released = new Object();
    component.onMousePressed(c -> {
      c.getSender().setTag(pressed);
    });
    
    component.onClicked(c -> {
      c.getSender().setTag(clicked);
    });
    
    component.onMouseReleased(c -> {
      c.getSender().setTag(released);
    });

    component.mousePressed(createTestEvent(50, 25));
    assertEquals(pressed, component.getTag());
    
    component.mouseClicked(createTestEvent(50, 25));
    assertEquals(clicked, component.getTag());
    
    component.mouseReleased(createTestEvent(50, 25));
    assertEquals(released, component.getTag());
  }

  private static MouseEvent createTestEvent(int x, int y) {
    return new MouseEvent(new JLabel(), 0, 0, 0, x, y, 0, false);
  }

  private class TestComponent extends GuiComponent {

    protected TestComponent(double x, double y, double width, double height) {
      super(x, y, width, height);
    }
  }
}

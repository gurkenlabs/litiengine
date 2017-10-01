package de.gurkenlabs.litiengine.gui;

import org.junit.Assert;
import org.junit.Test;

public class GuiComponentTests {

  @Test
  public void testInitializaion() {
    TestComponent component = new TestComponent(10, 20, 100, 50);

    Assert.assertNotNull(component);
    Assert.assertEquals(10.0, component.getX(), 0.0001);
    Assert.assertEquals(20.0, component.getY(), 0.0001);
    Assert.assertEquals(100.0, component.getWidth(), 0.0001);
    Assert.assertEquals(50.0, component.getHeight(), 0.0001);
  }

  @Test
  public void ensureThatDefaultAppearanceIsSet() {
    TestComponent component = new TestComponent(10, 20, 100, 50);

    Assert.assertNotNull(component);
    Assert.assertNotNull(component.getAppearance());
    Assert.assertNotNull(component.getAppearanceDisabled());
    Assert.assertNotNull(component.getAppearanceHovered());

    Assert.assertEquals(GuiProperties.getDefaultAppearance(), component.getAppearance());
    Assert.assertEquals(GuiProperties.getDefaultAppearanceDisabled(), component.getAppearanceDisabled());
    Assert.assertEquals(GuiProperties.getDefaultAppearanceHovered(), component.getAppearanceHovered());
  }

  private class TestComponent extends GuiComponent {

    protected TestComponent(double x, double y, double width, double height) {
      super(x, y, width, height);
    }
  }
}

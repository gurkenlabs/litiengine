package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Font;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.tweening.TweenType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
public class GuiComponentTests {

  @BeforeEach
  public void assertOnSwingThread() {
    assertTrue(SwingUtilities.isEventDispatchThread());
  }

  @BeforeAll
  public static void initialize() {
    // init required Game environment
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    // init Keyboard
    Input.InputGameAdapter adapter = new Input.InputGameAdapter();
    adapter.initialized();
  }

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


    final Object pressed = new Object();
    final Object clicked = new Object();
    final Object released = new Object();
    final Object hovered = new Object();
    final Object dragged = new Object();
    final Object entered = new Object();
    final Object moved = new Object();
    component.onMousePressed(c -> {
      c.getSender().setTag(pressed);
    });

    component.onClicked(c -> {
      c.getSender().setTag(clicked);
    });

    component.onMouseReleased(c -> {
      c.getSender().setTag(released);
    });

    component.onHovered(c -> {
      c.getSender().setTag(hovered);
    });

    component.onMouseEnter(c -> {
      c.getSender().setTag(entered);
    });

    component.onMouseDragged(c -> {
      c.getSender().setTag(dragged);
    });

    component.onMouseLeave(c -> {
      c.getSender().setTag(null);
    });

    component.onMouseMoved(c -> {
      c.getSender().setTag(moved);
    });

    component.mousePressed(createTestEvent(50, 25));
    assertNull(component.getTag());

    component.mouseClicked(createTestEvent(50, 25));
    assertNull(component.getTag());

    component.mouseReleased(createTestEvent(50, 25));
    assertNull(component.getTag());

    component.mouseExited(createTestEvent(50, 25));
    assertNull(component.getTag());

    component.mouseEntered(createTestEvent(50, 25));
    assertNull(component.getTag());

    component.mouseDragged(createTestEvent(50, 25));
    assertNull(component.getTag());

    component.mouseMoved(createTestEvent(50, 25));
    assertNotNull(component.getTag());

    component.setVisible(true);

    component.mousePressed(createTestEvent(50, 25));
    assertTrue(component.isPressed());
    assertEquals(pressed, component.getTag());

    component.mouseClicked(createTestEvent(50, 25));
    assertFalse(component.isPressed());
    assertEquals(clicked, component.getTag());

    component.mouseReleased(createTestEvent(50, 25));
    assertFalse(component.isPressed());
    assertEquals(released, component.getTag());

    component.mouseEntered(createTestEvent(50, 25));
    assertTrue(component.isHovered());
    assertEquals(entered, component.getTag());

    component.setHoverSound(component.getHoverSound());
    assertNull(component.getHoverSound());

    component.mouseDragged(createTestEvent(50, 25));

    component.mouseMoved(createTestEvent(50, 25));
    assertFalse(component.isPressed());
    assertTrue(component.isHovered());
    assertEquals(moved, component.getTag());
  }

  @Test
  public void testTweenPOSITION_X() {
    TestComponent component = new TestComponent(0, 0, 100, 50);
    component.getTweenValues(TweenType.POSITION_X);
    component.setTweenValues(TweenType.POSITION_X, new float[]{2, 2, 1, 1, 1, 2});
    assertEquals(2, component.getX());
  }

  @Test
  public void testTweenPOSITION_Y() {
    TestComponent component = new TestComponent(0, 0, 100, 50);
    component.getTweenValues(TweenType.POSITION_Y);
    component.setTweenValues(TweenType.POSITION_Y, new float[] { 2, 2, 1, 1, 1, 2 });
    assertEquals(2,component.getY());
  }

  @Test
  public void testTweenPOSITION_XY(){
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.getTweenValues(TweenType.POSITION_XY);
    component.setTweenValues(TweenType.POSITION_XY, new float[] { 2, 0, 1, 1, 1, 2 });
    assertEquals(2,component.getX());
    assertEquals(0,component.getY());
  }

  @Test
  public void testTweenSIZE_WIDTH(){
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.getTweenValues(TweenType.SIZE_WIDTH);
    component.setTweenValues(TweenType.SIZE_WIDTH, new float[] {10,20,50,100});
    assertEquals(10,component.getWidth());
  }

  @Test
  public void testTweenSIZE_HEIGHT(){
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.getTweenValues(TweenType.SIZE_HEIGHT);
    component.setTweenValues(TweenType.SIZE_HEIGHT, new float[] {10,20,50,100});
    assertEquals(10,component.getHeight());
  }

  @Test
  public void testTweenSIZE_BOTH(){
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.getTweenValues(TweenType.SIZE_BOTH);
    component.setTweenValues(TweenType.SIZE_BOTH, new float[] { 10, 20, 50, 100});
    assertEquals(10,component.getWidth());
    assertEquals(20,component.getHeight());
  }

  @Test
  public void testTweenDANGLE(){
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.getTweenValues(TweenType.ANGLE);
    component.setTweenValues(TweenType.ANGLE, new float[] {10.123f,20.987f});
    assertEquals(10,component.getTextAngle());
  }

  @Test
  public void testTweenFONTSIZE() {
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.setFont(new Font("Times", Font.PLAIN, 12));
    component.getTweenValues(TweenType.FONTSIZE);
    component.setTweenValues(TweenType.FONTSIZE, new float[] {8, 9, 10});
    assertEquals(8,component.getFont().getSize2D());
  }

  @Test
  public void testTweenDefault(){
    TestComponent component = new TestComponent(0, 0, 100, 50);
    component.getTweenValues(TweenType.UNDEFINED);
    component.setTweenValues(TweenType.UNDEFINED, new float[] {});
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

package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.litiengine.tweening.TweenType;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(SwingTestSuite.class)
class GuiComponentTests {

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
  void testInitializaion() {
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
  void ensureThatDefaultAppearanceIsSet() {
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
  void testEventRegistration() {
    TestComponent component = new TestComponent(0, 0, 100, 50);

    final Object pressed = new Object();
    final Object clicked = new Object();
    final Object released = new Object();
    final Object hovered = new Object();
    final Object dragged = new Object();
    final Object entered = new Object();
    final Object moved = new Object();
    component.onMousePressed(
      c -> {
        c.getSender().setTag(pressed);
      });

    component.onClicked(
      c -> {
        c.getSender().setTag(clicked);
      });

    component.onMouseReleased(
      c -> {
        c.getSender().setTag(released);
      });

    component.onHovered(
      c -> {
        c.getSender().setTag(hovered);
      });

    component.onMouseEnter(
      c -> {
        c.getSender().setTag(entered);
      });

    component.onMouseDragged(
      c -> {
        c.getSender().setTag(dragged);
      });

    component.onMouseLeave(
      c -> {
        c.getSender().setTag(null);
      });

    component.onMouseMoved(
      c -> {
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
  void testTweenLOCATION_X() {
    TestComponent component = new TestComponent(0, 0, 100, 50);
    component.setTweenValues(TweenType.LOCATION_X, new float[]{2, 2, 1, 1, 1, 2});
    assertEquals(2, component.getX());
  }

  @Test
  void testTweenLOCATION_Y() {
    TestComponent component = new TestComponent(0, 0, 100, 50);
    component.setTweenValues(TweenType.LOCATION_Y, new float[]{2, 2, 1, 1, 1, 2});
    assertEquals(2, component.getY());
  }

  @Test
  void testTweenLOCATION_XY() {
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.setTweenValues(TweenType.LOCATION_XY, new float[]{2, 0, 1, 1, 1, 2});
    assertEquals(2, component.getX());
    assertEquals(0, component.getY());
  }

  @Test
  void testTweenSIZE_WIDTH() {
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.setTweenValues(TweenType.SIZE_WIDTH, new float[]{10, 20, 50, 100});
    assertEquals(10, component.getWidth());
  }

  @Test
  void testTweenSIZE_HEIGHT() {
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.setTweenValues(TweenType.SIZE_HEIGHT, new float[]{10, 20, 50, 100});
    assertEquals(10, component.getHeight());
  }

  @Test
  void testTweenSIZE_BOTH() {
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.setTweenValues(TweenType.SIZE_BOTH, new float[]{10, 20, 50, 100});
    assertEquals(10, component.getWidth());
    assertEquals(20, component.getHeight());
  }

  @Test
  void testTweenDANGLE() {
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.setTweenValues(TweenType.ANGLE, new float[]{10.123f, 20.987f});
    assertEquals(10, component.getTextAngle());
  }

  @Test
  void testTweenFONTSIZE() {
    TestComponent component = new TestComponent(10, 20, 100, 50);
    component.setFont(new Font("Times", Font.PLAIN, 12));
    component.setTweenValues(TweenType.FONTSIZE, new float[]{8, 9, 10});
    assertEquals(8, component.getFont().getSize2D());
  }

  @Test
  void testTweenDefault() {
    TestComponent component = new TestComponent(0, 0, 100, 50);
    component.setTweenValues(TweenType.UNDEFINED, new float[]{});
  }

  @ParameterizedTest
  @MethodSource("getTweenValuesArguments")
  void testGetTweenValues(TweenType tweenType, float[] expectedValues) {
    // arrange
    TestComponent component = new TestComponent(10, 15, 50, 75);
    component.setFont(new Font("Times", 10, 10));

    // act
    float[] tweenValues = component.getTweenValues(tweenType);

    // assert
    assertArrayEquals(expectedValues, tweenValues);
  }

  @ParameterizedTest
  @MethodSource("getTweenValuesOpacityArguments")
  void testGetTweenValuesOpacity(Color bg1, Color bg2, Color fg, Color border, Color shadow,
    float[] expectedValues) {
    // arrange
    TestComponent component = new TestComponent(10, 15, 50, 75);
    component.getCurrentAppearance().setBackgroundColor1(bg1);
    component.getCurrentAppearance().setBackgroundColor2(bg2);
    component.getCurrentAppearance().setForeColor(fg);
    component.getCurrentAppearance().setBorderColor(border);
    component.setTextShadowColor(shadow);

    // act
    float[] tweenValues = component.getTweenValues(TweenType.OPACITY);

    // assert
    assertArrayEquals(expectedValues, tweenValues);
  }

  @Test
  void testSetTweenValuesOpacity() {
    // arrange
    TestComponent component = new TestComponent(10, 15, 50, 75);
    component.getCurrentAppearance().setBackgroundColor1(Color.RED);
    component.getCurrentAppearance().setBackgroundColor2(Color.GREEN);
    component.getCurrentAppearance().setForeColor(Color.BLUE);
    component.getCurrentAppearance().setBorderColor(Color.YELLOW);
    component.setTextShadowColor(Color.PINK);

    // act
    component.setTweenValues(TweenType.OPACITY, new float[]{128f, 128f, 128f, 128f, 128f});

    // assert
    Appearance appearance = component.getCurrentAppearance();

    assertEquals(new Color(255, 0, 255, 128), appearance.getBackgroundColor1());
    assertEquals(new Color(0, 255, 0, 128), appearance.getBackgroundColor2());
    assertEquals(new Color(0, 0, 0, 128), appearance.getForeColor());
    assertEquals(new Color(255, 255, 255, 128), appearance.getBorderColor());
    assertEquals(new Color(255, 175, 255, 128), component.getTextShadowColor());
  }

  @Test
  void testSetTweenValuesOpacityNull() {
    // arrange
    TestComponent component = new TestComponent(10, 15, 50, 75);
    component.getCurrentAppearance().setBackgroundColor1(null);
    component.getCurrentAppearance().setBackgroundColor2(null);
    component.getCurrentAppearance().setForeColor(null);
    component.getCurrentAppearance().setBorderColor(null);
    component.setTextShadowColor(null);

    // act
    component.setTweenValues(TweenType.OPACITY, new float[]{128f, 128f, 128f, 128f, 128f});

    // assert
    Appearance currentAppearance = component.getCurrentAppearance();

    assertNull(currentAppearance.getBackgroundColor1());
    assertNull(currentAppearance.getBackgroundColor2());
    assertNull(currentAppearance.getForeColor());
    assertNull(currentAppearance.getBorderColor());
    assertNull(component.getTextShadowColor());
  }

  @ParameterizedTest
  @MethodSource("getTextToRenderArguments")
  void testGetTextToRender(int stringWidth, boolean autoLineBreaks, String initialText,
    String expectedText) {
    // arrange
    TestComponent component = new TestComponent(10, 15, 50, 75);
    component.setText(initialText);
    component.setAutomaticLineBreaks(autoLineBreaks);

    FontMetrics fm = mock(FontMetrics.class);
    Graphics2D g = spy(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB).createGraphics());
    when(fm.stringWidth(any(String.class))).thenReturn(stringWidth);
    when(g.getFontMetrics()).thenReturn(fm);

    // act
    String actualText = component.getTextToRender(g);

    // assert
    assertEquals(expectedText, actualText);
  }

  @Test
  void testGetShape() {
    // arrange
    TestComponent component = new TestComponent(10, 15, 50, 75);

    // act
    RectangularShape shape = component.getShape();

    // assert
    assertEquals(10, shape.getX());
    assertEquals(15, shape.getY());
    assertEquals(50, shape.getWidth());
    assertEquals(75, shape.getHeight());
  }

  @Test
  void testGetShapeRadius() {
    // arrange
    TestComponent component = new TestComponent(10, 15, 50, 75);
    component.getCurrentAppearance().setBorderRadius(5f);

    // act
    RoundRectangle2D shape = (RoundRectangle2D) component.getShape();

    // assert
    assertEquals(10, shape.getX());
    assertEquals(15, shape.getY());
    assertEquals(50, shape.getWidth());
    assertEquals(75, shape.getHeight());
    assertEquals(5, shape.getArcHeight());
    assertEquals(5, shape.getArcWidth());
  }

  @SuppressWarnings("unused")
  private static Stream<Arguments> getTextToRenderArguments() {
    return Stream.of(
      Arguments.of(90, false, null, ""),
      Arguments.of(90, false, "", ""),
      Arguments.of(90, false, "test", "t"),
      Arguments.of(20, false, "test", "test"),
      Arguments.of(120, true, "test", "test"));
  }

  @SuppressWarnings("unused")
  private static Stream<Arguments> getTweenValuesArguments() {
    return Stream.of(
      Arguments.of(TweenType.LOCATION_X, new float[]{10f}),
      Arguments.of(TweenType.LOCATION_Y, new float[]{15f}),
      Arguments.of(TweenType.LOCATION_XY, new float[]{10f, 15f}),
      Arguments.of(TweenType.SIZE_WIDTH, new float[]{50f}),
      Arguments.of(TweenType.SIZE_HEIGHT, new float[]{75f}),
      Arguments.of(TweenType.SIZE_BOTH, new float[]{50f, 75f}),
      Arguments.of(TweenType.ANGLE, new float[]{0f}),
      Arguments.of(TweenType.FONTSIZE, new float[]{10f}),
      Arguments.of(TweenType.OPACITY, new float[]{0f, 0f, 255f, 0f, 0f}),
      Arguments.of(TweenType.UNDEFINED, new float[]{}));
  }

  @SuppressWarnings("unused")
  private static Stream<Arguments> getTweenValuesOpacityArguments() {
    return Stream.of(
      Arguments.of(null, null, null, null, null, new float[]{0f, 0f, 0f, 0f, 0f}),
      Arguments.of(Color.RED, Color.GREEN, Color.BLUE, Color.PINK, Color.YELLOW,
        new float[]{255f, 255f, 255f, 255f, 255f}));
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

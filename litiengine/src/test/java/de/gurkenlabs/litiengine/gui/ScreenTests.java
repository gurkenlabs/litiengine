package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.gui.screens.Screen;
import de.gurkenlabs.litiengine.test.GameTestSuite;
import java.awt.Dimension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GameTestSuite.class)
class ScreenTests {

  @BeforeAll
  static void initialize() {
    Game.init(Game.COMMANDLINE_ARG_NOGUI);
  }

  @Test
  void onResolutionChangedFallbackSetsFullResolution() {
    // Components without a reference resolution (e.g. created in no-GUI mode) fall back to
    // setting their dimensions to the full new resolution.
    TestComponent component = new TestComponent(0, 0, 100, 50);
    Dimension newResolution = new Dimension(1920, 1080);

    component.onResolutionChanged(newResolution);

    assertEquals(1920, component.getWidth(), 0.001);
    assertEquals(1080, component.getHeight(), 0.001);
  }

  @Test
  void onResolutionChangedScalesProportionally() {
    // Component at (100, 50) with size 200x100 on an 800x600 reference window.
    TestComponent component = new TestComponent(100, 50, 200, 100);
    component.initRelativeLayout(new Dimension(800, 600));

    // Window resizes to 1600x1200 (2x).
    component.onResolutionChanged(new Dimension(1600, 1200));

    assertEquals(200, component.getX(), 0.001);
    assertEquals(100, component.getY(), 0.001);
    assertEquals(400, component.getWidth(), 0.001);
    assertEquals(200, component.getHeight(), 0.001);
  }

  @Test
  void onResolutionChangedScalesChildrenProportionally() {
    // Parent fills an 800x600 window; child is a 200x100 button at (100, 50).
    TestComponent parent = new TestComponent(0, 0, 800, 600);
    TestComponent child = new TestComponent(100, 50, 200, 100);
    parent.getComponents().add(child);

    Dimension ref = new Dimension(800, 600);
    parent.initRelativeLayout(ref);
    child.initRelativeLayout(ref);

    // Window doubles.
    parent.onResolutionChanged(new Dimension(1600, 1200));

    // Parent fills the new window.
    assertEquals(1600, parent.getWidth(), 0.001);
    assertEquals(1200, parent.getHeight(), 0.001);

    // Child is proportionally scaled.
    assertEquals(200, child.getX(), 0.001);
    assertEquals(100, child.getY(), 0.001);
    assertEquals(400, child.getWidth(), 0.001);
    assertEquals(200, child.getHeight(), 0.001);
  }

  @Test
  void onResolutionChangedPreservesRelativeValuesAcrossMultipleChanges() {
    TestComponent component = new TestComponent(400, 300, 200, 100);
    component.initRelativeLayout(new Dimension(800, 600));

    // First resize to 1600x1200
    component.onResolutionChanged(new Dimension(1600, 1200));
    assertEquals(800, component.getX(), 0.001);
    assertEquals(600, component.getY(), 0.001);
    assertEquals(400, component.getWidth(), 0.001);
    assertEquals(200, component.getHeight(), 0.001);

    // Second resize back to 800x600 — should return to original values.
    component.onResolutionChanged(new Dimension(800, 600));
    assertEquals(400, component.getX(), 0.001);
    assertEquals(300, component.getY(), 0.001);
    assertEquals(200, component.getWidth(), 0.001);
    assertEquals(100, component.getHeight(), 0.001);
  }

  @Test
  void onResolutionChangedPropagatesRecursivelyToChildren() {
    TestComponent parent = new TestComponent(0, 0, 800, 600);
    TrackingComponent child = new TrackingComponent(10, 10, 200, 100);
    parent.getComponents().add(child);
    Dimension newResolution = new Dimension(1920, 1080);

    parent.onResolutionChanged(newResolution);

    assertTrue(child.resolutionChangedCalled);
    assertEquals(newResolution, child.receivedResolution);
  }

  @Test
  void onResolutionChangedUpdatesScreenDimensions() {
    TestScreen screen = new TestScreen("test");
    Dimension newResolution = new Dimension(1920, 1080);

    screen.onResolutionChanged(newResolution);

    assertEquals(1920, screen.getWidth(), 0.001);
    assertEquals(1080, screen.getHeight(), 0.001);
  }

  @Test
  void onResolutionChangedCanBeOverriddenToRescaleChildren() {
    TestScreenWithChild screen = new TestScreenWithChild("test");
    Dimension newResolution = new Dimension(800, 600);

    screen.onResolutionChanged(newResolution);

    assertEquals(800, screen.getWidth(), 0.001);
    assertEquals(600, screen.getHeight(), 0.001);
    assertEquals(newResolution, screen.lastResolution);
  }

  @Test
  void autoScalingIsEnabledByDefault() {
    TestComponent component = new TestComponent(100, 50, 200, 100);
    assertTrue(component.isAutoScaling());
  }

  @Test
  void disabledAutoScalingPreservesAbsoluteValuesOnResolutionChange() {
    TestComponent component = new TestComponent(100, 50, 200, 100);
    component.initRelativeLayout(new Dimension(800, 600));
    component.setAutoScaling(false);

    component.onResolutionChanged(new Dimension(1600, 1200));

    // Position and size are unchanged.
    assertEquals(100, component.getX(), 0.001);
    assertEquals(50, component.getY(), 0.001);
    assertEquals(200, component.getWidth(), 0.001);
    assertEquals(100, component.getHeight(), 0.001);
  }

  @Test
  void disabledAutoScalingStillPropagatesResolutionToChildren() {
    TestComponent parent = new TestComponent(0, 0, 800, 600);
    parent.setAutoScaling(false);

    TrackingComponent child = new TrackingComponent(10, 10, 200, 100);
    parent.getComponents().add(child);

    Dimension newResolution = new Dimension(1920, 1080);
    parent.onResolutionChanged(newResolution);

    // Parent is unchanged.
    assertEquals(800, parent.getWidth(), 0.001);
    assertEquals(600, parent.getHeight(), 0.001);

    // Child still received the event.
    assertTrue(child.resolutionChangedCalled);
    assertEquals(newResolution, child.receivedResolution);
  }

  @Test
  void reEnablingAutoScalingResumesProportionalScaling() {
    TestComponent component = new TestComponent(100, 50, 200, 100);
    component.initRelativeLayout(new Dimension(800, 600));
    component.setAutoScaling(false);

    // No scaling while disabled.
    component.onResolutionChanged(new Dimension(1600, 1200));
    assertEquals(200, component.getWidth(), 0.001);

    // Re-enable and trigger again.
    component.setAutoScaling(true);
    component.onResolutionChanged(new Dimension(1600, 1200));
    assertEquals(400, component.getWidth(), 0.001);
    assertEquals(200, component.getHeight(), 0.001);
  }

  @Test
  void setAutoScalingReturnsFalseAfterDisabling() {
    TestComponent component = new TestComponent(0, 0, 100, 100);
    component.setAutoScaling(false);
    assertFalse(component.isAutoScaling());
  }

  private static class TestComponent extends GuiComponent {
    protected TestComponent(double x, double y, double width, double height) {
      super(x, y, width, height);
    }
  }

  private static class TrackingComponent extends GuiComponent {
    boolean resolutionChangedCalled;
    Dimension receivedResolution;

    protected TrackingComponent(double x, double y, double width, double height) {
      super(x, y, width, height);
    }

    @Override
    public void onResolutionChanged(Dimension resolution) {
      super.onResolutionChanged(resolution);
      this.resolutionChangedCalled = true;
      this.receivedResolution = resolution;
    }
  }

  private static class TestScreen extends Screen {
    protected TestScreen(String name) {
      super(name);
    }
  }

  private static class TestScreenWithChild extends Screen {
    Dimension lastResolution;

    protected TestScreenWithChild(String name) {
      super(name);
    }

    @Override
    public void onResolutionChanged(Dimension newResolution) {
      super.onResolutionChanged(newResolution);
      this.lastResolution = newResolution;
    }
  }
}

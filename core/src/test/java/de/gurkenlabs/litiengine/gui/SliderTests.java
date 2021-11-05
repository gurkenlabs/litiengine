package de.gurkenlabs.litiengine.gui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SliderTests {

  /*
      public SliderTests(double x, double y, double width, double height, float minValue, float maxValue, float stepSize) {
          super(x, y, width, height, minValue, maxValue, stepSize);
      }
  */
  @BeforeAll
  public static void initialize() {
    // init required Game environment
    Game.init(Game.COMMANDLINE_ARG_NOGUI);

    // init Keyboard
    Input.InputGameAdapter adapter = new Input.InputGameAdapter();
    adapter.initialized();
  }

  @Test
  void testSetCurrentValue() {
    HorizontalSlider slider = new HorizontalSlider(0, 0, 150, 300, 0f, 100f, 2f);
    float minValue = slider.getMinValue();
    float maxValue = slider.getMaxValue();

    float newValue = 99f;
    slider.setCurrentValue(newValue);
    assertTrue(slider.getCurrentValue() >= minValue && slider.getCurrentValue() <= maxValue);

    float newValue2 = -10f;
    slider.setCurrentValue(newValue2);
    assertFalse(slider.getCurrentValue() < minValue);

    float newValue3 = 1001f;
    slider.setCurrentValue(newValue3);
    assertFalse(slider.getCurrentValue() > maxValue);
  }
}

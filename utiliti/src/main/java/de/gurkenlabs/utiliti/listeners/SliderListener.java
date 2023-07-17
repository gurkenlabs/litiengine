package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;
import javax.swing.JSlider;

public class SliderListener extends MapObjectPropertyChangeListener {

  public SliderListener(String mapObjectProperty, JSlider slider) {
    super(m -> m.hasCustomProperty(mapObjectProperty)
        || m.getIntValue(mapObjectProperty) != slider.getValue(),
      m -> m.setValue(mapObjectProperty, slider.getValue()));
  }

  public SliderListener(String mapObjectProperty, JSlider slider,
    float factor) {
    super(m -> m.hasCustomProperty(mapObjectProperty)
        || m.getFloatValue(mapObjectProperty) != slider.getValue() * factor,
      m -> m.setValue(mapObjectProperty, slider.getValue() * factor));
  }
}

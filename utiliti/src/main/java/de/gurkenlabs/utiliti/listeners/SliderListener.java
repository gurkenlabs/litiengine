package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;
import javax.swing.JSlider;

public class SliderListener extends MapObjectPropertyChangeListener {

  public SliderListener(PropertyPanel propertyPanel, String mapObjectProperty, JSlider slider) {
    super(propertyPanel,
      m -> m.hasCustomProperty(mapObjectProperty)
        || m.getIntValue(mapObjectProperty) != slider.getValue(),
      m -> m.setValue(mapObjectProperty, slider.getValue()));
  }

  public SliderListener(PropertyPanel propertyPanel, String mapObjectProperty, JSlider slider,
    float factor) {
    super(propertyPanel,
      m -> m.hasCustomProperty(mapObjectProperty)
        || m.getFloatValue(mapObjectProperty) != slider.getValue() * factor,
      m -> m.setValue(mapObjectProperty, slider.getValue() * factor));
  }
}

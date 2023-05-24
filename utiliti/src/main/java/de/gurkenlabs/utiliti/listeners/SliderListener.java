package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import javax.swing.JSlider;

public class SliderListener extends MapObjectPropertyChangeListener {

  public SliderListener(IMapObject mapObject, String mapObjectProperty, JSlider slider) {
    super(mapObject,
      m -> m.hasCustomProperty(mapObjectProperty)
        || m.getIntValue(mapObjectProperty) != slider.getValue(),
      m -> m.setValue(mapObjectProperty, slider.getValue()));
  }

  public SliderListener(IMapObject mapObject, String mapObjectProperty, JSlider slider,
    float factor) {
    super(mapObject,
      m -> m.hasCustomProperty(mapObjectProperty)
        || m.getFloatValue(mapObjectProperty) != slider.getValue() * factor,
      m -> m.setValue(mapObjectProperty, slider.getValue() * factor));
  }
}

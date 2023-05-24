package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import javax.swing.JSpinner;

public class SpinnerListener extends MapObjectPropertyChangeListener {

  public SpinnerListener(IMapObject mapObject, String mapObjectProperty, JSpinner spinner) {
    super(mapObject,
      m -> m.hasCustomProperty(mapObjectProperty)
        || m.getStringValue(mapObjectProperty) == null
        || !m.getStringValue(mapObjectProperty).equals(spinner.getValue().toString()),
      m -> m.setValue(mapObjectProperty, spinner.getValue().toString()));
  }
}

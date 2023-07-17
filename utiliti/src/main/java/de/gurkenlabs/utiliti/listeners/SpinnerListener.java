package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;
import javax.swing.JSpinner;

public class SpinnerListener extends MapObjectPropertyChangeListener {

  public SpinnerListener(String mapObjectProperty, JSpinner spinner) {
    super(m -> m.hasCustomProperty(mapObjectProperty)
        || m.getStringValue(mapObjectProperty) == null
        || !m.getStringValue(mapObjectProperty).equals(spinner.getValue().toString()),
      m -> m.setValue(mapObjectProperty, spinner.getValue().toString()));
  }
}

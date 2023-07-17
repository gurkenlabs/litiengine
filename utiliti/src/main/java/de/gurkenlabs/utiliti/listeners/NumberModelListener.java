package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SpinnerNumberModel;

public class NumberModelListener extends MapObjectPropertyChangeListener {

  private static final Logger log = Logger.getLogger(NumberModelListener.class.getName());

  public NumberModelListener(String mapObjectPropertyName,
    SpinnerNumberModel model) {
    super(m -> {
      log.log(Level.INFO, "Has custom property {0}: {1}", new Object[]{mapObjectPropertyName,
        m.hasCustomProperty(mapObjectPropertyName)});
      log.log(Level.INFO, "Property {0} equals model value {1}: {2}",
        new Object[]{m.getNumber(mapObjectPropertyName),
          model.getNumber(),
          Objects.equals(m.getNumber(mapObjectPropertyName), model.getNumber())});

      return m.hasCustomProperty(mapObjectPropertyName) || !Objects.equals(
        m.getNumber(mapObjectPropertyName), model.getNumber());
    }, m -> m.setValue(mapObjectPropertyName, model.getNumber()));
  }
}

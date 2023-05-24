package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import java.util.Objects;
import javax.swing.SpinnerNumberModel;

public class NumberModelListener extends MapObjectPropertyChangeListener {

  public NumberModelListener(IMapObject mapObject, String mapObjectPropertyName,
    SpinnerNumberModel model) {
    super(mapObject, m -> {
      System.out.printf("Has custom property %s: %b", mapObjectPropertyName,
        m.hasCustomProperty(mapObjectPropertyName));
      System.out.printf("Property %f equals model value %f: %b", m.getNumber(mapObjectPropertyName),
        model.getNumber(), Objects.equals(m.getNumber(mapObjectPropertyName), model.getNumber()));

      return m.hasCustomProperty(mapObjectPropertyName) || !Objects.equals(
        m.getNumber(mapObjectPropertyName), model.getNumber());
    }, m -> m.setValue(mapObjectPropertyName, model.getNumber()));
  }
}

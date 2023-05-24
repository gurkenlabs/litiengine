package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.utiliti.components.Editor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MapObjectPropertyFocusListener extends MapObjectPropertyListener implements
  FocusListener {

  public MapObjectPropertyFocusListener(IMapObject mapObject,
    Predicate<IMapObject> mapObjectStateCheck,
    Consumer<IMapObject> updateAction) {
    super(mapObject, mapObjectStateCheck, updateAction);
  }

  @Override
  public void focusGained(FocusEvent e) {
    applyChanges();
  }

  @Override
  public void focusLost(FocusEvent e) {
    applyChanges();
  }
}

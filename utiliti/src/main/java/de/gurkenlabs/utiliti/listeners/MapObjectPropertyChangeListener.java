package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MapObjectPropertyChangeListener extends MapObjectPropertyListener implements
  ChangeListener {


  public MapObjectPropertyChangeListener(Predicate<IMapObject> mapObjectStateCheck,
    Consumer<IMapObject> updateAction) {
    super(mapObjectStateCheck, updateAction);
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    applyChanges();
  }
}

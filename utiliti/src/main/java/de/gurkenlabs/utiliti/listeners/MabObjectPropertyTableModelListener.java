package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class MabObjectPropertyTableModelListener extends MapObjectPropertyListener implements
  TableModelListener {


  public MabObjectPropertyTableModelListener(IMapObject mapObject,
    Predicate<IMapObject> mapObjectStateCheck,
    Consumer<IMapObject> updateAction) {
    super(mapObject, mapObjectStateCheck, updateAction);
  }


  @Override
  public void tableChanged(TableModelEvent e) {
    applyChanges();
  }
}

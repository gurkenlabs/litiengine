package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MapObjectPropertyItemListener extends MapObjectPropertyListener implements
  ItemListener {


  public MapObjectPropertyItemListener(
    PropertyPanel propertyPanel,
    Predicate<IMapObject> mapObjectStateCheck,
    Consumer<IMapObject> updateAction) {
    super(propertyPanel, mapObjectStateCheck, updateAction);
  }

  @Override
  public void itemStateChanged(ItemEvent arg0) {
    applyChanges();
  }
}

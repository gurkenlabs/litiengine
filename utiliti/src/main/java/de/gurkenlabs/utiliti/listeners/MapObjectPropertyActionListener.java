package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MapObjectPropertyActionListener extends MapObjectPropertyListener implements
  ActionListener {


  public MapObjectPropertyActionListener(
    PropertyPanel propertyPanel,
    Predicate<IMapObject> mapObjectStateCheck,
    Consumer<IMapObject> updateAction) {
    super(propertyPanel, mapObjectStateCheck, updateAction);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    applyChanges();
  }
}

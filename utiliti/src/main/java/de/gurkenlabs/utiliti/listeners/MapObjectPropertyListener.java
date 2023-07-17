package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.swing.UI;
import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class MapObjectPropertyListener {

  private final PropertyPanel propertyPanel;
  private final Predicate<IMapObject> mapObjectStateCheck;
  private final Consumer<IMapObject> updateAction;


  protected MapObjectPropertyListener(
    PropertyPanel propertyPanel,
    Predicate<IMapObject> mapObjectStateCheck,
    Consumer<IMapObject> updateAction) {
    this.propertyPanel = propertyPanel;
    this.mapObjectStateCheck = mapObjectStateCheck;
    this.updateAction = updateAction;
  }

  public PropertyPanel getPropertyPanel() {
    return propertyPanel;
  }

  public Predicate<IMapObject> getMapObjectStateCheck() {
    return mapObjectStateCheck;
  }

  protected Consumer<IMapObject> getUpdateAction() {
    return updateAction;
  }

  protected void applyChanges() {
    if (Game.world().environment() == null
      || getPropertyPanel().getMapObject() == null
      || !getMapObjectStateCheck().test(getPropertyPanel().getMapObject())
      || !Editor.instance().getMapComponent().isFocussing()) {
      return;
    }
    UndoManager.instance().mapObjectChanging(getPropertyPanel().getMapObject());
    getUpdateAction().accept(getPropertyPanel().getMapObject());
    UndoManager.instance().mapObjectChanged(getPropertyPanel().getMapObject());
    updateEnvironment();
  }

  private void updateEnvironment() {
    if (getPropertyPanel().getMapObject() != null) {
      IMapObject obj = getPropertyPanel().getMapObject();
      Game.world().environment().reloadFromMap(obj.getId());
      UI.getEntityController().refresh(obj.getId());
    }
  }

}

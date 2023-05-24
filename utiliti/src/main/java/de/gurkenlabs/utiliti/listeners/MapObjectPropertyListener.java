package de.gurkenlabs.utiliti.listeners;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.swing.UI;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MapObjectPropertyListener {


  private final Predicate<IMapObject> mapObjectStateCheck;

  private final Consumer<IMapObject> updateAction;

  private final IMapObject mapObject;

  protected MapObjectPropertyListener(IMapObject mapObject,
    Predicate<IMapObject> mapObjectStateCheck,
    Consumer<IMapObject> updateAction) {
    this.mapObject = mapObject;
    this.mapObjectStateCheck = mapObjectStateCheck;
    this.updateAction = updateAction;
  }

  protected IMapObject getMapObject() {
    return this.mapObject;
  }

  public Predicate<IMapObject> getMapObjectStateCheck() {
    return mapObjectStateCheck;
  }

  protected Consumer<IMapObject> getUpdateAction() {
    return updateAction;
  }

  protected void applyChanges() {
    if (Game.world().environment() == null
      || getMapObject() == null
      || !getMapObjectStateCheck().test(getMapObject())
      || !Editor.instance().getMapComponent().isFocussing()) {
      return;
    }
    UndoManager.instance().mapObjectChanging(getMapObject());
    getUpdateAction().accept(getMapObject());
    UndoManager.instance().mapObjectChanged(getMapObject());
    updateEnvironment();
  }

  private void updateEnvironment() {
    if (getMapObject() != null) {
      IMapObject obj = getMapObject();
      Game.world().environment().reloadFromMap(obj.getId());
      UI.getEntityController().refresh(obj.getId());
    }
  }

}

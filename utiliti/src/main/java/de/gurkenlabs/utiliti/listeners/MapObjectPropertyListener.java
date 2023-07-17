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


  protected MapObjectPropertyListener(
    Predicate<IMapObject> mapObjectStateCheck,
    Consumer<IMapObject> updateAction) {
    this.mapObjectStateCheck = mapObjectStateCheck;
    this.updateAction = updateAction;
  }


  public Predicate<IMapObject> getMapObjectStateCheck() {
    return mapObjectStateCheck;
  }

  protected Consumer<IMapObject> getUpdateAction() {
    return updateAction;
  }

  protected void applyChanges() {
    if (Game.world().environment() == null
      || Editor.instance().getMapComponent().getFocusedMapObject() == null
      || !getMapObjectStateCheck().test(Editor.instance().getMapComponent().getFocusedMapObject())
    ) {
      return;
    }
    UndoManager.instance()
      .mapObjectChanging(Editor.instance().getMapComponent().getFocusedMapObject());
    getUpdateAction().accept(Editor.instance().getMapComponent().getFocusedMapObject());
    UndoManager.instance()
      .mapObjectChanged(Editor.instance().getMapComponent().getFocusedMapObject());
    updateEnvironment();
  }

  private void updateEnvironment() {
    if (Editor.instance().getMapComponent().getFocusedMapObject() != null) {
      IMapObject obj = Editor.instance().getMapComponent().getFocusedMapObject();
      Game.world().environment().reloadFromMap(obj.getId());
      UI.getEntityController().refresh(obj.getId());
    }
  }

}

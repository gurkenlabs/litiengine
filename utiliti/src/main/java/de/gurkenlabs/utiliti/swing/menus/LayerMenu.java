package de.gurkenlabs.utiliti.swing.menus;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.swing.UI;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public final class LayerMenu extends JMenu {

  public LayerMenu() {
    super(Resources.strings().get("menu_move_to_layer"));
    Game.world().onLoaded(e -> this.updateMenu(e.getMap()));

    UI.getLayerController().onLayersChanged(this::updateMenu);
    Editor.instance().getMapComponent().onSelectionChanged(this::updateMenuItemStates);
  }

  private void updateMenuItemStates(List<IMapObject> mapObjects) {
    this.setEnabled(!mapObjects.isEmpty());

    for (Component item : this.getMenuComponents()) {
      if (item instanceof JMenuItem jmi) {
        jmi.setEnabled(mapObjects.stream().anyMatch(x -> !x.getLayer().getName().equals(jmi.getText())));
      }
    }
  }

  private void updateMenu(IMap map) {
    this.removeAll();
    if (map == null) {
      return;
    }

    ArrayList<IMapObjectLayer> layers = new ArrayList<>(map.getMapObjectLayers());

    // the first layer is the one which is rendered first and thereby
    // technically below all other layers. Reversing the

    // list for the UI reflects this
    Collections.reverse(layers);
    for (IMapObjectLayer layer : layers) {
      JMenuItem item = new JMenuItem(layer.getName());
      item.addActionListener(event -> moveMapObjects(item.getText()));
      this.add(item);
    }

    this.updateMenuItemStates(Editor.instance().getMapComponent().getSelectedMapObjects());
  }

  private void moveMapObjects(String layerName) {
    final Environment env = Game.world().environment();
    if (env == null) {
      return;
    }

    IMapObjectLayer layer = env.getMap().getMapObjectLayer(layerName);
    if (layer == null) {
      return;
    }

    UndoManager.instance().beginOperation();
    for (IMapObject mapObject : Editor.instance().getMapComponent().getSelectedMapObjects()) {
      UndoManager.instance().mapObjectChanging(mapObject);
      layer.addMapObject(mapObject);
      env.reloadFromMap(mapObject.getId());
      UndoManager.instance().mapObjectChanged(mapObject);
    }

    UndoManager.instance().endOperation();

    // rebind to refresh the layer property
    UI.getInspector().bind(Editor.instance().getMapComponent().getFocusedMapObject());

    this.updateMenuItemStates(Editor.instance().getMapComponent().getSelectedMapObjects());
  }
}

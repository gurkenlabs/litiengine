package com.litiengine.utiliti.swing.menus;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.litiengine.Game;
import com.litiengine.environment.Environment;
import com.litiengine.environment.tilemap.IMap;
import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.IMapObjectLayer;
import com.litiengine.resources.Resources;
import com.litiengine.utiliti.components.Editor;
import com.litiengine.utiliti.UndoManager;
import com.litiengine.utiliti.swing.UI;

@SuppressWarnings("serial")
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
      if (item instanceof JMenuItem) {
        JMenuItem menuItem = (JMenuItem) item;
        menuItem.setEnabled(mapObjects.stream().anyMatch(x -> !x.getLayer().getName().equals(menuItem.getText())));
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

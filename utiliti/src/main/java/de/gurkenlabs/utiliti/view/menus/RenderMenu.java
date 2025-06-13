package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.view.components.UI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public final class RenderMenu extends JMenu {
  public RenderMenu() {
    super(Resources.strings().get("menu_rendertype"));

    Game.world().onLoaded(e -> this.setEnabled(false));

    UI.getLayerController()
        .onLayersChanged(
            map -> this.updateMenu(Editor.instance().getMapComponent().getSelectedMapObjects()));
    Editor.instance().getMapComponent().onSelectionChanged(this::updateMenu);
  }

  private void updateMenu(List<IMapObject> selectedMapObjects) {
    this.removeAll();

    // setting the render type is only supported for props, creatures and
    // emitters
    // for other entity types this might make no sense: e.g. lights or shadows
    // which are always rendered at a particular
    // section of the rendering pipeline (regardless of any explicitly set
    // render types).
    if (selectedMapObjects.isEmpty()
        || !selectedMapObjects.stream()
            .allMatch(
                x -> {
                  MapObjectType type = MapObjectType.get(x.getType());
                  return type == MapObjectType.PROP
                      || type == MapObjectType.CREATURE
                      || type == MapObjectType.EMITTER;
                })) {
      this.setEnabled(false);
      return;
    }

    List<RenderType> types = Arrays.asList(RenderType.values());

    // the first type is the one which is rendered first and thereby
    // technically below all other layers. Reversing the
    // list for the UI reflects this
    Collections.reverse(types);

    JMenuItem layerItem = new JMenuItem("Render with layer");
    boolean canRenderWithLayer = selectedMapObjects.stream().anyMatch(x -> x.getLayer() != null);

    layerItem.addActionListener(event -> setRenderWithLayer(selectedMapObjects));
    layerItem.setEnabled(canRenderWithLayer);
    this.add(layerItem);
    this.addSeparator();

    for (RenderType renderType : types) {
      JMenuItem item = new JMenuItem("[" + renderType.getOrder() + "] " + renderType.toString());
      item.addActionListener(event -> setRenderType(selectedMapObjects, renderType));
      this.add(item);
    }

    this.setEnabled(true);
  }

  private static void setRenderWithLayer(List<IMapObject> selectedMapObjects) {
    UndoManager.instance().beginOperation();
    for (IMapObject object : selectedMapObjects) {
      UndoManager.instance().mapObjectChanging(object);
      object.removeProperty(MapObjectProperty.RENDERTYPE);
      object.setValue(MapObjectProperty.RENDERWITHLAYER, true);
      Game.world().environment().reloadFromMap(object.getId());
      UndoManager.instance().mapObjectChanged(object);
    }

    UndoManager.instance().endOperation();

    // rebind to refresh the ui
    UI.getInspector().bind(Editor.instance().getMapComponent().getFocusedMapObject());
  }

  private static void setRenderType(List<IMapObject> selectedMapObjects, RenderType renderType) {
    UndoManager.instance().beginOperation();
    for (IMapObject object : selectedMapObjects) {
      UndoManager.instance().mapObjectChanging(object);

      object.removeProperty(MapObjectProperty.RENDERWITHLAYER);

      // for default value, just clear the property
      if (renderType == RenderType.NORMAL) {
        object.removeProperty(MapObjectProperty.RENDERTYPE);
      } else {
        object.setValue(MapObjectProperty.RENDERTYPE, renderType);
      }

      Game.world().environment().reloadFromMap(object.getId());
      UndoManager.instance().mapObjectChanged(object);
    }

    UndoManager.instance().endOperation();

    // rebind to refresh the ui
    UI.getInspector().bind(Editor.instance().getMapComponent().getFocusedMapObject());
  }
}

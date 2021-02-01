package com.litiengine.utiliti.swing.controllers;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.litiengine.Game;
import com.litiengine.environment.tilemap.IMap;
import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.IMapObjectLayer;
import com.litiengine.environment.tilemap.xml.MapObjectLayer;
import com.litiengine.resources.Resources;
import com.litiengine.util.ColorHelper;
import com.litiengine.utiliti.components.Editor;
import com.litiengine.utiliti.components.LayerController;
import com.litiengine.utiliti.handlers.Transform;
import com.litiengine.utiliti.UndoManager;
import com.litiengine.utiliti.swing.Icons;
import com.litiengine.utiliti.swing.panels.LayerTable;

@SuppressWarnings("serial")
public final class LayerList extends JPanel implements LayerController {
  private static final Dimension BUTTON_SIZE = new Dimension(32, 32);

  private final Map<String, Integer> selectedLayers;
  private final transient List<Consumer<IMap>> layerChangedListeners;

  private final LayerTable layerTable;
  private final JScrollPane scrollPane;

  private final Box layerButtonBox;
  private final JButton buttonAddLayer;
  private final JButton buttonRemoveLayer;
  private final JButton buttonDuplicateLayer;
  private final JButton buttonSetColor;
  private final JButton buttonHideOtherLayers;
  private final JButton buttonLiftLayer;
  private final JButton buttonLowerLayer;
  private final JButton buttonRenameLayer;

  private boolean refreshing;

  public LayerList() {
    this.setName(Resources.strings().get("panel_mapObjectLayers"));
    this.selectedLayers = new ConcurrentHashMap<>();
    this.layerChangedListeners = new CopyOnWriteArrayList<>();

    this.setMinimumSize(new Dimension(150, 0));
    this.setMaximumSize(new Dimension(0, 250));
    this.layerTable = new LayerTable();
    this.scrollPane = new JScrollPane();
    this.scrollPane.setViewportBorder(null);
    this.scrollPane.setViewportView(this.layerTable);
    this.setMaximumSize(new Dimension(0, 250));

    this.layerButtonBox = Box.createHorizontalBox();

    this.add(layerButtonBox);
    this.add(scrollPane);

    this.buttonAddLayer = createButton(Icons.ADD, (map, selectedLayer) -> {
      MapObjectLayer layer = new MapObjectLayer();
      layer.setName("new layer");
      int selIndex = this.layerTable.getSelectedRow();
      if (selIndex < 0 || selIndex >= map.getMapObjectLayers().size()) {
        map.addLayer(layer);
      } else {
        map.addLayer(getAbsoluteIndex(map, this.layerTable.getSelectedRow()), layer);
      }
      this.layerTable.bind(map);
      this.layerTable.select(selIndex);
      Transform.updateAnchors();
    }, false);

    this.buttonRemoveLayer = createButton(Icons.DELETE, (map, selectedLayer) -> {
      // we need at least on mapobject layer to work with LITIENGINE entities.
      if (map.getMapObjectLayers().size() <= 1) {
        return;
      }

      if (JOptionPane.showConfirmDialog(null, Resources.strings().get("panel_confirmDeleteLayer"), "", JOptionPane.YES_NO_OPTION) != 0) {
        return;
      }

      Editor.instance().getMapComponent().delete(selectedLayer);
      map.removeLayer(selectedLayer);
      this.layerTable.bind(map);
      Transform.updateAnchors();
    });

    this.buttonDuplicateLayer = createButton(Icons.COPY, (map, selectedLayer) -> {
      IMapObjectLayer copiedLayer = new MapObjectLayer((MapObjectLayer) selectedLayer);
      map.addLayer(getAbsoluteIndex(map, this.layerTable.getSelectedRow()), copiedLayer);
      this.refresh();
      Editor.instance().getMapComponent().add(copiedLayer);
    });

    this.buttonSetColor = createButton(Icons.COLOR, (map, selectedLayer) -> {
      Color newColor = JColorChooser.showDialog(null, Resources.strings().get("panel_selectLayerColor"), selectedLayer.getColor());
      if (newColor == null) {
        return;
      }

      selectedLayer.setColor(ColorHelper.encode(newColor));
    });

    this.buttonRenameLayer = createButton(Icons.RENAME, (map, selectedLayer) -> {
      String newLayerName = JOptionPane.showInputDialog(Resources.strings().get("panel_renameLayer"), selectedLayer.getName());
      if (newLayerName == null) {
        return;
      }

      selectedLayer.setName(newLayerName);
    });

    this.buttonHideOtherLayers = createButton(Icons.HIDEOTHER, (map, selectedLayer) -> {
      for (int i = 0; i < map.getMapObjectLayers().size(); i++) {
        if (i != this.layerTable.getSelectedRow()) {
          map.getMapObjectLayers().get(i).setVisible(false);
        } else if (!map.getMapObjectLayers().get(i).isVisible()) {
          map.getMapObjectLayers().get(i).setVisible(true);
        }
      }

      Transform.updateAnchors();
    }, true);

    this.buttonLiftLayer = createButton(Icons.LIFT, (map, selectedLayer) -> {
      final int selLayerIndex = this.layerTable.getSelectedRow();
      if (selLayerIndex < 0 || selLayerIndex >= map.getMapObjectLayers().size()) {
        return;
      }

      map.removeLayer(selectedLayer);
      map.addLayer(getAbsoluteIndex(map, selLayerIndex), selectedLayer);
      this.layerTable.select(selLayerIndex + 1);
    });

    this.buttonLowerLayer = createButton(Icons.LOWER, (map, selectedLayer) -> {
      int selLayerIndex = this.layerTable.getSelectedRow();
      if (selLayerIndex <= 0 || selLayerIndex >= map.getMapObjectLayers().size()) {
        return;
      }

      map.removeLayer(selectedLayer);
      map.addLayer(getAbsoluteIndex(map, selLayerIndex - 2), selectedLayer);
      this.layerTable.select(selLayerIndex - 1);
    });
    this.layerButtonBox.add(Box.createHorizontalGlue());

    this.layerButtonBox.add(this.buttonAddLayer);
    this.layerButtonBox.add(Box.createHorizontalGlue());

    this.layerButtonBox.add(this.buttonRemoveLayer);
    this.layerButtonBox.add(Box.createHorizontalGlue());

    this.layerButtonBox.add(this.buttonDuplicateLayer);
    this.layerButtonBox.add(Box.createHorizontalGlue());

    this.layerButtonBox.add(this.buttonSetColor);
    this.layerButtonBox.add(Box.createHorizontalGlue());

    this.layerButtonBox.add(this.buttonRenameLayer);
    this.layerButtonBox.add(Box.createHorizontalGlue());

    this.layerButtonBox.add(this.buttonHideOtherLayers);
    this.layerButtonBox.add(Box.createHorizontalGlue());

    this.layerButtonBox.add(this.buttonLiftLayer);
    this.layerButtonBox.add(Box.createHorizontalGlue());

    this.layerButtonBox.add(this.buttonLowerLayer);
    this.layerButtonBox.add(Box.createHorizontalGlue());

    // TODO: enabled states for all commands
    this.layerTable.getSelectionModel().addListSelectionListener(e -> {
      if (Game.world().environment() == null) {
        return;
      }

      IMap map = Game.world().environment().getMap();
      if (map == null || Editor.instance().getMapComponent().isLoading() || this.refreshing) {
        return;
      }

      selectedLayers.put(map.getName(), layerTable.getSelectedRow());
    });

    Editor.instance().getMapComponent().onMapLoaded(map -> {
      if (this.selectedLayers.containsKey(map.getName())) {
        this.layerTable.select(this.selectedLayers.get(map.getName()));
      }
    });
  }

  @Override
  public IMapObjectLayer getCurrentLayer() {
    if (this.layerTable.getModel().getRowCount() == 0) {
      return null;
    }

    IMapObject focus = Editor.instance().getMapComponent().getFocusedMapObject();
    if (focus != null && focus.getLayer() != null) {
      return focus.getLayer();
    } else if (this.layerTable.getSelectedRow() < 0) {
      return Game.world().environment().getMap().getMapObjectLayers().get(0);
    }
    return Game.world().environment().getMap().getMapObjectLayers().get(this.layerTable.getSelectedRow());
  }

  @Override
  public void clear() {
    this.selectedLayers.clear();
  }

  @Override
  public void refresh() {
    this.refreshing = true;
    try {
      IMap map = getCurrentMap();
      if (map == null) {
        return;
      }

      this.layerTable.bind(map);

      if (this.selectedLayers.containsKey(map.getName())) {
        this.layerTable.select(this.selectedLayers.get(map.getName()));
      }
    } finally {
      this.refreshing = false;
    }
  }

  @Override
  public void onLayersChanged(Consumer<IMap> consumer) {
    this.layerChangedListeners.add(consumer);
  }

  private static IMap getCurrentMap() {
    if (Game.world().environment() == null) {
      return null;
    }

    return Game.world().environment().getMap();
  }

  private JButton createButton(Icon icon, BiConsumer<IMap, IMapObjectLayer> consumer) {
    return createButton(icon, consumer, true);
  }

  private JButton createButton(Icon icon, BiConsumer<IMap, IMapObjectLayer> consumer, boolean requireLayer) {
    JButton button = new JButton("");
    button.setPreferredSize(BUTTON_SIZE);
    button.setMinimumSize(BUTTON_SIZE);
    button.setMaximumSize(BUTTON_SIZE);
    button.setIcon(icon);

    button.addActionListener(a -> {
      final IMap currentMap = getCurrentMap();
      if (currentMap == null) {
        return;
      }

      IMapObjectLayer layer = this.getCurrentLayer();
      if (requireLayer && layer == null) {
        return;
      }

      consumer.accept(currentMap, layer);
      this.refresh();
      UndoManager.instance().recordChanges();
      for (Consumer<IMap> c : this.layerChangedListeners) {
        c.accept(getCurrentMap());
      }
    });
    return button;
  }

  private static int getAbsoluteIndex(IMap map, int index) {
    if (map.getMapObjectLayers().size() <= 1) {
      return 0;
    }

    int mapObjectLayerIndex = 0;
    for (int i = 0; i < map.getRenderLayers().size(); i++) {
      if (mapObjectLayerIndex > index) {
        return i;
      }

      if (IMapObjectLayer.class.isAssignableFrom(map.getRenderLayers().get(i).getClass())) {
        mapObjectLayerIndex++;
      }
    }

    return map.getRenderLayers().size();
  }
}
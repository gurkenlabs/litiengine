package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.LayerController;
import de.gurkenlabs.utiliti.controller.Transform;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.CenterIcon;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public final class LayerList extends JPanel implements LayerController {

  private static final Dimension ICON_SIZE = new Dimension(16, 16);

  private final Map<String, Integer> selectedLayers;
  private final transient List<Consumer<IMap>> layerChangedListeners;

  private final LayerTable layerTable;

  private boolean refreshing;

  public LayerList() {
    super(new BorderLayout());
    this.setName(Resources.strings().get("panel_mapObjectLayers"));
    this.setMinimumSize(new Dimension(150, 0));
    this.setMaximumSize(new Dimension(0, 250));
    this.setMaximumSize(new Dimension(0, 250));

    this.selectedLayers = new ConcurrentHashMap<>();
    this.layerChangedListeners = new CopyOnWriteArrayList<>();
    this.layerTable = new LayerTable();

    this.add(createButtonArea(), BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(this.layerTable);
    scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
    this.add(scrollPane, BorderLayout.CENTER);

    // TODO: enabled states for all commands
    this.layerTable
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (Game.world().environment() == null) {
                return;
              }

              IMap map = Game.world().environment().getMap();
              if (map == null
                  || Editor.instance().getMapComponent().isLoading()
                  || this.refreshing) {
                return;
              }

              selectedLayers.put(map.getName(), layerTable.getSelectedRow());
            });

    Editor.instance()
        .getMapComponent()
        .onMapLoaded(
            map -> {
              if (this.selectedLayers.containsKey(map.getName())) {
                this.layerTable.select(this.selectedLayers.get(map.getName()));
              }
            });
  }

  private static IMap getCurrentMap() {
    if (Game.world().environment() == null) {
      return null;
    }

    return Game.world().environment().getMap();
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

  private JComponent createButtonArea() {
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(Box.createHorizontalGlue());

    box.add(createAddLayerButton());
    box.add(createRemoveLayerButton());
    box.add(createDuplicateLayerButton());
    box.add(createSetColorButton());
    box.add(createRenameLayerButton());
    box.add(createHideOtherLayersButton());
    box.add(createMoveLayerUpButton());
    box.add(createMoveLayerDownButton());

    box.add(Box.createHorizontalGlue());
    return box;
  }

  private JButton createAddLayerButton() {
    return createButton(
      Icons.ADD_16,
        (map, selectedLayer) -> {
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
        },
        false);
  }

  private JButton createRemoveLayerButton() {
    return createButton(
        Icons.DELETE,
        (map, selectedLayer) -> {
          // we need at least on mapobject layer to work with LITIENGINE entities.
          if (map.getMapObjectLayers().size() <= 1) {
            return;
          }

          if (JOptionPane.showConfirmDialog(
              null,
              Resources.strings().get("panel_confirmDeleteLayer"),
              "",
              JOptionPane.YES_NO_OPTION) != 0) {
            return;
          }

          Editor.instance().getMapComponent().delete(selectedLayer);
          map.removeLayer(selectedLayer);
          this.layerTable.bind(map);
          Transform.updateAnchors();
        });
  }

  private JButton createDuplicateLayerButton() {
    return createButton(
      Icons.COPY_16,
        (map, selectedLayer) -> {
          IMapObjectLayer copiedLayer = new MapObjectLayer((MapObjectLayer) selectedLayer);
          map.addLayer(getAbsoluteIndex(map, this.layerTable.getSelectedRow()), copiedLayer);
          this.refresh();
          Editor.instance().getMapComponent().add(copiedLayer);
        });
  }

  private JButton createSetColorButton() {
    return createButton(
      Icons.COLOR_16,
        (map, selectedLayer) -> {
          Color newColor =
              JColorChooser.showDialog(
                  null,
                  Resources.strings().get("panel_selectLayerColor"),
                  selectedLayer.getColor());
          if (newColor == null) {
            return;
          }
          selectedLayer.setColor(newColor);
        });
  }

  private JButton createRenameLayerButton() {
    return createButton(
        Icons.RENAME,
        (map, selectedLayer) -> {
          String newLayerName =
              JOptionPane.showInputDialog(
                  Resources.strings().get("panel_renameLayer"), selectedLayer.getName());
          if (newLayerName == null) {
            return;
          }

          selectedLayer.setName(newLayerName);
        });
  }

  private JButton createHideOtherLayersButton() {
    return createButton(
        Icons.HIDEOTHER,
        (map, selectedLayer) -> {
          for (int i = 0; i < map.getMapObjectLayers().size(); i++) {
            if (i != this.layerTable.getSelectedRow()) {
              map.getMapObjectLayers().get(i).setVisible(false);
            } else if (!map.getMapObjectLayers().get(i).isVisible()) {
              map.getMapObjectLayers().get(i).setVisible(true);
            }
          }

          Transform.updateAnchors();
        },
        true);
  }

  private JButton createMoveLayerUpButton() {
    return createButton(
        Icons.LIFT,
        (map, selectedLayer) -> {
          final int selLayerIndex = this.layerTable.getSelectedRow();
          if (selLayerIndex < 0 || selLayerIndex >= map.getMapObjectLayers().size()) {
            return;
          }

          map.removeLayer(selectedLayer);
          map.addLayer(getAbsoluteIndex(map, selLayerIndex), selectedLayer);
          this.layerTable.select(selLayerIndex + 1);
        });
  }

  private JButton createMoveLayerDownButton() {
    return createButton(
        Icons.LOWER,
        (map, selectedLayer) -> {
          int selLayerIndex = this.layerTable.getSelectedRow();
          if (selLayerIndex <= 0 || selLayerIndex >= map.getMapObjectLayers().size()) {
            return;
          }

          map.removeLayer(selectedLayer);
          map.addLayer(getAbsoluteIndex(map, selLayerIndex - 2), selectedLayer);
          this.layerTable.select(selLayerIndex - 1);
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
      return Game.world().environment().getMap().getMapObjectLayers().getFirst();
    }
    return Game.world()
        .environment()
        .getMap()
        .getMapObjectLayers()
        .get(this.layerTable.getSelectedRow());
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
      this.layerTable.bind(map);

      if (map != null && this.selectedLayers.containsKey(map.getName())) {
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

  private JButton createButton(Icon icon, BiConsumer<IMap, IMapObjectLayer> consumer) {
    return createButton(icon, consumer, true);
  }

  private JButton createButton(Icon icon, BiConsumer<IMap, IMapObjectLayer> consumer,
      boolean requireLayer) {
    JButton button = new JButton();
    button.setIcon(new CenterIcon(icon, ICON_SIZE));

    button.addActionListener(
        a -> {
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
}

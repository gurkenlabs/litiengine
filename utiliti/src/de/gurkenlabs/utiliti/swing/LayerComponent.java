package de.gurkenlabs.utiliti.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.components.SubComponent;

@SuppressWarnings("serial")
public final class LayerComponent extends JScrollPane implements SubComponent {
  private static final Dimension BUTTON_SIZE = new Dimension(24, 24);

  private final Map<String, Integer> selectedLayers;
  private final List<Consumer<IMap>> layerChangedListeners;

  private final JCheckBoxList list;
  private final DefaultListModel<JCheckBox> layerModel;

  private final Box layerButtonBox;
  private final JButton buttonAddLayer;
  private final JButton buttonRemoveLayer;
  private final JButton buttonDuplicateLayer;
  private final JButton buttonSetColor;
  private final JButton buttonHideOtherLayers;
  private final JButton buttonLiftLayer;
  private final JButton buttonLowerLayer;
  private final JButton buttonRenameLayer;

  public LayerComponent() {
    this.setName(Resources.strings().get("panel_mapObjectLayers").toUpperCase());
    this.selectedLayers = new ConcurrentHashMap<>();
    this.layerChangedListeners = new CopyOnWriteArrayList<>();

    this.setViewportBorder(null);
    this.setMinimumSize(new Dimension(150, 0));
    this.setMaximumSize(new Dimension(0, 250));
    this.layerModel = new DefaultListModel<>();
    this.list = new JCheckBoxList();
    this.list.setModel(layerModel);
    this.setViewportView(this.list);

    this.setMaximumSize(new Dimension(0, 250));

    this.layerButtonBox = Box.createHorizontalBox();
    this.setColumnHeaderView(layerButtonBox);

    this.buttonAddLayer = createButton(Icons.ADD, (map, selectedLayer) -> {
      MapObjectLayer layer = new MapObjectLayer();
      layer.setName("new layer");
      int selIndex = this.getCurrentLayerIndex();
      if (selIndex < 0 || selIndex >= this.layerModel.size()) {
        map.addLayer(layer);
      } else {
        map.addLayer(this.getAbsoluteIndex(map, this.getCurrentLayerIndex()), layer);
      }

      this.list.setSelectedIndex(selIndex);
      EditorScreen.instance().getMainComponent().updateTransformControls();
    }, false);

    this.buttonRemoveLayer = createButton(Icons.DELETE, (map, selectedLayer) -> {
      // we need at least on mapobject layer to work with LITIengine entities.
      if (map.getMapObjectLayers().size() <= 1) {
        return;
      }

      if (JOptionPane.showConfirmDialog(null, Resources.strings().get("panel_confirmDeleteLayer"), "", JOptionPane.YES_NO_OPTION) != 0) {
        return;
      }

      EditorScreen.instance().getMainComponent().delete(selectedLayer);
      map.removeLayer(selectedLayer);
      layerModel.remove(this.getCurrentLayerIndex());
      EditorScreen.instance().getMainComponent().updateTransformControls();
    });

    this.buttonDuplicateLayer = createButton(Icons.COPYX16, (map, selectedLayer) -> {
      IMapObjectLayer copiedLayer = new MapObjectLayer((MapObjectLayer) selectedLayer);
      map.addLayer(this.getAbsoluteIndex(map, this.getCurrentLayerIndex()), copiedLayer);
      this.refresh();
      EditorScreen.instance().getMainComponent().add(copiedLayer);
    });

    this.buttonSetColor = createButton(Icons.COLORX16, (map, selectedLayer) -> {
      Color newColor = JColorChooser.showDialog(null, Resources.strings().get("panel_selectLayerColor"), selectedLayer.getColor());
      if (newColor == null) {
        return;
      }

      selectedLayer.setColor(ColorHelper.encode(newColor));
    });

    this.buttonRenameLayer = createButton(Icons.RENAMEX16, (map, selectedLayer) -> {
      String newLayerName = JOptionPane.showInputDialog(Resources.strings().get("panel_renameLayer"), selectedLayer.getName());
      if (newLayerName == null) {
        return;
      }

      selectedLayer.setName(newLayerName);
    });

    this.buttonHideOtherLayers = createButton(Icons.HIDEOTHER, (map, selectedLayer) -> {
      for (int i = 0; i < map.getMapObjectLayers().size(); i++) {
        if (i != this.getCurrentLayerIndex()) {
          map.getMapObjectLayers().get(i).setVisible(false);
        } else if (!map.getMapObjectLayers().get(i).isVisible()) {
          map.getMapObjectLayers().get(i).setVisible(true);
        }
      }

      EditorScreen.instance().getMainComponent().updateTransformControls();
    }, true);

    this.buttonLiftLayer = createButton(Icons.LIFT, (map, selectedLayer) -> {
      final int selLayerIndex = this.getCurrentLayerIndex();
      if (selLayerIndex < 0 || selLayerIndex >= this.layerModel.getSize()) {
        return;
      }

      map.removeLayer(selectedLayer);
      map.addLayer(this.getAbsoluteIndex(map, selLayerIndex), selectedLayer);
      this.list.setSelectedIndex(selLayerIndex + 1);
    });

    this.buttonLowerLayer = createButton(Icons.LOWER, (map, selectedLayer) -> {
      int selLayerIndex = this.getCurrentLayerIndex();
      if (selLayerIndex <= 0 || selLayerIndex >= this.layerModel.getSize() - 1) {
        return;
      }

      map.removeLayer(selectedLayer);
      map.addLayer(this.getAbsoluteIndex(map, selLayerIndex-2), selectedLayer);
      this.list.setSelectedIndex(selLayerIndex - 1);
    });

    this.layerButtonBox.add(this.buttonAddLayer);
    this.layerButtonBox.add(this.buttonRemoveLayer);
    this.layerButtonBox.add(this.buttonDuplicateLayer);
    this.layerButtonBox.add(this.buttonSetColor);
    this.layerButtonBox.add(this.buttonRenameLayer);
    this.layerButtonBox.add(this.buttonHideOtherLayers);
    this.layerButtonBox.add(this.buttonLiftLayer);
    this.layerButtonBox.add(this.buttonLowerLayer);
    this.layerButtonBox.add(Box.createHorizontalGlue());

    // TODO: enabled states for all commands

    EditorScreen.instance().getMainComponent().onMapLoading(map -> {
      if (map == null) {
        return;
      }

      this.selectedLayers.put(map.getName(), this.list.getSelectedIndex());
    });

    EditorScreen.instance().getMainComponent().onMapLoaded(map -> {
      if (this.selectedLayers.containsKey(map.getName())) {
        this.selectLayer(this.selectedLayers.get(map.getName()));
      }
    });
  }

  public IMapObjectLayer getCurrentLayer() {
    JCheckBox current = this.list.getSelectedValue();
    if (current == null) {
      if (this.list.getModel().getSize() == 0) {
        return null;
      }

      IMapObject focus = EditorScreen.instance().getMainComponent().getFocusedMapObject();
      if (focus != null) {
        return focus.getLayer();
      }

      current = this.list.getModel().getElementAt(0);
    }

    Object property = current.getClientProperty("layer");
    if (!(property instanceof IMapObjectLayer)) {
      return null;
    }

    return (IMapObjectLayer) property;
  }

  public void selectLayer(int index) {
    this.list.setSelectedIndex(index);
  }

  public void clear() {
    this.selectedLayers.clear();
  }

  public void refresh() {
    IMap map = getCurrentMap();
    if (map == null) {
      this.layerModel.clear();
      return;
    }

    this.layerModel.clear();

    ArrayList<IMapObjectLayer> layers = new ArrayList<>(map.getMapObjectLayers());

    // the first layer is the one which is rendered first and thereby
    // technically below all other layers. Reversing the
    // list for the UI reflects this
    Collections.reverse(layers);
    for (IMapObjectLayer layer : layers) {
      String layerName = layer.getName();
      int layerSize = layer.getMapObjects().size();
      JCheckBox newBox = new JCheckBox(layerName + " (" + layerSize + ")");
      newBox.setName(map.getName() + "/" + layerName);
      Color layerColor = layer.getColor();
      if (layerColor != null) {
        final String cacheKey = map.getName() + layer.getName() + "#" + Integer.toHexString(layerColor.getRGB());

        BufferedImage newIconImage = Resources.images().get(cacheKey, () -> {
          BufferedImage img = Imaging.getCompatibleImage(10, 10);
          Graphics2D g = (Graphics2D) img.getGraphics();
          g.setColor(layer.getColor());
          g.fillRect(0, 0, 9, 9);
          g.setColor(Color.BLACK);
          g.drawRect(0, 0, 9, 9);
          g.dispose();
          return img;
        });

        newBox.setIcon(new ImageIcon(newIconImage));
      }
      newBox.setSelected(layer.isVisible());
      newBox.putClientProperty("layer", layer);
      newBox.addItemListener(sel -> {
        layer.setVisible(newBox.isSelected());
        UndoManager.instance().recordChanges();
      });
      layerModel.addElement(newBox);
    }

    if (this.selectedLayers.containsKey(map.getName())) {
      this.selectLayer(this.selectedLayers.get(map.getName()));
    }
  }

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

  private int getCurrentLayerIndex() {
    // invert since we display the layers exactly the other way around to
    // reflect the order in which they get rendered
    // -> first layer gets rendered first and thereby below all others
    return this.list.getModel().getSize() - 1 - this.list.getSelectedIndex();
  }
  
  private int getAbsoluteIndex(IMap map, int index) {
    if(map.getMapObjectLayers().size() <= 1) {
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
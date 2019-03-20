package de.gurkenlabs.utiliti.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.MapArea;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.EditorScreen;

@SuppressWarnings("serial")
public class MapSelectionPanel extends JSplitPane {
  private final JList<String> mapList;
  private final java.util.Map<String, java.util.Map<String, Boolean>> layerVisibility;
  private final JCheckBoxList listObjectLayers;
  private final DefaultListModel<String> model;
  private final DefaultListModel<JCheckBox> layerModel;
  private final JScrollPane mapScrollPane;
  private final JScrollPane layerScrollPane;
  private final JScrollPane entityScrollPane;
  private final JPopupMenu popupMenu;
  private final JMenuItem mntmExportMap;
  private final JMenuItem mntmDeleteMap;

  private final JPanel entityPanel;
  private final JPanel panel;
  private final JTextField textField;
  private final JButton btnSearch;
  private final JButton btnCollape;
  private final JTree tree;
  private final DefaultTreeModel entitiesTreeModel;
  private final DefaultMutableTreeNode nodeRoot;
  private final DefaultMutableTreeNode nodeProps;
  private final DefaultMutableTreeNode nodeCreatures;
  private final DefaultMutableTreeNode nodeLights;
  private final DefaultMutableTreeNode nodeTriggers;
  private final DefaultMutableTreeNode nodeSpawnpoints;
  private final DefaultMutableTreeNode nodeCollisionBoxes;
  private final DefaultMutableTreeNode nodeMapAreas;
  private final DefaultMutableTreeNode nodeStaticShadows;
  private final DefaultMutableTreeNode nodeEmitter;
  private final DefaultMutableTreeNode[] entityNodes;

  private boolean isFocussing;
  private Box layerButtonBox;
  private JButton buttonAddLayer;
  private JButton buttonRemoveLayer;
  private JButton buttonSetColor;
  private JButton buttonDuplicateLayer;
  private JButton buttonHideOtherLayers;
  private JButton buttonLiftLayer;
  private JButton buttonLowerLayer;
  private Component horizontalGlue4;
  private JButton buttonRenameLayer;

  /**
   * Create the panel.
   */
  public MapSelectionPanel() {
    super(JSplitPane.HORIZONTAL_SPLIT);
    this.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Program.getUserPreferences().setMapPanelSplitter(this.getDividerLocation()));
    if (Program.getUserPreferences().getMapPanelSplitter() != 0) {
      this.setDividerLocation(Program.getUserPreferences().getMapPanelSplitter());
    }

    this.setMaximumSize(new Dimension(0, 250));
    setContinuousLayout(true);
    mapScrollPane = new JScrollPane();
    mapScrollPane.setMinimumSize(new Dimension(80, 0));
    mapScrollPane.setMaximumSize(new Dimension(0, 250));
    this.setLeftComponent(mapScrollPane);

    model = new DefaultListModel<>();
    layerModel = new DefaultListModel<>();
    this.layerVisibility = new ConcurrentHashMap<>();
    this.mapList = new JList<>();
    this.mapList.setModel(model);
    this.mapList.setVisibleRowCount(8);
    this.mapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.mapList.setMaximumSize(new Dimension(0, 250));

    this.mapList.getSelectionModel().addListSelectionListener(e -> {
      if (EditorScreen.instance().isLoading() || EditorScreen.instance().getMapComponent().isLoading()) {
        return;
      }

      if (this.mapList.getSelectedIndex() < EditorScreen.instance().getMapComponent().getMaps().size() && this.mapList.getSelectedIndex() >= 0) {
        Map map = EditorScreen.instance().getMapComponent().getMaps().get(this.mapList.getSelectedIndex());
        if (Game.world().environment() != null && Game.world().environment().getMap().equals(map)) {
          return;
        }

        EditorScreen.instance().getMapComponent().loadEnvironment(map);
      }
    });

    mapScrollPane.setViewportView(this.mapList);

    popupMenu = new JPopupMenu();
    addPopup(this.mapList, popupMenu);

    mntmExportMap = new JMenuItem(Resources.strings().get("hud_exportMap"));
    mntmExportMap.setIcon(Icons.MAP_EXPORT);
    mntmExportMap.addActionListener(a -> EditorScreen.instance().getMapComponent().exportMap());

    popupMenu.add(mntmExportMap);

    mntmDeleteMap = new JMenuItem(Resources.strings().get("hud_deleteMap"));
    mntmDeleteMap.setIcon(Icons.MAP_DELETE);
    mntmDeleteMap.addActionListener(a -> EditorScreen.instance().getMapComponent().deleteMap());
    popupMenu.add(mntmDeleteMap);
    mapScrollPane.setViewportBorder(null);

    layerScrollPane = new JScrollPane();
    layerScrollPane.setViewportBorder(null);
    layerScrollPane.setMinimumSize(new Dimension(150, 0));
    layerScrollPane.setMaximumSize(new Dimension(0, 250));

    JTabbedPane tabPane = new JTabbedPane();

    this.entityPanel = new JPanel();
    this.entityPanel.setLayout(new BorderLayout(0, 0));

    tabPane.addTab(Resources.strings().get("panel_entities"), entityPanel);
    tabPane.add(Resources.strings().get("panel_mapObjectLayers"), layerScrollPane);

    panel = new JPanel();
    entityPanel.add(panel, BorderLayout.NORTH);
    panel.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
    panel.setBackground(Color.WHITE);
    panel.setLayout(new BorderLayout(0, 0));

    btnCollape = new JButton("");
    btnCollape.setOpaque(false);
    btnCollape.setMargin(new Insets(2, 2, 2, 2));
    btnCollape.addActionListener(e -> collapseAll());
    btnCollape.setIcon(Icons.COLLAPSE);
    panel.add(btnCollape, BorderLayout.WEST);

    textField = new JTextField(Resources.strings().get("panel_entities_search_default"));
    textField.setBorder(new EmptyBorder(0, 5, 0, 0));
    textField.setOpaque(false);
    textField.setForeground(Color.GRAY);
    textField.addActionListener(e -> search());
    textField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        if (textField.getText() != null && textField.getText().equals(Resources.strings().get("panel_entities_search_default"))) {
          textField.setText(null);
          textField.setForeground(Color.BLACK);
        }

        textField.selectAll();
        super.focusGained(e);
      }

      @Override
      public void focusLost(FocusEvent e) {
        if (textField.getText() == null || textField.getText().isEmpty()) {
          textField.setText(Resources.strings().get("panel_entities_search_default"));
          textField.setForeground(Color.DARK_GRAY);
        }
        super.focusLost(e);
      }
    });
    panel.add(textField, BorderLayout.CENTER);
    textField.setColumns(10);

    btnSearch = new JButton("");
    btnSearch.setBorderPainted(false);
    btnSearch.setContentAreaFilled(false);
    btnSearch.setOpaque(false);
    btnSearch.setMargin(new Insets(2, 2, 2, 2));
    btnSearch.addActionListener(e -> search());
    btnSearch.setIcon(Icons.SEARCH);
    panel.add(btnSearch, BorderLayout.EAST);

    entityScrollPane = new JScrollPane();
    entityPanel.add(entityScrollPane);
    entityScrollPane.setViewportBorder(null);
    entityScrollPane.setMinimumSize(new Dimension(150, 0));
    entityScrollPane.setMaximumSize(new Dimension(0, 250));

    this.tree = new JTree();
    this.tree.setBorder(null);
    this.tree.setRootVisible(false);
    this.tree.setShowsRootHandles(true);

    this.tree.setCellRenderer(new IconTreeListRenderer());
    this.tree.setMaximumSize(new Dimension(0, 250));

    this.tree.addTreeSelectionListener(e -> {
      this.isFocussing = true;
      try {
        if (e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
          if (node.getUserObject() instanceof IconTreeListItem) {
            IconTreeListItem item = (IconTreeListItem) node.getUserObject();
            if (item.getUserObject() instanceof IEntity) {
              IMapObject obj = Game.world().environment().getMap().getMapObject(((IEntity) item.getUserObject()).getMapId());
              if (obj != null) {
                EditorScreen.instance().getMapComponent().setFocus(obj, true);
              }
            }
          }
        }
      } finally {
        this.isFocussing = false;
      }
    });

    entityScrollPane.setViewportView(this.tree);
    tabPane.setMaximumSize(new Dimension(0, 150));

    this.nodeRoot = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("panel_mapselection_entities"), Icons.FOLDER));
    this.nodeProps = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("panel_mapselection_props"), Icons.PROP));
    this.nodeCreatures = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("panel_mapselection_creatures"), Icons.CREATURE));
    this.nodeLights = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("panel_mapselection_lights"), Icons.LIGHT));
    this.nodeTriggers = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("panel_mapselection_triggers"), Icons.TRIGGER));
    this.nodeSpawnpoints = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("panel_mapselection_spawnpoints"), Icons.SPAWNPOINT));
    this.nodeCollisionBoxes = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("panel_mapselection_collboxes"), Icons.COLLISIONBOX));
    this.nodeMapAreas = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("panel_mapselection_areas"), Icons.MAPAREA));
    this.nodeStaticShadows = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("panel_mapselection_shadow"), Icons.SHADOWBOX));
    this.nodeEmitter = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("panel_mapselection_emitter"), Icons.EMITTER));

    this.nodeRoot.add(this.nodeProps);
    this.nodeRoot.add(this.nodeCreatures);
    this.nodeRoot.add(this.nodeLights);
    this.nodeRoot.add(this.nodeTriggers);
    this.nodeRoot.add(this.nodeSpawnpoints);
    this.nodeRoot.add(this.nodeCollisionBoxes);
    this.nodeRoot.add(this.nodeMapAreas);
    this.nodeRoot.add(this.nodeStaticShadows);
    this.nodeRoot.add(this.nodeEmitter);
    this.entitiesTreeModel = new DefaultTreeModel(this.nodeRoot);

    this.entityNodes = new DefaultMutableTreeNode[] { this.nodeProps, this.nodeCreatures, this.nodeLights, this.nodeTriggers, this.nodeSpawnpoints, this.nodeCollisionBoxes, this.nodeMapAreas, this.nodeStaticShadows, this.nodeEmitter, };
    MouseListener ml = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        if (selRow != -1 && e.getClickCount() == 2) {
          EditorScreen.instance().getMapComponent().centerCameraOnFocus();
        }
      }
    };
    tree.setModel(this.entitiesTreeModel);
    tree.addMouseListener(ml);

    tabPane.setIconAt(0, Icons.CUBE);
    tabPane.setIconAt(1, Icons.LAYER);

    this.setRightComponent(tabPane);

    listObjectLayers = new JCheckBoxList();
    listObjectLayers.setModel(layerModel);
    listObjectLayers.setMaximumSize(new Dimension(0, 250));
    layerScrollPane.setViewportView(listObjectLayers);

    layerButtonBox = Box.createHorizontalBox();
    layerScrollPane.setColumnHeaderView(layerButtonBox);

    buttonAddLayer = new JButton("");
    buttonAddLayer.setPreferredSize(new Dimension(24, 24));
    buttonAddLayer.setMinimumSize(new Dimension(24, 24));
    buttonAddLayer.setMaximumSize(new Dimension(24, 24));
    buttonAddLayer.setIcon(Icons.ADD);

    buttonAddLayer.addActionListener(a -> {
      final IMap currentMap = this.getCurrentMap();
      if (currentMap == null) {
        return;
      }

      MapObjectLayer layer = new MapObjectLayer();
      layer.setName("new layer");
      int selIndex = this.getSelectedLayerIndex();
      if (selIndex < 0 || selIndex >= this.layerModel.size()) {
        currentMap.addLayer(layer);
        this.updateMapLayerControl();
        this.listObjectLayers.setSelectedIndex(selIndex);
      } else {
        currentMap.addLayer(this.getSelectedLayerIndex(), layer);
        this.updateMapLayerControl();
        this.listObjectLayers.setSelectedIndex(selIndex);
      }
      EditorScreen.instance().getMapComponent().updateTransformControls();
      UndoManager.instance().recordChanges();
    });
    layerButtonBox.add(buttonAddLayer);

    buttonRemoveLayer = new JButton("");
    buttonRemoveLayer.setPreferredSize(new Dimension(24, 24));
    buttonRemoveLayer.setMinimumSize(new Dimension(24, 24));
    buttonRemoveLayer.setMaximumSize(new Dimension(24, 24));

    buttonRemoveLayer.setIcon(Icons.DELETE);
    buttonRemoveLayer.addActionListener(a -> {
      final IMap currentMap = this.getCurrentMap();
      if (currentMap == null) {
        return;
      }

      if (this.getSelectedLayerIndex() < 0 || this.getSelectedLayerIndex() >= this.layerModel.size()) {
        return;
      }
      if (JOptionPane.showConfirmDialog(null, Resources.strings().get("panel_confirmDeleteLayer"), "", JOptionPane.YES_NO_OPTION) != 0) {
        return;
      }

      EditorScreen.instance().getMapComponent().delete(currentMap.getMapObjectLayers().get(this.getSelectedLayerIndex()));
      currentMap.removeLayer(this.getSelectedLayerIndex());
      layerModel.remove(this.getSelectedLayerIndex());
      EditorScreen.instance().getMapComponent().updateTransformControls();
      UndoManager.instance().recordChanges();
    });
    layerButtonBox.add(buttonRemoveLayer);

    buttonSetColor = new JButton("");
    buttonSetColor.setPreferredSize(new Dimension(24, 24));
    buttonSetColor.setMinimumSize(new Dimension(24, 24));
    buttonSetColor.setMaximumSize(new Dimension(24, 24));
    buttonSetColor.setIcon(Icons.COLORX16);
    buttonSetColor.addActionListener(a -> {
      if (this.getSelectedLayerIndex() < 0 || this.getSelectedLayerIndex() >= this.layerModel.size()) {
        return;
      }
      IMap currentMap = EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
      Color newColor = JColorChooser.showDialog(null, Resources.strings().get("panel_selectLayerColor"), currentMap.getMapObjectLayers().get(this.getSelectedLayerIndex()).getColor());
      if (newColor == null) {
        return;
      }
      currentMap.getMapObjectLayers().get(this.getSelectedLayerIndex()).setColor(ColorHelper.encode(newColor));
      this.updateMapLayerControl();
      UndoManager.instance().recordChanges();
    });
    layerButtonBox.add(buttonSetColor);

    buttonDuplicateLayer = new JButton("");
    buttonDuplicateLayer.setPreferredSize(new Dimension(24, 24));
    buttonDuplicateLayer.setMinimumSize(new Dimension(24, 24));
    buttonDuplicateLayer.setMaximumSize(new Dimension(24, 24));
    buttonDuplicateLayer.setIcon(Icons.COPYX16);

    buttonDuplicateLayer.addActionListener(a -> {
      if (this.getSelectedLayerIndex() < 0 || this.getSelectedLayerIndex() >= this.layerModel.size()) {
        return;
      }
      IMap currentMap = EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
      IMapObjectLayer copiedLayer = new MapObjectLayer((MapObjectLayer) currentMap.getMapObjectLayers().get(this.getSelectedLayerIndex()));
      currentMap.addLayer(this.getSelectedLayerIndex(), copiedLayer);
      this.updateMapLayerControl();
      EditorScreen.instance().getMapComponent().add(copiedLayer);
      UndoManager.instance().recordChanges();
    });

    buttonRenameLayer = new JButton("");
    buttonRenameLayer.setPreferredSize(new Dimension(24, 24));
    buttonRenameLayer.setMinimumSize(new Dimension(24, 24));
    buttonRenameLayer.setMaximumSize(new Dimension(24, 24));
    buttonRenameLayer.setIcon(Icons.RENAMEX16);

    buttonRenameLayer.addActionListener(a -> {
      if (this.getSelectedLayerIndex() < 0 || this.getSelectedLayerIndex() >= this.layerModel.size()) {
        return;
      }
      IMap currentMap = EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
      IMapObjectLayer selectedLayer = currentMap.getMapObjectLayers().get(this.getSelectedLayerIndex());
      String newLayerName = JOptionPane.showInputDialog(Resources.strings().get("panel_renameLayer"), selectedLayer.getName());
      if (newLayerName == null) {
        return;
      }
      selectedLayer.setName(newLayerName);
      this.updateMapLayerControl();
      UndoManager.instance().recordChanges();
    });

    layerButtonBox.add(buttonRenameLayer);

    layerButtonBox.add(buttonDuplicateLayer);

    buttonHideOtherLayers = new JButton("");
    buttonHideOtherLayers.setPreferredSize(new Dimension(24, 24));
    buttonHideOtherLayers.setMinimumSize(new Dimension(24, 24));
    buttonHideOtherLayers.setMaximumSize(new Dimension(24, 24));
    buttonHideOtherLayers.setIcon(Icons.HIDEOTHER);

    buttonHideOtherLayers.addActionListener(a -> {
      if (this.getSelectedLayerIndex() < 0 || this.getSelectedLayerIndex() >= this.layerModel.size()) {
        return;
      }
      IMap currentMap = EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());

      for (int i = 0; i < currentMap.getMapObjectLayers().size(); i++) {
        if (i != this.getSelectedLayerIndex()) {
          currentMap.getMapObjectLayers().get(i).setVisible(false);
        } else if (!currentMap.getMapObjectLayers().get(i).isVisible()) {
          currentMap.getMapObjectLayers().get(i).setVisible(true);
        }
      }
      this.updateMapLayerControl();
      EditorScreen.instance().getMapComponent().updateTransformControls();
      UndoManager.instance().recordChanges();
    });

    layerButtonBox.add(buttonHideOtherLayers);

    buttonLiftLayer = new JButton("");
    buttonLiftLayer.setPreferredSize(new Dimension(24, 24));
    buttonLiftLayer.setMinimumSize(new Dimension(24, 24));
    buttonLiftLayer.setMaximumSize(new Dimension(24, 24));
    buttonLiftLayer.setIcon(Icons.LIFT);

    buttonLiftLayer.addActionListener(a -> {
      int selLayerIndex = this.getSelectedLayerIndex();
      if (selLayerIndex < 1 || selLayerIndex >= this.layerModel.getSize()) {
        return;
      }
      IMap currentMap = EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
      IMapObjectLayer selectedLayer = currentMap.getMapObjectLayers().get(this.getSelectedLayerIndex());
      currentMap.removeLayer(selLayerIndex);
      currentMap.addLayer(selLayerIndex - 1, selectedLayer);
      this.listObjectLayers.setSelectedIndex(selLayerIndex - 1);
      this.updateMapLayerControl();
      UndoManager.instance().recordChanges();
    });

    layerButtonBox.add(buttonLiftLayer);

    buttonLowerLayer = new JButton("");
    buttonLowerLayer.setPreferredSize(new Dimension(24, 24));
    buttonLowerLayer.setMinimumSize(new Dimension(24, 24));
    buttonLowerLayer.setMaximumSize(new Dimension(24, 24));
    buttonLowerLayer.setIcon(Icons.LOWER);

    buttonLowerLayer.addActionListener(a -> {
      int selLayerIndex = this.getSelectedLayerIndex();
      if (selLayerIndex < 0 || selLayerIndex >= this.layerModel.getSize() - 1) {
        return;
      }
      IMap currentMap = EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
      IMapObjectLayer selectedLayer = currentMap.getMapObjectLayers().get(this.getSelectedLayerIndex());
      currentMap.removeLayer(selLayerIndex);
      currentMap.addLayer(selLayerIndex + 1, selectedLayer);
      this.listObjectLayers.setSelectedIndex(selLayerIndex + 1);
      this.updateMapLayerControl();
      UndoManager.instance().recordChanges();
    });

    layerButtonBox.add(buttonLowerLayer);

    horizontalGlue4 = Box.createHorizontalGlue();
    layerButtonBox.add(horizontalGlue4);

    UndoManager.onMapObjectAdded(manager -> {
      this.updateMapObjectTree();
      this.updateMapLayerControl();
    });

    UndoManager.onMapObjectRemoved(manager -> {
      this.updateMapObjectTree();
      this.updateMapLayerControl();
    });

    UndoManager.onUndoStackChanged(manager -> this.bind(EditorScreen.instance().getMapComponent().getMaps()));
  }

  public synchronized void bind(List<Map> maps) {
    this.bind(maps, false);
  }

  public synchronized void bind(List<Map> maps, boolean clear) {
    if (clear) {
      this.model.clear();
    }

    for (Map map : maps) {
      String name = map.getName();
      if (UndoManager.hasChanges(map)) {
        name += " *";
      }

      // update existing strings
      int indexToReplace = getIndexToReplace(map.getName());
      if (indexToReplace != -1) {
        this.model.set(indexToReplace, name);
      } else {
        // add new maps
        this.model.addElement(name);
      }
    }

    // remove maps that are no longer present
    for (int i = 0; i < this.model.getSize(); i++) {
      final String current = this.model.get(i);
      if (current == null || maps.stream().noneMatch(x -> current.startsWith(x.getName()))) {
        this.model.remove(i);
      }
    }

    mapList.revalidate();
    this.updateMapLayerControl();
    this.updateMapObjectTree();
  }

  public void setSelection(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      mapList.clearSelection();
    } else {
      if (model.contains(mapName)) {
        mapList.setSelectedValue(mapName, true);
      }
    }

    this.updateMapLayerControl();
    this.updateMapObjectTree();
  }

  public boolean isVisibleMapObjectLayer(String name) {

    // Get all the selected items using the indices
    for (int i = 0; i < this.listObjectLayers.getModel().getSize(); i++) {
      if (i >= this.listObjectLayers.getModel().getSize()) {
        return false;
      }
      Object sel = this.listObjectLayers.getModel().getElementAt(i);
      JCheckBox check = (JCheckBox) sel;
      String layerName = getLayerName(check);
      if (layerName != null && layerName.equals(name) && check.isSelected()) {
        return true;
      }
    }
    return false;
  }

  private void updateMapLayerControl() {
    if (mapList.getSelectedIndex() == -1 && this.model.size() > 0) {
      this.mapList.setSelectedIndex(0);
    }

    if (EditorScreen.instance().getMapComponent().getMaps().isEmpty() || mapList.getSelectedIndex() == -1) {
      layerModel.clear();
      return;
    }

    Map map = EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
    int lastSelection = listObjectLayers.getSelectedIndex();
    this.saveLayerVisibility();
    this.layerModel.clear();
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
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
      newBox.addItemListener(sel -> {
        Map currentMap = EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
        int boxIndex = this.layerModel.indexOf(newBox);
        currentMap.getMapObjectLayers().get(boxIndex).setVisible(newBox.isSelected());
        UndoManager.instance().recordChanges();
      });
      layerModel.addElement(newBox);
    }

    int start = 0;
    int end = mapList.getModel().getSize() - 1;
    if (end >= 0) {
      listObjectLayers.setSelectionInterval(start, end);
      this.selectLayer(lastSelection);
    }
  }

  public int getSelectedLayerIndex() {
    return listObjectLayers.getSelectedIndex();
  }

  public IMap getCurrentMap() {
    if (this.mapList.getSelectedIndex() == -1) {
      return null;
    }
    return EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
  }

  public void selectLayer(int index) {
    this.listObjectLayers.setSelectedIndex(index);
  }

  public void focus(final IMapObject mapObject) {
    if (this.isFocussing) {
      return;
    }

    if (mapObject == null) {
      tree.clearSelection();
      return;
    }

    switch (MapObjectType.get(mapObject.getType())) {
    case PROP:
      this.selectById(nodeProps, mapObject.getId());
      break;
    case CREATURE:
      this.selectById(nodeCreatures, mapObject.getId());
      break;
    case TRIGGER:
      this.selectById(nodeTriggers, mapObject.getId());
      break;
    case LIGHTSOURCE:
      this.selectById(nodeLights, mapObject.getId());
      break;
    case SPAWNPOINT:
      this.selectById(nodeSpawnpoints, mapObject.getId());
      break;
    case COLLISIONBOX:
      this.selectById(nodeCollisionBoxes, mapObject.getId());
      break;
    case AREA:
      this.selectById(nodeMapAreas, mapObject.getId());
      break;
    case STATICSHADOW:
      this.selectById(nodeStaticShadows, mapObject.getId());
      break;
    case EMITTER:
      this.selectById(nodeEmitter, mapObject.getId());
      break;
    default:
      return;
    }
  }

  private static String getLayerInfo(JCheckBox layer, int index) {
    if (layer == null) {
      return null;
    }

    String[] layerInfo = layer.getName().split("/");
    if (layerInfo.length < 2) {
      return null;
    }

    return layerInfo[index];
  }

  private void collapseAll() {
    int row = tree.getRowCount() - 1;
    while (row >= 0) {
      tree.collapseRow(row);
      row--;
    }
  }

  private void search() {
    this.btnSearch.requestFocus();
    if (this.textField.getText() == null || this.textField.getText().isEmpty()) {
      return;
    }

    // if typed in name is an integer, try to find by id first
    if (this.textField.getText().matches("-?\\d+")) {
      int id = Integer.parseInt(this.textField.getText());
      if (this.searchById(id)) {
        return;
      }
    }

    this.searchByName(this.textField.getText());
  }

  private boolean searchById(int id) {
    for (DefaultMutableTreeNode node : this.entityNodes) {
      if (this.selectById(node, id)) {
        return true;
      }
    }

    return false;
  }

  private boolean searchByName(String name) {
    for (DefaultMutableTreeNode node : this.entityNodes) {
      if (this.selectByName(node, name)) {
        return true;
      }
    }

    return false;
  }

  private boolean selectById(DefaultMutableTreeNode parent, int mapId) {
    return this.select(parent, e -> e.getMapId() == mapId);
  }

  private boolean selectByName(DefaultMutableTreeNode parent, String name) {
    if (name == null || name.isEmpty()) {
      return false;
    }

    return this.select(parent, e -> e.getName() != null && (e.getName().contains(name) || e.getName().matches(name)));
  }

  private boolean select(DefaultMutableTreeNode parent, Predicate<IEntity> selectionPredicate) {
    if (parent.getChildCount() == 0) {
      return false;
    }

    Enumeration<?> en = parent.depthFirstEnumeration();
    while (en.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
      IEntity ent = null;
      if (node.getUserObject() instanceof IconTreeListItem) {
        IconTreeListItem iconItem = (IconTreeListItem) node.getUserObject();
        if (iconItem.getUserObject() instanceof IEntity) {
          ent = (IEntity) iconItem.getUserObject();
        }
      } else if (node.getUserObject() instanceof IEntity) {
        ent = (IEntity) node.getUserObject();
      }

      if (ent == null) {
        continue;
      }

      if (selectionPredicate.test(ent)) {
        final TreePath newSelection = new TreePath(node.getPath());
        if (this.tree.getSelectionPath() != null && this.tree.getSelectionPath().equals(newSelection)) {
          continue;
        }

        this.tree.setSelectionPath(newSelection);
        TreePath path = this.tree.getSelectionPath();
        if (path == null || !this.tree.isVisible()) {
          return false;
        }

        Rectangle bounds = this.tree.getPathBounds(path);
        if (bounds == null) {
          return false;
        }
        // set the height to the visible height to force the node to top
        bounds.height = this.tree.getVisibleRect().height;
        this.tree.scrollRectToVisible(bounds);
        return true;
      }
    }

    return false;
  }

  private static void addPopup(JList<String> mapList, final JPopupMenu popup) {
    mapList.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          mapList.setSelectedIndex(mapList.locationToIndex(e.getPoint()));
          showMenu(e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          mapList.setSelectedIndex(mapList.locationToIndex(e.getPoint()));
          showMenu(e);
        }
      }

      private void showMenu(MouseEvent e) {
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    });
  }

  private void updateMapObjectTree() {
    this.nodeRoot.setUserObject(new IconTreeListItem((Game.world().environment() == null ? 0 : Game.world().environment().getEntities().size()) + " " + Resources.strings().get("panel_mapselection_entities"), Icons.FOLDER));
    for (DefaultMutableTreeNode node : this.entityNodes) {
      node.removeAllChildren();
    }

    this.nodeLights.setUserObject(new IconTreeListItem((Game.world().environment() == null ? 0 : Game.world().environment().getLightSources().size()) + " " + Resources.strings().get("panel_mapselection_lights"), Icons.LIGHT));
    this.nodeProps.setUserObject(new IconTreeListItem((Game.world().environment() == null ? 0 : Game.world().environment().getProps().size()) + " " + Resources.strings().get("panel_mapselection_props"), Icons.PROP));
    this.nodeCreatures.setUserObject(new IconTreeListItem((Game.world().environment() == null ? 0 : Game.world().environment().getCreatures().size()) + " " + Resources.strings().get("panel_mapselection_creatures"), Icons.CREATURE));
    this.nodeTriggers.setUserObject(new IconTreeListItem((Game.world().environment() == null ? 0 : Game.world().environment().getTriggers().size()) + " " + Resources.strings().get("panel_mapselection_triggers"), Icons.TRIGGER));
    this.nodeSpawnpoints.setUserObject(new IconTreeListItem((Game.world().environment() == null ? 0 : Game.world().environment().getSpawnPoints().size()) + " " + Resources.strings().get("panel_mapselection_spawnpoints"), Icons.SPAWNPOINT));
    this.nodeCollisionBoxes.setUserObject(new IconTreeListItem((Game.world().environment() == null ? 0 : Game.world().environment().getCollisionBoxes().size()) + " " + Resources.strings().get("panel_mapselection_collboxes"), Icons.COLLISIONBOX));
    this.nodeMapAreas.setUserObject(new IconTreeListItem((Game.world().environment() == null ? 0 : Game.world().environment().getAreas().size()) + " " + Resources.strings().get("panel_mapselection_areas"), Icons.MAPAREA));
    this.nodeStaticShadows.setUserObject(new IconTreeListItem((Game.world().environment() == null ? 0 : Game.world().environment().getStaticShadows().size()) + " " + Resources.strings().get("panel_mapselection_shadow"), Icons.SHADOWBOX));
    this.nodeEmitter.setUserObject(new IconTreeListItem((Game.world().environment() == null ? 0 : Game.world().environment().getEmitters().size()) + " " + Resources.strings().get("panel_mapselection_emitter"), Icons.EMITTER));

    if (Game.world().environment() != null) {
      for (LightSource light : Game.world().environment().getLightSources()) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(light));
        this.nodeLights.add(node);
      }

      for (Prop prop : Game.world().environment().getProps()) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(prop));
        this.nodeProps.add(node);
      }

      for (Creature creature : Game.world().environment().getCreatures()) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(creature));
        this.nodeCreatures.add(node);
      }

      for (Trigger trigger : Game.world().environment().getTriggers()) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(trigger));
        this.nodeTriggers.add(node);
      }

      for (Spawnpoint spawn : Game.world().environment().getSpawnPoints()) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(spawn));
        this.nodeSpawnpoints.add(node);
      }

      for (CollisionBox coll : Game.world().environment().getCollisionBoxes()) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(coll));
        this.nodeCollisionBoxes.add(node);
      }

      for (MapArea area : Game.world().environment().getAreas()) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(area));
        this.nodeMapAreas.add(node);
      }

      for (StaticShadow shadow : Game.world().environment().getStaticShadows()) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(shadow));
        this.nodeStaticShadows.add(node);
      }

      for (Emitter emitter : Game.world().environment().getEmitters()) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(emitter));
        this.nodeEmitter.add(node);
      }
    }

    this.entitiesTreeModel.reload();
  }

  private int getIndexToReplace(String mapName) {
    for (int i = 0; i < this.model.getSize(); i++) {
      final String currentName = this.model.get(i);
      if (currentName != null && (currentName.equals(mapName) || currentName.equals(mapName + " *"))) {
        return i;
      }
    }

    return -1;
  }

  private void saveLayerVisibility() {
    if (this.listObjectLayers.getModel().getSize() == 0) {
      return;
    }

    for (int i = 0; i < this.listObjectLayers.getModel().getSize(); i++) {
      JCheckBox layer = this.listObjectLayers.getModel().getElementAt(i);
      if (layer == null) {
        continue;
      }

      this.saveLayerVisibility(getMapName(layer), getLayerName(layer), layer.isSelected());
    }
  }

  private void saveLayerVisibility(String mapName, String layerName, boolean selected) {
    if (!this.layerVisibility.containsKey(mapName)) {
      this.layerVisibility.put(mapName, new HashMap<>());
    }

    this.layerVisibility.get(mapName).put(layerName, selected);
  }

  private static String getMapName(JCheckBox layer) {
    return getLayerInfo(layer, 0);
  }

  private static String getLayerName(JCheckBox layer) {
    return getLayerInfo(layer, 1);
  }
}

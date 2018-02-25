package de.gurkenlabs.utiliti;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import java.util.List;
import java.util.function.Predicate;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
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
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.StaticShadow;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.utiliti.swing.IconTreeListItem;
import de.gurkenlabs.utiliti.swing.IconTreeListRenderer;
import de.gurkenlabs.utiliti.swing.JCheckBoxList;

public class MapSelectionPanel extends JSplitPane {
  private final JList<String> mapList;
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
    mapList = new JList<>();
    mapList.setModel(model);
    mapList.setVisibleRowCount(8);
    mapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mapList.setMaximumSize(new Dimension(0, 250));

    mapList.getSelectionModel().addListSelectionListener(e -> {
      if (EditorScreen.instance().isLoading()) {
        return;
      }

      if (mapList.getSelectedIndex() < EditorScreen.instance().getMapComponent().getMaps().size() && mapList.getSelectedIndex() >= 0) {
        if (Game.getEnvironment() != null && Game.getEnvironment().getMap().equals(EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex()))) {
          return;
        }

        EditorScreen.instance().getMapComponent().loadEnvironment(EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex()));
      }
    });

    mapScrollPane.setViewportView(mapList);

    popupMenu = new JPopupMenu();
    addPopup(mapList, popupMenu);

    mntmExportMap = new JMenuItem(Resources.get("hud_exportMap"));
    mntmExportMap.setIcon(Icons.MAP_EXPORT);
    mntmExportMap.addActionListener(a -> {
      EditorScreen.instance().getMapComponent().exportMap();
    });

    popupMenu.add(mntmExportMap);

    mntmDeleteMap = new JMenuItem(Resources.get("hud_deleteMap"));
    mntmDeleteMap.setIcon(Icons.MAP_DELETE);
    mntmDeleteMap.addActionListener(a -> EditorScreen.instance().getMapComponent().deleteMap());
    popupMenu.add(mntmDeleteMap);
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_maps"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(11f));
    mapScrollPane.setViewportBorder(border);

    layerScrollPane = new JScrollPane();
    layerScrollPane.setViewportBorder(null);
    layerScrollPane.setMinimumSize(new Dimension(150, 0));
    layerScrollPane.setMaximumSize(new Dimension(0, 250));

    JTabbedPane tabPane = new JTabbedPane();

    this.entityPanel = new JPanel();
    this.entityPanel.setLayout(new BorderLayout(0, 0));

    tabPane.addTab(Resources.get("panel_entities"), entityPanel);
    tabPane.add(Resources.get("panel_mapObjectLayers"), layerScrollPane);
    
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

    textField = new JTextField();
    textField.setBorder(new EmptyBorder(0, 5, 0, 0));
    textField.setOpaque(false);
    textField.addActionListener(e -> search());
    textField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(final FocusEvent e) {
        textField.selectAll();
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
    tree.setBorder(null);

    tree.setCellRenderer(new IconTreeListRenderer());
    tree.setMaximumSize(new Dimension(0, 250));

    tree.addTreeSelectionListener(e -> {
      this.isFocussing = true;
      try {
        if (e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
          if (node.getUserObject() instanceof IconTreeListItem) {
            IconTreeListItem item = (IconTreeListItem) node.getUserObject();
            if (item.getUserObject() instanceof IEntity) {
              IMapObject obj = Game.getEnvironment().getMap().getMapObject(((IEntity) item.getUserObject()).getMapId());
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

    entityScrollPane.setViewportView(tree);
    tabPane.setMaximumSize(new Dimension(0, 150));

    this.nodeRoot = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_entities"), Icons.FOLDER));
    this.nodeProps = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_props"), Icons.PROP));
    this.nodeCreatures = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_creatures"), Icons.CREATURE));
    this.nodeLights = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_lights"), Icons.LIGHT));
    this.nodeTriggers = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_triggers"), Icons.TRIGGER));
    this.nodeSpawnpoints = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_spawnpoints"), Icons.SPAWMPOINT));
    this.nodeCollisionBoxes = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_collboxes"), Icons.COLLISIONBOX));
    this.nodeMapAreas = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_areas"), Icons.MAPAREA));
    this.nodeStaticShadows = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_shadow"), Icons.SHADOWBOX));
    this.nodeEmitter = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_emitter"), Icons.EMITTER));

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
  }

  public synchronized void bind(List<Map> maps) {
    this.bind(maps, false);
  }

  public synchronized void bind(List<Map> maps, boolean clear) {
    if (clear) {
      this.model.clear();
    }

    for (Map map : maps) {
      String name = map.getFileName();
      if (UndoManager.hasChanges(map)) {
        name += " *";
      }

      // update existing strings
      int indexToReplace = getIndexToReplace(map.getFileName());
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
      if (current == null || maps.stream().noneMatch(x -> current.startsWith(x.getFileName()))) {
        this.model.remove(i);
      }
    }

    mapList.revalidate();
  }

  public void setSelection(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      mapList.clearSelection();
      return;
    }

    if (model.contains(mapName)) {
      mapList.setSelectedValue(mapName, true);
    }

    UndoManager.onMapObjectAdded(manager -> this.populateMapObjectTree());
    UndoManager.onMapObjectRemoved(manager -> this.populateMapObjectTree());
    UndoManager.onUndoStackChanged(manager -> this.bind(EditorScreen.instance().getMapComponent().getMaps()));
    this.initLayerControl();
    this.populateMapObjectTree();
  }

  public boolean isSelectedMapObjectLayer(String name) {

    // Get all the selected items using the indices
    for (int i = 0; i < listObjectLayers.getModel().getSize(); i++) {
      if (i >= listObjectLayers.getModel().getSize()) {
        return false;
      }
      Object sel = listObjectLayers.getModel().getElementAt(i);
      JCheckBox check = (JCheckBox) sel;
      if (check.getText().startsWith(name) && check.isSelected()) {
        return true;
      }
    }
    return false;
  }

  private void initLayerControl() {
    if (mapList.getSelectedIndex() == -1 && this.model.size() > 0) {
      this.mapList.setSelectedIndex(0);
    }

    if (EditorScreen.instance().getMapComponent().getMaps().isEmpty()) {
      layerModel.clear();
      return;
    }

    Map map = EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
    layerModel.clear();
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      JCheckBox newBox = new JCheckBox(layer.getName() + " (" + layer.getMapObjects().size() + ")");
      if (layer.getColor() != null) {
        final String cacheKey = map.getFileName() + layer.getName();
        if (!ImageCache.IMAGES.containsKey(cacheKey)) {
          BufferedImage img = ImageProcessing.getCompatibleImage(10, 10);
          Graphics2D g = (Graphics2D) img.getGraphics();
          g.setColor(layer.getColor());
          g.fillRect(0, 0, 9, 9);
          g.setColor(Color.BLACK);
          g.drawRect(0, 0, 9, 9);
          g.dispose();
          ImageCache.IMAGES.put(cacheKey, img);
        }

        newBox.setIcon(new ImageIcon(ImageCache.IMAGES.get(cacheKey)));
      }
      newBox.setSelected(true);
      layerModel.addElement(newBox);
    }

    int start = 0;
    int end = mapList.getModel().getSize() - 1;
    if (end >= 0) {
      listObjectLayers.setSelectionInterval(start, end);
      this.listObjectLayers.setSelectedIndex(0);
    }
  }

  public int getSelectedLayerIndex() {
    return listObjectLayers.getSelectedIndex();
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

  private void collapseAll() {
    int row = tree.getRowCount() - 1;
    while (row > 0) {
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

    Enumeration en = parent.depthFirstEnumeration();
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

  private void populateMapObjectTree() {
    this.nodeRoot.setUserObject(new IconTreeListItem(Game.getEnvironment().getEntities().size() + " " + Resources.get("panel_mapselection_entities"), Icons.FOLDER));
    for (DefaultMutableTreeNode node : this.entityNodes) {
      node.removeAllChildren();
    }

    this.nodeLights.setUserObject(new IconTreeListItem(Game.getEnvironment().getLightSources().size() + " " + Resources.get("panel_mapselection_lights"), Icons.LIGHT));
    this.nodeProps.setUserObject(new IconTreeListItem(Game.getEnvironment().getProps().size() + " " + Resources.get("panel_mapselection_props"), Icons.PROP));
    this.nodeCreatures.setUserObject(new IconTreeListItem(Game.getEnvironment().getCreatures().size() + " " + Resources.get("panel_mapselection_creatures"), Icons.CREATURE));
    this.nodeTriggers.setUserObject(new IconTreeListItem(Game.getEnvironment().getTriggers().size() + " " + Resources.get("panel_mapselection_triggers"), Icons.TRIGGER));
    this.nodeSpawnpoints.setUserObject(new IconTreeListItem(Game.getEnvironment().getSpawnPoints().size() + " " + Resources.get("panel_mapselection_spawnpoints"), Icons.SPAWMPOINT));
    this.nodeCollisionBoxes.setUserObject(new IconTreeListItem(Game.getEnvironment().getCollisionBoxes().size() + " " + Resources.get("panel_mapselection_collboxes"), Icons.COLLISIONBOX));
    this.nodeMapAreas.setUserObject(new IconTreeListItem(Game.getEnvironment().getAreas().size() + " " + Resources.get("panel_mapselection_areas"), Icons.MAPAREA));
    this.nodeStaticShadows.setUserObject(new IconTreeListItem(Game.getEnvironment().getStaticShadows().size() + " " + Resources.get("panel_mapselection_shadow"), Icons.SHADOWBOX));
    this.nodeEmitter.setUserObject(new IconTreeListItem(Game.getEnvironment().getEmitters().size() + " " + Resources.get("panel_mapselection_emitter"), Icons.EMITTER));

    for (LightSource light : Game.getEnvironment().getLightSources()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(light));
      this.nodeLights.add(node);
    }

    for (Prop prop : Game.getEnvironment().getProps()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(prop));
      this.nodeProps.add(node);
    }
    
    for (Creature creature : Game.getEnvironment().getCreatures()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(creature));
      this.nodeCreatures.add(node);
    }

    for (Trigger trigger : Game.getEnvironment().getTriggers()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(trigger));
      this.nodeTriggers.add(node);
    }

    for (Spawnpoint spawn : Game.getEnvironment().getSpawnPoints()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(spawn));
      this.nodeSpawnpoints.add(node);
    }

    for (CollisionBox coll : Game.getEnvironment().getCollisionBoxes()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(coll));
      this.nodeCollisionBoxes.add(node);
    }

    for (MapArea area : Game.getEnvironment().getAreas()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(area));
      this.nodeMapAreas.add(node);
    }

    for (StaticShadow shadow : Game.getEnvironment().getStaticShadows()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(shadow));
      this.nodeStaticShadows.add(node);
    }

    for (Emitter emitter : Game.getEnvironment().getEmitters()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(emitter));
      this.nodeEmitter.add(node);
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
}

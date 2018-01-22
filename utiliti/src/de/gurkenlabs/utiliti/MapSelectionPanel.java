package de.gurkenlabs.utiliti;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
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
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.utiliti.swing.IconTreeListItem;
import de.gurkenlabs.utiliti.swing.IconTreeListRenderer;
import de.gurkenlabs.utiliti.swing.JCheckBoxList;

public class MapSelectionPanel extends JSplitPane {
  private static final Icon PROP_ICON = new ImageIcon(Resources.getImage("entity.png"));
  private static final Icon FOLDER_ICON = new ImageIcon(Resources.getImage("object_cube-10x10.png"));
  private static final Icon LIGHT_ICON = new ImageIcon(Resources.getImage("bulb.png"));
  private static final Icon TRIGGER_ICON = new ImageIcon(Resources.getImage("trigger.png"));
  private static final Icon SPAWMPOINT_ICON = new ImageIcon(Resources.getImage("spawnpoint.png"));
  private static final Icon COLLISIONBOX_ICON = new ImageIcon(Resources.getImage("collisionbox.png"));
  private static final Icon MAPAREA_ICON = new ImageIcon(Resources.getImage("maparea.png"));
  private static final Icon SHADOWBOX_ICON = new ImageIcon(Resources.getImage("shadowbox.png"));
  private static final Icon EMITTER_ICON = new ImageIcon(Resources.getImage("emitter.png"));

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

  private final JPanel panel;
  private final JTextField textField;
  private final JButton btnSearch;
  private final JTree tree;
  private final DefaultTreeModel entitiesTreeModel;
  private final DefaultMutableTreeNode nodeRoot;
  private final DefaultMutableTreeNode nodeProps;
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
    this.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Program.userPreferences.setMapPanelSplitter(this.getDividerLocation()));
    if (Program.userPreferences.getMapPanelSplitter() != 0) {
      this.setDividerLocation(Program.userPreferences.getMapPanelSplitter());
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
    mntmExportMap.setIcon(new ImageIcon(Resources.getImage("button-map-exportx16.png")));
    mntmExportMap.addActionListener(a -> EditorScreen.instance().getMapComponent().exportMap());

    popupMenu.add(mntmExportMap);

    mntmDeleteMap = new JMenuItem(Resources.get("hud_deleteMap"));
    mntmDeleteMap.setIcon(new ImageIcon(Resources.getImage("button-deletex16.png")));
    mntmDeleteMap.addActionListener(a -> EditorScreen.instance().getMapComponent().deleteMap());
    popupMenu.add(mntmDeleteMap);
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_maps"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(11f));
    mapScrollPane.setViewportBorder(border);

    layerScrollPane = new JScrollPane();
    layerScrollPane.setViewportBorder(null);
    layerScrollPane.setMinimumSize(new Dimension(150, 0));
    layerScrollPane.setMaximumSize(new Dimension(0, 250));

    entityScrollPane = new JScrollPane();
    entityScrollPane.setViewportBorder(null);
    entityScrollPane.setMinimumSize(new Dimension(150, 0));
    entityScrollPane.setMaximumSize(new Dimension(0, 250));

    JTabbedPane tabPane = new JTabbedPane();
    tabPane.add(Resources.get("panel_mapObjectLayers"), layerScrollPane);
    tabPane.add(Resources.get("panel_entities"), entityScrollPane);
    tabPane.setMaximumSize(new Dimension(0, 150));

    this.tree = new JTree();

    this.nodeRoot = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_entities"), FOLDER_ICON));
    this.nodeProps = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_props"), PROP_ICON));
    this.nodeLights = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_lights"), LIGHT_ICON));
    this.nodeTriggers = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_triggers"), TRIGGER_ICON));
    this.nodeSpawnpoints = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_spawnpoints"), SPAWMPOINT_ICON));
    this.nodeCollisionBoxes = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_collboxes"), COLLISIONBOX_ICON));
    this.nodeMapAreas = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_areas"), MAPAREA_ICON));
    this.nodeStaticShadows = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_shadow"), SHADOWBOX_ICON));
    this.nodeEmitter = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("panel_mapselection_emitter"), EMITTER_ICON));

    this.nodeRoot.add(nodeProps);
    this.nodeRoot.add(nodeLights);
    this.nodeRoot.add(nodeTriggers);
    this.nodeRoot.add(nodeSpawnpoints);
    this.nodeRoot.add(nodeCollisionBoxes);
    this.nodeRoot.add(nodeMapAreas);
    this.nodeRoot.add(nodeStaticShadows);
    this.nodeRoot.add(nodeEmitter);
    this.entitiesTreeModel = new DefaultTreeModel(this.nodeRoot);

    this.entityNodes = new DefaultMutableTreeNode[] { this.nodeProps, this.nodeLights, this.nodeTriggers, this.nodeSpawnpoints, this.nodeCollisionBoxes, this.nodeMapAreas, this.nodeStaticShadows, this.nodeEmitter, };

    tree.setModel(this.entitiesTreeModel);
    tree.setCellRenderer(new IconTreeListRenderer());
    tree.setMaximumSize(new Dimension(0, 250));
    MouseListener ml = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        if (selRow != -1 && e.getClickCount() == 2) {
          EditorScreen.instance().getMapComponent().centerCameraOnFocus();
        }
      }
    };

    tree.addMouseListener(ml);
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
                EditorScreen.instance().getMapComponent().setFocus(obj);
              }
            }
          }
        }
      } finally {
        this.isFocussing = false;
      }
    });

    entityScrollPane.setViewportView(tree);

    panel = new JPanel();
    entityScrollPane.setColumnHeaderView(panel);
    panel.setLayout(new BorderLayout(0, 0));

    textField = new JTextField();
    textField.addActionListener(e -> search());
    panel.add(textField, BorderLayout.CENTER);
    textField.setColumns(10);

    btnSearch = new JButton("");
    btnSearch.addActionListener(e -> search());
    btnSearch.setIcon(new ImageIcon(Resources.getImage("search.png")));
    panel.add(btnSearch, BorderLayout.EAST);
    tabPane.setIconAt(0, new ImageIcon(Resources.getImage("layer.png")));
    tabPane.setIconAt(1, new ImageIcon(Resources.getImage("object_cube-10x10.png")));
    this.setRightComponent(tabPane);

    listObjectLayers = new JCheckBoxList();
    listObjectLayers.setModel(layerModel);
    listObjectLayers.setMaximumSize(new Dimension(0, 250));
    layerScrollPane.setViewportView(listObjectLayers);

  }

  public void bind(List<Map> maps) {
    model.clear();
    for (int i = 0; i < maps.size(); i++) {
      model.addElement(maps.get(i).getFileName());
    }
    mapList.setVisible(false);
    mapList.setVisible(true);
  }

  public void setSelection(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      mapList.clearSelection();
      return;
    }

    if (model.contains(mapName)) {
      mapList.setSelectedValue(mapName, true);
    }

    Game.getEnvironment().onEntityAdded(ent -> this.populateMapObjectTree());
    Game.getEnvironment().onEntityRemoved(ent -> this.populateMapObjectTree());
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
    Map map = EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
    layerModel.clear();
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      JCheckBox newBox = new JCheckBox(layer.getName() + " (" + layer.getMapObjects().size() + ")");
      if (layer.getColor() != null) {
        BufferedImage img = ImageProcessing.getCompatibleImage(10, 10);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(layer.getColor());
        g.fillRect(0, 0, 9, 9);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, 9, 9);
        g.dispose();
        newBox.setIcon(new ImageIcon(img));
      }
      newBox.setSelected(true);
      layerModel.addElement(newBox);
    }

    int start = 0;
    int end = mapList.getModel().getSize() - 1;
    if (end >= 0) {
      listObjectLayers.setSelectionInterval(start, end);
    }
  }

  public int getSelectedLayerIndex() {
    return listObjectLayers.getSelectedIndex();
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

  private void search() {
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

  private static void addPopup(Component component, final JPopupMenu popup) {
    component.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showMenu(e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          showMenu(e);
        }
      }

      private void showMenu(MouseEvent e) {
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    });
  }

  private void populateMapObjectTree() {
    this.nodeRoot.setUserObject(new IconTreeListItem(Game.getEnvironment().getEntities().size() + " " + Resources.get("panel_mapselection_entities"), FOLDER_ICON));
    for (DefaultMutableTreeNode node : this.entityNodes) {
      node.removeAllChildren();
    }

    this.nodeLights.setUserObject(new IconTreeListItem(Game.getEnvironment().getLightSources().size() + " " + Resources.get("panel_mapselection_lights"), LIGHT_ICON));
    this.nodeProps.setUserObject(new IconTreeListItem(Game.getEnvironment().getProps().size() + " " + Resources.get("panel_mapselection_props"), PROP_ICON));
    this.nodeTriggers.setUserObject(new IconTreeListItem(Game.getEnvironment().getTriggers().size() + " " + Resources.get("panel_mapselection_triggers"), TRIGGER_ICON));
    this.nodeSpawnpoints.setUserObject(new IconTreeListItem(Game.getEnvironment().getSpawnPoints().size() + " " + Resources.get("panel_mapselection_spawnpoints"), SPAWMPOINT_ICON));
    this.nodeCollisionBoxes.setUserObject(new IconTreeListItem(Game.getEnvironment().getCollisionBoxes().size() + " " + Resources.get("panel_mapselection_collboxes"), COLLISIONBOX_ICON));
    this.nodeMapAreas.setUserObject(new IconTreeListItem(Game.getEnvironment().getAreas().size() + " " + Resources.get("panel_mapselection_areas"), MAPAREA_ICON));
    this.nodeStaticShadows.setUserObject(new IconTreeListItem(Game.getEnvironment().getStaticShadows().size() + " " + Resources.get("panel_mapselection_shadow"), SHADOWBOX_ICON));
    this.nodeEmitter.setUserObject(new IconTreeListItem(Game.getEnvironment().getEmitters().size() + " " + Resources.get("panel_mapselection_emitter"), EMITTER_ICON));

    for (LightSource light : Game.getEnvironment().getLightSources()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(light));
      this.nodeLights.add(node);
    }

    for (Prop prop : Game.getEnvironment().getProps()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(prop));
      this.nodeProps.add(node);
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
}

package de.gurkenlabs.utiliti.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.EditorScreen;

@SuppressWarnings("serial")
public class MapSelectionPanel extends JSplitPane {
  private final JList<String> mapList;
  private final DefaultListModel<String> model;
  private final JScrollPane mapScrollPane;
  private final MapLayerList mapLayerList;
  private final JScrollPane entityScrollPane;
  private final JPopupMenu popupMenu;
  private final JMenuItem exportMap;
  private final JMenuItem deleteMap;

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
    this.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Program.preferences().setMapPanelSplitter(this.getDividerLocation()));
    if (Program.preferences().getMapPanelSplitter() != 0) {
      this.setDividerLocation(Program.preferences().getMapPanelSplitter());
    }

    this.setMaximumSize(new Dimension(0, 250));
    setContinuousLayout(true);
    mapScrollPane = new JScrollPane();
    mapScrollPane.setMinimumSize(new Dimension(80, 0));
    mapScrollPane.setMaximumSize(new Dimension(0, 250));
    this.setLeftComponent(mapScrollPane);

    model = new DefaultListModel<>();

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
        TmxMap map = EditorScreen.instance().getMapComponent().getMaps().get(this.mapList.getSelectedIndex());
        if (Game.world().environment() != null && Game.world().environment().getMap().equals(map)) {
          return;
        }

        EditorScreen.instance().getMapComponent().loadEnvironment(map);
      }
    });

    mapScrollPane.setViewportView(this.mapList);

    popupMenu = new JPopupMenu();
    addPopup(this.mapList, popupMenu);

    exportMap = new JMenuItem(Resources.strings().get("hud_exportMap"));
    exportMap.setIcon(Icons.MAP_EXPORT);
    exportMap.addActionListener(a -> EditorScreen.instance().getMapComponent().exportMap());

    deleteMap = new JMenuItem(Resources.strings().get("hud_deleteMap"));
    deleteMap.setIcon(Icons.MAP_DELETE);
    deleteMap.addActionListener(a -> EditorScreen.instance().getMapComponent().deleteMap());

    popupMenu.add(exportMap);
    popupMenu.add(deleteMap);

    mapScrollPane.setViewportBorder(null);

    this.mapLayerList = new MapLayerList();
    EditorScreen.instance().setMapLayerList(this.mapLayerList);
    JTabbedPane tabPane = new JTabbedPane();

    this.entityPanel = new JPanel();
    this.entityPanel.setLayout(new BorderLayout(0, 0));

    tabPane.addTab(Resources.strings().get("panel_entities"), entityPanel);
    tabPane.add(Resources.strings().get("panel_mapObjectLayers"), this.mapLayerList);

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
      final Environment env = Game.world().environment();
      if (env == null) {
        return;
      }

      this.isFocussing = true;
      try {
        if (e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
          if (node.getUserObject() instanceof IconTreeListItem) {
            IconTreeListItem item = (IconTreeListItem) node.getUserObject();
            if (item.getUserObject() instanceof IEntity) {
              IMapObject obj = env.getMap().getMapObject(((IEntity) item.getUserObject()).getMapId());
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

    UndoManager.onMapObjectAdded(manager -> {
      this.updateComponents();

    });

    UndoManager.onMapObjectRemoved(manager -> {
      this.updateComponents();
    });

    UndoManager.onUndoStackChanged(manager -> this.bind(EditorScreen.instance().getMapComponent().getMaps()));
  }

  public synchronized void bind(List<TmxMap> maps) {
    this.bind(maps, false);
  }

  public synchronized void bind(List<TmxMap> maps, boolean clear) {
    if (clear) {
      this.model.clear();
    }

    for (TmxMap map : maps) {
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
    this.updateComponents();
  }

  public void setSelection(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      mapList.clearSelection();
    } else {
      if (model.contains(mapName)) {
        mapList.setSelectedValue(mapName, true);
      }
    }

    this.updateComponents();
  }

  public IMap getCurrentMap() {
    if (this.mapList.getSelectedIndex() == -1) {
      return null;
    }
    return EditorScreen.instance().getMapComponent().getMaps().get(mapList.getSelectedIndex());
  }

  public void focus(final IMapObject mapObject) {
    if (this.isFocussing) {
      return;
    }

    if (mapObject == null || mapObject.getType() == null) {
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

  public void updateComponents() {
    if (mapList.getSelectedIndex() == -1 && this.model.size() > 0) {
      this.mapList.setSelectedIndex(0);
    }

    this.updateMapObjectTree();
    this.mapLayerList.update();
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

    if (Game.world().environment() != null) {
      addEntitiesToTreeNode(Game.world().environment().getProps(), this.nodeProps, Resources.strings().get("panel_mapselection_props"), Icons.PROP);
      addEntitiesToTreeNode(Game.world().environment().getCreatures(), this.nodeCreatures, Resources.strings().get("panel_mapselection_creatures"), Icons.CREATURE);
      addEntitiesToTreeNode(Game.world().environment().getCollisionBoxes(), this.nodeCollisionBoxes, Resources.strings().get("panel_mapselection_collboxes"), Icons.COLLISIONBOX);
      addEntitiesToTreeNode(Game.world().environment().getTriggers(), this.nodeTriggers, Resources.strings().get("panel_mapselection_triggers"), Icons.TRIGGER);
      addEntitiesToTreeNode(Game.world().environment().getSpawnPoints(), this.nodeSpawnpoints, Resources.strings().get("panel_mapselection_spawnpoints"), Icons.SPAWNPOINT);
      addEntitiesToTreeNode(Game.world().environment().getAreas(), this.nodeMapAreas, Resources.strings().get("panel_mapselection_areas"), Icons.MAPAREA);
      addEntitiesToTreeNode(Game.world().environment().getLightSources(), this.nodeLights, Resources.strings().get("panel_mapselection_lights"), Icons.LIGHT);
      addEntitiesToTreeNode(Game.world().environment().getStaticShadows(), this.nodeStaticShadows, Resources.strings().get("panel_mapselection_shadow"), Icons.SHADOWBOX);
      addEntitiesToTreeNode(Game.world().environment().getEmitters(), this.nodeEmitter, Resources.strings().get("panel_mapselection_emitter"), Icons.EMITTER);
    } else {
      this.nodeRoot.removeAllChildren();
    }

    this.entitiesTreeModel.reload();
  }

  private <T extends Entity> void addEntitiesToTreeNode(Collection<T> entities, DefaultMutableTreeNode entityNode, String nodeName, Icon nodeIcon) {
    entityNode.setUserObject(new IconTreeListItem((Game.world().environment() == null ? 0 : entities.size()) + " " + nodeName, nodeIcon));

    for (T entity : entities.stream().sorted((p1, p2) -> Integer.compare(p1.getMapId(), p2.getMapId())).collect(Collectors.toList())) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(entity));
      entityNode.add(node);
    }

    if (entities.isEmpty()) {
      entityNode.removeFromParent();
    } else {
      this.nodeRoot.add(entityNode);
    }
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

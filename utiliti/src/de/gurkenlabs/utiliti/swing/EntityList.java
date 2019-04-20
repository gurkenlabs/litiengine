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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
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
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.components.EditorScreen;

@SuppressWarnings("serial")
public final class EntityList extends JPanel {
  private final JScrollPane entityScrollPane;
  private final JPanel searchPanel;

  // commands and search
  private final JButton btnCollape;
  private final JButton btnSearch;
  private final JTextField textField;

  // entity tree
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

  public EntityList() {
    this.setLayout(new BorderLayout(0, 0));

    this.entityScrollPane = new JScrollPane();
    this.entityScrollPane.setViewportBorder(null);
    this.entityScrollPane.setMinimumSize(new Dimension(150, 0));
    this.entityScrollPane.setMaximumSize(new Dimension(0, 250));

    this.searchPanel = new JPanel();
    this.searchPanel.setBorder(new LineBorder(UIManager.getColor("Button.shadow")));
    this.searchPanel.setBackground(Color.WHITE);
    this.searchPanel.setLayout(new BorderLayout(0, 0));

    this.btnCollape = new JButton("");
    this.btnCollape.setOpaque(false);
    this.btnCollape.setMargin(new Insets(2, 2, 2, 2));
    this.btnCollape.addActionListener(e -> collapseAll());
    this.btnCollape.setIcon(Icons.COLLAPSE);

    this.textField = new JTextField(Resources.strings().get("panel_entities_search_default"));
    this.textField.setBorder(new EmptyBorder(0, 5, 0, 0));
    this.textField.setOpaque(false);
    this.textField.setForeground(Color.GRAY);
    this.textField.setColumns(10);
    this.textField.addActionListener(e -> search());
    this.textField.addFocusListener(new FocusAdapter() {
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

    this.btnSearch = new JButton("");
    this.btnSearch.setBorderPainted(false);
    this.btnSearch.setContentAreaFilled(false);
    this.btnSearch.setOpaque(false);
    this.btnSearch.setMargin(new Insets(2, 2, 2, 2));
    this.btnSearch.addActionListener(e -> search());
    this.btnSearch.setIcon(Icons.SEARCH);

    this.searchPanel.add(this.textField, BorderLayout.CENTER);
    this.searchPanel.add(this.btnSearch, BorderLayout.EAST);
    this.searchPanel.add(this.btnCollape, BorderLayout.WEST);

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

    this.entityScrollPane.setViewportView(this.tree);
    this.add(this.entityScrollPane);
    this.add(this.searchPanel, BorderLayout.NORTH);
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
  
  public void update() {
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
}

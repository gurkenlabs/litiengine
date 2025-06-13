package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.components.OverlayScrollPane;
import com.github.weisj.darklaf.ui.text.DarkTextUI;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.EntityController;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.renderers.IconTreeListRenderer;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.function.Predicate;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public final class EntityList extends JPanel implements EntityController {
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
    this.setName(Resources.strings().get("panel_entities"));
    this.setLayout(new BorderLayout(0, 0));

    this.searchPanel = new JPanel();
    this.searchPanel.setLayout(new BorderLayout(0, 0));

    this.btnCollape = new JButton("");
    this.btnCollape.setOpaque(false);
    this.btnCollape.setMargin(new Insets(2, 2, 2, 2));
    this.btnCollape.addActionListener(e -> collapseAll());
    this.btnCollape.setIcon(Icons.COLLAPSE);

    final String entitySearchDefault = Resources.strings().get("panel_entities_search_default");

    this.textField = new JTextField();
    this.textField.putClientProperty(DarkTextUI.KEY_DEFAULT_TEXT, entitySearchDefault);
    this.textField.setColumns(10);
    this.textField.addActionListener(e -> search());

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
    this.tree.setRowHeight((int) (this.tree.getRowHeight() * Editor.preferences().getUiScale()));

    this.tree.addTreeSelectionListener(
      e -> {
        final Environment env = Game.world().environment();
        if (env == null) {
          return;
        }

        this.isFocussing = true;
        try {

          final TreePath path = e.getNewLeadSelectionPath();
          if (path == null) {
            return;
          }
          if (path.getLastPathComponent() instanceof DefaultMutableTreeNode dmtn
            && (dmtn.getUserObject() instanceof IconTreeListItem itli
            && (itli.getUserObject() instanceof IEntity ie))) {
            IMapObject obj = env.getMap().getMapObject(ie.getMapId());
            if (obj != null) {
              Editor.instance().getMapComponent().setFocus(obj, true);
            }


          }

        } finally {
          this.isFocussing = false;
        }
      });

    this.nodeRoot =
      new DefaultMutableTreeNode(
        new IconTreeListItem(
          Resources.strings().get("panel_mapselection_entities"), Icons.FOLDER));
    this.nodeProps =
      new DefaultMutableTreeNode(
        new IconTreeListItem(Resources.strings().get("panel_mapselection_props"), Icons.PROP));
    this.nodeCreatures =
      new DefaultMutableTreeNode(
        new IconTreeListItem(
          Resources.strings().get("panel_mapselection_creatures"), Icons.CREATURE));
    this.nodeLights =
      new DefaultMutableTreeNode(
        new IconTreeListItem(
          Resources.strings().get("panel_mapselection_lights"), Icons.LIGHT));
    this.nodeTriggers =
      new DefaultMutableTreeNode(
        new IconTreeListItem(
          Resources.strings().get("panel_mapselection_triggers"), Icons.TRIGGER));
    this.nodeSpawnpoints =
      new DefaultMutableTreeNode(
        new IconTreeListItem(
          Resources.strings().get("panel_mapselection_spawnpoints"), Icons.SPAWNPOINT));
    this.nodeCollisionBoxes =
      new DefaultMutableTreeNode(
        new IconTreeListItem(
          Resources.strings().get("panel_mapselection_collboxes"), Icons.COLLISIONBOX));
    this.nodeMapAreas =
      new DefaultMutableTreeNode(
        new IconTreeListItem(
          Resources.strings().get("panel_mapselection_areas"), Icons.MAPAREA));
    this.nodeStaticShadows =
      new DefaultMutableTreeNode(
        new IconTreeListItem(
          Resources.strings().get("panel_mapselection_shadow"), Icons.SHADOWBOX));
    this.nodeEmitter =
      new DefaultMutableTreeNode(
        new IconTreeListItem(
          Resources.strings().get("panel_mapselection_emitter"), Icons.EMITTER));

    this.entitiesTreeModel = new DefaultTreeModel(this.nodeRoot);

    this.entityNodes =
      new DefaultMutableTreeNode[] {
        this.nodeProps,
        this.nodeCreatures,
        this.nodeLights,
        this.nodeTriggers,
        this.nodeSpawnpoints,
        this.nodeCollisionBoxes,
        this.nodeMapAreas,
        this.nodeStaticShadows,
        this.nodeEmitter,
      };
    MouseListener ml =
      new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          int selRow = tree.getRowForLocation(e.getX(), e.getY());
          if (selRow != -1 && e.getClickCount() == 2) {
            Editor.instance().getMapComponent().centerCameraOnFocus();
          }
        }
      };
    tree.setModel(this.entitiesTreeModel);
    tree.addMouseListener(ml);

    this.add(new OverlayScrollPane(tree));
    this.add(this.searchPanel, BorderLayout.NORTH);
  }

  @Override
  public void select(final IMapObject mapObject) {
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

  @Override
  public void refresh(int mapId) {
    boolean updated = false;
    for (DefaultMutableTreeNode parent : this.entityNodes) {
      if (updated) {
        break;
      }

      if (parent.getChildCount() == 0) {
        continue;
      }

      Enumeration<?> en = parent.depthFirstEnumeration();
      while (en.hasMoreElements()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
        IEntity ent = null;
        if (node.getUserObject() instanceof IconTreeListItem itli
          && itli.getUserObject() instanceof IEntity ie) {
          ent = ie;
        } else if (node.getUserObject() instanceof IEntity ie2) {
          ent = ie2;
        }

        if (ent == null) {
          continue;
        }

        if (ent.getMapId() == mapId) {
          node.setUserObject(new IconTreeListItem(Game.world().environment().get(mapId)));
          entitiesTreeModel.reload(node);
          updated = true;
          break;
        }
      }
    }

    if (!updated) {
      this.refresh();
    }
  }

  @Override
  public void remove(IMapObject mapObject) {
    boolean removed = false;
    for (DefaultMutableTreeNode parent : this.entityNodes) {
      if (removed) {
        break;
      }

      if (parent.getChildCount() == 0) {
        continue;
      }

      Enumeration<?> en = parent.depthFirstEnumeration();
      while (en.hasMoreElements()) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
        IEntity ent = null;
        if (node.getUserObject() instanceof IconTreeListItem itli
          && itli.getUserObject() instanceof IEntity ie) {
          ent = ie;
        } else if (node.getUserObject() instanceof IEntity ie2) {
          ent = ie2;
        }

        if (ent == null) {
          continue;
        }

        if (ent.getMapId() == mapObject.getId()) {
          parent.remove(node);
          entitiesTreeModel.reload(parent);
          removed = true;
          break;
        }
      }
    }
  }

  @Override
  public void refresh() {
    this.nodeRoot.setUserObject(
      new IconTreeListItem(
        (Game.world().environment() == null
          ? 0
          : Game.world().environment().getEntities().size())
          + " "
          + Resources.strings().get("panel_mapselection_entities"),
        Icons.FOLDER));
    for (DefaultMutableTreeNode node : this.entityNodes) {
      node.removeAllChildren();
    }

    if (Game.world().environment() != null) {
      addEntitiesToTreeNode(
        Game.world().environment().getProps(),
        this.nodeProps,
        Resources.strings().get("panel_mapselection_props"),
        Icons.PROP);
      addEntitiesToTreeNode(
        Game.world().environment().getCreatures(),
        this.nodeCreatures,
        Resources.strings().get("panel_mapselection_creatures"),
        Icons.CREATURE);
      addEntitiesToTreeNode(
        Game.world().environment().getCollisionBoxes(),
        this.nodeCollisionBoxes,
        Resources.strings().get("panel_mapselection_collboxes"),
        Icons.COLLISIONBOX);
      addEntitiesToTreeNode(
        Game.world().environment().getTriggers(),
        this.nodeTriggers,
        Resources.strings().get("panel_mapselection_triggers"),
        Icons.TRIGGER);
      addEntitiesToTreeNode(
        Game.world().environment().getSpawnpoints(),
        this.nodeSpawnpoints,
        Resources.strings().get("panel_mapselection_spawnpoints"),
        Icons.SPAWNPOINT);
      addEntitiesToTreeNode(
        Game.world().environment().getAreas(),
        this.nodeMapAreas,
        Resources.strings().get("panel_mapselection_areas"),
        Icons.MAPAREA);
      addEntitiesToTreeNode(
        Game.world().environment().getLightSources(),
        this.nodeLights,
        Resources.strings().get("panel_mapselection_lights"),
        Icons.LIGHT);
      addEntitiesToTreeNode(
        Game.world().environment().getStaticShadows(),
        this.nodeStaticShadows,
        Resources.strings().get("panel_mapselection_shadow"),
        Icons.SHADOWBOX);
      addEntitiesToTreeNode(
        Game.world().environment().getEmitters(),
        this.nodeEmitter,
        Resources.strings().get("panel_mapselection_emitter"),
        Icons.EMITTER);
    } else {
      this.nodeRoot.removeAllChildren();
    }

    this.entitiesTreeModel.reload();
    this.select(Editor.instance().getMapComponent().getFocusedMapObject());
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

    return this.select(
      parent,
      e -> e.getName() != null && (e.getName().contains(name) || e.getName().matches(name)));
  }

  private boolean select(DefaultMutableTreeNode parent, Predicate<IEntity> selectionPredicate) {
    if (parent.getChildCount() == 0) {
      return false;
    }

    Enumeration<?> en = parent.depthFirstEnumeration();
    while (en.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
      IEntity ent = null;
      if (node.getUserObject() instanceof IconTreeListItem itli
        && itli.getUserObject() instanceof IEntity ie) {
        ent = ie;
      } else if (node.getUserObject() instanceof IEntity ie2) {
        ent = ie2;
      }

      if (ent == null) {
        continue;
      }

      if (selectionPredicate.test(ent)) {
        final TreePath newSelection = new TreePath(node.getPath());
        if (this.tree.getSelectionPath() != null
          && this.tree.getSelectionPath().equals(newSelection)) {
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

  private <T extends Entity> void addEntitiesToTreeNode(Collection<T> entities, DefaultMutableTreeNode entityNode,
    String nodeName, Icon nodeIcon) {

    for (T entity : entities.stream().sorted(Comparator.comparingInt(Entity::getMapId))
      .toList()) {
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(new IconTreeListItem(entity));
      entityNode.add(node);
    }

    entityNode.setUserObject(
      new ParentIconTreeListItem(nodeName, nodeIcon, entityNode::getChildCount));

    if (entities.isEmpty()) {
      entityNode.removeFromParent();
    } else {
      this.nodeRoot.add(entityNode);
    }
  }
}

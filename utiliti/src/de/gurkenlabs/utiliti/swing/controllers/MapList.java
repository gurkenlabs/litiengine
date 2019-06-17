package de.gurkenlabs.utiliti.swing.controllers;

import java.awt.Dimension;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.components.MapController;
import de.gurkenlabs.utiliti.swing.CustomMutableTreeNode;
import de.gurkenlabs.utiliti.swing.LeafOnlyTreeSelectionModel;
import de.gurkenlabs.utiliti.swing.UI;

@SuppressWarnings("serial")
public class MapList extends JScrollPane implements MapController {
  private final JTree tree;
  private final CustomMutableTreeNode root;
  private final DefaultTreeModel model;

  public MapList() {
    super();
    this.setMinimumSize(new Dimension(80, 0));
    this.setMaximumSize(new Dimension(0, 250));

    this.root = new CustomMutableTreeNode("! root should be invisible !", -1);

    this.model = new DefaultTreeModel(this.root);

    this.tree = new JTree();
    this.tree.setRootVisible(false);
    this.tree.setShowsRootHandles(true);
    this.tree.setVisibleRowCount(8);
    this.tree.setModel(this.model);
    this.tree.setSelectionModel(new LeafOnlyTreeSelectionModel());
    this.tree.setMaximumSize(new Dimension(0, 250));

    this.tree.getSelectionModel().addTreeSelectionListener(e -> {
      if (Editor.instance().isLoading() || Editor.instance().getMapComponent().isLoading()) {
        return;
      }

      CustomMutableTreeNode selectedNode = (CustomMutableTreeNode) this.tree.getLastSelectedPathComponent();
      if (selectedNode != null && this.root.getLeafCount() == Editor.instance().getMapComponent().getMaps().size() && selectedNode.getIndex() >= 0) {
        TmxMap map = Editor.instance().getMapComponent().getMaps().get(selectedNode.getIndex());
        if (Game.world().environment() != null && Game.world().environment().getMap().equals(map)) {
          return;
        }

        Editor.instance().getMapComponent().loadEnvironment(map);
      }
    });

    this.setViewportView(this.tree);
    this.setViewportBorder(null);

    UndoManager.onMapObjectAdded(manager -> {
      this.refresh();
    });

    UndoManager.onMapObjectRemoved(manager -> {
      this.refresh();
    });

    UndoManager.onUndoStackChanged(manager -> this.bind(Editor.instance().getMapComponent().getMaps()));
  }

  @Override
  public synchronized void bind(List<TmxMap> maps) {
    this.bind(maps, false);
  }

  @Override
  public synchronized void bind(List<TmxMap> maps, boolean clear) {
    if (clear) {
      this.root.removeAllChildren();
      this.model.reload();
    }

    /**
     * TODO
     * Best way would be to group maps as they would already be grouped in their folders.
     * If they aren't grouped in any folders, auto-group maps.
     */
    this.autoGroupMaps(maps);

    // remove maps that are no longer present
    @SuppressWarnings("unchecked")
    Enumeration<CustomMutableTreeNode> e = this.root.depthFirstEnumeration();
    while (e.hasMoreElements()) {
      CustomMutableTreeNode node = e.nextElement();
      if (node.isLeaf() && (node.toString() == null || maps.stream().noneMatch(x -> node.toString().startsWith(x.getName())))) {
        this.root.remove(node);
      }
    }

    this.tree.revalidate();
    this.refresh();
  }

  @Override
  public void setSelection(String mapName) {
    if (mapName == null || mapName.isEmpty()) {
      this.tree.clearSelection();
    } else {
      CustomMutableTreeNode node = this.findLeaf(mapName);
      if (node != null) {
        this.tree.setSelectionPath(new TreePath(node.getPath()));
      }
    }

    this.refresh();
  }

  @Override
  public IMap getCurrentMap() {
    CustomMutableTreeNode selectedNode = (CustomMutableTreeNode) this.tree.getLastSelectedPathComponent();
    if (selectedNode != null && ! selectedNode.isLeaf()) {
      return null;
    }
    return Editor.instance().getMapComponent().getMaps().get(selectedNode.getIndex());
  }

  @Override
  public void refresh() {
    CustomMutableTreeNode selectedNode = (CustomMutableTreeNode) this.tree.getLastSelectedPathComponent();
    if (selectedNode != null && selectedNode.isLeaf() && this.root.getLeafCount() > 0) {
      this.tree.setSelectionPath(new TreePath(selectedNode.getPath()));
    }

    UI.getEntityController().refresh();
    UI.getLayerController().refresh();
  }
  
  public CustomMutableTreeNode findNode(String name) {
    @SuppressWarnings("unchecked")
    Enumeration<CustomMutableTreeNode> e = this.root.depthFirstEnumeration();
    while (e.hasMoreElements()) {
      CustomMutableTreeNode node = e.nextElement();
      if (node.toString() != null && (node.toString().equals(name) || node.toString().equals(name + " *"))) {
        return node;
      }
    }
    return null;
  }

  public CustomMutableTreeNode findLeaf(String mapName) {
    CustomMutableTreeNode nodeResult = this.findNode(mapName);
    if (nodeResult != null && nodeResult.isLeaf()) {
      return nodeResult;
    }
    return null;
  }
  
  private void autoGroupMaps(List<TmxMap> maps) {
    TmxMap map;
    for (int i=0; i<maps.size(); i++) {
      map = maps.get(i);
      String name = map.getName();
      if (UndoManager.hasChanges(map)) {
        name += " *";
      }

      CustomMutableTreeNode node = this.findLeaf(map.getName());
      if (node != null) {
        node.setName(name);
      } else {
        String nameParent = name;
        while (nameParent.matches(".*\\d")) {
          nameParent = nameParent.substring(0, (nameParent.length()-1));
        }
        if (nameParent.matches(".*[^a-zA-Z0-9]")) {
          nameParent = nameParent.substring(0, nameParent.length()-1);
        }

        // if map would be part of a group
        if (! nameParent.equals(name)) {
          CustomMutableTreeNode nodeParent = this.findNode(nameParent);
          // if node group doesn't exist
          if (nodeParent == null || (! this.root.isNodeChild(nodeParent))) {
            // create and add node group
            nodeParent = new CustomMutableTreeNode(nameParent, -1);
            this.root.add(nodeParent);
          }
          // add new map its node group
          nodeParent.add(new CustomMutableTreeNode(name, i));
        }
        else {
          // add new map to root
          this.root.add(new CustomMutableTreeNode(name, i));
        }
      }
    }
  }
}

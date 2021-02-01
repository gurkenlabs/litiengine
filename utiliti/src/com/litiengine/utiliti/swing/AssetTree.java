package com.litiengine.utiliti.swing;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.litiengine.environment.tilemap.ITileset;
import com.litiengine.environment.tilemap.xml.Tileset;
import com.litiengine.environment.tilemap.xml.TmxMap;
import com.litiengine.graphics.animation.PropAnimationController;
import com.litiengine.resources.ResourceBundle;
import com.litiengine.resources.Resources;
import com.litiengine.utiliti.components.Editor;
import com.litiengine.utiliti.swing.panels.CreaturePanel;

@SuppressWarnings("serial")
public class AssetTree extends JTree {
  private final AssetPanel assetPanel;
  private final DefaultTreeModel entitiesTreeModel;
  private final DefaultMutableTreeNode nodeRoot;
  private final DefaultMutableTreeNode nodeSpritesheets;
  private final DefaultMutableTreeNode nodeSpriteProps;
  private final DefaultMutableTreeNode nodeSpriteMisc;
  private final DefaultMutableTreeNode nodeTileSets;
  private final DefaultMutableTreeNode nodeSounds;
  private final DefaultMutableTreeNode nodeEmitters;
  private final DefaultMutableTreeNode nodeBlueprints;
  private final DefaultMutableTreeNode nodeCreatures;

  public AssetTree(AssetPanel assetPanel) {
    this.setRootVisible(false);

    this.assetPanel = assetPanel;

    this.nodeRoot = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_assets"), Icons.ASSET));
    this.nodeSpritesheets = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_spritesheets"), Icons.SPRITESHEET));
    this.nodeSpriteProps = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_spritesheets_props"), Icons.PROP));
    this.nodeSpriteMisc = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_spritesheets_misc"), Icons.MISC));
    this.nodeTileSets = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_tilesets"), Icons.TILESET));
    this.nodeSounds = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_sounds"), Icons.SOUND));
    this.nodeEmitters = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_emitters"), Icons.EMITTER));
    this.nodeBlueprints = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_blueprints"), Icons.BLUEPRINT));
    this.nodeCreatures = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_creatures"), Icons.CREATURE));

    this.nodeSpritesheets.add(this.nodeSpriteProps);
    this.nodeSpritesheets.add(this.nodeCreatures);
    this.nodeSpritesheets.add(this.nodeSpriteMisc);

    this.nodeRoot.add(this.nodeSpritesheets);
    this.nodeRoot.add(this.nodeEmitters);
    this.nodeRoot.add(this.nodeBlueprints);
    this.nodeRoot.add(this.nodeTileSets);
    this.nodeRoot.add(this.nodeSounds);

    this.entitiesTreeModel = new DefaultTreeModel(this.nodeRoot);

    this.setModel(this.entitiesTreeModel);
    this.setCellRenderer(new IconTreeListRenderer());
    this.setMaximumSize(new Dimension(0, 250));
    this.setRowHeight((int) (this.getRowHeight() * Editor.preferences().getUiScale()));
    for (int i = 0; i < this.getRowCount(); i++) {
      this.expandRow(i);
    }

    this.addTreeSelectionListener(e -> loadAssetsOfCurrentSelection(e.getPath()));
  }

  public void forceUpdate() {
    loadAssetsOfCurrentSelection(this.getSelectionPath());
  }

  @Override
  protected void setExpandedState(TreePath path, boolean state) {
    if (state) {
      super.setExpandedState(path, state);
    }
  }

  private void loadAssetsOfCurrentSelection(TreePath selectedPath) {
    if (selectedPath == null) {
      return;
    }

    final TreePath spritePath = new TreePath(this.nodeSpritesheets.getPath());
    final TreePath propPath = new TreePath(this.nodeSpriteProps.getPath());
    final TreePath creaturePath = new TreePath(this.nodeCreatures.getPath());
    final TreePath miscPath = new TreePath(this.nodeSpriteMisc.getPath());
    final TreePath tilesetPath = new TreePath(this.nodeTileSets.getPath());
    final TreePath emitterPath = new TreePath(this.nodeEmitters.getPath());
    final TreePath blueprintPath = new TreePath(this.nodeBlueprints.getPath());
    final TreePath soundPath = new TreePath(this.nodeSounds.getPath());

    final ResourceBundle gameFile = Editor.instance().getGameFile();
    if(gameFile == null) {
      return;
    }
    
    if (selectedPath.equals(spritePath)) {
      this.assetPanel.loadSprites(gameFile.getSpriteSheets().stream().collect(Collectors.toList()));
    } else if (this.getSelectionPath().equals(propPath)) {
      this.assetPanel.loadSprites(gameFile.getSpriteSheets().stream().filter(x -> x.getName() != null && x.getName().contains(PropAnimationController.PROP_IDENTIFIER)).collect(Collectors.toList()));
    } else if (this.getSelectionPath().equals(creaturePath)) {
      this.assetPanel.loadSprites(gameFile.getSpriteSheets().stream().filter(x -> x.getName() != null && CreaturePanel.getCreatureSpriteName(x.getName()) != null).collect(Collectors.toList()));
    } else if (selectedPath.equals(miscPath)) {
      this.assetPanel.loadSprites(gameFile.getSpriteSheets().stream().filter(x -> x.getName() != null && !x.getName().contains(PropAnimationController.PROP_IDENTIFIER) && CreaturePanel.getCreatureSpriteName(x.getName()) == null).collect(Collectors.toList()));
    } else if (selectedPath.equals(tilesetPath)) {
      ArrayList<Tileset> allTilesets = new ArrayList<>();
      allTilesets.addAll(gameFile.getTilesets().stream().filter(x -> x.getName() != null).collect(Collectors.toList()));

      for (TmxMap map : gameFile.getMaps()) {
        for (ITileset tileset : map.getTilesets()) {
          if (allTilesets.stream().anyMatch(x -> x.getName() != null && x.getName().equals(tileset.getName()))) {
            continue;
          }

          allTilesets.add((Tileset) tileset);
        }
      }

      this.assetPanel.loadTilesets(allTilesets);
    } else if (selectedPath.equals(emitterPath)) {
      this.assetPanel.loadEmitters(gameFile.getEmitters());
    } else if (selectedPath.equals(blueprintPath)) {
      this.assetPanel.loadBlueprints(gameFile.getBluePrints());
    } else if (selectedPath.equals(soundPath)) {
      this.assetPanel.loadSounds(gameFile.getSounds());
    }
  }
}

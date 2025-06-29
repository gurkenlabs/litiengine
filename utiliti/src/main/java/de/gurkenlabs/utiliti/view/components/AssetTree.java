package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.renderers.IconTreeListRenderer;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Objects;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

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

    this.nodeRoot = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_assets"), Icons.ASSET_8));
    this.nodeSpritesheets = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_spritesheets"), Icons.SPRITESHEET_24));
    this.nodeSpriteProps = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_spritesheets_props"), Icons.ENTITY_24));
    this.nodeSpriteMisc = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_spritesheets_misc"), Icons.MISC_24));
    this.nodeTileSets = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_tilesets"), Icons.TILESET_24));
    this.nodeSounds = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_sounds"), Icons.SOUND_24));
    this.nodeEmitters = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_emitters"), Icons.EMITTER_24));
    this.nodeBlueprints = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_blueprints"), Icons.BLUEPRINT_24));
    this.nodeCreatures = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_creatures"), Icons.CREATURE_24));

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
    this.setRowHeight((int) (24 * Editor.preferences().getUiScale()));
    for (int i = 0; i < getRowCount(); i++) {
      this.expandRow(i);
    }

    this.addTreeSelectionListener(e -> loadAssetsOfCurrentSelection(e.getPath()));
  }

  public void forceUpdate() {
    loadAssetsOfCurrentSelection(getSelectionPath());
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
    if (gameFile == null) {
      return;
    }

    if (selectedPath.equals(spritePath)) {
      this.assetPanel.loadSprites(new ArrayList<>(gameFile.getSpriteSheets()));
    } else if (Objects.equals(getSelectionPath(), propPath)) {
      this.assetPanel.loadSprites(
        gameFile.getSpriteSheets().stream().filter(x -> x.getName() != null && x.getName().contains(PropAnimationController.PROP_IDENTIFIER))
          .toList());
    } else if (Objects.equals(getSelectionPath(), creaturePath)) {
      this.assetPanel.loadSprites(
        gameFile.getSpriteSheets().stream().filter(x -> x.getName() != null && CreaturePanel.getCreatureSpriteName(x.getName()) != null).toList());
    } else if (selectedPath.equals(miscPath)) {
      this.assetPanel.loadSprites(gameFile.getSpriteSheets().stream().filter(
        x -> x.getName() != null && !x.getName().contains(PropAnimationController.PROP_IDENTIFIER)
          && CreaturePanel.getCreatureSpriteName(x.getName()) == null).toList());
    } else if (selectedPath.equals(tilesetPath)) {
      ArrayList<Tileset> allTilesets = new ArrayList<>(gameFile.getTilesets().stream().filter(x -> x.getName() != null).toList());

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

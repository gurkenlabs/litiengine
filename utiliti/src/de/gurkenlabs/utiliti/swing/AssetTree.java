package de.gurkenlabs.utiliti.swing;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.gurkenlabs.litiengine.GameFile;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.utiliti.EditorScreen;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.swing.panels.CreaturePanel;

@SuppressWarnings("serial")
public class AssetTree extends JTree {
  private static final Icon ASSET_ICON = new ImageIcon(Resources.getImage("asset.png"));
  private static final Icon SPRITESHEET_ICON = new ImageIcon(Resources.getImage("spritesheet.png"));
  private static final Icon PROP_ICON = new ImageIcon(Resources.getImage("entity.png"));
  private static final Icon CREATURE_ICON = new ImageIcon(Resources.getImage("creature.png"));
  private static final Icon MISC_ICON = new ImageIcon(Resources.getImage("misc.png"));
  private static final Icon TILESET_ICON = new ImageIcon(Resources.getImage("tileset.png"));
  private static final Icon EMITTER_ICON = new ImageIcon(Resources.getImage("emitter.png"));
  private static final Icon BLUEPRINT_ICON = new ImageIcon(Resources.getImage("blueprint.png"));

  private final DefaultTreeModel entitiesTreeModel;
  private final DefaultMutableTreeNode nodeRoot;
  private final DefaultMutableTreeNode nodeSpritesheets;
  private final DefaultMutableTreeNode nodeSpriteProps;
  private final DefaultMutableTreeNode nodeSpriteMisc;
  private final DefaultMutableTreeNode nodeTileSets;
  private final DefaultMutableTreeNode nodeEmitters;
  private final DefaultMutableTreeNode nodeBlueprints;
  private final DefaultMutableTreeNode nodeCreatures;

  public AssetTree() {
    this.nodeRoot = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_assets"), ASSET_ICON));
    this.nodeSpritesheets = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_spritesheets"), SPRITESHEET_ICON));
    this.nodeSpriteProps = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_spritesheets_props"), PROP_ICON));
    this.nodeSpriteMisc = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_spritesheets_misc"), MISC_ICON));
    this.nodeTileSets = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_tilesets"), TILESET_ICON));
    this.nodeEmitters = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_emitters"), EMITTER_ICON));
    this.nodeBlueprints = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_blueprints"), BLUEPRINT_ICON));
    this.nodeCreatures = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_creatures"), CREATURE_ICON));
    
    this.nodeSpritesheets.add(this.nodeSpriteProps);
    this.nodeSpritesheets.add(this.nodeCreatures);
    this.nodeSpritesheets.add(this.nodeSpriteMisc);

    this.nodeRoot.add(this.nodeSpritesheets);
    this.nodeRoot.add(this.nodeEmitters);
    this.nodeRoot.add(this.nodeBlueprints);
    this.nodeRoot.add(this.nodeTileSets);

    this.entitiesTreeModel = new DefaultTreeModel(this.nodeRoot);

    this.setModel(this.entitiesTreeModel);
    this.setCellRenderer(new IconTreeListRenderer());
    this.setMaximumSize(new Dimension(0, 250));
    for (int i = 0; i < this.getRowCount(); i++) {
      this.expandRow(i);
    }

    this.addTreeSelectionListener(e -> {
      loadAssetsOfCurrentSelection(e.getPath());
    });
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

    final GameFile gameFile = EditorScreen.instance().getGameFile();
    if (selectedPath.equals(spritePath)) {
      Program.getAssetPanel().loadSprites(gameFile.getSpriteSheets().stream().collect(Collectors.toList()));
    } else if (this.getSelectionPath().equals(propPath)) {
      Program.getAssetPanel().loadSprites(gameFile.getSpriteSheets().stream().filter(x -> x.getName() != null && x.getName().contains(Program.PROP_SPRITE_PREFIX)).collect(Collectors.toList()));
    } else if (this.getSelectionPath().equals(creaturePath)) {
      Program.getAssetPanel().loadSprites(gameFile.getSpriteSheets().stream().filter(x -> x.getName() != null && CreaturePanel.getCreatureSpriteName(x.getName()) != null).collect(Collectors.toList()));
    }else if (selectedPath.equals(miscPath)) {
      Program.getAssetPanel().loadSprites(gameFile.getSpriteSheets().stream().filter(x -> x.getName() != null && !x.getName().contains(Program.PROP_SPRITE_PREFIX) && CreaturePanel.getCreatureSpriteName(x.getName()) == null).collect(Collectors.toList()));
    } else if (selectedPath.equals(tilesetPath)) {
      ArrayList<Tileset> allTilesets = new ArrayList<>();
      allTilesets.addAll(gameFile.getTilesets().stream().filter(x -> x.getName() != null).collect(Collectors.toList()));

      for (Map map : gameFile.getMaps()) {
        for (ITileset tileset : map.getTilesets()) {
          if (allTilesets.stream().anyMatch(x -> x.getName() != null && x.getName().equals(tileset.getName()))) {
            continue;
          }

          allTilesets.add((Tileset) tileset);
        }
      }

      Program.getAssetPanel().loadTilesets(allTilesets);
    } else if (selectedPath.equals(emitterPath)) {
      Program.getAssetPanel().loadEmitters(gameFile.getEmitters());
    } else if (selectedPath.equals(blueprintPath)) {
      Program.getAssetPanel().loadBlueprints(gameFile.getBluePrints());
    }
  }
}

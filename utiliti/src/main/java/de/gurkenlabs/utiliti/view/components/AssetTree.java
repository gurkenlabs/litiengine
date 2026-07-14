package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.SpriteVariantSelector;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.renderers.IconTreeListRenderer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
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
  private final DefaultMutableTreeNode nodeAnimations;
  private String currentBreadcrumb = "Resources";
  private int hoveredRow = -1;

  public AssetTree(AssetPanel assetPanel) {
    this.setRootVisible(false);
    this.setShowsRootHandles(true);
    this.setBackground(Style.surface());
    this.getAccessibleContext().setAccessibleName("Resource categories");

    this.assetPanel = assetPanel;

    this.nodeRoot = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_assets"), Icons.ASSET_8));
    this.nodeSpritesheets = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_spritesheets"), Icons.SPRITESHEET_24));
    this.nodeSpriteProps = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_spritesheets_props"), Icons.PROP_24));
    this.nodeSpriteMisc = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_spritesheets_misc"), Icons.MISC_24));
    this.nodeTileSets = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_tilesets"), Icons.TILESET_24));
    this.nodeSounds = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_sounds"), Icons.SOUND_24));
    this.nodeEmitters = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_emitters"), Icons.EMITTER_24));
    this.nodeBlueprints = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_blueprints"), Icons.BLUEPRINT_24));
    this.nodeCreatures = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_creatures"), Icons.CREATURE_24));
    this.nodeAnimations = new DefaultMutableTreeNode(new IconTreeListItem(Resources.strings().get("assettree_animations"), Icons.ANIMATION_24));

    this.nodeSpritesheets.add(this.nodeSpriteProps);
    this.nodeSpritesheets.add(this.nodeCreatures);
    this.nodeSpritesheets.add(this.nodeSpriteMisc);

    this.nodeRoot.add(this.nodeSpritesheets);
    this.nodeRoot.add(this.nodeEmitters);
    this.nodeRoot.add(this.nodeBlueprints);
    this.nodeRoot.add(this.nodeTileSets);
    this.nodeRoot.add(this.nodeSounds);
    this.nodeRoot.add(this.nodeAnimations);

    this.entitiesTreeModel = new DefaultTreeModel(this.nodeRoot);

    this.setModel(this.entitiesTreeModel);
    this.setCellRenderer(new IconTreeListRenderer() {
      @Override
      public Component getTreeCellRendererComponent(
          JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (component instanceof JComponent cell) {
          cell.setForeground(Style.text());
          cell.setBackground(selected ? Style.selection() : row == hoveredRow ? Style.hover() : Style.surface());
          cell.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(hasFocus ? Style.accent() : cell.getBackground()),
            BorderFactory.createEmptyBorder(1, 5, 1, 5)));
        }
        return component;
      }
    });
    this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
    this.setRowHeight((int) (Style.TREE_ROW_HEIGHT * Editor.preferences().getUiScale()));
    for (int i = 0; i < getRowCount(); i++) {
      this.expandRow(i);
    }

    this.addTreeSelectionListener(e -> loadAssetsOfCurrentSelection(e.getPath()));
    this.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        int row = getRowForLocation(e.getX(), e.getY());
        if (row != hoveredRow) {
          hoveredRow = row;
          repaint();
        }
      }
    });
    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseExited(MouseEvent e) {
        hoveredRow = -1;
        repaint();
      }
    });
  }

  @Override
  public void updateUI() {
    super.updateUI();
    setBackground(Style.surface());
  }

  public void forceUpdate() {
    if (getSelectionPath() == null) {
      selectDefault();
      return;
    }
    loadAssetsOfCurrentSelection(getSelectionPath());
  }

  public void selectDefault() {
    TreePath propPath = new TreePath(this.nodeSpriteProps.getPath());
    setSelectionPath(propPath);
    scrollPathToVisible(propPath);
    loadAssetsOfCurrentSelection(propPath);
  }

  public String getCurrentBreadcrumb() {
    return this.currentBreadcrumb;
  }

  private void loadAssetsOfCurrentSelection(TreePath selectedPath) {
    if (selectedPath == null) {
      return;
    }
    this.currentBreadcrumb = breadcrumb(selectedPath);

    // Precompute TreePaths once
    final TreePath spritePath = new TreePath(this.nodeSpritesheets.getPath());
    final TreePath propPath = new TreePath(this.nodeSpriteProps.getPath());
    final TreePath creaturePath = new TreePath(this.nodeCreatures.getPath());
    final TreePath miscPath = new TreePath(this.nodeSpriteMisc.getPath());
    final TreePath tilesetPath = new TreePath(this.nodeTileSets.getPath());
    final TreePath emitterPath = new TreePath(this.nodeEmitters.getPath());
    final TreePath blueprintPath = new TreePath(this.nodeBlueprints.getPath());
    final TreePath soundPath = new TreePath(this.nodeSounds.getPath());
    final TreePath animationPath = new TreePath(this.nodeAnimations.getPath());

    // Animations live in the engine's in-memory resource container rather than the resource bundle,
    // so they can be displayed even when no project (game file) is loaded yet.
    if (selectedPath.equals(animationPath)) {
      this.assetPanel.loadAnimations(new ArrayList<>(Resources.animations().getAll()));
      return;
    }

    final ResourceBundle gameFile = Editor.instance().getGameFile();
    if (gameFile == null) {
      return;
    }

    if (selectedPath.equals(spritePath)) {
      this.assetPanel.loadSprites(getAllBaseAndMisc(gameFile));
      return;
    }
    if (selectedPath.equals(propPath)) {
      this.assetPanel.loadSprites(getBasePropSprites(gameFile));
      return;
    }
    if (selectedPath.equals(creaturePath)) {
      this.assetPanel.loadSprites(getBaseCreatureSprites(gameFile));
      return;
    }
    if (selectedPath.equals(miscPath)) {
      this.assetPanel.loadSprites(getMiscSprites(gameFile));
      return;
    }
    if (selectedPath.equals(tilesetPath)) {
      this.assetPanel.loadTilesets(collectAllTilesets(gameFile));
      return;
    }
    if (selectedPath.equals(emitterPath)) {
      this.assetPanel.loadEmitters(gameFile.getEmitters());
      return;
    }
    if (selectedPath.equals(blueprintPath)) {
      this.assetPanel.loadBlueprints(gameFile.getBluePrints());
      return;
    }
    if (selectedPath.equals(soundPath)) {
      this.assetPanel.loadSounds(gameFile.getSounds());
    }
  }

  private static String breadcrumb(TreePath path) {
    Object[] parts = path.getPath();
    StringBuilder sb = new StringBuilder("Resources");
    for (int i = 1; i < parts.length; i++) {
      Object part = parts[i];
      if (part instanceof DefaultMutableTreeNode node) {
        sb.append("  ›  ").append(node.getUserObject());
      }
    }
    return sb.toString();
  }

  // --- Sprite classification helpers -----------------------------------------------------------
  private boolean isPropSprite(String name) {
    return name != null && name.contains(PropAnimationController.PROP_IDENTIFIER);
  }

  private boolean isCreatureSprite(String name) {
    return name != null && CreaturePanel.getCreatureSpriteName(name) != null;
  }

  private List<SpritesheetResource> getBasePropSprites(ResourceBundle gameFile) {
    return new ArrayList<>(SpriteVariantSelector.selectBasePropResources(gameFile.getSpriteSheets()).values());
  }

  private List<SpritesheetResource> getBaseCreatureSprites(ResourceBundle gameFile) {
    return new ArrayList<>(SpriteVariantSelector.selectBaseCreatureResources(gameFile.getSpriteSheets()).values());
  }

  private List<SpritesheetResource> getMiscSprites(ResourceBundle gameFile) {
    List<SpritesheetResource> misc = new ArrayList<>();
    for (SpritesheetResource res : gameFile.getSpriteSheets()) {
      String name = res.getName();
      if (name == null) {
        continue;
      }
      if (!isPropSprite(name) && !isCreatureSprite(name) && !isTilesetSprite(gameFile, name)) {
        misc.add(res);
      }
    }
    return misc;
  }

  private static boolean isTilesetSprite(ResourceBundle gameFile, String spriteName) {
    if (gameFile == null || spriteName == null) {
      return false;
    }
    return isTilesetSpriteName(spriteName, collectTilesetSpriteKeys(gameFile));
  }

  static boolean isTilesetSpriteName(String spriteName, Set<String> tilesetSpriteKeys) {
    if (spriteName == null || tilesetSpriteKeys == null || tilesetSpriteKeys.isEmpty()) {
      return false;
    }
    Set<String> spriteKeys = normalizedSpriteKeys(spriteName);
    return spriteKeys.stream().anyMatch(tilesetSpriteKeys::contains);
  }

  static Set<String> collectTilesetSpriteKeys(ResourceBundle gameFile) {
    Set<String> keys = new HashSet<>();
    if (gameFile == null) {
      return keys;
    }
    for (Tileset tileset : gameFile.getTilesets()) {
      addTilesetSpriteKeys(keys, tileset);
    }
    for (TmxMap map : gameFile.getMaps()) {
      for (ITileset tileset : map.getTilesets()) {
        addTilesetSpriteKeys(keys, tileset);
      }
    }
    return keys;
  }

  private static void addTilesetSpriteKeys(Set<String> keys, ITileset tileset) {
    if (tileset == null || tileset.getImage() == null) {
      return;
    }
    keys.addAll(normalizedSpriteKeys(tileset.getImage().getSource()));
  }

  private static Set<String> normalizedSpriteKeys(String source) {
    Set<String> keys = new HashSet<>();
    if (source == null || source.isBlank()) {
      return keys;
    }
    String normalized = source.replace('\\', '/').toLowerCase(Locale.ROOT);
    keys.add(normalized);
    int slash = normalized.lastIndexOf('/');
    String fileName = slash >= 0 ? normalized.substring(slash + 1) : normalized;
    keys.add(fileName);
    int dot = fileName.lastIndexOf('.');
    if (dot > 0) {
      keys.add(fileName.substring(0, dot));
      keys.add(normalized.substring(0, normalized.length() - (fileName.length() - dot)));
    }
    return keys;
  }

  private List<SpritesheetResource> getAllBaseAndMisc(ResourceBundle gameFile) {
    List<SpritesheetResource> aggregated = new ArrayList<>();
    aggregated.addAll(getBasePropSprites(gameFile));
    aggregated.addAll(getBaseCreatureSprites(gameFile));
    aggregated.addAll(getMiscSprites(gameFile));
    return aggregated;
  }

  // --- Tileset aggregation (unchanged logic, grouped for clarity) ------------------------------
  private List<Tileset> collectAllTilesets(ResourceBundle gameFile) {
    List<Tileset> all = new ArrayList<>(gameFile.getTilesets().stream().filter(x -> x.getName() != null).toList());
    for (TmxMap map : gameFile.getMaps()) {
      for (ITileset tileset : map.getTilesets()) {
        String name = tileset.getName();
        if (name == null) {
          continue;
        }
        boolean alreadyPresent = all.stream().anyMatch(x -> Objects.equals(x.getName(), name));
        if (!alreadyPresent) {
          all.add((Tileset) tileset);
        }
      }
    }
    return all;
  }
}

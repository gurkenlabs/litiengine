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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class AssetTree extends JTree {
  private final AssetPanel assetPanel;
  private final DefaultTreeModel entitiesTreeModel;
  private final DefaultMutableTreeNode nodeRoot;
  private final DefaultMutableTreeNode nodeSpritesheets;
  private final DefaultMutableTreeNode nodeResources;
  private final DefaultMutableTreeNode nodeSpriteProps;
  private final DefaultMutableTreeNode nodeSpriteMisc;
  private final DefaultMutableTreeNode nodeTileSets;
  private final DefaultMutableTreeNode nodeSounds;
  private final DefaultMutableTreeNode nodeEmitters;
  private final DefaultMutableTreeNode nodeBlueprints;
  private final DefaultMutableTreeNode nodeCreatures;
  private final DefaultMutableTreeNode nodeAnimations;
  private final Map<DefaultMutableTreeNode, Integer> categoryCounts = new IdentityHashMap<>();
  private int hoveredRow = -1;

  public AssetTree(AssetPanel assetPanel) {
    this.setRootVisible(false);
    this.setShowsRootHandles(true);
    this.setBackground(Style.assetExplorerBackground());
    this.setOpaque(false);
    this.putClientProperty("JTree.lineStyle", "None");
    this.getAccessibleContext().setAccessibleName("Resource categories");

    this.assetPanel = assetPanel;

    this.nodeRoot = categoryNode(Resources.strings().get("assettree_assets"), Icons.ASSET_8, true);
    this.nodeSpritesheets = categoryNode(Resources.strings().get("assettree_spritesheets"), Icons.SPRITESHEET_16, true);
    this.nodeResources = categoryNode("Resources", Icons.ASSET_16, true);
    this.nodeSpriteProps = categoryNode(Resources.strings().get("assettree_spritesheets_props"), Icons.PROP_16, false);
    this.nodeSpriteMisc = categoryNode(Resources.strings().get("assettree_spritesheets_misc"), Icons.MISC_16, false);
    this.nodeTileSets = categoryNode(Resources.strings().get("assettree_tilesets"), Icons.TILESET_16, false);
    this.nodeSounds = categoryNode(Resources.strings().get("assettree_sounds"), Icons.SOUND_16, false);
    this.nodeEmitters = categoryNode(Resources.strings().get("assettree_emitters"), Icons.EMITTER_16, false);
    this.nodeBlueprints = categoryNode(Resources.strings().get("assettree_blueprints"), Icons.BLUEPRINT_16, false);
    this.nodeCreatures = categoryNode(Resources.strings().get("assettree_creatures"), Icons.CREATURE_16, false);
    this.nodeAnimations = categoryNode(Resources.strings().get("assettree_animations"), Icons.ANIMATION_16, false);

    this.nodeSpritesheets.add(this.nodeSpriteProps);
    this.nodeSpritesheets.add(this.nodeCreatures);
    this.nodeSpritesheets.add(this.nodeSpriteMisc);

    this.nodeRoot.add(this.nodeSpritesheets);
    this.nodeResources.add(this.nodeEmitters);
    this.nodeResources.add(this.nodeBlueprints);
    this.nodeResources.add(this.nodeTileSets);
    this.nodeResources.add(this.nodeSounds);
    this.nodeResources.add(this.nodeAnimations);
    this.nodeRoot.add(this.nodeResources);

    this.entitiesTreeModel = new DefaultTreeModel(this.nodeRoot);

    this.setModel(this.entitiesTreeModel);
    this.setCellRenderer(new AssetCategoryRenderer());
    this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
    this.setRowHeight((int) (Style.TREE_ROW_HEIGHT * Editor.preferences().getUiScale()));
    refreshCounts();
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
    setBackground(Style.assetExplorerBackground());
    setOpaque(false);
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    graphics.setColor(Style.assetExplorerBackground());
    graphics.fillRect(0, 0, getWidth(), getHeight());
    super.paintComponent(graphics);
  }

  public void forceUpdate() {
    refreshCounts();
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

  private void loadAssetsOfCurrentSelection(TreePath selectedPath) {
    if (selectedPath == null) {
      return;
    }

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

  private static DefaultMutableTreeNode categoryNode(String label, javax.swing.Icon icon, boolean group) {
    return new DefaultMutableTreeNode(new AssetCategory(label, icon, group));
  }

  private void refreshCounts() {
    this.categoryCounts.clear();
    ResourceBundle gameFile = Editor.instance().getGameFile();
    if (gameFile != null) {
      this.categoryCounts.put(this.nodeSpriteProps, getBasePropSprites(gameFile).size());
      this.categoryCounts.put(this.nodeCreatures, getBaseCreatureSprites(gameFile).size());
      this.categoryCounts.put(this.nodeSpriteMisc, getMiscSprites(gameFile).size());
      this.categoryCounts.put(this.nodeEmitters, gameFile.getEmitters().size());
      this.categoryCounts.put(this.nodeBlueprints, gameFile.getBluePrints().size());
      this.categoryCounts.put(this.nodeTileSets, collectAllTilesets(gameFile).size());
      this.categoryCounts.put(this.nodeSounds, gameFile.getSounds().size());
    }
    this.categoryCounts.put(this.nodeAnimations, Resources.animations().getAll().size());
    repaint();
  }

  private record AssetCategory(String label, javax.swing.Icon icon, boolean group) {
    @Override
    public String toString() {
      return this.label;
    }
  }

  private final class AssetCategoryRenderer extends JPanel implements TreeCellRenderer {
    private final JLabel name = new JLabel();
    private final JLabel count = new JLabel();
    private boolean selectedRow;
    private boolean hovered;

    private AssetCategoryRenderer() {
      super(new BorderLayout(8, 0));
      setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 8));
      this.name.setIconTextGap(8);
      this.count.setHorizontalAlignment(JLabel.RIGHT);
      add(this.name, BorderLayout.CENTER);
      add(this.count, BorderLayout.EAST);
    }

    @Override
    public Component getTreeCellRendererComponent(
        JTree tree,
        Object value,
        boolean selected,
        boolean expanded,
        boolean leaf,
        int row,
        boolean hasFocus) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
      AssetCategory category = (AssetCategory) node.getUserObject();
      this.name.setText(category.label());
      this.name.setIcon(category.icon());
      this.name.setFont(tree.getFont().deriveFont(category.group() ? Font.BOLD : Font.PLAIN));
      this.count.setFont(tree.getFont().deriveFont(Font.PLAIN));
      this.count.setText(category.group() ? "" : Integer.toString(categoryCounts.getOrDefault(node, 0)));

      java.awt.Color foreground = Style.text();
      this.name.setForeground(foreground);
      this.count.setForeground(selected ? Style.text() : Style.mutedText());
      this.selectedRow = selected;
      this.hovered = row == hoveredRow;
      setBackground(Style.assetExplorerBackground());
      setOpaque(true);

      int depthInset = Math.max(0, node.getLevel() - 1) * 20;
      int width = Math.max(120, tree.getWidth() - 34 - depthInset);
      setPreferredSize(new Dimension(width, tree.getRowHeight()));
      return this;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);
      if (this.selectedRow || this.hovered) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        try {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(this.selectedRow ? Style.raisedSurface() : Style.hover());
          g2.fillRoundRect(
              0,
              1,
              getWidth(),
              Math.max(0, getHeight() - 2),
              Style.CORNER_RADIUS * 2,
              Style.CORNER_RADIUS * 2);
        } finally {
          g2.dispose();
        }
      }
    }
  }
}

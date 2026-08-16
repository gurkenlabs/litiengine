package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.Editor;
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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class ScriptAssetTree extends JTree {
  private final AssetPanel assetPanel;
  private final DefaultTreeModel treeModel;
  private final DefaultMutableTreeNode nodeRoot;
  private final DefaultMutableTreeNode nodeAll;
  private final DefaultMutableTreeNode nodeEntity;
  private final DefaultMutableTreeNode nodeEnvironment;
  private final DefaultMutableTreeNode nodeGame;
  private final DefaultMutableTreeNode nodeSources;
  private final Map<DefaultMutableTreeNode, Integer> categoryCounts = new IdentityHashMap<>();
  private int hoveredRow = -1;

  public ScriptAssetTree(AssetPanel assetPanel) {
    this.setRootVisible(false);
    this.setShowsRootHandles(true);
    this.setBackground(Style.assetExplorerBackground());
    this.setOpaque(true);
    this.putClientProperty("JTree.lineStyle", "None");

    this.assetPanel = assetPanel;

    this.nodeRoot = categoryNode("Scripts", Icons.SCRIPT_16, true);
    this.nodeAll = categoryNode("All Scripts", Icons.SCRIPT_16, false);
    this.nodeEntity = categoryNode("Entity Scripts", Icons.CREATURE_16, false);
    this.nodeEnvironment = categoryNode("Environment Scripts", Icons.MAPAREA_16, false);
    this.nodeGame = categoryNode("Game Scripts", Icons.PLAY_16, false);
    this.nodeSources = categoryNode("Project Sources", Icons.PACKAGE_16, false);

    this.nodeRoot.add(this.nodeAll);
    this.nodeRoot.add(this.nodeEntity);
    this.nodeRoot.add(this.nodeEnvironment);
    this.nodeRoot.add(this.nodeGame);
    this.nodeRoot.add(this.nodeSources);

    this.treeModel = new DefaultTreeModel(this.nodeRoot);
    this.setModel(this.treeModel);
    this.setCellRenderer(new ScriptCategoryRenderer());
    this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
    this.setRowHeight((int) (Style.TREE_ROW_HEIGHT * Editor.preferences().getUiScale()));

    this.refreshCounts();
    for (int i = 0; i < getRowCount(); i++) {
      this.expandRow(i);
    }

    this.addTreeSelectionListener(e -> loadScriptsOfCurrentSelection(e.getPath()));
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

      @Override
      public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
      }
    });
  }

  @Override
  public void updateUI() {
    super.updateUI();
    setBackground(Style.assetExplorerBackground());
    setOpaque(true);
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    graphics.setColor(Style.assetExplorerBackground());
    graphics.fillRect(0, 0, getWidth(), getHeight());
    super.paintComponent(graphics);
  }

  public void forceUpdate() {
    refreshCounts();
    if (Editor.instance().getGameFile() == null) {
      this.assetPanel.clearAssets();
      return;
    }
    if (getSelectionPath() == null) {
      selectDefault();
      return;
    }
    loadScriptsOfCurrentSelection(getSelectionPath());
  }

  public void selectDefault() {
    TreePath allPath = new TreePath(this.nodeAll.getPath());
    setSelectionPath(allPath);
    scrollPathToVisible(allPath);
    loadScriptsOfCurrentSelection(allPath);
  }

  private void loadScriptsOfCurrentSelection(TreePath selectedPath) {
    if (selectedPath == null || Editor.instance().getGameFile() == null) {
      return;
    }

    ResourceBundle gameFile = Editor.instance().getGameFile();
    List<ScriptDefinition> allScripts = gameFile.getScripts();
    if (allScripts == null) {
      this.assetPanel.clearAssets();
      return;
    }

    Object userObj = ((DefaultMutableTreeNode) selectedPath.getLastPathComponent()).getUserObject();
    if (!(userObj instanceof ScriptCategory category)) return;

    if (selectedPath.getLastPathComponent() == this.nodeAll) {
      this.assetPanel.loadScripts(new ArrayList<>(allScripts));
    } else if (selectedPath.getLastPathComponent() == this.nodeEntity) {
      this.assetPanel.loadScripts(allScripts.stream()
          .filter(s -> s.getHost() == ScriptHostType.ENTITY).toList());
    } else if (selectedPath.getLastPathComponent() == this.nodeEnvironment) {
      this.assetPanel.loadScripts(allScripts.stream()
          .filter(s -> s.getHost() == ScriptHostType.ENVIRONMENT).toList());
    } else if (selectedPath.getLastPathComponent() == this.nodeGame) {
      this.assetPanel.loadScripts(allScripts.stream()
          .filter(s -> s.getHost() == ScriptHostType.GAME).toList());
    } else if (selectedPath.getLastPathComponent() == this.nodeSources) {
      List<ScriptDefinition> sources = new ArrayList<>();
      if (Editor.instance().getProjectCodeIntegration() != null) {
        sources.addAll(Editor.instance().getProjectCodeIntegration().getScriptDefinitions().stream()
            .map(desc -> {
              ScriptDefinition def = new ScriptDefinition(desc.id(), "java", desc.id(), desc.id(), desc.host());
              def.setName(desc.id());
              return def;
            }).toList());
      }
      this.assetPanel.loadScripts(sources);
    }
  }

  public void refreshCounts() {
    this.categoryCounts.clear();
    ResourceBundle gameFile = Editor.instance().getGameFile();
    if (gameFile != null && gameFile.getScripts() != null) {
      List<ScriptDefinition> scripts = gameFile.getScripts();
      int total = scripts.size();
      int entityCount = (int) scripts.stream().filter(s -> s.getHost() == ScriptHostType.ENTITY).count();
      int envCount = (int) scripts.stream().filter(s -> s.getHost() == ScriptHostType.ENVIRONMENT).count();
      int gameCount = (int) scripts.stream().filter(s -> s.getHost() == ScriptHostType.GAME).count();
      int sourceCount = Editor.instance().getProjectCodeIntegration() != null
          ? Editor.instance().getProjectCodeIntegration().getScriptDefinitions().size()
          : 0;

      this.categoryCounts.put(this.nodeAll, total);
      this.categoryCounts.put(this.nodeEntity, entityCount);
      this.categoryCounts.put(this.nodeEnvironment, envCount);
      this.categoryCounts.put(this.nodeGame, gameCount);
      this.categoryCounts.put(this.nodeSources, sourceCount);
    }
    repaint();
  }

  private void maybeShowPopup(MouseEvent e) {
    if (!e.isPopupTrigger()) return;
    int row = getRowForLocation(e.getX(), e.getY());
    if (row >= 0) setSelectionRow(row);

    JPopupMenu menu = new JPopupMenu();
    JMenuItem newEntity = new JMenuItem("New Entity Script...", Icons.CREATURE_16);
    newEntity.addActionListener(evt -> {
      if (UI.getScriptWorkspacePanel() != null) {
        UI.getScriptWorkspacePanel().createScript(ScriptWorkspacePanel.ScriptKind.ENTITY);
      }
    });
    JMenuItem newEnv = new JMenuItem("New Environment Script...", Icons.MAPAREA_16);
    newEnv.addActionListener(evt -> {
      if (UI.getScriptWorkspacePanel() != null) {
        UI.getScriptWorkspacePanel().createScript(ScriptWorkspacePanel.ScriptKind.ENVIRONMENT);
      }
    });
    JMenuItem newGame = new JMenuItem("New Game Script...", Icons.PLAY_16);
    newGame.addActionListener(evt -> {
      if (UI.getScriptWorkspacePanel() != null) {
        UI.getScriptWorkspacePanel().createScript(ScriptWorkspacePanel.ScriptKind.GAME);
      }
    });
    JMenuItem refresh = new JMenuItem("Refresh Scripts", Icons.RELOAD_16);
    refresh.addActionListener(evt -> forceUpdate());

    menu.add(newEntity);
    menu.add(newEnv);
    menu.add(newGame);
    menu.addSeparator();
    menu.add(refresh);
    menu.show(this, e.getX(), e.getY());
  }

  private static DefaultMutableTreeNode categoryNode(String label, javax.swing.Icon icon, boolean group) {
    return new DefaultMutableTreeNode(new ScriptCategory(label, icon, group));
  }

  private record ScriptCategory(String label, javax.swing.Icon icon, boolean group) {
    @Override
    public String toString() {
      return this.label;
    }
  }

  private final class ScriptCategoryRenderer extends JPanel implements TreeCellRenderer {
    private final JLabel name = new JLabel();
    private final JLabel count = new JLabel();
    private boolean selectedRow;
    private boolean hovered;

    private ScriptCategoryRenderer() {
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
      ScriptCategory category = (ScriptCategory) node.getUserObject();
      this.name.setText(category.label());
      this.name.setIcon(category.icon());
      this.name.setFont(tree.getFont().deriveFont(category.group() ? Font.BOLD : Font.PLAIN));
      this.count.setFont(tree.getFont().deriveFont(Font.PLAIN));
      this.count.setText(category.group() ? "" : Resources.strings().get(
          "assettree_category_count", categoryCounts.getOrDefault(node, 0)));

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

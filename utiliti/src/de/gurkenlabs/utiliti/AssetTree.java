package de.gurkenlabs.utiliti;

import java.awt.Dimension;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import de.gurkenlabs.litiengine.Resources;

public class AssetTree extends JTree {
  private static final long serialVersionUID = -1889916538755378262L;
  private static final Icon ASSET_ICON = new ImageIcon(Resources.getImage("asset.png"));
  private static final Icon SPRITESHEET_ICON = new ImageIcon(Resources.getImage("spritesheet.png"));
  private static final Icon PROP_ICON = new ImageIcon(Resources.getImage("entity.png"));
  private static final Icon MISC_ICON = new ImageIcon(Resources.getImage("misc.png"));

  private final DefaultTreeModel entitiesTreeModel;
  private final DefaultMutableTreeNode nodeRoot;
  private final DefaultMutableTreeNode nodeSpritesheets;
  private final DefaultMutableTreeNode nodeSpriteProps;
  private final DefaultMutableTreeNode nodeSpriteMisc;

  public AssetTree() {
    this.nodeRoot = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_assets"), ASSET_ICON));
    this.nodeSpritesheets = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_spritesheets"), SPRITESHEET_ICON));
    this.nodeSpriteProps = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_spritesheets_props"), PROP_ICON));
    this.nodeSpriteMisc = new DefaultMutableTreeNode(new IconTreeListItem(Resources.get("assettree_spritesheets_misc"), MISC_ICON));
    this.nodeSpritesheets.add(this.nodeSpriteProps);
    this.nodeSpritesheets.add(this.nodeSpriteMisc);

    this.nodeRoot.add(nodeSpritesheets);

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

  @Override
  protected void setExpandedState(TreePath path, boolean state) {
    if (state) {
      super.setExpandedState(path, state);
    }
  }

  private void loadAssetsOfCurrentSelection(TreePath selectedPath) {
    final TreePath propPath = new TreePath(this.nodeSpriteProps.getPath());
    final TreePath miscPath = new TreePath(this.nodeSpriteMisc.getPath());
    if (selectedPath != null && this.getSelectionPath().equals(propPath)) {
      Program.getAssetPanel().load(EditorScreen.instance().getGameFile().getSpriteSheets().stream().filter(x -> x.getName() != null && x.getName().contains("prop-")).collect(Collectors.toList()));
    } else if (selectedPath != null && getSelectionPath().equals(miscPath)) {
      Program.getAssetPanel().load(EditorScreen.instance().getGameFile().getSpriteSheets().stream().filter(x -> x.getName() != null && !x.getName().contains("prop-")).collect(Collectors.toList()));
    }
  }
}

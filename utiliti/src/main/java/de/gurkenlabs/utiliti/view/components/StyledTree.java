package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Standardized tree component sharing SceneGraph hierarchy rendering,
 * smooth hover/selection row backgrounds, and focus isolation across Utiliti.
 */
public class StyledTree extends JTree {
  private boolean paintingBaseRows;

  public StyledTree() {
    this(new DefaultTreeModel(new DefaultMutableTreeNode("root")));
  }

  public StyledTree(TreeModel model) {
    super(model);
    this.initStyledTree();
  }

  private void initStyledTree() {
    this.setRootVisible(false);
    this.setShowsRootHandles(true);
    this.setBackground(Style.background());
    this.setOpaque(false);
    this.putClientProperty("JTree.lineStyle", "None");
    this.setRowHeight((int) (Style.TREE_ROW_HEIGHT * Editor.preferences().getUiScale()));

    this.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        int row = getRowForLocation(e.getX(), e.getY());
        Object prev = getClientProperty("hoverRow");
        if (!Objects.equals(prev, row)) {
          putClientProperty("hoverRow", row >= 0 ? row : null);
          repaint();
        }
      }
    });

    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseExited(MouseEvent e) {
        if (getClientProperty("hoverRow") != null) {
          putClientProperty("hoverRow", null);
          repaint();
        }
      }
    });
  }

  @Override
  public boolean isPathSelected(TreePath path) {
    return !this.paintingBaseRows && super.isPathSelected(path);
  }

  @Override
  public boolean isRowSelected(int row) {
    return !this.paintingBaseRows && super.isRowSelected(row);
  }

  @Override
  public boolean hasFocus() {
    return !this.paintingBaseRows && super.hasFocus();
  }

  @Override
  public int getLeadSelectionRow() {
    if (this.paintingBaseRows) return -1;
    TreePath leadPath = super.getLeadSelectionPath();
    if (leadPath != null) {
      int row = this.getRowForPath(leadPath);
      if (row >= 0) return row;
    }
    TreePath selPath = super.getSelectionPath();
    if (selPath != null) {
      int row = this.getRowForPath(selPath);
      if (row >= 0) return row;
    }
    int lead = super.getLeadSelectionRow();
    return lead >= 0 && lead < this.getRowCount() ? lead : -1;
  }

  @Override
  public TreePath getLeadSelectionPath() {
    return this.paintingBaseRows ? null : super.getLeadSelectionPath();
  }

  @Override
  public TreePath getAnchorSelectionPath() {
    return this.paintingBaseRows ? null : super.getAnchorSelectionPath();
  }

  @Override
  public void updateUI() {
    if (UIManager.getUI(this) instanceof com.github.weisj.darklaf.ui.tree.DarkTreeUI) {
      setUI(new StyledTreeUI());
    } else {
      super.updateUI();
    }
    this.setBackground(Style.background());
    this.setOpaque(false);
    this.setRowHeight((int) (Style.TREE_ROW_HEIGHT * Editor.preferences().getUiScale()));
    this.putClientProperty("JTree.lineStyle", "None");
    this.putClientProperty("JTree.alternateRowColor", Boolean.FALSE);
    this.putClientProperty("Tree.paintLines", Boolean.FALSE);
  }



  public static class StyledTreeUI extends com.github.weisj.darklaf.ui.tree.DarkTreeUI {
    @Override
    protected void paintRowBackground(
        Graphics g,
        Rectangle rowBounds,
        Rectangle cellBounds,
        TreePath path,
        int row,
        boolean selected) {
      // Suppress Darklaf's default row rectangle painting.
      // StyledTree paints hover and selection highlights with custom rounded shapes.
    }

    @Override
    protected void paintVerticalLegs(
        Graphics g,
        Rectangle clipBounds,
        Rectangle rowBounds,
        java.awt.Insets insets,
        TreePath path) {
      // Suppress Darklaf's default vertical legs.
    }

    @Override
    protected void paintVerticalPartOfLeg(
        Graphics g,
        Rectangle clipBounds,
        java.awt.Insets insets,
        TreePath path) {
      // Suppress Darklaf's vertical part of leg.
    }

    @Override
    protected void paintHorizontalPartOfLeg(
        Graphics g,
        Rectangle clipBounds,
        java.awt.Insets insets,
        Rectangle bounds,
        TreePath path,
        int row,
        boolean isExpanded,
        boolean hasBeenExpanded,
        boolean isLeaf) {
      // Suppress Darklaf's horizontal legs.
    }

    @Override
    protected boolean shouldPaintLines() {
      return false;
    }
  }

  public void paintBaseRows(Graphics g) {
    this.paintingBaseRows = true;
    try {
      super.paintComponent(g);
    } finally {
      this.paintingBaseRows = false;
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    this.paintRowStateBackgrounds(g);
    this.paintHierarchyConnectors(g);
    this.paintBaseRows(g);
    this.paintSelectionIndicators(g);
  }




  protected void paintHierarchyConnectors(Graphics graphics) {
    if (this.getRowCount() == 0) return;
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Style.border());
      g2.setStroke(new BasicStroke(1f));

      for (int row = 0; row < this.getRowCount(); row++) {
        TreePath parentPath = this.getPathForRow(row);
        if (parentPath == null || !this.isExpanded(parentPath)
            || !(parentPath.getLastPathComponent() instanceof DefaultMutableTreeNode parent)
            || parent.getChildCount() == 0) {
          continue;
        }
        Rectangle parentBounds = this.getPathBounds(parentPath);
        if (parentBounds == null) continue;

        List<Rectangle> childBounds = new ArrayList<>();
        List<DefaultMutableTreeNode> visibleChildren = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) {
          DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parent.getChildAt(i);
          TreePath childPath = parentPath.pathByAddingChild(childNode);
          Rectangle bounds = this.getPathBounds(childPath);
          if (bounds != null && this.isVisible(childPath)) {
            childBounds.add(bounds);
            visibleChildren.add(childNode);
          }
        }
        if (childBounds.isEmpty()) continue;

        Rectangle firstChild = childBounds.getFirst();
        int indent = Math.max(12, firstChild.x - parentBounds.x);
        int trunkX = firstChild.x - indent / 2;
        int parentY = parentBounds.y + parentBounds.height;
        int lastY = childBounds.getLast().y + childBounds.getLast().height / 2;
        g2.drawLine(trunkX, parentY, trunkX, lastY);

        for (int i = 0; i < childBounds.size(); i++) {
          Rectangle child = childBounds.get(i);
          DefaultMutableTreeNode childNode = visibleChildren.get(i);
          int childY = child.y + child.height / 2;
          int endpoint = childNode.isLeaf() ? child.x + 8 : child.x - 3;
          g2.drawLine(trunkX, childY, endpoint, childY);
        }
      }
    } finally {
      g2.dispose();
    }
  }

  protected void paintRowStateBackgrounds(Graphics graphics) {
    if (this.getRowCount() == 0) return;
    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Object hoverVal = this.getClientProperty("hoverRow");
      int hoverRow = hoverVal instanceof Integer r ? r : -1;

      for (int row = 0; row < this.getRowCount(); row++) {
        Rectangle bounds = this.getRowBounds(row);
        if (bounds == null || bounds.width <= 0) continue;
        int x = bounds.x;
        int y = bounds.y + 2;
        int width = Math.max(1, this.getWidth() - x - 4);
        int height = Math.max(1, bounds.height - 4);

        if (this.isRowSelected(row)) {
          g2.setColor(Style.sceneRowSelected());
          g2.fillRoundRect(x, y, width, height, Style.CORNER_RADIUS, Style.CORNER_RADIUS);
        } else if (row == hoverRow) {
          g2.setColor(Style.sceneRowHover());
          g2.fillRoundRect(x, y, width, height, Style.CORNER_RADIUS, Style.CORNER_RADIUS);
        }
      }
    } finally {
      g2.dispose();
    }
  }

  protected void paintSelectionIndicators(Graphics graphics) {
    if (this.getRowCount() == 0) return;

    Graphics2D g2 = (Graphics2D) graphics.create();
    try {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int leadRow = this.getLeadSelectionRow();
      int[] selectionRows = this.getSelectionRows();
      if (selectionRows != null) {
        for (int row : selectionRows) {
          if (row < 0 || row >= this.getRowCount()) continue;
          Rectangle bounds = this.getRowBounds(row);
          if (bounds == null || bounds.width <= 0) continue;
          int x = bounds.x;
          int y = bounds.y + 2;
          int height = Math.max(1, bounds.height - 4);
          boolean isLead = (row == leadRow) || (selectionRows.length == 1);
          g2.setColor(isLead ? Style.accent() : Style.border());
          int barWidth = isLead ? 3 : 2;
          int barHeight = Math.max(4, height - 4);
          int barY = y + (height - barHeight) / 2;
          g2.fillRoundRect(x + 1, barY, barWidth, barHeight, 2, 2);
        }
      }
      if (this.hasFocus() && leadRow >= 0 && this.isRowSelected(leadRow)) {
        Rectangle bounds = this.getRowBounds(leadRow);
        if (bounds != null && bounds.width > 0) {
          int x = bounds.x;
          int y = bounds.y + 1;
          int width = Math.max(1, this.getWidth() - x - 4);
          int height = Math.max(1, bounds.height - 2);
          g2.setColor(Style.accent());
          g2.setStroke(new BasicStroke(1f));
          g2.drawRoundRect(x, y, width, height, Style.CORNER_RADIUS, Style.CORNER_RADIUS);
        }
      }
    } finally {
      g2.dispose();
    }
  }

  /**
   * Creates a scroll pane configured with transparent viewport and matching dark background.
   */

  public static JScrollPane createScrollPane(Component view) {
    JScrollPane scroll = new JScrollPane(
        view,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(null);
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(true);
    scroll.getViewport().setBackground(Style.background());
    if (view instanceof JTree tree) {
      scroll.getVerticalScrollBar().setUnitIncrement(
          tree.getRowHeight() > 0 ? tree.getRowHeight() : (int) (Style.TREE_ROW_HEIGHT * Editor.preferences().getUiScale()));
    }
    return scroll;
  }
}



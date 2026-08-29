package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.graphics.CreatureAnimationState;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.components.IconTreeListItem;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

public class IconTreeListRenderer implements TreeCellRenderer {

  private final JLabel label;

  public IconTreeListRenderer() {
    this.label = new JLabel();
    this.label.setOpaque(true);
    this.label.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
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
    this.label.setText(value.toString());
    if (value instanceof DefaultMutableTreeNode defaultMutableTreeNode
        && defaultMutableTreeNode.getUserObject()instanceof IconTreeListItem iconTreeListItem) {
      Object userObj = iconTreeListItem.getUserObject();
      this.label.setText(formatEntityLabel(userObj));
      if (iconTreeListItem.getIcon() != null) {
        this.label.setIcon(iconTreeListItem.getIcon());
      } else if (userObj instanceof de.gurkenlabs.litiengine.entities.IEntity entity) {
        label.setIcon(de.gurkenlabs.utiliti.controller.SpriteVariantSelector.getEntityIcon(entity, null, 16));
      }
    }
    this.label.setBackground(selected ? Style.COLOR_SELECTION_INACTIVE : Style.COLOR_SURFACE);
    this.label.setForeground(selected ? Style.COLOR_TEXT : Style.COLOR_TEXT);
    return label;
  }

  private static String formatEntityLabel(Object userObj) {
    if (userObj instanceof de.gurkenlabs.litiengine.entities.Entity entity) {
      String name = entity.getName();
      int id = entity.getMapId();
      if (name != null && !name.isEmpty()) {
        return "<html>" + escapeHtml(name) + " <span style='color:#787898;font-size:10px'>#" + id + "</span></html>";
      }
      return entity.getClass().getSimpleName() + " (#" + id + ")";
    }
    if (userObj instanceof IconTreeListItem inner) {
      return inner.toString();
    }
    return userObj.toString();
  }

  private static String escapeHtml(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}

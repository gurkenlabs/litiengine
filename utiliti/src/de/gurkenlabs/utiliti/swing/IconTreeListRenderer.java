package de.gurkenlabs.utiliti.swing;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.utiliti.Program;

public class IconTreeListRenderer implements TreeCellRenderer {
  public static final Icon DEFAULT_NODE_ICON = new ImageIcon(Resources.getImage("bullet.png"));
  private static final Border normalBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
  private static final Border focusBorder = BorderFactory.createDashedBorder(UIManager.getDefaults().getColor("Tree.selectionBorderColor"));

  private final JLabel label;

  public IconTreeListRenderer() {
    this.label = new JLabel();
    this.label.setBorder(normalBorder);
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    this.label.setIcon(DEFAULT_NODE_ICON);
    this.label.setText(value.toString());

    if (value instanceof DefaultMutableTreeNode) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
      if (node.getUserObject() instanceof IconTreeListItem) {
        IconTreeListItem iconItem = (IconTreeListItem) node.getUserObject();
        this.label.setText(iconItem.getUserObject().toString());
        if (iconItem.getIcon() != null) {
          this.label.setIcon(iconItem.getIcon());
        } else if (iconItem.getUserObject() instanceof Prop) {
          Prop prop = (Prop) iconItem.getUserObject();
          label.setIcon(getIcon(prop));
        } else if (iconItem.getUserObject() instanceof Creature) {
          Creature creature = (Creature) iconItem.getUserObject();
          label.setIcon(getIcon(creature));
        }
      }
    }

    UIDefaults defaults = UIManager.getDefaults();
    this.label.setOpaque(true);
    this.label.setBackground(hasFocus || selected ? defaults.getColor("Tree.selectionBackground") : defaults.getColor("Tree.background"));
    this.label.setForeground(hasFocus || selected ? defaults.getColor("Tree.selectionForeground") : defaults.getColor("Tree.foreground"));
    this.label.setBorder(hasFocus ? focusBorder : normalBorder);

    return label;
  }

  private static Icon getIcon(Prop prop) {
    String cacheKey = Game.getEnvironment().getMap().getName() + "-" + prop.getSpritesheetName().toLowerCase() + "-tree";
    BufferedImage propImag;
    if (ImageCache.IMAGES.containsKey(cacheKey)) {
      propImag = ImageCache.IMAGES.get(cacheKey);
    } else {

      final String name = Program.PROP_SPRITE_PREFIX + prop.getSpritesheetName().toLowerCase() + "-" + PropState.INTACT.toString().toLowerCase();
      final String fallbackName = Program.PROP_SPRITE_PREFIX + prop.getSpritesheetName().toLowerCase();
      Spritesheet sprite = Spritesheet.find(name);
      if (sprite == null) {
        sprite = Spritesheet.find(fallbackName);
      }

      if (sprite == null || sprite.getSprite(0) == null) {
        return null;
      }

      propImag = ImageProcessing.scaleImage(sprite.getSprite(0), 16, 16, true);
      ImageCache.IMAGES.put(cacheKey, propImag);
    }

    return new ImageIcon(propImag);
  }

  private static Icon getIcon(Creature creature) {
    String cacheKey = Game.getEnvironment().getMap().getName() + "-" + creature.getSpritePrefix() + "-" + creature.getMapId() + "-tree";
    BufferedImage propImag;
    if (ImageCache.IMAGES.containsKey(cacheKey)) {
      propImag = ImageCache.IMAGES.get(cacheKey);
    } else {
      Collection<Spritesheet> sprites = Spritesheet.find(s -> s.getName().startsWith(creature.getSpritePrefix()));
      if (sprites.isEmpty()) {
        return null;
      }

      propImag = ImageProcessing.scaleImage(sprites.iterator().next().getSprite(0), 16, 16, true);
      ImageCache.IMAGES.put(cacheKey, propImag);
    }

    return new ImageIcon(propImag);
  }
}
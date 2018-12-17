package de.gurkenlabs.utiliti.swing;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Optional;

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
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ImageProcessing;
import de.gurkenlabs.utiliti.Icons;
import de.gurkenlabs.utiliti.Program;

public class IconTreeListRenderer implements TreeCellRenderer {

  private static final Border normalBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
  private static final Border focusBorder = BorderFactory.createDashedBorder(UIManager.getDefaults().getColor("Tree.selectionBorderColor"));

  private final JLabel label;

  public IconTreeListRenderer() {
    this.label = new JLabel();
    this.label.setBorder(normalBorder);
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    this.label.setIcon(Icons.DEFAULT_NODE);
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
    if (prop == null || prop.getSpritesheetName() == null) {
      return null;
    }

    String cacheKey = Game.world().environment().getMap().getName() + "-" + prop.getSpritesheetName().toLowerCase() + "-tree";
    BufferedImage propImag = Resources.images().get(cacheKey, () -> {
      final String name = Program.PROP_SPRITE_PREFIX + prop.getSpritesheetName().toLowerCase() + "-" + PropState.INTACT.toString().toLowerCase();
      final String fallbackName = Program.PROP_SPRITE_PREFIX + prop.getSpritesheetName().toLowerCase();

      Optional<Spritesheet> opt = Resources.spritesheets().tryGet(name);
      Spritesheet sprite = null;
      if (opt.isPresent()) {
        sprite = opt.get();
      } else if (Resources.spritesheets().contains(fallbackName)) {
        sprite = Resources.spritesheets().get(fallbackName);
      }

      if (sprite == null || sprite.getSprite(0) == null) {
        return null;
      }

      return ImageProcessing.scaleImage(sprite.getSprite(0), 16, 16, true);
    });

    return new ImageIcon(propImag);
  }

  private static Icon getIcon(Creature creature) {
    String cacheKey = Game.world().environment().getMap().getName() + "-" + creature.getSpritePrefix() + "-" + creature.getMapId() + "-tree";
    
    BufferedImage propImag = Resources.images().get(cacheKey, () -> {
      Collection<Spritesheet> sprites = Resources.spritesheets().get(s -> s.getName().equals(creature.getSpritePrefix() + CreatureAnimationController.IDLE) || s.getName().equals(creature.getSpritePrefix() + CreatureAnimationController.WALK)
          || s.getName().equals(creature.getSpritePrefix() + CreatureAnimationController.DEAD) || s.getName().startsWith(creature.getSpritePrefix() + "-"));
      if (sprites.isEmpty()) {
        return null;
      }

      return ImageProcessing.scaleImage(sprites.iterator().next().getSprite(0), 16, 16, true);
    });

    return new ImageIcon(propImag);
  }
}
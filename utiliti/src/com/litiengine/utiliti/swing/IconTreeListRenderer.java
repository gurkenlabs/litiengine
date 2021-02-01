package com.litiengine.utiliti.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import com.github.weisj.darklaf.components.border.DarkBorders;

import com.litiengine.Game;
import com.litiengine.entities.Creature;
import com.litiengine.entities.LightSource;
import com.litiengine.entities.Prop;
import com.litiengine.entities.PropState;
import com.litiengine.graphics.CreatureAnimationState;
import com.litiengine.graphics.Spritesheet;
import com.litiengine.graphics.animation.CreatureAnimationController;
import com.litiengine.graphics.animation.PropAnimationController;
import com.litiengine.resources.Resources;
import com.litiengine.util.Imaging;

public class IconTreeListRenderer implements TreeCellRenderer {

  private final JLabel label;

  public IconTreeListRenderer() {
    this.label = new JLabel();
    this.label.setBorder(DarkBorders.createLineBorder(1, 1, 1, 1));
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
        } else if (iconItem.getUserObject() instanceof LightSource) {
          LightSource creature = (LightSource) iconItem.getUserObject();
          label.setIcon(getIcon(creature));
        }
      }
    }
    return label;
  }

  private static Icon getIcon(Prop prop) {
    if (prop == null || prop.getSpritesheetName() == null) {
      return null;
    }

    String cacheKey = Game.world().environment().getMap().getName() + "-" + prop.getSpritesheetName().toLowerCase() + "-tree";
    BufferedImage propImag = Resources.images().get(cacheKey, () -> {
      final String fallbackName = PropAnimationController.getSpriteName(prop, false);

      Spritesheet sprite = Resources.spritesheets().get(PropAnimationController.getSpriteName(prop, PropState.INTACT, true));
      if (sprite == null && Resources.spritesheets().contains(fallbackName)) {
        sprite = Resources.spritesheets().get(fallbackName);
      }

      if (sprite == null || sprite.getSprite(0) == null) {
        return null;
      }

      return Imaging.scale(sprite.getSprite(0), 16, 16, true);
    });

    if (propImag == null) {
      return null;
    }

    return new ImageIcon(propImag);
  }

  private static Icon getIcon(Creature creature) {
    String cacheKey = Game.world().environment().getMap().getName() + "-" + creature.getSpritesheetName() + "-" + creature.getMapId() + "-tree";

    BufferedImage propImag = Resources.images().get(cacheKey, () -> {
      Collection<Spritesheet> sprites = Resources.spritesheets().get(s -> s.getName().equals(CreatureAnimationController.getSpriteName(creature, CreatureAnimationState.IDLE)) || s.getName().equals(CreatureAnimationController.getSpriteName(creature, CreatureAnimationState.WALK))
          || s.getName().equals(CreatureAnimationController.getSpriteName(creature, CreatureAnimationState.DEAD)) || s.getName().startsWith(creature.getSpritesheetName() + "-"));
      if (sprites.isEmpty()) {
        return null;
      }

      return Imaging.scale(sprites.iterator().next().getSprite(0), 16, 16, true);
    });

    if (propImag == null) {
      return null;
    }

    return new ImageIcon(propImag);
  }

  private static Icon getIcon(LightSource lightSource) {
    Color lightColor = lightSource.getColor();
    if (lightColor != null) {
      final String cacheKey = Game.world().environment().getMap().getName() + "-" + Integer.toHexString(lightSource.getColor().getRGB());

      BufferedImage newIconImage = Resources.images().get(cacheKey, () -> {
        BufferedImage img = Imaging.getCompatibleImage(10, 10);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(lightColor);
        g.fillRect(0, 0, 9, 9);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, 9, 9);
        g.dispose();
        return img;
      });

      return new ImageIcon(newIconImage);
    }

    return null;
  }
}
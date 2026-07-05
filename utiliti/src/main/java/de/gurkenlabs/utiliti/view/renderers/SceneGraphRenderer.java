package de.gurkenlabs.utiliti.view.renderers;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.graphics.CreatureAnimationState;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.CreatureAnimationController;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.components.SceneGraph;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

public class SceneGraphRenderer extends JPanel implements TreeCellRenderer {

  private final JLabel label;
  private final JLabel visibilityLabel;

  public SceneGraphRenderer() {
    super();
    setLayout(new java.awt.BorderLayout(2, 0));
    setOpaque(true);

    this.label = new JLabel();
    this.label.setOpaque(false);

    this.visibilityLabel = new JLabel();
    this.visibilityLabel.setOpaque(false);
    this.visibilityLabel.setPreferredSize(new Dimension(18, 18));
    this.visibilityLabel.setHorizontalAlignment(JLabel.CENTER);

    add(this.visibilityLabel, java.awt.BorderLayout.WEST);
    add(this.label, java.awt.BorderLayout.CENTER);
  }

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean selected, boolean expanded,
      boolean leaf, int row, boolean hasFocus) {

    this.label.setIcon(null);
    this.label.setText("");
    this.visibilityLabel.setIcon(null);

    if (value instanceof DefaultMutableTreeNode dmtn
        && dmtn.getUserObject() instanceof SceneGraph.SceneNode node) {

      if (node.isLayer()) {
        this.label.setText(formatLayerLabel(node));
        if (node.getIcon() != null) {
          this.label.setIcon(node.getIcon());
        } else {
          this.label.setIcon(getLayerIcon(node.getLayer()));
        }
        if (node.getLayer() instanceof IMapObjectLayer) {
          this.visibilityLabel.setIcon(node.isVisible() ? Icons.SHOW_16 : Icons.HIDE_16);
        }
      } else {
        this.label.setText(formatEntityLabel(node));
        Icon entityIcon = getEntityIcon(node);
        if (entityIcon != null) {
          this.label.setIcon(entityIcon);
        } else if (node.getIcon() != null) {
          this.label.setIcon(node.getIcon());
        } else {
          this.label.setIcon(getDefaultEntityIcon(node));
        }
      }
    }

    if (selected) {
      setBackground(UIManager.getColor("Tree.selectionBackground"));
      setForeground(UIManager.getColor("Tree.selectionForeground"));
      this.label.setForeground(UIManager.getColor("Tree.selectionForeground"));
      this.visibilityLabel.setForeground(UIManager.getColor("Tree.selectionForeground"));
    } else {
      setBackground(UIManager.getColor("Tree.background"));
      setForeground(UIManager.getColor("Tree.foreground"));
      this.label.setForeground(UIManager.getColor("Tree.foreground"));
      this.visibilityLabel.setForeground(UIManager.getColor("Tree.foreground"));
    }

    return this;
  }

  private static String formatLayerLabel(SceneGraph.SceneNode node) {
    StringBuilder sb = new StringBuilder();
    sb.append("<html>");
    sb.append(escapeHtml(node.getName()));
    if (node.getObjectCount() > 0) {
      sb.append(" <span style='color:#787898;font-size:10px'>(")
        .append(node.getObjectCount())
        .append(")</span>");
    }
    if (!node.isVisible()) {
      sb.append(" <span style='color:#787898;font-size:10px'>hidden</span>");
    }
    sb.append("</html>");
    return sb.toString();
  }

  private static String formatEntityLabel(SceneGraph.SceneNode node) {
    String name = node.getName();
    if (name == null || name.isEmpty()) {
      return "?";
    }
    int hashIdx = name.lastIndexOf(" #");
    if (hashIdx > 0) {
      String base = name.substring(0, hashIdx);
      String id = name.substring(hashIdx + 1);
      return "<html>" + escapeHtml(base)
          + " <span style='color:#787898;font-size:10px'>" + escapeHtml(id) + "</span></html>";
    }
    return "<html>" + escapeHtml(name) + "</html>";
  }

  private static String escapeHtml(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private static Icon getLayerIcon(ILayer layer) {
    if (layer instanceof de.gurkenlabs.litiengine.environment.tilemap.ITileLayer) {
      return Icons.SPRITESHEET_16;
    } else if (layer instanceof de.gurkenlabs.litiengine.environment.tilemap.IImageLayer) {
      return Icons.ASSET_16;
    } else if (layer instanceof de.gurkenlabs.litiengine.environment.tilemap.IGroupLayer) {
      return Icons.LAYER_16;
    }
    return Icons.LAYER_16;
  }

  private static Icon getEntityIcon(SceneGraph.SceneNode node) {
    if (node.getEntity() == null) {
      return null;
    }

    if (node.getEntity() instanceof Prop prop) {
      return getIcon(prop);
    } else if (node.getEntity() instanceof Creature creature) {
      return getIcon(creature);
    } else if (node.getEntity() instanceof LightSource lightSource) {
      return getIcon(lightSource);
    }
    return null;
  }

  private static Icon getDefaultEntityIcon(SceneGraph.SceneNode node) {
    if (node.getMapObject() == null) {
      return null;
    }
    de.gurkenlabs.litiengine.environment.tilemap.MapObjectType type =
        de.gurkenlabs.litiengine.environment.tilemap.MapObjectType.get(node.getMapObject().getType());
    return Icons.forMapObjectType(type);
  }

  private static Icon getIcon(Prop prop) {
    if (prop == null || prop.getSpritesheetName() == null) {
      return null;
    }
    String cacheKey = Game.world().environment().getMap().getName()
        + "-" + prop.getSpritesheetName().toLowerCase() + "-scene";
    BufferedImage img = Resources.images().get(cacheKey, () -> {
      String fallbackName = PropAnimationController.getSpriteName(prop, false);
      Spritesheet sprite = Resources.spritesheets()
          .get(PropAnimationController.getSpriteName(prop, PropState.INTACT, true));
      if (sprite == null && Resources.spritesheets().contains(fallbackName)) {
        sprite = Resources.spritesheets().get(fallbackName);
      }
      if (sprite == null || sprite.getSprite(0) == null) {
        return null;
      }
      return Imaging.scale(sprite.getSprite(0), 16, 16, true);
    });
    return img != null ? new ImageIcon(img) : null;
  }

  private static Icon getIcon(Creature creature) {
    String cacheKey = Game.world().environment().getMap().getName()
        + "-" + creature.getSpritesheetName() + "-" + creature.getMapId() + "-scene";
    BufferedImage img = Resources.images().get(cacheKey, () -> {
      Collection<Spritesheet> sprites = Resources.spritesheets().get(
          s -> s.getName().equals(CreatureAnimationController.getSpriteName(creature, CreatureAnimationState.IDLE))
              || s.getName().equals(CreatureAnimationController.getSpriteName(creature, CreatureAnimationState.MOVE))
              || s.getName().equals(CreatureAnimationController.getSpriteName(creature, CreatureAnimationState.DEAD))
              || s.getName().startsWith(creature.getSpritesheetName() + "-"));
      if (sprites.isEmpty()) {
        return null;
      }
      return Imaging.scale(sprites.iterator().next().getSprite(0), 16, 16, true);
    });
    return img != null ? new ImageIcon(img) : null;
  }

  private static Icon getIcon(LightSource lightSource) {
    Color lightColor = lightSource.getColor();
    if (lightColor == null) {
      return null;
    }
    String cacheKey = Game.world().environment().getMap().getName()
        + "-" + Integer.toHexString(lightSource.getColor().getRGB());
    BufferedImage img = Resources.images().get(cacheKey, () -> {
      BufferedImage newImg = Imaging.getCompatibleImage(10, 10);
      Graphics2D g = (Graphics2D) Objects.requireNonNull(newImg).getGraphics();
      g.setColor(lightColor);
      g.fillRect(0, 0, 9, 9);
      g.setColor(Color.BLACK);
      g.drawRect(0, 0, 9, 9);
      g.dispose();
      return newImg;
    });
    return img != null ? new ImageIcon(img) : null;
  }
}

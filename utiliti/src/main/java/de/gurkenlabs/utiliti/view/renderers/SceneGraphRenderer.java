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
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.components.SceneGraph;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

public class SceneGraphRenderer extends JPanel implements TreeCellRenderer {
  private static final Color HOVER_BG = new Color(28, 31, 40);
  private static final Color SELECTED_BG = new Color(31, 76, 140);
  private enum BadgeKind { COUNT, ID }

  private final JPanel rowPanel;
  private final JLabel visibilityLabel;
  private final JLabel typeLabel;
  private final JLabel nameLabel;
  private final JLabel badgeLabel;
  private final JLabel swatchLabel;
  private final JLabel moreLabel;

  public SceneGraphRenderer() {
    super(new BorderLayout(0, 0));
    setOpaque(true);

    this.rowPanel = new JPanel(new BorderLayout(6, 0));
    this.rowPanel.setOpaque(false);
    this.rowPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 4));

    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
    left.setOpaque(false);

    this.visibilityLabel = fixedLabel(18);
    this.typeLabel = fixedLabel(18);
    this.nameLabel = new JLabel();
    this.nameLabel.setOpaque(false);
    this.badgeLabel = new BadgeLabel();

    left.add(this.visibilityLabel);
    left.add(this.typeLabel);
    left.add(this.nameLabel);
    left.add(this.badgeLabel);

    JPanel right = new JPanel(new FlowLayout(FlowLayout.TRAILING, 8, 0));
    right.setOpaque(false);
    this.swatchLabel = fixedLabel(14);
    this.moreLabel = fixedLabel(12);
    this.moreLabel.setIcon(MoreIcon.INSTANCE);
    right.add(this.swatchLabel);
    right.add(this.moreLabel);

    this.rowPanel.add(left, BorderLayout.CENTER);
    this.rowPanel.add(right, BorderLayout.EAST);
    add(this.rowPanel, BorderLayout.CENTER);
  }

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean selected, boolean expanded,
      boolean leaf, int row, boolean hasFocus) {

    reset();

    SceneGraph.SceneNode node = null;
    if (value instanceof DefaultMutableTreeNode dmtn
        && dmtn.getUserObject() instanceof SceneGraph.SceneNode sceneNode) {
      node = sceneNode;
    }

    if (node != null && node.isSection()) {
      renderSection(node);
    } else if (node != null && node.isLayer()) {
      renderLayer(node);
    } else if (node != null) {
      renderEntity(node);
    }

    Object hoverRow = tree.getClientProperty("SceneGraph.hoverRow");
    boolean hover = hoverRow instanceof Integer hovered && hovered == row;
    setBackground(selected ? SELECTED_BG : hover ? HOVER_BG : Style.COLOR_BG);
    Color foreground = selected ? Color.WHITE : Style.COLOR_TEXT;
    this.nameLabel.setForeground(node != null && node.isSection() ? Style.COLOR_SUBTEXT : foreground);
    this.moreLabel.setForeground(selected ? Color.WHITE : Style.COLOR_SUBTEXT);

    return this;
  }

  private void renderSection(SceneGraph.SceneNode node) {
    this.visibilityLabel.setVisible(false);
    this.typeLabel.setVisible(false);
    this.badgeLabel.setVisible(false);
    this.swatchLabel.setVisible(false);
    this.moreLabel.setVisible(false);
    this.nameLabel.setText(node.getName());
    this.nameLabel.setFont(this.nameLabel.getFont().deriveFont(10f));
  }

  private void renderLayer(SceneGraph.SceneNode node) {
    this.visibilityLabel.setIcon(node.isVisible() ? Icons.SHOW_16 : Icons.HIDE_16);
    this.typeLabel.setIcon(node.getIcon() != null ? node.getIcon() : getLayerIcon(node.getLayer()));
    this.nameLabel.setText(node.getName());
    this.badgeLabel.setVisible(node.getObjectCount() > 0);
    this.badgeLabel.putClientProperty("badgeKind", BadgeKind.COUNT);
    this.badgeLabel.setText(node.getObjectCount() + " items");
    Color color = node.getLayerColor();
    this.swatchLabel.setIcon(color != null ? new ColorSwatchIcon(color) : null);
  }

  private void renderEntity(SceneGraph.SceneNode node) {
    this.visibilityLabel.setIcon(null);
    Icon entityIcon = getEntityIcon(node);
    this.typeLabel.setIcon(entityIcon != null ? entityIcon : getDefaultEntityIcon(node));
    this.nameLabel.setText(formatEntityName(node.getName()));
    this.badgeLabel.setVisible(false);
    this.swatchLabel.setVisible(false);
  }

  private void reset() {
    this.visibilityLabel.setVisible(true);
    this.visibilityLabel.setIcon(null);
    this.typeLabel.setVisible(true);
    this.typeLabel.setIcon(null);
    this.nameLabel.setText("");
    this.nameLabel.setFont(getFont());
    this.badgeLabel.setVisible(false);
    this.badgeLabel.setText("");
    this.badgeLabel.putClientProperty("badgeKind", BadgeKind.COUNT);
    this.swatchLabel.setVisible(true);
    this.swatchLabel.setIcon(null);
    this.moreLabel.setVisible(true);
  }

  private static JLabel fixedLabel(int width) {
    JLabel label = new JLabel();
    label.setOpaque(false);
    label.setHorizontalAlignment(JLabel.CENTER);
    label.setPreferredSize(new Dimension(width, 18));
    return label;
  }

  private static String formatEntityName(String name) {
    int hashIdx = name != null ? name.lastIndexOf(" #") : -1;
    if (hashIdx <= 0) {
      return name;
    }
    String base = name.substring(0, hashIdx);
    String id = name.substring(hashIdx + 2);
    return "<html>" + escapeHtml(base)
        + " <span style='color:#969EB9'>#" + escapeHtml(id) + "</span></html>";
  }

  private static String escapeHtml(String value) {
    return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private static Icon getLayerIcon(ILayer layer) {
    if (layer instanceof de.gurkenlabs.litiengine.environment.tilemap.ITileLayer) {
      return Icons.TILESET_16;
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

  private static final class BadgeLabel extends JLabel {
    BadgeLabel() {
      setOpaque(false);
      setForeground(Style.COLOR_TEXT);
      setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Object kind = getClientProperty("badgeKind");
      g2.setColor(kind == BadgeKind.ID ? new Color(42, 65, 112) : Style.COLOR_SELECTION_INACTIVE);
      g2.fillRoundRect(0, 2, getWidth(), getHeight() - 4, 10, 10);
      g2.dispose();
      super.paintComponent(g);
    }
  }

  private static final class ColorSwatchIcon implements Icon {
    private final Color color;

    ColorSwatchIcon(Color color) {
      this.color = color;
    }

    @Override
    public int getIconWidth() {
      return 10;
    }

    @Override
    public int getIconHeight() {
      return 10;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(color);
      g2.fillRoundRect(x, y, 10, 10, 3, 3);
      g2.dispose();
    }
  }

  private static final class MoreIcon implements Icon {
    private static final MoreIcon INSTANCE = new MoreIcon();

    @Override
    public int getIconWidth() {
      return 12;
    }

    @Override
    public int getIconHeight() {
      return 16;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Style.COLOR_SUBTEXT);
      int cx = x + 6;
      g2.fillOval(cx - 1, y + 3, 2, 2);
      g2.fillOval(cx - 1, y + 7, 2, 2);
      g2.fillOval(cx - 1, y + 11, 2, 2);
      g2.dispose();
    }
  }
}

package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.ScriptBindingService;
import de.gurkenlabs.utiliti.controller.ScriptBindingTarget;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Overview panel shown under "Current Script" displaying exposed fields, declared methods,
 * and usages with breadcrumbs.
 */
public final class ScriptOverviewPanel extends JPanel {
  private final Consumer<ScriptBindingService.ScriptUsage> onUsageClick;
  private final Consumer<Integer> onLineJump;
  private final JPanel content = new JPanel();
  private final JPanel fieldsListPanel = new JPanel();
  private final JPanel methodsListPanel = new JPanel();
  private final JPanel usagesListPanel = new JPanel();
  private final JLabel usagesHeaderLabel = new JLabel("USED IN (0)");

  private ScriptDefinition currentDefinition;

  public ScriptOverviewPanel(
      Consumer<ScriptBindingService.ScriptUsage> onUsageClick,
      Consumer<Integer> onLineJump) {
    super(new BorderLayout());
    this.onUsageClick = onUsageClick == null ? ignored -> {} : onUsageClick;
    this.onLineJump = onLineJump == null ? ignored -> {} : onLineJump;

    this.setBackground(Style.background());
    this.content.setLayout(new BoxLayout(this.content, BoxLayout.Y_AXIS));
    this.content.setOpaque(false);
    this.content.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));


    // --- EXPOSED FIELDS SECTION ---
    JPanel fieldsHeader = createSectionHeader(createSectionTitleLabel("EXPOSED FIELDS"));
    this.fieldsListPanel.setLayout(new BoxLayout(this.fieldsListPanel, BoxLayout.Y_AXIS));
    this.fieldsListPanel.setOpaque(false);

    JPanel fieldsSection = new JPanel(new BorderLayout(0, 2)) {
      @Override
      public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
      }
    };
    fieldsSection.setOpaque(false);
    fieldsSection.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()),
        BorderFactory.createEmptyBorder(4, 0, 8, 0)));
    fieldsSection.add(fieldsHeader, BorderLayout.NORTH);
    fieldsSection.add(this.fieldsListPanel, BorderLayout.CENTER);
    this.content.add(fieldsSection);

    // --- METHODS SECTION ---
    JPanel methodsHeader = createSectionHeader(createSectionTitleLabel("METHODS"));
    this.methodsListPanel.setLayout(new BoxLayout(this.methodsListPanel, BoxLayout.Y_AXIS));
    this.methodsListPanel.setOpaque(false);

    JPanel methodsSection = new JPanel(new BorderLayout(0, 2)) {
      @Override
      public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
      }
    };
    methodsSection.setOpaque(false);
    methodsSection.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()),
        BorderFactory.createEmptyBorder(6, 0, 8, 0)));
    methodsSection.add(methodsHeader, BorderLayout.NORTH);
    methodsSection.add(this.methodsListPanel, BorderLayout.CENTER);
    this.content.add(methodsSection);

    // --- USED IN SECTION ---
    this.usagesHeaderLabel.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 10.5f));
    this.usagesHeaderLabel.setForeground(Style.mutedText());
    JPanel usagesHeader = createSectionHeader(this.usagesHeaderLabel);
    this.usagesListPanel.setLayout(new BoxLayout(this.usagesListPanel, BoxLayout.Y_AXIS));
    this.usagesListPanel.setOpaque(false);

    JPanel usagesSection = new JPanel(new BorderLayout(0, 2)) {
      @Override
      public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
      }
    };
    usagesSection.setOpaque(false);
    usagesSection.setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));
    usagesSection.add(usagesHeader, BorderLayout.NORTH);
    usagesSection.add(this.usagesListPanel, BorderLayout.CENTER);
    this.content.add(usagesSection);

    this.content.add(Box.createVerticalGlue());


    JScrollPane scroll = new JScrollPane(this.content);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(null);
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    scroll.getViewport().setBackground(Style.background());
    scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    this.add(scroll, BorderLayout.CENTER);

    this.bind(null, null, List.of());
  }

  public void refreshTheme() {
    this.setBackground(Style.background());
    this.usagesHeaderLabel.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 10.5f));
    this.usagesHeaderLabel.setForeground(Style.mutedText());
    this.repaint();
  }

  public void bind(ScriptDefinition definition, ScriptOutline.Symbol outline, List<ScriptBindingService.ScriptUsage> usages) {
    this.currentDefinition = definition;
    int usageCount = (usages == null || definition == null) ? 0 : usages.size();
    this.usagesHeaderLabel.setText("USED IN (" + usageCount + ")");

    if (definition == null) {
      this.fieldsListPanel.removeAll();
      this.methodsListPanel.removeAll();
      this.usagesListPanel.removeAll();
      this.revalidate();
      this.repaint();
      return;
    }

    // Populate Fields
    this.fieldsListPanel.removeAll();
    List<ScriptOutline.Symbol> fields = extractSymbols(outline, ScriptOutline.Kind.FIELD);
    if (fields.isEmpty()) {
      this.fieldsListPanel.add(createEmptyLabel("No exposed fields"));
    } else {
      for (var f : fields) {
        String label = f.name() + (f.detail() != null && !f.detail().isBlank() ? " : " + f.detail() : "");
        this.fieldsListPanel.add(createSymbolRow(new RedDotIcon(), label, f.line() + 1));
      }
    }

    // Populate Methods
    this.methodsListPanel.removeAll();
    List<ScriptOutline.Symbol> methods = extractSymbols(outline, ScriptOutline.Kind.METHOD);
    if (methods.isEmpty()) {
      this.methodsListPanel.add(createEmptyLabel("No declared methods"));
    } else {
      for (var m : methods) {
        String detail = m.detail() != null ? m.detail() : "";
        String label;
        if (detail.startsWith("(") || detail.startsWith(" :")) {
          label = m.name() + detail;
        } else if (!detail.isBlank()) {
          label = m.name() + "() : " + detail;
        } else {
          label = m.name() + "()";
        }
        this.methodsListPanel.add(createSymbolRow(new PurpleDotIcon(), label, m.line() + 1));
      }
    }

    // Populate Usages
    this.usagesListPanel.removeAll();
    if (usages == null || usages.isEmpty()) {
      this.usagesListPanel.add(createEmptyLabel("Not used in any entity or map"));
    } else {
      for (var usage : usages) {
        this.usagesListPanel.add(createUsageRow(usage));
      }
    }

    this.revalidate();
    this.repaint();
  }

  private JLabel createEmptyLabel(String text) {
    JLabel empty = new JLabel(text);
    empty.setForeground(Style.mutedText());
    empty.setFont(Style.getDefaultFont().deriveFont(11f));
    empty.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
    empty.setPreferredSize(new Dimension(0, Style.TREE_ROW_HEIGHT));
    empty.setMaximumSize(new Dimension(Integer.MAX_VALUE, Style.TREE_ROW_HEIGHT));
    empty.setMinimumSize(new Dimension(0, Style.TREE_ROW_HEIGHT));
    return empty;
  }

  private JPanel createSymbolRow(Icon icon, String text, int line) {
    JPanel row = new JPanel(new BorderLayout(6, 0));
    row.setOpaque(false);
    row.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
    row.setPreferredSize(new Dimension(0, Style.TREE_ROW_HEIGHT));
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Style.TREE_ROW_HEIGHT));
    row.setMinimumSize(new Dimension(0, Style.TREE_ROW_HEIGHT));
    row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    JLabel iconLabel = new JLabel(icon);
    JLabel textLabel = new JLabel(text);
    textLabel.setForeground(Style.text());
    textLabel.setFont(Style.getDefaultFont().deriveFont(11.5f));

    row.add(iconLabel, BorderLayout.WEST);
    row.add(textLabel, BorderLayout.CENTER);

    row.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        row.setOpaque(true);
        row.setBackground(Style.surface());
        row.repaint();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        row.setOpaque(false);
        row.repaint();
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if (line > 0) {
          onLineJump.accept(line);
        }
      }
    });

    return row;
  }

  private JPanel createUsageRow(ScriptBindingService.ScriptUsage usage) {
    JPanel row = new JPanel(new BorderLayout(6, 0));
    row.setOpaque(false);
    row.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));
    row.setPreferredSize(new Dimension(0, Style.TREE_ROW_HEIGHT));
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Style.TREE_ROW_HEIGHT));
    row.setMinimumSize(new Dimension(0, Style.TREE_ROW_HEIGHT));
    row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    JLabel iconLabel = new JLabel(usageIcon(usage));
    JLabel textLabel = new JLabel(usageDisplayLabel(usage));
    textLabel.setForeground(Style.text());
    textLabel.setFont(Style.getDefaultFont().deriveFont(11.5f));

    row.add(iconLabel, BorderLayout.WEST);
    row.add(textLabel, BorderLayout.CENTER);

    row.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        row.setOpaque(true);
        row.setBackground(Style.surface());
        row.repaint();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        row.setOpaque(false);
        row.repaint();
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        onUsageClick.accept(usage);
      }
    });

    return row;
  }

  private static Icon usageIcon(ScriptBindingService.ScriptUsage usage) {
    if (usage == null || usage.target() == null) return Icons.ENTITY_16;
    return switch (usage.target()) {
      case ScriptBindingTarget.Game ignored -> Icons.PLAY_16;
      case ScriptBindingTarget.Environment ignored -> Icons.MAP_16;
      case ScriptBindingTarget.EntityInstance entity -> {
        IMap map = Editor.instance().getMapComponent() == null ? null : Editor.instance().getMapComponent().getMaps().stream()
            .filter(m -> Objects.equals(m.getName(), entity.mapName())).findFirst().orElse(null);
        IMapObject object = map == null ? null : map.getMapObject(entity.entityId());
        Icon spriteIcon = getEntitySpriteIcon(object);
        if (spriteIcon != null) {
          yield spriteIcon;
        }
        MapObjectType type = object == null ? null : MapObjectType.get(object.getType());
        yield type == null ? Icons.ENTITY_16 : Icons.forMapObjectType(type);
      }
      case ScriptBindingTarget.EntityType ignored -> Icons.ENTITY_16;
    };
  }

  private static Icon getEntitySpriteIcon(IMapObject object) {
    if (object == null) return null;
    String spriteSheet = object.getStringValue(MapObjectProperty.SPRITESHEETNAME);
    if (spriteSheet == null || spriteSheet.isBlank()) {
      spriteSheet = object.getStringValue("spritesheet");
    }
    if (spriteSheet != null && !spriteSheet.isBlank()) {
      String name = spriteSheet;
      Spritesheet ss = Resources.spritesheets().get(name);
      if (ss == null) {
        java.util.Collection<Spritesheet> matches = Resources.spritesheets().get(s -> s.getName().startsWith(name + "-") || s.getName().equals(name));
        if (!matches.isEmpty()) {
          ss = matches.iterator().next();
        }
      }
      if (ss != null && ss.getSprite(0) != null) {
        BufferedImage scaled = Imaging.scale(ss.getSprite(0), 16, 16, true);
        if (scaled != null) {
          return new ImageIcon(scaled);
        }
      }
    }
    return null;
  }

  private static String usageDisplayLabel(ScriptBindingService.ScriptUsage usage) {
    if (usage == null) return "";
    if (usage.target() instanceof ScriptBindingTarget.EntityInstance entity) {
      IMap map = Editor.instance().getMapComponent() == null ? null : Editor.instance().getMapComponent().getMaps().stream()
          .filter(m -> Objects.equals(m.getName(), entity.mapName())).findFirst().orElse(null);
      IMapObject object = map == null ? null : map.getMapObject(entity.entityId());
      String name = (object != null && object.getName() != null && !object.getName().isBlank())
          ? object.getName()
          : (object != null && object.getType() != null ? object.getType() : "Entity");
      String entityLabel = name + " #" + entity.entityId();
      if (entity.mapName() != null && !entity.mapName().isBlank()) {
        return entity.mapName() + " -> " + entityLabel;
      }
      return entityLabel;
    }
    if (usage.target() instanceof ScriptBindingTarget.Environment env) {
      return "Map \"" + env.mapName() + "\"";
    }
    if (usage.target() instanceof ScriptBindingTarget.Game) {
      return "Game Lifecycle";
    }
    return usage.label();
  }

  private static List<ScriptOutline.Symbol> extractSymbols(ScriptOutline.Symbol root, ScriptOutline.Kind kind) {
    List<ScriptOutline.Symbol> result = new ArrayList<>();
    if (root == null) return result;
    for (var child : root.children()) {
      if (child.kind() == ScriptOutline.Kind.GROUP) {
        for (var grandChild : child.children()) {
          if (grandChild.kind() == kind) result.add(grandChild);
        }
      } else if (child.kind() == kind) {
        result.add(child);
      }
    }
    return result;
  }

  private static JLabel createSectionTitleLabel(String title) {
    JLabel label = new JLabel(title);
    label.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 10.5f));
    label.setForeground(Style.mutedText());
    return label;
  }

  private static JPanel createSectionHeader(JComponent titleComponent) {
    JPanel panel = new JPanel(new BorderLayout(6, 0));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
    panel.setPreferredSize(new Dimension(0, Style.TREE_ROW_HEIGHT));
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Style.TREE_ROW_HEIGHT));
    panel.add(titleComponent, BorderLayout.WEST);

    return panel;
  }

  private static final class RedDotIcon implements Icon {
    private static final int SIZE = 8;
    private static final Color RED = new Color(245, 90, 90);

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(RED);
        g2.fillOval(x + 2, y + 3, SIZE, SIZE);
      } finally {
        g2.dispose();
      }
    }

    @Override
    public int getIconWidth() {
      return SIZE + 4;
    }

    @Override
    public int getIconHeight() {
      return SIZE + 6;
    }
  }

  private static final class PurpleDotIcon implements Icon {
    private static final int SIZE = 8;
    private static final Color PURPLE = new Color(175, 120, 245);

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(PURPLE);
        g2.fillOval(x + 2, y + 3, SIZE, SIZE);
      } finally {
        g2.dispose();
      }
    }

    @Override
    public int getIconWidth() {
      return SIZE + 4;
    }

    @Override
    public int getIconHeight() {
      return SIZE + 6;
    }
  }
}

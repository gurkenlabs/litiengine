package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.ScriptBindingService;
import de.gurkenlabs.utiliti.controller.ScriptBindingTarget;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

/** Reverse-reference view for the assignments of the script currently open in the editor. */
final class ScriptUsagesPanel extends JPanel {
  private final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Used By");
  private final DefaultTreeModel model = new DefaultTreeModel(this.root);
  private final JTree usages = UI.createStyledTree(this.model);
  private final JLabel count = new JLabel("No uses");
  private final JButton toggle = new JButton(Icons.SCROLL_RIGHT_16);
  private final JPanel header = new JPanel(new BorderLayout(10, 0));
  private final JScrollPane scroll = new JScrollPane(this.usages);
  private final Consumer<ScriptBindingService.ScriptUsage> navigation;
  private final Consumer<Boolean> expansionListener;
  private ScriptDefinition definition;
  private boolean expanded;

  ScriptUsagesPanel(Consumer<ScriptBindingService.ScriptUsage> navigation,
                    Consumer<Boolean> expansionListener) {
    super(new BorderLayout());
    this.navigation = navigation == null ? ignored -> {} : navigation;
    this.expansionListener = expansionListener == null ? ignored -> {} : expansionListener;
    this.setBackground(Style.background());
    this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()));

    this.header.setBackground(Style.background());
    this.header.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
    this.header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.header.setToolTipText("Show or hide script usages");

    JPanel title = new JPanel(new BorderLayout(7, 0));
    title.setOpaque(false);
    this.toggle.setBorder(BorderFactory.createEmptyBorder());
    this.toggle.setContentAreaFilled(false);
    this.toggle.setFocusable(false);
    this.toggle.setPreferredSize(new Dimension(20, 20));
    this.toggle.setToolTipText("Show or hide script usages");
    this.toggle.addActionListener(event -> this.setExpanded(!this.expanded));
    JLabel heading = new JLabel("Used By");
    heading.setFont(heading.getFont().deriveFont(Font.BOLD, 11f));
    heading.setForeground(Style.text());
    title.add(this.toggle, BorderLayout.WEST);
    title.add(heading, BorderLayout.CENTER);
    this.header.add(title, BorderLayout.WEST);

    this.count.setFont(this.count.getFont().deriveFont(11f));
    this.count.setForeground(Style.mutedText());
    this.header.add(this.count, BorderLayout.EAST);
    this.header.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent event) {
        setExpanded(!expanded);
      }
    });
    this.add(this.header, BorderLayout.NORTH);

    this.usages.setRootVisible(false);
    this.usages.setShowsRootHandles(true);
    this.usages.setRowHeight(Style.TREE_ROW_HEIGHT);
    this.usages.setBackground(Style.background());
    this.usages.setOpaque(false);
    this.usages.setBorder(BorderFactory.createEmptyBorder(5, 18, 8, 12));
    this.usages.putClientProperty("JTree.lineStyle", "None");
    this.usages.setCellRenderer(new UsageRenderer());
    this.usages.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) navigateSelectedUsage();
      }
    });
    this.usages.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ENTER) navigateSelectedUsage();
      }
    });
    this.scroll.setBorder(null);
    this.scroll.getViewport().setBackground(Style.background());
    this.add(this.scroll, BorderLayout.CENTER);
    this.setExpanded(false);
  }

  void showScript(ScriptDefinition definition) {
    boolean changed = !Objects.equals(this.definition == null ? null : this.definition.getId(),
      definition == null ? null : definition.getId());
    this.definition = definition;
    int usageCount = this.rebuild();
    if (usageCount == 0 || changed) {
      this.setExpanded(false);
    }
  }

  void refresh() {
    boolean wasExpanded = this.expanded;
    int usageCount = this.rebuild();
    this.setExpanded(usageCount > 0 && wasExpanded);
  }

  int collapsedHeight() {
    return this.header.getPreferredSize().height + 1;
  }

  void reveal() {
    int usageCount = this.rebuild();
    this.setExpanded(usageCount > 0);
  }

  private int rebuild() {
    this.root.removeAllChildren();
    List<ScriptBindingService.ScriptUsage> visible = this.definition == null ? List.of()
      : displayableUsages(ScriptBindingService.instance().findUsages(this.definition.getId()));
    this.count.setText(visible.isEmpty() ? "No uses" : visible.size() + (visible.size() == 1 ? " use" : " uses"));

    List<ScriptBindingService.ScriptUsage> game = visible.stream()
      .filter(usage -> usage.target() instanceof ScriptBindingTarget.Game).toList();
    if (!game.isEmpty()) {
      this.root.add(new DefaultMutableTreeNode(new UsageNode(labelWithCount("Game", game.size()), Icons.PLAY_16,
        game.getFirst())));
    }

    Map<String, List<ScriptBindingService.ScriptUsage>> byMap = new LinkedHashMap<>();
    visible.stream().filter(usage -> mapName(usage.target()) != null)
      .sorted(Comparator.comparing(usage -> mapName(usage.target()), String.CASE_INSENSITIVE_ORDER))
      .forEach(usage -> byMap.computeIfAbsent(mapName(usage.target()), ignored -> new ArrayList<>()).add(usage));
    byMap.forEach((mapName, mapUsages) -> this.root.add(this.mapNode(mapName, mapUsages)));

    this.model.reload();
    for (int row = 0; row < this.usages.getRowCount(); row++) this.usages.expandRow(row);
    return visible.size();
  }

  private DefaultMutableTreeNode mapNode(String mapName, List<ScriptBindingService.ScriptUsage> mapUsages) {
    List<ScriptBindingService.ScriptUsage> direct = mapUsages.stream()
      .filter(usage -> usage.target() instanceof ScriptBindingTarget.Environment).toList();
    ScriptBindingService.ScriptUsage navigationTarget = direct.isEmpty()
      ? new ScriptBindingService.ScriptUsage(new ScriptBindingTarget.Environment(mapName), mapName, -1)
      : direct.getFirst();
    DefaultMutableTreeNode mapNode = new DefaultMutableTreeNode(
      new UsageNode(labelWithCount(mapName, direct.size()), Icons.MAP_16, navigationTarget));

    Map<Integer, List<ScriptBindingService.ScriptUsage>> entities = new LinkedHashMap<>();
    mapUsages.stream().filter(usage -> usage.target() instanceof ScriptBindingTarget.EntityInstance)
      .forEach(usage -> {
        ScriptBindingTarget.EntityInstance entity = (ScriptBindingTarget.EntityInstance) usage.target();
        entities.computeIfAbsent(entity.entityId(), ignored -> new ArrayList<>()).add(usage);
      });
    entities.forEach((entityId, entityUsages) -> {
      ScriptBindingService.ScriptUsage usage = entityUsages.getFirst();
      mapNode.add(new DefaultMutableTreeNode(new UsageNode(
        labelWithCount(entityLabel(mapName, entityId), entityUsages.size()), entityIcon(mapName, entityId), usage)));
    });
    return mapNode;
  }

  private void setExpanded(boolean expanded) {
    this.expanded = expanded;
    this.toggle.setIcon(expanded ? Icons.SCROLL_DOWN_16 : Icons.SCROLL_RIGHT_16);
    this.scroll.setVisible(expanded);
    this.expansionListener.accept(expanded);
    this.revalidate();
    this.repaint();
  }

  private void navigateSelectedUsage() {
    Object selected = this.usages.getLastSelectedPathComponent();
    if (selected instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof UsageNode usageNode
        && usageNode.usage() != null) {
      this.navigation.accept(usageNode.usage());
    }
  }

  static List<ScriptBindingService.ScriptUsage> displayableUsages(ScriptBindingService.UsageIndex index) {
    if (index == null) return List.of();
    return index.usages().stream()
      .filter(usage -> !(usage.target() instanceof ScriptBindingTarget.EntityType))
      .toList();
  }

  private static String mapName(ScriptBindingTarget target) {
    return switch (target) {
      case ScriptBindingTarget.Environment map -> map.mapName();
      case ScriptBindingTarget.EntityInstance entity -> entity.mapName();
      default -> null;
    };
  }

  private static String labelWithCount(String label, int count) {
    return count > 1 ? label + "  ·  " + count + " uses" : label;
  }

  private static String entityLabel(String mapName, int entityId) {
    IMapObject object = mapObject(mapName, entityId);
    return object == null || object.getName() == null || object.getName().isBlank()
      ? "Entity #" + entityId : object.getName() + " #" + entityId;
  }

  private static javax.swing.Icon entityIcon(String mapName, int entityId) {
    IMapObject object = mapObject(mapName, entityId);
    return object == null ? Icons.ENTITY_16 : Icons.forMapObjectType(MapObjectType.get(object.getType()));
  }

  private static IMapObject mapObject(String mapName, int entityId) {
    if (Editor.instance().getGameFile() == null) return null;
    IMap map = Editor.instance().getGameFile().getMaps().stream()
      .filter(candidate -> Objects.equals(candidate.getName(), mapName)).findFirst().orElse(null);
    return map == null ? null : map.getMapObject(entityId);
  }

  private record UsageNode(String label, javax.swing.Icon icon, ScriptBindingService.ScriptUsage usage) {
    @Override
    public String toString() {
      return this.label;
    }
  }

  private static final class UsageRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                   boolean leaf, int row, boolean focused) {
      super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, focused);
      if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof UsageNode usage) {
        this.setText(usage.label());
        this.setIcon(usage.icon());
      }
      this.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
      return this;
    }
  }
}

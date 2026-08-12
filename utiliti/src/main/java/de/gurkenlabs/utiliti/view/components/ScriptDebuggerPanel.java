package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.controller.debug.ScriptDebugSnapshot;
import de.gurkenlabs.utiliti.controller.debug.ScriptDebuggerBackend;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Compact bottom-panel controls and paused-state inspection for runtime scripts. */
final class ScriptDebuggerPanel extends JPanel {
  private static final Dimension CONTROL_SIZE = new Dimension(28, 26);
  private final JLabel stateLabel = new JLabel("Debugger disconnected");
  private final JLabel stackTitle = sectionTitle("Call Stack (0)");
  private final JLabel variableTitle = sectionTitle("Variables (0)");
  private final DefaultListModel<ScriptDebugSnapshot.Frame> framesModel = new DefaultListModel<>();
  private final JList<ScriptDebugSnapshot.Frame> frames = new JList<>(this.framesModel);
  private final DefaultTableModel variablesModel = new DefaultTableModel(new Object[] {"Name", "Value", "Type"}, 0) {
    @Override public boolean isCellEditable(int row, int column) { return false; }
  };
  private final JTable variables = new JTable(this.variablesModel);
  private final List<VariableRow> variableRows = new ArrayList<>();
  private final JButton resume = control(Icons.GREEN_PLAY_16, "Continue (F5)");
  private final JButton pause = control(Icons.PAUSE_16, "Pause");
  private final JButton stepOver = control(new StepIcon(StepKind.OVER), "Step Over (F10)");
  private final JButton stepInto = control(new StepIcon(StepKind.INTO), "Step Into (F11)");
  private final JButton stepOut = control(new StepIcon(StepKind.OUT), "Step Out (Shift+F11)");
  private final JButton stop = control(Icons.RED_STOP_16, "Stop Debugging (Ctrl+F2)");
  private final JToggleButton showFrameworkFrames = toggleControl(new FilterIcon(), "Show engine and JDK frames");
  private List<ScriptDebugSnapshot.Frame> allFrames = List.of();
  private Runnable resumeAction = () -> {};
  private Runnable pauseAction = () -> {};
  private Runnable stepIntoAction = () -> {};
  private Runnable stepOverAction = () -> {};
  private Runnable stepOutAction = () -> {};
  private Runnable stopAction = () -> {};
  private Consumer<ScriptDebugSnapshot.Frame> frameAction = ignored -> {};
  private Consumer<ScriptDebugSnapshot.Variable> expandAction = ignored -> {};
  private boolean refreshingFrames;

  ScriptDebuggerPanel() {
    super(new BorderLayout());
    this.setBackground(Style.COLOR_BG);
    this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()));
    this.add(this.createToolbar(), BorderLayout.NORTH);
    this.add(this.createContent(), BorderLayout.CENTER);

    this.resume.addActionListener(event -> this.resumeAction.run());
    this.pause.addActionListener(event -> this.pauseAction.run());
    this.stepInto.addActionListener(event -> this.stepIntoAction.run());
    this.stepOver.addActionListener(event -> this.stepOverAction.run());
    this.stepOut.addActionListener(event -> this.stepOutAction.run());
    this.stop.addActionListener(event -> this.stopAction.run());
    this.showFrameworkFrames.addActionListener(event -> this.refreshFrames());
    this.updateState(ScriptDebuggerBackend.State.DISCONNECTED, "Debugger disconnected");
  }

  void onResume(Runnable action) { this.resumeAction = action == null ? () -> {} : action; }
  void onPause(Runnable action) { this.pauseAction = action == null ? () -> {} : action; }
  void onStepInto(Runnable action) { this.stepIntoAction = action == null ? () -> {} : action; }
  void onStepOver(Runnable action) { this.stepOverAction = action == null ? () -> {} : action; }
  void onStepOut(Runnable action) { this.stepOutAction = action == null ? () -> {} : action; }
  void onStop(Runnable action) { this.stopAction = action == null ? () -> {} : action; }
  void onFrameSelected(Consumer<ScriptDebugSnapshot.Frame> action) { this.frameAction = action == null ? ignored -> {} : action; }
  void onExpandVariable(Consumer<ScriptDebugSnapshot.Variable> action) { this.expandAction = action == null ? ignored -> {} : action; }

  void updateState(ScriptDebuggerBackend.State state, String detail) {
    String label = switch (state) {
      case ATTACHING -> "Attaching";
      case RUNNING -> "Running";
      case PAUSED -> "Paused";
      case FAILED -> "Failed";
      case DISCONNECTED -> "Disconnected";
    };
    this.stateLabel.setText(label + (detail == null || detail.isBlank() || detail.equalsIgnoreCase(label)
        ? "" : "  \u00b7  " + detail));
    this.stateLabel.setForeground(switch (state) {
      case RUNNING -> new Color(53, 208, 115);
      case PAUSED -> new Color(224, 175, 104);
      case FAILED -> new Color(229, 87, 86);
      default -> Style.mutedText();
    });
    boolean pausedState = state == ScriptDebuggerBackend.State.PAUSED;
    this.resume.setEnabled(pausedState);
    this.pause.setEnabled(state == ScriptDebuggerBackend.State.RUNNING);
    this.stepInto.setEnabled(pausedState);
    this.stepOver.setEnabled(pausedState);
    this.stepOut.setEnabled(pausedState);
    this.stop.setEnabled(state != ScriptDebuggerBackend.State.DISCONNECTED && state != ScriptDebuggerBackend.State.FAILED);
    if (state == ScriptDebuggerBackend.State.DISCONNECTED) {
      this.allFrames = List.of();
      this.refreshFrames();
      this.variablesModel.setRowCount(0);
      this.variableTitle.setText("Variables (0)");
    } else if (state == ScriptDebuggerBackend.State.RUNNING || state == ScriptDebuggerBackend.State.ATTACHING) {
      this.variableRows.clear();
      this.rebuildVariables();
    }
  }

  void showSnapshot(ScriptDebugSnapshot snapshot, ScriptDebugSnapshot.Frame preferred) {
    this.allFrames = snapshot == null ? List.of() : snapshot.frames();
    this.refreshFrames(preferred);
  }

  private JPanel createToolbar() {
    JPanel toolbar = new JPanel(new BorderLayout());
    toolbar.setOpaque(true);
    toolbar.setBackground(Style.background());
    toolbar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
    controls.setOpaque(false);
    controls.add(this.resume);
    controls.add(this.pause);
    controls.add(separator());
    controls.add(this.stepOver);
    controls.add(this.stepInto);
    controls.add(this.stepOut);
    controls.add(separator());
    controls.add(this.stop);
    toolbar.add(controls, BorderLayout.WEST);

    this.stateLabel.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 11f));
    this.stateLabel.setHorizontalAlignment(SwingConstants.CENTER);
    toolbar.add(this.stateLabel, BorderLayout.CENTER);

    return toolbar;
  }

  private Component createContent() {
    this.frames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.frames.setBackground(Style.COLOR_BG);
    this.frames.setForeground(Style.text());
    this.frames.setSelectionBackground(Style.sceneRowSelected());
    this.frames.setFixedCellHeight(28);
    this.frames.setCellRenderer(new FrameRenderer());
    this.frames.addListSelectionListener(event -> {
      if (event.getValueIsAdjusting() || this.refreshingFrames) return;
      ScriptDebugSnapshot.Frame frame = this.frames.getSelectedValue();
      this.showVariables(frame);
      if (frame != null) this.frameAction.accept(frame);
    });

    this.variables.setBackground(Style.COLOR_BG);
    this.variables.setForeground(Style.text());
    this.variables.setSelectionBackground(Style.sceneRowSelected());
    this.variables.setGridColor(Style.border());
    this.variables.setRowHeight(26);
    this.variables.setShowVerticalLines(false);
    this.variables.setIntercellSpacing(new Dimension(0, 0));
    this.variables.setDefaultRenderer(Object.class, new VariableRenderer());
    this.variables.getTableHeader().setBackground(Style.background());
    this.variables.getTableHeader().setForeground(Style.mutedText());
    this.variables.getTableHeader().setReorderingAllowed(false);
    this.variables.getColumnModel().getColumn(0).setPreferredWidth(150);
    this.variables.getColumnModel().getColumn(1).setPreferredWidth(320);
    this.variables.getColumnModel().getColumn(2).setPreferredWidth(220);
    this.variables.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent event) {
        int row = variables.rowAtPoint(event.getPoint());
        int column = variables.columnAtPoint(event.getPoint());
        if (row >= 0 && column == 0) toggleVariable(row);
      }
    });

    JScrollPane frameScroll = scroll(this.frames);
    JScrollPane variableScroll = scroll(this.variables);
    JPanel stackPane = section(this.stackTitle, frameScroll, this.showFrameworkFrames);
    JPanel variablesPane = section(this.variableTitle, variableScroll, null);
    stackPane.setMinimumSize(new Dimension(260, 80));
    variablesPane.setMinimumSize(new Dimension(340, 80));

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, stackPane, variablesPane);
    UI.configureSplitPane(split);
    split.setBorder(null);
    split.setResizeWeight(0.38);
    split.setDividerLocation(420);
    return split;
  }

  private void refreshFrames() {
    this.refreshFrames(this.frames.getSelectedValue());
  }

  private void refreshFrames(ScriptDebugSnapshot.Frame preferred) {
    this.refreshingFrames = true;
    try {
      this.framesModel.clear();
      boolean hasUserFrame = this.allFrames.stream().anyMatch(frame -> !isFrameworkFrame(frame));
      for (int index = 0; index < this.allFrames.size(); index++) {
        ScriptDebugSnapshot.Frame frame = this.allFrames.get(index);
        if (this.showFrameworkFrames.isSelected() || frame.equals(preferred) || !isFrameworkFrame(frame)
            || (!hasUserFrame && index == 0)) this.framesModel.addElement(frame);
      }
      this.stackTitle.setText("Call Stack (" + this.framesModel.size() + ")");
      if (preferred != null && this.framesModel.contains(preferred)) this.frames.setSelectedValue(preferred, true);
      else if (!this.framesModel.isEmpty()) this.frames.setSelectedIndex(0);
    } finally {
      this.refreshingFrames = false;
    }
    ScriptDebugSnapshot.Frame selected = this.frames.getSelectedValue();
    this.showVariables(selected);
    if (selected != null) this.frameAction.accept(selected);
  }

  private void showVariables(ScriptDebugSnapshot.Frame frame) {
    this.variableRows.clear();
    if (frame != null) frame.variables().forEach(variable -> this.variableRows.add(new VariableRow(variable, 0)));
    this.rebuildVariables();
  }

  void showVariableChildren(String reference, List<ScriptDebugSnapshot.Variable> children) {
    VariableRow target = this.variableRows.stream()
        .filter(row -> row.loading && java.util.Objects.equals(reference, row.variable.reference()))
        .findFirst().orElseGet(() -> this.variableRows.stream()
            .filter(row -> java.util.Objects.equals(reference, row.variable.reference())).findFirst().orElse(null));
    if (target == null) return;
    for (int index = 0; index < this.variableRows.size(); index++) {
      VariableRow row = this.variableRows.get(index);
      if (row != target) continue;
      row.loading = false;
      row.children = children == null ? List.of() : List.copyOf(children);
      if (!row.expanded) {
        row.expanded = true;
        this.insertChildren(index, row);
      }
      this.rebuildVariables();
      return;
    }
  }

  private void toggleVariable(int index) {
    if (index < 0 || index >= this.variableRows.size()) return;
    VariableRow row = this.variableRows.get(index);
    if (!row.variable.expandable() || row.loading) return;
    if (row.expanded) {
      row.expanded = false;
      while (index + 1 < this.variableRows.size() && this.variableRows.get(index + 1).depth > row.depth) {
        this.variableRows.remove(index + 1);
      }
      this.rebuildVariables();
    } else if (row.children != null) {
      row.expanded = true;
      this.insertChildren(index, row);
      this.rebuildVariables();
    } else {
      row.loading = true;
      this.rebuildVariables();
      this.expandAction.accept(row.variable);
    }
  }

  private void insertChildren(int parentIndex, VariableRow parent) {
    int offset = 1;
    for (ScriptDebugSnapshot.Variable child : parent.children) {
      this.variableRows.add(parentIndex + offset++, new VariableRow(child, parent.depth + 1));
    }
  }

  private void rebuildVariables() {
    this.variablesModel.setRowCount(0);
    this.variableTitle.setText("Variables (" + this.variableRows.size() + ")");
    for (VariableRow row : this.variableRows) {
      this.variablesModel.addRow(new Object[] { row, row.variable.value(), row.variable.type() });
    }
  }

  private static boolean isFrameworkFrame(ScriptDebugSnapshot.Frame frame) {
    String name = frame.className();
    return name.startsWith("java.") || name.startsWith("jdk.") || name.startsWith("sun.")
        || name.startsWith("com.sun.") || name.startsWith("de.gurkenlabs.litiengine.");
  }

  private static JButton control(Icon icon, String tooltip) {
    JButton button = new JButton(icon);
    button.setPreferredSize(CONTROL_SIZE);
    button.setMinimumSize(CONTROL_SIZE);
    button.setMaximumSize(CONTROL_SIZE);
    button.setToolTipText(tooltip);
    button.getAccessibleContext().setAccessibleName(tooltip);
    button.setFocusable(false);
    button.setMargin(new java.awt.Insets(0, 0, 0, 0));
    Style.styleButton(button, Style.ButtonVariant.TOOLBAR);
    return button;
  }

  private static JToggleButton toggleControl(Icon icon, String tooltip) {
    JToggleButton button = new JToggleButton(icon);
    button.setPreferredSize(CONTROL_SIZE);
    button.setMinimumSize(CONTROL_SIZE);
    button.setMaximumSize(CONTROL_SIZE);
    button.setToolTipText(tooltip);
    button.getAccessibleContext().setAccessibleName(tooltip);
    button.setFocusable(false);
    button.setMargin(new java.awt.Insets(0, 0, 0, 0));
    Style.styleButton(button, Style.ButtonVariant.TOOLBAR);
    return button;
  }

  private static JLabel sectionTitle(String text) {
    JLabel label = new JLabel(text);
    label.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 11f));
    label.setForeground(Style.text());
    return label;
  }

  private static JPanel section(JLabel titleLabel, Component content, Component action) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(true);
    panel.setBackground(Style.COLOR_BG);
    JPanel header = new JPanel(new BorderLayout());
    header.setOpaque(true);
    header.setBackground(Style.background());
    header.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()),
        BorderFactory.createEmptyBorder(2, 10, 2, 8)));
    header.add(titleLabel, BorderLayout.WEST);

    if (action != null) {
      header.add(action, BorderLayout.EAST);
    }

    panel.add(header, BorderLayout.NORTH);
    panel.add(content, BorderLayout.CENTER);
    return panel;
  }

  private static JLabel sectionCount() {
    JLabel count = new JLabel("0");
    count.setFont(Style.getDefaultFont().deriveFont(10f));
    count.setForeground(Style.mutedText());
    return count;
  }

  private static JScrollPane scroll(Component component) {
    JScrollPane scroll = new JScrollPane(component);
    scroll.setBorder(null);
    scroll.getViewport().setBackground(Style.COLOR_BG);
    return scroll;
  }

  private static JPanel separator() {
    JPanel separator = new JPanel();
    separator.setBackground(Style.border());
    separator.setPreferredSize(new Dimension(1, 18));
    separator.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
    return separator;
  }

  private static final class FrameRenderer extends JPanel implements ListCellRenderer<ScriptDebugSnapshot.Frame> {
    private final JLabel method = new JLabel();
    private final JLabel location = new JLabel();

    private FrameRenderer() {
      super(new BorderLayout(8, 0));
      this.setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 9));
      this.method.setFont(Style.getDefaultFont().deriveFont(Font.PLAIN, 12f));
      this.location.setFont(Style.getDefaultFont().deriveFont(10f));
      this.location.setHorizontalAlignment(SwingConstants.TRAILING);
      this.add(this.method, BorderLayout.CENTER);
      this.add(this.location, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ScriptDebugSnapshot.Frame> list,
        ScriptDebugSnapshot.Frame frame, int index, boolean selected, boolean focused) {
      String simpleClass = frame.className().substring(frame.className().lastIndexOf('.') + 1);
      this.method.setText(simpleClass + "." + frame.method() + "()");
      this.location.setText(frame.source() + ":" + frame.line());
      this.setOpaque(true);
      this.setBackground(selected ? Style.sceneRowSelected() : Style.COLOR_BG);
      this.method.setForeground(selected ? Color.WHITE : Style.text());
      this.location.setForeground(selected ? new Color(205, 215, 230) : Style.mutedText());
      return this;
    }
  }

  private static final class DisclosureIcon implements Icon {
    private final boolean expandable;
    private final boolean expanded;

    DisclosureIcon(boolean expandable, boolean expanded) {
      this.expandable = expandable;
      this.expanded = expanded;
    }

    @Override public int getIconWidth() { return 10; }
    @Override public int getIconHeight() { return 10; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (!expandable) return;
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Style.mutedText());
        Path2D p = new Path2D.Double();
        if (expanded) {
          p.moveTo(x + 1, y + 3);
          p.lineTo(x + 9, y + 3);
          p.lineTo(x + 5, y + 8);
        } else {
          p.moveTo(x + 3, y + 1);
          p.lineTo(x + 8, y + 5);
          p.lineTo(x + 3, y + 9);
        }
        p.closePath();
        g2.fill(p);
      } finally {
        g2.dispose();
      }
    }
  }

  private static final class VariableRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
        boolean focused, int row, int column) {
      super.getTableCellRendererComponent(table, value, selected, focused, row, column);
      this.setIcon(null);
      this.setBorder(BorderFactory.createEmptyBorder(0, 9, 0, 9));
      if (column == 0 && value instanceof VariableRow variableRow) {
        this.setIcon(new DisclosureIcon(variableRow.variable.expandable(), variableRow.expanded));
        this.setIconTextGap(6);
        this.setText(variableRow.variable.name() + (variableRow.loading ? " (loading...)" : ""));
        this.setBorder(BorderFactory.createEmptyBorder(0, 9 + variableRow.depth * 16, 0, 9));
      }
      this.setFont(Style.getDefaultFont().deriveFont(column == 0 ? Font.BOLD : Font.PLAIN, 11f));
      this.setForeground(selected ? Color.WHITE : column == 2 ? Style.mutedText() : Style.text());
      this.setBackground(selected ? Style.sceneRowSelected() : Style.COLOR_BG);
      this.setToolTipText(value == null ? null : value.toString());
      return this;
    }
  }

  private static final class VariableRow {
    private final ScriptDebugSnapshot.Variable variable;
    private final int depth;
    private List<ScriptDebugSnapshot.Variable> children;
    private boolean expanded;
    private boolean loading;

    private VariableRow(ScriptDebugSnapshot.Variable variable, int depth) {
      this.variable = variable;
      this.depth = depth;
    }

    @Override
    public String toString() {
      return this.variable.name();
    }
  }

  private static final class FilterIcon implements Icon {
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        boolean selected = c instanceof AbstractButton b && b.isSelected();
        g2.setColor(selected ? Style.COLOR_ACCENT_BLUE : c.isEnabled() ? Style.text() : Style.mutedText());
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 2, y + 4, x + 14, y + 4);
        g2.drawLine(x + 5, y + 8, x + 11, y + 8);
        g2.drawLine(x + 7, y + 12, x + 9, y + 12);
      } finally {
        g2.dispose();
      }
    }
  }

  private enum StepKind { OVER, INTO, OUT }

  private record StepIcon(StepKind kind) implements Icon {
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      Graphics2D g = (Graphics2D) graphics.create();
      try {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(component.isEnabled() ? Style.text() : Style.mutedText().darker());
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if (this.kind == StepKind.OVER) {
          Path2D curve = new Path2D.Double();
          curve.moveTo(x + 3, y + 9);
          curve.curveTo(x + 3, y + 3, x + 13, y + 3, x + 13, y + 9);
          g.draw(curve);

          Path2D arrow = new Path2D.Double();
          arrow.moveTo(x + 10, y + 7);
          arrow.lineTo(x + 13, y + 10);
          arrow.lineTo(x + 16, y + 7);
          g.draw(arrow);
        } else if (this.kind == StepKind.INTO) {
          g.drawLine(x + 8, y + 3, x + 8, y + 11);
          Path2D arrow = new Path2D.Double();
          arrow.moveTo(x + 5, y + 8);
          arrow.lineTo(x + 8, y + 11);
          arrow.lineTo(x + 11, y + 8);
          g.draw(arrow);
          g.drawLine(x + 3, y + 13, x + 13, y + 13);
        } else if (this.kind == StepKind.OUT) {
          g.drawLine(x + 8, y + 11, x + 8, y + 3);
          Path2D arrow = new Path2D.Double();
          arrow.moveTo(x + 5, y + 6);
          arrow.lineTo(x + 8, y + 3);
          arrow.lineTo(x + 11, y + 6);
          g.draw(arrow);
          g.drawLine(x + 3, y + 13, x + 13, y + 13);
        }
      } finally {
        g.dispose();
      }
    }
  }
}

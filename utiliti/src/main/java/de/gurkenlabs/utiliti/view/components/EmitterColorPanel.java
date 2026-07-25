package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.components.border.DarkBorders;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterAttributes;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class EmitterColorPanel extends PropertyPanel {
  private final DefaultTableModel model;
  private final JTable table;
  private final JButton btnAdd;
  private final JButton btnDuplicate;
  private final JButton btnRemove;
  private final ColorComponent colorEditor;
  private final CardLayout listLayout;
  private final JPanel listCards;
  private final JSpinner colorVariance;
  private final JSpinner alphaVariance;
  private final VariationPreview colorPreview;
  private final VariationPreview alphaPreview;
  private boolean updatingEditor;

  public EmitterColorPanel() {
    super();
    model = new DefaultTableModel(0, 1) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    table = new JTable(model);
    table.getColumnModel().getColumn(0).setCellRenderer(new ColorRowRenderer());
    table.setTableHeader(null);
    table.setRowHeight(CONTROL_HEIGHT + CONTROL_MARGIN * 2);
    table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    table.setDropMode(DropMode.INSERT_ROWS);
    table.setTransferHandler(new ColorRowTransferHandler());
    if (!GraphicsEnvironment.isHeadless()) {
      table.setDragEnabled(true);
    }
    JScrollPane scrollPanel = new JScrollPane(table);
    scrollPanel.setBorder(DarkBorders.createLineBorder(1, 1, 1, 1));
    scrollPanel.setPreferredSize(new Dimension(CONTROL_WIDTH, CONTROL_HEIGHT * 3));

    JPanel emptyState = new JPanel(new GridBagLayout());
    emptyState.setOpaque(false);
    JLabel emptyLabel = new JLabel("<html><center>" + Resources.strings().get("emitter_colorsEmpty") + "</center></html>");
    emptyLabel.setForeground(Style.mutedText());
    emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
    emptyState.add(emptyLabel);
    listLayout = new CardLayout();
    listCards = new JPanel(listLayout);
    listCards.add(scrollPanel, "list");
    listCards.add(emptyState, "empty");

    btnAdd = Style.iconButton(Icons.ADD_16);
    btnAdd.setToolTipText(Resources.strings().get("emitter_addColor"));
    btnDuplicate = Style.iconButton(Icons.COPY_16);
    btnDuplicate.setToolTipText(Resources.strings().get("emitter_duplicateColor"));
    btnRemove = Style.iconButton(Icons.DELETE_16);
    btnRemove.setToolTipText(Resources.strings().get("emitter_removeColor"));
    Style.styleButton(btnRemove, Style.ButtonVariant.DESTRUCTIVE);
    btnDuplicate.setEnabled(false);
    btnRemove.setEnabled(false);

    Box actionRail = Box.createVerticalBox();
    actionRail.setOpaque(false);
    actionRail.add(btnAdd);
    actionRail.add(Box.createVerticalStrut(CONTROL_MARGIN));
    actionRail.add(btnDuplicate);
    actionRail.add(Box.createVerticalStrut(CONTROL_MARGIN));
    actionRail.add(btnRemove);
    actionRail.add(Box.createVerticalGlue());

    colorEditor = new ColorComponent(EmitterAttributes.DEFAULT_COLOR);
    colorEditor.setSeparateAlphaField(true);
    colorEditor.setEnabled(false);
    colorEditor.setClearAction(() -> removeRow(table.getSelectedRow()));

    JPanel colorControls = new JPanel(new BorderLayout(CONTROL_MARGIN, CONTROL_MARGIN));
    colorControls.setOpaque(false);
    colorControls.setBorder(new EmptyBorder(CONTROL_MARGIN * 2, CONTROL_MARGIN * 3,
        CONTROL_MARGIN * 2, CONTROL_MARGIN * 3));
    colorControls.add(actionRail, BorderLayout.LINE_START);
    colorControls.add(listCards, BorderLayout.CENTER);
    colorControls.add(colorEditor, BorderLayout.PAGE_END);

    colorVariance = percentageSpinner(EmitterAttributes.DEFAULT_COLOR_VARIANCE);
    alphaVariance = percentageSpinner(EmitterAttributes.DEFAULT_ALPHA_VARIANCE);
    colorVariance.setToolTipText(Resources.strings().get("emitter_colorVariance_tip"));
    alphaVariance.setToolTipText(Resources.strings().get("emitter_alphaVariance_tip"));
    colorPreview = new VariationPreview(false);
    alphaPreview = new VariationPreview(true);

    CollapsibleSection colors = new CollapsibleSection("particle_colors", colorControls);
    colors.setInfoText("particle_colors_info");
    CollapsibleSection randomization = new CollapsibleSection(
        "emitter_sectionRandomization", createRandomizationPanel());
    randomization.setInfoText("emitter_sectionRandomization_info");

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(colors);
    add(javax.swing.Box.createVerticalStrut(CONTROL_MARGIN * 2));
    add(randomization);
    setupChangedListeners();
    updateEmptyState();
  }

  private static JSpinner percentageSpinner(double initialValue) {
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(
        (int) Math.round(initialValue * 100), 0, 100, 1));
    spinner.setEditor(new JSpinner.NumberEditor(spinner, "0'%'"));
    spinner.setPreferredSize(SPINNER_SIZE);
    return spinner;
  }

  private JPanel createRandomizationPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setOpaque(false);
    panel.setBorder(new EmptyBorder(CONTROL_MARGIN * 2, CONTROL_MARGIN * 3,
        CONTROL_MARGIN * 2, CONTROL_MARGIN * 3));
    addRandomizationRow(panel, 0, "emitter_colorVariance", colorVariance, colorPreview);
    addRandomizationRow(panel, 1, "emitter_alphaVariance", alphaVariance, alphaPreview);
    return panel;
  }

  private static void addRandomizationRow(
      JPanel panel, int row, String labelKey, JSpinner spinner, Component preview) {
    GridBagConstraints label = constraints(0, row, 0);
    label.anchor = GridBagConstraints.LINE_END;
    label.insets = new java.awt.Insets(CONTROL_MARGIN, 0, CONTROL_MARGIN, GUTTER_WIDTH);
    panel.add(new JLabel(Resources.strings().get(labelKey)), label);

    GridBagConstraints value = constraints(1, row, 0);
    value.insets = new java.awt.Insets(CONTROL_MARGIN, 0, CONTROL_MARGIN, GUTTER_WIDTH * 2);
    panel.add(spinner, value);

    GridBagConstraints result = constraints(2, row, 1);
    result.fill = GridBagConstraints.HORIZONTAL;
    result.insets = new java.awt.Insets(CONTROL_MARGIN, 0, CONTROL_MARGIN, 0);
    panel.add(preview, result);
  }

  private static GridBagConstraints constraints(int x, int y, double weight) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.weightx = weight;
    return constraints;
  }

  @Override
  protected void clearControls() {
    model.setRowCount(0);
    table.clearSelection();
    colorEditor.setEnabled(false);
    btnDuplicate.setEnabled(false);
    btnRemove.setEnabled(false);
    colorVariance.setValue(Math.round(EmitterAttributes.DEFAULT_COLOR_VARIANCE * 100));
    alphaVariance.setValue(Math.round(EmitterAttributes.DEFAULT_ALPHA_VARIANCE * 100));
    updateEmptyState();
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    setColors(mapObject.getStringValue(MapObjectProperty.Emitter.COLORS, null));
    colorVariance.setValue((int) Math.round(mapObject.getDoubleValue(
        MapObjectProperty.Emitter.COLORVARIANCE, EmitterAttributes.DEFAULT_COLOR_VARIANCE) * 100));
    alphaVariance.setValue((int) Math.round(mapObject.getDoubleValue(
        MapObjectProperty.Emitter.ALPHAVARIANCE, EmitterAttributes.DEFAULT_ALPHA_VARIANCE) * 100));
  }

  private void setColors(String commaSeparatedHexStrings) {
    model.setRowCount(0);
    if (commaSeparatedHexStrings != null && !commaSeparatedHexStrings.isBlank()) {
      for (String color : commaSeparatedHexStrings.split(",")) {
        model.addRow(new Object[] {color});
      }
      table.setRowSelectionInterval(0, 0);
    } else {
      colorEditor.setEnabled(false);
    }
    updateEmptyState();
  }

  private void setupChangedListeners() {
    btnAdd.addActionListener(event -> {
      model.addRow(new Object[] {ColorHelper.encode(EmitterAttributes.DEFAULT_COLOR.brighter())});
      table.setRowSelectionInterval(model.getRowCount() - 1, model.getRowCount() - 1);
    });
    btnDuplicate.addActionListener(event -> duplicateRow(table.getSelectedRow()));
    btnRemove.addActionListener(event -> removeRow(table.getSelectedRow()));
    table.getSelectionModel().addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting()) {
        updateEditor();
        boolean selected = table.getSelectedRow() >= 0;
        btnDuplicate.setEnabled(selected);
        btnRemove.setEnabled(selected);
        colorPreview.repaint();
        alphaPreview.repaint();
      }
    });
    table.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mousePressed(java.awt.event.MouseEvent event) {
        if (event.isPopupTrigger()) {
          int row = table.rowAtPoint(event.getPoint());
          if (row >= 0) {
            table.setRowSelectionInterval(row, row);
            showRowMenu(event, row);
          }
        }
      }

      @Override
      public void mouseReleased(java.awt.event.MouseEvent event) {
        if (event.isPopupTrigger()) {
          mousePressed(event);
        }
      }
    });
    colorEditor.addActionListener(event -> {
      int row = table.getSelectedRow();
      if (!updatingEditor && row >= 0 && colorEditor.getColor() != null) {
        model.setValueAt(ColorHelper.encode(colorEditor.getColor()), row, 0);
      }
    });
    model.addTableModelListener(event -> {
      updateEmptyState();
      colorPreview.repaint();
      alphaPreview.repaint();
    });
    colorVariance.addChangeListener(event -> colorPreview.repaint());
    alphaVariance.addChangeListener(event -> alphaPreview.repaint());
    setup(table, MapObjectProperty.Emitter.COLORS);
    setupPercentageSpinner(colorVariance, MapObjectProperty.Emitter.COLORVARIANCE);
    setupPercentageSpinner(alphaVariance, MapObjectProperty.Emitter.ALPHAVARIANCE);
  }

  private void setupPercentageSpinner(JSpinner spinner, String property) {
    spinner.addChangeListener(new MapObjectPropertyChangeListener(
        mapObject -> Math.round(mapObject.getDoubleValue(property, 0) * 100)
            != ((Number) spinner.getValue()).intValue(),
        mapObject -> mapObject.setValue(property, ((Number) spinner.getValue()).doubleValue() / 100)));
  }

  private void showRowMenu(java.awt.event.MouseEvent event, int row) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem duplicate = new JMenuItem(Resources.strings().get("emitter_duplicateColor"), Icons.COPY_16);
    duplicate.addActionListener(action -> duplicateRow(row));
    JMenuItem remove = new JMenuItem(Resources.strings().get("emitter_removeColor"), Icons.DELETE_16);
    remove.addActionListener(action -> removeRow(row));
    menu.add(duplicate);
    menu.add(remove);
    menu.show(table, event.getX(), event.getY());
  }

  private void duplicateRow(int row) {
    if (row < 0 || row >= model.getRowCount()) {
      return;
    }
    model.insertRow(row + 1, new Object[] {model.getValueAt(row, 0)});
    table.setRowSelectionInterval(row + 1, row + 1);
  }

  private void removeRow(int row) {
    if (row < 0 || row >= model.getRowCount()) {
      return;
    }
    model.removeRow(row);
    if (model.getRowCount() > 0) {
      int next = Math.min(row, model.getRowCount() - 1);
      table.setRowSelectionInterval(next, next);
    } else {
      colorEditor.setEnabled(false);
      btnDuplicate.setEnabled(false);
      btnRemove.setEnabled(false);
    }
  }

  private void updateEditor() {
    int row = table.getSelectedRow();
    colorEditor.setEnabled(row >= 0);
    if (row < 0) {
      return;
    }
    Color color = ColorHelper.decode(String.valueOf(model.getValueAt(row, 0)));
    if (color == null) {
      return;
    }
    updatingEditor = true;
    try {
      colorEditor.setColor(color);
    } finally {
      updatingEditor = false;
    }
  }

  private void updateEmptyState() {
    listLayout.show(listCards, model.getRowCount() == 0 ? "empty" : "list");
  }

  private List<Color> colors() {
    List<Color> colors = new ArrayList<>();
    for (int row = 0; row < model.getRowCount(); row++) {
      Color color = ColorHelper.decode(String.valueOf(model.getValueAt(row, 0)));
      if (color != null) {
        colors.add(color);
      }
    }
    return colors;
  }

  private final class VariationPreview extends JPanel {
    private final boolean alpha;

    private VariationPreview(boolean alpha) {
      this.alpha = alpha;
      setPreferredSize(new Dimension(CONTROL_MIN_WIDTH, CONTROL_HEIGHT));
      setMinimumSize(new Dimension(CONTROL_HEIGHT, CONTROL_HEIGHT));
      setBorder(BorderFactory.createLineBorder(Style.border()));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);
      Graphics2D g = (Graphics2D) graphics.create();
      try {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        List<Color> palette = colors();
        int selectedRow = table.getSelectedRow();
        Color selectedColor = selectedRow >= 0 && selectedRow < model.getRowCount()
            ? ColorHelper.decode(String.valueOf(model.getValueAt(selectedRow, 0)))
            : null;
        Color base = selectedColor != null
            ? selectedColor
            : palette.isEmpty() ? Color.WHITE : palette.get(0);
        double variance = ((Number) (alpha ? alphaVariance.getValue() : colorVariance.getValue()))
            .doubleValue() / 100;
        int steps = 12;
        for (int index = 0; index < steps; index++) {
          double position = steps == 1 ? 0 : index / (double) (steps - 1);
          Color sample = alpha
              ? new Color(base.getRed(), base.getGreen(), base.getBlue(),
                  clamp((int) Math.round(base.getAlpha()
                      + 255 * (position * 2 - 1) * variance)))
              : shift(base, (position * 2 - 1) * variance);
          g.setColor(sample);
          int left = index * getWidth() / steps;
          int right = (index + 1) * getWidth() / steps;
          g.fillRect(left, 0, right - left, getHeight());
        }
      } finally {
        g.dispose();
      }
    }

    private Color shift(Color color, double amount) {
      int target = amount >= 0 ? 255 : 0;
      double strength = Math.abs(amount);
      return new Color(
          clamp((int) Math.round(color.getRed() + (target - color.getRed()) * strength)),
          clamp((int) Math.round(color.getGreen() + (target - color.getGreen()) * strength)),
          clamp((int) Math.round(color.getBlue() + (target - color.getBlue()) * strength)),
          color.getAlpha());
    }

    private int clamp(int value) {
      return Math.max(0, Math.min(255, value));
    }
  }

  private final class ColorRowTransferHandler extends TransferHandler {
    private int sourceRow = -1;

    @Override
    protected Transferable createTransferable(javax.swing.JComponent component) {
      sourceRow = table.getSelectedRow();
      return new StringSelection(Integer.toString(sourceRow));
    }

    @Override
    public int getSourceActions(javax.swing.JComponent component) {
      return MOVE;
    }

    @Override
    public boolean canImport(TransferSupport support) {
      return support.isDrop() && support.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    @Override
    public boolean importData(TransferSupport support) {
      if (!canImport(support) || sourceRow < 0) {
        return false;
      }
      JTable.DropLocation location = (JTable.DropLocation) support.getDropLocation();
      int target = location.getRow();
      if (target > sourceRow) {
        target--;
      }
      if (target == sourceRow || target < 0 || target >= model.getRowCount()) {
        return false;
      }
      model.moveRow(sourceRow, sourceRow, target);
      table.setRowSelectionInterval(target, target);
      sourceRow = -1;
      return true;
    }
  }

  private static final class ColorRowRenderer extends JPanel implements TableCellRenderer {
    private final JLabel swatch = new JLabel();
    private final JLabel value = new JLabel();
    private final JLabel alpha = new JLabel();

    private ColorRowRenderer() {
      super(new BorderLayout(CONTROL_MARGIN * 2, 0));
      setBorder(new EmptyBorder(CONTROL_MARGIN, CONTROL_MARGIN, CONTROL_MARGIN, CONTROL_MARGIN));
      swatch.setOpaque(true);
      swatch.setPreferredSize(new Dimension(CONTROL_HEIGHT * 2, CONTROL_HEIGHT));
      swatch.setBorder(BorderFactory.createLineBorder(Style.border()));
      alpha.setHorizontalAlignment(SwingConstants.TRAILING);
      alpha.setPreferredSize(new Dimension(SPINNER_WIDTH, CONTROL_HEIGHT));
      add(swatch, BorderLayout.LINE_START);
      add(value, BorderLayout.CENTER);
      add(alpha, BorderLayout.LINE_END);
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object rawValue, boolean selected, boolean focus, int row, int column) {
      Color color = ColorHelper.decode(String.valueOf(rawValue));
      if (color == null) {
        color = Color.WHITE;
      }
      swatch.setBackground(color);
      value.setText(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
      alpha.setText(Math.round(color.getAlpha() * 100 / 255f) + "%");
      Color background = selected ? table.getSelectionBackground() : table.getBackground();
      Color foreground = selected ? table.getSelectionForeground() : table.getForeground();
      setBackground(background);
      value.setForeground(foreground);
      alpha.setForeground(foreground);
      return this;
    }
  }
}

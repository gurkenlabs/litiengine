package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

public class MapPropertyPanel extends JPanel {
  private static final int CONTENT_WIDTH =
      PropertyPanel.LABEL_WIDTH + PropertyPanel.GUTTER_WIDTH + PropertyPanel.CONTROL_WIDTH;

  private final AmbientLightPreviewPanel ambientlightPreview;
  private final JScrollPane scrollPane;
  private final JSpinner spinnerGravity;
  private final ColorComponent ambientColorComponent;
  private final JEditorPane textFieldDesc;
  private final JTextField textFieldName;
  private final ColorComponent shadowColorComponent;
  private final JTextField textFieldTitle;
  private final JTable tableCustomProperties;
  private final DefaultTableModel model;

  private transient IMap dataSource;
  private boolean binding;

  public MapPropertyPanel() {
    setBorder(null);
    setLayout(new BorderLayout());
    setOpaque(true);
    setBackground(Style.COLOR_BG);

    this.textFieldName = ControlBehavior.apply(new JTextField());
    this.textFieldTitle = ControlBehavior.apply(new JTextField());
    this.textFieldDesc = new JEditorPane();
    JScrollPane scrollPaneDesc = new JScrollPane(this.textFieldDesc);
    scrollPaneDesc.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));

    this.spinnerGravity = new JSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    ControlBehavior.apply(this.spinnerGravity);

    this.ambientlightPreview = new AmbientLightPreviewPanel();
    this.ambientColorComponent = new ColorComponent(AmbientLight.DEFAULT_COLOR);
    this.ambientColorComponent.addActionListener(
        a -> {
          this.ambientlightPreview.setAmbientColor(this.ambientColorComponent.getColor());
          this.saveChanges();
        });

    this.shadowColorComponent = new ColorComponent(StaticShadow.DEFAULT_COLOR);
    this.shadowColorComponent.addActionListener(
        a -> {
          this.ambientlightPreview.setStaticShadowColor(this.shadowColorComponent.getColor());
          this.saveChanges();
        });

    JButton buttonAdd = createPillButton("+");
    JButton buttonRemove = createPillButton("-");

    this.scrollPane = new JScrollPane();
    this.tableCustomProperties = createPropertiesTable();
    this.scrollPane.setViewportView(this.tableCustomProperties);
    this.model = (DefaultTableModel) this.tableCustomProperties.getModel();

    buttonAdd.addActionListener(
        a -> {
          stopTableEditing();
          this.model.addRow(new Object[] {"", ""});
          this.saveChanges();
        });
    buttonRemove.addActionListener(
        a -> {
          stopTableEditing();
          int[] rows = this.tableCustomProperties.getSelectedRows();
          for (int i = 0; i < rows.length; i++) {
            this.model.removeRow(rows[i] - i);
          }
          this.saveChanges();
        });

    JPanel accordion = new JPanel();
    accordion.setLayout(new BoxLayout(accordion, BoxLayout.Y_AXIS));
    accordion.setOpaque(true);
    accordion.setBackground(Style.COLOR_BG);
    accordion.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 0));

    ExpandableCard generalCard =
        new ExpandableCard(Resources.strings().get("panel_general"), createGeneralPanel(scrollPaneDesc), true);
    ExpandableCard lightingCard =
        new ExpandableCard("Lighting", createLightingPanel(), true);
    ExpandableCard propertiesCard =
        new ExpandableCard(Resources.strings().get("panel_customProperties"), createPropertiesPanel(buttonAdd, buttonRemove), true);

    generalCard.setContentInsets(8, 0, 12, 0);
    lightingCard.setContentInsets(8, 0, 12, 0);
    propertiesCard.setContentInsets(8, 0, 12, 0);

    accordion.add(generalCard);
    accordion.add(lightingCard);
    accordion.add(propertiesCard);
    accordion.add(Box.createVerticalGlue());

    JScrollPane hostScrollPane = new JScrollPane(accordion);
    hostScrollPane.setBorder(null);
    hostScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    hostScrollPane.getViewport().setBackground(Style.COLOR_BG);
    add(hostScrollPane, BorderLayout.CENTER);

    this.setupChangeListeners();
  }

  private JPanel createGeneralPanel(JComponent scrollPaneDesc) {
    return createForm(
        new JLabel[] {
            createLabel(Resources.strings().get("panel_name")),
            createLabel(Resources.strings().get("panel_title")),
            createLabel(Resources.strings().get("panel_description")),
            createLabel(Resources.strings().get("panel_gravity")),
        },
        new JComponent[] {
            this.textFieldName,
            this.textFieldTitle,
            scrollPaneDesc,
            this.spinnerGravity,
        },
        new int[] {
            PropertyPanel.CONTROL_HEIGHT,
            PropertyPanel.CONTROL_HEIGHT,
            PropertyPanel.CONTROL_HEIGHT * 2,
            PropertyPanel.CONTROL_HEIGHT,
        });
  }

  private JPanel createLightingPanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(
        createForm(
            new JLabel[] {
                createLabel(Resources.strings().get("panel_ambientlight")),
                createLabel(Resources.strings().get("panel_staticshadows")),
            },
            new JComponent[] {
                this.ambientColorComponent,
                this.shadowColorComponent,
            },
            new int[] {
                this.ambientColorComponent.getPreferredSize().height,
                this.shadowColorComponent.getPreferredSize().height,
            }));
    panel.add(Box.createVerticalStrut(6));
    panel.add(createAlignedControl(this.ambientlightPreview, this.ambientlightPreview.getPreferredSize().height));
    return panel;
  }

  private JPanel createPropertiesPanel(JButton buttonAdd, JButton buttonRemove) {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(createPropertiesHeader(buttonAdd, buttonRemove));
    panel.add(Box.createVerticalStrut(6));
    panel.add(createAlignedControl(this.scrollPane, 150));
    return panel;
  }

  private JPanel createForm(JLabel[] labels, JComponent[] controls, int[] heights) {
    JPanel form = new JPanel();
    form.setOpaque(false);
    GroupLayout gl = new GroupLayout(form);
    form.setLayout(gl);
    gl.setAutoCreateGaps(false);

    GroupLayout.ParallelGroup labelGroup = gl.createParallelGroup(Alignment.TRAILING);
    GroupLayout.ParallelGroup controlGroup = gl.createParallelGroup(Alignment.LEADING);
    GroupLayout.SequentialGroup vertical = gl.createSequentialGroup();

    for (int i = 0; i < labels.length; i++) {
      labelGroup.addComponent(labels[i], PropertyPanel.LABEL_WIDTH, PropertyPanel.LABEL_WIDTH, PropertyPanel.LABEL_WIDTH);
      controlGroup.addComponent(controls[i], PropertyPanel.CONTROL_MIN_WIDTH, PropertyPanel.CONTROL_WIDTH, Short.MAX_VALUE);
      vertical
          .addGroup(
              gl.createParallelGroup(Alignment.LEADING)
                  .addComponent(labels[i], heights[i], heights[i], heights[i])
                  .addComponent(controls[i], heights[i], heights[i], heights[i]))
          .addGap(PropertyPanel.CONTROL_MARGIN);
    }

    gl.setHorizontalGroup(
        gl.createSequentialGroup()
            .addGroup(labelGroup)
            .addGap(PropertyPanel.GUTTER_WIDTH)
            .addGroup(controlGroup));
    gl.setVerticalGroup(vertical);

    setRowSize(form, form.getPreferredSize().height);
    return form;
  }

  private JPanel createAlignedControl(JComponent component, int height) {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    GroupLayout gl = new GroupLayout(panel);
    panel.setLayout(gl);
    gl.setHorizontalGroup(
        gl.createSequentialGroup()
            .addGap(PropertyPanel.LABEL_WIDTH + PropertyPanel.GUTTER_WIDTH)
            .addComponent(component, PropertyPanel.CONTROL_WIDTH, PropertyPanel.CONTROL_WIDTH, PropertyPanel.CONTROL_WIDTH));
    gl.setVerticalGroup(
        gl.createSequentialGroup().addComponent(component, height, height, height));
    setRowSize(panel, height);
    return panel;
  }

  private JPanel createPropertiesHeader(JButton buttonAdd, JButton buttonRemove) {
    JLabel label = new JLabel(Resources.strings().get("panel_customProperties"));
    label.setForeground(Style.COLOR_TEXT);
    label.setHorizontalAlignment(SwingConstants.TRAILING);
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    GroupLayout gl = new GroupLayout(panel);
    panel.setLayout(gl);
    gl.setHorizontalGroup(
        gl.createSequentialGroup()
            .addComponent(label, PropertyPanel.LABEL_WIDTH, PropertyPanel.LABEL_WIDTH, PropertyPanel.LABEL_WIDTH)
            .addGap(PropertyPanel.GUTTER_WIDTH)
            .addComponent(buttonAdd)
            .addGap(6)
            .addComponent(buttonRemove));
    gl.setVerticalGroup(
        gl.createParallelGroup(Alignment.CENTER)
            .addComponent(label)
            .addComponent(buttonAdd)
            .addComponent(buttonRemove));
    setRowSize(panel, Math.max(32, panel.getPreferredSize().height));
    return panel;
  }

  private static void setRowSize(JComponent component, int height) {
    Dimension size = new Dimension(CONTENT_WIDTH, height);
    component.setPreferredSize(size);
    component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    component.setAlignmentX(LEFT_ALIGNMENT);
  }

  private static JLabel createLabel(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(Style.COLOR_TEXT);
    label.setHorizontalAlignment(SwingConstants.TRAILING);
    return label;
  }

  private static JButton createPillButton(String text) {
    JButton btn = new JButton(text) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getModel().isRollover() ? Style.COLOR_SURFACE2.brighter() : Style.COLOR_SURFACE2);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2.setColor(Style.COLOR_BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
      }
    };
    btn.setOpaque(false);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setForeground(Style.COLOR_TEXT);
    btn.setPreferredSize(new Dimension(28, 22));
    return btn;
  }

  private JTable createPropertiesTable() {
    JTable table =
        new JTable() {
          private static final String EMPTY_TEXT = "No properties defined";

          @Override
          protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() == 0) {
              g.setColor(new Color(74, 74, 74));
              FontMetrics fm = g.getFontMetrics();
              int x = (getWidth() - fm.stringWidth(EMPTY_TEXT)) / 2;
              int y = getHeight() / 2;
              g.drawString(EMPTY_TEXT, x, y);
            }
          }
        };
    table.getTableHeader().setReorderingAllowed(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setShowGrid(false);
    table.setIntercellSpacing(new Dimension(0, 0));
    table.setFillsViewportHeight(true);
    table.setModel(
        new DefaultTableModel(
            new Object[][] {},
            new String[] {Resources.strings().get("panel_name"), Resources.strings().get("panel_value")}));
    return table;
  }

  private void setupChangeListeners() {
    FocusAdapter saveOnFocusLost =
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            saveChanges();
          }
        };
    this.textFieldName.addFocusListener(saveOnFocusLost);
    this.textFieldTitle.addFocusListener(saveOnFocusLost);
    this.textFieldDesc.addFocusListener(saveOnFocusLost);
    this.textFieldName.addActionListener(e -> saveChanges());
    this.textFieldTitle.addActionListener(e -> saveChanges());
    this.spinnerGravity.addChangeListener(e -> saveChanges());
    this.model.addTableModelListener(e -> saveChanges());
  }

  public void bind(final IMap map) {
    this.dataSource = map;
    if (map == null) {
      return;
    }

    this.setControlValues(map);
  }

  public void saveChanges() {
    if (this.binding || this.dataSource == null) {
      return;
    }

    this.dataSource.setValue(MapProperty.MAP_DESCRIPTION, this.textFieldDesc.getText());
    this.dataSource.setValue(MapProperty.MAP_TITLE, this.textFieldTitle.getText());
    this.dataSource.setValue(MapProperty.GRAVITY, (int) this.spinnerGravity.getValue());
    this.dataSource.setValue(MapProperty.AMBIENTCOLOR, this.ambientColorComponent.getHexColor());
    this.dataSource.setValue(MapProperty.SHADOWCOLOR, this.shadowColorComponent.getHexColor());
    this.dataSource.setName(this.textFieldName.getText());

    final List<String> setProperties = new ArrayList<>();
    for (int row = 0; row < this.model.getRowCount(); row++) {
      final String name = (String) this.model.getValueAt(row, 0);
      final String value = (String) this.model.getValueAt(row, 1);
      if (name != null && !name.isEmpty()) {
        setProperties.add(name);
        this.dataSource.setValue(name, value);
      }
    }
    this.dataSource
        .getProperties()
        .keySet()
        .removeIf(p -> !setProperties.contains(p) && MapProperty.isCustom(p));
    if (Game.world().environment() != null && Game.world().environment().getAmbientLight() != null) {
      Game.world().environment().getAmbientLight().setColor(this.ambientColorComponent.getColor());
    }
    UndoManager.instance().recordChanges();
  }

  private void stopTableEditing() {
    TableCellEditor editor = this.tableCustomProperties.getCellEditor();
    if (editor != null) {
      editor.stopCellEditing();
    }
  }

  private void setControlValues(final IMap map) {
    this.binding = true;
    try {
      this.model.setRowCount(0);
      this.textFieldDesc.setText(map.getStringValue(MapProperty.MAP_DESCRIPTION, null));
      this.textFieldTitle.setText(map.getStringValue(MapProperty.MAP_TITLE, null));
      this.textFieldName.setText(map.getName());
      this.ambientColorComponent.setColor(
          map.hasCustomProperty(MapProperty.AMBIENTCOLOR)
              ? map.getColorValue(MapProperty.AMBIENTCOLOR)
              : AmbientLight.DEFAULT_COLOR);
      this.shadowColorComponent.setColor(
          map.hasCustomProperty(MapProperty.SHADOWCOLOR)
              ? map.getColorValue(MapProperty.SHADOWCOLOR)
              : StaticShadow.DEFAULT_COLOR);

      this.spinnerGravity.setValue(map.getIntValue(MapProperty.GRAVITY, 0));

      for (Map.Entry<String, ICustomProperty> prop : map.getProperties().entrySet()) {
        if (prop.getKey().equals(MapProperty.AMBIENTCOLOR)
            || prop.getKey().equals(MapProperty.GRAVITY)
            || prop.getKey().equals(MapProperty.MAP_DESCRIPTION)
            || prop.getKey().equals(MapProperty.MAP_TITLE)
            || prop.getKey().equals(MapProperty.SHADOWCOLOR)) {
          continue;
        }
        this.model.addRow(new Object[] {prop.getKey(), prop.getValue().getAsString()});
      }
    } finally {
      this.binding = false;
    }
  }
}

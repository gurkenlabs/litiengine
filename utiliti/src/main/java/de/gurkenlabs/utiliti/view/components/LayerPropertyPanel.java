package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.ImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapImage;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

public class LayerPropertyPanel extends JPanel {

  private final JTextField textFieldName;
  private final JComboBox<String> imageSourceCombo;
  private final JLabel imagePreview;
  private final JPanel imageSourceControl;
  private final JLabel labelImageSource;
  private final JSpinner spinnerOpacity;
  private final JCheckBox checkBoxVisible;
  private final ColorComponent tintColorComponent;
  private final JComboBox<RenderType> comboRenderType;
  private final ColorComponent layerColorComponent;
  private final JLabel labelLayerColor;
  private final JTable tableCustomProperties;
  private final DefaultTableModel model;
  private final JPanel accordion;
  private final JScrollPane scrollPane;
  private final ExpandableCard generalCard;

  private transient ILayer dataSource;
  private boolean binding;

  public LayerPropertyPanel() {
    setBorder(null);
    setLayout(new BorderLayout());
    setOpaque(true);
    setBackground(Style.background());

    this.textFieldName = ControlBehavior.apply(new JTextField());
    this.imageSourceCombo = new SearchableResourceComboBox();
    ControlBehavior.apply(this.imageSourceCombo);
    this.imageSourceCombo.setRenderer(new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean selected, boolean focus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
        if (value != null) {
          var spritesheet = Resources.spritesheets().get(value.toString());
          var preview = spritesheet != null && spritesheet.getTotalNumberOfSprites() > 0 ? spritesheet.getPreview(24) : null;
          label.setIcon(preview != null ? new ImageIcon(preview) : null);
        }
        return label;
      }
    });
    this.imagePreview = new JLabel();
    this.imagePreview.setPreferredSize(new Dimension(32, 32));
    this.imagePreview.setHorizontalAlignment(SwingConstants.CENTER);
    this.imageSourceControl = new JPanel(new BorderLayout(4, 0));
    this.imageSourceControl.setOpaque(false);
    this.imageSourceControl.add(this.imageSourceCombo, BorderLayout.CENTER);
    this.imageSourceControl.add(this.imagePreview, BorderLayout.EAST);
    this.labelImageSource = createLabel(Resources.strings().get("layerProperties_imageSource"));
    this.spinnerOpacity = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.05));
    ControlBehavior.apply(this.spinnerOpacity);
    this.checkBoxVisible = new JCheckBox(Resources.strings().get("layerProperties_visible"));
    checkBoxVisible.setOpaque(false);
    checkBoxVisible.setForeground(Style.text());

    this.tintColorComponent = new ColorComponent(java.awt.Color.WHITE);
    this.tintColorComponent.addActionListener(a -> saveChanges());

    this.comboRenderType = new JComboBox<>(RenderType.values());
    ControlBehavior.apply(this.comboRenderType);

    this.layerColorComponent = new ColorComponent(java.awt.Color.WHITE);
    this.layerColorComponent.addActionListener(a -> saveChanges());
    this.labelLayerColor = createLabel(Resources.strings().get("panel_color"));
    this.labelLayerColor.setVisible(false);
    this.layerColorComponent.setVisible(false);

    JButton buttonAdd = Style.textButton("+");
    JButton buttonRemove = Style.textButton("\u2212");

    this.tableCustomProperties = createPropertiesTable();
    this.scrollPane = new JScrollPane(this.tableCustomProperties);
    this.model = (DefaultTableModel) this.tableCustomProperties.getModel();

    buttonAdd.addActionListener(a -> {
      stopTableEditing();
      this.model.addRow(new Object[] {"", ""});
      this.saveChanges();
    });
    buttonRemove.addActionListener(a -> {
      stopTableEditing();
      int[] rows = this.tableCustomProperties.getSelectedRows();
      for (int i = 0; i < rows.length; i++) {
        this.model.removeRow(rows[i] - i);
      }
      this.saveChanges();
    });

    this.accordion = new JPanel();
    this.accordion.setLayout(new BoxLayout(this.accordion, BoxLayout.Y_AXIS));
    this.accordion.setOpaque(true);
    this.accordion.setBackground(Style.background());
    this.accordion.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

    this.generalCard = new ExpandableCard(Resources.strings().get("panel_general"), createGeneralPanel(), false);
    this.imageSourceControl.setVisible(false);
    this.labelImageSource.setVisible(false);
    ExpandableCard renderingCard =
        new ExpandableCard(Resources.strings().get("layerProperties_rendering"), createRenderingPanel(), false);
    ExpandableCard propertiesCard =
        new ExpandableCard(Resources.strings().get("layerProperties_customProperties"), createPropertiesPanel(buttonAdd, buttonRemove), false);

    this.generalCard.setContentInsets(8, 0, 8, 0);
    renderingCard.setContentInsets(8, 0, 8, 0);
    propertiesCard.setContentInsets(8, 0, 8, 0);

    this.accordion.add(this.generalCard);
    this.accordion.add(renderingCard);
    this.accordion.add(propertiesCard);

    JScrollPane hostScrollPane = new JScrollPane(this.accordion);
    hostScrollPane.setBorder(null);
    hostScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    hostScrollPane.getViewport().setBackground(Style.background());
    add(hostScrollPane, BorderLayout.CENTER);

    this.setupChangeListeners();
  }

  ExpandableCard addSection(String title, JComponent content, boolean expanded) {
    ExpandableCard card = new ExpandableCard(title, content, expanded);
    card.setContentInsets(8, 0, 8, 0);
    this.accordion.add(card);
    this.accordion.revalidate();
    return card;
  }

  private JPanel createGeneralPanel() {
    return createForm(
        new JLabel[] {
            createLabel(Resources.strings().get("panel_name")),
            createLabel(Resources.strings().get("layerProperties_opacity")),
            createLabel(Resources.strings().get("layerProperties_visible")),
            this.labelLayerColor,
            this.labelImageSource,
        },
        new JComponent[] {
            this.textFieldName,
            this.spinnerOpacity,
            this.checkBoxVisible,
            this.layerColorComponent,
            this.imageSourceControl,
        },
        new int[] {
            PropertyPanel.CONTROL_HEIGHT,
            PropertyPanel.CONTROL_HEIGHT,
            PropertyPanel.CONTROL_HEIGHT,
            PropertyPanel.CONTROL_HEIGHT,
            PropertyPanel.CONTROL_HEIGHT,
        });
  }

  private JPanel createRenderingPanel() {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JPanel renderTypeRow = createForm(
        new JLabel[] {
          createLabel(Resources.strings().get("layerProperties_renderType")),
          createLabel(Resources.strings().get("layerProperties_tintColor")) },
        new JComponent[] { this.comboRenderType, this.tintColorComponent },
        new int[] { PropertyPanel.CONTROL_HEIGHT, this.tintColorComponent.getPreferredSize().height });
    panel.add(renderTypeRow);
    return panel;
  }

  private JPanel createPropertiesPanel(JButton buttonAdd, JButton buttonRemove) {
    int inset = PropertyPanel.LABEL_WIDTH + PropertyPanel.GUTTER_WIDTH - 6;
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(createAlignedControl(this.scrollPane, 150, inset));
    panel.add(Box.createVerticalStrut(6));
    JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    buttonRow.setOpaque(false);
    buttonRow.add(buttonAdd);
    buttonRow.add(buttonRemove);
    JPanel buttonWrapper = new JPanel(new BorderLayout());
    buttonWrapper.setOpaque(false);
    buttonWrapper.add(buttonRow, BorderLayout.WEST);
    panel.add(createAlignedControl(buttonWrapper, buttonRow.getPreferredSize().height, inset));
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
      if (labels[i] == null || !labels[i].isVisible()) {
        continue;
      }
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

  private JPanel createAlignedControl(JComponent component, int height, int inset) {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    GroupLayout gl = new GroupLayout(panel);
    panel.setLayout(gl);
    gl.setHorizontalGroup(
        gl.createSequentialGroup()
            .addGap(inset)
            .addComponent(component, PropertyPanel.CONTROL_MIN_WIDTH, PropertyPanel.CONTROL_WIDTH, Integer.MAX_VALUE));
    gl.setVerticalGroup(
        gl.createSequentialGroup().addComponent(component, height, height, height));
    setRowSize(panel, height);
    return panel;
  }

  private static void setRowSize(JComponent component, int height) {
    component.setPreferredSize(new Dimension(0, height));
    component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    component.setAlignmentX(LEFT_ALIGNMENT);
  }

  private static JLabel createLabel(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(Style.text());
    label.setHorizontalAlignment(SwingConstants.TRAILING);
    return label;
  }

  private JTable createPropertiesTable() {
    JTable table =
        new JTable() {
          private static final String EMPTY_TEXT = Resources.strings().get("panel_noPropertiesDefined");

          @Override
          protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() == 0) {
              g.setColor(Style.COLOR_PLACEHOLDER);
              FontMetrics fm = g.getFontMetrics();
              int x = (getWidth() - fm.stringWidth(EMPTY_TEXT)) / 2;
              int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
              g.drawString(EMPTY_TEXT, x, y);
            }
          }
        };
    table.getTableHeader().setReorderingAllowed(false);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setShowHorizontalLines(true);
    table.setShowVerticalLines(false);
    table.setGridColor(Style.border());
    table.setIntercellSpacing(new Dimension(0, 1));
    table.setFillsViewportHeight(true);
    table.setModel(
        new DefaultTableModel(
            new Object[][] {},
            new String[] {Resources.strings().get("panel_name"), Resources.strings().get("panel_value")}));
    return table;
  }

  private void setupChangeListeners() {
    FocusAdapter saveOnFocusLost = new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        saveChanges();
      }
    };
    ChangeListener saveOnChange = e -> saveChanges();

    this.textFieldName.addFocusListener(saveOnFocusLost);
    this.textFieldName.addActionListener(e -> saveChanges());
    this.imageSourceCombo.addActionListener(e -> {
      updateImagePreview();
      saveChanges();
    });
    this.spinnerOpacity.addChangeListener(saveOnChange);
    this.checkBoxVisible.addActionListener(e -> saveChanges());
    this.comboRenderType.addActionListener(e -> saveChanges());
    this.model.addTableModelListener(e -> saveChanges());
  }

  public void bind(final ILayer layer) {
    this.binding = true;
    try {
      this.dataSource = layer;
      if (layer == null) {
        clearControls();
        return;
      }

      boolean isMapObjectLayer = layer instanceof IMapObjectLayer;
      boolean isImageLayer = layer instanceof IImageLayer;
      this.labelLayerColor.setVisible(isMapObjectLayer);
      this.layerColorComponent.setVisible(isMapObjectLayer);
      if (isMapObjectLayer) {
        this.layerColorComponent.setColor(((IMapObjectLayer) layer).getColor());
      }
      this.labelImageSource.setVisible(isImageLayer);
      this.imageSourceControl.setVisible(isImageLayer);
      bindImageSources(isImageLayer && ((IImageLayer) layer).getImage() != null
          ? ((IImageLayer) layer).getImage().getSource() : null);
      this.generalCard.revalidate();

      this.setControlValues(layer);
      String layerName = layer.getName() != null && !layer.getName().isBlank()
        ? layer.getName() : Resources.strings().get("layerProperties_unnamedLayer");
      this.generalCard.setTitle(Resources.strings().get("panel_general") + "  ·  " + layerName);
    } finally {
      this.binding = false;
    }
  }

  public void clearControls() {
    this.generalCard.setTitle(Resources.strings().get("panel_general"));
    this.textFieldName.setText("");
    this.imageSourceCombo.removeAllItems();
    this.imagePreview.setIcon(null);
    this.spinnerOpacity.setValue(1.0);
    this.checkBoxVisible.setSelected(true);
    this.tintColorComponent.setColor(java.awt.Color.WHITE);
    this.comboRenderType.setSelectedItem(RenderType.NORMAL);
    this.model.setRowCount(0);
  }

  public void saveChanges() {
    if (this.binding || this.dataSource == null) {
      return;
    }

    stopTableEditing();

    UndoManager.instance().layerChanging(this.dataSource);

    this.dataSource.setName(this.textFieldName.getText());
    this.dataSource.setOpacity(((Number) this.spinnerOpacity.getValue()).floatValue());
    this.dataSource.setVisible(this.checkBoxVisible.isSelected());
    this.dataSource.setTintColor(this.tintColorComponent.getColor());
    this.dataSource.setRenderType(this.comboRenderType.getSelectedItem() instanceof RenderType rt ? rt : RenderType.NORMAL);

    if (this.dataSource instanceof IMapObjectLayer mol) {
      mol.setColor(this.layerColorComponent.getColor());
    }
    if (this.dataSource instanceof ImageLayer imageLayer) {
      MapImage image = imageLayer.getImage() instanceof MapImage mapImage ? mapImage : new MapImage();
      Object selected = this.imageSourceCombo.getSelectedItem();
      image.setSource(selected != null ? selected.toString() : "");
      imageLayer.setImage(image);
    }

    final List<String> setProperties = new ArrayList<>();
    for (int row = 0; row < this.model.getRowCount(); row++) {
      final String name = (String) this.model.getValueAt(row, 0);
      final String value = (String) this.model.getValueAt(row, 1);
      if (name != null && !name.isEmpty()) {
        setProperties.add(name);
        this.dataSource.setValue(name, value);
      }
    }
    removeUnsetProperties(this.dataSource, setProperties);

    UndoManager.instance().layerChanged(this.dataSource);

    if (UI.getLayerController() instanceof SceneGraph sg) {
      sg.refresh();
    }
  }

  private void setControlValues(final ILayer layer) {
    this.textFieldName.setText(layer.getName());
    this.spinnerOpacity.setValue((double) layer.getOpacity());
    this.checkBoxVisible.setSelected(layer.isVisible());

    java.awt.Color tint = layer.getTintColor();
    this.tintColorComponent.setColor(tint != null ? tint : java.awt.Color.WHITE);

    this.comboRenderType.setSelectedItem(layer.getRenderType());

    this.model.setRowCount(0);
    for (Map.Entry<String, ICustomProperty> prop : layer.getProperties().entrySet()) {
      this.model.addRow(new Object[] {prop.getKey(), prop.getValue().getAsString()});
    }
  }

  private void bindImageSources(String selectedSource) {
    this.imageSourceCombo.removeAllItems();
    if (Editor.instance().getGameFile() != null) {
      for (var sprite : Editor.instance().getGameFile().getSpriteSheets()) {
        if (sprite.getName() != null && !sprite.getName().isBlank()) {
          this.imageSourceCombo.addItem(sprite.getName());
        }
      }
    }
    if (selectedSource != null && !selectedSource.isBlank()) {
      boolean present = false;
      for (int i = 0; i < this.imageSourceCombo.getItemCount(); i++) {
        if (selectedSource.equals(this.imageSourceCombo.getItemAt(i))) {
          present = true;
          break;
        }
      }
      if (!present) {
        this.imageSourceCombo.addItem(selectedSource);
      }
      this.imageSourceCombo.setSelectedItem(selectedSource);
    }
    updateImagePreview();
  }

  private void updateImagePreview() {
    Object selected = this.imageSourceCombo.getSelectedItem();
    var spritesheet = selected != null ? Resources.spritesheets().get(selected.toString()) : null;
    var preview = spritesheet != null && spritesheet.getTotalNumberOfSprites() > 0 ? spritesheet.getPreview(32) : null;
    this.imagePreview.setIcon(preview != null ? new ImageIcon(preview) : null);
  }

  private void stopTableEditing() {
    TableCellEditor editor = this.tableCustomProperties.getCellEditor();
    if (editor != null) {
      editor.stopCellEditing();
    }
  }

  static void removeUnsetProperties(ILayer layer, List<String> setProperties) {
    layer.getProperties().keySet().removeIf(p -> !setProperties.contains(p));
  }
}

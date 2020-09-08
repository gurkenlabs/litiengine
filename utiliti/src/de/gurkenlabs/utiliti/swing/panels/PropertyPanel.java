package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.swing.ControlBehavior;
import de.gurkenlabs.utiliti.swing.TextList;
import de.gurkenlabs.utiliti.swing.UI;

@SuppressWarnings("serial")
public abstract class PropertyPanel extends JPanel {
  public static final int LABEL_WIDTH = (int) (35 * Editor.preferences().getUiScale());
  public static final int CONTROL_MIN_WIDTH = (int) (80 * Editor.preferences().getUiScale());
  public static final int CONTROL_WIDTH = (int) (160 * Editor.preferences().getUiScale());
  public static final int SPINNER_WIDTH = (int) (70 * Editor.preferences().getUiScale());
  public static final int CONTROL_HEIGHT = (int) (30 * Editor.preferences().getUiScale());
  public static final int CONTROL_MARGIN = (int) (5 * Editor.preferences().getUiScale());
  public static final int PANEL_WIDTH = 2 * (CONTROL_WIDTH + LABEL_WIDTH + CONTROL_MARGIN);
  public static final int LABEL_GAP = 0;
  public static final int DUAL_SPINNER_GAP = CONTROL_WIDTH - 2 * (LABEL_WIDTH + SPINNER_WIDTH);
  public static final Dimension LABEL_SIZE = new Dimension(LABEL_WIDTH, CONTROL_HEIGHT);
  public static final Dimension BUTTON_SIZE = new Dimension(CONTROL_HEIGHT, CONTROL_HEIGHT);
  public static final Dimension SPINNER_SIZE = new Dimension(SPINNER_WIDTH, CONTROL_HEIGHT);
  public static final Dimension SMALL_CONTROL_SIZE = new Dimension(CONTROL_MIN_WIDTH, CONTROL_HEIGHT);
  public static final Dimension LARGE_CONTROL_SIZE = new Dimension(CONTROL_WIDTH, CONTROL_HEIGHT);
  public static final Border STANDARDBORDER = new EmptyBorder(0, 4, 0, 4);

  public static final int STEP_ONE = 1;
  public static final int STEP_COARSE = 10;
  public static final int STEP_SPARSE = 100;
  public static final float STEP_FINE = .05f;
  public static final float STEP_FINEST = .01f;

  protected boolean isFocussing;
  protected transient IMapObject dataSource;
  private String identifier;
  private transient Icon icon;

  public PropertyPanel(String identifier, Icon icon) {
    this(identifier);
    this.icon = icon;
  }

  public PropertyPanel(String identifier) {
    this();
    this.identifier = identifier;
  }

  public PropertyPanel() {
    setBorder(null);
    UI.addOrphanComponent(this);
  }

  public static float getSpinnerValue(JSpinner spinner) {
    if (spinner.getValue() instanceof Integer) {
      return ((Integer) spinner.getValue()).floatValue();
    } else if (spinner.getValue() instanceof Double) {
      return ((Double) spinner.getValue()).floatValue();
    } else {
      return (float) spinner.getValue();
    }
  }

  protected IMapObject getDataSource() {
    return this.dataSource;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public Icon getIcon() {
    return this.icon;
  }

  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  public void bind(IMapObject mapObject) {
    this.dataSource = mapObject;

    this.isFocussing = true;

    if (this.dataSource == null) {
      this.clearControls();
      return;
    }

    this.setControlValues(mapObject);
    this.isFocussing = false;
  }

  protected static void populateComboBoxWithSprites(JComboBox<JLabel> comboBox, Map<String, String> m) {
    comboBox.removeAllItems();

    for (Map.Entry<String, String> entry : m.entrySet()) {
      JLabel label = new JLabel();
      label.setText(entry.getKey());
      Spritesheet opt = Resources.spritesheets().get(entry.getValue());
      if (opt != null && opt.getTotalNumberOfSprites() > 0) {
        BufferedImage scaled = opt.getPreview(24);
        if (scaled != null) {
          label.setIcon(new ImageIcon(scaled));
        }
      }

      comboBox.addItem(label);
    }
  }

  protected static void selectSpriteSheet(JComboBox<JLabel> comboBox, IMapObject mapObject) {
    if (mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME) != null) {
      for (int i = 0; i < comboBox.getModel().getSize(); i++) {
        JLabel label = comboBox.getModel().getElementAt(i);
        if (label != null && label.getText().equals(mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME))) {
          comboBox.setSelectedItem(label);
          break;
        }
      }
    }
  }

  protected abstract void clearControls();

  protected abstract void setControlValues(IMapObject mapObject);

  protected void setup(JToggleButton toggle, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    toggle.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(property, toggle.isSelected())));
  }

  protected void setup(JCheckBox checkbox, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    checkbox.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(property, checkbox.isSelected())));
  }

  protected <T> void setup(JComboBox<T> comboBox, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    comboBox.addActionListener(new MapObjectPropertyActionListener(m -> {
      T value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
      m.setValue(property, value.toString());
    }));
  }

  protected void setupL(JComboBox<JLabel> comboBox, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    comboBox.addActionListener(new MapObjectPropertyActionListener(m -> {
      JLabel value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
      m.setValue(property, value.getText());
    }));
  }

  protected void setup(JSlider slider, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    slider.addChangeListener(new SliderListener(property, slider));
  }

  protected void setup(JSlider slider, String property, float factor) {
    if (property == null || property.isEmpty()) {
      return;
    }
    slider.addChangeListener(new SliderListener(property, slider, factor));
  }

  protected void setup(JSpinner spinner, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    spinner.addChangeListener(new SpinnerListener(property, spinner));
  }

  protected void setup(JTextField textField, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    textField.addFocusListener(new MapObjectPropteryFocusListener(m -> m.setValue(property, textField.getText())));
    textField.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(property, textField.getText())));
  }

  protected void setup(TextList textList, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    textList.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(property, textList.getJoinedString())));
  }

  protected void setup(JTable table, String... properties) {
    if (properties == null || properties.length == 0) {
      return;
    }
    table.getModel().addTableModelListener(new TableListener(table, properties));
  }

  protected class MapObjectPropertyItemListener implements ItemListener {
    private final Consumer<IMapObject> updateAction;

    MapObjectPropertyItemListener(Consumer<IMapObject> updateAction) {
      this.updateAction = updateAction;
    }

    @Override
    public void itemStateChanged(ItemEvent arg0) {
      if (getDataSource() == null || isFocussing) {
        return;
      }

      applyChanges(this.updateAction);
    }
  }

  protected class MapObjectPropertyActionListener implements ActionListener {
    private final Consumer<IMapObject> updateAction;

    MapObjectPropertyActionListener(Consumer<IMapObject> updateAction) {
      this.updateAction = updateAction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (getDataSource() == null || isFocussing) {
        return;
      }

      applyChanges(this.updateAction);
    }
  }

  protected class MabObjectPropertyTableModelListener implements TableModelListener {
    private final Consumer<IMapObject> updateAction;

    MabObjectPropertyTableModelListener(Consumer<IMapObject> updateAction) {
      this.updateAction = updateAction;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
      if (getDataSource() == null || isFocussing) {
        return;
      }
      applyChanges(this.updateAction);

    }

  }

  protected class MapObjectPropertyChangeListener implements ChangeListener {
    private final Consumer<IMapObject> updateAction;

    MapObjectPropertyChangeListener(Consumer<IMapObject> updateAction) {
      this.updateAction = updateAction;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      if (getDataSource() == null || isFocussing) {
        return;
      }

      applyChanges(this.updateAction);
    }
  }

  protected class SpinnerListener extends MapObjectPropertyChangeListener {
    SpinnerListener(String mapObjectProperty, JSpinner spinner) {
      super(m -> m.setValue(mapObjectProperty, spinner.getValue().toString()));
    }
  }

  protected class SliderListener extends MapObjectPropertyChangeListener {
    SliderListener(String mapObjectProperty, JSlider slider) {
      super(m -> m.setValue(mapObjectProperty, slider.getValue()));
    }

    SliderListener(String mapObjectProperty, JSlider slider, float factor) {
      super(m -> m.setValue(mapObjectProperty, slider.getValue() * factor));
    }
  }

  protected class TableListener extends MabObjectPropertyTableModelListener {
    TableListener(JTable table, String... mapObjectProperties) {
      super(m -> {
        int column = 0;
        for (String prop : mapObjectProperties) {
          ArrayList<Object> values = new ArrayList<>();
          for (int i = 0; i < table.getRowCount(); i++) {
            values.add(table.getValueAt(i, column));
          }
          m.setValue(prop, ArrayUtilities.join(values, ","));
          column++;
        }
      });
    }
  }

  protected class MapObjectPropteryFocusListener extends FocusAdapter {
    private final Consumer<IMapObject> updateAction;

    MapObjectPropteryFocusListener(Consumer<IMapObject> updateAction) {
      this.updateAction = updateAction;
    }

    @Override
    public void focusLost(FocusEvent e) {
      if (getDataSource() == null || isFocussing) {
        return;
      }

      applyChanges(this.updateAction);
    }
  }

  protected void updateEnvironment() {
    if (getDataSource() instanceof IMapObject) {
      IMapObject obj = getDataSource();
      Game.world().environment().reloadFromMap(obj.getId());
      UI.getEntityController().refresh();
    }
  }

  protected LayoutManager createLayout(LayoutItem[] layoutItems, Component... additionalComponents) {
    GroupLayout groupLayout = new GroupLayout(this);

    // prepare the parallel group for the labels
    // add additional components to the group
    ParallelGroup parallel = groupLayout.createParallelGroup(Alignment.TRAILING);
    for (Component component : additionalComponents) {
      parallel.addComponent(component, Alignment.LEADING, CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE);
    }

    for (LayoutItem item : layoutItems) {
      SequentialGroup horGrp = groupLayout.createSequentialGroup();
      if (item.getLabel() != null) {
        horGrp.addComponent(item.getLabel(), LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(item.getComponent(), CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE);
      } else {
        horGrp.addComponent(item.getComponent(), CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE);
      }
      parallel.addGroup(Alignment.LEADING, horGrp);
    }

    // initialize the horizontal layout group with the parallel groups for
    // labels and components and some additional gaps
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGroup(parallel)));

    // now prepare the vertical groups
    SequentialGroup seq = groupLayout.createSequentialGroup();
    SequentialGroup current = seq.addGap(CONTROL_MARGIN);

    for (LayoutItem item : layoutItems) {
      ParallelGroup verGrp = groupLayout.createParallelGroup(Alignment.LEADING);
      if (item.getLabel() != null) {
        verGrp.addComponent(item.getComponent(), item.getMinHeight(), item.getMinHeight(), item.getMinHeight()).addComponent(item.getLabel(), GroupLayout.PREFERRED_SIZE, item.getMinHeight(), item.getMinHeight()).addGap(CONTROL_MARGIN);
      } else {
        verGrp.addComponent(item.getComponent(), item.getMinHeight(), item.getMinHeight(), item.getMinHeight());
      }

      current = current.addGroup(verGrp);
    }

    current.addPreferredGap(ComponentPlacement.UNRELATED);
    for (Component component : additionalComponents) {
      current = current.addComponent(component, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addGap(CONTROL_MARGIN);
    }

    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(seq));
    return groupLayout;
  }

  private void applyChanges(Consumer<IMapObject> updateAction) {
    UndoManager.instance().mapObjectChanging(getDataSource());
    updateAction.accept(getDataSource());
    UndoManager.instance().mapObjectChanged(getDataSource());
    updateEnvironment();
  }

  protected class LayoutItem {
    private final String caption;
    private final Component component;
    private final JLabel label;

    private int minHeight;

    public LayoutItem(Component component) {
      this.component = component;
      this.label = null;
      this.caption = "";
      this.setMinHeight(CONTROL_HEIGHT);
      ControlBehavior.apply(this.getComponent());
    }

    public LayoutItem(Component component, int minHeight) {
      this(component);
      this.setMinHeight(minHeight);
    }

    public LayoutItem(String resource, Component component) {
      this.caption = Resources.strings().get(resource);
      this.component = component;
      this.label = new JLabel(this.caption);
      this.label.setVerticalAlignment(SwingConstants.CENTER);
      this.setMinHeight(CONTROL_HEIGHT);
      ControlBehavior.apply(this.getComponent());
    }

    public LayoutItem(String resource, Component component, int minHeight) {
      this(resource, component);
      this.setMinHeight(minHeight);
    }

    public String getCaption() {
      return this.caption;
    }

    public JLabel getLabel() {
      return this.label;
    }

    public Component getComponent() {
      return this.component;
    }

    private int getMinHeight() {
      return minHeight;
    }

    private void setMinHeight(int minHeight) {
      this.minHeight = minHeight;
    }
  }
}

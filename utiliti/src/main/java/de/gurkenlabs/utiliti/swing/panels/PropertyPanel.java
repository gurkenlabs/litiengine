package de.gurkenlabs.utiliti.swing.panels;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.listeners.MapObjectPropertyActionListener;
import de.gurkenlabs.utiliti.listeners.MapObjectPropertyFocusListener;
import de.gurkenlabs.utiliti.listeners.NumberModelListener;
import de.gurkenlabs.utiliti.listeners.SliderListener;
import de.gurkenlabs.utiliti.listeners.SpinnerListener;
import de.gurkenlabs.utiliti.listeners.TableListener;
import de.gurkenlabs.utiliti.swing.ControlBehavior;
import de.gurkenlabs.utiliti.swing.TextList;
import de.gurkenlabs.utiliti.swing.UI;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.util.Map;
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
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

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
  public static final Dimension SMALL_CONTROL_SIZE =
    new Dimension(CONTROL_MIN_WIDTH, CONTROL_HEIGHT);
  public static final Dimension LARGE_CONTROL_SIZE = new Dimension(CONTROL_WIDTH, CONTROL_HEIGHT);
  public static final Border STANDARDBORDER = new EmptyBorder(0, 4, 0, 4);

  public static final int STEP_ONE = 1;
  public static final int STEP_COARSE = 10;
  public static final int STEP_SPARSE = 100;
  public static final float STEP_FINE = .05f;
  public static final float STEP_FINEST = .01f;

  protected transient IMapObject mapObject;
  private String identifier;
  private transient Icon icon;

  protected PropertyPanel(String identifier, Icon icon) {
    this(identifier);
    this.icon = icon;
  }

  protected PropertyPanel(String identifier) {
    this();
    this.identifier = identifier;
  }

  protected PropertyPanel() {
    setBorder(null);
    UI.addOrphanComponent(this);
  }

  protected static void populateComboBoxWithSprites(
    JComboBox<JLabel> comboBox, Map<String, String> m) {
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
        if (label != null
          && label
          .getText()
          .equals(mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME))) {
          comboBox.setSelectedItem(label);
          break;
        }
      }
    }
  }

  public IMapObject getMapObject() {
    return this.mapObject;
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
    this.mapObject = mapObject;

    if (this.mapObject == null) {
      this.clearControls();
      return;
    }

    this.setControlValues(mapObject);
  }

  protected abstract void clearControls();

  protected abstract void setControlValues(IMapObject mapObject);

  protected void setup(JToggleButton toggle, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    toggle.addActionListener(
      new MapObjectPropertyActionListener(
        this,
        m -> m.hasCustomProperty(property) && m.getBoolValue(property) != toggle.isSelected(),
        m -> m.setValue(property, toggle.isSelected())));
  }

  protected void setup(JCheckBox checkbox, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    checkbox.addActionListener(
      new MapObjectPropertyActionListener(
        this,
        m -> !m.hasCustomProperty(property) || m.getBoolValue(property) != checkbox.isSelected(),
        m -> m.setValue(property, checkbox.isSelected())));
  }

  protected <T> void setup(JComboBox<T> comboBox, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    comboBox.addActionListener(
      new MapObjectPropertyActionListener(
        this,
        m -> {
          if (!m.hasCustomProperty(property) || m.getStringValue(property) == null) {
            return true;
          }

          T value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
          return !m.getStringValue(property).equals(value.toString());
        },
        m -> {
          T value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
          m.setValue(property, value != null ? value.toString() : null);
        }));
  }

  protected void setupL(JComboBox<JLabel> comboBox, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    comboBox.addActionListener(
      new MapObjectPropertyActionListener(
        this,
        m -> {
          if (!m.hasCustomProperty(property) || m.getStringValue(property) == null) {
            return true;
          }

          JLabel value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
          return value != null && !m.getStringValue(property).equals(value.getText());
        },
        m -> {
          JLabel value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
          m.setValue(property, value != null ? value.getText() : null);
        }));
  }

  protected void setup(JSlider slider, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    slider.addChangeListener(new SliderListener(this, property, slider));
  }

  protected void setup(JSlider slider, String property, float factor) {
    if (property == null || property.isEmpty()) {
      return;
    }
    slider.addChangeListener(new SliderListener(this, property, slider, factor));
  }

  protected void setup(JSpinner spinner, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    spinner.addChangeListener(new SpinnerListener(this, property, spinner));
  }

  protected void setup(SpinnerNumberModel numberModel, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    numberModel.addChangeListener(new NumberModelListener(this, property, numberModel));
  }

  protected void setup(JTextField textField, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    textField.addFocusListener(
      new MapObjectPropertyFocusListener(this, m -> true,
        m -> m.setValue(property, textField.getText())));
    textField.addActionListener(
      new MapObjectPropertyActionListener(this,
        m -> !m.hasCustomProperty(property)
          || m.getStringValue(property) == null
          || !m.getStringValue(property).equals(textField.getText()),
        m -> m.setValue(property, textField.getText())));
  }

  protected void setup(TextList textList, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    textList.addActionListener(
      new MapObjectPropertyActionListener(this,
        m -> !m.hasCustomProperty(property)
          || m.getStringValue(property) == null
          || !m.getStringValue(property).equals(textList.getJoinedString()),
        m -> m.setValue(property, textList.getJoinedString())));
  }

  protected void setup(JTable table, String... properties) {
    if (properties == null || properties.length == 0) {
      return;
    }
    table.getModel()
      .addTableModelListener(new TableListener(this, table, m -> true, properties));
  }


  protected LayoutManager createLayout(
    LayoutItem[] layoutItems, Component... additionalComponents) {
    GroupLayout groupLayout = new GroupLayout(this);

    // prepare the parallel group for the labels
    // add additional components to the group
    ParallelGroup parallel = groupLayout.createParallelGroup(Alignment.TRAILING);
    for (Component component : additionalComponents) {
      parallel.addComponent(
        component, Alignment.LEADING, CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE);
    }

    for (LayoutItem item : layoutItems) {
      SequentialGroup horGrp = groupLayout.createSequentialGroup();
      if (item.getLabel() != null) {
        horGrp
          .addComponent(item.getLabel(), LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(item.getComponent(), CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE);
      } else {
        horGrp.addComponent(item.getComponent(), CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE);
      }
      parallel.addGroup(Alignment.LEADING, horGrp);
    }

    // initialize the horizontal layout group with the parallel groups for
    // labels and components and some additional gaps
    groupLayout.setHorizontalGroup(
      groupLayout
        .createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGroup(parallel)));

    // now prepare the vertical groups
    SequentialGroup seq = groupLayout.createSequentialGroup();
    SequentialGroup current = seq.addGap(CONTROL_MARGIN);

    for (LayoutItem item : layoutItems) {
      ParallelGroup verGrp = groupLayout.createParallelGroup(Alignment.LEADING);
      if (item.getLabel() != null) {
        verGrp
          .addComponent(
            item.getComponent(), item.getMinHeight(), item.getMinHeight(), item.getMinHeight())
          .addComponent(
            item.getLabel(),
            GroupLayout.PREFERRED_SIZE,
            item.getMinHeight(),
            item.getMinHeight())
          .addGap(CONTROL_MARGIN);
      } else {
        verGrp.addComponent(
          item.getComponent(), item.getMinHeight(), item.getMinHeight(), item.getMinHeight());
      }

      current = current.addGroup(verGrp);
    }

    current.addPreferredGap(ComponentPlacement.UNRELATED);
    for (Component component : additionalComponents) {
      current =
        current
          .addComponent(
            component,
            GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
          .addGap(CONTROL_MARGIN);
    }

    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(seq));
    return groupLayout;
  }

  protected static class LayoutItem {

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

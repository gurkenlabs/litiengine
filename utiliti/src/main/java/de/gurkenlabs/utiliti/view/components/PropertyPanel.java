package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
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
import java.util.function.Function;
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

/**
 * An abstract class representing a property panel in the UI. This class extends JPanel and provides various constants and methods for managing UI
 * components and their properties.
 */
public abstract class PropertyPanel extends JPanel {

  /**
   * The width of the label in pixels, scaled by the UI scale factor.
   */
  public static final int LABEL_WIDTH = (int) (35 * Editor.preferences().getUiScale());

  /**
   * The minimum width of the control in pixels, scaled by the UI scale factor.
   */
  public static final int CONTROL_MIN_WIDTH = (int) (80 * Editor.preferences().getUiScale());

  /**
   * The width of the control in pixels, scaled by the UI scale factor.
   */
  public static final int CONTROL_WIDTH = (int) (160 * Editor.preferences().getUiScale());

  /**
   * The width of the spinner in pixels, scaled by the UI scale factor.
   */
  public static final int SPINNER_WIDTH = (int) (70 * Editor.preferences().getUiScale());

  /**
   * The height of the control in pixels, scaled by the UI scale factor.
   */
  public static final int CONTROL_HEIGHT = (int) (32 * Editor.preferences().getUiScale());

  /**
   * The margin between controls in pixels, scaled by the UI scale factor.
   */
  public static final int CONTROL_MARGIN = (int) (5 * Editor.preferences().getUiScale());

  /**
   * The width of the panel in pixels, calculated based on control and label widths.
   */
  public static final int PANEL_WIDTH = 2 * (CONTROL_WIDTH + LABEL_WIDTH + CONTROL_MARGIN);

  /**
   * The gap between labels in pixels.
   */
  public static final int LABEL_GAP = 0;

  /**
   * The gap between dual spinners in pixels, calculated based on control and label widths.
   */
  public static final int DUAL_SPINNER_GAP = CONTROL_WIDTH - 2 * (LABEL_WIDTH + SPINNER_WIDTH);

  /**
   * The dimension of the label, based on label width and control height.
   */
  public static final Dimension LABEL_SIZE = new Dimension(LABEL_WIDTH, CONTROL_HEIGHT);

  /**
   * The dimension of the button, based on control height.
   */
  public static final Dimension BUTTON_SIZE = new Dimension(CONTROL_HEIGHT, CONTROL_HEIGHT);

  /**
   * The dimension of the spinner, based on spinner width and control height.
   */
  public static final Dimension SPINNER_SIZE = new Dimension(SPINNER_WIDTH, CONTROL_HEIGHT);

  /**
   * The dimension of a small control, based on minimum control width and control height.
   */
  public static final Dimension SMALL_CONTROL_SIZE = new Dimension(CONTROL_MIN_WIDTH, CONTROL_HEIGHT);

  /**
   * The dimension of a large control, based on control width and control height.
   */
  public static final Dimension LARGE_CONTROL_SIZE = new Dimension(CONTROL_WIDTH, CONTROL_HEIGHT);

  /**
   * The standard border for the panel, with padding on the left and right.
   */
  public static final Border STANDARDBORDER = new EmptyBorder(0, 4, 0, 4);

  /**
   * The step value for incrementing by one.
   */
  public static final int STEP_ONE = 1;

  /**
   * The step value for coarse increments.
   */
  public static final int STEP_COARSE = 10;

  /**
   * The step value for sparse increments.
   */
  public static final int STEP_SPARSE = 100;

  /**
   * The step value for fine increments.
   */
  public static final float STEP_FINE = .05f;

  /**
   * The step value for the finest increments.
   */
  public static final float STEP_FINEST = .01f;

  /**
   * The data source for the property panel, representing a map object.
   */
  protected transient IMapObject dataSource;

  /**
   * The identifier for the property panel.
   */
  private String identifier;

  /**
   * The icon for the property panel.
   */
  private transient Icon icon;

  /**
   * Constructs a PropertyPanel with the specified identifier and icon.
   *
   * @param identifier the identifier for the property panel
   * @param icon       the icon for the property panel
   */
  protected PropertyPanel(String identifier, Icon icon) {
    this(identifier);
    this.icon = icon;
  }

  /**
   * Constructs a PropertyPanel with the specified identifier.
   *
   * @param identifier the identifier for the property panel
   */
  protected PropertyPanel(String identifier) {
    this();
    this.identifier = identifier;
  }

  /**
   * Constructs a PropertyPanel with default settings.
   */
  protected PropertyPanel() {
    setBorder(null);
    UI.addOrphanComponent(this);
  }

  /**
   * Retrieves the float value from a JSpinner.
   *
   * @param spinner the JSpinner to get the value from
   * @return the float value of the spinner
   */
  public static float getSpinnerValue(JSpinner spinner) {
    if (spinner.getValue() instanceof Integer integer) {
      return integer.floatValue();
    } else if (spinner.getValue() instanceof Double dbl) {
      return dbl.floatValue();
    } else {
      return (float) spinner.getValue();
    }
  }

  /**
   * Populates a JComboBox with sprite labels.
   *
   * @param comboBox the JComboBox to populate
   * @param sprites  a map of sprite names to their paths
   */
  protected static void populateComboBoxWithSprites(JComboBox<JLabel> comboBox, Map<String, String> sprites) {
    comboBox.removeAllItems();

    sprites.forEach((name, path) -> {
      JLabel label = new JLabel(name);
      Spritesheet spritesheet = Resources.spritesheets().get(path);
      if (spritesheet != null && spritesheet.getTotalNumberOfSprites() > 0) {
        BufferedImage preview = spritesheet.getPreview(24);
        if (preview != null) {
          label.setIcon(new ImageIcon(preview));
        }
      }
      comboBox.addItem(label);
    });
  }

  /**
   * Selects a sprite sheet in a JComboBox based on the map object.
   *
   * @param comboBox  the JComboBox to select the sprite sheet in
   * @param mapObject the map object containing the sprite sheet name
   */
  protected static void selectSpriteSheet(JComboBox<JLabel> comboBox, IMapObject mapObject) {
    if (mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME, null) != null) {
      for (int i = 0; i < comboBox.getModel().getSize(); i++) {
        JLabel label = comboBox.getModel().getElementAt(i);
        if (label != null && label.getText().equals(mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME, null))) {
          comboBox.setSelectedItem(label);
          break;
        }
      }
    }
  }

  /**
   * Retrieves the data source for the property panel.
   *
   * @return the data source, which is an instance of IMapObject
   */
  protected IMapObject getDataSource() {
    return this.dataSource;
  }

  /**
   * Retrieves the identifier for the property panel.
   *
   * @return the identifier as a String
   */
  public String getIdentifier() {
    return this.identifier;
  }

  /**
   * Retrieves the icon for the property panel.
   *
   * @return the icon as an instance of Icon
   */
  public Icon getIcon() {
    return this.icon;
  }

  /**
   * Sets the icon for the property panel.
   *
   * @param icon the icon to set
   */
  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  /**
   * Binds the specified map object to the property panel. If the map object is null, clears the controls. Otherwise, sets the control values based on
   * the map object.
   *
   * @param mapObject the map object to bind
   */
  public void bind(IMapObject mapObject) {
    this.dataSource = mapObject;

    if (this.dataSource == null) {
      this.clearControls();
      return;
    }

    this.setControlValues(mapObject);
  }

  /**
   * Clears the controls in the property panel. This method must be implemented by subclasses.
   */
  protected abstract void clearControls();

  /**
   * Sets the control values based on the specified map object. This method must be implemented by subclasses.
   *
   * @param mapObject the map object to use for setting control values
   */
  protected abstract void setControlValues(IMapObject mapObject);

  /**
   * Sets up a JToggleButton to update the specified property of the map object.
   *
   * @param toggle   the JToggleButton to set up
   * @param property the property to update
   */
  public void setup(JToggleButton toggle, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    toggle.addActionListener(
      new MapObjectPropertyActionListener(m -> !m.hasCustomProperty(property) || m.getBoolValue(property) != toggle.isSelected(),
        m -> m.setValue(property, toggle.isSelected())));
  }

  /**
   * Sets up a JCheckBox to update the specified property of the map object.
   *
   * @param checkbox the JCheckBox to set up
   * @param property the property to update
   */
  public void setup(JCheckBox checkbox, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    checkbox.addActionListener(
      new MapObjectPropertyActionListener(m -> !m.hasCustomProperty(property) || m.getBoolValue(property) != checkbox.isSelected(),
        m -> m.setValue(property, checkbox.isSelected())));
  }

  /**
   * Sets up a JComboBox to update the specified property of the map object.
   *
   * @param <T>      the type of the items in the JComboBox
   * @param comboBox the JComboBox to set up
   * @param property the property to update
   */
  public <T> void setup(JComboBox<T> comboBox, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    comboBox.addActionListener(new MapObjectPropertyActionListener(m -> {
      if (!m.hasCustomProperty(property) || m.getStringValue(property, null) == null) {
        return true;
      }

      T value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
      return !m.getStringValue(property, null).equals(value.toString());
    }, m -> {
      T value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
      m.setValue(property, value != null ? value.toString() : null);
    }));
  }

  /**
   * Sets up a JComboBox with JLabel items to update the specified property of the map object.
   *
   * @param comboBox the JComboBox to set up
   * @param property the property to update
   */
  public void setupL(JComboBox<JLabel> comboBox, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    comboBox.addActionListener(new MapObjectPropertyActionListener(m -> {
      if (!m.hasCustomProperty(property) || m.getStringValue(property, null) == null) {
        return true;
      }

      JLabel value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
      return value != null && !m.getStringValue(property, null).equals(value.getText());
    }, m -> {
      JLabel value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
      m.setValue(property, value != null ? value.getText() : null);
    }));
  }

  /**
   * Sets up a JSlider to update the specified property of the map object.
   *
   * @param slider   the JSlider to set up
   * @param property the property to update
   */
  public void setup(JSlider slider, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    slider.addChangeListener(new SliderListener(property, slider));
  }

  /**
   * Sets up a JSlider to update the specified property of the map object, with a scaling factor.
   *
   * @param slider   the JSlider to set up
   * @param property the property to update
   * @param factor   the scaling factor to apply to the slider value
   */
  public void setup(JSlider slider, String property, float factor) {
    if (property == null || property.isEmpty()) {
      return;
    }
    slider.addChangeListener(new SliderListener(property, slider, factor));
  }

  /**
   * Sets up a JSpinner to update the specified property of the map object.
   *
   * @param spinner  the JSpinner to set up
   * @param property the property to update
   */
  public void setup(JSpinner spinner, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    spinner.addChangeListener(new SpinnerListener(property, spinner));
  }

  /**
   * Sets up a JTextField to update the specified property of the map object.
   *
   * @param textField the JTextField to set up
   * @param property  the property to update
   */
  public void setup(JTextField textField, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    textField.addFocusListener(new MapObjectPropertyFocusListener(m -> m.setValue(property, textField.getText())));
    textField.addActionListener(new MapObjectPropertyActionListener(
      m -> !m.hasCustomProperty(property) || m.getStringValue(property, null) == null || !m.getStringValue(property, null)
        .equals(textField.getText()), m -> m.setValue(property, textField.getText())));
  }

  /**
   * Sets up a TextList to update the specified property of the map object.
   *
   * @param textList the TextList to set up
   * @param property the property to update
   */
  public void setup(TextList textList, String property) {
    if (property == null || property.isEmpty()) {
      return;
    }
    textList.addActionListener(new MapObjectPropertyActionListener(
      m -> !m.hasCustomProperty(property) || m.getStringValue(property, null) == null || !m.getStringValue(property, null)
        .equals(textList.getJoinedString()), m -> m.setValue(property, textList.getJoinedString())));
  }

  /**
   * Sets up a JTable to update the specified properties of the map object.
   *
   * @param table      the JTable to set up
   * @param properties the properties to update
   */
  public void setup(JTable table, String... properties) {
    if (properties == null || properties.length == 0) {
      return;
    }
    table.getModel().addTableModelListener(new TableListener(table, properties));
  }

  /**
   * Updates the environment by reloading the map object and refreshing the entity controller. If the data source is not null, it retrieves the map
   * object, reloads it from the map, and refreshes the entity controller with the map object's ID.
   */
  protected void updateEnvironment() {
    if (getDataSource() != null) {
      IMapObject obj = getDataSource();
      Game.world().environment().reloadFromMap(obj.getId());
      UI.getEntityController().refresh(obj.getId());
    }
  }

  /**
   * Creates a layout for the property panel using the specified layout items and additional components. This method sets up a GroupLayout with
   * parallel and sequential groups for labels and components.
   *
   * @param layoutItems          an array of LayoutItem objects representing the components and their labels
   * @param additionalComponents additional components to be added to the layout
   * @return the created GroupLayout
   */
  protected LayoutManager createLayout(LayoutItem[] layoutItems, Component... additionalComponents) {
    GroupLayout groupLayout = new GroupLayout(this);

    // prepare the parallel group for the labels
    // add additional components to the group
    ParallelGroup parallel = groupLayout.createParallelGroup(Alignment.TRAILING);
    for (Component component : additionalComponents) {
      parallel.addComponent(component, Alignment.LEADING, CONTROL_MIN_WIDTH, CONTROL_WIDTH, Integer.MAX_VALUE);
    }

    for (LayoutItem item : layoutItems) {
      SequentialGroup horGrp = groupLayout.createSequentialGroup();
      if (item.getLabel() != null) {
        horGrp.addComponent(item.getLabel(), LABEL_WIDTH, LABEL_WIDTH, Integer.MAX_VALUE).addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(item.getComponent(), CONTROL_MIN_WIDTH, CONTROL_WIDTH, Integer.MAX_VALUE);
      } else {
        horGrp.addComponent(item.getComponent(), CONTROL_MIN_WIDTH, CONTROL_WIDTH, Integer.MAX_VALUE);
      }
      parallel.addGroup(Alignment.LEADING, horGrp);
    }

    // initialize the horizontal layout group with the parallel groups for
    // labels and components and some additional gaps
    groupLayout.setHorizontalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGroup(parallel)));

    // now prepare the vertical groups
    SequentialGroup seq = groupLayout.createSequentialGroup();
    SequentialGroup current = seq.addGap(CONTROL_MARGIN);

    for (LayoutItem item : layoutItems) {
      ParallelGroup verGrp = groupLayout.createParallelGroup(Alignment.LEADING);
      if (item.getLabel() != null) {
        verGrp.addComponent(item.getComponent(), item.getMinHeight(), item.getMinHeight(), item.getMinHeight())
          .addComponent(item.getLabel(), GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, item.getMinHeight()).addGap(CONTROL_MARGIN);
      } else {
        verGrp.addComponent(item.getComponent(), item.getMinHeight(), item.getMinHeight(), item.getMinHeight());
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

  /**
   * Applies changes to the map object by invoking the provided update action. This method also manages the undo/redo functionality and updates the
   * environment.
   *
   * @param updateAction the action to apply to the map object
   */
  private void applyChanges(Consumer<IMapObject> updateAction) {
    UndoManager.instance().mapObjectChanging(getDataSource());
    updateAction.accept(getDataSource());
    UndoManager.instance().mapObjectChanged(getDataSource());
    updateEnvironment();
  }

  /**
   * Represents an item in the layout of the property panel. Each item consists of a component and an optional label.
   */
  protected static class LayoutItem {

    private final String caption;
    private final Component component;
    private final JLabel label;
    private int minHeight;

    /**
     * Constructs a LayoutItem with the specified component.
     *
     * @param component the component for the layout item
     */
    public LayoutItem(Component component) {
      this.component = component;
      this.label = null;
      this.caption = "";
      this.setMinHeight(CONTROL_HEIGHT);
      ControlBehavior.apply(this.getComponent());
    }

    /**
     * Constructs a LayoutItem with the specified component and minimum height.
     *
     * @param component the component for the layout item
     * @param minHeight the minimum height for the layout item
     */
    public LayoutItem(Component component, int minHeight) {
      this(component);
      this.setMinHeight(minHeight);
    }

    /**
     * Constructs a LayoutItem with the specified resource string and component.
     *
     * @param resource  the resource string for the label
     * @param component the component for the layout item
     */
    public LayoutItem(String resource, Component component) {
      this.caption = Resources.strings().get(resource);
      this.component = component;
      this.label = new JLabel(this.caption);
      this.label.setVerticalAlignment(SwingConstants.CENTER);
      this.setMinHeight(CONTROL_HEIGHT);
      ControlBehavior.apply(this.getComponent());
    }

    /**
     * Constructs a LayoutItem with the specified resource string, component, and minimum height.
     *
     * @param resource  the resource string for the label
     * @param component the component for the layout item
     * @param minHeight the minimum height for the layout item
     */
    public LayoutItem(String resource, Component component, int minHeight) {
      this(resource, component);
      this.setMinHeight(minHeight);
    }

    /**
     * Retrieves the caption of the layout item.
     *
     * @return the caption as a String
     */
    public String getCaption() {
      return this.caption;
    }

    /**
     * Retrieves the label of the layout item.
     *
     * @return the label as a JLabel
     */
    public JLabel getLabel() {
      return this.label;
    }

    /**
     * Retrieves the component of the layout item.
     *
     * @return the component as a Component
     */
    public Component getComponent() {
      return this.component;
    }

    /**
     * Retrieves the minimum height of the layout item.
     *
     * @return the minimum height as an int
     */
    private int getMinHeight() {
      return minHeight;
    }

    /**
     * Sets the minimum height of the layout item.
     *
     * @param minHeight the minimum height to set
     */
    private void setMinHeight(int minHeight) {
      this.minHeight = minHeight;
    }
  }

  /**
   * An ItemListener that updates the map object property when an item state changes.
   */
  protected class MapObjectPropertyItemListener implements ItemListener {

    private final Consumer<IMapObject> updateAction;

    /**
     * Constructs a MapObjectPropertyItemListener with the specified update action.
     *
     * @param updateAction the action to apply to the map object
     */
    MapObjectPropertyItemListener(Consumer<IMapObject> updateAction) {
      this.updateAction = updateAction;
    }

    @Override public void itemStateChanged(ItemEvent arg0) {
      if (getDataSource() == null || Editor.instance().getMapComponent().isFocussing()) {
        return;
      }

      applyChanges(this.updateAction);
    }
  }

  /**
   * An ActionListener that updates the map object property when an action is performed.
   */
  protected class MapObjectPropertyActionListener implements ActionListener {

    private final Consumer<IMapObject> updateAction;
    private final Function<IMapObject, Boolean> newValueCheck;

    /**
     * Constructs a MapObjectPropertyActionListener with the specified new value check and update action.
     *
     * @param newValueCheck the function to check if the new value should be applied
     * @param updateAction  the action to apply to the map object
     */
    MapObjectPropertyActionListener(Function<IMapObject, Boolean> newValueCheck, Consumer<IMapObject> updateAction) {
      this.updateAction = updateAction;
      this.newValueCheck = newValueCheck;
    }

    @Override public void actionPerformed(ActionEvent e) {
      if (getDataSource() == null || Editor.instance().getMapComponent().isFocussing() || Boolean.FALSE.equals(
        this.newValueCheck.apply(getDataSource()))) {
        return;
      }

      applyChanges(this.updateAction);
    }
  }

  /**
   * A TableModelListener that updates the map object property when the table model changes.
   */
  protected class MabObjectPropertyTableModelListener implements TableModelListener {

    private final Consumer<IMapObject> updateAction;

    /**
     * Constructs a MabObjectPropertyTableModelListener with the specified update action.
     *
     * @param updateAction the action to apply to the map object
     */
    MabObjectPropertyTableModelListener(Consumer<IMapObject> updateAction) {
      this.updateAction = updateAction;
    }

    @Override public void tableChanged(TableModelEvent e) {
      if (getDataSource() == null || Editor.instance().getMapComponent().isFocussing()) {
        return;
      }
      applyChanges(this.updateAction);
    }
  }

  /**
   * A ChangeListener that updates the map object property when a change event occurs.
   */
  protected class MapObjectPropertyChangeListener implements ChangeListener {

    private final Consumer<IMapObject> updateAction;
    private final Function<IMapObject, Boolean> newValueCheck;

    /**
     * Constructs a MapObjectPropertyChangeListener with the specified new value check and update action.
     *
     * @param newValueCheck the function to check if the new value should be applied
     * @param updateAction  the action to apply to the map object
     */
    MapObjectPropertyChangeListener(Function<IMapObject, Boolean> newValueCheck, Consumer<IMapObject> updateAction) {
      this.updateAction = updateAction;
      this.newValueCheck = newValueCheck;
    }

    @Override public void stateChanged(ChangeEvent e) {
      if (getDataSource() == null || Editor.instance().getMapComponent().isFocussing() || Boolean.FALSE.equals(
        this.newValueCheck.apply(getDataSource()))) {
        return;
      }

      applyChanges(this.updateAction);
    }
  }

  /**
   * A ChangeListener for JSpinner components that updates the map object property when the spinner value changes.
   */
  protected class SpinnerListener extends MapObjectPropertyChangeListener {

    /**
     * Constructs a SpinnerListener with the specified map object property and spinner.
     *
     * @param mapObjectProperty the property to update
     * @param spinner           the JSpinner component
     */
    SpinnerListener(String mapObjectProperty, JSpinner spinner) {
      super(
        m -> m.hasCustomProperty(mapObjectProperty) || m.getStringValue(mapObjectProperty, null) == null || !m.getStringValue(mapObjectProperty, null)
          .equals(spinner.getValue().toString()), m -> m.setValue(mapObjectProperty, spinner.getValue().toString()));
    }
  }

  /**
   * A ChangeListener for JSlider components that updates the map object property when the slider value changes.
   */
  protected class SliderListener extends MapObjectPropertyChangeListener {

    /**
     * Constructs a SliderListener with the specified map object property and slider.
     *
     * @param mapObjectProperty the property to update
     * @param slider            the JSlider component
     */
    SliderListener(String mapObjectProperty, JSlider slider) {
      super(m -> m.hasCustomProperty(mapObjectProperty) || m.getIntValue(mapObjectProperty, 0) != slider.getValue(),
        m -> m.setValue(mapObjectProperty, slider.getValue()));
    }

    /**
     * Constructs a SliderListener with the specified map object property, slider, and scaling factor.
     *
     * @param mapObjectProperty the property to update
     * @param slider            the JSlider component
     * @param factor            the scaling factor to apply to the slider value
     */
    SliderListener(String mapObjectProperty, JSlider slider, float factor) {
      super(m -> m.hasCustomProperty(mapObjectProperty) || m.getFloatValue(mapObjectProperty) != slider.getValue() * factor,
        m -> m.setValue(mapObjectProperty, slider.getValue() * factor));
    }
  }

  /**
   * A TableModelListener for JTable components that updates the map object properties when the table model changes.
   */
  protected class TableListener extends MabObjectPropertyTableModelListener {

    /**
     * Constructs a TableListener with the specified table and map object properties.
     *
     * @param table               the JTable component
     * @param mapObjectProperties the properties to update
     */
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

  /**
   * A FocusAdapter for JTextField components that updates the map object property when the text field loses focus.
   */
  protected class MapObjectPropertyFocusListener extends FocusAdapter {

    private final Consumer<IMapObject> updateAction;

    /**
     * Constructs a MapObjectPropertyFocusListener with the specified update action.
     *
     * @param updateAction the action to apply to the map object
     */
    MapObjectPropertyFocusListener(Consumer<IMapObject> updateAction) {
      this.updateAction = updateAction;
    }

    @Override public void focusLost(FocusEvent e) {
      if (getDataSource() == null || Editor.instance().getMapComponent().isFocussing()) {
        return;
      }

      applyChanges(this.updateAction);
    }
  }
}

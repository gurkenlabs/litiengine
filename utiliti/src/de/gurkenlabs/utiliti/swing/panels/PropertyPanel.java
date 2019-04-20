package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.swing.TextList;

@SuppressWarnings("serial")
public abstract class PropertyPanel extends JPanel {
  public static final int LABEL_WIDTH = 80;
  public static final int LABEL_HEIGHT = 25;
  public static final int CONTROL_MIN_WIDTH = 120;
  public static final int CONTROL_WIDTH = 200;
  public static final int CONTROL_HEIGHT = 25;
  public static final int CONTROL_MARGIN = 5;
  public static final int LABEL_GAP = 0;

  protected boolean isFocussing;
  private transient IMapObject dataSource;
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

  protected void setup(JCheckBox checkbox, String property) {
    checkbox.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(property, checkbox.isSelected())));
  }

  protected <T> void setup(JComboBox<T> comboBox, String property) {
    comboBox.addActionListener(new MapObjectPropertyActionListener(m -> {
      T value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
      m.setValue(property, value.toString());
    }));
  }

  protected void setupL(JComboBox<JLabel> comboBox, String property) {
    comboBox.addActionListener(new MapObjectPropertyActionListener(m -> {
      JLabel value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
      m.setValue(property, value.getText());
    }));
  }

  protected void setup(JSpinner spinner, String property) {
    spinner.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setValue(property, spinner.getValue().toString())));
  }

  protected void setup(JTextField textField, String property) {
    textField.addFocusListener(new MapObjectPropteryFocusListener(m -> m.setValue(property, textField.getText())));
    textField.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(property, textField.getText())));
  }

  protected void setup(TextList textList, String property) {
    textList.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(property, textList.getJoinedString())));
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

  protected void updateEnvironment(final IMapObject before) {
    if (getDataSource() instanceof IMapObject) {
      IMapObject obj = getDataSource();
      Game.world().environment().reloadFromMap(obj.getId());
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
      parallel.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup().addComponent(item.getLabel(), LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(item.getComponent(), CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE));
    }

    // initialize the horizontal layout group with the parallel groups for
    // labels and components and some additional gaps
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(parallel)));

    // now prepare the vertical groups
    SequentialGroup seq = groupLayout.createSequentialGroup();
    SequentialGroup current = seq.addGap(CONTROL_MARGIN);

    for (LayoutItem item : layoutItems) {
      current = current.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(item.getComponent(), GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, CONTROL_HEIGHT).addComponent(item.getLabel(), GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, LABEL_HEIGHT)).addGap(CONTROL_MARGIN);
    }

    current.addPreferredGap(ComponentPlacement.UNRELATED);
    for (Component component : additionalComponents) {
      current = current.addComponent(component, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addGap(CONTROL_MARGIN);
    }

    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addGroup(seq));
    return groupLayout;
  }

  private void applyChanges(Consumer<IMapObject> updateAction) {
    final IMapObject before = new MapObject((MapObject) getDataSource());
    UndoManager.instance().mapObjectChanging(getDataSource());
    updateAction.accept(getDataSource());
    UndoManager.instance().mapObjectChanged(getDataSource());
    updateEnvironment(before);
  }

  protected class LayoutItem {
    private final String caption;
    private final Component component;
    private final JLabel label;

    public LayoutItem(String resource, Component component) {
      this.caption = Resources.strings().get(resource);
      this.component = component;
      this.label = new JLabel(this.caption);
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
  }
}

package de.gurkenlabs.utiliti.swing.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.UndoManager;

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

  public PropertyPanel(String identifier) {
    this.identifier = identifier;
    setBorder(null);
  }

  public PropertyPanel() {
  }

  protected IMapObject getDataSource() {
    return this.dataSource;
  }

  public String getIdentifier() {
    return this.identifier;
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
      Optional<Spritesheet> opt = Resources.spritesheets().tryGet(entry.getValue());
      if (opt.isPresent() && opt.get().getTotalNumberOfSprites() > 0) {
        BufferedImage scaled = opt.get().getPreview(24);
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

  protected <T extends Enum<?>> void setup(JComboBox<T> comboBox, String property) {
    comboBox.addActionListener(new MapObjectPropertyActionListener(m -> {
      T value = comboBox.getModel().getElementAt(comboBox.getSelectedIndex());
      m.setValue(property, value);
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
    textField.addFocusListener(new MapObjectPropteryFocusListener(m -> m.setValue(MapObjectProperty.SPAWN_TYPE, textField.getText())));
    textField.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.SPAWN_TYPE, textField.getText())));
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
      if (MapObjectType.get(obj.getType()) == MapObjectType.LIGHTSOURCE) {
        Game.world().environment().getAmbientLight().updateSection(MapObject.getBounds(before, obj));
      }
    }
  }

  private void applyChanges(Consumer<IMapObject> updateAction) {
    final IMapObject before = new MapObject((MapObject) getDataSource());
    UndoManager.instance().mapObjectChanging(getDataSource());
    updateAction.accept(getDataSource());
    UndoManager.instance().mapObjectChanged(getDataSource());
    updateEnvironment(before);
  }
}

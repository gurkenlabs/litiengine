package de.gurkenlabs.utiliti.swing.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.utiliti.UndoManager;

@SuppressWarnings("serial")
public abstract class PropertyPanel extends JPanel {
  protected boolean isFocussing;
  private transient IMapObject dataSource;

  public PropertyPanel() {
  }

  protected IMapObject getDataSource() {
    return this.dataSource;
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
      Spritesheet sprite = Spritesheet.find(entry.getValue());
      if (sprite != null && sprite.getTotalNumberOfSprites() > 0) {
        BufferedImage scaled = sprite.getPreview(24);
        if (scaled != null) {
          label.setIcon(new ImageIcon(scaled));
        }
      }

      comboBox.addItem(label);
    }
  }

  protected static void selectSpriteSheet(JComboBox<JLabel> comboBox, IMapObject mapObject) {
    if (mapObject.getString(MapObjectProperty.SPRITESHEETNAME) != null) {
      for (int i = 0; i < comboBox.getModel().getSize(); i++) {
        JLabel label = comboBox.getModel().getElementAt(i);
        if (label != null && label.getText().equals(mapObject.getString(MapObjectProperty.SPRITESHEETNAME))) {
          comboBox.setSelectedItem(label);
          break;
        }
      }
    }
  }

  protected abstract void clearControls();

  protected abstract void setControlValues(IMapObject mapObject);

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
      super(m -> m.set(mapObjectProperty, spinner.getValue().toString()));
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
      Game.getEnvironment().reloadFromMap(obj.getId());
      if (MapObjectType.get(obj.getType()) == MapObjectType.LIGHTSOURCE) {
        Game.getEnvironment().getAmbientLight().updateSection(MapObject.getBounds(before, obj));
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

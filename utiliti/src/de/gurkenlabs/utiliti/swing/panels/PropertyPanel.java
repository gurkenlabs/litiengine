package de.gurkenlabs.utiliti.swing.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.utiliti.UndoManager;

@SuppressWarnings("serial")
public abstract class PropertyPanel<T extends IMapObject> extends JPanel {
  public PropertyPanel() {
  }
  private transient T dataSource;

  protected boolean isFocussing;

  protected T getDataSource() {
    return this.dataSource;
  }

  public void bind(T mapObject) {
    this.dataSource = mapObject;

    this.isFocussing = true;

    if (this.dataSource == null) {
      this.clearControls();
      return;
    }

    this.setControlValues(mapObject);
    this.isFocussing = false;
  }

  protected abstract void clearControls();

  protected abstract void setControlValues(T mapObject);

  protected class MapObjectPropertyItemListener implements ItemListener {
    private final Consumer<T> updateAction;

    MapObjectPropertyItemListener(Consumer<T> updateAction) {
      this.updateAction = updateAction;
    }

    @Override
    public void itemStateChanged(ItemEvent arg0) {
      if (getDataSource() == null || isFocussing) {
        return;
      }

      UndoManager.instance().mapObjectChanging(getDataSource());
      this.updateAction.accept(getDataSource());
      UndoManager.instance().mapObjectChanged(getDataSource());
      updateEnvironment();
    }

  }

  protected class MapObjectPropertyActionListener implements ActionListener {
    private final Consumer<T> updateAction;

    MapObjectPropertyActionListener(Consumer<T> updateAction) {
      this.updateAction = updateAction;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      if (getDataSource() == null || isFocussing) {
        return;
      }

      UndoManager.instance().mapObjectChanging(getDataSource());
      this.updateAction.accept(getDataSource());
      UndoManager.instance().mapObjectChanged(getDataSource());
      updateEnvironment();
    }
  }

  protected class MapObjectPropertyChangeListener implements ChangeListener {
    private final Consumer<T> updateAction;

    MapObjectPropertyChangeListener(Consumer<T> updateAction) {
      this.updateAction = updateAction;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      if (getDataSource() == null || isFocussing) {
        return;
      }

      UndoManager.instance().mapObjectChanging(getDataSource());
      this.updateAction.accept(getDataSource());
      UndoManager.instance().mapObjectChanged(getDataSource());
      updateEnvironment();
    }
  }

  protected class SpinnerListener extends MapObjectPropertyChangeListener{
    SpinnerListener(String mapObjectProperty, JSpinner spinner) {
      super(m -> m.setCustomProperty(mapObjectProperty, spinner.getValue().toString()));
    }
  }

  protected class MapObjectPropteryFocusListener extends FocusAdapter {
    private final Consumer<T> updateAction;

    MapObjectPropteryFocusListener(Consumer<T> updateAction) {
      this.updateAction = updateAction;
    }

    @Override
    public void focusLost(FocusEvent e) {
      if (getDataSource() == null || isFocussing) {
        return;
      }

      UndoManager.instance().mapObjectChanging(getDataSource());
      this.updateAction.accept(getDataSource());
      UndoManager.instance().mapObjectChanged(getDataSource());
      updateEnvironment();
    }
  }

  protected void updateEnvironment() {
    if (getDataSource() instanceof IMapObject) {
      IMapObject obj = (IMapObject) getDataSource();
      Game.getEnvironment().reloadFromMap(obj.getId());
      if (MapObjectType.get(obj.getType()) == MapObjectType.LIGHTSOURCE) {
        Game.getEnvironment().getAmbientLight().updateSection(getDataSource().getBoundingBox());
      }
    }

  }
}

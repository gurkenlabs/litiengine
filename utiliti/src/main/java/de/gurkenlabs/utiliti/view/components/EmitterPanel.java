package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

public class EmitterPanel extends PropertyPanel {

  public enum EmitterPropertyGroup {
    EMISSION,
    APPEARANCE,
    TRANSFORM,
    PHYSICS
  }

  JTabbedPane propertyGrouptabs;
  private final JButton restartPreview;
  private final JToggleButton pausePreview;
  private final JPanel previewActions;
  private transient Emitter emitter;
  private int boundEmitterId = -1;

  public EmitterPanel() {
    super("panel_emitter", Icons.EMITTER_24);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    previewActions = new JPanel(new FlowLayout(FlowLayout.TRAILING, CONTROL_MARGIN, 0));
    previewActions.setOpaque(false);
    restartPreview = Style.iconButton(Icons.REWIND_16);
    restartPreview.setToolTipText(Resources.strings().get("emitter_restartPreview_tip"));
    restartPreview.setEnabled(false);
    pausePreview = Style.iconToggleButton(Icons.PAUSE_16, false);
    pausePreview.setToolTipText(Resources.strings().get("emitter_pausePreview_tip"));
    pausePreview.setEnabled(false);
    previewActions.add(restartPreview);
    previewActions.add(pausePreview);

    this.propertyGrouptabs = new JTabbedPane();
    this.propertyGrouptabs.setAlignmentX(Component.LEFT_ALIGNMENT);
    this.propertyGrouptabs.setTabPlacement(SwingConstants.TOP);
    for (EmitterPropertyGroup e : EmitterPropertyGroup.values()) {
      String localized =
          Resources.strings().get(String.format("emitter_%s", e.name().toLowerCase()));
      this.propertyGrouptabs.addTab(localized, EmitterPropertyPanel.getEmitterPropertyPanel(e));
    }

    this.add(this.propertyGrouptabs);

    restartPreview.addActionListener(event -> {
      if (boundEmitterId < 0 || Game.world().environment() == null) {
        return;
      }
      Game.world().environment().reloadFromMap(boundEmitterId);
      Emitter current = resolveEmitter();
      if (current != null) {
        current.deactivate();
        current.setStopped(false);
        current.setPaused(false);
        current.activate();
      }
      refreshPreviewControls();
    });
    pausePreview.addActionListener(event -> {
      Emitter current = resolveEmitter();
      if (current != null) {
        current.setPaused(pausePreview.isSelected());
      }
      refreshPreviewControls();
    });
  }

  @Override
  public void bind(IMapObject mapObject) {
    for (EmitterPropertyGroup e : EmitterPropertyGroup.values()) {
      ((EmitterPropertyPanel) this.propertyGrouptabs.getComponent(e.ordinal())).bind(mapObject);
    }
    bindPreview(mapObject);
  }

  @Override
  public void bindAll(List<IMapObject> mapObjects) {
    for (EmitterPropertyGroup e : EmitterPropertyGroup.values()) {
      ((EmitterPropertyPanel) this.propertyGrouptabs.getComponent(e.ordinal())).bindAll(mapObjects);
    }
    bindPreview(mapObjects != null && mapObjects.size() == 1 ? mapObjects.get(0) : null);
  }

  private void bindPreview(IMapObject mapObject) {
    this.boundEmitterId = mapObject == null ? -1 : mapObject.getId();
    refreshPreviewControls();
  }

  private Emitter resolveEmitter() {
    this.emitter = this.boundEmitterId < 0 || Game.world().environment() == null
        ? null
        : Game.world().environment().getEmitter(this.boundEmitterId);
    return this.emitter;
  }

  private void refreshPreviewControls() {
    Emitter current = resolveEmitter();
    this.restartPreview.setEnabled(this.boundEmitterId >= 0);
    this.pausePreview.setEnabled(current != null);
    this.pausePreview.setSelected(current != null && current.isPaused());
    updatePreviewButton();
  }

  private void updatePreviewButton() {
    boolean paused = this.pausePreview.isSelected();
    this.pausePreview.setIcon(paused ? Icons.PLAY_16 : Icons.PAUSE_16);
    this.pausePreview.setToolTipText(Resources.strings().get(
        paused ? "emitter_resumePreview" : "emitter_pausePreview_tip"));
  }

  @Override
  public Component getHeaderActions() {
    return this.previewActions;
  }



  @Override
  protected void clearControls() {
    // do nothing
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    // do nothing
  }
}

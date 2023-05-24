package de.gurkenlabs.utiliti.swing.panels.emission;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.utiliti.swing.panels.PropertyPanel;
import de.gurkenlabs.utiliti.swing.panels.emission.EmitterPanel.EmitterPropertyGroup;
import java.awt.LayoutManager;

public abstract class EmitterPropertyPanel extends PropertyPanel {

  private transient Emitter emitter;

  protected EmitterPropertyPanel() {
    super();
    this.setBorder(STANDARDBORDER);
  }

  public static EmitterPropertyPanel getEmitterPropertyPanel(EmitterPropertyGroup category) {
    return switch (category) {
      case COLLISION -> new ParticleCollisionPanel();
      case EMISSION -> new EmissionPanel();
      case ORIGIN -> new ParticleOriginPanel();
      case ROTATION -> new ParticleRotationPanel();
      case SIZE -> new ParticleSizePanel();
      case STYLE -> new ParticleStylePanel();
      case MOTION -> new ParticleMotionPanel();
    };
  }
  public Emitter getEmitter() {
    return emitter;
  }
  @Override
  protected void clearControls() {
    this.emitter = null;
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.emitter = Game.world().environment().getEmitter(mapObject.getId());
  }

  protected abstract LayoutManager createLayout();

  protected abstract void setupChangedListeners();
}

package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.EmitterInfo;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.utiliti.swing.Icons;
import de.gurkenlabs.utiliti.swing.panels.UpdatedEmitterPanel.EmitterPropertyGroup;

@SuppressWarnings("serial")
public abstract class UpdatedEmitterPropertyPanel extends PropertyPanel {

  protected transient Emitter emitter;
  private static final double PARTICLESPINNER_MAX_VALUE = 100.0;
  private static final double PARTICLEDELTA_MAX_VALUE = 1.0;
  private static final double PARTICLEDELTA_DEFAULT_VALUE = 0.1;

  private UpdatedEmitterPropertyPanel() {
    super();
  }

  public static UpdatedEmitterPropertyPanel getEmitterPropertyPanel(EmitterPropertyGroup category) {
    switch (category) {
    case ACCELERATION:
      return new ParticleAccelerationPanel();
    case COLLISION:
      return new ParticleCollisionPanel();
    case COLOR:
      return new ParticleColorPanel();
    case EMISSION:
      return new EmissionPanel();
    case ORIGIN:
      return new ParticleOffsetPanel();
    case SIZE:
      return new ParticleSizePanel();
    case STYLE:
      return new ParticleStylePanel();
    case VELOCITY:
      return new ParticleVelocityPanel();
    default:
      return null;
    }
  };

  protected abstract LayoutManager createLayout();

  protected abstract void setupChangedListeners();

  private static SpinnerNumberModel getParticleMinModel() {
    return new SpinnerNumberModel(-PARTICLEDELTA_DEFAULT_VALUE, -PARTICLESPINNER_MAX_VALUE, PARTICLESPINNER_MAX_VALUE, 0.1);
  }

  private static SpinnerNumberModel getParticleMaxModel() {
    return new SpinnerNumberModel(PARTICLEDELTA_DEFAULT_VALUE, -PARTICLESPINNER_MAX_VALUE, PARTICLESPINNER_MAX_VALUE, 0.1);
  }

  private static SpinnerNumberModel getLocationModel() {
    return new SpinnerNumberModel(0.0, -PARTICLESPINNER_MAX_VALUE, PARTICLESPINNER_MAX_VALUE, 0.1);
  }

  private static SpinnerNumberModel getDeltaModel() {
    return new SpinnerNumberModel(0.0, -PARTICLEDELTA_MAX_VALUE, PARTICLEDELTA_MAX_VALUE, 0.01);
  }

  private static SpinnerNumberModel getParticleDimensionModel() {
    return new SpinnerNumberModel(1.0, 0.0, PARTICLESPINNER_MAX_VALUE, 1.0);
  }

  private static SpinnerNumberModel getPercentModel() {
    return new SpinnerNumberModel(0.0, 0.0, 1.0, 0.01);
  }

  private static class EmissionPanel extends UpdatedEmitterPropertyPanel {
    private JSpinner spawnRateSpinner;
    private JSpinner spawnAmountSpinner;
    private JSpinner updateDelaySpinner;
    private JSpinner ttlSpinner;
    private JSpinner maxParticlesSpinner;
    private JToggleButton btnPause;

    private EmissionPanel() {
      super();
      this.spawnRateSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_SPAWNRATE, 10, Integer.MAX_VALUE, 10));
      this.spawnAmountSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_SPAWNAMOUNT, 1, 500, 1));
      this.updateDelaySpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_UPDATERATE, 0, Integer.MAX_VALUE, 10));
      this.ttlSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_TTL, 0, Integer.MAX_VALUE, 100));
      this.maxParticlesSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_MAXPARTICLES, 1, Integer.MAX_VALUE, 1));

      this.btnPause = new JToggleButton();
      this.btnPause.setSelected(true);
      this.btnPause.setIcon(Icons.PAUSE);

      setLayout(this.createLayout());
      this.setBorder(new EmptyBorder(0, 4, 0, 0));
      this.setupChangedListeners();
    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("emitter_spawnrate", spawnRateSpinner), new LayoutItem("emitter_spawnamount", spawnAmountSpinner), new LayoutItem("emitter_updateDelay", updateDelaySpinner), new LayoutItem("emitter_ttl", ttlSpinner),
          new LayoutItem("emitter_maxparticles", maxParticlesSpinner) };
      return this.createLayout(layoutItems, this.btnPause);
    }

    @Override
    protected void clearControls() {
      this.spawnRateSpinner.setValue(0);
      this.spawnAmountSpinner.setValue(0);
      this.updateDelaySpinner.setValue(0);
      this.ttlSpinner.setValue(0);
      this.maxParticlesSpinner.setValue(0);
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());
      this.spawnRateSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNRATE));
      this.spawnAmountSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNAMOUNT));
      this.updateDelaySpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.UPDATERATE));
      this.ttlSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.TIMETOLIVE));
      this.maxParticlesSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.MAXPARTICLES));
    }

    @Override
    protected void setupChangedListeners() {
      this.setup(this.spawnRateSpinner, MapObjectProperty.Emitter.SPAWNRATE);
      this.setup(this.spawnAmountSpinner, MapObjectProperty.Emitter.SPAWNAMOUNT);
      this.setup(this.updateDelaySpinner, MapObjectProperty.Emitter.UPDATERATE);
      this.setup(this.ttlSpinner, MapObjectProperty.Emitter.TIMETOLIVE);
      this.setup(this.maxParticlesSpinner, MapObjectProperty.Emitter.MAXPARTICLES);
      this.btnPause.addActionListener(a -> {
        if (this.emitter != null) {
          this.emitter.togglePaused();
        }

        if (!btnPause.isSelected()) {
          this.btnPause.setIcon(Icons.PLAY);
        } else {
          this.btnPause.setIcon(Icons.PAUSE);
        }
      });
    }
  }

  private static class ParticleStylePanel extends UpdatedEmitterPropertyPanel {
    private ParticleStylePanel() {
      super();
    }

    @Override
    protected void clearControls() {
      // TODO Auto-generated method stub

    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());

    }

    @Override
    protected LayoutManager createLayout() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected void setupChangedListeners() {
      // TODO Auto-generated method stub

    }
  }

  private static class ParticleColorPanel extends UpdatedEmitterPropertyPanel {
    private ParticleColorPanel() {
      super();
    }

    @Override
    protected void clearControls() {
      // TODO Auto-generated method stub

    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());

    }

    @Override
    protected LayoutManager createLayout() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected void setupChangedListeners() {
      // TODO Auto-generated method stub

    }
  }

  private static class ParticleSizePanel extends UpdatedEmitterPropertyPanel {
    private ParticleSizePanel() {
      super();
    }

    @Override
    protected void clearControls() {
      // TODO Auto-generated method stub

    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());

    }

    @Override
    protected LayoutManager createLayout() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected void setupChangedListeners() {
      // TODO Auto-generated method stub

    }
  }

  private static class ParticleOffsetPanel extends UpdatedEmitterPropertyPanel {
    private ParticleOffsetPanel() {
      super();
    }

    @Override
    protected void clearControls() {
      // TODO Auto-generated method stub

    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());

    }

    @Override
    protected LayoutManager createLayout() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected void setupChangedListeners() {
      // TODO Auto-generated method stub

    }
  }

  private static class ParticleVelocityPanel extends UpdatedEmitterPropertyPanel {
    private ParticleVelocityPanel() {
      super();
    }

    @Override
    protected void clearControls() {
      // TODO Auto-generated method stub

    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());

    }

    @Override
    protected LayoutManager createLayout() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected void setupChangedListeners() {
      // TODO Auto-generated method stub

    }
  }

  private static class ParticleAccelerationPanel extends UpdatedEmitterPropertyPanel {
    private ParticleAccelerationPanel() {
      super();
    }

    @Override
    protected void clearControls() {
      // TODO Auto-generated method stub

    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());

    }

    @Override
    protected LayoutManager createLayout() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected void setupChangedListeners() {
      // TODO Auto-generated method stub

    }
  }

  private static class ParticleCollisionPanel extends UpdatedEmitterPropertyPanel {
    private ParticleCollisionPanel() {
      super();
    }

    @Override
    protected void clearControls() {
      // TODO Auto-generated method stub

    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());

    }

    @Override
    protected LayoutManager createLayout() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected void setupChangedListeners() {
      // TODO Auto-generated method stub

    }
  }

}

package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import com.github.weisj.darklaf.ui.togglebutton.DarkToggleButtonUI;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.utiliti.swing.ColorList;
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
    this.setBorder(new EmptyBorder(0, 4, 0, 0));
  }

  public static UpdatedEmitterPropertyPanel getEmitterPropertyPanel(EmitterPropertyGroup category) {
    switch (category) {
    case ACCELERATION:
      return new ParticleAccelerationPanel();
    case COLLISION:
      return new ParticleCollisionPanel();
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
      spawnRateSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_SPAWNRATE, 10, Integer.MAX_VALUE, 10));
      spawnAmountSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_SPAWNAMOUNT, 1, 500, 1));
      updateDelaySpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_UPDATERATE, 0, Integer.MAX_VALUE, 10));
      ttlSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_TTL, 0, Integer.MAX_VALUE, 100));
      maxParticlesSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_MAXPARTICLES, 1, Integer.MAX_VALUE, 1));

      btnPause = new JToggleButton();
      btnPause.setSelected(true);
      btnPause.setIcon(Icons.PAUSE);

      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("emitter_spawnrate", spawnRateSpinner), new LayoutItem("emitter_spawnamount", spawnAmountSpinner), new LayoutItem("emitter_updateDelay", updateDelaySpinner), new LayoutItem("emitter_ttl", ttlSpinner),
          new LayoutItem("emitter_maxparticles", maxParticlesSpinner) };
      return createLayout(layoutItems, btnPause);
    }

    @Override
    protected void clearControls() {
      spawnRateSpinner.setValue(0);
      spawnAmountSpinner.setValue(0);
      updateDelaySpinner.setValue(0);
      ttlSpinner.setValue(0);
      maxParticlesSpinner.setValue(0);
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      emitter = Game.world().environment().getEmitter(mapObject.getId());
      spawnRateSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNRATE));
      spawnAmountSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNAMOUNT));
      updateDelaySpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.UPDATERATE));
      ttlSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.TIMETOLIVE));
      maxParticlesSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.MAXPARTICLES));
    }

    @Override
    protected void setupChangedListeners() {
      setup(spawnRateSpinner, MapObjectProperty.Emitter.SPAWNRATE);
      setup(spawnAmountSpinner, MapObjectProperty.Emitter.SPAWNAMOUNT);
      setup(updateDelaySpinner, MapObjectProperty.Emitter.UPDATERATE);
      setup(ttlSpinner, MapObjectProperty.Emitter.TIMETOLIVE);
      setup(maxParticlesSpinner, MapObjectProperty.Emitter.MAXPARTICLES);
      btnPause.addActionListener(a -> {
        if (emitter != null) {
          emitter.togglePaused();
        }

        if (!btnPause.isSelected()) {
          btnPause.setIcon(Icons.PLAY);
        } else {
          btnPause.setIcon(Icons.PAUSE);
        }
      });
    }
  }

  private static class ParticleStylePanel extends UpdatedEmitterPropertyPanel {
    private JComboBox<ParticleType> comboBoxParticleType;
    private JToggleButton fade;
    private final ColorList colorList;
    private final JScrollPane colorListScrollPane;

    private ParticleStylePanel() {
      super();
      comboBoxParticleType = new JComboBox<>(new DefaultComboBoxModel<ParticleType>(ParticleType.values()));
      fade = new JToggleButton();
      fade.putClientProperty("JToggleButton.variant", DarkToggleButtonUI.VARIANT_SLIDER);

      colorList = new ColorList();
      colorListScrollPane = new JScrollPane();
      colorListScrollPane.setViewportView(colorList);

      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    protected void clearControls() {
      comboBoxParticleType.setSelectedItem(EmitterData.DEFAULT_PARTICLE_TYPE);
      fade.setSelected(EmitterData.DEFAULT_FADE);
      colorList.clear();
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      emitter = Game.world().environment().getEmitter(mapObject.getId());
      comboBoxParticleType.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class, EmitterData.DEFAULT_PARTICLE_TYPE));
      fade.setSelected(mapObject.getBoolValue(MapObjectProperty.Particle.FADE));
      colorList.setColors(mapObject.getStringValue(MapObjectProperty.Emitter.COLORS));
    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("emitter_particleType", comboBoxParticleType), new LayoutItem("particle_fade", fade), new LayoutItem("particle_colors", colorListScrollPane, CONTROL_HEIGHT * 3) };
      return this.createLayout(layoutItems);
    }

    @Override
    protected void setupChangedListeners() {
      setup(comboBoxParticleType, MapObjectProperty.Emitter.PARTICLETYPE);
      setup(fade, MapObjectProperty.Particle.FADE);
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

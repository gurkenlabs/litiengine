package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import com.github.weisj.darklaf.ui.togglebutton.DarkToggleButtonUI;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.utiliti.swing.ColorTable;
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
      return new ParticleOriginPanel();
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
    private JToggleButton outlineOnly;
    private final ColorTable colorTable;
    private JSpinner colorVariance;
    private JSpinner alphaVariance;

    private ParticleStylePanel() {
      super();
      comboBoxParticleType = new JComboBox<>(new DefaultComboBoxModel<ParticleType>(ParticleType.values()));
      outlineOnly = new JToggleButton();
      outlineOnly.putClientProperty("JToggleButton.variant", DarkToggleButtonUI.VARIANT_SLIDER);
      fade = new JToggleButton();
      fade.putClientProperty("JToggleButton.variant", DarkToggleButtonUI.VARIANT_SLIDER);
      colorTable = new ColorTable();
      colorVariance = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_COLOR_VARIANCE, 0d, 1d, .05d));
      alphaVariance = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_ALPHA_VARIANCE, 0d, 1d, .05d));

      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      colorTable.bind(mapObject);
    }

    @Override
    protected void clearControls() {
      comboBoxParticleType.setSelectedItem(EmitterData.DEFAULT_PARTICLE_TYPE);
      outlineOnly.setSelected(EmitterData.DEFAULT_OUTLINE_ONLY);
      fade.setSelected(EmitterData.DEFAULT_FADE);
      colorVariance.setValue(EmitterData.DEFAULT_COLOR_VARIANCE);
      alphaVariance.setValue(EmitterData.DEFAULT_ALPHA_VARIANCE);
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      emitter = Game.world().environment().getEmitter(mapObject.getId());
      comboBoxParticleType.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class, EmitterData.DEFAULT_PARTICLE_TYPE));
      outlineOnly.setSelected(mapObject.getBoolValue(MapObjectProperty.Particle.OUTLINEONLY, EmitterData.DEFAULT_OUTLINE_ONLY));
      fade.setSelected(mapObject.getBoolValue(MapObjectProperty.Particle.FADE, EmitterData.DEFAULT_FADE));
      colorVariance.setValue(mapObject.getFloatValue(MapObjectProperty.Emitter.COLORVARIANCE, EmitterData.DEFAULT_COLOR_VARIANCE));
      alphaVariance.setValue(mapObject.getFloatValue(MapObjectProperty.Emitter.ALPHAVARIANCE, EmitterData.DEFAULT_ALPHA_VARIANCE));
    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("emitter_particleType", comboBoxParticleType), new LayoutItem("particle_fade", fade), new LayoutItem("particle_outlineonly", outlineOnly), new LayoutItem("particle_colors", colorTable, CONTROL_HEIGHT * 3),
          new LayoutItem("emitter_colorVariance", colorVariance), new LayoutItem("emitter_alphaVariance", alphaVariance) };
      return this.createLayout(layoutItems);
    }

    @Override
    protected void setupChangedListeners() {
      setup(comboBoxParticleType, MapObjectProperty.Emitter.PARTICLETYPE);
      setup(outlineOnly, MapObjectProperty.Particle.OUTLINEONLY);
      setup(fade, MapObjectProperty.Particle.FADE);
      setup(colorVariance, MapObjectProperty.Emitter.COLORVARIANCE);
      setup(alphaVariance, MapObjectProperty.Emitter.ALPHAVARIANCE);
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
      return null;
    }

    @Override
    protected void setupChangedListeners() {
      // TODO Auto-generated method stub

    }
  }

  private static class ParticleOriginPanel extends UpdatedEmitterPropertyPanel {
    private JComboBox<Align> comboBoxAlign;
    private JComboBox<Valign> comboBoxValign;
    private ParticleParameterModifier offsetX;
    private ParticleParameterModifier offsetY;

    private ParticleOriginPanel() {
      super();
      comboBoxAlign = new JComboBox<>(new DefaultComboBoxModel<Align>(Align.values()));
      comboBoxValign = new JComboBox<>(new DefaultComboBoxModel<Valign>(Valign.values()));
      offsetX = new ParticleParameterModifier(MapObjectProperty.Particle.MINX, MapObjectProperty.Particle.MAXX, Short.MIN_VALUE, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_OFFSET_X, EmitterData.DEFAULT_MAX_OFFSET_X, 1);
      offsetY = new ParticleParameterModifier(MapObjectProperty.Particle.MINY, MapObjectProperty.Particle.MAXY, Short.MIN_VALUE, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_OFFSET_Y, EmitterData.DEFAULT_MAX_OFFSET_Y, 1);

      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      offsetX.bind(mapObject);
      offsetY.bind(mapObject);
    }

    @Override
    protected void clearControls() {
      comboBoxAlign.setSelectedItem(EmitterData.DEFAULT_ORIGIN_ALIGN);
      comboBoxValign.setSelectedItem(EmitterData.DEFAULT_ORIGIN_VALIGN);

    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());
      comboBoxAlign.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_ALIGN, Align.class, EmitterData.DEFAULT_ORIGIN_ALIGN));
      comboBoxValign.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.Emitter.ORIGIN_VALIGN, Valign.class, EmitterData.DEFAULT_ORIGIN_VALIGN));
    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("emitter_originAlign", comboBoxAlign), new LayoutItem("emitter_originValign", comboBoxValign), new LayoutItem("emitter_offsetX", offsetX), new LayoutItem("emitter_offsetY", offsetY) };
      return this.createLayout(layoutItems);
    }

    @Override
    protected void setupChangedListeners() {
      setup(comboBoxAlign, MapObjectProperty.Emitter.ORIGIN_ALIGN);
      setup(comboBoxValign, MapObjectProperty.Emitter.ORIGIN_VALIGN);

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

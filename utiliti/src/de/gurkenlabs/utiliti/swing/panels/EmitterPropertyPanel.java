package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
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
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.swing.Icons;
import de.gurkenlabs.utiliti.swing.panels.EmitterPanel.EmitterPropertyGroup;

@SuppressWarnings("serial")
public abstract class EmitterPropertyPanel extends PropertyPanel {

  protected transient Emitter emitter;

  private EmitterPropertyPanel() {
    super();
    this.setBorder(new EmptyBorder(0, 4, 0, 0));
  }

  public static EmitterPropertyPanel getEmitterPropertyPanel(EmitterPropertyGroup category) {
    switch (category) {
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
    case MOTION:
      return new ParticleMotionPanel();
    default:
      return null;
    }
  };

  protected abstract LayoutManager createLayout();

  protected abstract void setupChangedListeners();

  private static class EmissionPanel extends EmitterPropertyPanel {
    private JSpinner spawnRateSpinner;
    private JSpinner spawnAmountSpinner;
    private JSpinner updateDelaySpinner;
    private JSpinner durationSpinner;
    private JSpinner maxParticlesSpinner;
    private ParticleParameterModifier ttl;
    private JToggleButton btnPause;

    private EmissionPanel() {
      super();
      spawnRateSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_SPAWNRATE, 10, Integer.MAX_VALUE, STEP_COARSE));
      spawnAmountSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_SPAWNAMOUNT, 1, 500, STEP_ONE));
      updateDelaySpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_UPDATERATE, 0, Integer.MAX_VALUE, STEP_COARSE));
      durationSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_DURATION, 0, Integer.MAX_VALUE, STEP_SPARSE));
      maxParticlesSpinner = new JSpinner(new SpinnerNumberModel(EmitterData.DEFAULT_MAXPARTICLES, 1, Integer.MAX_VALUE, STEP_ONE));
      ttl = new ParticleParameterModifier(MapObjectProperty.Particle.TTL_MIN, MapObjectProperty.Particle.TTL_MIN, Integer.MIN_VALUE, Integer.MAX_VALUE, EmitterData.DEFAULT_MIN_PARTICLE_TTL, EmitterData.DEFAULT_MAX_PARTICLE_TTL, STEP_SPARSE);

      btnPause = new JToggleButton();
      btnPause.setSelected(true);
      btnPause.setIcon(Icons.PAUSE);
      btnPause.setSelectedIcon(Icons.PLAY);

      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      ttl.bind(mapObject);
    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("emitter_spawnrate", spawnRateSpinner), new LayoutItem("emitter_spawnamount", spawnAmountSpinner), new LayoutItem("emitter_updateDelay", updateDelaySpinner), new LayoutItem("emitter_duration", durationSpinner),
          new LayoutItem("emitter_maxparticles", maxParticlesSpinner), new LayoutItem("emitter_particleTTL", ttl) };
      return createLayout(layoutItems, btnPause);
    }

    @Override
    protected void clearControls() {
      spawnRateSpinner.setValue(EmitterData.DEFAULT_SPAWNRATE);
      spawnAmountSpinner.setValue(EmitterData.DEFAULT_SPAWNAMOUNT);
      updateDelaySpinner.setValue(EmitterData.DEFAULT_UPDATERATE);
      durationSpinner.setValue(EmitterData.DEFAULT_DURATION);
      maxParticlesSpinner.setValue(EmitterData.DEFAULT_MAXPARTICLES);
      emitter = null;
      btnPause.setSelected(true);
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      emitter = Game.world().environment().getEmitter(mapObject.getId());
      spawnRateSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNRATE));
      spawnAmountSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNAMOUNT));
      updateDelaySpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.UPDATERATE));
      durationSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.DURATION));
      maxParticlesSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.MAXPARTICLES));
      btnPause.setSelected(emitter != null ? emitter.isPaused() : true);
    }

    @Override
    protected void setupChangedListeners() {
      setup(spawnRateSpinner, MapObjectProperty.Emitter.SPAWNRATE);
      setup(spawnAmountSpinner, MapObjectProperty.Emitter.SPAWNAMOUNT);
      setup(updateDelaySpinner, MapObjectProperty.Emitter.UPDATERATE);
      setup(durationSpinner, MapObjectProperty.Emitter.DURATION);
      setup(maxParticlesSpinner, MapObjectProperty.Emitter.MAXPARTICLES);
      btnPause.addActionListener(a -> {
        if (emitter != null) {
          emitter.togglePaused();
        }
      });
    }

  }

  private static class ParticleStylePanel extends EmitterPropertyPanel {
    private JComboBox<ParticleType> comboBoxParticleType;
    private JToggleButton fade;
    private JToggleButton outlineOnly;
    private final EmitterColorPanel colorPanel;
    private final EmitterTextPanel textPanel;
    private final EmitterSpritePanel spritePanel;
    private JTabbedPane styleTabs;

    private ParticleStylePanel() {
      super();
      comboBoxParticleType = new JComboBox<>(new DefaultComboBoxModel<ParticleType>(ParticleType.values()));
      outlineOnly = new JToggleButton();
      outlineOnly.putClientProperty("JToggleButton.variant", DarkToggleButtonUI.VARIANT_SLIDER);
      fade = new JToggleButton();
      fade.putClientProperty("JToggleButton.variant", DarkToggleButtonUI.VARIANT_SLIDER);
      colorPanel = new EmitterColorPanel();
      textPanel = new EmitterTextPanel();
      spritePanel = new EmitterSpritePanel();
      styleTabs = new JTabbedPane();
      styleTabs.add(Resources.strings().get("particle_colors"), colorPanel);
      styleTabs.add(Resources.strings().get("particle_text"), textPanel);
      styleTabs.add(Resources.strings().get("particle_sprite"), spritePanel);

      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      colorPanel.bind(mapObject);
      textPanel.bind(mapObject);
      spritePanel.bind(mapObject);
    }

    @Override
    protected void clearControls() {
      comboBoxParticleType.setSelectedItem(EmitterData.DEFAULT_PARTICLE_TYPE);
      outlineOnly.setSelected(EmitterData.DEFAULT_OUTLINE_ONLY);
      fade.setSelected(EmitterData.DEFAULT_FADE);
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      emitter = Game.world().environment().getEmitter(mapObject.getId());
      comboBoxParticleType.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class, EmitterData.DEFAULT_PARTICLE_TYPE));
      outlineOnly.setSelected(mapObject.getBoolValue(MapObjectProperty.Particle.OUTLINEONLY, EmitterData.DEFAULT_OUTLINE_ONLY));
      fade.setSelected(mapObject.getBoolValue(MapObjectProperty.Particle.FADE, EmitterData.DEFAULT_FADE));
    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("emitter_particleType", comboBoxParticleType), new LayoutItem("particle_fade", fade), new LayoutItem("particle_outlineonly", outlineOnly) };
      return this.createLayout(layoutItems, styleTabs);
    }

    @Override
    protected void setupChangedListeners() {
      setup(comboBoxParticleType, MapObjectProperty.Emitter.PARTICLETYPE);
      comboBoxParticleType.addItemListener(e -> {
        switch ((ParticleType) e.getItem()) {
        case SPRITE:
          styleTabs.setEnabledAt(0, false);
          styleTabs.setEnabledAt(1, false);
          styleTabs.setEnabledAt(2, true);
          styleTabs.setSelectedIndex(2);
          break;
        case TEXT:
          styleTabs.setEnabledAt(0, true);
          styleTabs.setEnabledAt(1, true);
          styleTabs.setEnabledAt(2, false);
          styleTabs.setSelectedIndex(1);
          break;
        default:
          styleTabs.setEnabledAt(0, true);
          styleTabs.setEnabledAt(1, false);
          styleTabs.setEnabledAt(2, false);
          styleTabs.setSelectedIndex(0);
          break;
        }
      });
      setup(outlineOnly, MapObjectProperty.Particle.OUTLINEONLY);
      setup(fade, MapObjectProperty.Particle.FADE);
    }
  }

  private static class ParticleSizePanel extends EmitterPropertyPanel {
    private ParticleParameterModifier startWidth;
    private ParticleParameterModifier startHeight;
    private ParticleParameterModifier deltaWidth;
    private ParticleParameterModifier deltaHeight;

    private ParticleSizePanel() {
      super();
      startWidth = new ParticleParameterModifier(MapObjectProperty.Particle.STARTWIDTH_MIN, MapObjectProperty.Particle.STARTWIDTH_MAX, 0, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_WIDTH, EmitterData.DEFAULT_MAX_WIDTH, STEP_ONE);
      startHeight = new ParticleParameterModifier(MapObjectProperty.Particle.STARTHEIGHT_MIN, MapObjectProperty.Particle.STARTHEIGHT_MAX, 0, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_HEIGHT, EmitterData.DEFAULT_MAX_HEIGHT, STEP_ONE);
      deltaWidth = new ParticleParameterModifier(MapObjectProperty.Particle.DELTAWIDTH_MIN, MapObjectProperty.Particle.DELTAWIDTH_MAX, Short.MIN_VALUE, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_DELTA_WIDTH, EmitterData.DEFAULT_MAX_DELTA_WIDTH, STEP_FINEST);
      deltaHeight = new ParticleParameterModifier(MapObjectProperty.Particle.DELTAHEIGHT_MIN, MapObjectProperty.Particle.DELTAHEIGHT_MAX, Short.MIN_VALUE, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_DELTA_HEIGHT, EmitterData.DEFAULT_MAX_DELTA_HEIGHT, STEP_FINEST);
      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      startWidth.bind(mapObject);
      startHeight.bind(mapObject);
      deltaWidth.bind(mapObject);
      deltaHeight.bind(mapObject);
    }

    @Override
    protected void clearControls() {

    }

    @Override
    protected void setControlValues(IMapObject mapObject) {

    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("emitter_startWidth", startWidth), new LayoutItem("emitter_startHeight", startHeight), new LayoutItem("emitter_deltaWidth", deltaWidth), new LayoutItem("emitter_deltaHeight", deltaHeight) };
      return this.createLayout(layoutItems);
    }

    @Override
    protected void setupChangedListeners() {
      // TODO Auto-generated method stub

    }
  }

  private static class ParticleOriginPanel extends EmitterPropertyPanel {
    private JComboBox<Align> comboBoxAlign;
    private JComboBox<Valign> comboBoxValign;
    private ParticleParameterModifier offsetX;
    private ParticleParameterModifier offsetY;

    private ParticleOriginPanel() {
      super();
      comboBoxAlign = new JComboBox<>(new DefaultComboBoxModel<Align>(Align.values()));
      comboBoxValign = new JComboBox<>(new DefaultComboBoxModel<Valign>(Valign.values()));
      offsetX = new ParticleParameterModifier(MapObjectProperty.Particle.OFFSET_X_MIN, MapObjectProperty.Particle.OFFSET_X_MAX, Short.MIN_VALUE, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_OFFSET_X, EmitterData.DEFAULT_MAX_OFFSET_X, STEP_ONE);
      offsetY = new ParticleParameterModifier(MapObjectProperty.Particle.OFFSET_Y_MIN, MapObjectProperty.Particle.OFFSET_Y_MAX, Short.MIN_VALUE, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_OFFSET_Y, EmitterData.DEFAULT_MAX_OFFSET_Y, STEP_ONE);

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
      LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("emitter_originAlign", comboBoxAlign), new LayoutItem("emitter_originValign", comboBoxValign), new LayoutItem("offsetX", offsetX), new LayoutItem("offsetY", offsetY) };
      return this.createLayout(layoutItems);
    }

    @Override
    protected void setupChangedListeners() {
      setup(comboBoxAlign, MapObjectProperty.Emitter.ORIGIN_ALIGN);
      setup(comboBoxValign, MapObjectProperty.Emitter.ORIGIN_VALIGN);

    }
  }

  private static class ParticleMotionPanel extends EmitterPropertyPanel {
    private ParticleParameterModifier velocityX;
    private ParticleParameterModifier velocityY;
    private ParticleParameterModifier accelerationX;
    private ParticleParameterModifier accelerationY;

    private ParticleMotionPanel() {
      super();
      velocityX = new ParticleParameterModifier(MapObjectProperty.Particle.VELOCITY_X_MIN, MapObjectProperty.Particle.VELOCITY_X_MAX, Short.MIN_VALUE, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_VELOCITY_X, EmitterData.DEFAULT_MAX_DELTA_X, .01f);
      velocityY = new ParticleParameterModifier(MapObjectProperty.Particle.VELOCITY_Y_MIN, MapObjectProperty.Particle.VELOCITY_Y_MAX, Short.MIN_VALUE, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_VELOCITY_Y, EmitterData.DEFAULT_MAX_VELOCITY_Y, .01f);
      accelerationX = new ParticleParameterModifier(MapObjectProperty.Particle.ACCELERATION_X_MIN, MapObjectProperty.Particle.ACCELERATION_X_MAX, Short.MIN_VALUE, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_ACCELERATION_X, EmitterData.DEFAULT_MAX_ACCELERATION_X, .01f);
      accelerationY = new ParticleParameterModifier(MapObjectProperty.Particle.ACCELERATION_Y_MIN, MapObjectProperty.Particle.ACCELERATION_Y_MAX, Short.MIN_VALUE, Short.MAX_VALUE, EmitterData.DEFAULT_MIN_ACCELERATION_Y, EmitterData.DEFAULT_MAX_ACCELERATION_Y, .01f);
      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      velocityX.bind(mapObject);
      velocityY.bind(mapObject);
      accelerationX.bind(mapObject);
      accelerationY.bind(mapObject);
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
      LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("emitter_velocityX", velocityX), new LayoutItem("emitter_velocityY", velocityY), new LayoutItem("emitter_accelerationX", accelerationX), new LayoutItem("emitter_accelerationY", accelerationY) };
      return this.createLayout(layoutItems);
    }

    @Override
    protected void setupChangedListeners() {
      // TODO Auto-generated method stub

    }
  }

  private static class ParticleCollisionPanel extends EmitterPropertyPanel {
    JComboBox<Collision> collisionType;
    JToggleButton fadeOnCollision;

    private ParticleCollisionPanel() {
      super();
      collisionType = new JComboBox<Collision>(new DefaultComboBoxModel<Collision>(Collision.values()));
      fadeOnCollision = new JToggleButton();
      fadeOnCollision.putClientProperty("JToggleButton.variant", DarkToggleButtonUI.VARIANT_SLIDER);
      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    protected void clearControls() {
      collisionType.setSelectedItem(EmitterData.DEFAULT_COLLISION);
      fadeOnCollision.setSelected(EmitterData.DEFAULT_FADE_ON_COLLISION);
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      collisionType.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.COLLISION_TYPE, Collision.class, EmitterData.DEFAULT_COLLISION));
      fadeOnCollision.setSelected(mapObject.getBoolValue(MapObjectProperty.Particle.FADEONCOLLISION, EmitterData.DEFAULT_FADE_ON_COLLISION));

    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("collisionType", collisionType), new LayoutItem("particle_fadeOnCollision", fadeOnCollision) };
      return this.createLayout(layoutItems);
    }

    @Override
    protected void setupChangedListeners() {
      setup(collisionType, MapObjectProperty.COLLISION_TYPE);
      setup(fadeOnCollision, MapObjectProperty.Particle.FADEONCOLLISION);
    }
  }

}

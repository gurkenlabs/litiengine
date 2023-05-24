package de.gurkenlabs.utiliti.swing.panels.emission;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.utiliti.swing.Icons;
import de.gurkenlabs.utiliti.swing.panels.DualSpinner;
import java.awt.LayoutManager;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

/**
 * A panel to display and modify emitter properties related to particle emission. This panel
 * inherits from the EmitterPropertyPanel class.
 */
public class EmissionPanel extends EmitterPropertyPanel {

  private final JSpinner spawnRateSpinner;
  private final JSpinner spawnAmountSpinner;
  private final JSpinner updateDelaySpinner;
  private final JSpinner durationSpinner;
  private final JSpinner maxParticlesSpinner;

  private final SpinnerNumberModel spawnRateSpinnerModel;
  private final SpinnerNumberModel spawnAmountSpinnerModel;
  private final SpinnerNumberModel updateDelaySpinnerModel;
  private final SpinnerNumberModel durationSpinnerModel;
  private final SpinnerNumberModel maxParticlesSpinnerModel;
  private final DualSpinner ttl;
  private final JToggleButton btnPause;

  /**
   * Constructs a new EmissionPanel object with default values for the emitter properties.
   */
  protected EmissionPanel() {
    super();
    this.spawnRateSpinnerModel = new SpinnerNumberModel(EmitterData.DEFAULT_SPAWNRATE, 10,
      Integer.MAX_VALUE, STEP_COARSE);
    this.spawnRateSpinner = new JSpinner(spawnRateSpinnerModel);
    this.spawnAmountSpinnerModel = new SpinnerNumberModel(EmitterData.DEFAULT_SPAWNAMOUNT, 1, 500,
      STEP_ONE);
    this.spawnAmountSpinner =
      new JSpinner(spawnAmountSpinnerModel);
    this.updateDelaySpinnerModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_UPDATERATE, 0, Integer.MAX_VALUE, STEP_COARSE);
    this.updateDelaySpinner =
      new JSpinner(updateDelaySpinnerModel);
    this.durationSpinnerModel =
      new SpinnerNumberModel(
        EmitterData.DEFAULT_DURATION, 0, Integer.MAX_VALUE, STEP_SPARSE);
    this.durationSpinner = new JSpinner(durationSpinnerModel);
    this.maxParticlesSpinnerModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MAXPARTICLES, 1, Integer.MAX_VALUE, STEP_ONE);
    this.maxParticlesSpinner = new JSpinner(maxParticlesSpinnerModel);
    SpinnerNumberModel minTTLModel = new SpinnerNumberModel(EmitterData.DEFAULT_MIN_PARTICLE_TTL,
      0, Long.MAX_VALUE, STEP_SPARSE);
    SpinnerNumberModel maxTTLModel = new SpinnerNumberModel(EmitterData.DEFAULT_MAX_PARTICLE_TTL,
      0, Long.MAX_VALUE, STEP_SPARSE);
    this.ttl = new DualSpinner(Particle.TTL_MIN, Particle.TTL_MAX, minTTLModel, maxTTLModel);

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
    LayoutItem[] layoutItems =
      new LayoutItem[]{
        new LayoutItem("emitter_spawnrate", spawnRateSpinner),
        new LayoutItem("emitter_spawnamount", spawnAmountSpinner),
        new LayoutItem("emitter_updateDelay", updateDelaySpinner),
        new LayoutItem("emitter_duration", durationSpinner),
        new LayoutItem("emitter_maxparticles", maxParticlesSpinner),
        new LayoutItem("emitter_particleTTL", ttl)
      };
    return createLayout(layoutItems, btnPause);
  }

  @Override
  protected void clearControls() {
    spawnRateSpinner.setValue(EmitterData.DEFAULT_SPAWNRATE);
    spawnAmountSpinner.setValue(EmitterData.DEFAULT_SPAWNAMOUNT);
    updateDelaySpinner.setValue(EmitterData.DEFAULT_UPDATERATE);
    durationSpinner.setValue(EmitterData.DEFAULT_DURATION);
    maxParticlesSpinner.setValue(EmitterData.DEFAULT_MAXPARTICLES);
    btnPause.setSelected(true);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    spawnRateSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNRATE));
    spawnAmountSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNAMOUNT));
    updateDelaySpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.UPDATERATE));
    durationSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.DURATION));
    maxParticlesSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.MAXPARTICLES));
    btnPause.setSelected(getEmitter() == null || getEmitter().isPaused());
  }

  @Override
  protected void setupChangedListeners() {
    setup(spawnRateSpinnerModel, MapObjectProperty.Emitter.SPAWNRATE);
    setup(spawnAmountSpinnerModel, MapObjectProperty.Emitter.SPAWNAMOUNT);
    setup(updateDelaySpinnerModel, MapObjectProperty.Emitter.UPDATERATE);
    setup(durationSpinnerModel, MapObjectProperty.Emitter.DURATION);
    setup(maxParticlesSpinnerModel, MapObjectProperty.Emitter.MAXPARTICLES);
    btnPause.addActionListener(
      a -> {
        if (getEmitter() != null) {
          getEmitter().togglePaused();
        }
      });
  }
}

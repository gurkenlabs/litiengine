package de.gurkenlabs.utiliti.swing.panels.emission;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.utiliti.swing.panels.DualSpinner;
import java.awt.LayoutManager;
import javax.swing.SpinnerNumberModel;

public class ParticleRotationPanel extends EmitterPropertyPanel {

  private final DualSpinner startAngle;
  private final DualSpinner deltaAngle;

  protected  ParticleRotationPanel() {
    super();
    SpinnerNumberModel minStartAngleModel = new SpinnerNumberModel(EmitterData.DEFAULT_MIN_ANGLE,
      -360, 360,
      STEP_ONE);
    SpinnerNumberModel maxStartAngleModel = new SpinnerNumberModel(EmitterData.DEFAULT_MAX_ANGLE,
      -360, 360,
      STEP_ONE);
    startAngle = new DualSpinner(MapObjectProperty.Particle.ANGLE_MIN,
      MapObjectProperty.Particle.ANGLE_MAX, minStartAngleModel, maxStartAngleModel);
    SpinnerNumberModel minDeltaAngleModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MIN_DELTA_ANGLE, -360,
      360, STEP_FINE);
    SpinnerNumberModel maxDeltaAngleModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MAX_DELTA_ANGLE, -360,
      360, STEP_FINE);
    deltaAngle = new DualSpinner(MapObjectProperty.Particle.DELTA_ANGLE_MIN,
      MapObjectProperty.Particle.DELTA_ANGLE_MAX, minDeltaAngleModel, maxDeltaAngleModel);

    setLayout(createLayout());
    setupChangedListeners();
  }

  @Override
  public void bind(IMapObject mapObject) {
    super.bind(mapObject);
    startAngle.bind(mapObject);
    deltaAngle.bind(mapObject);
  }

  @Override
  protected LayoutManager createLayout() {
    LayoutItem[] layoutItems =
      new LayoutItem[]{
        new LayoutItem("particle_startAngle", startAngle),
        new LayoutItem("particle_deltaAngle", deltaAngle)
      };
    return this.createLayout(layoutItems);
  }

  @Override
  protected void setupChangedListeners() {

  }
}

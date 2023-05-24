package de.gurkenlabs.utiliti.swing.panels.emission;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.utiliti.swing.panels.DualSpinner;
import java.awt.LayoutManager;
import javax.swing.SpinnerNumberModel;

public class ParticleMotionPanel extends EmitterPropertyPanel {

  private final DualSpinner velocityX;
  private final DualSpinner velocityY;
  private final DualSpinner accelerationX;
  private final DualSpinner accelerationY;

  protected ParticleMotionPanel() {
    super();
    SpinnerNumberModel minVelocityXModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MIN_VELOCITY_X, Short.MIN_VALUE, Short.MAX_VALUE, STEP_FINEST);
    SpinnerNumberModel maxVelocityXModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MAX_VELOCITY_X, Short.MIN_VALUE, Short.MAX_VALUE, STEP_FINEST);
    this.velocityX =
      new DualSpinner(
        MapObjectProperty.Particle.VELOCITY_X_MIN,
        MapObjectProperty.Particle.VELOCITY_X_MAX,
        minVelocityXModel, maxVelocityXModel);
    SpinnerNumberModel minVelocityYModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MIN_VELOCITY_Y, Short.MIN_VALUE, Short.MAX_VALUE, STEP_FINEST);
    SpinnerNumberModel maxVelocityYModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MAX_VELOCITY_Y, Short.MIN_VALUE, Short.MAX_VALUE, STEP_FINEST);
    this.velocityY =
      new DualSpinner(
        MapObjectProperty.Particle.VELOCITY_Y_MIN,
        MapObjectProperty.Particle.VELOCITY_Y_MAX,
        minVelocityYModel, maxVelocityYModel);
    SpinnerNumberModel minAccelerationXModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MIN_ACCELERATION_X, Short.MIN_VALUE, Short.MAX_VALUE, STEP_FINEST);
    SpinnerNumberModel maxAccelerationXModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MAX_ACCELERATION_X, Short.MIN_VALUE, Short.MAX_VALUE, STEP_FINEST);
    this.accelerationX =
      new DualSpinner(MapObjectProperty.Particle.ACCELERATION_X_MIN,
        MapObjectProperty.Particle.ACCELERATION_X_MAX, minAccelerationXModel,
        maxAccelerationXModel);
    SpinnerNumberModel minAccelerationYModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MIN_ACCELERATION_Y, Short.MIN_VALUE, Short.MAX_VALUE, STEP_FINEST);
    SpinnerNumberModel maxAccelerationYModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MAX_ACCELERATION_Y, Short.MIN_VALUE, Short.MAX_VALUE, STEP_FINEST);
    this.accelerationY =
      new DualSpinner(MapObjectProperty.Particle.ACCELERATION_Y_MIN,
        MapObjectProperty.Particle.ACCELERATION_Y_MAX, minAccelerationYModel,
        maxAccelerationYModel);
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
  protected LayoutManager createLayout() {
    LayoutItem[] layoutItems =
      new LayoutItem[]{
        new LayoutItem("emitter_velocityX", velocityX),
        new LayoutItem("emitter_velocityY", velocityY),
        new LayoutItem("emitter_accelerationX", accelerationX),
        new LayoutItem("emitter_accelerationY", accelerationY)
      };
    return this.createLayout(layoutItems);
  }

  @Override
  protected void setupChangedListeners() {
    // do nothing
  }
}

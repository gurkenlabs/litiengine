package de.gurkenlabs.utiliti.swing.panels.emission;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.utiliti.swing.panels.DualSpinner;
import java.awt.LayoutManager;
import javax.swing.SpinnerNumberModel;

public class ParticleSizePanel extends EmitterPropertyPanel {

  private final DualSpinner startWidth;
  private final DualSpinner startHeight;
  private final DualSpinner deltaWidth;
  private final DualSpinner deltaHeight;

  protected  ParticleSizePanel() {
    super();
    SpinnerNumberModel minStartWidthModel = new SpinnerNumberModel(EmitterData.DEFAULT_MIN_WIDTH,
      0,
      Short.MAX_VALUE, STEP_FINE);
    SpinnerNumberModel maxStartWidthModel = new SpinnerNumberModel(EmitterData.DEFAULT_MAX_WIDTH,
      0,
      Short.MAX_VALUE, STEP_FINE);
    this.startWidth =
      new DualSpinner(Particle.STARTWIDTH_MIN, Particle.STARTWIDTH_MAX, minStartWidthModel,
        maxStartWidthModel);
    SpinnerNumberModel minStartHeightModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MIN_HEIGHT, 0,
      Short.MAX_VALUE, STEP_FINE);
    SpinnerNumberModel maxStartHeightModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MAX_HEIGHT, 0,
      Short.MAX_VALUE, STEP_FINE);
    this.startHeight =
      new DualSpinner(
        MapObjectProperty.Particle.STARTHEIGHT_MIN,
        MapObjectProperty.Particle.STARTHEIGHT_MAX,
        minStartHeightModel, maxStartHeightModel);
    SpinnerNumberModel minDeltaWidthModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MIN_DELTA_WIDTH,
      Short.MIN_VALUE,
      Short.MAX_VALUE, STEP_FINEST);
    SpinnerNumberModel maxDeltaWidthModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MAX_DELTA_WIDTH,
      Short.MIN_VALUE,
      Short.MAX_VALUE, STEP_FINEST);
    this.deltaWidth =
      new DualSpinner(MapObjectProperty.Particle.DELTAWIDTH_MIN,
        MapObjectProperty.Particle.DELTAWIDTH_MAX, minDeltaWidthModel, maxDeltaWidthModel);
    SpinnerNumberModel minDeltaHeightModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MIN_DELTA_HEIGHT,
      Short.MIN_VALUE,
      Short.MAX_VALUE, STEP_FINEST);
    SpinnerNumberModel maxDeltaHeightModel = new SpinnerNumberModel(
      EmitterData.DEFAULT_MAX_DELTA_HEIGHT,
      Short.MIN_VALUE,
      Short.MAX_VALUE, STEP_FINEST);
    this.deltaHeight = new DualSpinner(MapObjectProperty.Particle.DELTAHEIGHT_MIN,
      MapObjectProperty.Particle.DELTAHEIGHT_MAX, minDeltaHeightModel, maxDeltaHeightModel);
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
  protected LayoutManager createLayout() {
    LayoutItem[] layoutItems =
      new LayoutItem[]{
        new LayoutItem("emitter_startWidth", startWidth),
        new LayoutItem("emitter_startHeight", startHeight),
        new LayoutItem("emitter_deltaWidth", deltaWidth),
        new LayoutItem("emitter_deltaHeight", deltaHeight)
      };
    return this.createLayout(layoutItems);
  }

  @Override
  protected void setupChangedListeners() {

  }
}

package de.gurkenlabs.utiliti.swing.panels.emission;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.utiliti.swing.panels.DualSpinner;
import java.awt.LayoutManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SpinnerNumberModel;

public class ParticleOriginPanel extends EmitterPropertyPanel {

  private final JComboBox<Align> comboBoxAlign;
  private final JComboBox<Valign> comboBoxValign;
  private final DualSpinner offsetX;
  private final DualSpinner offsetY;

  protected  ParticleOriginPanel() {
    super();
    comboBoxAlign = new JComboBox<>(new DefaultComboBoxModel<>(Align.values()));
    comboBoxValign = new JComboBox<>(new DefaultComboBoxModel<>(Valign.values()));
    SpinnerNumberModel minOffsetXModel = new SpinnerNumberModel(EmitterData.DEFAULT_MIN_OFFSET_X,
      Short.MIN_VALUE, Short.MAX_VALUE, STEP_ONE);
    SpinnerNumberModel maxOffsetXModel = new SpinnerNumberModel(EmitterData.DEFAULT_MAX_OFFSET_X,
      Short.MIN_VALUE, Short.MAX_VALUE, STEP_ONE);
    this.offsetX = new DualSpinner(Particle.OFFSET_X_MIN, Particle.OFFSET_X_MAX, minOffsetXModel,
      maxOffsetXModel);
    SpinnerNumberModel minOffsetYModel = new SpinnerNumberModel(EmitterData.DEFAULT_MIN_OFFSET_Y,
      Short.MIN_VALUE, Short.MAX_VALUE, STEP_ONE);
    SpinnerNumberModel maxOffsetYModel = new SpinnerNumberModel(EmitterData.DEFAULT_MAX_OFFSET_Y,
      Short.MIN_VALUE, Short.MAX_VALUE, STEP_ONE);
    this.offsetY = new DualSpinner(Particle.OFFSET_Y_MIN, Particle.OFFSET_Y_MAX, minOffsetYModel,
      maxOffsetYModel);

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
    super.clearControls();
    comboBoxAlign.setSelectedItem(EmitterData.DEFAULT_ORIGIN_ALIGN);
    comboBoxValign.setSelectedItem(EmitterData.DEFAULT_ORIGIN_VALIGN);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    super.setControlValues(mapObject);
    comboBoxAlign.setSelectedItem(
      mapObject.getEnumValue(
        MapObjectProperty.Emitter.ORIGIN_ALIGN,
        Align.class,
        EmitterData.DEFAULT_ORIGIN_ALIGN));
    comboBoxValign.setSelectedItem(
      mapObject.getEnumValue(
        MapObjectProperty.Emitter.ORIGIN_VALIGN,
        Valign.class,
        EmitterData.DEFAULT_ORIGIN_VALIGN));
  }

  @Override
  protected LayoutManager createLayout() {
    LayoutItem[] layoutItems =
      new LayoutItem[]{
        new LayoutItem("emitter_originAlign", comboBoxAlign),
        new LayoutItem("emitter_originValign", comboBoxValign),
        new LayoutItem("offsetX", offsetX),
        new LayoutItem("offsetY", offsetY)
      };
    return this.createLayout(layoutItems);
  }

  @Override
  protected void setupChangedListeners() {
    setup(comboBoxAlign, MapObjectProperty.Emitter.ORIGIN_ALIGN);
    setup(comboBoxValign, MapObjectProperty.Emitter.ORIGIN_VALIGN);
  }
}

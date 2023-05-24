package de.gurkenlabs.utiliti.swing.panels.emission;

import com.github.weisj.darklaf.ui.togglebutton.ToggleButtonConstants;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.physics.Collision;
import java.awt.LayoutManager;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;

public class ParticleCollisionPanel extends EmitterPropertyPanel {

  JComboBox<Collision> collisionType;
  JToggleButton fadeOnCollision;

  protected ParticleCollisionPanel() {
    super();
    collisionType = new JComboBox<>(Collision.values());
    fadeOnCollision = new JToggleButton();
    fadeOnCollision.putClientProperty(
      ToggleButtonConstants.KEY_VARIANT, ToggleButtonConstants.VARIANT_SLIDER);
    setLayout(createLayout());
    setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    super.clearControls();
    collisionType.setSelectedItem(EmitterData.DEFAULT_COLLISION);
    fadeOnCollision.setSelected(EmitterData.DEFAULT_FADE_ON_COLLISION);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    super.setControlValues(mapObject);
    collisionType.setSelectedItem(
      mapObject.getEnumValue(
        MapObjectProperty.COLLISION_TYPE, Collision.class, EmitterData.DEFAULT_COLLISION));
    fadeOnCollision.setSelected(
      mapObject.getBoolValue(
        MapObjectProperty.Particle.FADEONCOLLISION, EmitterData.DEFAULT_FADE_ON_COLLISION));
  }

  @Override
  protected LayoutManager createLayout() {
    LayoutItem[] layoutItems =
      new LayoutItem[]{
        new LayoutItem("collisionType", collisionType),
        new LayoutItem("particle_fadeOnCollision", fadeOnCollision)
      };
    return this.createLayout(layoutItems);
  }

  @Override
  protected void setupChangedListeners() {
    setup(collisionType, MapObjectProperty.COLLISION_TYPE);
    setup(fadeOnCollision, MapObjectProperty.Particle.FADEONCOLLISION);
  }
}

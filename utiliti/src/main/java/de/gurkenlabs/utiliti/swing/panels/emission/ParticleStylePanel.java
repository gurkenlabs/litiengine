package de.gurkenlabs.utiliti.swing.panels.emission;

import com.github.weisj.darklaf.ui.togglebutton.ToggleButtonConstants;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.LayoutManager;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;

public class ParticleStylePanel extends EmitterPropertyPanel {

  private final JComboBox<ParticleType> comboBoxParticleType;
  private final JToggleButton fade;
  private final JToggleButton outlineOnly;
  private final JToggleButton antiAliasing;
  private final EmitterColorPanel colorPanel;
  private final EmitterTextPanel textPanel;
  private final EmitterSpritePanel spritePanel;
  private final JTabbedPane styleTabs;

  protected ParticleStylePanel() {
    super();
    comboBoxParticleType = new JComboBox<>(new DefaultComboBoxModel<>(ParticleType.values()));
    fade = new JToggleButton();
    fade.putClientProperty(
      ToggleButtonConstants.KEY_VARIANT, ToggleButtonConstants.VARIANT_SLIDER);
    outlineOnly = new JToggleButton();
    outlineOnly.putClientProperty(
      ToggleButtonConstants.KEY_VARIANT, ToggleButtonConstants.VARIANT_SLIDER);
    antiAliasing = new JToggleButton();
    antiAliasing.putClientProperty(
      ToggleButtonConstants.KEY_VARIANT, ToggleButtonConstants.VARIANT_SLIDER);
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
    fade.setSelected(EmitterData.DEFAULT_FADE);
    outlineOnly.setSelected(EmitterData.DEFAULT_OUTLINE_ONLY);
    antiAliasing.setSelected(EmitterData.DEFAULT_ANTIALIASING);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    super.setControlValues(mapObject);
    comboBoxParticleType.setSelectedItem(
      mapObject.getEnumValue(
        MapObjectProperty.Emitter.PARTICLETYPE,
        ParticleType.class,
        EmitterData.DEFAULT_PARTICLE_TYPE));
    updateTabSelection();
    fade.setSelected(
      mapObject.getBoolValue(MapObjectProperty.Particle.FADE, EmitterData.DEFAULT_FADE));
    outlineOnly.setSelected(
      mapObject.getBoolValue(
        MapObjectProperty.Particle.OUTLINEONLY, EmitterData.DEFAULT_OUTLINE_ONLY));
    antiAliasing.setSelected(
      mapObject.getBoolValue(
        MapObjectProperty.Particle.ANTIALIASING, EmitterData.DEFAULT_ANTIALIASING));
  }

  @Override
  protected LayoutManager createLayout() {
    LayoutItem[] layoutItems =
      new LayoutItem[]{
        new LayoutItem("emitter_particleType", comboBoxParticleType),
        new LayoutItem("particle_fade", fade),
        new LayoutItem("particle_outlineonly", outlineOnly),
        new LayoutItem("particle_antiAliasing", antiAliasing)
      };
    return this.createLayout(layoutItems, styleTabs);
  }

  @Override
  protected void setupChangedListeners() {
    setup(comboBoxParticleType, MapObjectProperty.Emitter.PARTICLETYPE);
    comboBoxParticleType.addItemListener(e -> updateTabSelection());
    setup(fade, MapObjectProperty.Particle.FADE);
    setup(outlineOnly, MapObjectProperty.Particle.OUTLINEONLY);
    setup(antiAliasing, MapObjectProperty.Particle.ANTIALIASING);
  }

  private void updateTabSelection() {
    switch ((ParticleType) Objects.requireNonNull(comboBoxParticleType.getSelectedItem())) {
      case SPRITE -> {
        styleTabs.setEnabledAt(0, false);
        styleTabs.setEnabledAt(1, false);
        styleTabs.setEnabledAt(2, true);
        styleTabs.setSelectedIndex(2);
      }
      case TEXT -> {
        styleTabs.setEnabledAt(0, true);
        styleTabs.setEnabledAt(1, true);
        styleTabs.setEnabledAt(2, false);
        styleTabs.setSelectedIndex(1);
      }
      default -> {
        styleTabs.setEnabledAt(0, true);
        styleTabs.setEnabledAt(1, false);
        styleTabs.setEnabledAt(2, false);
        styleTabs.setSelectedIndex(0);
      }
    }
  }
}

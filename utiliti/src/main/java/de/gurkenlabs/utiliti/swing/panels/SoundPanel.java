package de.gurkenlabs.utiliti.swing.panels;

import com.github.weisj.darklaf.components.VolumeSlider;
import com.github.weisj.darklaf.ui.togglebutton.ToggleButtonConstants;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.ResourcesContainerListener;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.utiliti.swing.Icons;
import java.awt.LayoutManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

public class SoundPanel extends PropertyPanel {
  private final VolumeSlider volume;
  private final JSpinner range;
  private final JToggleButton loop;
  private final JComboBox<Sound> soundResource;
  private final JButton play;

  public SoundPanel() {
    super("panel_sound", Icons.SOUND);
    this.volume = new VolumeSlider();
    this.volume.setShowVolumeIcon(true);
    this.range = new JSpinner(new SpinnerNumberModel(Game.audio().getMaxDistance(), 0, Integer.MAX_VALUE, 2));
    this.loop = new JToggleButton();
    this.loop.putClientProperty(ToggleButtonConstants.KEY_VARIANT, ToggleButtonConstants.VARIANT_SLIDER);
    this.soundResource = new JComboBox<>();
    this.play = new JButton(Resources.strings().get("panel_play_sound"), Icons.PLAY);
    this.play.addActionListener(
      l -> Game.audio().playSound((Sound) this.soundResource.getSelectedItem(), false, (int) this.range.getValue(), this.volume.getValue() / 50f));
    setLayout(this.createLayout());
    this.setupChangedListeners();

    Resources.sounds().addContainerListener(new ResourcesContainerListener<>() {
      @Override public void added(String resourceName, Sound resource) {
        updateModel();
      }

      @Override public void removed(String resourceName, Sound resource) {
        updateModel();
      }

      @Override public void cleared() {
        updateModel();
      }
    });
  }

  @Override protected void clearControls() {
    this.volume.setValue((int) Math.clamp(Game.config().sound().getSoundVolume() * 50, 0, 100));
    this.range.setValue(Game.audio().getMaxDistance());
    this.loop.setSelected(false);
    updateModel();
  }

  @Override protected void setControlValues(IMapObject mapObject) {
    this.volume.setValue((int) Math.clamp(mapObject.getFloatValue(MapObjectProperty.SOUND_VOLUME, 0) * 50, 0, 100));
    this.range.setValue(mapObject.getIntValue(MapObjectProperty.SOUND_RANGE, 0));
    this.loop.setSelected(mapObject.getBoolValue(MapObjectProperty.SOUND_LOOP, false));
    updateModel();
    if (!mapObject.hasCustomProperty(MapObjectProperty.SOUND_NAME)) {
      return;
    }

    var sound = mapObject.getStringValue(MapObjectProperty.SOUND_NAME);
    if (!Resources.sounds().contains(sound)) {
      return;
    }

    this.soundResource.setSelectedItem(Resources.sounds().get(sound));
  }

  public void updateModel() {
    this.soundResource.setModel(new DefaultComboBoxModel<>(Resources.sounds().getAll().toArray(new Sound[0])));
  }

  private void setupChangedListeners() {
    this.setup(this.volume, MapObjectProperty.SOUND_VOLUME, 1 / 50f);
    this.setup(this.range, MapObjectProperty.SOUND_RANGE);
    this.setup(this.loop, MapObjectProperty.SOUND_LOOP);
    this.setup(this.soundResource, MapObjectProperty.SOUND_NAME);
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem[] {new LayoutItem("panel_soundname", this.soundResource), new LayoutItem("panel_range", this.range),
      new LayoutItem("panel_volume", this.volume), new LayoutItem("panel_loop", this.loop),};

    return this.createLayout(layoutItems, this.play);
  }
}

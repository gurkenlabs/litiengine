package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

import com.github.weisj.darklaf.components.VolumeSlider;
import com.github.weisj.darklaf.ui.togglebutton.ToggleButtonConstants;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.utiliti.swing.Icons;

@SuppressWarnings("serial")
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
    this.range = new JSpinner(new SpinnerNumberModel((int) Game.audio().getMaxDistance(), 0, Integer.MAX_VALUE, 2));
    this.loop = new JToggleButton();
    this.loop.putClientProperty(ToggleButtonConstants.KEY_VARIANT, ToggleButtonConstants.VARIANT_SLIDER);
    this.soundResource = new JComboBox<>();
    this.play = new JButton(Resources.strings().get("panel_play_sound"), Icons.PLAY);
    this.play.addActionListener(l -> Game.audio().playSound((Sound) this.soundResource.getSelectedItem()));
    setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.volume.setValue((int) MathUtilities.clamp(Game.config().sound().getSoundVolume() * 100, 0, 100));
    this.range.setValue((int) Game.audio().getMaxDistance());
    this.loop.setSelected(false);
    updateModel();
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.volume.setValue((int) MathUtilities.clamp(mapObject.getIntValue(MapObjectProperty.SOUND_VOLUME), 0, 100));
    this.range.setValue(mapObject.getIntValue(MapObjectProperty.SOUND_RANGE));
    this.loop.setSelected(mapObject.getBoolValue(MapObjectProperty.SOUND_LOOP));
    updateModel();
    if (mapObject.getStringValue(MapObjectProperty.SOUND_NAME) == null || !Resources.sounds().contains(mapObject.getStringValue(MapObjectProperty.SOUND_NAME))) {
      return;
    }
    this.soundResource.setSelectedItem(Resources.sounds().get(mapObject.getStringValue(MapObjectProperty.SOUND_NAME)));
  }

  public void updateModel() {
    this.soundResource.setModel(new DefaultComboBoxModel<>(Resources.sounds().getAll().toArray(new Sound[0])));
  }

  private void setupChangedListeners() {
    this.volume.addChangeListener(l -> Game.config().sound().setSoundVolume(this.volume.getValue()));
    this.setup(this.range, MapObjectProperty.SOUND_RANGE);
    this.setup(this.loop, MapObjectProperty.SOUND_LOOP);
    this.setup(this.soundResource, MapObjectProperty.SOUND_NAME);
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem[] { new LayoutItem("panel_soundname", this.soundResource), new LayoutItem("panel_range", this.range), new LayoutItem("panel_volume", this.volume), new LayoutItem("panel_loop", this.loop), };

    return this.createLayout(layoutItems, this.play);
  }
}

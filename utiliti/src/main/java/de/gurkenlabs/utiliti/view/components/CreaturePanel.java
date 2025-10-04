package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.CreatureAnimationState;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.renderers.LabelListCellRenderer;
import java.awt.LayoutManager;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

public class CreaturePanel extends PropertyPanel {
  private final JComboBox<JLabel> comboBoxSpriteSheets;
  private final JComboBox<Direction> comboBoxDirection;
  private final JCheckBox checkBoxScale;
  private boolean creaturesLoaded; // mirrors PropPanel.propsLoaded behavior

  public CreaturePanel() {
    super("panel_creature", Icons.CREATURE_16);
    this.comboBoxSpriteSheets = new JComboBox<>();
    this.comboBoxSpriteSheets.setRenderer(new LabelListCellRenderer());

    this.comboBoxDirection = new JComboBox<>();
    this.comboBoxDirection.setModel(new DefaultComboBoxModel<>(Direction.values()));
    this.checkBoxScale = new JCheckBox(Resources.strings().get("panel_stretch_sprite"));

    setLayout(this.createLayout());
    setupChangedListeners();

    // if images are cleared (e.g. resource reload), repopulate on next bind
    Resources.images().addClearedListener(() -> this.creaturesLoaded = false);
  }

  public static String getCreatureSpriteName(String name) {
    if (Arrays.stream(CreatureAnimationState.values()).anyMatch(state -> name.contains(state.spriteString()))) {
      return name.split("-")[0];
    }
    return null;
  }

  @Override
  public void bind(IMapObject mapObject) {
    this.loadAvailableCreatureSprites();
    if (mapObject != null) {
      setControlValues(mapObject);
    }
    super.bind(mapObject);
  }

  @Override
  protected void clearControls() {
    this.comboBoxSpriteSheets.setSelectedItem(null);
    this.comboBoxDirection.setSelectedItem(Direction.UNDEFINED);
    this.checkBoxScale.setSelected(false);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    // first try regular selection by stored property (base name expected)
    selectSpriteSheet(this.comboBoxSpriteSheets, mapObject);

    // fallback: if nothing selected and a full spritesheet name was stored earlier, try its base
    if (this.comboBoxSpriteSheets.getSelectedItem() == null) {
      String stored = mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME, null);
      if (stored != null && stored.contains("-")) {
        String base = stored.split("-")[0];
        for (int i = 0; i < this.comboBoxSpriteSheets.getItemCount(); i++) {
          JLabel lbl = this.comboBoxSpriteSheets.getItemAt(i);
          if (lbl != null && lbl.getText().equals(base)) {
            this.comboBoxSpriteSheets.setSelectedItem(lbl);
            break;
          }
        }
      }
    }

    this.comboBoxDirection.setSelectedItem(
      mapObject.getEnumValue(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.UNDEFINED));
    this.checkBoxScale.setSelected(mapObject.getBoolValue(MapObjectProperty.SCALE_SPRITE, false));
  }

  private void setupChangedListeners() {
    // use the standard helper just like PropPanel (stores label text/base creature name)
    setupL(this.comboBoxSpriteSheets, MapObjectProperty.SPRITESHEETNAME);
    setup(this.comboBoxDirection, MapObjectProperty.SPAWN_DIRECTION);
    setup(this.checkBoxScale, MapObjectProperty.SCALE_SPRITE);
  }

  private void loadAvailableCreatureSprites() {
    if (this.creaturesLoaded) {
      return;
    }

    Map<String, String> m = new TreeMap<>();
    for (Spritesheet s : Resources.spritesheets().getAll()) {
      String creatureSpriteName = getCreatureSpriteName(s.getName());
      if (creatureSpriteName != null) {
        m.putIfAbsent(creatureSpriteName, s.getName());
      }
    }

    populateComboBoxWithSprites(this.comboBoxSpriteSheets, m);
    this.creaturesLoaded = true;
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem[] {
      new LayoutItem("panel_sprite", this.comboBoxSpriteSheets),
      new LayoutItem("panel_direction", this.comboBoxDirection),
    };
    return this.createLayout(layoutItems, this.checkBoxScale);
  }
}

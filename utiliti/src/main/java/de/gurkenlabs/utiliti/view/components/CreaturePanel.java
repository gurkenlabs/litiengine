package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.entities.CombatEntity;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.CreatureAnimationState;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.SpriteVariantSelector;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.renderers.LabelListCellRenderer;
import java.awt.LayoutManager;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

public class CreaturePanel extends PropertyPanel {
  public static final String WALK_SPRITE_TOKEN = "walk";
  private final JComboBox<JLabel> comboBoxSpriteSheets;
  private final JComboBox<Direction> comboBoxDirection;
  private final JCheckBox checkBoxScale;
  private final JCheckBox checkBoxStartDead;
  private final SpriteAnimationPreview animationPreview;
  private boolean creaturesLoaded; // mirrors PropPanel.propsLoaded behavior

  public CreaturePanel() {
    super("panel_creature", Icons.CREATURE_16);
    this.comboBoxSpriteSheets = new SearchableSpriteComboBox();
    this.comboBoxSpriteSheets.setRenderer(new LabelListCellRenderer());
    this.animationPreview = new SpriteAnimationPreview();

    this.comboBoxDirection = new JComboBox<>();
    this.comboBoxDirection.setModel(new DefaultComboBoxModel<>(Direction.values()));
    this.checkBoxScale = new JCheckBox(Resources.strings().get("panel_stretch_sprite"));
    this.checkBoxStartDead = new JCheckBox(Resources.strings().get("panel_creature_startDead"));
    Resources.spritesheets().addClearedListener(this::clearSpriteCache);

    setLayout(this.createLayout());
    setupChangedListeners();
    this.comboBoxSpriteSheets.addActionListener(e -> updateSpritePreview());
    this.checkBoxStartDead.addActionListener(e -> updateSpritePreview());

    // if images are cleared (e.g. resource reload), repopulate on next bind
    Resources.images().addClearedListener(() -> this.creaturesLoaded = false);
  }

  private void clearSpriteCache() {
    this.creaturesLoaded = false;
    this.comboBoxSpriteSheets.removeAllItems();
  }

  public static String getCreatureSpriteName(String name) {
    if (name == null || name.isBlank()) {
      return null;
    }

    for (CreatureAnimationState state : CreatureAnimationState.values()) {
      String stateToken = "-" + state.spriteString();
      if (name.endsWith(stateToken)) {
        return name.substring(0, name.length() - stateToken.length());
      }

      int stateIndex = name.indexOf(stateToken + "-");
      if (stateIndex > 0) {
        return name.substring(0, stateIndex);
      }
    }
    String walkToken = "-" + WALK_SPRITE_TOKEN;
    if (name.endsWith(walkToken)) {
      return name.substring(0, name.length() - walkToken.length());
    }
    int walkIndex = name.indexOf(walkToken + "-");
    if (walkIndex > 0) {
      return name.substring(0, walkIndex);
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
    this.checkBoxStartDead.setSelected(false);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    // first try regular selection by stored property (base name expected)
    selectSpriteSheet(this.comboBoxSpriteSheets, mapObject);
    updateSpritePreview();

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
    this.checkBoxStartDead.setSelected(isStartDead(mapObject));
  }

  private void setupChangedListeners() {
    // use the standard helper just like PropPanel (stores label text/base creature name)
    setupL(this.comboBoxSpriteSheets, MapObjectProperty.SPRITESHEETNAME);
    setup(this.comboBoxDirection, MapObjectProperty.SPAWN_DIRECTION);
    setup(this.checkBoxScale, MapObjectProperty.SCALE_SPRITE);
    this.checkBoxStartDead.addActionListener(
      new MapObjectPropertyActionListener(
        m -> isStartDead(m) != this.checkBoxStartDead.isSelected(),
        m -> applyStartDead(m, this.checkBoxStartDead.isSelected())));
  }

  static boolean isStartDead(IMapObject mapObject) {
    return mapObject.getIntValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, CombatEntity.DEFAULT_HITPOINTS) <= 0
      && !mapObject.getBoolValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, false);
  }

  static void applyStartDead(IMapObject mapObject, boolean startDead) {
    if (startDead) {
      mapObject.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, false);
      mapObject.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, 0);
    } else {
      mapObject.removeProperty(MapObjectProperty.COMBAT_CURRENT_HITPOINTS);
    }
  }

  private void loadAvailableCreatureSprites() {
    if (this.creaturesLoaded) {
      return;
    }

    // Use reusable selector to get representative sprite per creature base
    Map<String, String> m = SpriteVariantSelector.selectBaseCreatureSpriteNames(Resources.spritesheets().getAll());
    populateComboBoxWithSprites(this.comboBoxSpriteSheets, new TreeMap<>(m)); // TreeMap for sorted order
    this.creaturesLoaded = true;
  }

  int getSpriteItemCountForTest() {
    return this.comboBoxSpriteSheets.getItemCount();
  }

  boolean isStartDeadSelectedForTest() {
    return this.checkBoxStartDead.isSelected();
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem[] {
      new LayoutItem(this.animationPreview, 140),
      new LayoutItem("panel_sprite", this.comboBoxSpriteSheets),
      new LayoutItem("panel_direction", this.comboBoxDirection),
    };
    return this.createLayout(layoutItems, this.checkBoxScale, this.checkBoxStartDead);
  }

  private void updateSpritePreview() {
    String name = SearchableSpriteComboBox.selectedText(this.comboBoxSpriteSheets);
    String source = name != null ? SpriteVariantSelector.selectBaseCreatureSpriteNames(Resources.spritesheets().getAll()).get(name) : null;
    var spritesheet = this.checkBoxStartDead.isSelected() && name != null
        ? Resources.spritesheets().get(name + "-" + CreatureAnimationState.DEAD.spriteString()) : null;
    if (spritesheet == null) {
      spritesheet = source != null ? Resources.spritesheets().get(source) : null;
    }
    this.animationPreview.setSpritesheet(spritesheet);
  }
}

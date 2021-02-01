package com.litiengine.utiliti.swing.panels;

import java.awt.LayoutManager;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.litiengine.Direction;
import com.litiengine.environment.tilemap.IMapObject;
import com.litiengine.environment.tilemap.MapObjectProperty;
import com.litiengine.graphics.CreatureAnimationState;
import com.litiengine.graphics.Spritesheet;
import com.litiengine.resources.Resources;
import com.litiengine.utiliti.swing.Icons;
import com.litiengine.utiliti.swing.LabelListCellRenderer;

@SuppressWarnings("serial")
public class CreaturePanel extends PropertyPanel {
  private final JComboBox<JLabel> comboBoxSpriteSheets;
  private final JComboBox<Direction> comboBoxDirection;
  private final JCheckBox checkBoxScale;

  /**
   * Create the panel.
   */
  public CreaturePanel() {
    super("panel_creature", Icons.CREATURE);

    this.comboBoxSpriteSheets = new JComboBox<>();
    this.comboBoxSpriteSheets.setRenderer(new LabelListCellRenderer());

    this.comboBoxDirection = new JComboBox<>();
    this.comboBoxDirection.setModel(new DefaultComboBoxModel<>(Direction.values()));
    this.checkBoxScale = new JCheckBox(Resources.strings().get("panel_stretch_spripte"));
    
    setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  public static String getCreatureSpriteName(String name) {
    for (CreatureAnimationState state : CreatureAnimationState.values()) {
      if (name.endsWith(state.spriteString())) {
        return name.substring(0, name.length() - state.spriteString().length() - 1);
      }
    }

    for (Direction dir : Direction.values()) {
      String idle = CreatureAnimationState.IDLE.spriteString() + "-" + dir.toString().toLowerCase();
      if (name.endsWith(idle)) {
        return name.substring(0, name.length() - idle.length() - 1);
      }

      String walk = CreatureAnimationState.WALK.spriteString() + "-" + dir.toString().toLowerCase();
      if (name.endsWith(walk)) {
        return name.substring(0, name.length() - walk.length() - 1);
      }
    }

    return null;
  }

  @Override
  public void bind(IMapObject mapObject) {
    this.isFocussing = true;
    this.loadAvailableCreatureSprites();
    if (mapObject != null) {
      this.setControlValues(mapObject);
    }

    this.isFocussing = false;
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
    selectSpriteSheet(this.comboBoxSpriteSheets, mapObject);
    this.comboBoxDirection.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.UNDEFINED));
    this.checkBoxScale.setSelected(mapObject.getBoolValue(MapObjectProperty.SCALE_SPRITE));
  }

  private void setupChangedListeners() {
    this.setupL(this.comboBoxSpriteSheets, MapObjectProperty.SPRITESHEETNAME);
    this.setup(this.comboBoxDirection, MapObjectProperty.SPAWN_DIRECTION);
    this.setup(this.checkBoxScale, MapObjectProperty.SCALE_SPRITE);
  }

  private void loadAvailableCreatureSprites() {
    Map<String, String> m = new TreeMap<>();
    for (Spritesheet s : Resources.spritesheets().getAll()) {
      String creatureSpriteName = getCreatureSpriteName(s.getName());
      if (creatureSpriteName != null && !m.containsKey(creatureSpriteName)) {
        m.put(creatureSpriteName, s.getName());
      }
    }

    populateComboBoxWithSprites(this.comboBoxSpriteSheets, m);
  }
  
  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem [] {
        new LayoutItem("panel_sprite", this.comboBoxSpriteSheets),
        new LayoutItem("panel_direction", this.comboBoxDirection),
    };
    
    return this.createLayout(layoutItems, this.checkBoxScale);
  }
}
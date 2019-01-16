package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.CreatureAnimationState;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.swing.LabelListCellRenderer;

@SuppressWarnings("serial")
public class CreaturePanel extends PropertyPanel {
  private final JComboBox<JLabel> comboBoxSpriteSheets;
  private final JComboBox<Direction> comboBoxDirection;
  private final JTextField textFieldType;
  private final JCheckBox checkBoxScale;

  /**
   * Create the panel.
   */
  public CreaturePanel() {
    super("panel_creature");

    this.comboBoxSpriteSheets = new JComboBox<>();
    this.comboBoxSpriteSheets.setRenderer(new LabelListCellRenderer());

    this.comboBoxDirection = new JComboBox<>();
    this.comboBoxDirection.setModel(new DefaultComboBoxModel<>(Direction.values()));
    this.checkBoxScale = new JCheckBox("stretch sprite");
    
    this.textFieldType = new JTextField();
    this.textFieldType.setColumns(10);

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
    this.textFieldType.setText(null);
    this.comboBoxDirection.setSelectedItem(Direction.UNDEFINED);
    this.checkBoxScale.setSelected(false);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    selectSpriteSheet(this.comboBoxSpriteSheets, mapObject);
    this.textFieldType.setText(mapObject.getStringValue(MapObjectProperty.SPAWN_TYPE));
    this.comboBoxDirection.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.UNDEFINED));
    this.checkBoxScale.setSelected(mapObject.getBoolValue(MapObjectProperty.SCALE_SPRITE));
  }

  private void setupChangedListeners() {
    this.setupL(this.comboBoxSpriteSheets, MapObjectProperty.SPRITESHEETNAME);
    this.setup(this.comboBoxDirection, MapObjectProperty.SPAWN_DIRECTION);
    this.setup(this.textFieldType, MapObjectProperty.SPAWN_TYPE);
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
    JLabel lblSprite = new JLabel(Resources.strings().get("panel_sprite"));
    JLabel label = new JLabel("direction");
    JLabel lblType = new JLabel("type");
    
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addGap(CONTROL_MARGIN)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
            .addComponent(lblType, LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE)
            .addComponent(lblSprite, LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE)
            .addComponent(label, LABEL_WIDTH, LABEL_WIDTH, Short.MAX_VALUE))
          .addPreferredGap(ComponentPlacement.RELATED, LABEL_GAP, Short.MAX_VALUE)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(comboBoxSpriteSheets, CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE)
            .addComponent(comboBoxDirection,  CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE)
            .addComponent(textFieldType, CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE)
            .addComponent(checkBoxScale, CONTROL_MIN_WIDTH, CONTROL_WIDTH, Short.MAX_VALUE))
          .addGap(CONTROL_MARGIN)));
    
    groupLayout.setVerticalGroup(
        groupLayout.createParallelGroup(Alignment.TRAILING)
        .addGroup(groupLayout.createSequentialGroup()
            .addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(comboBoxSpriteSheets, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblSprite, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
              .addComponent(comboBoxDirection, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)
              .addComponent(label, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE))
            .addGap(CONTROL_MARGIN)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
              .addComponent(textFieldType, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)
              .addComponent(lblType, GroupLayout.PREFERRED_SIZE, LABEL_HEIGHT, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(checkBoxScale, GroupLayout.PREFERRED_SIZE, CONTROL_HEIGHT, GroupLayout.PREFERRED_SIZE)));
    
    return groupLayout;
  }
}
package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.annotation.AnimationInfo;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.ImageCache;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.swing.LabelListCellRenderer;

@SuppressWarnings("serial")
public class PropPanel extends PropertyPanel {
  private JComboBox<JLabel> comboBoxSpriteSheets;
  private JCheckBox chckbxIndestructible;
  private JSpinner spinnerHealth;
  private JComboBox<Material> comboBoxMaterial;
  private JCheckBox chckbxIsObstacle;
  private JCheckBox chckbxShadow;
  private JComboBox<Rotation> comboBoxRotation;
  private JCheckBox checkBoxHorizontalFlip;
  private JCheckBox checkBoxVerticalFlip;
  private JCheckBox checkBoxScale;

  private boolean propsLoaded;

  /**
   * Create the panel.
   */
  public PropPanel() {
    ImageCache.SPRITES.onCleared(e -> this.propsLoaded = false);

    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.strings().get("panel_prop"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblHealth = new JLabel(Resources.strings().get("panel_health"));

    this.spinnerHealth = new JSpinner();
    this.spinnerHealth.setModel(new SpinnerNumberModel(100, 0, 1000000, 1));

    JLabel lblMaterial = new JLabel(Resources.strings().get("panel_material"));

    this.comboBoxMaterial = new JComboBox<>();
    this.comboBoxMaterial.setModel(new DefaultComboBoxModel<Material>(Material.values()));

    JLabel lblSprite = new JLabel(Resources.strings().get("panel_sprite"));

    this.comboBoxSpriteSheets = new JComboBox<>();
    this.comboBoxSpriteSheets.setRenderer(new LabelListCellRenderer());

    this.chckbxIndestructible = new JCheckBox(Resources.strings().get("panel_destructible"));
    this.chckbxIsObstacle = new JCheckBox(Resources.strings().get("panel_isObstacle"));
    this.chckbxShadow = new JCheckBox("shadow");

    this.comboBoxRotation = new JComboBox<>();
    this.comboBoxRotation.setModel(new DefaultComboBoxModel<>(Rotation.values()));

    JLabel lblRotation = new JLabel("rotation");

    this.checkBoxHorizontalFlip = new JCheckBox("horizontal flip");
    this.checkBoxVerticalFlip = new JCheckBox("vertical flip");

    this.checkBoxScale = new JCheckBox("stretch sprite");

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup()
        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblHealth, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addComponent(lblRotation, GroupLayout.PREFERRED_SIZE, 51,
            GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(ComponentPlacement.RELATED)
        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup()
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(chckbxIsObstacle).addComponent(chckbxIndestructible, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE).addComponent(chckbxShadow, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(checkBoxHorizontalFlip).addComponent(checkBoxVerticalFlip, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE).addComponent(checkBoxScale, GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)).addContainerGap())
            .addGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(comboBoxMaterial, 0, 191, Short.MAX_VALUE).addComponent(spinnerHealth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxRotation, 0, 191, Short.MAX_VALUE))
                .addGap(5))))
        .addGroup(groupLayout.createSequentialGroup().addComponent(lblSprite, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(comboBoxSpriteSheets, 0, 191, Short.MAX_VALUE).addGap(5)))));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblSprite, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxSpriteSheets, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblHealth, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(spinnerHealth, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxMaterial, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
            .addGap(7).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(comboBoxRotation, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE).addComponent(lblRotation, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(chckbxIndestructible, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE).addComponent(checkBoxHorizontalFlip)).addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(chckbxIsObstacle, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addComponent(checkBoxVerticalFlip)).addPreferredGap(ComponentPlacement.UNRELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(chckbxShadow, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addComponent(checkBoxScale, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    setLayout(groupLayout);
    this.setupChangedListeners();
  }

  public static String getNameBySpriteName(String spriteName) {
    if (spriteName == null || spriteName.isEmpty()) {
      return null;
    }

    AnimationInfo info = Prop.class.getAnnotation(AnimationInfo.class);
    if (info == null || info.spritePrefix() == null || info.spritePrefix().length == 0) {
      return null;
    }

    if (Arrays.asList(info.spritePrefix()).stream().noneMatch(x -> spriteName.toLowerCase().startsWith(x))) {
      return null;
    }

    String[] parts = spriteName.split("-");
    return parts[1];
  }

  @Override
  public void bind(IMapObject mapObject) {
    this.isFocussing = true;
    this.loadAvailableProps();
    if (mapObject != null) {
      this.setControlValues(mapObject);
    }
    this.isFocussing = false;
    super.bind(mapObject);
  }

  @Override
  protected void clearControls() {
    this.chckbxIndestructible.setSelected(false);
    this.chckbxIsObstacle.setSelected(false);
    this.chckbxShadow.setSelected(false);
    this.checkBoxScale.setSelected(false);
    this.comboBoxMaterial.setSelectedItem(Material.UNDEFINED);
    this.comboBoxRotation.setSelectedItem(Rotation.NONE);
    this.spinnerHealth.setValue(0);
    this.comboBoxSpriteSheets.setSelectedItem(null);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    selectSpriteSheet(this.comboBoxSpriteSheets, mapObject);

    final Material material = mapObject.getStringValue(MapObjectProperty.PROP_MATERIAL) == null ? Material.UNDEFINED : Material.valueOf(mapObject.getStringValue(MapObjectProperty.PROP_MATERIAL));
    this.comboBoxMaterial.setSelectedItem(material);

    final Rotation rotation = mapObject.getStringValue(MapObjectProperty.PROP_ROTATION) == null ? Rotation.NONE : Rotation.valueOf(mapObject.getStringValue(MapObjectProperty.PROP_ROTATION));
    this.comboBoxRotation.setSelectedItem(rotation);

    this.spinnerHealth.setValue(mapObject.getIntValue(MapObjectProperty.COMBAT_HEALTH));
    this.chckbxIndestructible.setSelected(!mapObject.getBoolValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE));
    this.chckbxShadow.setSelected(mapObject.getBoolValue(MapObjectProperty.PROP_ADDSHADOW));
    this.chckbxIsObstacle.setSelected(mapObject.getBoolValue(MapObjectProperty.PROP_OBSTACLE));
    this.checkBoxHorizontalFlip.setSelected(mapObject.getBoolValue(MapObjectProperty.PROP_FLIPHORIZONTALLY));
    this.checkBoxVerticalFlip.setSelected(mapObject.getBoolValue(MapObjectProperty.PROP_FLIPVERTICALLY));
    this.checkBoxScale.setSelected(mapObject.getBoolValue(MapObjectProperty.PROP_SCALE));
  }

  private void setupChangedListeners() {
    this.chckbxIndestructible.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, !chckbxIndestructible.isSelected())));
    this.chckbxShadow.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.PROP_ADDSHADOW, chckbxShadow.isSelected())));

    this.comboBoxMaterial.addActionListener(new MapObjectPropertyActionListener(m -> {
      Material material = (Material) this.comboBoxMaterial.getSelectedItem();
      m.setValue(MapObjectProperty.PROP_MATERIAL, material);
    }));

    this.comboBoxRotation.addActionListener(new MapObjectPropertyActionListener(m -> {
      Rotation rotation = (Rotation) this.comboBoxRotation.getSelectedItem();
      m.setValue(MapObjectProperty.PROP_ROTATION, rotation);
    }));

    this.comboBoxSpriteSheets.addActionListener(new MapObjectPropertyActionListener(m -> {
      JLabel selected = (JLabel) this.comboBoxSpriteSheets.getSelectedItem();
      m.setValue(MapObjectProperty.SPRITESHEETNAME, selected.getText());
    }));

    this.spinnerHealth.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setValue(MapObjectProperty.COMBAT_HEALTH, (int) this.spinnerHealth.getValue())));
    this.chckbxIsObstacle.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.PROP_OBSTACLE, chckbxIsObstacle.isSelected())));

    this.checkBoxHorizontalFlip.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.PROP_FLIPHORIZONTALLY, checkBoxHorizontalFlip.isSelected())));
    this.checkBoxVerticalFlip.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.PROP_FLIPVERTICALLY, checkBoxVerticalFlip.isSelected())));
    this.checkBoxScale.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.PROP_SCALE, checkBoxScale.isSelected())));
  }

  private void loadAvailableProps() {
    if (this.propsLoaded) {
      return;
    }
    Map<String, String> m = new TreeMap<>();
    for (Spritesheet s : Resources.spritesheets().getAll()) {
      String spriteName = s.getName();
      String propName = getNameBySpriteName(spriteName);

      if (propName == null) {
        continue;
      }

      if (!m.containsKey(propName)) {
        m.put(propName, spriteName);
      }
    }

    populateComboBoxWithSprites(this.comboBoxSpriteSheets, m);

    this.propsLoaded = true;
  }
}
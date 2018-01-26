package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.utiliti.swing.LabelListCellRenderer;

public class PropPanel extends PropertyPanel<IMapObject> {
  private JComboBox<JLabel> comboBoxSpriteSheets;
  private JCheckBox chckbxIndestructible;
  private JSpinner spinnerHealth;
  private JComboBox<Material> comboBoxMaterial;
  private JCheckBox chckbxIsObstacle;
  private JCheckBox chckbxShadow;

  /**
   * Create the panel.
   */
  public PropPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_prop"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblHealth = new JLabel(Resources.get("panel_health"));

    spinnerHealth = new JSpinner();
    spinnerHealth.setModel(new SpinnerNumberModel(100, 0, 1000000, 1));

    JLabel lblMaterial = new JLabel(Resources.get("panel_material"));

    comboBoxMaterial = new JComboBox<>();
    comboBoxMaterial.setModel(new DefaultComboBoxModel<Material>(Material.values()));

    JLabel lblSprite = new JLabel(Resources.get("panel_sprite"));

    comboBoxSpriteSheets = new JComboBox<>();
    comboBoxSpriteSheets.setRenderer(new LabelListCellRenderer());

    chckbxIndestructible = new JCheckBox(Resources.get("panel_destructible"));

    chckbxIsObstacle = new JCheckBox(Resources.get("panel_isObstacle"));

    chckbxShadow = new JCheckBox("shadow");
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout
        .setHorizontalGroup(
            groupLayout
                .createParallelGroup(
                    Alignment.LEADING)
                .addGroup(
                    groupLayout.createSequentialGroup().addContainerGap()
                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblHealth, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addComponent(lblSprite,
                            GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(
                            groupLayout.createParallelGroup(Alignment.LEADING).addComponent(chckbxShadow, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxMaterial, 0, 365, Short.MAX_VALUE)
                                .addComponent(spinnerHealth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxSpriteSheets, 0, 365, Short.MAX_VALUE).addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
                                    .addComponent(chckbxIsObstacle, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(chckbxIndestructible, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap()));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblHealth, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(spinnerHealth, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxMaterial, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(comboBoxSpriteSheets, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblSprite, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addComponent(chckbxIndestructible).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(chckbxIsObstacle, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(chckbxShadow, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addContainerGap(138, Short.MAX_VALUE)));
    setLayout(groupLayout);
    this.setupChangedListeners();
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
    this.comboBoxMaterial.setSelectedItem(Material.UNDEFINED);
    this.spinnerHealth.setValue(0);
    this.comboBoxSpriteSheets.setSelectedItem(null);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    if (mapObject.getCustomProperty(MapObjectProperty.SPRITESHEETNAME) != null) {
      for (int i = 0; i < this.comboBoxSpriteSheets.getModel().getSize(); i++) {
        JLabel label = this.comboBoxSpriteSheets.getModel().getElementAt(i);
        if (label != null && label.getText().equals(mapObject.getCustomProperty(MapObjectProperty.SPRITESHEETNAME))) {
          this.comboBoxSpriteSheets.setSelectedItem(label);
          break;
        }
      }
    }

    final Material material = mapObject.getCustomProperty(MapObjectProperty.PROP_MATERIAL) == null ? Material.UNDEFINED : Material.valueOf(mapObject.getCustomProperty(MapObjectProperty.PROP_MATERIAL));
    this.comboBoxMaterial.setSelectedItem(material);

    this.spinnerHealth.setValue(mapObject.getCustomPropertyInt(MapObjectProperty.HEALTH));
    this.chckbxIndestructible.setSelected(!mapObject.getCustomPropertyBool(MapObjectProperty.PROP_INDESTRUCTIBLE));
    this.chckbxShadow.setSelected(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_ADDSHADOW));
    this.chckbxIsObstacle.setSelected(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_OBSTACLE));
  }

  private void setupChangedListeners() {
    this.chckbxIndestructible.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.PROP_INDESTRUCTIBLE, Boolean.toString(!chckbxIndestructible.isSelected()))));

    this.chckbxShadow.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.PROP_ADDSHADOW, Boolean.toString(chckbxShadow.isSelected()))));

    this.comboBoxMaterial.addActionListener(new MapObjectPropertyActionListener(m -> {
      Material material = (Material) this.comboBoxMaterial.getSelectedItem();
      m.setCustomProperty(MapObjectProperty.PROP_MATERIAL, material.toString());
    }));

    this.comboBoxSpriteSheets.addActionListener(new MapObjectPropertyActionListener(m -> {
      JLabel selected = (JLabel) this.comboBoxSpriteSheets.getSelectedItem();
      m.setCustomProperty(MapObjectProperty.SPRITESHEETNAME, selected.getText());
    }));

    this.spinnerHealth.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.HEALTH, Integer.toString((int) this.spinnerHealth.getValue()))));

    this.chckbxIsObstacle.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.PROP_OBSTACLE, Boolean.toString(chckbxIsObstacle.isSelected()))));
  }

  private void loadAvailableProps() {
    Map<String, String> m = new TreeMap<>();
    for (Spritesheet s : Spritesheet.getSpritesheets()) {
      String spriteName = s.getName();
      String propName = Prop.getNameBySpriteName(spriteName);

      if (propName == null) {
        continue;
      }

      if (!m.containsKey(propName)) {
        m.put(propName, spriteName);
      }
    }

    this.comboBoxSpriteSheets.removeAllItems();
    for (String key : m.keySet()) {
      JLabel label = new JLabel();
      label.setText(key);
      String value = m.get(key);
      Spritesheet sprite = Spritesheet.find(value);
      if (sprite != null && sprite.getTotalNumberOfSprites() > 0) {
        BufferedImage img = sprite.getSprite(0);
        BufferedImage scaled = ImageProcessing.scaleImage(img, 24, 24, true);
        if (scaled != null) {
          label.setIcon(new ImageIcon(scaled));
        }
      }

      this.comboBoxSpriteSheets.addItem(label);
    }
  }
}

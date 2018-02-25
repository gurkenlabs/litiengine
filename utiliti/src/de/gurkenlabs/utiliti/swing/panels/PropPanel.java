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
import de.gurkenlabs.litiengine.annotation.AnimationInfo;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.utiliti.swing.LabelListCellRenderer;

@SuppressWarnings("serial")
public class PropPanel extends PropertyPanel<IMapObject> {
  private JComboBox<JLabel> comboBoxSpriteSheets;
  private JCheckBox chckbxIndestructible;
  private JSpinner spinnerHealth;
  private JComboBox<Material> comboBoxMaterial;
  private JCheckBox chckbxIsObstacle;
  private JCheckBox chckbxShadow;
  private JComboBox<Rotation> comboBoxRotation;
  private JCheckBox checkBoxHorizontalFlip;
  private JCheckBox checkBoxVerticalFlip;

  private boolean propsLoaded;

  /**
   * Create the panel.
   */
  public PropPanel() {
    ImageCache.SPRITES.onCleared(e -> this.propsLoaded = false);

    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_prop"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblHealth = new JLabel(Resources.get("panel_health"));

    this.spinnerHealth = new JSpinner();
    this.spinnerHealth.setModel(new SpinnerNumberModel(100, 0, 1000000, 1));

    JLabel lblMaterial = new JLabel(Resources.get("panel_material"));

    this.comboBoxMaterial = new JComboBox<>();
    this.comboBoxMaterial.setModel(new DefaultComboBoxModel<Material>(Material.values()));

    JLabel lblSprite = new JLabel(Resources.get("panel_sprite"));

    this.comboBoxSpriteSheets = new JComboBox<>();
    this.comboBoxSpriteSheets.setRenderer(new LabelListCellRenderer());

    this.chckbxIndestructible = new JCheckBox(Resources.get("panel_destructible"));
    this.chckbxIsObstacle = new JCheckBox(Resources.get("panel_isObstacle"));
    this.chckbxShadow = new JCheckBox("shadow");

    this.comboBoxRotation = new JComboBox<>();
    this.comboBoxRotation.setModel(new DefaultComboBoxModel<>(Rotation.values()));

    JLabel lblRotation = new JLabel("rotation");

    this.checkBoxHorizontalFlip = new JCheckBox("horizontal flip");
    this.checkBoxVerticalFlip = new JCheckBox("vertical flip");

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblHealth, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addComponent(lblRotation,
                        GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(chckbxShadow, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
                                .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(chckbxIsObstacle).addComponent(chckbxIndestructible, GroupLayout.PREFERRED_SIZE, 97, GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(ComponentPlacement.RELATED).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(checkBoxHorizontalFlip).addComponent(checkBoxVerticalFlip, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE))))
                            .addGap(10))
                        .addGroup(groupLayout.createSequentialGroup()
                            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(comboBoxMaterial, 0, 191, Short.MAX_VALUE).addComponent(spinnerHealth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxRotation, 0, 191,
                                Short.MAX_VALUE))
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
            .addComponent(chckbxShadow, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    setLayout(groupLayout);
    this.setupChangedListeners();
  }

  public static String getNameBySpriteName(String spriteName) {
    if (spriteName == null || spriteName.isEmpty()) {
      return null;
    }

    AnimationInfo info = Prop.class.getAnnotation(AnimationInfo.class);
    if (info == null || info.spritePrefix() == null || info.spritePrefix().isEmpty()) {
      return null;
    }

    if (!spriteName.toLowerCase().startsWith(info.spritePrefix())) {
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
    this.comboBoxMaterial.setSelectedItem(Material.UNDEFINED);
    this.comboBoxRotation.setSelectedItem(Rotation.NONE);
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

    final Rotation rotation = mapObject.getCustomProperty(MapObjectProperty.PROP_ROTATION) == null ? Rotation.NONE : Rotation.valueOf(mapObject.getCustomProperty(MapObjectProperty.PROP_ROTATION));
    this.comboBoxRotation.setSelectedItem(rotation);

    this.spinnerHealth.setValue(mapObject.getCustomPropertyInt(MapObjectProperty.HEALTH));
    this.chckbxIndestructible.setSelected(!mapObject.getCustomPropertyBool(MapObjectProperty.PROP_INDESTRUCTIBLE));
    this.chckbxShadow.setSelected(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_ADDSHADOW));
    this.chckbxIsObstacle.setSelected(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_OBSTACLE));
    this.checkBoxHorizontalFlip.setSelected(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_FLIPHORIZONTALLY));
    this.checkBoxVerticalFlip.setSelected(mapObject.getCustomPropertyBool(MapObjectProperty.PROP_FLIPVERTICALLY));
  }

  private void setupChangedListeners() {
    this.chckbxIndestructible.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.PROP_INDESTRUCTIBLE, Boolean.toString(!chckbxIndestructible.isSelected()))));
    this.chckbxShadow.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.PROP_ADDSHADOW, Boolean.toString(chckbxShadow.isSelected()))));

    this.comboBoxMaterial.addActionListener(new MapObjectPropertyActionListener(m -> {
      Material material = (Material) this.comboBoxMaterial.getSelectedItem();
      m.setCustomProperty(MapObjectProperty.PROP_MATERIAL, material.toString());
    }));

    this.comboBoxRotation.addActionListener(new MapObjectPropertyActionListener(m -> {
      Rotation rotation = (Rotation) this.comboBoxRotation.getSelectedItem();
      m.setCustomProperty(MapObjectProperty.PROP_ROTATION, rotation.toString());
    }));

    this.comboBoxSpriteSheets.addActionListener(new MapObjectPropertyActionListener(m -> {
      JLabel selected = (JLabel) this.comboBoxSpriteSheets.getSelectedItem();
      m.setCustomProperty(MapObjectProperty.SPRITESHEETNAME, selected.getText());
    }));

    this.spinnerHealth.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.HEALTH, Integer.toString((int) this.spinnerHealth.getValue()))));
    this.chckbxIsObstacle.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.PROP_OBSTACLE, Boolean.toString(chckbxIsObstacle.isSelected()))));

    this.checkBoxHorizontalFlip.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.PROP_FLIPHORIZONTALLY, Boolean.toString(checkBoxHorizontalFlip.isSelected()))));
    this.checkBoxVerticalFlip.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.PROP_FLIPVERTICALLY, Boolean.toString(checkBoxVerticalFlip.isSelected()))));
  }

  private void loadAvailableProps() {
    if (this.propsLoaded) {
      return;
    }
    Map<String, String> m = new TreeMap<>();
    for (Spritesheet s : Spritesheet.getSpritesheets()) {
      String spriteName = s.getName();
      String propName = getNameBySpriteName(spriteName);

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
        BufferedImage scaled;
        String cacheKey = "iconx24" + sprite.getName();
        if (ImageCache.SPRITES.containsKey(cacheKey)) {
          scaled = ImageCache.SPRITES.get(cacheKey);
        } else {
          if (img != null) {
            scaled = ImageProcessing.scaleImage(img, 24, 24, true);
          } else {
            scaled = ImageProcessing.getCompatibleImage(24, 24);
          }

          ImageCache.SPRITES.put(cacheKey, scaled);
        }

        if (scaled != null) {
          label.setIcon(new ImageIcon(scaled));
        }
      }

      this.comboBoxSpriteSheets.addItem(label);
    }

    this.propsLoaded = true;
  }
}
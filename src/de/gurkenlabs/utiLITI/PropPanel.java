package de.gurkenlabs.utiLITI;

import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JList;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;

public class PropPanel extends PropertyPanel<IMapObject> {
  private JComboBox<JLabel> comboBoxSpriteSheets;
  private JCheckBox chckbxIndestructible;
  private JSpinner spinnerHealth;
  private JComboBox comboBoxMaterial;
  private JCheckBox chckbxIsObstacle;

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

    comboBoxMaterial = new JComboBox();
    comboBoxMaterial.setModel(new DefaultComboBoxModel(Material.values()));

    JLabel lblSprite = new JLabel(Resources.get("panel_sprite"));

    comboBoxSpriteSheets = new JComboBox<>();
    comboBoxSpriteSheets.setRenderer(new MyComboRenderer());

    chckbxIndestructible = new JCheckBox(Resources.get("panel_destructible"));

    chckbxIsObstacle = new JCheckBox(Resources.get("panel_isObstacle"));
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(lblHealth, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSprite, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(comboBoxMaterial, 0, 365, Short.MAX_VALUE)
                    .addComponent(spinnerHealth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxSpriteSheets, 0, 365, Short.MAX_VALUE)
                    .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
                        .addComponent(chckbxIsObstacle, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(chckbxIndestructible, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap()));
    groupLayout.setVerticalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblHealth, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerHealth, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(comboBoxMaterial, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(comboBoxSpriteSheets, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSprite, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(chckbxIndestructible)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(chckbxIsObstacle, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(159, Short.MAX_VALUE)));
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
    this.comboBoxMaterial.setSelectedItem(Material.UNDEFINED);
    this.spinnerHealth.setValue(0);
    this.comboBoxSpriteSheets.setSelectedItem(null);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    if (mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME) != null) {
      for (int i = 0; i < this.comboBoxSpriteSheets.getModel().getSize(); i++) {
        JLabel label = this.comboBoxSpriteSheets.getModel().getElementAt(i);
        if (label != null && label.getText().equals(mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME))) {
          this.comboBoxSpriteSheets.setSelectedItem(label);
          break;
        }
      }
    }

    final Material material = mapObject.getCustomProperty(MapObjectProperties.MATERIAL) == null ? Material.UNDEFINED : Material.valueOf(mapObject.getCustomProperty(MapObjectProperties.MATERIAL));
    this.comboBoxMaterial.setSelectedItem(material);

    if (mapObject.getCustomProperty(MapObjectProperties.HEALTH) != null) {
      this.spinnerHealth.setValue(Integer.parseInt(mapObject.getCustomProperty(MapObjectProperties.HEALTH)));
    }
    if (mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE) != null && !mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE).isEmpty()) {
      this.chckbxIndestructible.setSelected(!Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE)));
    }

    String obstacle = mapObject.getCustomProperty(MapObjectProperties.OBSTACLE);
    if (obstacle != null && !obstacle.isEmpty()) {
      this.chckbxIsObstacle.setSelected(Boolean.valueOf(obstacle));
    }
  }

  private void setupChangedListeners() {
    this.chckbxIndestructible.addActionListener(new MapObjectPropertyActionListener((m) -> {
      m.setCustomProperty(MapObjectProperties.INDESTRUCTIBLE, Boolean.toString(!chckbxIndestructible.isSelected()));
    }));

    this.comboBoxMaterial.addActionListener(new MapObjectPropertyActionListener((m) -> {
      Material material = (Material) this.comboBoxMaterial.getSelectedItem();
      m.setCustomProperty(MapObjectProperties.MATERIAL, material.toString());
    }));

    this.comboBoxSpriteSheets.addActionListener(new MapObjectPropertyActionListener((m) -> {
      JLabel selected = (JLabel) this.comboBoxSpriteSheets.getSelectedItem();
      m.setCustomProperty(MapObjectProperties.SPRITESHEETNAME, selected.getText());
    }));

    this.spinnerHealth.addChangeListener(new MapObjectPropertyChangeListener((m) -> {
      m.setCustomProperty(MapObjectProperties.HEALTH, Integer.toString((int) this.spinnerHealth.getValue()));
    }));

    this.chckbxIsObstacle.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setCustomProperty(MapObjectProperties.OBSTACLE, Boolean.toString(chckbxIsObstacle.isSelected()));
    }));
  }

  private void loadAvailableProps() {
    Map<String, String> m = new TreeMap<>();
    for (Spritesheet s : Spritesheet.spritesheets.values()) {
      String spriteName = s.getName();
      if (spriteName.startsWith("prop-")) {
        String[] parts = spriteName.split("-");
        String propName = parts[1];
        if (!m.containsKey(propName)) {
          m.put(propName, spriteName);
        }
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

  class MyComboRenderer implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      if (value != null) {
        JLabel label = (JLabel) value;
        return label;
      }
      return new JLabel();
    }
  }
}

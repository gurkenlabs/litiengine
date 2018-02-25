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
import de.gurkenlabs.litiengine.entities.DecorMob.MovementBehavior;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.utiliti.swing.LabelListCellRenderer;

@SuppressWarnings("serial")
public class DecorMobPanel extends PropertyPanel<IMapObject> {
  private final JComboBox<JLabel> comboBoxSpriteSheets;
  private final JComboBox<MovementBehavior> comboBoxBehaviour;
  private JSpinner spinnerVelocity;
  private JCheckBox chckbxAttackable;

  /**
   * Create the panel.
   */
  public DecorMobPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_decorMob"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    JLabel lblSprite = new JLabel(Resources.get("panel_sprite"));

    this.comboBoxSpriteSheets = new JComboBox<>();
    this.comboBoxSpriteSheets.setRenderer(new LabelListCellRenderer());

    JLabel lblBehaviour = new JLabel(Resources.get("panel_behavior"));

    this.comboBoxBehaviour = new JComboBox<>();
    this.comboBoxBehaviour.setModel(new DefaultComboBoxModel<MovementBehavior>(MovementBehavior.values()));

    JLabel lblVvelocity = new JLabel(Resources.get("panel_velocity"));

    this.spinnerVelocity = new JSpinner();
    this.spinnerVelocity.setModel(new SpinnerNumberModel(0, 0, 100, 1));

    this.chckbxAttackable = new JCheckBox(Resources.get("panel_attackable"));
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addComponent(lblSprite, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(comboBoxSpriteSheets, 0, 95, Short.MAX_VALUE).addGap(10))
        .addGroup(groupLayout.createSequentialGroup().addComponent(lblBehaviour, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(comboBoxBehaviour, 0, 95, Short.MAX_VALUE).addContainerGap())
        .addGroup(groupLayout.createSequentialGroup().addComponent(lblVvelocity, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addGroup(
            groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(chckbxAttackable, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGap(28)).addComponent(spinnerVelocity, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE))
            .addContainerGap()))));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblSprite, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxSpriteSheets, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.UNRELATED).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblBehaviour, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxBehaviour, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblVvelocity, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(spinnerVelocity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.UNRELATED).addComponent(chckbxAttackable).addContainerGap(171, Short.MAX_VALUE)));
    setLayout(groupLayout);
    this.setupChangedListeners();
  }

  @Override
  public void bind(IMapObject mapObject) {
    this.isFocussing = true;
    this.loadAvailableDecorMobs();
    if (mapObject != null) {
      this.setControlValues(mapObject);
    }

    this.isFocussing = false;
    super.bind(mapObject);
  }

  @Override
  protected void clearControls() {
    this.comboBoxSpriteSheets.setSelectedItem(null);
    this.comboBoxBehaviour.setSelectedItem(MovementBehavior.IDLE);
    this.spinnerVelocity.setValue(2);
    this.chckbxAttackable.setSelected(false);
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
    if (mapObject.getCustomProperty(MapObjectProperty.DECORMOB_BEHAVIOUR) != null) {
      MovementBehavior beh = MovementBehavior.get(mapObject.getCustomProperty(MapObjectProperty.DECORMOB_BEHAVIOUR));
      this.comboBoxBehaviour.setSelectedItem(beh);
    }

    this.spinnerVelocity.setValue(mapObject.getCustomPropertyInt(MapObjectProperty.DECORMOB_VELOCITY));
    this.chckbxAttackable.setSelected(!mapObject.getCustomPropertyBool(MapObjectProperty.PROP_INDESTRUCTIBLE));
  }

  private void setupChangedListeners() {

    this.comboBoxSpriteSheets.addActionListener(new MapObjectPropertyActionListener(m -> {
      JLabel selected = (JLabel) this.comboBoxSpriteSheets.getSelectedItem();
      m.setCustomProperty(MapObjectProperty.SPRITESHEETNAME, selected.getText());
    }));

    this.comboBoxBehaviour.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.DECORMOB_BEHAVIOUR, this.comboBoxBehaviour.getSelectedItem().toString())));
    this.spinnerVelocity.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.DECORMOB_VELOCITY, this.spinnerVelocity.getValue().toString())));
    this.chckbxAttackable.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.PROP_INDESTRUCTIBLE, Boolean.toString(!this.chckbxAttackable.isSelected()))));
  }

  private void loadAvailableDecorMobs() {
    Map<String, String> m = new TreeMap<>();
    for (Spritesheet s : Spritesheet.getSpritesheets()) {
      String spriteName = s.getName();
      if (spriteName.startsWith("decormob-")) {
        String[] parts = spriteName.split("-");
        String propName = parts[1];
        if (!m.containsKey(propName)) {
          m.put(propName, spriteName);
        }
      }
    }

    this.comboBoxSpriteSheets.removeAllItems();
    for (Map.Entry<String, String> entry : m.entrySet()) {
      JLabel label = new JLabel();
      label.setText(entry.getKey());
      String value = entry.getValue();
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
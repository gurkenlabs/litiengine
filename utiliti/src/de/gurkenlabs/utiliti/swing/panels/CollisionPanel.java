package de.gurkenlabs.utiliti.swing.panels;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.resources.Resources;

@SuppressWarnings("serial")
public class CollisionPanel extends PropertyPanel {
  private JCheckBox chckbxHasCollision;
  private JSpinner spinnerWidth;
  private JSpinner spinnerHeight;
  private JComboBox<Align> comboBoxAlign;
  private JComboBox<Valign> comboBoxValign;
  private JLabel lblHorizontalAlignment;
  private JLabel lblVerticalAlignment;
  private JLabel lblCollision;

  /**
   * Create the panel.
   */
  public CollisionPanel() {
    super("panel_collisionEntity");

    JLabel lblMaterial = new JLabel(Resources.strings().get("panel_width"));

    this.chckbxHasCollision = new JCheckBox("");

    JLabel lblHeightFactor = new JLabel(Resources.strings().get("panel_height"));

    this.spinnerWidth = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));

    this.spinnerHeight = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.1));

    this.comboBoxAlign = new JComboBox<>();
    this.comboBoxAlign.setModel(new DefaultComboBoxModel<Align>(Align.values()));

    this.comboBoxValign = new JComboBox<>();
    this.comboBoxValign.setModel(new DefaultComboBoxModel<Valign>(Valign.values()));
    
    lblHorizontalAlignment = new JLabel("horizontal Alignment");
    
    lblVerticalAlignment = new JLabel("vertical alignment");
    
    lblCollision = new JLabel("collision");

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addContainerGap()
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(lblCollision, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addPreferredGap(ComponentPlacement.RELATED))
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(lblMaterial)
              .addPreferredGap(ComponentPlacement.RELATED))
            .addComponent(lblHeightFactor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(lblHorizontalAlignment, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addPreferredGap(ComponentPlacement.RELATED))
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(lblVerticalAlignment, GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
              .addPreferredGap(ComponentPlacement.RELATED)))
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addPreferredGap(ComponentPlacement.RELATED)
              .addComponent(chckbxHasCollision, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
              .addComponent(comboBoxValign, Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addComponent(spinnerHeight, Alignment.LEADING)
              .addComponent(comboBoxAlign, Alignment.LEADING, 0, 147, Short.MAX_VALUE)
              .addComponent(spinnerWidth, Alignment.LEADING)))
          .addContainerGap())
    );
    groupLayout.setVerticalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addContainerGap()
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(lblCollision, GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
            .addComponent(chckbxHasCollision, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGap(5)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblMaterial, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
            .addComponent(spinnerWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblHeightFactor, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
            .addComponent(spinnerHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addGap(6)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(comboBoxAlign, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(lblHorizontalAlignment, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(comboBoxValign, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(lblVerticalAlignment, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
          .addGap(143))
    );
    setLayout(groupLayout);
    this.setupChangedListeners();
  }

  private void setupChangedListeners() {
    this.chckbxHasCollision.addActionListener(new MapObjectPropertyActionListener(m -> m.setValue(MapObjectProperty.COLLISION, chckbxHasCollision.isSelected())));

    this.spinnerWidth.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setValue(MapObjectProperty.COLLISIONBOX_WIDTH, this.spinnerWidth.getValue().toString())));

    this.spinnerHeight.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setValue(MapObjectProperty.COLLISIONBOX_HEIGHT, this.spinnerHeight.getValue().toString())));

    this.comboBoxAlign.addActionListener(new MapObjectPropertyActionListener(m -> {
      Align align = (Align) this.comboBoxAlign.getSelectedItem();
      m.setValue(MapObjectProperty.COLLISION_ALIGN, align);
    }));

    this.comboBoxValign.addActionListener(new MapObjectPropertyActionListener(m -> {
      Valign valign = (Valign) this.comboBoxValign.getSelectedItem();
      m.setValue(MapObjectProperty.COLLISION_VALIGN, valign);
    }));
  }

  @Override
  protected void clearControls() {
    this.chckbxHasCollision.setSelected(false);
    this.spinnerWidth.setValue(0.0);
    this.spinnerHeight.setValue(0.0);
    this.comboBoxAlign.setSelectedItem(Align.CENTER);
    this.comboBoxAlign.setSelectedItem(Valign.DOWN);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.chckbxHasCollision.setSelected(mapObject.getBoolValue(MapObjectProperty.COLLISION));
    this.spinnerWidth.setValue(mapObject.getDoubleValue(MapObjectProperty.COLLISIONBOX_WIDTH));
    this.spinnerHeight.setValue(mapObject.getDoubleValue(MapObjectProperty.COLLISIONBOX_HEIGHT));

    this.comboBoxAlign.setSelectedItem(Align.get(mapObject.getStringValue(MapObjectProperty.COLLISION_ALIGN)));
    this.comboBoxValign.setSelectedItem(Valign.get(mapObject.getStringValue(MapObjectProperty.COLLISION_VALIGN)));
  }
}

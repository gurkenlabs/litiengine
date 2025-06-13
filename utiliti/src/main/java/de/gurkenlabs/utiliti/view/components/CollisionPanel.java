package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.LayoutManager;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class CollisionPanel extends PropertyPanel {
  private final JCheckBox chckbxHasCollision;
  private final JSpinner spinnerWidth;
  private final JSpinner spinnerHeight;
  private final JComboBox<Align> comboBoxAlign;
  private final JComboBox<Valign> comboBoxValign;
  private final JComboBox<Collision> comboBoxColl;

  /** Create the panel. */
  public CollisionPanel() {
    super("panel_collisionEntity");

    this.chckbxHasCollision = new JCheckBox(Resources.strings().get("panel_collision"));
    this.spinnerWidth = new JSpinner(new SpinnerNumberModel(0.0, 0.0, Integer.MAX_VALUE, 0.2));
    this.spinnerHeight = new JSpinner(new SpinnerNumberModel(0.0, 0.0, Integer.MAX_VALUE, 0.2));
    this.comboBoxAlign = new JComboBox<>();
    this.comboBoxAlign.setModel(new DefaultComboBoxModel<>(Align.values()));
    this.comboBoxValign = new JComboBox<>();
    this.comboBoxValign.setModel(new DefaultComboBoxModel<>(Valign.values()));
    this.comboBoxColl = new JComboBox<>();
    this.comboBoxColl.setModel(
        new DefaultComboBoxModel<>(new Collision[] {Collision.DYNAMIC, Collision.STATIC}));

    this.setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    this.chckbxHasCollision.setSelected(false);
    this.spinnerWidth.setValue(0.0);
    this.spinnerHeight.setValue(0.0);
    this.comboBoxAlign.setSelectedItem(Align.CENTER);
    this.comboBoxValign.setSelectedItem(Valign.DOWN);
    this.comboBoxColl.setSelectedItem(Collision.STATIC);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.chckbxHasCollision.setSelected(mapObject.getBoolValue(MapObjectProperty.COLLISION, false));
    this.spinnerWidth.setValue(mapObject.getDoubleValue(MapObjectProperty.COLLISIONBOX_WIDTH, mapObject.getWidth()));
    this.spinnerHeight.setValue(mapObject.getDoubleValue(MapObjectProperty.COLLISIONBOX_HEIGHT, mapObject.getHeight()));
    this.comboBoxAlign.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.COLLISION_ALIGN, Align.class, Align.CENTER));
    this.comboBoxValign.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.COLLISION_VALIGN, Valign.class, Valign.DOWN));
    this.comboBoxColl.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.COLLISION_TYPE, Collision.class, Collision.DYNAMIC));
  }

  private void setupChangedListeners() {
    setup(chckbxHasCollision, MapObjectProperty.COLLISION);
    setup(spinnerWidth, MapObjectProperty.COLLISIONBOX_WIDTH);
    setup(spinnerHeight, MapObjectProperty.COLLISIONBOX_HEIGHT);
    setup(comboBoxAlign, MapObjectProperty.COLLISION_ALIGN);
    setup(comboBoxValign, MapObjectProperty.COLLISION_VALIGN);
    setup(comboBoxColl, MapObjectProperty.COLLISION_TYPE);
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems =
        new LayoutItem[] {
            new LayoutItem("panel_width", this.spinnerWidth),
            new LayoutItem("panel_height", this.spinnerHeight),
            new LayoutItem("panel_align", this.comboBoxAlign),
            new LayoutItem("panel_valign", this.comboBoxValign),
            new LayoutItem("collisionType", this.comboBoxColl),
        };

    return this.createLayout(layoutItems, this.chckbxHasCollision);
  }
}

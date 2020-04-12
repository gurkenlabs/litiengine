package de.gurkenlabs.utiliti.swing.panels;

import java.awt.LayoutManager;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.swing.Icons;
import de.gurkenlabs.utiliti.swing.LabelListCellRenderer;

@SuppressWarnings("serial")
public class PropPanel extends PropertyPanel {
  private JComboBox<JLabel> comboBoxSpriteSheets;
  private JComboBox<Material> comboBoxMaterial;
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
    super("panel_prop", Icons.PROP);
    Resources.images().addClearedListener(() -> this.propsLoaded = false);

    this.comboBoxSpriteSheets = new JComboBox<>();
    this.comboBoxSpriteSheets.setRenderer(new LabelListCellRenderer());

    this.comboBoxMaterial = new JComboBox<>();
    this.comboBoxMaterial.setModel(new DefaultComboBoxModel<Material>(Material.getMaterials().toArray(new Material[Material.getMaterials().size()])));

    this.comboBoxRotation = new JComboBox<>();
    this.comboBoxRotation.setModel(new DefaultComboBoxModel<>(Rotation.values()));

    this.chckbxShadow = new JCheckBox(Resources.strings().get("panel_prop_shadow"));
    this.checkBoxHorizontalFlip = new JCheckBox(Resources.strings().get("panel_flip_horizontal"));
    this.checkBoxVerticalFlip = new JCheckBox(Resources.strings().get("panel_flip_vertical"));
    this.checkBoxScale = new JCheckBox(Resources.strings().get("panel_stretch_spripte"));

    setLayout(this.createLayout());
    this.setupChangedListeners();
  }

  public static String getIdentifierBySpriteName(String spriteName) {
    if (spriteName == null || spriteName.isEmpty()) {
      return null;
    }

    if (!spriteName.toLowerCase().startsWith(PropAnimationController.PROP_IDENTIFIER)) {
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
    this.chckbxShadow.setSelected(false);
    this.checkBoxScale.setSelected(false);
    this.comboBoxMaterial.setSelectedItem(Material.UNDEFINED);
    this.comboBoxRotation.setSelectedItem(Rotation.NONE);
    this.comboBoxSpriteSheets.setSelectedItem(null);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    selectSpriteSheet(this.comboBoxSpriteSheets, mapObject);

    final Material material = mapObject.getStringValue(MapObjectProperty.PROP_MATERIAL) == null ? Material.UNDEFINED : Material.get(mapObject.getStringValue(MapObjectProperty.PROP_MATERIAL));
    this.comboBoxMaterial.setSelectedItem(material);

    this.comboBoxRotation.setSelectedItem(mapObject.getEnumValue(MapObjectProperty.PROP_ROTATION, Rotation.class, Rotation.NONE));

    this.chckbxShadow.setSelected(mapObject.getBoolValue(MapObjectProperty.PROP_ADDSHADOW));
    this.checkBoxHorizontalFlip.setSelected(mapObject.getBoolValue(MapObjectProperty.PROP_FLIPHORIZONTALLY));
    this.checkBoxVerticalFlip.setSelected(mapObject.getBoolValue(MapObjectProperty.PROP_FLIPVERTICALLY));
    this.checkBoxScale.setSelected(mapObject.getBoolValue(MapObjectProperty.SCALE_SPRITE));
  }

  private void setupChangedListeners() {
    this.setup(this.comboBoxMaterial, MapObjectProperty.PROP_MATERIAL);
    this.setup(this.comboBoxRotation, MapObjectProperty.PROP_ROTATION);
    this.setupL(this.comboBoxSpriteSheets, MapObjectProperty.SPRITESHEETNAME);

    this.setup(this.chckbxShadow, MapObjectProperty.PROP_ADDSHADOW);
    this.setup(this.checkBoxHorizontalFlip, MapObjectProperty.PROP_FLIPHORIZONTALLY);
    this.setup(this.checkBoxVerticalFlip, MapObjectProperty.PROP_FLIPVERTICALLY);
    this.setup(this.checkBoxScale, MapObjectProperty.SCALE_SPRITE);
  }

  private void loadAvailableProps() {
    if (this.propsLoaded) {
      return;
    }
    Map<String, String> m = new TreeMap<>();
    for (Spritesheet s : Resources.spritesheets().getAll()) {
      String spriteName = s.getName();
      String propName = getIdentifierBySpriteName(spriteName);

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

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems = new LayoutItem[] 
        { 
            new LayoutItem("panel_sprite", this.comboBoxSpriteSheets), 
            new LayoutItem("panel_material", this.comboBoxMaterial), 
            new LayoutItem("panel_rotation", this.comboBoxRotation), 
        };

    return this.createLayout(layoutItems, this.checkBoxScale, this.chckbxShadow, this.checkBoxHorizontalFlip, this.checkBoxVerticalFlip);
  }
}
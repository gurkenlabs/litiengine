package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.SpriteVariantSelector;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.renderers.LabelListCellRenderer;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PropPanel extends PropertyPanel {
  private final JComboBox<JLabel> comboBoxSpriteSheets;
  private final JComboBox<JLabel> comboBoxAnimations;
  private final JComboBox<Material> comboBoxMaterial;
  private final JCheckBox chckbxShadow;
  private final JComboBox<Rotation> comboBoxRotation;
  private final JCheckBox checkBoxHorizontalFlip;
  private final JCheckBox checkBoxVerticalFlip;
  private final JCheckBox checkBoxScale;
  private final SpriteAnimationPreview animationPreview;

  private boolean propsLoaded;

  /**
   * Create the panel.
   */
  public PropPanel() {
    super("panel_prop", Icons.PROP_24);
    Resources.images().addClearedListener(() -> this.propsLoaded = false);

    this.comboBoxSpriteSheets = new SearchableSpriteComboBox();
    this.comboBoxSpriteSheets.setRenderer(new LabelListCellRenderer());
    this.comboBoxAnimations = new JComboBox<>();
    this.comboBoxAnimations.setRenderer(new LabelListCellRenderer());
    this.animationPreview = new SpriteAnimationPreview();

    this.comboBoxMaterial = new JComboBox<>();
    this.comboBoxMaterial.setModel(
      new DefaultComboBoxModel<>(Material.getMaterials().toArray(new Material[0])));

    this.comboBoxRotation = new JComboBox<>();
    this.comboBoxRotation.setModel(new DefaultComboBoxModel<>(Rotation.values()));

    this.chckbxShadow = new JCheckBox(Resources.strings().get("panel_prop_shadow"));
    this.checkBoxHorizontalFlip = new JCheckBox(Resources.strings().get("panel_flip_horizontal"));
    this.checkBoxVerticalFlip = new JCheckBox(Resources.strings().get("panel_flip_vertical"));
    this.checkBoxScale = new JCheckBox(Resources.strings().get("panel_stretch_sprite"));
    Resources.spritesheets().addClearedListener(this::clearSpriteCache);

    setLayout(this.createLayout());
    setupChangedListeners();
    this.comboBoxSpriteSheets.addActionListener(e -> refreshAnimationChoices());
    this.comboBoxAnimations.addActionListener(e -> updateSpritePreview());
  }

  private void clearSpriteCache() {
    this.propsLoaded = false;
    this.comboBoxSpriteSheets.removeAllItems();
    this.comboBoxAnimations.removeAllItems();
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
    this.loadAvailableProps();
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
    String storedSprite = mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME, null);
    selectSpriteSheet(this.comboBoxSpriteSheets, mapObject);
    refreshAnimationChoices();
    if (storedSprite != null) {
      selectAnimation(storedSprite);
    }

    var material = Material.UNDEFINED;
    if(mapObject.hasCustomProperty(MapObjectProperty.PROP_MATERIAL)){
      material = Material.get(mapObject.getStringValue(MapObjectProperty.PROP_MATERIAL));
    }

    this.comboBoxMaterial.setSelectedItem(material);

    this.comboBoxRotation.setSelectedItem(
      mapObject.getEnumValue(MapObjectProperty.PROP_ROTATION, Rotation.class, Rotation.NONE));

    this.chckbxShadow.setSelected(mapObject.getBoolValue(MapObjectProperty.PROP_ADDSHADOW, false));
    this.checkBoxHorizontalFlip.setSelected(
        mapObject.getBoolValue(MapObjectProperty.PROP_FLIPHORIZONTALLY, false));
    this.checkBoxVerticalFlip.setSelected(
        mapObject.getBoolValue(MapObjectProperty.PROP_FLIPVERTICALLY, false));
    this.checkBoxScale.setSelected(mapObject.getBoolValue(MapObjectProperty.SCALE_SPRITE, false));
  }

  private void setupChangedListeners() {
    setup(this.comboBoxMaterial, MapObjectProperty.PROP_MATERIAL);
    setup(this.comboBoxRotation, MapObjectProperty.PROP_ROTATION);
    setupL(this.comboBoxSpriteSheets, MapObjectProperty.SPRITESHEETNAME);

    setup(this.chckbxShadow, MapObjectProperty.PROP_ADDSHADOW);
    setup(this.checkBoxHorizontalFlip, MapObjectProperty.PROP_FLIPHORIZONTALLY);
    setup(this.checkBoxVerticalFlip, MapObjectProperty.PROP_FLIPVERTICALLY);
    setup(this.checkBoxScale, MapObjectProperty.SCALE_SPRITE);
  }

  private void loadAvailableProps() {
    if (this.propsLoaded) {
      return;
    }
    // Use reusable selector to get representative sprite per prop base identifier
    Map<String, String> m = SpriteVariantSelector.selectBasePropSpriteNames(Resources.spritesheets().getAll());
    populateComboBoxWithSprites(this.comboBoxSpriteSheets, new TreeMap<>(m)); // TreeMap for sorted order
    this.propsLoaded = true;
  }

  @Override
  public void bindAll(java.util.List<IMapObject> mapObjects) {
    this.loadAvailableProps();
    super.bindAll(mapObjects);
  }

  int getSpriteItemCountForTest() {
    return this.comboBoxSpriteSheets.getItemCount();
  }

  private static JPanel wrapCheckbox(JCheckBox cb) {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    p.setOpaque(false);
    p.add(cb);
    return p;
  }

  private void updateSpritePreview() {
    String name = SearchableSpriteComboBox.selectedText(this.comboBoxSpriteSheets);
    String source = SearchableSpriteComboBox.selectedText(this.comboBoxAnimations);
    if (source == null && name != null) {
      source = SpriteVariantSelector.selectBasePropSpriteNames(Resources.spritesheets().getAll()).get(name);
    }
    var spritesheet = source != null ? Resources.spritesheets().get(source) : null;
    this.animationPreview.setSpritesheet(spritesheet, source);
  }

  private void refreshAnimationChoices() {
    String name = SearchableSpriteComboBox.selectedText(this.comboBoxSpriteSheets);
    Map<String, String> animations = getAnimationSpriteNames(name, Resources.spritesheets().getAll());
    populateComboBoxWithSprites(this.comboBoxAnimations, animations);
    String defaultAnimation = name != null
        ? SpriteVariantSelector.selectBasePropSpriteNames(Resources.spritesheets().getAll()).get(name)
        : null;
    selectAnimation(defaultAnimation);
    updateSpritePreview();
  }

  static Map<String, String> getAnimationSpriteNames(String identifier, Collection<Spritesheet> spritesheets) {
    Map<String, String> animations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if (identifier == null) {
      return animations;
    }
    for (var spritesheet : spritesheets) {
      if (identifier.equalsIgnoreCase(getIdentifierBySpriteName(spritesheet.getName()))) {
        animations.put(spritesheet.getName(), spritesheet.getName());
      }
    }
    return animations;
  }

  private void selectAnimation(String name) {
    for (int i = 0; i < this.comboBoxAnimations.getItemCount(); i++) {
      JLabel item = this.comboBoxAnimations.getItemAt(i);
      if (item != null && item.getText().equalsIgnoreCase(name)) {
        this.comboBoxAnimations.setSelectedItem(item);
        return;
      }
    }
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems =
      new LayoutItem[] {
        new LayoutItem(this.animationPreview, 140),
        new LayoutItem("panel_sprite", this.comboBoxSpriteSheets),
        new LayoutItem("panel_animation", this.comboBoxAnimations),
        new LayoutItem("panel_material", this.comboBoxMaterial),
        new LayoutItem("panel_rotation", this.comboBoxRotation),
      };

    JPanel checkboxGrid = new JPanel(new GridLayout(2, 2, 2, 0));
    checkboxGrid.setOpaque(false);
    checkboxGrid.add(wrapCheckbox(checkBoxScale));
    checkboxGrid.add(wrapCheckbox(chckbxShadow));
    checkboxGrid.add(wrapCheckbox(checkBoxHorizontalFlip));
    checkboxGrid.add(wrapCheckbox(checkBoxVerticalFlip));

    return this.createLayout(layoutItems, checkboxGrid);
  }
}

package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.Editor;
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
  private final JComboBox<Rotation> comboBoxRotation;
  private final JComboBox<PropState> comboBoxState;
  private final JCheckBox chckbxShadow;
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

    this.comboBoxState = new JComboBox<>();
    this.comboBoxState.setModel(new DefaultComboBoxModel<>(PropState.values()));

    this.comboBoxMaterial = new JComboBox<>();
    this.comboBoxMaterial.setModel(
      new DefaultComboBoxModel<>(Material.getMaterials().toArray(new Material[0])));

    this.comboBoxRotation = new JComboBox<>();
    this.comboBoxRotation.setModel(new DefaultComboBoxModel<>(Rotation.values()));

    this.chckbxShadow = ControlBehavior.apply(new JCheckBox(Resources.strings().get("panel_prop_shadow")));
    this.checkBoxHorizontalFlip = ControlBehavior.apply(new JCheckBox(Resources.strings().get("panel_flip_horizontal")));
    this.checkBoxVerticalFlip = ControlBehavior.apply(new JCheckBox(Resources.strings().get("panel_flip_vertical")));
    this.checkBoxScale = ControlBehavior.apply(new JCheckBox(Resources.strings().get("panel_stretch_sprite")));
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

    String identifier = spriteName.substring(PropAnimationController.PROP_IDENTIFIER.length());
    for (PropState state : PropState.values()) {
      String suffix = "-" + state.spriteString();
      if (identifier.toLowerCase().endsWith(suffix.toLowerCase())
          && identifier.length() > suffix.length()) {
        return identifier.substring(0, identifier.length() - suffix.length());
      }
    }
    int variantSeparator = identifier.indexOf('-');
    return variantSeparator >= 0 ? identifier.substring(0, variantSeparator) : identifier;
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
    String identifier = SearchableSpriteComboBox.selectedText(this.comboBoxSpriteSheets);
    refreshAnimationChoices();
    if (storedSprite != null) {
      selectAnimationBySpriteName(storedSprite);
    }

    PropState state = resolvePropState(mapObject);
    this.comboBoxState.setSelectedItem(state);

    // select the animation matching the resolved state
    if (identifier != null && !identifier.isEmpty()) {
      String expectedAnim = PropAnimationController.PROP_IDENTIFIER + identifier + "-" + state.spriteString();
      selectAnimationBySpriteName(expectedAnim);
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

  public static PropState resolvePropState(IMapObject mapObject) {
    int maxHp = mapObject.getIntValue(MapObjectProperty.COMBAT_HITPOINTS, 100);
    int currentHp = mapObject.hasCustomProperty(MapObjectProperty.COMBAT_CURRENT_HITPOINTS)
        ? mapObject.getIntValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS)
        : maxHp;
    boolean indestructible = mapObject.getBoolValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, false);

    if (!indestructible && currentHp <= 0) {
      return PropState.DESTROYED;
    } else if (!indestructible && currentHp <= maxHp * 0.5) {
      return PropState.DAMAGED;
    }
    return PropState.INTACT;
  }

  private void setupChangedListeners() {
    setup(this.comboBoxMaterial, MapObjectProperty.PROP_MATERIAL);
    setup(this.comboBoxRotation, MapObjectProperty.PROP_ROTATION);
    setupL(this.comboBoxSpriteSheets, MapObjectProperty.SPRITESHEETNAME);

    this.comboBoxState.addActionListener(
      new MapObjectPropertyActionListener(
        m -> {
          PropState state = (PropState) this.comboBoxState.getSelectedItem();
          return state != null && state != resolvePropState(m);
        },
        m -> {
          PropState state = (PropState) this.comboBoxState.getSelectedItem();
          if (state == null) {
            return;
          }
          int maxHp = m.getIntValue(MapObjectProperty.COMBAT_HITPOINTS, 100);
          switch (state) {
            case INTACT -> m.removeProperty(MapObjectProperty.COMBAT_CURRENT_HITPOINTS);
            case DAMAGED -> {
              m.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, false);
              m.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, Math.max(1, (int) (Math.max(1, maxHp) * 0.5)));
            }
            case DESTROYED -> {
              m.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, false);
              m.setValue(MapObjectProperty.COMBAT_CURRENT_HITPOINTS, 0);
            }
          }
        }
      )
    );

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

  String getSelectedSpriteForTest() {
    return SearchableSpriteComboBox.selectedText(this.comboBoxSpriteSheets);
  }

  private static JPanel wrapCheckbox(JCheckBox cb) {
    ControlBehavior.apply(cb);
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    p.setOpaque(false);
    p.add(cb);
    return p;
  }

  private void updateSpritePreview() {
    String name = SearchableSpriteComboBox.selectedText(this.comboBoxSpriteSheets);
    // resolve actual sprite name from the selected animation label
    String source = null;
    JLabel selectedAnim = (JLabel) this.comboBoxAnimations.getSelectedItem();
    if (selectedAnim != null) {
      source = selectedAnim.getName() != null ? selectedAnim.getName() : selectedAnim.getText();
    }
    if (source == null && name != null) {
      source = SpriteVariantSelector.selectBasePropSpriteNames(Resources.spritesheets().getAll()).get(name);
    }
    var spritesheet = source != null ? Resources.spritesheets().get(source) : null;
    this.animationPreview.setSpritesheet(spritesheet, source);
  }

  private void refreshAnimationChoices() {
    String name = SearchableSpriteComboBox.selectedText(this.comboBoxSpriteSheets);
    Map<String, String> animations = getAnimationSpriteNames(name, Resources.spritesheets().getAll());
    Map<String, String> display = toDisplayNames(name, animations);
    populateComboBoxWithSpritesAndNames(this.comboBoxAnimations, display, animations);
    String defaultAnimation = name != null
        ? SpriteVariantSelector.selectBasePropSpriteNames(Resources.spritesheets().getAll()).get(name)
        : null;
    selectAnimationBySpriteName(defaultAnimation);
    updateSpritePreview();
  }

  /**
   * Populates the animation combo box with display names, storing the actual sprite name
   * in each JLabel's {@code name} property for later resolution.
   */
  private static void populateComboBoxWithSpritesAndNames(
      JComboBox<JLabel> comboBox, Map<String, String> display, Map<String, String> fullNames) {
    comboBox.removeAllItems();
    display.forEach((displayName, spriteName) -> {
      JLabel label = new JLabel(displayName);
      label.setName(spriteName); // store full sprite name for resolution
      Spritesheet spritesheet = Resources.spritesheets().get(spriteName);
      if (spritesheet != null && spritesheet.getTotalNumberOfSprites() > 0) {
        java.awt.image.BufferedImage preview = spritesheet.getPreview(24);
        if (preview != null) {
          label.setIcon(new javax.swing.ImageIcon(preview));
        }
      }
      comboBox.addItem(label);
    });
  }

  /**
   * Converts full sprite names to clean display names for the animation dropdown.
   * Strips the "prop-{identifier}-" prefix, showing just the variant/state name (e.g. "intact", "damaged").
   */
  static Map<String, String> toDisplayNames(String identifier, Map<String, String> animations) {
    if (identifier == null) {
      return animations;
    }
    String prefix = (PropAnimationController.PROP_IDENTIFIER + identifier).toLowerCase();
    Map<String, String> display = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (Map.Entry<String, String> entry : animations.entrySet()) {
      String spriteName = entry.getValue();
      String displayName;
      if (spriteName.toLowerCase().startsWith(prefix) && spriteName.length() > prefix.length()) {
        // strip "prop-barrel1-" to get "damaged", "destroyed", "intact", etc.
        displayName = spriteName.substring(prefix.length());
        if (displayName.startsWith("-")) {
          displayName = displayName.substring(1);
        }
      } else {
        displayName = spriteName;
      }
      display.put(displayName, spriteName);
    }
    return display;
  }

  static Map<String, String> getAnimationSpriteNames(String identifier, Collection<Spritesheet> spritesheets) {
    Map<String, String> animations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    if (identifier == null) {
      return animations;
    }
    String prefix = PropAnimationController.PROP_IDENTIFIER + identifier;
    for (Spritesheet sprite : spritesheets) {
      if (sprite.getName() != null && sprite.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
        animations.put(sprite.getName(), sprite.getName());
      }
    }

    if (animations.isEmpty()) {
      String source = SpriteVariantSelector.selectBasePropSpriteNames(Resources.spritesheets().getAll()).get(identifier);
      if (source != null) {
        animations.put(source, source);
      }
    }
    return animations;
  }

  /**
   * Selects the animation entry whose actual sprite name (stored in JLabel.getName()) matches.
   */
  private void selectAnimationBySpriteName(String spriteName) {
    if (spriteName == null) {
      return;
    }
    for (int i = 0; i < this.comboBoxAnimations.getItemCount(); i++) {
      JLabel item = this.comboBoxAnimations.getItemAt(i);
      if (item != null) {
        String itemSpriteName = item.getName() != null ? item.getName() : item.getText();
        if (itemSpriteName.equalsIgnoreCase(spriteName)) {
          this.comboBoxAnimations.setSelectedItem(item);
          return;
        }
      }
    }
  }

  private LayoutManager createLayout() {
    LayoutItem[] layoutItems =
      new LayoutItem[] {
        new LayoutItem(this.animationPreview, 140),
        new LayoutItem("panel_sprite", this.comboBoxSpriteSheets),
        new LayoutItem("panel_animation", this.comboBoxAnimations),
        new LayoutItem("panel_state", this.comboBoxState),
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

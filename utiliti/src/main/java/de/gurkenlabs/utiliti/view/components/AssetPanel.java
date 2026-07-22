package de.gurkenlabs.utiliti.view.components;

import static java.awt.FlowLayout.LEFT;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterAttributes;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.utiliti.controller.WrapLayout;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.menus.AssetPanelPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class AssetPanel extends JPanel {

  public enum AssetType {
    SPRITESHEET, TILESET, EMITTER, BLUEPRINT, SOUND, ANIMATION
  }

  private AssetType currentType;
  private String filterText = "";
  private boolean compact;
  private final List<AssetPanelItem> allItems = new ArrayList<>();
  private final LinkedHashSet<AssetPanelItem> selectedItems = new LinkedHashSet<>();
  private int visibleItemCount;
  private Runnable changedCallback;
  private int cardSize = 118;
  private AssetPanelItem focusedItem;
  private AssetPanelItem selectionAnchor;

  public AssetPanel() {
    this.setLayout(createLayout());
    this.setBorder(new EmptyBorder(
      Style.SPACE_MEDIUM, Style.SPACE_MEDIUM, Style.SPACE_MEDIUM, Style.SPACE_MEDIUM));
    this.setBackground(Style.assetExplorerBackground());
    this.setOpaque(true);

    MouseAdapter popupHandler = new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
          clearSelection();
        }
        maybeShowPopup(e);
      }

      @Override public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
      }
    };
    this.addMouseListener(popupHandler);
  }

  @Override
  public void updateUI() {
    super.updateUI();
    setBackground(Style.assetExplorerBackground());
  }

  public AssetType getCurrentType() {
    return currentType;
  }

  public void setFilterText(String text) {
    this.filterText = text != null ? text.toLowerCase(Locale.ROOT).trim() : "";
    applyFilter();
  }

  public void setCompact(boolean compact) {
    if (this.compact == compact) {
      return;
    }
    this.compact = compact;
    this.setLayout(createLayout());
    for (AssetPanelItem item : allItems) {
      item.setCompact(compact);
      item.setCardSize(this.cardSize);
    }
    applyFilter();
  }

  public void setCardSize(int cardSize) {
    this.cardSize = cardSize;
    for (AssetPanelItem item : this.allItems) {
      item.setCardSize(cardSize);
    }
    revalidate();
    repaint();
  }

  public boolean isCompact() {
    return compact;
  }

  private java.awt.LayoutManager createLayout() {
    if (compact) {
      return new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS);
    }
    WrapLayout layout = new WrapLayout();
    layout.setVgap(Style.SPACE_MEDIUM);
    layout.setHgap(Style.SPACE_MEDIUM);
    layout.setAlignment(LEFT);
    return layout;
  }

  private void applyFilter() {
    this.removeAll();
    this.visibleItemCount = 0;
    for (AssetPanelItem item : allItems) {
      String name = item.getName();
      if (filterText.isEmpty() || name != null && name.toLowerCase(Locale.ROOT).contains(filterText)) {
        this.add(item);
        this.visibleItemCount++;
        item.validate();
      }
    }
    List<AssetPanelItem> visibleItems = visibleItems();
    this.selectedItems.removeIf(item -> !visibleItems.contains(item));
    if (!visibleItems.contains(this.selectionAnchor)) {
      this.selectionAnchor = null;
    }
    if (!visibleItems.contains(this.focusedItem)) {
      this.focusedItem = null;
    }
    updateSelectedStates();
    this.revalidate();
    this.repaint();
    if (this.changedCallback != null) {
      this.changedCallback.run();
    }
  }

  public void setChangedCallback(Runnable changedCallback) {
    this.changedCallback = changedCallback;
  }

  public int getVisibleItemCount() {
    return this.visibleItemCount;
  }

  public int getTotalItemCount() {
    return this.allItems.size();
  }

  public AssetPanelItem getFocusedItem() {
    return this.focusedItem;
  }

  public List<AssetPanelItem> getSelectedItems() {
    return this.allItems.stream().filter(this.selectedItems::contains).toList();
  }

  public List<Object> getSelectedOrigins() {
    return getSelectedItems().stream().map(AssetPanelItem::getOrigin).toList();
  }

  public String getCurrentTitle() {
    if (this.currentType == null) {
      return Resources.strings().get("assettree_assets");
    }
    return switch (this.currentType) {
      case SPRITESHEET -> Resources.strings().get("assettree_spritesheets");
      case TILESET -> Resources.strings().get("assettree_tilesets");
      case EMITTER -> Resources.strings().get("assettree_emitters");
      case BLUEPRINT -> Resources.strings().get("assettree_blueprints");
      case SOUND -> Resources.strings().get("assettree_sounds");
      case ANIMATION -> Resources.strings().get("assettree_animations");
    };
  }

  private void maybeShowPopup(MouseEvent e) {
    if (!e.isPopupTrigger()) {
      return;
    }
    new AssetPanelPopupMenu(currentType).show(this, e.getX(), e.getY());
  }

  public void loadSprites(List<SpritesheetResource> infos) {
    this.currentType = AssetType.SPRITESHEET;
    loadItems(() -> {
      for (SpritesheetResource info : infos.stream().sorted().toList()) {
        Icon icon;
        Spritesheet opt = Resources.spritesheets().get(info.getName());

        if (opt != null && opt.getSprite(0) != null) {
          icon = new ImageIcon(opt.getPreview(64));
        } else {
          icon = null;
        }

        allItems.add(createItem(icon, getDisplayName(info), info));
      }
    });
  }

  public void loadTilesets(List<Tileset> tilesets) {
    this.currentType = AssetType.TILESET;
    loadItems(() -> {
      Collections.sort(tilesets);
      for (Tileset tileset : tilesets) {
        allItems.add(
          createItem(Icons.ASSET_TILESET_32, tileset.getName(), tileset));
      }
    });
  }

  public void loadEmitters(List<EmitterAttributes> emitters) {
    this.currentType = AssetType.EMITTER;
    loadItems(() -> {
      Collections.sort(emitters);
      for (EmitterAttributes emitter : emitters) {
        allItems.add(
          createItem(Icons.ASSET_EMITTER_32, emitter.getName(), emitter));
      }
    });
  }

  public void loadBlueprints(List<Blueprint> blueprints) {
    this.currentType = AssetType.BLUEPRINT;
    loadItems(() -> {
      Collections.sort(blueprints);
      for (MapObject blueprint : blueprints) {
        allItems.add(
          createItem(Icons.ASSET_BLUEPRINT_32, blueprint.getName(), blueprint));
      }
    });
  }

  public void loadSounds(List<SoundResource> sounds) {
    this.currentType = AssetType.SOUND;
    loadItems(() -> {
      Collections.sort(sounds);
      for (SoundResource sound : sounds) {
        allItems.add(
          createItem(Icons.ASSET_SOUND_32, sound.getName(), sound));
      }
    });
  }

  public void loadAnimations(List<Animation> animations) {
    this.currentType = AssetType.ANIMATION;
    loadItems(() -> {
      animations.sort((a, b) -> {
        String nameA = a.getName() == null ? "" : a.getName();
        String nameB = b.getName() == null ? "" : b.getName();
        return nameA.compareToIgnoreCase(nameB);
      });
      for (Animation animation : animations) {
        Icon icon = Icons.ASSET_ANIMATION_32;
        Spritesheet sheet = animation.getSpritesheet();
        if (sheet != null && sheet.getSprite(0) != null) {
          icon = new ImageIcon(sheet.getPreview(64));
        }
        allItems.add(createItem(icon, animation.getName(), animation));
      }
    });
  }

  public void clearAssets() {
    this.currentType = null;
    loadItems(() -> {});
  }

  private void loadItems(Runnable runnable) {
    allItems.clear();
    selectedItems.clear();
    selectionAnchor = null;
    focusedItem = null;
    this.removeAll();
    runnable.run();
    applyFilter();
  }

  private AssetPanelItem createItem(Icon icon, String name, Object origin) {
    AssetPanelItem item = new AssetPanelItem(icon, name, origin);
    item.setCompact(this.compact);
    item.setCardSize(this.cardSize);
    item.setFocusCallback(focused -> {
      this.focusedItem = focused;
      updateSelectedStates();
      if (this.changedCallback != null) {
        this.changedCallback.run();
      }
    });
    item.setSelectionCallbacks(
        event -> handleSelectionPressed(item, event),
        event -> handleSelectionClicked(item, event));
    item.setTransferAssetsSupplier(() -> {
      List<Object> selection = getSelectedOrigins();
      return selection.isEmpty() ? List.of(item.getOrigin()) : selection;
    });
    return item;
  }

  private void handleSelectionPressed(AssetPanelItem item, MouseEvent event) {
    if (!javax.swing.SwingUtilities.isLeftMouseButton(event) && !event.isPopupTrigger()) {
      return;
    }
    boolean menuShortcut = (event.getModifiersEx() & menuShortcutMask()) != 0;
    boolean shift = event.isShiftDown();
    if (shift) {
      selectRange(item, menuShortcut);
    } else if (menuShortcut) {
      toggleSelection(item);
    } else if (!this.selectedItems.contains(item)) {
      selectOnly(item);
    }
    this.focusedItem = item;
  }

  private void handleSelectionClicked(AssetPanelItem item, MouseEvent event) {
    if (javax.swing.SwingUtilities.isLeftMouseButton(event)
        && (event.getModifiersEx() & menuShortcutMask()) == 0 && !event.isShiftDown()) {
      selectOnly(item);
    }
  }

  private void selectOnly(AssetPanelItem item) {
    this.selectedItems.clear();
    this.selectedItems.add(item);
    this.selectionAnchor = item;
    this.focusedItem = item;
    selectionChanged();
  }

  private void toggleSelection(AssetPanelItem item) {
    if (!this.selectedItems.remove(item)) {
      this.selectedItems.add(item);
    }
    this.selectionAnchor = item;
    this.focusedItem = item;
    selectionChanged();
  }

  private void selectRange(AssetPanelItem item, boolean additive) {
    List<AssetPanelItem> visible = visibleItems();
    AssetPanelItem anchor = visible.contains(this.selectionAnchor) ? this.selectionAnchor : item;
    int start = visible.indexOf(anchor);
    int end = visible.indexOf(item);
    if (!additive) {
      this.selectedItems.clear();
    }
    for (int i = Math.min(start, end); i <= Math.max(start, end); i++) {
      this.selectedItems.add(visible.get(i));
    }
    this.focusedItem = item;
    selectionChanged();
  }

  private void clearSelection() {
    this.selectedItems.clear();
    this.selectionAnchor = null;
    this.focusedItem = null;
    selectionChanged();
  }

  private List<AssetPanelItem> visibleItems() {
    return java.util.Arrays.stream(getComponents())
        .filter(AssetPanelItem.class::isInstance)
        .map(AssetPanelItem.class::cast)
        .toList();
  }

  private void selectionChanged() {
    updateSelectedStates();
    if (this.changedCallback != null) {
      this.changedCallback.run();
    }
  }

  private void updateSelectedStates() {
    boolean individualActionsEnabled = this.selectedItems.size() <= 1;
    for (AssetPanelItem item : this.allItems) {
      item.setSelected(this.selectedItems.contains(item));
      item.setFocused(item == this.focusedItem);
      item.setIndividualActionsEnabled(individualActionsEnabled);
    }
  }

  private static String getDisplayName(SpritesheetResource info) {
    if (info == null || info.getName() == null) {
      return "";
    }
    String name = info.getName();

    // Prop base name: remove leading 'prop-' and any state suffix (-intact, -broken, etc.) retaining identifier only
    if (name.startsWith("prop-")) {
      String identifier = PropPanel.getIdentifierBySpriteName(name);
      if (identifier != null) {
        return identifier; // always just the identifier for props
      }
    }

    // Creature base name: use base part before first dash if recognized as creature sprite
    String creatureBase = CreaturePanel.getCreatureSpriteName(name);
    if (creatureBase != null) {
      return creatureBase;
    }

    // default: original name (e.g., misc sprites not following prop/creature conventions)
    return name;
  }

  List<AssetPanelItem> getItemsForTest() {
    return List.copyOf(this.allItems);
  }

  void selectItemForTest(int index, boolean menuShortcut, boolean shift) {
    AssetPanelItem item = this.allItems.get(index);
    int modifiers = (menuShortcut ? menuShortcutMask() : 0)
        | (shift ? MouseEvent.SHIFT_DOWN_MASK : 0);
    handleSelectionPressed(item, new MouseEvent(
        item, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), modifiers,
        1, 1, 1, false, MouseEvent.BUTTON1));
  }

  static int menuShortcutMask() {
    try {
      return java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    } catch (java.awt.HeadlessException ignored) {
      return MouseEvent.CTRL_DOWN_MASK;
    }
  }
}

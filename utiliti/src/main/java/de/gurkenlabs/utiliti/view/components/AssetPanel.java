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
import de.gurkenlabs.utiliti.view.menus.AssetPanelPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

  public AssetPanel() {
    this.setLayout(createLayout());
    this.setBorder(new EmptyBorder(5, 5, 5, 5));

    MouseAdapter popupHandler = new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        maybeShowPopup(e);
      }

      @Override public void mouseReleased(MouseEvent e) {
        maybeShowPopup(e);
      }
    };
    this.addMouseListener(popupHandler);
  }

  public AssetType getCurrentType() {
    return currentType;
  }

  public void setFilterText(String text) {
    this.filterText = text != null ? text.toLowerCase().trim() : "";
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
    }
    applyFilter();
  }

  public boolean isCompact() {
    return compact;
  }

  private java.awt.LayoutManager createLayout() {
    if (compact) {
      return new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS);
    }
    WrapLayout layout = new WrapLayout();
    layout.setVgap(5);
    layout.setHgap(5);
    layout.setAlignment(LEFT);
    return layout;
  }

  private void applyFilter() {
    this.removeAll();
    for (AssetPanelItem item : allItems) {
      if (filterText.isEmpty() || item.getName().toLowerCase().contains(filterText)) {
        this.add(item);
        item.validate();
      }
    }
    this.revalidate();
    this.repaint();
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

        allItems.add(new AssetPanelItem(icon, getDisplayName(info), info));
      }
    });
  }

  public void loadTilesets(List<Tileset> tilesets) {
    this.currentType = AssetType.TILESET;
    loadItems(() -> {
      Collections.sort(tilesets);
      for (Tileset tileset : tilesets) {
        allItems.add(
          new AssetPanelItem(Icons.ASSET_TILESET_32, tileset.getName(), tileset));
      }
    });
  }

  public void loadEmitters(List<EmitterAttributes> emitters) {
    this.currentType = AssetType.EMITTER;
    loadItems(() -> {
      Collections.sort(emitters);
      for (EmitterAttributes emitter : emitters) {
        allItems.add(
          new AssetPanelItem(Icons.ASSET_EMITTER_32, emitter.getName(), emitter));
      }
    });
  }

  public void loadBlueprints(List<Blueprint> blueprints) {
    this.currentType = AssetType.BLUEPRINT;
    loadItems(() -> {
      Collections.sort(blueprints);
      for (MapObject blueprint : blueprints) {
        allItems.add(
          new AssetPanelItem(Icons.ASSET_BLUEPRINT_32, blueprint.getName(), blueprint));
      }
    });
  }

  public void loadSounds(List<SoundResource> sounds) {
    this.currentType = AssetType.SOUND;
    loadItems(() -> {
      Collections.sort(sounds);
      for (SoundResource sound : sounds) {
        allItems.add(
          new AssetPanelItem(Icons.ASSET_SOUND_32, sound.getName(), sound));
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
        allItems.add(new AssetPanelItem(icon, animation.getName(), animation));
      }
    });
  }

  private void loadItems(Runnable runnable) {
    allItems.clear();
    this.removeAll();
    runnable.run();
    applyFilter();
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
}

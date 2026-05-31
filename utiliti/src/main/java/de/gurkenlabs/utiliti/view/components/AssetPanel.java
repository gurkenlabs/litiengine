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

  public AssetPanel() {
    WrapLayout layout = new WrapLayout();
    layout.setVgap(5);
    layout.setHgap(5);
    layout.setAlignment(LEFT);
    this.setLayout(layout);

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

  private void maybeShowPopup(MouseEvent e) {
    if (!e.isPopupTrigger()) {
      return;
    }
    new AssetPanelPopupMenu(currentType).show(this, e.getX(), e.getY());
  }

  public void loadSprites(List<SpritesheetResource> infos) {
    this.currentType = AssetType.SPRITESHEET;
    this.load(
      () -> {
        for (SpritesheetResource info : infos.stream().sorted().toList()) {
          Icon icon;
          Spritesheet opt = Resources.spritesheets().get(info.getName());

          if (opt != null && opt.getSprite(0) != null) {
            icon = new ImageIcon(opt.getPreview(64));
          } else {
            icon = null;
          }

          AssetPanelItem panelItem = new AssetPanelItem(icon, getDisplayName(info), info);
          this.add(panelItem);
          panelItem.validate();
        }
      });
  }

  public void loadTilesets(List<Tileset> tilesets) {
    this.currentType = AssetType.TILESET;
    this.load(
      () -> {
        Collections.sort(tilesets);
        for (Tileset tileset : tilesets) {
          AssetPanelItem panelItem =
            new AssetPanelItem(Icons.ASSET_TILESET_32, tileset.getName(), tileset);
          this.add(panelItem);
          panelItem.validate();
        }
      });
  }

  public void loadEmitters(List<EmitterAttributes> emitters) {
    this.currentType = AssetType.EMITTER;
    this.load(
      () -> {
        Collections.sort(emitters);
        for (EmitterAttributes emitter : emitters) {
          AssetPanelItem panelItem =
            new AssetPanelItem(Icons.ASSET_EMITTER_32, emitter.getName(), emitter);
          this.add(panelItem);
          panelItem.validate();
        }
      });
  }

  public void loadBlueprints(List<Blueprint> blueprints) {
    this.currentType = AssetType.BLUEPRINT;
    this.load(
      () -> {
        Collections.sort(blueprints);
        for (MapObject blueprint : blueprints) {
          AssetPanelItem panelItem =
            new AssetPanelItem(Icons.ASSET_BLUEPRINT_32, blueprint.getName(), blueprint);
          this.add(panelItem);
          panelItem.validate();
        }
      });
  }

  public void loadSounds(List<SoundResource> sounds) {
    this.currentType = AssetType.SOUND;
    this.load(
      () -> {
        Collections.sort(sounds);
        for (SoundResource sound : sounds) {
          AssetPanelItem panelItem = new AssetPanelItem(Icons.ASSET_SOUND_32, sound.getName(), sound);
          this.add(panelItem);
          panelItem.validate();
        }
      });
  }

  /**
   * Populates this panel with the given animations.
   *
   * <p>
   * Each animation is rendered using its first sprite as a preview icon (falling back to the
   * generic animation icon if the sprite sheet is not available).
   * </p>
   *
   * @param animations The animations to display.
   */
  public void loadAnimations(List<Animation> animations) {
    this.currentType = AssetType.ANIMATION;
    this.load(
      () -> {
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
          AssetPanelItem panelItem = new AssetPanelItem(icon, animation.getName(), animation);
          this.add(panelItem);
          panelItem.validate();
        }
      });
  }

  public void load(Runnable runnable) {
    this.removeAll();
    runnable.run();
    this.getRootPane().repaint();
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

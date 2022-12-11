package de.gurkenlabs.utiliti.swing;

import static java.awt.FlowLayout.LEFT;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.Imaging;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class AssetPanel extends JPanel {

  public AssetPanel() {
    WrapLayout layout = new WrapLayout();
    layout.setVgap(5);
    layout.setHgap(5);
    layout.setAlignment(LEFT);
    this.setLayout(layout);

    this.setBorder(new EmptyBorder(5, 5, 5, 5));
  }

  public void loadSprites(List<SpritesheetResource> infos) {
    this.load(
        () -> {
          for (SpritesheetResource info : infos.stream().sorted().toList()) {
            Icon icon;
            Spritesheet opt = Resources.spritesheets().get(info.getName());

            if (opt != null && opt.getSprite(0) != null) {
              icon = new ImageIcon(Imaging.scale(opt.getSprite(0), 64, 64, true));
            } else {
              icon = null;
            }

            AssetPanelItem panelItem =
                new AssetPanelItem(icon, getResourceNameWithoutPrefix(info), info);
            this.add(panelItem);
            panelItem.validate();
          }
        });
  }

  public void loadTilesets(List<Tileset> tilesets) {
    this.load(
        () -> {
          Collections.sort(tilesets);
          for (Tileset tileset : tilesets) {
            AssetPanelItem panelItem =
                new AssetPanelItem(Icons.DOC_TILESET, tileset.getName(), tileset);
            this.add(panelItem);
            panelItem.validate();
          }
        });
  }

  public void loadEmitters(List<EmitterData> emitters) {
    this.load(
        () -> {
          Collections.sort(emitters);
          for (EmitterData emitter : emitters) {
            AssetPanelItem panelItem =
                new AssetPanelItem(Icons.DOC_EMITTER, emitter.getName(), emitter);
            this.add(panelItem);
            panelItem.validate();
          }
        });
  }

  public void loadBlueprints(List<Blueprint> blueprints) {
    this.load(
        () -> {
          Collections.sort(blueprints);
          for (MapObject blueprint : blueprints) {
            AssetPanelItem panelItem =
                new AssetPanelItem(Icons.DOC_BLUEPRINT, blueprint.getName(), blueprint);
            this.add(panelItem);
            panelItem.validate();
          }
        });
  }

  public void loadSounds(List<SoundResource> sounds) {
    this.load(
        () -> {
          Collections.sort(sounds);
          for (SoundResource sound : sounds) {
            AssetPanelItem panelItem = new AssetPanelItem(Icons.DOC_SOUND, sound.getName(), sound);
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

  private static String getResourceNameWithoutPrefix(SpritesheetResource info) {
    return info.getName().startsWith("prop-")
        ? info.getName().replace("prop-", "")
        : info.getName();
  }
}

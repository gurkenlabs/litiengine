package de.gurkenlabs.utiliti.swing;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.particles.xml.EmitterData;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.utiliti.Icons;

@SuppressWarnings("serial")
public class AssetPanel extends JPanel {
  public static final Color BACKGROUND = new Color(24, 24, 24);
  private static final int COLUMNS = 10;
  private final GridLayout gridLayout;

  public AssetPanel() {
    this.gridLayout = new GridLayout(3, COLUMNS);
    this.gridLayout.setVgap(5);
    this.gridLayout.setHgap(5);
    this.setLayout(this.gridLayout);

    this.setBorder(new EmptyBorder(5, 5, 5, 5));
    this.setBackground(BACKGROUND);

    // TODO: implement support for arrow keys to change focus
  }

  public void loadSprites(List<SpriteSheetInfo> infos) {
    this.load(infos, () -> {
      Collections.sort(infos);
      for (SpriteSheetInfo info : infos) {
        Icon icon;
        Spritesheet sprite = Spritesheet.find(info.getName());

        if (sprite == null || sprite.getSprite(0) == null) {
          icon = null;
        } else {
          icon = new ImageIcon(ImageProcessing.scaleImage(sprite.getSprite(0), 64, 64, true));
        }

        AssetPanelItem panelItem = new AssetPanelItem(icon, info.getName(), info);
        this.add(panelItem);
        panelItem.validate();
      }
    });
  }

  public void loadTilesets(List<Tileset> tilesets) {
    this.load(tilesets, () -> {
      Collections.sort(tilesets);
      for (Tileset tileset : tilesets) {
        AssetPanelItem panelItem = new AssetPanelItem(Icons.DOC_TILESET, tileset.getName(), tileset);
        this.add(panelItem);
        panelItem.validate();
      }
    });
  }

  public void loadEmitters(List<EmitterData> emitters) {
    this.load(emitters, () -> {
      Collections.sort(emitters);
      for (EmitterData emitter : emitters) {
        AssetPanelItem panelItem = new AssetPanelItem(Icons.DOC_EMITTER, emitter.getName(), emitter);
        this.add(panelItem);
        panelItem.validate();
      }
    });
  }

  public void loadBlueprints(List<Blueprint> blueprints) {
    this.load(blueprints, () -> {
      Collections.sort(blueprints);
      for (MapObject blueprint : blueprints) {
        AssetPanelItem panelItem = new AssetPanelItem(Icons.DOC_BLUEPRINT, blueprint.getName(), blueprint);
        this.add(panelItem);
        panelItem.validate();
      }
    });
  }

  public <T> void load(List<T> list, Runnable runnable) {
    this.removeAll();
    this.gridLayout.setRows(Math.max(list.size() / COLUMNS, 2));

    runnable.run();

    if (list.size() < COLUMNS * 2) {
      for (int i = 0; i < COLUMNS * 2 - list.size(); i++) {
        JPanel placeholder = new JPanel();
        placeholder.setOpaque(false);
        this.add(placeholder);
      }
    }

    this.getRootPane().repaint();
  }
}

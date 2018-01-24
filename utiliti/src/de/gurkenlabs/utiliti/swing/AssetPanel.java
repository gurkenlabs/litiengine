package de.gurkenlabs.utiliti.swing;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;

public class AssetPanel extends JPanel {
  private static final int COLUMNS = 10;
  private static final Icon emptyDoc = new ImageIcon(Resources.getImage("document_64.png"));
  private static final Icon tilesetIcon = new ImageIcon(Resources.getImage("document-tsx.png"));
  private final GridLayout gridLayout;

  public AssetPanel() {
    this.gridLayout = new GridLayout(3, COLUMNS);
    this.gridLayout.setVgap(5);
    this.gridLayout.setHgap(5);
    this.setLayout(this.gridLayout);

    this.setBorder(new EmptyBorder(5, 5, 5, 5));
    this.setBackground(Color.DARK_GRAY);

    // TODO: implement support for arrow keys to change focus
  }

  public void loadSprites(List<SpriteSheetInfo> infos) {
    this.load(infos, () -> {
      for (SpriteSheetInfo info : infos) {
        Icon icon;
        Spritesheet sprite = Spritesheet.find(info.getName());

        if (sprite == null) {
          icon = emptyDoc;
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
      for (Tileset tileset : tilesets) {
        AssetPanelItem panelItem = new AssetPanelItem(tilesetIcon, tileset.getName(), tileset);
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

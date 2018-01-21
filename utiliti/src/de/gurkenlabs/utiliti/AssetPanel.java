package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.util.ImageProcessing;

public class AssetPanel extends JPanel {
  private static final long serialVersionUID = 140092523457098565L;
  private static final int COLUMNS = 10;
  private static final Icon emptyDoc = new ImageIcon(Resources.getImage("document_64.png"));
  private final GridLayout gridLayout;

  public AssetPanel() {
    this.gridLayout = new GridLayout(3, COLUMNS);
    this.gridLayout.setVgap(5);
    this.gridLayout.setHgap(5);
    this.setLayout(this.gridLayout);

    this.setBorder(new EmptyBorder(5, 5, 5, 5));
    this.setBackground(Color.DARK_GRAY);
  }

  public void load(List<SpriteSheetInfo> infos) {
    this.removeAll();
    this.gridLayout.setRows(infos.size() / COLUMNS);

    for (SpriteSheetInfo info : infos) {
      Icon icon;
      Spritesheet sprite = Spritesheet.find(info.getName());

      if (sprite == null) {
        icon = emptyDoc;
      } else {
        icon = new ImageIcon(ImageProcessing.scaleImage(sprite.getSprite(0), 64, 64, true));
      }

      AssetPanelItem test = new AssetPanelItem(icon, info.getName(), info);
      this.add(test);
      test.validate();
    }

    this.getRootPane().repaint();
  }
}

package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

final class SpriteAnimationPreview extends JPanel {
  private final JLabel preview = new JLabel("", SwingConstants.CENTER);
  private final Timer timer = new Timer(120, _ -> update());
  private Spritesheet spritesheet;

  SpriteAnimationPreview() {
    super(new BorderLayout());
    setOpaque(false);
    this.preview.setOpaque(true);
    this.preview.setBackground(Style.COLOR_SURFACE);
    this.preview.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));
    this.preview.setPreferredSize(new Dimension(0, 112));
    add(this.preview, BorderLayout.CENTER);
  }

  @Override public void addNotify() {
    super.addNotify();
    this.timer.start();
  }

  @Override public void removeNotify() {
    this.timer.stop();
    super.removeNotify();
  }

  void setSpritesheet(Spritesheet spritesheet) {
    this.spritesheet = spritesheet;
    update();
  }

  private void update() {
    if (this.spritesheet == null || this.spritesheet.getTotalNumberOfSprites() == 0) {
      this.preview.setIcon(null);
      return;
    }
    int frame = (int) ((System.currentTimeMillis() / 120) % this.spritesheet.getTotalNumberOfSprites());
    var image = this.spritesheet.getSprite(frame);
    var scaled = image != null ? Imaging.scale(image, 96, 96, true) : null;
    this.preview.setIcon(scaled != null ? new ImageIcon(scaled) : null);
  }

  javax.swing.Icon getIconForTest() {
    return this.preview.getIcon();
  }
}

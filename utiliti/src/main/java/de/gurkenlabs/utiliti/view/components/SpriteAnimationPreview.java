package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
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
  private Spritesheet spritesheet;

  SpriteAnimationPreview() {
    super(new BorderLayout());
    setOpaque(false);
    this.preview.setOpaque(true);
    this.preview.setBackground(Style.COLOR_SURFACE);
    this.preview.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));
    this.preview.setPreferredSize(new Dimension(0, 112));
    add(this.preview, BorderLayout.CENTER);
    new Timer(120, e -> update()).start();
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
    this.preview.setIcon(image != null ? new ImageIcon(image.getScaledInstance(96, 96, java.awt.Image.SCALE_SMOOTH)) : null);
  }
}

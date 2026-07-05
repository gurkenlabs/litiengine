package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.resources.Resources;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class AmbientLightPreviewPanel extends JPanel {
  private Color ambientColor = AmbientLight.DEFAULT_COLOR;
  private Color staticShadowColor = StaticShadow.DEFAULT_COLOR;
  private Image previewImage;
  private boolean previewImageLoadFailed;

  public AmbientLightPreviewPanel() {

    this.setMinimumSize(new Dimension(300, 116));
    this.setPreferredSize(new Dimension(300, 116));
    this.setSize(new Dimension(300, 116));
    this.setBorder(BorderFactory.createLineBorder(Color.black));
  }

  /**
   * Sets the ambient color of this panel.<br>
   * A null argument is silently ignored.
   *
   * @param color
   *          The color to set as ambient color.
   */
  public void setAmbientColor(Color color) {
    if (color == null) {
      return;
    }

    this.ambientColor = color;
    this.repaint();
  }

  /**
   * Sets the ambient color of this panel.<br>
   * A null argument is silently ignored.
   *
   * @param color
   *          The color to set as ambient color.
   */
  public void setStaticShadowColor(Color color) {
    if (color == null) {
      return;
    }

    this.staticShadowColor = color;
    this.repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Image preview = getPreviewImage();
    if (preview != null) {
      g.drawImage(preview, 0, 0, 300, 116, null);
    } else {
      paintFallbackPreview(g);
    }
    g.setColor(this.staticShadowColor);
    g.fillRect(0, 38, 300, 40);
    g.setColor(this.ambientColor);
    g.fillRect(0, 0, 82, 116);
    g.fillRect(156, 0, 72, 116);
  }

  private Image getPreviewImage() {
    if (this.previewImage != null || this.previewImageLoadFailed) {
      return this.previewImage;
    }

    try {
      this.previewImage = Resources.images().get("ambientlight-preview.png");
    } catch (RuntimeException e) {
      this.previewImageLoadFailed = true;
    }
    return this.previewImage;
  }

  private static void paintFallbackPreview(Graphics g) {
    g.setColor(new Color(42, 44, 48));
    g.fillRect(0, 0, 300, 116);
    g.setColor(new Color(62, 64, 70));
    g.fillRect(0, 76, 300, 40);
    g.setColor(new Color(82, 84, 90));
    g.fillRect(112, 20, 70, 56);
  }
}

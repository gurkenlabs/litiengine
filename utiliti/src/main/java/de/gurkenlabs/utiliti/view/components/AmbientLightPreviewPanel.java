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
    int width = PropertyPanel.LABEL_WIDTH + PropertyPanel.GUTTER_WIDTH + PropertyPanel.CONTROL_WIDTH;
    this.setMinimumSize(new Dimension(width, 104));
    this.setPreferredSize(new Dimension(width, 104));
    this.setSize(new Dimension(width, 104));
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
      g.drawImage(preview, 0, 0, getWidth(), getHeight(), null);
    } else {
      paintFallbackPreview(g);
    }
    g.setColor(this.staticShadowColor);
    g.fillRect(0, getHeight() / 3, getWidth(), getHeight() / 3);
    g.setColor(this.ambientColor);
    g.fillRect(0, 0, (int) (getWidth() * 0.27), getHeight());
    g.fillRect((int) (getWidth() * 0.52), 0, (int) (getWidth() * 0.24), getHeight());
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

  private void paintFallbackPreview(Graphics g) {
    g.setColor(new Color(42, 44, 48));
    g.fillRect(0, 0, getWidth(), getHeight());
    g.setColor(new Color(62, 64, 70));
    g.fillRect(0, (int) (getHeight() * 0.66), getWidth(), (int) (getHeight() * 0.34));
    g.setColor(new Color(82, 84, 90));
    g.fillRect((int) (getWidth() * 0.38), (int) (getHeight() * 0.18), (int) (getWidth() * 0.24), (int) (getHeight() * 0.48));
  }
}

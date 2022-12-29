package de.gurkenlabs.utiliti.swing.panels;

import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.resources.Resources;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class AmbientLightPreviewPanel extends JPanel {
  private Color ambientColor = AmbientLight.DEFAULT_COLOR;
  private Color staticShadowColor = StaticShadow.DEFAULT_COLOR;

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
    g.drawImage(Resources.images().get("ambientlight-preview.png"), 0, 0, 300, 116, null);
    g.setColor(this.staticShadowColor);
    g.fillRect(0, 38, 300, 40);
    g.setColor(this.ambientColor);
    g.fillRect(0, 0, 82, 116);
    g.fillRect(156, 0, 72, 116);
  }
}

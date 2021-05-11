package de.gurkenlabs.utiliti.swing.panels;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class AmbientLightPreviewPanel extends JPanel {

  private Color ambientColor = ColorHelper.decode("#ffffffff");
  private Color staticShadowColor = ColorHelper.decode("#5a000000");

  public AmbientLightPreviewPanel() {

    this.setMinimumSize(new Dimension(300, 116));
    this.setPreferredSize(new Dimension(300, 116));
    this.setSize(new Dimension(300, 116));
    this.setBorder(BorderFactory.createLineBorder(Color.black));
  }

  public void setAmbientColor(Color color) {
    this.ambientColor = color;
    this.repaint();
  }

  public void setStaticShadowColor(Color color) {
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

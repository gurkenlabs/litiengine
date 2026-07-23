package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

final class ZoomControls extends JPanel {
  private final JLabel zoomLabel;
  private final JPanel zoomGroup;
  private final JPanel fitGroup;

  ZoomControls(Runnable zoomOut, Runnable zoomIn, Runnable fit, String fitTooltip) {
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setOpaque(false);

    JButton out = Style.textButton("−");
    JButton in = Style.textButton("+");
    JButton fitButton = Style.iconButton(Icons.FIT_16);
    configureButton(out, Resources.strings().get("menu_view_zoomOut"), zoomOut);
    configureButton(in, Resources.strings().get("menu_view_zoomIn"), zoomIn);
    configureButton(fitButton, fitTooltip, fit);
    out.putClientProperty("Editor.groupedButtonEdge", "left");
    in.putClientProperty("Editor.groupedButtonEdge", "right");
    out.setFont(out.getFont().deriveFont(18f));
    in.setFont(in.getFont().deriveFont(18f));

    this.zoomLabel = new JLabel("100%", SwingConstants.CENTER);
    this.zoomLabel.setOpaque(true);
    this.zoomLabel.setPreferredSize(new Dimension(58, Style.CONTROL_HEIGHT));
    this.zoomLabel.setMinimumSize(this.zoomLabel.getPreferredSize());
    this.zoomLabel.getAccessibleContext().setAccessibleName(
        Resources.strings().get("toolbar_zoomLevel"));

    this.zoomGroup = new RoundedGroupPanel();
    this.zoomGroup.add(out, BorderLayout.WEST);
    this.zoomGroup.add(this.zoomLabel, BorderLayout.CENTER);
    this.zoomGroup.add(in, BorderLayout.EAST);

    this.fitGroup = new RoundedGroupPanel();
    this.fitGroup.add(fitButton, BorderLayout.CENTER);

    add(this.zoomGroup);
    add(Box.createHorizontalStrut(Style.SPACE_MEDIUM));
    add(this.fitGroup);
    refreshStyle();
    this.zoomGroup.setMaximumSize(this.zoomGroup.getPreferredSize());
    this.fitGroup.setMaximumSize(this.fitGroup.getPreferredSize());
    setMaximumSize(getPreferredSize());
  }

  void setZoom(double zoom) {
    setZoomText(Math.round(zoom * 100) + "%");
  }

  void setZoomText(String text) {
    this.zoomLabel.setText(text);
  }

  String getZoomText() {
    return this.zoomLabel.getText();
  }

  void refreshStyle() {
    this.zoomLabel.setForeground(Style.text());
    this.zoomLabel.setBackground(Style.surface());
    this.zoomLabel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Style.border()));
    styleGroup(this.zoomGroup);
    styleGroup(this.fitGroup);
  }

  @Override public void updateUI() {
    super.updateUI();
    if (this.zoomGroup != null) {
      refreshStyle();
    }
  }

  private static void configureButton(JButton button, String tooltip, Runnable action) {
    button.putClientProperty("Editor.groupedToolbarButton", true);
    button.putClientProperty("Editor.buttonArc", Style.CORNER_RADIUS * 2);
    button.setToolTipText(tooltip);
    button.addActionListener(event -> action.run());
  }

  private static void styleGroup(JPanel group) {
    group.setBackground(Style.surface());
    group.setBorder(BorderFactory.createEmptyBorder());
    for (java.awt.Component component : group.getComponents()) {
      if (component instanceof JButton button) {
        button.setForeground(Style.text());
      }
    }
  }

  private static final class RoundedGroupPanel extends JPanel {
    private RoundedGroupPanel() {
      super(new BorderLayout());
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      Graphics2D g2 = (Graphics2D) graphics.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(
            0,
            0,
            getWidth(),
            getHeight(),
            Style.CORNER_RADIUS * 2,
            Style.CORNER_RADIUS * 2);
      } finally {
        g2.dispose();
      }
      super.paintComponent(graphics);
    }

    @Override
    protected void paintChildren(Graphics graphics) {
      super.paintChildren(graphics);
      Graphics2D g2 = (Graphics2D) graphics.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Style.border());
        g2.drawRoundRect(
            0,
            0,
            getWidth() - 1,
            getHeight() - 1,
            Style.CORNER_RADIUS * 2,
            Style.CORNER_RADIUS * 2);
      } finally {
        g2.dispose();
      }
    }
  }
}

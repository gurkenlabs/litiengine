package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ExpandableCard extends JPanel {
  private static final int ARC = 6;
  private static final Color CARD_BG = new Color(30, 31, 34);
  private static final Color HEADER_BG = new Color(30, 30, 34);
  private static final Color HEADER_BORDER = new Color(55, 55, 64);
  private static final Color CHEVRON_COLOR = new Color(160, 160, 180);
  private static final Icon CHEVRON_EXPANDED = new ChevronIcon(true);
  private static final Icon CHEVRON_COLLAPSED = new ChevronIcon(false);

  private final JPanel contentPanel;
  private final JLabel arrowLabel;
  private final JLabel titleLabel;
  private final JPanel headerPanel;
  private boolean expanded;

  public ExpandableCard(String title, JComponent content) {
    this(title, content, true);
  }

  public ExpandableCard(String title, JComponent content, boolean startExpanded) {
    this.expanded = startExpanded;
    setLayout(new BorderLayout());
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

    this.headerPanel = new JPanel(new BorderLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(expanded ? HEADER_BG : Style.COLOR_HEADER_COLLAPSED);
        if (expanded) {
          g2.fillRoundRect(0, 0, getWidth(), getHeight() + ARC, ARC, ARC);
          g2.fillRect(0, getHeight() - ARC, getWidth(), ARC);
          g2.setColor(HEADER_BORDER);
          g2.fillRect(1, getHeight() - 1, getWidth() - 2, 1);
        } else {
          g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
        }
        g2.dispose();
      }
    };
    headerPanel.setOpaque(false);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    this.arrowLabel = new JLabel(expanded ? CHEVRON_EXPANDED : CHEVRON_COLLAPSED);

    this.titleLabel = new JLabel(title);
    titleLabel.setForeground(new Color(200, 200, 215));
    titleLabel.setFont(titleLabel.getFont().deriveFont(11f));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));

    headerPanel.add(arrowLabel, BorderLayout.WEST);
    headerPanel.add(titleLabel, BorderLayout.CENTER);

    headerPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        toggle();
      }
    });

    this.contentPanel = new JPanel(new BorderLayout());
    contentPanel.setOpaque(true);
    contentPanel.setBackground(CARD_BG);
    contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    contentPanel.setVisible(expanded);
    if (content != null) {
      content.setOpaque(false);
      contentPanel.add(content, BorderLayout.CENTER);
    }

    add(headerPanel, BorderLayout.NORTH);
    add(contentPanel, BorderLayout.CENTER);
  }

  @Override
  public Dimension getMaximumSize() {
    Dimension pref = getPreferredSize();
    return new Dimension(Integer.MAX_VALUE, pref.height);
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (expanded && g instanceof Graphics2D g2) {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(CARD_BG);
      g2.fillRect(0, 0, getWidth(), getHeight());
    }
    super.paintComponent(g);
  }

  public void toggle() {
    expanded = !expanded;
    contentPanel.setVisible(expanded);
    arrowLabel.setIcon(expanded ? CHEVRON_EXPANDED : CHEVRON_COLLAPSED);
    headerPanel.repaint();
    revalidate();
    repaint();
  }

  public boolean isExpanded() {
    return expanded;
  }

  public void setExpanded(boolean expanded) {
    if (this.expanded != expanded) {
      toggle();
    }
  }

  public void setContent(JComponent content) {
    contentPanel.removeAll();
    if (content != null) {
      content.setOpaque(false);
      contentPanel.add(content, BorderLayout.CENTER);
    }
    contentPanel.revalidate();
    contentPanel.repaint();
  }

  public void setContentInsets(int top, int left, int bottom, int right) {
    contentPanel.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
  }

  public void setTitle(String title) {
    titleLabel.setText(title);
  }

  private static final class ChevronIcon implements Icon {
    private static final int SIZE = 12;
    private final boolean expanded;

    private ChevronIcon(boolean expanded) {
      this.expanded = expanded;
    }

    @Override
    public int getIconWidth() {
      return SIZE;
    }

    @Override
    public int getIconHeight() {
      return SIZE;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2.setColor(CHEVRON_COLOR);
      if (expanded) {
        g2.drawLine(x + 3, y + 5, x + 6, y + 8);
        g2.drawLine(x + 6, y + 8, x + 9, y + 5);
      } else {
        g2.drawLine(x + 5, y + 3, x + 8, y + 6);
        g2.drawLine(x + 8, y + 6, x + 5, y + 9);
      }
      g2.dispose();
    }
  }
}

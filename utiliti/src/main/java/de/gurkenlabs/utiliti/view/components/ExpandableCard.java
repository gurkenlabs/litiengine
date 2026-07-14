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
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class ExpandableCard extends JPanel {
  private static final int ARC = Style.CORNER_RADIUS;
  private static final Icon CHEVRON_EXPANDED = new ChevronIcon(true);
  private static final Icon CHEVRON_COLLAPSED = new ChevronIcon(false);

  private final JPanel contentPanel;
  private final JLabel arrowLabel;
  private final JLabel titleLabel;
  private final JPanel headerPanel;
  private boolean expanded;
  private boolean fillsAvailableHeight;

  public ExpandableCard(String title, JComponent content) {
    this(title, content, true);
  }

  public ExpandableCard(String title, JComponent content, boolean startExpanded) {
    this.expanded = startExpanded;
    setLayout(new BorderLayout());
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

    this.headerPanel = new JPanel(new BorderLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Style.surface());
        if (expanded) {
          g2.fillRoundRect(0, 0, getWidth(), getHeight() + ARC, ARC, ARC);
          g2.fillRect(0, getHeight() - ARC, getWidth(), ARC);
          g2.setColor(Style.border());
          g2.fillRect(1, getHeight() - 1, getWidth() - 2, 1);
        } else {
          g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
        }
        if (isFocusOwner()) {
          g2.setColor(Style.accent());
          g2.setStroke(new BasicStroke(2f));
          g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, ARC, ARC);
        }
        g2.dispose();
      }
    };
    headerPanel.setOpaque(false);
    headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
    headerPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    headerPanel.setFocusable(true);
    headerPanel.getAccessibleContext().setAccessibleName(title);
    headerPanel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "toggle");
    headerPanel.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "toggle");
    headerPanel.getActionMap().put("toggle", new AbstractAction() {
      @Override public void actionPerformed(java.awt.event.ActionEvent e) { toggle(); }
    });

    this.arrowLabel = new JLabel(expanded ? CHEVRON_EXPANDED : CHEVRON_COLLAPSED);

    this.titleLabel = new JLabel(title);
    titleLabel.setForeground(Style.text());
    titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 11f));
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
    contentPanel.setBackground(Style.surface());
    contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
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
    return new Dimension(Integer.MAX_VALUE, this.fillsAvailableHeight ? Integer.MAX_VALUE : pref.height);
  }

  @Override
  public void updateUI() {
    super.updateUI();
    if (this.contentPanel != null) {
      this.contentPanel.setBackground(Style.surface());
    }
    if (this.titleLabel != null) {
      this.titleLabel.setForeground(Style.text());
    }
  }

  public void setFillsAvailableHeight(boolean fillsAvailableHeight) {
    this.fillsAvailableHeight = fillsAvailableHeight;
    revalidate();
  }

  public void setHeaderTrailing(Component component) {
    this.headerPanel.add(component, BorderLayout.EAST);
    this.headerPanel.revalidate();
    this.headerPanel.repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (expanded && g instanceof Graphics2D g2) {
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Style.surface());
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
    headerPanel.getAccessibleContext().setAccessibleName(title);
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
      g2.setColor(Style.mutedText());
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

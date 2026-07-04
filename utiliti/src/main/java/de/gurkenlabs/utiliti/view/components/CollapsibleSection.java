package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CollapsibleSection extends JPanel {
  private final JPanel contentPanel;
  private final JLabel headerLabel;
  private final String title;
  private boolean expanded;

  public CollapsibleSection(String titleKey, JPanel content) {
    this(titleKey, content, true);
  }

  public CollapsibleSection(String titleKey, JPanel content, boolean startExpanded) {
    this.title = Resources.strings().get(titleKey);
    this.expanded = startExpanded;

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setOpaque(false);
    setAlignmentX(Component.LEFT_ALIGNMENT);

    this.headerLabel = new JLabel(this.title);
    this.headerLabel.setFont(Style.getHeaderFont());
    this.headerLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.headerLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
    this.headerLabel.setOpaque(true);
    this.headerLabel.setBackground(Style.COLOR_SURFACE);
    this.headerLabel.setForeground(Style.COLOR_TEXT);
    this.headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    this.headerLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        toggle();
      }
    });

    this.contentPanel = new JPanel(new BorderLayout());
    this.contentPanel.setOpaque(false);
    this.contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    this.contentPanel.add(content, BorderLayout.CENTER);
    this.contentPanel.setVisible(this.expanded);

    add(headerLabel);
    add(contentPanel);
  }

  public void toggle() {
    expanded = !expanded;
    contentPanel.setVisible(expanded);
    headerLabel.setText(title);
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
}

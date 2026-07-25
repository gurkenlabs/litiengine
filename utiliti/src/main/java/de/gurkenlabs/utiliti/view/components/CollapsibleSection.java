package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** A localized inspector section using the same card chrome as the main property inspector. */
public class CollapsibleSection extends JPanel {
  private final ExpandableCard card;
  private final JPanel headerActions;

  public CollapsibleSection(String titleKey, JPanel content) {
    this(titleKey, content, true);
  }

  public CollapsibleSection(String titleKey, JPanel content, boolean startExpanded) {
    super(new BorderLayout());
    setOpaque(false);
    setAlignmentX(Component.LEFT_ALIGNMENT);
    this.card = new ExpandableCard(Resources.strings().get(titleKey), content, startExpanded);
    this.headerActions = new JPanel();
    this.headerActions.setOpaque(false);
    this.headerActions.setLayout(new BoxLayout(this.headerActions, BoxLayout.X_AXIS));
    this.card.setContentInsets(
        PropertyPanel.CONTROL_MARGIN * 2,
        PropertyPanel.CONTROL_MARGIN * 2,
        PropertyPanel.CONTROL_MARGIN * 2,
        PropertyPanel.CONTROL_MARGIN * 2);
    add(this.card, BorderLayout.CENTER);
  }

  @Override
  public Dimension getMaximumSize() {
    return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
  }

  public void toggle() {
    this.card.toggle();
  }

  public boolean isExpanded() {
    return this.card.isExpanded();
  }

  public void setExpanded(boolean expanded) {
    this.card.setExpanded(expanded);
  }

  public void setHeaderAction(Component action) {
    if (action != null) {
      this.headerActions.add(Box.createHorizontalStrut(PropertyPanel.CONTROL_MARGIN));
      this.headerActions.add(action);
      this.card.setHeaderTrailing(this.headerActions);
    }
  }

  public void setInfoText(String resourceKey) {
    JLabel info = new JLabel(Icons.ABOUT_16);
    String text = Resources.strings().get(resourceKey);
    info.setToolTipText(text);
    info.getAccessibleContext().setAccessibleName(text);
    this.headerActions.add(info, 0);
    this.card.setHeaderTrailing(this.headerActions);
  }
}

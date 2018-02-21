package de.gurkenlabs.utiliti.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class JCheckBoxList extends JList<JCheckBox> {
  protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

  public JCheckBoxList() {
    setCellRenderer(new CellRenderer());
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        int index = locationToIndex(e.getPoint());
        if (index != -1) {
          JCheckBox checkbox = getModel().getElementAt(index);

          if (e.getPoint().x < checkbox.getHorizontalTextPosition()) {

            checkbox.setSelected(!checkbox.isSelected());
          }
          repaint();
        }
      }
    });
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

  public JCheckBoxList(ListModel<JCheckBox> model) {
    this();
    setModel(model);
  }

  protected class CellRenderer implements ListCellRenderer<JCheckBox> {
    private final Map<JCheckBox, CheckBoxPanel> panels;

    public CellRenderer() {
      panels = new ConcurrentHashMap<>();
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox value, int index, boolean isSelected, boolean cellHasFocus) {
      if(!this.panels.containsKey(value)) {
        this.panels.put(value, new CheckBoxPanel(value.getText()));
      }
      
      CheckBoxPanel panel = this.panels.get(value);

      panel.getCheck().setSelected(value.isSelected());
      // Drawing checkbox, change the appearance here
      panel.getCheck().setBackground(isSelected ? getSelectionBackground() : getBackground());
      panel.getCheck().setForeground(isSelected ? getSelectionForeground() : getForeground());
      panel.getCheck().setEnabled(isEnabled());
      panel.getCheck().setFont(getFont());
      panel.getCheck().setFocusPainted(false);
      panel.getCheck().setBorderPainted(true);
      panel.getCheck().setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
      panel.getLabel().setOpaque(true);
      panel.getLabel().setBackground(isSelected ? getSelectionBackground() : getBackground());
      panel.getLabel().setForeground(isSelected ? getSelectionForeground() : getForeground());
      panel.getLabel().setEnabled(isEnabled());
      panel.getLabel().setFocusable(false);
      panel.getLabel().setFont(getFont());
      panel.getLabel().setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
      panel.getLabel().setIcon(value.getIcon());

      return panel;
    }

    private class CheckBoxPanel extends JPanel {
      private final JCheckBox newCheck;
      private final JLabel newLabel;

      public CheckBoxPanel(String text) {
        this.newCheck = new JCheckBox();
        this.newLabel = new JLabel(text);
        this.newCheck.setFocusPainted(false);
        this.newCheck.setBorderPainted(true);
        this.newLabel.setOpaque(true);
        this.newLabel.setFocusable(false);
        this.setLayout(new BorderLayout());
        this.add(newCheck, BorderLayout.WEST);
        this.add(newLabel, BorderLayout.CENTER);
      }

      public JCheckBox getCheck() {
        return this.newCheck;
      }

      public JLabel getLabel() {
        return this.newLabel;
      }

    }
  }
}
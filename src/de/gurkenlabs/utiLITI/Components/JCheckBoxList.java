package de.gurkenlabs.utiLITI.Components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
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

import de.gurkenlabs.litiengine.graphics.RenderEngine;

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
    @Override
    public Component getListCellRendererComponent(
        JList<? extends JCheckBox> list, JCheckBox value, int index,
        boolean isSelected, boolean cellHasFocus) {
      JPanel panel = new JPanel(new BorderLayout());

      JCheckBox checkbox = value;
      final JCheckBox newCheck = new JCheckBox();
      final JLabel newLabel = new JLabel(checkbox.getText());
      newCheck.setSelected(checkbox.isSelected());
      // Drawing checkbox, change the appearance here
      newCheck.setBackground(isSelected ? getSelectionBackground()
          : getBackground());
      newCheck.setForeground(isSelected ? getSelectionForeground()
          : getForeground());
      newCheck.setEnabled(isEnabled());
      newCheck.setFont(getFont());
      newCheck.setFocusPainted(false);
      newCheck.setBorderPainted(true);
      newCheck.setBorder(isSelected ? UIManager
          .getBorder("List.focusCellHighlightBorder") : noFocusBorder);
      newLabel.setOpaque(true);
      newLabel.setBackground(isSelected ? getSelectionBackground()
          : getBackground());
      newLabel.setForeground(isSelected ? getSelectionForeground()
          : getForeground());
      newLabel.setEnabled(isEnabled());
      newLabel.setFocusable(false);
      newLabel.setFont(getFont());
      newLabel.setBorder(isSelected ? UIManager
          .getBorder("List.focusCellHighlightBorder") : noFocusBorder);
      newLabel.setIcon(new ImageIcon(RenderEngine.getImage("object_cube-10x10.png")));

      panel.add(newCheck, BorderLayout.WEST);
      panel.add(newLabel, BorderLayout.CENTER);
      return panel;
    }
  }
}
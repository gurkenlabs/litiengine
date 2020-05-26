package de.gurkenlabs.utiliti.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import com.github.weisj.darklaf.components.border.DarkBorders;

import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.SwingHelpers;

@SuppressWarnings("serial")
public class ColorList extends JList<Color> {
  private final DefaultListModel<Color> colorListModel;

  public ColorList() {
    super();
    colorListModel = new DefaultListModel<>();
    setModel(colorListModel);
    setCellRenderer(ControlBehavior.apply(new ColorListCellRenderer()));
    setBorder(DarkBorders.createLineBorder(1, 1, 1, 1));
  }

  public void setColors(String commaSeparatedHexstrings) {
    colorListModel.clear();
    for (String colorStr : commaSeparatedHexstrings.split(",")) {
      colorListModel.addElement(ColorHelper.decode(colorStr));
    }
    revalidate();
    repaint();
  }

  public void clear() {
    colorListModel.clear();
  }

  private class ColorListCellRenderer extends JTextField implements ListCellRenderer<Color> {
    Border focusBorder = DarkBorders.createLineBorder(1, 1, 1, 1);

    @Override
    public Component getListCellRendererComponent(JList<? extends Color> list, Color value, int index, boolean isSelected, boolean cellHasFocus) {
      SwingHelpers.updateColorTextField(this, value);
      this.setBorder(cellHasFocus ? focusBorder : null);
      this.setAlignmentX(CENTER_ALIGNMENT);
      this.setEditable(true);

      return this;
    }

  }
}

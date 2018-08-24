package de.gurkenlabs.utiliti.swing.dialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.text.ParseException;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.gurkenlabs.litiengine.Resources;

public class GridEditPanel extends JPanel {
  private JSpinner widthSpinner, heightSpinner;

  public GridEditPanel(int gridWidth, int gridHeight) {

    Box horizontalBox = Box.createHorizontalBox();
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(horizontalBox, GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE).addContainerGap()));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addContainerGap().addComponent(horizontalBox, GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE).addContainerGap()));

    Component horizontalGlue0 = Box.createHorizontalGlue();
    horizontalBox.add(horizontalGlue0);

    JLabel lblWidth = new JLabel(Resources.get("menu_gridWidth"));
    horizontalBox.add(lblWidth);

    Component horizontalGlue1 = Box.createHorizontalGlue();
    horizontalBox.add(horizontalGlue1);

    this.widthSpinner = new JSpinner();
    horizontalBox.add(this.widthSpinner);
    this.widthSpinner.setModel(new SpinnerNumberModel(gridWidth, null, null, new Integer(1)));

    Component horizontalGlue2 = Box.createHorizontalGlue();
    horizontalBox.add(horizontalGlue2);

    JLabel lblHeight = new JLabel(Resources.get("menu_gridHeight"));
    horizontalBox.add(lblHeight);

    Component horizontalGlue3 = Box.createHorizontalGlue();
    horizontalBox.add(horizontalGlue3);

    this.heightSpinner = new JSpinner();
    horizontalBox.add(this.heightSpinner);
    this.heightSpinner.setModel(new SpinnerNumberModel(gridHeight, null, null, new Integer(1)));

    Component horizontalGlue4 = Box.createHorizontalGlue();
    horizontalBox.add(horizontalGlue4);

    setLayout(groupLayout);
  }

  public Dimension getGridSize() {
    try {
      this.widthSpinner.commitEdit();
      this.heightSpinner.commitEdit();
    } catch (ParseException e) {
      System.err.println("One of your edits in the grid size spinners could not be parsed as Integer.");
      e.printStackTrace();
    }
    return new Dimension((Integer) this.widthSpinner.getValue(), (Integer) this.heightSpinner.getValue());
  }
}

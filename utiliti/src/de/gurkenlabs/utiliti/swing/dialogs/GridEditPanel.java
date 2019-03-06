package de.gurkenlabs.utiliti.swing.dialogs;

import java.awt.Color;
import java.awt.Dimension;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.Icons;

@SuppressWarnings("serial")
public class GridEditPanel extends JPanel {
  private static final Logger log = Logger.getLogger(GridEditPanel.class.getName());
  private JSpinner widthSpinner;
  private JSpinner heightSpinner;
  private JSpinner strokeSpinner;
  private JButton buttonSetColor;
  private Color gridColor;

  public GridEditPanel(int gridWidth, int gridHeight, float strokeWidth, Color strokeColor) {

    JLabel lblStroke = new JLabel(Resources.strings().get("menu_gridStroke"));

    JLabel lblWidth = new JLabel(Resources.strings().get("menu_gridWidth"));

    this.widthSpinner = new JSpinner();
    this.widthSpinner.setModel(new SpinnerNumberModel(gridWidth, null, null, 1));

    JLabel lblHeight = new JLabel(Resources.strings().get("menu_gridHeight"));

    this.heightSpinner = new JSpinner();
    this.heightSpinner.setModel(new SpinnerNumberModel(gridHeight, null, null, 1));

    this.strokeSpinner = new JSpinner();
    strokeSpinner.setModel(new SpinnerNumberModel((Float) strokeWidth, 1f, null, 0.1f));

    this.gridColor = strokeColor;
    this.buttonSetColor = new JButton("");
    this.buttonSetColor.setIcon(Icons.COLORX16);
    this.buttonSetColor.addActionListener(a -> {
      Color newColor = JColorChooser.showDialog(null, Resources.strings().get("panel_selectLayerColor"), strokeColor);
      this.gridColor = newColor == null ? strokeColor : newColor;
    });

    JLabel lblColor = new JLabel(Resources.strings().get("menu_gridColor"));

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblWidth).addComponent(lblStroke).addComponent(lblColor)).addGap(18)
            .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(strokeSpinner).addComponent(widthSpinner, GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE).addComponent(buttonSetColor, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 52, Short.MAX_VALUE)).addGap(18)
            .addComponent(lblHeight).addPreferredGap(ComponentPlacement.RELATED).addComponent(heightSpinner, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE).addGap(49)));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
        .addGroup(groupLayout.createSequentialGroup().addGap(22)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblWidth).addComponent(widthSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblHeight).addComponent(heightSpinner, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGap(18).addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblStroke).addComponent(strokeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(18)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblColor).addComponent(this.buttonSetColor, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)).addGap(110)));

    setLayout(groupLayout);
  }

  public Color getGridColor() {
    return this.gridColor;
  }

  public float getStrokeWidth() {
    try {
      this.strokeSpinner.commitEdit();
    } catch (ParseException e) {
      log.log(Level.WARNING, "Your edits in the grid line thickness spinner could not be parsed as Float.");
    }
    return (Float) this.strokeSpinner.getValue();
  }

  public Dimension getGridSize() {
    try {
      this.widthSpinner.commitEdit();
      this.heightSpinner.commitEdit();
    } catch (ParseException e) {
      log.log(Level.WARNING, "\"One of your edits in the grid size spinners could not be parsed as Integer.");
    }
    return new Dimension((Integer) this.widthSpinner.getValue(), (Integer) this.heightSpinner.getValue());
  }
}

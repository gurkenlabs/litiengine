package de.gurkenlabs.utiliti.swing.dialogs;

import java.awt.Color;
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
  private JSpinner strokeSpinner;
  private JButton buttonSetColor;
  private Color gridColor;

  public GridEditPanel(float strokeWidth, Color strokeColor) {

    JLabel lblStroke = new JLabel(Resources.strings().get("menu_gridStroke"));

    this.strokeSpinner = new JSpinner();
    strokeSpinner.setModel(new SpinnerNumberModel(strokeWidth, 1f, 5f, 0.1f));

    this.gridColor = strokeColor;
    this.buttonSetColor = new JButton("");
    this.buttonSetColor.setIcon(Icons.COLORX16);
    this.buttonSetColor.addActionListener(a -> {
      Color newColor = JColorChooser.showDialog(null, Resources.strings().get("panel_selectLayerColor"), strokeColor);
      this.gridColor = newColor == null ? strokeColor : newColor;
    });

    JLabel lblColor = new JLabel(Resources.strings().get("menu_gridColor"));

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addContainerGap()
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(lblStroke)
              .addGap(18)
              .addComponent(strokeSpinner, GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE))
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(lblColor)
              .addGap(58)
              .addComponent(buttonSetColor, GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)))
          .addContainerGap())
    );
    groupLayout.setVerticalGroup(
      groupLayout.createParallelGroup(Alignment.TRAILING)
        .addGroup(groupLayout.createSequentialGroup()
          .addContainerGap()
          .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblStroke)
            .addComponent(strokeSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(lblColor)
            .addComponent(buttonSetColor, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
          .addGap(171))
    );

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
}

package de.gurkenlabs.utiliti.components;

import java.awt.Font;
import java.text.NumberFormat;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

import de.gurkenlabs.utiliti.Program;

public class GridEditPanel extends JPanel {
  private JFormattedTextField textField;

  public GridEditPanel(int rasterSize) {

    JLabel lblSize = new JLabel("size (px)");
    lblSize.setFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(10f));

    textField = new JFormattedTextField(NumberFormat.getIntegerInstance());

    textField.setText("16");
    textField.setFont(Program.TEXT_FONT.deriveFont(10f));
    textField.setColumns(10);
    textField.setValue(rasterSize);
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSize, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(textField, GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addGap(34)));
    groupLayout.setVerticalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblSize, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(textField, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(46, Short.MAX_VALUE)));
    setLayout(groupLayout);
  }

  public int getGridSize() {
    return Integer.parseInt(this.textField.getText());
  }
}

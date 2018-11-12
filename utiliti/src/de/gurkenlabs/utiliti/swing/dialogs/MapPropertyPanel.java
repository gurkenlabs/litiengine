package de.gurkenlabs.utiliti.swing.dialogs;

import java.awt.Color;
import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.Program;

@SuppressWarnings("serial")
public class MapPropertyPanel extends JPanel {

  private final JTextField textFieldDescription;
  private final JTextField textFieldTitle;
  private JTextField textFieldAmbientColor;
  private JSpinner spinnerAmbientAlpha;
  private JSpinner spinnerShadowAlpha;
  private JTextField textFieldName;
  private transient IMap dataSource;
  private JTextField textFieldShadowColor;

  /**
   * Create the dialog.
   */
  public MapPropertyPanel() {
    setBounds(100, 100, 450, 302);
    this.setBorder(new EmptyBorder(5, 5, 5, 5));
    JLabel lblMapName = new JLabel("name");
    lblMapName.setFont(Program.TEXT_FONT);

    textFieldDescription = new JTextField();
    textFieldDescription.setFont(Program.TEXT_FONT);
    textFieldDescription.setColumns(10);

    JLabel lblMapTitle = new JLabel("title");
    lblMapTitle.setFont(new Font("Tahoma", Font.PLAIN, 10));

    textFieldTitle = new JTextField();
    textFieldTitle.setFont(Program.TEXT_FONT);
    textFieldTitle.setColumns(10);

    JLabel label = new JLabel("color");
    label.setFont(Program.TEXT_FONT);

    JButton buttonAmbientColor = new JButton("...");
    buttonAmbientColor.addActionListener(a -> {
      Color result = JColorChooser.showDialog(null, Resources.get("panel_selectAmbientColor"), ColorHelper.decode(textFieldAmbientColor.getText()));
      if (result == null) {
        return;
      }

      String h = ColorHelper.encode(result);
      textFieldAmbientColor.setText(h);
      spinnerAmbientAlpha.setValue(result.getAlpha());
    });
    buttonAmbientColor.setFont(Program.TEXT_FONT.deriveFont(10f));

    textFieldAmbientColor = new JTextField();
    textFieldAmbientColor.setText("#ffffff");
    textFieldAmbientColor.setFont(Program.TEXT_FONT);
    textFieldAmbientColor.setEditable(false);
    textFieldAmbientColor.setColumns(10);

    JLabel lblAlpha = new JLabel("alpha");
    lblAlpha.setFont(new Font("Tahoma", Font.PLAIN, 10));

    spinnerAmbientAlpha = new JSpinner();
    spinnerAmbientAlpha.setModel(new SpinnerNumberModel(0, 0, 255, 1));
    spinnerAmbientAlpha.setFont(Program.TEXT_FONT);
    spinnerAmbientAlpha.addChangeListener(e -> {
      Color oldColor = ColorHelper.decode(textFieldAmbientColor.getText());
      Color newColor = new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), (int) spinnerAmbientAlpha.getValue());
      String hex = ColorHelper.encode(newColor);
      textFieldAmbientColor.setText(hex);

    });

    JLabel lblNewLabel = new JLabel("General");
    lblNewLabel.setFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(12f));

    JLabel lblAmbientLight = new JLabel("Ambient Light");
    lblAmbientLight.setFont(Program.TEXT_FONT.deriveFont(Font.BOLD).deriveFont(12f));

    JLabel lblDesc = new JLabel("description");
    lblDesc.setFont(new Font("Tahoma", Font.PLAIN, 10));

    textFieldName = new JTextField();
    textFieldName.setFont(Program.TEXT_FONT);
    textFieldName.setColumns(10);

    JLabel lblStaticShadows = new JLabel("Static Shadows");
    lblStaticShadows.setFont(new Font("Dialog", Font.BOLD, 12));

    JLabel labelAlpha = new JLabel("alpha");
    labelAlpha.setFont(new Font("Tahoma", Font.PLAIN, 10));

    spinnerShadowAlpha = new JSpinner();
    spinnerShadowAlpha.setModel(new SpinnerNumberModel(0, 0, 255, 1));
    spinnerShadowAlpha.setFont(null);
    spinnerShadowAlpha.addChangeListener(e -> {
      Color oldColor = ColorHelper.decode(textFieldShadowColor.getText());
      Color newColor = new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), (int) spinnerShadowAlpha.getValue());
      String hex = ColorHelper.encode(newColor);
      textFieldShadowColor.setText(hex);
    });

    JLabel labelColor = new JLabel("color");
    labelColor.setFont(new Font("Tahoma", Font.PLAIN, 10));

    JButton buttonColorShadow = new JButton("...");
    buttonColorShadow.addActionListener(a -> {
      Color result = JColorChooser.showDialog(null, Resources.get("panel_selectShadowColor"), ColorHelper.decode(textFieldShadowColor.getText()));
      if (result == null) {
        return;
      }

      String h = ColorHelper.encode(result);
      textFieldShadowColor.setText(h);
      spinnerShadowAlpha.setValue(result.getAlpha());
    });
    buttonColorShadow.setFont(null);

    textFieldShadowColor = new JTextField();
    textFieldShadowColor.setText("#ffffff");
    textFieldShadowColor.setFont(null);
    textFieldShadowColor.setEditable(false);
    textFieldShadowColor.setColumns(10);
    GroupLayout glContentPanel = new GroupLayout(this);
    glContentPanel.setHorizontalGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(glContentPanel.createSequentialGroup().addContainerGap()
            .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING).addComponent(lblNewLabel)
                .addGroup(glContentPanel.createSequentialGroup()
                    .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING).addComponent(lblMapName).addComponent(lblMapTitle, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE).addComponent(lblDesc, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addGroup(
                        glContentPanel
                            .createParallelGroup(Alignment.LEADING).addComponent(textFieldDescription, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE).addComponent(textFieldTitle, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                            .addComponent(textFieldName, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)))
                .addGroup(glContentPanel.createSequentialGroup().addGroup(glContentPanel.createParallelGroup(Alignment.LEADING).addComponent(lblAmbientLight, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
                    .addGroup(glContentPanel.createSequentialGroup().addComponent(label, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addGap(10).addComponent(buttonAmbientColor, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(textFieldAmbientColor, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
                    .addGroup(glContentPanel.createSequentialGroup().addComponent(lblAlpha, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.UNRELATED).addComponent(spinnerAmbientAlpha, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)))
                    .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(glContentPanel.createSequentialGroup().addGap(18).addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                            .addGroup(glContentPanel.createSequentialGroup().addComponent(labelColor, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(buttonColorShadow, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED).addComponent(textFieldShadowColor, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
                            .addGroup(
                                glContentPanel.createSequentialGroup().addComponent(labelAlpha, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(spinnerShadowAlpha, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE))))
                        .addGroup(glContentPanel.createSequentialGroup().addGap(16).addComponent(lblStaticShadows, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)))
                    .addGap(52)))
            .addGap(0)));
    glContentPanel.setVerticalGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(glContentPanel.createSequentialGroup().addComponent(lblNewLabel).addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE).addComponent(lblMapName).addComponent(textFieldName, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE).addComponent(lblMapTitle).addComponent(textFieldTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE).addComponent(textFieldDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblDesc, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)).addGap(63)
            .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE).addComponent(lblAmbientLight).addComponent(lblStaticShadows)).addGap(9)
            .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE).addComponent(lblAlpha, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(spinnerAmbientAlpha, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(labelAlpha, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(spinnerShadowAlpha, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE).addComponent(buttonAmbientColor, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE).addComponent(textFieldAmbientColor, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                .addComponent(label, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(labelColor, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE).addComponent(buttonColorShadow, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                .addComponent(textFieldShadowColor, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
            .addContainerGap(60, Short.MAX_VALUE)));
    this.setLayout(glContentPanel);
  }

  public void bind(IMap map) {
    this.dataSource = map;
    if (map == null) {
      return;
    }

    this.setControlValues(map);
  }

  private void setControlValues(IMap map) {
    this.textFieldDescription.setText(map.getStringProperty(MapProperty.MAP_DESCRIPTION));
    this.textFieldTitle.setText(map.getStringProperty(MapProperty.MAP_TITLE));
    this.textFieldName.setText(map.getName());
    if (map.getStringProperty(MapProperty.AMBIENTCOLOR) != null) {
      final String hexColor = map.getStringProperty(MapProperty.AMBIENTCOLOR);
      this.textFieldAmbientColor.setText(hexColor);
      this.spinnerAmbientAlpha.setValue(ColorHelper.decode(hexColor).getAlpha());
    }
    if (map.getStringProperty(MapProperty.SHADOWCOLOR) != null) {
      final String hexColor = map.getStringProperty(MapProperty.SHADOWCOLOR);
      this.textFieldShadowColor.setText(hexColor);
      this.spinnerShadowAlpha.setValue(ColorHelper.decode(hexColor).getAlpha());
    }
  }

  public void saveChanges() {
    if (this.dataSource == null) {
      return;
    }

    this.dataSource.setProperty(MapProperty.MAP_DESCRIPTION, this.textFieldDescription.getText());
    this.dataSource.setProperty(MapProperty.MAP_TITLE, this.textFieldTitle.getText());
    this.dataSource.setProperty(MapProperty.AMBIENTCOLOR, this.textFieldAmbientColor.getText());
    this.dataSource.setProperty(MapProperty.SHADOWCOLOR, this.textFieldShadowColor.getText());
    this.dataSource.setName(this.textFieldName.getText());
  }
}

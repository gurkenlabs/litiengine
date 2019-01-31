package de.gurkenlabs.utiliti.swing.dialogs;

import java.awt.Color;

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

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;


@SuppressWarnings("serial")
public class MapPropertyPanel extends JPanel {
  private final JTextField textFieldDescription;
  private final JTextField textFieldTitle;
  private final JSpinner spinnerGravity;
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

    textFieldDescription = new JTextField();
    textFieldDescription.setColumns(10);

    JLabel lblMapTitle = new JLabel("title");

    textFieldTitle = new JTextField();
    textFieldTitle.setColumns(10);

    JLabel label = new JLabel("color");

    JButton buttonAmbientColor = new JButton("...");
    buttonAmbientColor.addActionListener(a -> {
      Color result = JColorChooser.showDialog(null, Resources.strings().get("panel_selectAmbientColor"), ColorHelper.decode(textFieldAmbientColor.getText()));
      if (result == null) {
        return;
      }

      String h = ColorHelper.encode(result);
      textFieldAmbientColor.setText(h);
      spinnerAmbientAlpha.setValue(result.getAlpha());
    });

    textFieldAmbientColor = new JTextField();
    textFieldAmbientColor.setText("#ffffff");
    textFieldAmbientColor.setEditable(false);
    textFieldAmbientColor.setColumns(10);

    JLabel lblAlpha = new JLabel("alpha");

    spinnerAmbientAlpha = new JSpinner();
    spinnerAmbientAlpha.setModel(new SpinnerNumberModel(0, 0, 255, 1));
    spinnerAmbientAlpha.addChangeListener(e -> {
      Color oldColor = ColorHelper.decode(textFieldAmbientColor.getText());
      Color newColor = new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), (int) spinnerAmbientAlpha.getValue());
      String hex = ColorHelper.encode(newColor);
      textFieldAmbientColor.setText(hex);

    });

    JLabel lblNewLabel = new JLabel("General");

    JLabel lblAmbientLight = new JLabel("Ambient Light");

    JLabel lblDesc = new JLabel("description");

    textFieldName = new JTextField();
    textFieldName.setColumns(10);

    JLabel lblStaticShadows = new JLabel("Static Shadows");

    JLabel labelAlpha = new JLabel("alpha");

    spinnerShadowAlpha = new JSpinner();
    spinnerShadowAlpha.setModel(new SpinnerNumberModel(0, 0, 255, 1));
    spinnerShadowAlpha.addChangeListener(e -> {
      Color oldColor = ColorHelper.decode(textFieldShadowColor.getText());
      Color newColor = new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), (int) spinnerShadowAlpha.getValue());
      String hex = ColorHelper.encode(newColor);
      textFieldShadowColor.setText(hex);
    });

    JLabel labelColor = new JLabel("color");

    JButton buttonColorShadow = new JButton("...");
    buttonColorShadow.addActionListener(a -> {
      Color result = JColorChooser.showDialog(null, Resources.strings().get("panel_selectShadowColor"), ColorHelper.decode(textFieldShadowColor.getText()));
      if (result == null) {
        return;
      }

      String h = ColorHelper.encode(result);
      textFieldShadowColor.setText(h);
      spinnerShadowAlpha.setValue(result.getAlpha());
    });


    textFieldShadowColor = new JTextField();
    textFieldShadowColor.setText("#ffffff");
    textFieldShadowColor.setEditable(false);
    textFieldShadowColor.setColumns(10);
    
    JLabel lblGravity = new JLabel("gravity");
    
    this.spinnerGravity = new JSpinner();
    GroupLayout glContentPanel = new GroupLayout(this);
    glContentPanel.setHorizontalGroup(
      glContentPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(glContentPanel.createSequentialGroup()
          .addContainerGap()
          .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
            .addGroup(glContentPanel.createSequentialGroup()
              .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                .addComponent(lblNewLabel)
                .addGroup(glContentPanel.createSequentialGroup()
                  .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                    .addComponent(lblMapName)
                    .addComponent(lblMapTitle, GroupLayout.PREFERRED_SIZE, 52, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDesc, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(ComponentPlacement.RELATED)
                  .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                    .addComponent(textFieldDescription, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                    .addComponent(textFieldTitle, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                    .addComponent(textFieldName, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                    .addComponent(spinnerGravity, GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)))
                .addGroup(glContentPanel.createSequentialGroup()
                  .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                    .addComponent(lblAmbientLight, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
                    .addGroup(glContentPanel.createSequentialGroup()
                      .addComponent(label, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                      .addGap(10)
                      .addComponent(buttonAmbientColor, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                      .addPreferredGap(ComponentPlacement.RELATED)
                      .addComponent(textFieldAmbientColor, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
                    .addGroup(glContentPanel.createSequentialGroup()
                      .addComponent(lblAlpha, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                      .addPreferredGap(ComponentPlacement.UNRELATED)
                      .addComponent(spinnerAmbientAlpha, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)))
                  .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                    .addGroup(glContentPanel.createSequentialGroup()
                      .addGap(18)
                      .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
                        .addGroup(glContentPanel.createSequentialGroup()
                          .addComponent(labelColor, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                          .addPreferredGap(ComponentPlacement.RELATED)
                          .addComponent(buttonColorShadow, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                          .addPreferredGap(ComponentPlacement.RELATED)
                          .addComponent(textFieldShadowColor, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
                        .addGroup(glContentPanel.createSequentialGroup()
                          .addComponent(labelAlpha, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
                          .addPreferredGap(ComponentPlacement.RELATED)
                          .addComponent(spinnerShadowAlpha, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE))))
                    .addGroup(glContentPanel.createSequentialGroup()
                      .addGap(16)
                      .addComponent(lblStaticShadows, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)))
                  .addGap(52)))
              .addGap(0))
            .addGroup(glContentPanel.createSequentialGroup()
              .addComponent(lblGravity)
              .addContainerGap(384, Short.MAX_VALUE))))
    );
    glContentPanel.setVerticalGroup(
      glContentPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(glContentPanel.createSequentialGroup()
          .addComponent(lblNewLabel)
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblMapName)
            .addComponent(textFieldName, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblMapTitle)
            .addComponent(textFieldTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(textFieldDescription, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(lblDesc, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(glContentPanel.createParallelGroup(Alignment.LEADING)
            .addComponent(lblGravity)
            .addComponent(spinnerGravity, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addGap(41)
          .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblAmbientLight)
            .addComponent(lblStaticShadows))
          .addGap(9)
          .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(lblAlpha, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
            .addComponent(spinnerAmbientAlpha, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(labelAlpha, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
            .addComponent(spinnerShadowAlpha, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(glContentPanel.createParallelGroup(Alignment.BASELINE)
            .addComponent(buttonAmbientColor, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
            .addComponent(textFieldAmbientColor, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
            .addComponent(label, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
            .addComponent(labelColor, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
            .addComponent(buttonColorShadow, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
            .addComponent(textFieldShadowColor, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
          .addContainerGap(64, Short.MAX_VALUE))
    );
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
    this.textFieldDescription.setText(map.getStringValue(MapProperty.MAP_DESCRIPTION));
    this.textFieldTitle.setText(map.getStringValue(MapProperty.MAP_TITLE));
    this.textFieldName.setText(map.getName());
    if (map.getStringValue(MapProperty.AMBIENTCOLOR) != null) {
      final String hexColor = map.getStringValue(MapProperty.AMBIENTCOLOR);
      this.textFieldAmbientColor.setText(hexColor);
      this.spinnerAmbientAlpha.setValue(ColorHelper.decode(hexColor).getAlpha());
    }
    if (map.getStringValue(MapProperty.SHADOWCOLOR) != null) {
      final String hexColor = map.getStringValue(MapProperty.SHADOWCOLOR);
      this.textFieldShadowColor.setText(hexColor);
      this.spinnerShadowAlpha.setValue(ColorHelper.decode(hexColor).getAlpha());
    }
    
    this.spinnerGravity.setValue(map.getIntValue(MapProperty.GRAVITY));
  }

  public void saveChanges() {
    if (this.dataSource == null) {
      return;
    }

    this.dataSource.setValue(MapProperty.MAP_DESCRIPTION, this.textFieldDescription.getText());
    this.dataSource.setValue(MapProperty.MAP_TITLE, this.textFieldTitle.getText());
    this.dataSource.setValue(MapProperty.GRAVITY, (int)this.spinnerGravity.getValue());
    this.dataSource.setValue(MapProperty.AMBIENTCOLOR, this.textFieldAmbientColor.getText());
    this.dataSource.setValue(MapProperty.SHADOWCOLOR, this.textFieldShadowColor.getText());
    this.dataSource.setName(this.textFieldName.getText());
  }
}

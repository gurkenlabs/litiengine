package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitterData;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleColor;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleType;

public class EmitterPropertyPanel extends JPanel {
  private transient CustomEmitter dataSource;
  DefaultTableModel model;
  private JTextField txtIffffff;
  private JButton btnSelectColor;
  private JTable table;
  private List<ParticleColor> colors;
  private CustomEmitterData controlValues;

  /**
   * Create the dialog.
   */
  public EmitterPropertyPanel() {
    setBounds(100, 100, 681, 483);
    this.setBorder(new EmptyBorder(5, 5, 5, 5));

    colors = new ArrayList<>();

    JLabel label = new JLabel(Resources.get("panel_emitterData"));
    label.setFont(new Font("Tahoma", Font.BOLD, 12));

    JLabel lblSpawnRate = new JLabel(Resources.get("panel_emitterSpawnRate"));

    JSpinner spinner = new JSpinner();

    JLabel lblSpawnAmount = new JLabel(Resources.get("panel_emitterSpawnAmount"));

    JSpinner spinner_1 = new JSpinner();

    JSpinner spinner_2 = new JSpinner();

    JLabel lblParticleData = new JLabel(Resources.get("panel_particleData"));
    lblParticleData.setFont(new Font("Tahoma", Font.BOLD, 12));

    JLabel lblDeltax = new JLabel(Resources.get("panel_particleDeltaX"));
    lblDeltax.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltax.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblDeltaY = new JLabel(Resources.get("panel_particleDeltaY"));
    lblDeltaY.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltaY.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JSpinner spinner_11 = new JSpinner();

    JSpinner spinner_12 = new JSpinner();

    JLabel lblMin_1 = new JLabel(Resources.get("panel_min"));
    lblMin_1.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_1.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMin_2 = new JLabel(Resources.get("panel_min"));
    lblMin_2.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_2.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblLock = new JLabel(Resources.get("panel_lock"));
    lblLock.setHorizontalAlignment(SwingConstants.LEFT);
    lblLock.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JRadioButton rdbtnNewRadioButton = new JRadioButton("");
    rdbtnNewRadioButton.setHorizontalAlignment(SwingConstants.CENTER);
    rdbtnNewRadioButton.setSelected(true);

    JLabel lblRandom = new JLabel(Resources.get("panel_random"));
    lblRandom.setHorizontalAlignment(SwingConstants.CENTER);
    lblRandom.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JRadioButton radioButton = new JRadioButton("");

    JRadioButton radioButton_1 = new JRadioButton("");
    radioButton_1.setSelected(true);

    JRadioButton radioButton_2 = new JRadioButton("");

    JLabel lblMax = new JLabel(Resources.get("panel_max"));
    lblMax.setEnabled(false);
    lblMax.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMax_1 = new JLabel(Resources.get("panel_max"));
    lblMax_1.setEnabled(false);
    lblMax_1.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_1.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JSpinner spinner_23 = new JSpinner();
    spinner_23.setEnabled(false);
    setLayout(new FormLayout(new ColumnSpec[] {
        ColumnSpec.decode("24px"),
        ColumnSpec.decode("40px"),
        ColumnSpec.decode("40px"),
        ColumnSpec.decode("80px:grow"),
        ColumnSpec.decode("100px"),
        ColumnSpec.decode("18px"),
        ColumnSpec.decode("80px:grow"),
        ColumnSpec.decode("40px"),
        ColumnSpec.decode("40px"),
        ColumnSpec.decode("40px"),
        ColumnSpec.decode("40px"),
        ColumnSpec.decode("20px"),
        ColumnSpec.decode("40px"),
        ColumnSpec.decode("40px"), },
        new RowSpec[] {
            RowSpec.decode("26px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"), }));
    add(label, "2, 2, 2, 1, left, center");
    add(spinner, "4, 3, fill, center");
    add(lblSpawnRate, "2, 3, 2, 1, left, center");
    add(lblSpawnAmount, "2, 4, 2, 1, left, center");
    add(spinner_1, "4, 4, fill, center");

    JSpinner spinner_22 = new JSpinner();
    spinner_22.setEnabled(false);
    add(spinner_22, "11, 4, fill, center");

    JLabel lblUpdateDelay = new JLabel(Resources.get("panel_emitterUpdateDelay"));
    add(lblUpdateDelay, "2, 5, 2, 1, left, center");

    JLabel lblGravityX = new JLabel(Resources.get("panel_particleGravityX"));
    lblGravityX.setHorizontalAlignment(SwingConstants.LEFT);
    lblGravityX.setFont(new Font("Tahoma", Font.PLAIN, 11));
    add(lblGravityX, "7, 5, fill, center");

    JLabel lblMin_3 = new JLabel(Resources.get("panel_min"));
    lblMin_3.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_3.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMin_3, "8, 5, fill, center");

    JSpinner spinner_10 = new JSpinner();
    add(spinner_10, "9, 5, fill, center");

    JLabel lblMax_2 = new JLabel(Resources.get("panel_max"));
    lblMax_2.setEnabled(false);
    lblMax_2.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_2.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMax_2, "10, 5, fill, center");

    JSpinner spinner_21 = new JSpinner();
    spinner_21.setEnabled(false);
    add(spinner_21, "11, 5, fill, center");

    JRadioButton radioButton_3 = new JRadioButton("");
    radioButton_3.setSelected(true);
    add(radioButton_3, "13, 5, center, center");

    JRadioButton radioButton_9 = new JRadioButton("");
    add(radioButton_9, "14, 5, center, center");

    JLabel label_5 = new JLabel(Resources.get("panel_emitterTTL"));
    add(label_5, "2, 6, 2, 1, left, center");

    JSpinner spinner_3 = new JSpinner();
    add(spinner_3, "4, 6, fill, center");

    JLabel lblGravityY = new JLabel(Resources.get("panel_particleGravityY"));
    lblGravityY.setHorizontalAlignment(SwingConstants.LEFT);
    lblGravityY.setFont(new Font("Tahoma", Font.PLAIN, 11));
    add(lblGravityY, "7, 6, fill, center");

    JLabel lblMin_4 = new JLabel(Resources.get("panel_min"));
    lblMin_4.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_4.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMin_4, "8, 6, fill, center");

    JSpinner spinner_9 = new JSpinner();
    add(spinner_9, "9, 6, fill, center");

    JLabel lblMax_3 = new JLabel(Resources.get("panel_max"));
    lblMax_3.setEnabled(false);
    lblMax_3.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_3.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMax_3, "10, 6, fill, center");

    JSpinner spinner_20 = new JSpinner();
    spinner_20.setEnabled(false);
    add(spinner_20, "11, 6, fill, center");

    JRadioButton radioButton_4 = new JRadioButton("");
    radioButton_4.setSelected(true);
    add(radioButton_4, "13, 6, center, center");

    JRadioButton radioButton_10 = new JRadioButton("");
    add(radioButton_10, "14, 6, center, center");
    add(spinner_2, "4, 5, fill, center");

    JLabel lblMaxParticles = new JLabel(Resources.get("panel_emitterMaxParticles"));
    add(lblMaxParticles, "2, 7, 2, 1, left, center");

    JSpinner spinner_4 = new JSpinner();
    add(spinner_4, "4, 7, fill, center");

    JLabel lblStartWidth = new JLabel(Resources.get("panel_particleStartWidth"));
    lblStartWidth.setHorizontalAlignment(SwingConstants.LEFT);
    lblStartWidth.setFont(new Font("Tahoma", Font.PLAIN, 11));
    add(lblStartWidth, "7, 7, fill, center");

    JLabel lblMin_5 = new JLabel(Resources.get("panel_min"));
    lblMin_5.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_5.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMin_5, "8, 7, fill, center");

    JSpinner spinner_8 = new JSpinner();
    spinner_8.setModel(new SpinnerNumberModel(new Integer(2), null, null, new Integer(1)));
    add(spinner_8, "9, 7, fill, center");

    JLabel lblMax_4 = new JLabel(Resources.get("panel_max"));
    lblMax_4.setEnabled(false);
    lblMax_4.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_4.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMax_4, "10, 7, fill, center");

    JSpinner spinner_19 = new JSpinner();
    spinner_19.setEnabled(false);
    add(spinner_19, "11, 7, fill, center");

    JRadioButton radioButton_5 = new JRadioButton("");
    radioButton_5.setSelected(true);
    add(radioButton_5, "13, 7, center, center");

    JRadioButton radioButton_11 = new JRadioButton("");
    add(radioButton_11, "14, 7, center, center");

    JLabel lblParticleType = new JLabel("particle type");
    add(lblParticleType, "2, 8, 2, 1, left, center");

    JComboBox comboBoxParticleType = new JComboBox();
    comboBoxParticleType.setModel((ComboBoxModel) new DefaultComboBoxModel(ParticleType.values()));
    add(comboBoxParticleType, "4, 8, 2, 1, fill, center");

    JLabel lblStartHeight = new JLabel("start height");
    lblStartHeight.setHorizontalAlignment(SwingConstants.LEFT);
    lblStartHeight.setFont(new Font("Tahoma", Font.PLAIN, 11));
    add(lblStartHeight, "7, 8, fill, center");

    JLabel lblMin_6 = new JLabel("min");
    lblMin_6.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_6.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMin_6, "8, 8, fill, center");

    JSpinner spinner_13 = new JSpinner();
    spinner_13.setModel(new SpinnerNumberModel(new Integer(2), null, null, new Integer(1)));
    add(spinner_13, "9, 8, fill, center");

    JLabel lblMax_5 = new JLabel("max");
    lblMax_5.setEnabled(false);
    lblMax_5.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_5.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMax_5, "10, 8, fill, center");

    JSpinner spinner_18 = new JSpinner();
    spinner_18.setEnabled(false);
    add(spinner_18, "11, 8, fill, center");

    JRadioButton radioButton_6 = new JRadioButton("");
    radioButton_6.setSelected(true);
    add(radioButton_6, "13, 8, center, center");

    JRadioButton radioButton_13 = new JRadioButton("");
    add(radioButton_13, "14, 8, center, center");

    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
    tabbedPane.setBorder(null);
    add(tabbedPane, "2, 9, 4, 7, fill, fill");

    JPanel colorPanel = new JPanel();
    tabbedPane.addTab("color", null, colorPanel, null);
    tabbedPane.setEnabledAt(0, true);
    colorPanel.setLayout(new FormLayout(new ColumnSpec[] {
        ColumnSpec.decode("10px"),
        ColumnSpec.decode("75px"),
        ColumnSpec.decode("45px"),
        ColumnSpec.decode("70px"),
        ColumnSpec.decode("70px"), },
        new RowSpec[] {
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"),
            RowSpec.decode("30px"), }));

    btnSelectColor = new JButton();
    colorPanel.add(btnSelectColor, "2, 1, 2, 2, fill, fill");
    btnSelectColor.setIcon(new ImageIcon(RenderEngine.getImage(Resources.get("button-color.png"))));
    btnSelectColor.setMinimumSize(new Dimension(30, 10));
    btnSelectColor.setMaximumSize(new Dimension(30, 10));

    JScrollPane scrollPane = new JScrollPane();
    colorPanel.add(scrollPane, "4, 1, 2, 6, fill, default");

    table = new JTable();
    table.setModel(new DefaultTableModel(
        new Object[][] {
        },
        new String[] {
            "percentage", "color"
        }));
    model = (DefaultTableModel) table.getModel();
    table.setFont(Program.TEXT_FONT);
    scrollPane.setViewportView(table);

    JButton buttonPlus = new JButton("+");
    colorPanel.add(buttonPlus, "2, 3, 2, 1, default, bottom");

    JButton buttonMinus = new JButton("-");
    colorPanel.add(buttonMinus, "2, 4, 2, 1, default, top");

    JLabel lblColorDeviation = new JLabel("color deviation");
    colorPanel.add(lblColorDeviation, "2, 5, left, center");

    JSpinner spinner_5 = new JSpinner();
    colorPanel.add(spinner_5, "3, 5, fill, center");
    spinner_5.setModel(new SpinnerNumberModel(new Float(0), null, new Float(1), new Float(0.01)));

    JLabel lblAlphaDeviation = new JLabel("alpha deviation");
    colorPanel.add(lblAlphaDeviation, "2, 6, left, center");

    JSpinner spinner_6 = new JSpinner();
    colorPanel.add(spinner_6, "3, 6, fill, center");
    spinner_6.setModel(new SpinnerNumberModel(new Float(0), null, new Float(1), new Float(0.01)));

    JPanel spritePanel = new JPanel();
    tabbedPane.addTab("sprite", null, spritePanel, null);
    tabbedPane.setEnabledAt(1, false);
    spritePanel.setLayout(new FormLayout(new ColumnSpec[] {
        ColumnSpec.decode("10px"),
        ColumnSpec.decode("60px"),
        ColumnSpec.decode("default:grow"),
        ColumnSpec.decode("10px"),},
      new RowSpec[] {
        RowSpec.decode("30px"),
        RowSpec.decode("30px"),
        RowSpec.decode("30px"),
        RowSpec.decode("30px"),
        RowSpec.decode("30px"),}));

    JLabel lblEmittertype = new JLabel("sprite type");
    spritePanel.add(lblEmittertype, "2, 1, left, center");

    JComboBox comboBox = new JComboBox();
    comboBox.addItem("animation");
    comboBox.addItem("random sprite");
    comboBox.setSelectedIndex(0);
    spritePanel.add(comboBox, "3, 1, fill, center");
    buttonMinus.addActionListener(a -> {
      for (int removeIndex = 0; removeIndex < colors.size(); removeIndex++) {
        if (removeIndex == table.getSelectedRow()) {
          colors.remove(removeIndex);
          break;
        }
      }

      model.removeRow(table.getSelectedRow());
    });
    buttonPlus.addActionListener(a -> {
      ParticleColor c = new ParticleColor();
      colors.add(c);
      model.addRow(new Object[] { c.toString() });
    });

    JLabel lblDeltaWidth = new JLabel("delta width");
    lblDeltaWidth.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltaWidth.setFont(new Font("Tahoma", Font.PLAIN, 11));
    add(lblDeltaWidth, "7, 9, fill, center");

    JLabel lblMin_7 = new JLabel("min");
    lblMin_7.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_7.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMin_7, "8, 9, fill, center");

    JSpinner spinner_14 = new JSpinner();
    add(spinner_14, "9, 9, fill, center");

    JLabel lblMax_6 = new JLabel("max");
    lblMax_6.setEnabled(false);
    lblMax_6.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_6.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMax_6, "10, 9, fill, center");

    JSpinner spinner_17 = new JSpinner();
    spinner_17.setEnabled(false);
    add(spinner_17, "11, 9, fill, center");

    JRadioButton radioButton_8 = new JRadioButton("");
    radioButton_8.setSelected(true);
    add(radioButton_8, "13, 9, center, center");

    JRadioButton radioButton_14 = new JRadioButton("");
    add(radioButton_14, "14, 9, center, center");

    JLabel lblDeltaHeight = new JLabel("delta height");
    lblDeltaHeight.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltaHeight.setFont(new Font("Tahoma", Font.PLAIN, 11));
    add(lblDeltaHeight, "7, 10, left, center");

    JLabel lblMin = new JLabel("min");
    lblMin.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMin, "8, 10, fill, center");

    JSpinner spinner_15 = new JSpinner();
    add(spinner_15, "9, 10, fill, center");

    JLabel lblMax_7 = new JLabel("max");
    lblMax_7.setEnabled(false);
    lblMax_7.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_7.setFont(new Font("Tahoma", Font.ITALIC, 11));
    add(lblMax_7, "10, 10, fill, center");

    JSpinner spinner_16 = new JSpinner();
    spinner_16.setEnabled(false);
    add(spinner_16, "11, 10, fill, center");

    JRadioButton radioButton_7 = new JRadioButton("");
    radioButton_7.setSelected(true);
    add(radioButton_7, "13, 10, center, center");

    JRadioButton radioButton_12 = new JRadioButton("");
    add(radioButton_12, "14, 10, center, center");

    JLabel lblStaticPhysics = new JLabel("static physics");
    lblStaticPhysics.setHorizontalAlignment(SwingConstants.LEFT);
    lblStaticPhysics.setFont(new Font("Tahoma", Font.PLAIN, 11));
    add(lblStaticPhysics, "7, 11, fill, center");

    JCheckBox chckbxNewCheckBox = new JCheckBox("");
    add(chckbxNewCheckBox, "9, 11, center, center");

    JLabel lblText = new JLabel("text");
    lblText.setEnabled(false);
    lblText.setHorizontalAlignment(SwingConstants.LEFT);
    lblText.setFont(new Font("Tahoma", Font.PLAIN, 11));
    add(lblText, "7, 12, fill, center");
    add(lblDeltaY, "7, 4, fill, center");
    add(lblDeltax, "7, 3, fill, center");
    add(lblMin_2, "8, 4, fill, center");
    add(lblMin_1, "8, 3, fill, center");

    txtIffffff = new JTextField();
    txtIffffff.setEnabled(false);
    txtIffffff.setText("IFFF");
    txtIffffff.setColumns(10);
    add(txtIffffff, "8, 12, 2, 1, fill, center");
    add(spinner_11, "9, 4, fill, center");
    add(spinner_12, "9, 3, fill, center");
    add(lblMax, "10, 3, fill, center");
    add(spinner_23, "11, 3, fill, center");
    add(rdbtnNewRadioButton, "13, 3, center, center");
    add(radioButton, "14, 3, center, center");
    add(lblMax_1, "10, 4, fill, center");
    add(radioButton_1, "13, 4, center, center");
    add(radioButton_2, "14, 4, center, center");
    add(lblParticleData, "7, 2, left, center");
    add(lblLock, "13, 2, center, center");
    add(lblRandom, "14, 2, center, center");
    this.setupChangedListeners();

  }

  public void bind(CustomEmitter emitter) {
    this.dataSource = emitter;
    if (emitter == null) {
      return;
    }

    this.setControlValues(emitter);

  }

  public void discardChanges() {
    this.dataSource.setEmitterData(controlValues);
  }

  private void setupChangedListeners() {
    btnSelectColor.addActionListener(a -> {
      Color result = JColorChooser.showDialog(null, Resources.get("panel_selectEmitterColor"), colors.get(table.getSelectedRow()).toColor());
      if (result == null) {
        return;
      }

      ParticleColor c = new ParticleColor(result);
      colors.set(table.getSelectedRow(), c);
      model.setValueAt(c, table.getSelectedRow(), 1);
      if (getDataSource() != null) {
        getDataSource().getEmitterData().setColors(colors);
      }
    });
  }

  private CustomEmitter getDataSource() {
    return this.dataSource;
  }

  private void setControlValues(CustomEmitter emitter) {

  }
}

package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import javax.swing.plaf.basic.BasicBorders.RadioButtonBorder;
import javax.swing.table.DefaultTableModel;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleColor;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleType;

public class EmitterPropertyPanel extends PropertyPanel<IMapObject> {
  DefaultTableModel model;
  private JTextField txt;
  private JButton btnSelectColor;
  private JTable table;
  private List<ParticleColor> colors;
  private IMapObject backupMapObject;
  private JTabbedPane tabbedPanel;
  private JPanel colorPanel;
  private JPanel spritePanel;
  private static DecimalFormat df = new DecimalFormat("#.00");
  private JRadioButton rdbtnLockDeltaX, rdbtnLockDeltaY, rdbtnLockGravityX, rdbtnLockGravityY, rdbtnLockStartWidth, rdbtnLockStartHeight, rdbtnLockDeltaWidth, rdbtnLockDeltaHeight;
  private JRadioButton rdbtnRandomDeltaX, rdbtnRandomDeltaY, rdbtnRandomGravityX, rdbtnRandomGravityY, rdbtnRandomStartWidth, rdbtnRandomStartHeight, rdbtnRandomDeltaWidth, rdbtnRandomDeltaHeight;
  private JCheckBox chckbxStaticPhysics;
  private JComboBox comboBoxParticleType;
  /**
   * @wbp.nonvisual location=572,339
   */
  private final ButtonGroup buttonGroupDeltaX = new ButtonGroup();
  private final ButtonGroup buttonGroupDeltaY = new ButtonGroup();
  private final ButtonGroup buttonGroupGravityX = new ButtonGroup();
  private final ButtonGroup buttonGroupGravityY = new ButtonGroup();
  private final ButtonGroup buttonGroupStartWidth = new ButtonGroup();
  private final ButtonGroup buttonGroupStartHeight = new ButtonGroup();
  private final ButtonGroup buttonGroupDeltaWidth = new ButtonGroup();
  private final ButtonGroup buttonGroupDeltaHeight = new ButtonGroup();
  private JSpinner spinnerSpawnRate, spinnerSpawnAmount, spinnerUpdateDelay, spinnerTTL, spinnerMaxParticles, spinnerColorDeviation, spinnerAlphaDeviation;
  private JSpinner spinnerMinDeltaX, spinnerMinDeltaY, spinnerMinGravityX, spinnerMinGravityY, spinnerMinStartWidth, spinnerMinStartHeight, spinnerMinDeltaWidth, spinnerMinDeltaHeight;
  private JSpinner spinnerMaxDeltaX, spinnerMaxDeltaY, spinnerMaxGravityX, spinnerMaxGravityY, spinnerMaxStartWidth, spinnerMaxStartHeight, spinnerMaxDeltaWidth, spinnerMaxDeltaHeight;

  /**
   * Create the dialog.
   */
  public EmitterPropertyPanel() {
    super();
    setBounds(100, 100, 700, 464);
    this.setBorder(new EmptyBorder(5, 5, 5, 5));

    colors = new ArrayList<>();
    JLabel label = new JLabel(Resources.get("panel_emitterData"));
    label.setFont(new Font("Tahoma", Font.BOLD, 12));

    JLabel lblSpawnRate = new JLabel(Resources.get("panel_emitterSpawnRate"));

    spinnerSpawnRate = new JSpinner();

    JLabel lblSpawnAmount = new JLabel(Resources.get("panel_emitterSpawnAmount"));

    spinnerSpawnAmount = new JSpinner();

    spinnerUpdateDelay = new JSpinner();

    JLabel lblParticleData = new JLabel(Resources.get("panel_particleData"));
    lblParticleData.setFont(new Font("Tahoma", Font.BOLD, 12));

    JLabel lblDeltax = new JLabel(Resources.get("panel_particleDeltaX"));
    lblDeltax.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltax.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblDeltaY = new JLabel(Resources.get("panel_particleDeltaY"));
    lblDeltaY.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltaY.setFont(new Font("Tahoma", Font.PLAIN, 11));

    spinnerMinDeltaY = new JSpinner();

    spinnerMinDeltaX = new JSpinner();

    JLabel lblMin_1 = new JLabel(Resources.get("panel_min"));
    lblMin_1.setEnabled(false);
    lblMin_1.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_1.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMin_2 = new JLabel(Resources.get("panel_min"));
    lblMin_2.setEnabled(false);
    lblMin_2.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_2.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblLock = new JLabel(Resources.get("panel_lock"));
    lblLock.setHorizontalAlignment(SwingConstants.LEFT);
    lblLock.setFont(new Font("Tahoma", Font.PLAIN, 11));

    rdbtnLockDeltaX = new JRadioButton("");
    buttonGroupDeltaX.add(rdbtnLockDeltaX);
    rdbtnLockDeltaX.setHorizontalAlignment(SwingConstants.CENTER);
    rdbtnLockDeltaX.setSelected(true);

    JLabel lblRandom = new JLabel(Resources.get("panel_random"));
    lblRandom.setHorizontalAlignment(SwingConstants.CENTER);
    lblRandom.setFont(new Font("Tahoma", Font.PLAIN, 11));

    rdbtnRandomDeltaX = new JRadioButton("");
    buttonGroupDeltaX.add(rdbtnRandomDeltaX);

    rdbtnLockDeltaY = new JRadioButton("");
    buttonGroupDeltaY.add(rdbtnLockDeltaY);
    rdbtnLockDeltaY.setSelected(true);

    rdbtnRandomDeltaY = new JRadioButton("");
    buttonGroupDeltaY.add(rdbtnRandomDeltaY);

    JLabel lblMax = new JLabel(Resources.get("panel_max"));
    lblMax.setEnabled(false);
    lblMax.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMax_1 = new JLabel(Resources.get("panel_max"));
    lblMax_1.setEnabled(false);
    lblMax_1.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_1.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxDeltaX = new JSpinner();
    spinnerMaxDeltaX.setEnabled(false);

    spinnerMaxDeltaY = new JSpinner();
    spinnerMaxDeltaY.setEnabled(false);

    JLabel lblUpdateDelay = new JLabel(Resources.get("panel_emitterUpdateDelay"));

    JLabel lblGravityX = new JLabel(Resources.get("panel_particleGravityX"));
    lblGravityX.setHorizontalAlignment(SwingConstants.LEFT);
    lblGravityX.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin_3 = new JLabel(Resources.get("panel_min"));
    lblMin_3.setEnabled(false);
    lblMin_3.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_3.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMinGravityX = new JSpinner();

    JLabel lblMax_2 = new JLabel(Resources.get("panel_max"));
    lblMax_2.setEnabled(false);
    lblMax_2.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_2.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxGravityX = new JSpinner();
    spinnerMaxGravityX.setEnabled(false);

    rdbtnLockGravityX = new JRadioButton("");
    buttonGroupGravityX.add(rdbtnLockGravityX);
    rdbtnLockGravityX.setSelected(true);

    rdbtnRandomGravityX = new JRadioButton("");
    buttonGroupGravityX.add(rdbtnRandomGravityX);
    JLabel label_5 = new JLabel(Resources.get("panel_emitterTTL"));

    spinnerTTL = new JSpinner();

    JLabel lblGravityY = new JLabel(Resources.get("panel_particleGravityY"));
    lblGravityY.setHorizontalAlignment(SwingConstants.LEFT);
    lblGravityY.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin_4 = new JLabel(Resources.get("panel_min"));
    lblMin_4.setEnabled(false);
    lblMin_4.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_4.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMinGravityY = new JSpinner();

    JLabel lblMax_3 = new JLabel(Resources.get("panel_max"));
    lblMax_3.setEnabled(false);
    lblMax_3.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_3.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxGravityY = new JSpinner();
    spinnerMaxGravityY.setEnabled(false);

    rdbtnLockGravityY = new JRadioButton("");
    buttonGroupGravityY.add(rdbtnLockGravityY);
    rdbtnLockGravityY.setSelected(true);

    rdbtnRandomGravityY = new JRadioButton("");
    buttonGroupGravityY.add(rdbtnRandomGravityY);
    JLabel lblMaxParticles = new JLabel(Resources.get("panel_emitterMaxParticles"));

    spinnerMaxParticles = new JSpinner();

    JLabel lblStartWidth = new JLabel(Resources.get("panel_particleStartWidth"));
    lblStartWidth.setEnabled(false);
    lblStartWidth.setHorizontalAlignment(SwingConstants.LEFT);
    lblStartWidth.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin_5 = new JLabel(Resources.get("panel_min"));
    lblMin_5.setEnabled(false);
    lblMin_5.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_5.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMinStartWidth = new JSpinner();
    spinnerMinStartWidth.setEnabled(false);
    spinnerMinStartWidth.setModel(new SpinnerNumberModel(new Integer(2), null, null, new Integer(1)));

    JLabel lblMax_4 = new JLabel(Resources.get("panel_max"));
    lblMax_4.setEnabled(false);
    lblMax_4.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_4.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxStartWidth = new JSpinner();
    spinnerMaxStartWidth.setEnabled(false);

    rdbtnLockStartWidth = new JRadioButton("");
    rdbtnLockStartWidth.setEnabled(false);
    buttonGroupStartWidth.add(rdbtnLockStartWidth);
    rdbtnLockStartWidth.setSelected(true);

    rdbtnRandomStartWidth = new JRadioButton("");
    rdbtnRandomStartWidth.setEnabled(false);
    buttonGroupStartWidth.add(rdbtnRandomStartWidth);
    JLabel lblParticleType = new JLabel(Resources.get("panel_particleType"));

    comboBoxParticleType = new JComboBox();
    comboBoxParticleType.setModel((ComboBoxModel) new DefaultComboBoxModel(ParticleType.values()));

    JLabel lblStartHeight = new JLabel(Resources.get("panel_particleStartHeight"));
    lblStartHeight.setEnabled(false);
    lblStartHeight.setHorizontalAlignment(SwingConstants.LEFT);
    lblStartHeight.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin_6 = new JLabel(Resources.get("panel_min"));
    lblMin_6.setEnabled(false);
    lblMin_6.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_6.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMinStartHeight = new JSpinner();
    spinnerMinStartHeight.setEnabled(false);
    spinnerMinStartHeight.setModel(new SpinnerNumberModel(new Integer(2), null, null, new Integer(1)));

    JLabel lblMax_5 = new JLabel(Resources.get("panel_max"));
    lblMax_5.setEnabled(false);
    lblMax_5.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_5.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxStartHeight = new JSpinner();
    spinnerMaxStartHeight.setEnabled(false);

    rdbtnLockStartHeight = new JRadioButton("");
    rdbtnLockStartHeight.setEnabled(false);
    buttonGroupStartHeight.add(rdbtnLockStartHeight);
    rdbtnLockStartHeight.setSelected(true);

    rdbtnRandomStartHeight = new JRadioButton("");
    rdbtnRandomStartHeight.setEnabled(false);
    buttonGroupStartHeight.add(rdbtnRandomStartHeight);
    tabbedPanel = new JTabbedPane(JTabbedPane.TOP);
    tabbedPanel.setBorder(null);

    colorPanel = new JPanel();
    tabbedPanel.addTab("color", null, colorPanel, null);
    tabbedPanel.setEnabledAt(0, true);

    btnSelectColor = new JButton();
    btnSelectColor.setIcon(new ImageIcon(Resources.getImage("button-color.png")));
    btnSelectColor.setMinimumSize(new Dimension(30, 10));
    btnSelectColor.setMaximumSize(new Dimension(30, 10));

    JScrollPane scrollPane = new JScrollPane();

    table = new JTable();
    table.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "percentage", "color" }));
    model = (DefaultTableModel) table.getModel();
    table.setFont(Program.TEXT_FONT);
    scrollPane.setViewportView(table);

    JButton buttonPlus = new JButton("+");

    JButton buttonMinus = new JButton("-");

    JLabel lblColorDeviation = new JLabel(Resources.get("panel_colorDeviation"));

    spinnerColorDeviation = new JSpinner();
    spinnerColorDeviation.setModel(new SpinnerNumberModel(new Float(0), null, new Float(1), new Float(0.01)));

    JLabel lblAlphaDeviation = new JLabel(Resources.get("panel_alphaDeviation"));

    spinnerAlphaDeviation = new JSpinner();
    spinnerAlphaDeviation.setModel(new SpinnerNumberModel(new Float(0), null, new Float(1), new Float(0.01)));

    GroupLayout gl_colorPanel = new GroupLayout(colorPanel);
    gl_colorPanel.setHorizontalGroup(gl_colorPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_colorPanel.createSequentialGroup().addGap(10)
            .addGroup(gl_colorPanel.createParallelGroup(Alignment.LEADING).addComponent(btnSelectColor, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE).addComponent(buttonPlus, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addComponent(buttonMinus, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addGroup(gl_colorPanel.createSequentialGroup().addComponent(lblColorDeviation, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE).addComponent(spinnerColorDeviation, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
                .addGroup(gl_colorPanel.createSequentialGroup().addComponent(lblAlphaDeviation, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE).addComponent(spinnerAlphaDeviation, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)))
            .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)));
    gl_colorPanel.setVerticalGroup(gl_colorPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_colorPanel.createSequentialGroup().addComponent(btnSelectColor, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE).addGap(7).addComponent(buttonPlus).addComponent(buttonMinus).addGap(12)
            .addGroup(gl_colorPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_colorPanel.createSequentialGroup().addGap(3).addComponent(lblColorDeviation)).addComponent(spinnerColorDeviation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(10)
            .addGroup(gl_colorPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_colorPanel.createSequentialGroup().addGap(3).addComponent(lblAlphaDeviation)).addComponent(spinnerAlphaDeviation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE));
    colorPanel.setLayout(gl_colorPanel);

    spritePanel = new JPanel();
    tabbedPanel.addTab("sprite", null, spritePanel, null);
    tabbedPanel.setEnabledAt(1, false);

    JLabel lblEmittertype = new JLabel(Resources.get("panel_spriteType"));

    JComboBox comboBox = new JComboBox();
    comboBox.addItem("animation");
    comboBox.addItem("random sprite");
    comboBox.setSelectedIndex(0);

    JLabel lblSpritesheet = new JLabel(Resources.get("panel_sprite"));

    JComboBox comboBox_1 = new JComboBox();
    for (SpriteSheetInfo s : EditorScreen.instance().getGameFile().getSpriteSheets()) {
      comboBox_1.addItem(s.getName());
    }
    GroupLayout gl_spritePanel = new GroupLayout(spritePanel);
    gl_spritePanel.setHorizontalGroup(gl_spritePanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_spritePanel.createSequentialGroup().addGap(10)
            .addGroup(gl_spritePanel.createParallelGroup(Alignment.LEADING).addGroup(gl_spritePanel.createSequentialGroup().addComponent(lblEmittertype, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE).addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE))
                .addGroup(gl_spritePanel.createSequentialGroup().addComponent(lblSpritesheet).addGap(43).addComponent(comboBox_1, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE)))));
    gl_spritePanel.setVerticalGroup(gl_spritePanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_spritePanel.createSequentialGroup().addGap(5)
            .addGroup(gl_spritePanel.createParallelGroup(Alignment.LEADING).addGroup(gl_spritePanel.createSequentialGroup().addGap(3).addComponent(lblEmittertype)).addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(10)
            .addGroup(gl_spritePanel.createParallelGroup(Alignment.LEADING).addGroup(gl_spritePanel.createSequentialGroup().addGap(3).addComponent(lblSpritesheet)).addComponent(comboBox_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))));
    spritePanel.setLayout(gl_spritePanel);
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

    JLabel lblDeltaWidth = new JLabel(Resources.get("panel_particleDeltaWidth"));
    lblDeltaWidth.setEnabled(false);
    lblDeltaWidth.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltaWidth.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin_7 = new JLabel(Resources.get("panel_min"));
    lblMin_7.setEnabled(false);
    lblMin_7.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin_7.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMinDeltaWidth = new JSpinner();
    spinnerMinDeltaWidth.setEnabled(false);

    JLabel lblMax_6 = new JLabel(Resources.get("panel_max"));
    lblMax_6.setEnabled(false);
    lblMax_6.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_6.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxDeltaWidth = new JSpinner();
    spinnerMaxDeltaWidth.setEnabled(false);

    rdbtnLockDeltaWidth = new JRadioButton("");
    rdbtnLockDeltaWidth.setEnabled(false);
    buttonGroupDeltaWidth.add(rdbtnLockDeltaWidth);
    rdbtnLockDeltaWidth.setSelected(true);

    rdbtnRandomDeltaWidth = new JRadioButton("");
    rdbtnRandomDeltaWidth.setEnabled(false);
    buttonGroupDeltaWidth.add(rdbtnRandomDeltaWidth);
    JLabel lblDeltaHeight = new JLabel(Resources.get("panel_particleDeltaHeight"));
    lblDeltaHeight.setEnabled(false);
    lblDeltaHeight.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltaHeight.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin = new JLabel(Resources.get("panel_min"));
    lblMin.setEnabled(false);
    lblMin.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JSpinner spinnerMinDeltaHeight = new JSpinner();
    spinnerMinDeltaHeight.setEnabled(false);

    JLabel lblMax_7 = new JLabel(Resources.get("panel_max"));
    lblMax_7.setEnabled(false);
    lblMax_7.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax_7.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JSpinner spinnerMaxDeltaHeight = new JSpinner();
    spinnerMaxDeltaHeight.setEnabled(false);

    rdbtnLockDeltaHeight = new JRadioButton("");
    rdbtnLockDeltaHeight.setEnabled(false);
    buttonGroupDeltaHeight.add(rdbtnLockDeltaHeight);
    rdbtnLockDeltaHeight.setSelected(true);

    rdbtnRandomDeltaHeight = new JRadioButton("");
    rdbtnRandomDeltaHeight.setEnabled(false);
    buttonGroupDeltaHeight.add(rdbtnRandomDeltaHeight);

    JLabel lblStaticPhysics = new JLabel(Resources.get("panel_particleStaticPhysics"));
    lblStaticPhysics.setHorizontalAlignment(SwingConstants.LEFT);
    lblStaticPhysics.setFont(new Font("Tahoma", Font.PLAIN, 11));

    chckbxStaticPhysics = new JCheckBox("");

    JLabel lblText = new JLabel(Resources.get("panel_particleText"));
    lblText.setEnabled(false);
    lblText.setHorizontalAlignment(SwingConstants.LEFT);
    lblText.setFont(new Font("Tahoma", Font.PLAIN, 11));

    txt = new JTextField();
    txt.setEnabled(false);
    txt.setText("IFFF");
    txt.setColumns(10);
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGap(10)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(label).addGap(226).addComponent(lblParticleData).addGap(219).addComponent(lblLock).addGap(13).addComponent(lblRandom))
                .addGroup(groupLayout.createSequentialGroup().addComponent(lblSpawnRate).addGap(26).addComponent(spinnerSpawnRate, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE).addGap(118).addComponent(lblDeltax, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin_1, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinDeltaX, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxDeltaX, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockDeltaX).addGap(19).addComponent(rdbtnRandomDeltaX))
                .addGroup(groupLayout.createSequentialGroup().addComponent(lblSpawnAmount).addGap(10).addComponent(spinnerSpawnAmount, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE).addGap(118).addComponent(lblDeltaY, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin_2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinDeltaY, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax_1, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxDeltaY, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockDeltaY).addGap(19).addComponent(rdbtnRandomDeltaY))
                .addGroup(groupLayout.createSequentialGroup().addComponent(lblUpdateDelay).addGap(17).addComponent(spinnerUpdateDelay, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE).addGap(118).addComponent(lblGravityX, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin_3, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinGravityX, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax_2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxGravityX, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockGravityX).addGap(19).addComponent(rdbtnRandomGravityX))
                .addGroup(groupLayout.createSequentialGroup().addComponent(label_5).addGap(28).addComponent(spinnerTTL, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE).addGap(118).addComponent(lblGravityY, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin_4, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinGravityY, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax_3, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxGravityY, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockGravityY).addGap(19).addComponent(rdbtnRandomGravityY))
                .addGroup(groupLayout.createSequentialGroup().addComponent(lblMaxParticles).addGap(17).addComponent(spinnerMaxParticles, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE).addGap(118).addComponent(lblStartWidth, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin_5, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinStartWidth, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax_4, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxStartWidth, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockStartWidth).addGap(19).addComponent(rdbtnRandomStartWidth))
                .addGroup(groupLayout
                    .createSequentialGroup().addComponent(lblParticleType).addGap(20).addComponent(comboBoxParticleType, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE).addGap(18).addComponent(lblStartHeight, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin_6, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinStartHeight, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax_5, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxStartHeight, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockStartHeight).addGap(19).addComponent(rdbtnRandomStartHeight))
                .addGroup(groupLayout.createSequentialGroup().addComponent(tabbedPanel, GroupLayout.PREFERRED_SIZE, 286, GroupLayout.PREFERRED_SIZE).addGap(18)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblDeltaWidth, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE).addComponent(lblDeltaHeight).addComponent(lblStaticPhysics, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblText, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE))
                    .addGroup(
                        groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(lblMin_7, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinDeltaWidth, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                            .addGroup(groupLayout.createSequentialGroup().addComponent(lblMin, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinDeltaHeight, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                            .addGroup(groupLayout.createSequentialGroup().addGap(49).addComponent(chckbxStaticPhysics)).addComponent(txt, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblMax_6, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax_7, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(spinnerMaxDeltaWidth, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMaxDeltaHeight, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)).addGap(29)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(rdbtnLockDeltaWidth).addComponent(rdbtnLockDeltaHeight)).addGap(19).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(rdbtnRandomDeltaWidth).addComponent(rdbtnRandomDeltaHeight))))));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGap(33)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(label).addComponent(lblParticleData).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(lblLock)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(lblRandom))).addGap(12)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblSpawnRate))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerSpawnRate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblDeltax))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin_1)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinDeltaX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxDeltaX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(rdbtnLockDeltaX)
                .addComponent(rdbtnRandomDeltaX))
            .addGap(9)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblSpawnAmount))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerSpawnAmount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblDeltaY))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin_2)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinDeltaY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax_1)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxDeltaY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(rdbtnLockDeltaY)
                .addComponent(rdbtnRandomDeltaY))
            .addGap(9)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblUpdateDelay))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerUpdateDelay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblGravityX))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin_3)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinGravityX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax_2)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxGravityX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(rdbtnLockGravityX)
                .addComponent(rdbtnRandomGravityX))
            .addGap(9)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(label_5))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerTTL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblGravityY))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin_4)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinGravityY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax_3)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxGravityY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(rdbtnLockGravityY)
                .addComponent(rdbtnRandomGravityY))
            .addGap(9)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMaxParticles))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxParticles, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblStartWidth))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin_5)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinStartWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax_4)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxStartWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(rdbtnLockStartWidth).addComponent(rdbtnRandomStartWidth))
            .addGap(9)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblParticleType))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(comboBoxParticleType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblStartHeight))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin_6)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinStartHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax_5)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxStartHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(rdbtnLockStartHeight).addComponent(rdbtnRandomStartHeight))
            .addGap(5)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(tabbedPanel, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
                .addGroup(groupLayout.createSequentialGroup().addGap(8).addComponent(lblDeltaWidth).addGap(16).addComponent(lblDeltaHeight).addGap(16).addComponent(lblStaticPhysics).addGap(16).addComponent(lblText))
                .addGroup(groupLayout.createSequentialGroup().addGap(5)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(3).addComponent(lblMin_7)).addComponent(spinnerMinDeltaWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(10)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(3).addComponent(lblMin)).addComponent(spinnerMinDeltaHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(9)
                    .addComponent(chckbxStaticPhysics).addGap(10).addComponent(txt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(8).addComponent(lblMax_6).addGap(16).addComponent(lblMax_7))
                .addGroup(groupLayout.createSequentialGroup().addGap(5).addComponent(spinnerMaxDeltaWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(10).addComponent(spinnerMaxDeltaHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(rdbtnLockDeltaWidth).addGap(9).addComponent(rdbtnLockDeltaHeight)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(rdbtnRandomDeltaWidth).addGap(9).addComponent(rdbtnRandomDeltaHeight)))));
    setLayout(groupLayout);
    this.setupChangedListeners();

  }

  @Override
  public void bind(IMapObject mapObject) {
    // TODO Auto-generated method stub
    super.bind(mapObject);
  }

  public void discardChanges() {
    this.bind(this.backupMapObject);
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
        StringBuilder colorString = new StringBuilder();
        for (ParticleColor color : colors) {
          colorString.append(color.toHexString() + ",");
        }
        this.getDataSource().setCustomProperty(MapObjectProperty.EMITTERCOLORS, colorString.toString());
      }
    });

    spinnerSpawnRate.addChangeListener(v -> {
      if (this.getDataSource() != null) {
        this.getDataSource().setCustomProperty(MapObjectProperty.EMITTERSPAWNRATE, ((Integer) spinnerSpawnRate.getValue()).toString());
        Game.getEnvironment().reloadFromMap(this.getDataSource().getId());
      }
    });

    spinnerSpawnAmount.addChangeListener(v -> {
      if (this.getDataSource() != null) {
        this.getDataSource().setCustomProperty(MapObjectProperty.EMITTERSPAWNAMOUNT, ((Integer) spinnerSpawnAmount.getValue()).toString());
        Game.getEnvironment().reloadFromMap(this.getDataSource().getId());
      }
    });

    spinnerUpdateDelay.addChangeListener(v -> {
      if (this.getDataSource() != null) {
        this.getDataSource().setCustomProperty(MapObjectProperty.EMITTERUPDATEDELAY, ((Integer) spinnerUpdateDelay.getValue()).toString());
        Game.getEnvironment().reloadFromMap(this.getDataSource().getId());
      }
    });

    spinnerMinDeltaY.addChangeListener(v -> {
      if (this.getDataSource() != null) {
        if (rdbtnLockDeltaY.isSelected()) {

        }
        this.getDataSource().setCustomProperty(MapObjectProperty.PARTICLEDELTAY, ((Integer) spinnerMinDeltaY.getValue()).toString());
        Game.getEnvironment().reloadFromMap(this.getDataSource().getId());
      }
    });
  }

  @Override
  protected void clearControls() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    // TODO Auto-generated method stub

  }

}

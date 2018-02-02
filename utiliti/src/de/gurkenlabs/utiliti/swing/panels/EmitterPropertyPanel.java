package de.gurkenlabs.utiliti.swing.panels;

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

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleColor;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleType;
import de.gurkenlabs.utiliti.EditorScreen;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.swing.ColorChooser;
import de.gurkenlabs.utiliti.swing.panels.PropertyPanel.MapObjectPropertyActionListener;
import de.gurkenlabs.utiliti.swing.panels.PropertyPanel.MapObjectPropertyChangeListener;

@SuppressWarnings("serial")
public class EmitterPropertyPanel extends PropertyPanel<IMapObject> {
  private DefaultTableModel model;
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
  private JComboBox<ParticleType> comboBoxParticleType;

  private JButton btnAddColor, btnRemoveColor;
  private JComboBox<String> comboBoxSpriteType, comboBoxSprite;

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

  public static final int PARTICLESPINNER_MAX_VALUE=100;
  
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
    spinnerMinDeltaY.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));


    spinnerMinDeltaX = new JSpinner();
    spinnerMinDeltaX.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));


    JLabel lblMin1 = new JLabel(Resources.get("panel_min"));
    lblMin1.setEnabled(false);
    lblMin1.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin1.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMin2 = new JLabel(Resources.get("panel_min"));
    lblMin2.setEnabled(false);
    lblMin2.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin2.setFont(new Font("Tahoma", Font.ITALIC, 11));

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

    JLabel lblMax1 = new JLabel(Resources.get("panel_max"));
    lblMax1.setEnabled(false);
    lblMax1.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax1.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxDeltaX = new JSpinner();
    spinnerMaxDeltaX.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));
    spinnerMaxDeltaX.setEnabled(false);

    spinnerMaxDeltaY = new JSpinner();
    spinnerMaxDeltaY.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));
    spinnerMaxDeltaY.setEnabled(false);

    JLabel lblUpdateDelay = new JLabel(Resources.get("panel_emitterUpdateDelay"));

    JLabel lblGravityX = new JLabel(Resources.get("panel_particleGravityX"));
    lblGravityX.setHorizontalAlignment(SwingConstants.LEFT);
    lblGravityX.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin3 = new JLabel(Resources.get("panel_min"));
    lblMin3.setEnabled(false);
    lblMin3.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin3.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMinGravityX = new JSpinner();
    spinnerMinGravityX.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));

    JLabel lblMax2 = new JLabel(Resources.get("panel_max"));
    lblMax2.setEnabled(false);
    lblMax2.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax2.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxGravityX = new JSpinner();
    spinnerMaxGravityX.setEnabled(false);
    spinnerMaxGravityX.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));

    rdbtnLockGravityX = new JRadioButton("");
    buttonGroupGravityX.add(rdbtnLockGravityX);
    rdbtnLockGravityX.setSelected(true);

    rdbtnRandomGravityX = new JRadioButton("");
    buttonGroupGravityX.add(rdbtnRandomGravityX);
    JLabel labelTtl = new JLabel(Resources.get("panel_emitterTTL"));

    spinnerTTL = new JSpinner();

    JLabel lblGravityY = new JLabel(Resources.get("panel_particleGravityY"));
    lblGravityY.setHorizontalAlignment(SwingConstants.LEFT);
    lblGravityY.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin4 = new JLabel(Resources.get("panel_min"));
    lblMin4.setEnabled(false);
    lblMin4.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin4.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMinGravityY = new JSpinner();
    spinnerMinGravityY.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));

    JLabel lblMax3 = new JLabel(Resources.get("panel_max"));
    lblMax3.setEnabled(false);
    lblMax3.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax3.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxGravityY = new JSpinner();
    spinnerMaxGravityY.setEnabled(false);
    spinnerMaxGravityY.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));

    rdbtnLockGravityY = new JRadioButton("");
    buttonGroupGravityY.add(rdbtnLockGravityY);
    rdbtnLockGravityY.setSelected(true);

    rdbtnRandomGravityY = new JRadioButton("");
    buttonGroupGravityY.add(rdbtnRandomGravityY);
    JLabel lblMaxParticles = new JLabel(Resources.get("panel_emitterMaxParticles"));

    spinnerMaxParticles = new JSpinner();
    spinnerMaxParticles.setModel(new SpinnerNumberModel(0, 0, 0, 1));

    JLabel lblStartWidth = new JLabel(Resources.get("panel_particleStartWidth"));
    lblStartWidth.setEnabled(false);
    lblStartWidth.setHorizontalAlignment(SwingConstants.LEFT);
    lblStartWidth.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin5 = new JLabel(Resources.get("panel_min"));
    lblMin5.setEnabled(false);
    lblMin5.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin5.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMinStartWidth = new JSpinner();
    spinnerMinStartWidth.setEnabled(false);
    spinnerMinStartWidth.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));

    JLabel lblMax4 = new JLabel(Resources.get("panel_max"));
    lblMax4.setEnabled(false);
    lblMax4.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax4.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxStartWidth = new JSpinner();
    spinnerMaxStartWidth.setEnabled(false);
    spinnerMaxStartWidth.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));

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

    JLabel lblMin6 = new JLabel(Resources.get("panel_min"));
    lblMin6.setEnabled(false);
    lblMin6.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin6.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMinStartHeight = new JSpinner();
    spinnerMinStartHeight.setEnabled(false);
    spinnerMinStartHeight.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));

    JLabel lblMax5 = new JLabel(Resources.get("panel_max"));
    lblMax5.setEnabled(false);
    lblMax5.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax5.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxStartHeight = new JSpinner();
    spinnerMaxStartHeight.setEnabled(false);
    spinnerMaxStartHeight.setModel(new SpinnerNumberModel(0, 0, PARTICLESPINNER_MAX_VALUE, 1));

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
    tabbedPanel.addTab(Resources.get("panel_color"), null, colorPanel, null);
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

    btnAddColor = new JButton("+");

    btnRemoveColor = new JButton("-");

    JLabel lblColorDeviation = new JLabel(Resources.get("panel_colorDeviation"));

    spinnerColorDeviation = new JSpinner();
    spinnerColorDeviation.setModel(new SpinnerNumberModel(0, null, 1, 0.01f));

    JLabel lblAlphaDeviation = new JLabel(Resources.get("panel_alphaDeviation"));

    spinnerAlphaDeviation = new JSpinner();
    spinnerAlphaDeviation.setModel(new SpinnerNumberModel(0, null, 1, 0.01f));

    GroupLayout groupLayoutcolorPanel = new GroupLayout(colorPanel);
    groupLayoutcolorPanel.setHorizontalGroup(groupLayoutcolorPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayoutcolorPanel.createSequentialGroup().addGap(10)
            .addGroup(groupLayoutcolorPanel.createParallelGroup(Alignment.LEADING).addComponent(btnSelectColor, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE).addComponent(btnAddColor, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addComponent(btnRemoveColor, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addGroup(groupLayoutcolorPanel.createSequentialGroup().addComponent(lblColorDeviation, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE).addComponent(spinnerColorDeviation, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayoutcolorPanel.createSequentialGroup().addComponent(lblAlphaDeviation, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE).addComponent(spinnerAlphaDeviation, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE)))
            .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)));
    groupLayoutcolorPanel.setVerticalGroup(groupLayoutcolorPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayoutcolorPanel.createSequentialGroup().addComponent(btnSelectColor, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE).addGap(7).addComponent(btnAddColor).addComponent(btnRemoveColor).addGap(12)
            .addGroup(groupLayoutcolorPanel.createParallelGroup(Alignment.LEADING).addGroup(groupLayoutcolorPanel.createSequentialGroup().addGap(3).addComponent(lblColorDeviation)).addComponent(spinnerColorDeviation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGap(10)
            .addGroup(groupLayoutcolorPanel.createParallelGroup(Alignment.LEADING).addGroup(groupLayoutcolorPanel.createSequentialGroup().addGap(3).addComponent(lblAlphaDeviation)).addComponent(spinnerAlphaDeviation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 180, GroupLayout.PREFERRED_SIZE));
    colorPanel.setLayout(groupLayoutcolorPanel);

    spritePanel = new JPanel();
    tabbedPanel.addTab(Resources.get("panel_sprite"), null, spritePanel, null);
    tabbedPanel.setEnabledAt(1, false);

    JLabel lblEmittertype = new JLabel(Resources.get("panel_spriteType"));

    comboBoxSpriteType = new JComboBox();
    comboBoxSpriteType.addItem(Resources.get("panel_animation"));
    comboBoxSpriteType.addItem(Resources.get("panel_spritesheet"));
    comboBoxSpriteType.setSelectedIndex(0);

    JLabel lblSpritesheet = new JLabel(Resources.get("panel_sprite"));

    comboBoxSprite = new JComboBox();
    for (SpriteSheetInfo s : EditorScreen.instance().getGameFile().getSpriteSheets()) {
      comboBoxSprite.addItem(s.getName());
    }
    GroupLayout gl_spritePanel = new GroupLayout(spritePanel);
    gl_spritePanel.setHorizontalGroup(gl_spritePanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_spritePanel.createSequentialGroup().addGap(10)
            .addGroup(gl_spritePanel.createParallelGroup(Alignment.LEADING)
                .addGroup(gl_spritePanel.createSequentialGroup().addComponent(lblEmittertype, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxSpriteType, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE))
                .addGroup(gl_spritePanel.createSequentialGroup().addComponent(lblSpritesheet).addGap(43).addComponent(comboBoxSprite, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE)))));
    gl_spritePanel.setVerticalGroup(gl_spritePanel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_spritePanel.createSequentialGroup().addGap(5)
            .addGroup(gl_spritePanel.createParallelGroup(Alignment.LEADING).addGroup(gl_spritePanel.createSequentialGroup().addGap(3).addComponent(lblEmittertype)).addComponent(comboBoxSpriteType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(10)
            .addGroup(gl_spritePanel.createParallelGroup(Alignment.LEADING).addGroup(gl_spritePanel.createSequentialGroup().addGap(3).addComponent(lblSpritesheet)).addComponent(comboBoxSprite, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))));
    spritePanel.setLayout(gl_spritePanel);

    JLabel lblDeltaWidth = new JLabel(Resources.get("panel_particleDeltaWidth"));
    lblDeltaWidth.setEnabled(false);
    lblDeltaWidth.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltaWidth.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin7 = new JLabel(Resources.get("panel_min"));
    lblMin7.setEnabled(false);
    lblMin7.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin7.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMinDeltaWidth = new JSpinner();
    spinnerMinDeltaWidth.setEnabled(false);

    JLabel lblMax6 = new JLabel(Resources.get("panel_max"));
    lblMax6.setEnabled(false);
    lblMax6.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax6.setFont(new Font("Tahoma", Font.ITALIC, 11));

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

    spinnerMinDeltaHeight = new JSpinner();
    spinnerMinDeltaHeight.setEnabled(false);

    JLabel lblMax7 = new JLabel(Resources.get("panel_max"));
    lblMax7.setEnabled(false);
    lblMax7.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax7.setFont(new Font("Tahoma", Font.ITALIC, 11));

    spinnerMaxDeltaHeight = new JSpinner();
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
                    .addComponent(lblMin1, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinDeltaX, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxDeltaX, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockDeltaX).addGap(19).addComponent(rdbtnRandomDeltaX))
                .addGroup(groupLayout.createSequentialGroup().addComponent(lblSpawnAmount).addGap(10).addComponent(spinnerSpawnAmount, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE).addGap(118).addComponent(lblDeltaY, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinDeltaY, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax1, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxDeltaY, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockDeltaY).addGap(19).addComponent(rdbtnRandomDeltaY))
                .addGroup(groupLayout.createSequentialGroup().addComponent(lblUpdateDelay).addGap(17).addComponent(spinnerUpdateDelay, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE).addGap(118).addComponent(lblGravityX, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin3, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinGravityX, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxGravityX, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockGravityX).addGap(19).addComponent(rdbtnRandomGravityX))
                .addGroup(groupLayout.createSequentialGroup().addComponent(labelTtl).addGap(28).addComponent(spinnerTTL, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE).addGap(118).addComponent(lblGravityY, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin4, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinGravityY, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax3, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxGravityY, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockGravityY).addGap(19).addComponent(rdbtnRandomGravityY))
                .addGroup(groupLayout.createSequentialGroup().addComponent(lblMaxParticles).addGap(17).addComponent(spinnerMaxParticles, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE).addGap(118).addComponent(lblStartWidth, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin5, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinStartWidth, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax4, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxStartWidth, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockStartWidth).addGap(19).addComponent(rdbtnRandomStartWidth))
                .addGroup(groupLayout
                    .createSequentialGroup().addComponent(lblParticleType).addGap(20).addComponent(comboBoxParticleType, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE).addGap(18).addComponent(lblStartHeight, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMin6, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinStartHeight, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax5, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(spinnerMaxStartHeight, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addGap(29).addComponent(rdbtnLockStartHeight).addGap(19).addComponent(rdbtnRandomStartHeight))
                .addGroup(groupLayout.createSequentialGroup().addComponent(tabbedPanel, GroupLayout.PREFERRED_SIZE, 286, GroupLayout.PREFERRED_SIZE).addGap(18)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblDeltaWidth, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE).addComponent(lblDeltaHeight).addComponent(lblStaticPhysics, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblText, GroupLayout.PREFERRED_SIZE, 106, GroupLayout.PREFERRED_SIZE))
                    .addGroup(
                        groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(lblMin7, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinDeltaWidth, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                            .addGroup(groupLayout.createSequentialGroup().addComponent(lblMin, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMinDeltaHeight, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                            .addGroup(groupLayout.createSequentialGroup().addGap(49).addComponent(chckbxStaticPhysics)).addComponent(txt, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE))
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblMax6, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(lblMax7, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(spinnerMaxDeltaWidth, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE).addComponent(spinnerMaxDeltaHeight, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)).addGap(29)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(rdbtnLockDeltaWidth).addComponent(rdbtnLockDeltaHeight)).addGap(19).addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(rdbtnRandomDeltaWidth).addComponent(rdbtnRandomDeltaHeight))))));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGap(33)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(label).addComponent(lblParticleData).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(lblLock)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(lblRandom))).addGap(12)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblSpawnRate))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerSpawnRate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblDeltax))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin1)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinDeltaX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxDeltaX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(rdbtnLockDeltaX)
                .addComponent(rdbtnRandomDeltaX))
            .addGap(9)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblSpawnAmount))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerSpawnAmount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblDeltaY))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin2)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinDeltaY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax1)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxDeltaY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(rdbtnLockDeltaY)
                .addComponent(rdbtnRandomDeltaY))
            .addGap(9)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblUpdateDelay))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerUpdateDelay, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblGravityX))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin3)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinGravityX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax2)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxGravityX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(rdbtnLockGravityX)
                .addComponent(rdbtnRandomGravityX))
            .addGap(9)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(labelTtl))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerTTL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblGravityY))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin4)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinGravityY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax3)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxGravityY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addComponent(rdbtnLockGravityY)
                .addComponent(rdbtnRandomGravityY))
            .addGap(9)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMaxParticles))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxParticles, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblStartWidth))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin5)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinStartWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax4)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxStartWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(rdbtnLockStartWidth).addComponent(rdbtnRandomStartWidth))
            .addGap(9)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblParticleType))
                .addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(comboBoxParticleType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblStartHeight))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMin6)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMinStartHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(lblMax5)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(spinnerMaxStartHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(rdbtnLockStartHeight).addComponent(rdbtnRandomStartHeight))
            .addGap(5)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(tabbedPanel, GroupLayout.PREFERRED_SIZE, 210, GroupLayout.PREFERRED_SIZE)
                .addGroup(groupLayout.createSequentialGroup().addGap(8).addComponent(lblDeltaWidth).addGap(16).addComponent(lblDeltaHeight).addGap(16).addComponent(lblStaticPhysics).addGap(16).addComponent(lblText))
                .addGroup(groupLayout.createSequentialGroup().addGap(5)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(3).addComponent(lblMin7)).addComponent(spinnerMinDeltaWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(10)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(3).addComponent(lblMin)).addComponent(spinnerMinDeltaHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(9)
                    .addComponent(chckbxStaticPhysics).addGap(10).addComponent(txt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(8).addComponent(lblMax6).addGap(16).addComponent(lblMax7))
                .addGroup(groupLayout.createSequentialGroup().addGap(5).addComponent(spinnerMaxDeltaWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(10).addComponent(spinnerMaxDeltaHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(rdbtnLockDeltaWidth).addGap(9).addComponent(rdbtnLockDeltaHeight)).addGroup(groupLayout.createSequentialGroup().addGap(4).addComponent(rdbtnRandomDeltaWidth).addGap(9).addComponent(rdbtnRandomDeltaHeight)))));
    setLayout(groupLayout);
    this.setupChangedListeners();

  }

  @Override
  public void bind(IMapObject mapObject) {
    super.bind(mapObject);
  }

  public void discardChanges() {
    this.bind(this.backupMapObject);
  }

  private void setupChangedListeners() {
    btnSelectColor.addActionListener(a -> {
      Color result = ColorChooser.showRgbDialog(Resources.get("panel_selectEmitterColor"), colors.get(table.getSelectedRow()).toColor());
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
        this.getDataSource().setCustomProperty(MapObjectProperty.EMITTER_COLORS, colorString.toString());
      }
    });

    btnRemoveColor.addActionListener(a -> {
      for (int removeIndex = 0; removeIndex < colors.size(); removeIndex++) {
        if (removeIndex == table.getSelectedRow()) {
          colors.remove(removeIndex);
          break;
        }
      }

      model.removeRow(table.getSelectedRow());
    });

    btnAddColor.addActionListener(a -> {
      ParticleColor c = new ParticleColor();
      colors.add(c);
      model.addRow(new Object[] { null, c.toString() });
    });

    this.spinnerSpawnRate.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.EMITTER_SPAWNRATE, this.spinnerSpawnRate.getValue().toString())));
    this.spinnerSpawnAmount.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.EMITTER_SPAWNAMOUNT, this.spinnerSpawnAmount.getValue().toString())));
    this.spinnerUpdateDelay.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.EMITTER_UPDATEDELAY, this.spinnerUpdateDelay.getValue().toString())));
    this.spinnerTTL.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.EMITTER_TIMETOLIVE, this.spinnerTTL.getValue().toString())));
    this.spinnerMaxParticles.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.EMITTER_MAXPARTICLES, this.spinnerMaxParticles.getValue().toString())));

    this.comboBoxParticleType.addActionListener(new MapObjectPropertyActionListener(m -> {
      ParticleType particleType = (ParticleType) this.comboBoxParticleType.getSelectedItem();
      if(particleType == ParticleType.SPRITE) {
        this.tabbedPanel.setSelectedIndex(1);
        tabbedPanel.setEnabledAt(0, false);
        tabbedPanel.setEnabledAt(1, true);
      }
      else {
        this.tabbedPanel.setSelectedIndex(0);
        tabbedPanel.setEnabledAt(0, true);
        tabbedPanel.setEnabledAt(1, false);
      }
      m.setCustomProperty(MapObjectProperty.EMITTER_PARTICLETYPE, particleType.toString());
    }));

    this.spinnerMinDeltaX.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MINDELTAX, this.spinnerMinDeltaX.getValue().toString())));
    this.spinnerMaxDeltaX.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MAXDELTAX, this.spinnerMaxDeltaX.getValue().toString())));
    this.spinnerMinDeltaY.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MINDELTAY, this.spinnerMinDeltaY.getValue().toString())));
    this.spinnerMaxDeltaY.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MAXDELTAY, this.spinnerMaxDeltaY.getValue().toString())));

    this.spinnerMinGravityX.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MINGRAVITYX, this.spinnerMinGravityX.getValue().toString())));
    this.spinnerMaxGravityX.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MAXGRAVITYX, this.spinnerMaxGravityX.getValue().toString())));
    this.spinnerMinGravityY.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MINGRAVITYX, this.spinnerMinGravityY.getValue().toString())));
    this.spinnerMaxGravityY.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MAXGRAVITYX, this.spinnerMaxGravityY.getValue().toString())));

    this.spinnerMinStartWidth.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MINSTARTWIDTH, this.spinnerMinStartWidth.getValue().toString())));
    this.spinnerMaxStartWidth.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MAXSTARTWIDTH, this.spinnerMaxStartWidth.getValue().toString())));
    this.spinnerMinStartHeight.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MINSTARTHEIGHT, this.spinnerMinStartHeight.getValue().toString())));
    this.spinnerMaxStartHeight.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MAXSTARTHEIGHT, this.spinnerMaxStartHeight.getValue().toString())));

    this.spinnerMinDeltaWidth.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MINDELTAWIDTH, this.spinnerMinDeltaWidth.getValue().toString())));
    this.spinnerMaxDeltaWidth.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MAXDELTAWIDTH, this.spinnerMaxDeltaWidth.getValue().toString())));
    this.spinnerMinDeltaHeight.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MINDELTAHEIGHT, this.spinnerMinDeltaHeight.getValue().toString())));
    this.spinnerMaxDeltaHeight.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_MAXDELTAHEIGHT, this.spinnerMaxDeltaHeight.getValue().toString())));

    this.chckbxStaticPhysics.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_STATICPHYSICS, Boolean.toString(this.chckbxStaticPhysics.isSelected()))));

    this.txt.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_TEXT, this.txt.getText())));

    this.spinnerColorDeviation.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.EMITTER_COLORDEVIATION, this.spinnerColorDeviation.getValue().toString())));
    this.spinnerAlphaDeviation.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.EMITTER_ALPHADEVIATION, this.spinnerAlphaDeviation.getValue().toString())));


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

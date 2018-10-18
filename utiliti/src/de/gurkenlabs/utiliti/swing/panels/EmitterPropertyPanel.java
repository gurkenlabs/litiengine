package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpritesheetInfo;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.environment.EmitterMapObjectLoader;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.graphics.emitters.xml.ParticleColor;
import de.gurkenlabs.litiengine.physics.CollisionType;
import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.utiliti.EditorScreen;
import de.gurkenlabs.utiliti.Icons;
import de.gurkenlabs.utiliti.Program;

@SuppressWarnings("serial")
public class EmitterPropertyPanel extends PropertyPanel {
  // TODO: implement support to adjust rendertype (GROUND, NORMAL, OVERLAY)
  private static final double PARTICLESPINNER_MAX_VALUE = 100.0;
  private static final double PARTICLEDELTA_MAX_VALUE = 1.0;
  private static final double PARTICLEDELTA_DEFAULT_VALUE = 0.1;
  private static final int PARTICLEMINTTL_DEFAULT_VALUE = 2000;
  private static final String TAHOMA = "Tahoma";

  private final DefaultTableModel model;
  private final JTextField txt;
  private final JTable table;
  private final List<ParticleColor> colors;
  private transient IMapObject backupMapObject;
  private final JTabbedPane tabbedPanel;
  private final JPanel colorPanel;
  private final JPanel spritePanel;

  private final JComboBox<ParticleType> comboBoxParticleType = new JComboBox<>();
  private final JComboBox<String> comboBoxSpriteType = new JComboBox<>();
  private final JComboBox<String> comboBoxSprite = new JComboBox<>();
  private final JComboBox<CollisionType> comboBoxCollisionType = new JComboBox<>();
  private final JComboBox<Align> comboBoxAlign = new JComboBox<>();
  private final JComboBox<Valign> comboBoxValign = new JComboBox<>();
  
  // TODO: refactor this code to use a custom ParticleParameter JPanel implementation that wraps the controls and logic
  private final JRadioButton rdbtnLockDeltaX = new JRadioButton("");
  private final JRadioButton rdbtnLockDeltaY = new JRadioButton("");
  private final JRadioButton rdbtnLockGravityX = new JRadioButton("");
  private final JRadioButton rdbtnLockGravityY = new JRadioButton("");
  private final JRadioButton rdbtnLockStartWidth = new JRadioButton("");
  private final JRadioButton rdbtnLockStartHeight = new JRadioButton("");
  private final JRadioButton rdbtnLockDeltaWidth = new JRadioButton("");
  private final JRadioButton rdbtnLockDeltaHeight = new JRadioButton("");
  private final JRadioButton rdbtnRandomDeltaX = new JRadioButton("");
  private final JRadioButton rdbtnRandomDeltaY = new JRadioButton("");
  private final JRadioButton rdbtnRandomGravityX = new JRadioButton("");
  private final JRadioButton rdbtnRandomGravityY = new JRadioButton("");
  private final JRadioButton rdbtnRandomStartWidth = new JRadioButton("");
  private final JRadioButton rdbtnRandomStartHeight = new JRadioButton("");
  private final JRadioButton rdbtnRandomDeltaWidth = new JRadioButton("");
  private final JRadioButton rdbtnRandomDeltaHeight = new JRadioButton("");
  private final JRadioButton rdbtnLockParticleTTL = new JRadioButton("");
  private final JRadioButton rdbtnRandomParticleTTL = new JRadioButton("");
  private final JRadioButton rdbtnLockStartX = new JRadioButton("");
  private final JRadioButton rdbtnRandomStartX = new JRadioButton("");
  private final JRadioButton rdbtnLockStartY = new JRadioButton("");
  private final JRadioButton rdbtnRandomStartY = new JRadioButton("");

  private final JButton btnSelectColor = new JButton();
  private final JButton btnAddColor = new JButton("+");
  private final JButton btnRemoveColor = new JButton("-");

  private final ButtonGroup buttonGroupDeltaX = new ButtonGroup();
  private final ButtonGroup buttonGroupDeltaY = new ButtonGroup();
  private final ButtonGroup buttonGroupGravityX = new ButtonGroup();
  private final ButtonGroup buttonGroupGravityY = new ButtonGroup();
  private final ButtonGroup buttonGroupStartWidth = new ButtonGroup();
  private final ButtonGroup buttonGroupStartHeight = new ButtonGroup();
  private final ButtonGroup buttonGroupDeltaWidth = new ButtonGroup();
  private final ButtonGroup buttonGroupDeltaHeight = new ButtonGroup();
  private final ButtonGroup buttonGroupParticleTTL = new ButtonGroup();
  private final ButtonGroup buttonGroupStartX = new ButtonGroup();
  private final ButtonGroup buttonGroupStartY = new ButtonGroup();
  private final JSpinner spinnerSpawnRate = new JSpinner();
  private final JSpinner spinnerSpawnAmount = new JSpinner(new SpinnerNumberModel(Emitter.DEFAULT_SPAWNAMOUNT, 1, 100, 1));
  private final JSpinner spinnerUpdateRate = new JSpinner();
  private final JSpinner spinnerTTL = new JSpinner();
  private final JSpinner spinnerMaxParticles = new JSpinner(new SpinnerNumberModel(Emitter.DEFAULT_MAXPARTICLES, 1, 10000, 1));
  private final JSpinner spinnerColorDeviation = new JSpinner(getPercentModel());
  private final JSpinner spinnerAlphaDeviation = new JSpinner(getPercentModel());
  private final JSpinner spinnerMinDeltaX = new JSpinner(getParticleMinModel());
  private final JSpinner spinnerMinDeltaY = new JSpinner(getParticleMinModel());
  private final JSpinner spinnerMinGravityX = new JSpinner(getDeltaModel());
  private final JSpinner spinnerMinGravityY = new JSpinner(getDeltaModel());
  private final JSpinner spinnerMinStartWidth = new JSpinner(getParticleDimensionModel());
  private final JSpinner spinnerMinStartHeight = new JSpinner(getParticleDimensionModel());
  private final JSpinner spinnerMinDeltaWidth = new JSpinner(getDeltaModel());
  private final JSpinner spinnerMinDeltaHeight = new JSpinner(getDeltaModel());
  private final JSpinner spinnerMaxDeltaX = new JSpinner(getParticleMaxModel());
  private final JSpinner spinnerMaxDeltaY = new JSpinner(getParticleMaxModel());
  private final JSpinner spinnerMaxGravityX = new JSpinner(getDeltaModel());
  private final JSpinner spinnerMaxGravityY = new JSpinner(getDeltaModel());
  private final JSpinner spinnerMaxStartWidth = new JSpinner(getParticleDimensionModel());
  private final JSpinner spinnerMaxStartHeight = new JSpinner(getParticleDimensionModel());
  private final JSpinner spinnerMaxDeltaWidth = new JSpinner(getDeltaModel());
  private final JSpinner spinnerMaxDeltaHeight = new JSpinner(getDeltaModel());
  
  private final JSpinner spinnerMinStartX = new JSpinner(getLocationModel());
  private final JSpinner spinnerMaxStartX = new JSpinner(getLocationModel());
  private final JSpinner spinnerMinStartY = new JSpinner(getLocationModel());
  private final JSpinner spinnerMaxStartY = new JSpinner(getLocationModel());
  
  private final JSpinner spinnerMinParticleTTL = new JSpinner(new SpinnerNumberModel(PARTICLEMINTTL_DEFAULT_VALUE, 0, 30000, 1));
  private final JSpinner spinnerMaxParticleTTL = new JSpinner(new SpinnerNumberModel(0, 0, 30000, 1));
  
  private final JCheckBox checkBoxFade;
  private final JLabel lblPivotY = new JLabel("pivot Y");
  private final JLabel label = new JLabel("pivot X");

  /**
   * Create the dialog.
   */
  public EmitterPropertyPanel() {
    super();
    rdbtnLockParticleTTL.setSelected(true);
    setBounds(100, 100, 700, 464);
    this.setBorder(new EmptyBorder(5, 5, 5, 5));

    this.colors = new ArrayList<>();

    this.initRadioButtons();

    this.btnSelectColor.setIcon(Icons.COLOR);
    this.btnSelectColor.setMinimumSize(new Dimension(30, 10));
    this.btnSelectColor.setMaximumSize(new Dimension(30, 10));

    this.comboBoxParticleType.setModel(new DefaultComboBoxModel<ParticleType>(ParticleType.values()));
    this.comboBoxCollisionType.setModel(new DefaultComboBoxModel<CollisionType>(CollisionType.values()));
    this.comboBoxAlign.setModel(new DefaultComboBoxModel<Align>(Align.values()));
    this.comboBoxValign.setModel(new DefaultComboBoxModel<Valign>(Valign.values()));
    
    this.comboBoxSpriteType.addItem(Resources.get("panel_animation"));
    this.comboBoxSpriteType.addItem(Resources.get("panel_spritesheet"));
    this.comboBoxSpriteType.setSelectedIndex(0);
    for (SpritesheetInfo s : EditorScreen.instance().getGameFile().getSpriteSheets()) {
      this.comboBoxSprite.addItem(s.getName());
    }
    
    this.txt = new JTextField();
    this.txt.setEnabled(false);
    this.txt.setText("IFFF");
    this.txt.setColumns(10);

    this.tabbedPanel = new JTabbedPane(JTabbedPane.TOP);
    this.tabbedPanel.setBorder(null);

    this.colorPanel = new JPanel();
    this.tabbedPanel.addTab(Resources.get("panel_color"), null, colorPanel, null);
    this.tabbedPanel.setEnabledAt(0, true);

    JScrollPane scrollPane = new JScrollPane();

    // TODO: implement percentage logic
    this.table = new JTable();
    this.table.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "percentage", "color" }));
    this.model = (DefaultTableModel) table.getModel();
    this.table.setFont(Program.TEXT_FONT);
    this.table.getColumnModel().getColumn(1).setCellRenderer(new ParticleColorCellRenderer());
    scrollPane.setViewportView(this.table);
    this.spritePanel = new JPanel();
    this.tabbedPanel.addTab(Resources.get("panel_sprite"), null, spritePanel, null);
    this.tabbedPanel.setEnabledAt(1, false);
    
    final String min = Resources.get("panel_min");
    final String max = Resources.get("panel_max");
 
    
    // init all labels
    JLabel labelEmitterData = new JLabel(Resources.get("panel_emitterData"));
    labelEmitterData.setFont(new Font(TAHOMA, Font.BOLD, 12));

    JLabel lblSpawnRate = new JLabel(Resources.get("panel_emitterSpawnRate"));
    JLabel lblSpawnAmount = new JLabel(Resources.get("panel_emitterSpawnAmount"));
    JLabel lblParticleData = new JLabel(Resources.get("panel_particleData"));
    lblParticleData.setFont(new Font(TAHOMA, Font.BOLD, 12));

    JLabel lblDeltax = new JLabel(Resources.get("panel_particleDeltaX"));

    JLabel lblDeltaY = new JLabel(Resources.get("panel_particleDeltaY"));

    JLabel lblMin1 = new JLabel(min);
    lblMin1.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin1.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblMin2 = new JLabel(min);
    lblMin2.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin2.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblLock = new JLabel(Resources.get("panel_lock"));

    JLabel lblRandom = new JLabel(Resources.get("panel_random"));

    JLabel lblMax = new JLabel(max);
    lblMax.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblMax1 = new JLabel(max);
    lblMax1.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax1.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblUpdateDelay = new JLabel(Resources.get("panel_emitterUpdateDelay"));

    JLabel lblGravityX = new JLabel(Resources.get("panel_particleGravityX"));

    JLabel lblMin3 = new JLabel(min);
    lblMin3.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin3.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblMax2 = new JLabel(max);
    lblMax2.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax2.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel labelTtl = new JLabel(Resources.get("panel_emitterTTL"));

    JLabel lblGravityY = new JLabel(Resources.get("panel_particleGravityY"));

    JLabel lblMin4 = new JLabel(min);
    lblMin4.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin4.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblMax3 = new JLabel(max);
    lblMax3.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax3.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblMaxParticles = new JLabel(Resources.get("panel_emitterMaxParticles"));

    JLabel lblStartWidth = new JLabel(Resources.get("panel_particleStartWidth"));

    JLabel lblMin5 = new JLabel(min);
    lblMin5.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin5.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblMax4 = new JLabel(max);
    lblMax4.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax4.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblParticleType = new JLabel(Resources.get("panel_particleType"));

    JLabel lblStartHeight = new JLabel(Resources.get("panel_particleStartHeight"));

    JLabel lblMin6 = new JLabel(min);
    lblMin6.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin6.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblMax5 = new JLabel(max);
    lblMax5.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax5.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblColorDeviation = new JLabel(Resources.get("panel_colorDeviation"));

    JLabel lblAlphaDeviation = new JLabel(Resources.get("panel_alphaDeviation"));

    JLabel lblEmittertype = new JLabel(Resources.get("panel_spriteType"));

    JLabel lblSpritesheet = new JLabel(Resources.get("panel_sprite"));

    JLabel lblDeltaWidth = new JLabel(Resources.get("panel_particleDeltaWidth"));

    JLabel lblMin7 = new JLabel(min);
    lblMin7.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin7.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblMax6 = new JLabel(max);
    lblMax6.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax6.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblDeltaHeight = new JLabel(Resources.get("panel_particleDeltaHeight"));

    JLabel lblMin = new JLabel(min);
    lblMin.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblMax7 = new JLabel(max);
    lblMax7.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax7.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblStaticPhysics = new JLabel("collision type");
    lblStaticPhysics.setHorizontalAlignment(SwingConstants.LEFT);
    lblStaticPhysics.setFont(new Font(TAHOMA, Font.PLAIN, 11));

    JLabel lblText = new JLabel(Resources.get("panel_particleText"));

    JLabel lblParticleTtl = new JLabel("particle ttl");

    JLabel labelMinParticleTtl = new JLabel(min);
    labelMinParticleTtl.setHorizontalAlignment(SwingConstants.CENTER);
    labelMinParticleTtl.setFont(new Font(TAHOMA, Font.ITALIC, 11));

    JLabel lblMaxParticleTtl = new JLabel(max);
    lblMaxParticleTtl.setHorizontalAlignment(SwingConstants.CENTER);
    lblMaxParticleTtl.setFont(new Font(TAHOMA, Font.ITALIC, 11));
    
    JLabel lblStartX = new JLabel("start x");
    JLabel lblStartY = new JLabel("start y");
    
    JLabel label8 = new JLabel(min);
    label8.setHorizontalAlignment(SwingConstants.CENTER);
    label8.setFont(new Font(TAHOMA, Font.ITALIC, 11));
    
    JLabel label9 = new JLabel(max);
    label9.setHorizontalAlignment(SwingConstants.CENTER);
    label9.setFont(new Font(TAHOMA, Font.ITALIC, 11));
    
    JLabel label10 = new JLabel(min);
    label10.setHorizontalAlignment(SwingConstants.CENTER);
    label10.setFont(new Font(TAHOMA, Font.ITALIC, 11));
    
    JLabel label11 = new JLabel(max);
    label11.setHorizontalAlignment(SwingConstants.CENTER);
    label11.setFont(new Font(TAHOMA, Font.ITALIC, 11));

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

    GroupLayout groupLayoutSpritePanel = new GroupLayout(spritePanel);
    groupLayoutSpritePanel.setHorizontalGroup(groupLayoutSpritePanel.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayoutSpritePanel.createSequentialGroup().addGap(10)
            .addGroup(groupLayoutSpritePanel.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayoutSpritePanel.createSequentialGroup().addComponent(lblEmittertype, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE).addComponent(comboBoxSpriteType, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayoutSpritePanel.createSequentialGroup().addComponent(lblSpritesheet).addGap(43).addComponent(comboBoxSprite, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE)))));
    groupLayoutSpritePanel.setVerticalGroup(groupLayoutSpritePanel.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayoutSpritePanel.createSequentialGroup().addGap(5)
            .addGroup(groupLayoutSpritePanel.createParallelGroup(Alignment.LEADING).addGroup(groupLayoutSpritePanel.createSequentialGroup().addGap(3).addComponent(lblEmittertype)).addComponent(comboBoxSpriteType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGap(10)
            .addGroup(groupLayoutSpritePanel.createParallelGroup(Alignment.LEADING).addGroup(groupLayoutSpritePanel.createSequentialGroup().addGap(3).addComponent(lblSpritesheet)).addComponent(comboBoxSprite, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))));
    spritePanel.setLayout(groupLayoutSpritePanel);
    
    this.checkBoxFade = new JCheckBox("fade");
    this.checkBoxFade.setSelected(true);

    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addGap(10)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(labelEmitterData)
              .addGap(226)
              .addComponent(lblParticleData)
              .addGap(219)
              .addComponent(lblLock)
              .addGap(13)
              .addComponent(lblRandom))
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(lblParticleType)
              .addGap(20)
              .addComponent(comboBoxParticleType, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE)
              .addGap(18)
              .addComponent(lblStartHeight, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
              .addComponent(lblMin6, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
              .addComponent(spinnerMinStartHeight, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
              .addComponent(lblMax5, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
              .addComponent(spinnerMaxStartHeight, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
              .addGap(29)
              .addComponent(rdbtnLockStartHeight)
              .addGap(19)
              .addComponent(rdbtnRandomStartHeight))
            .addGroup(groupLayout.createSequentialGroup()
              .addComponent(tabbedPanel, GroupLayout.PREFERRED_SIZE, 286, GroupLayout.PREFERRED_SIZE)
              .addGap(18)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                  .addComponent(lblStaticPhysics, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(ComponentPlacement.UNRELATED)
                  .addComponent(comboBoxCollisionType, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(ComponentPlacement.UNRELATED)
                  .addComponent(checkBoxFade))
                .addGroup(groupLayout.createSequentialGroup()
                  .addComponent(lblStartY, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                  .addComponent(label10, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMinStartY, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addComponent(label11, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMaxStartY, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addGap(29)
                  .addComponent(rdbtnLockStartY, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                  .addGap(19)
                  .addComponent(rdbtnRandomStartY, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addComponent(lblStartX, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                  .addComponent(label8, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMinStartX, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addComponent(label9, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMaxStartX, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addGap(29)
                  .addComponent(rdbtnLockStartX, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                  .addGap(19)
                  .addComponent(rdbtnRandomStartX, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                    .addGroup(groupLayout.createSequentialGroup()
                      .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblDeltaWidth, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblDeltaHeight, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE))
                      .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                          .addComponent(lblMin7, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                          .addComponent(spinnerMinDeltaWidth, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))
                        .addGroup(groupLayout.createSequentialGroup()
                          .addComponent(lblMin, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                          .addComponent(spinnerMinDeltaHeight, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)))
                      .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(lblMax6, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblMax7, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                      .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(spinnerMaxDeltaWidth, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                        .addComponent(spinnerMaxDeltaHeight, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)))
                    .addGroup(groupLayout.createSequentialGroup()
                      .addComponent(lblParticleTtl, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                      .addComponent(labelMinParticleTtl, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                      .addGap(1)
                      .addComponent(spinnerMinParticleTTL, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                      .addPreferredGap(ComponentPlacement.RELATED)
                      .addComponent(lblMaxParticleTtl, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                      .addGap(1)
                      .addComponent(spinnerMaxParticleTTL, 0, 0, Short.MAX_VALUE)))
                  .addGap(29)
                  .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                    .addGroup(groupLayout.createSequentialGroup()
                      .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(rdbtnLockDeltaWidth)
                        .addComponent(rdbtnLockDeltaHeight))
                      .addGap(19)
                      .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                        .addComponent(rdbtnRandomDeltaWidth)
                        .addComponent(rdbtnRandomDeltaHeight)))
                    .addGroup(groupLayout.createSequentialGroup()
                      .addComponent(rdbtnLockParticleTTL, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                      .addGap(19)
                      .addComponent(rdbtnRandomParticleTTL, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))))
                .addGroup(groupLayout.createSequentialGroup()
                  .addPreferredGap(ComponentPlacement.RELATED)
                  .addComponent(lblText, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(ComponentPlacement.UNRELATED)
                  .addComponent(txt, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE))))
            .addGroup(groupLayout.createSequentialGroup()
              .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
                .addGroup(groupLayout.createSequentialGroup()
                  .addComponent(lblMaxParticles)
                  .addGap(17)
                  .addComponent(spinnerMaxParticles, 0, 0, Short.MAX_VALUE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(Alignment.TRAILING, groupLayout.createParallelGroup(Alignment.LEADING)
                      .addGroup(Alignment.TRAILING, groupLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
                          .addComponent(lblSpawnRate)
                          .addGap(26))
                        .addGroup(groupLayout.createSequentialGroup()
                          .addComponent(lblSpawnAmount)
                          .addGap(1)))
                      .addGroup(groupLayout.createSequentialGroup()
                        .addComponent(lblUpdateDelay)
                        .addPreferredGap(ComponentPlacement.RELATED)))
                    .addGroup(groupLayout.createSequentialGroup()
                      .addComponent(labelTtl)
                      .addGap(1)))
                  .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                    .addComponent(spinnerTTL)
                    .addComponent(spinnerUpdateRate)
                    .addComponent(spinnerSpawnAmount)
                    .addComponent(spinnerSpawnRate, GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))))
              .addPreferredGap(ComponentPlacement.UNRELATED)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
                .addComponent(lblPivotY, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(label, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
              .addGap(18)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addComponent(comboBoxAlign, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
                .addComponent(comboBoxValign, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE))
              .addGap(16)
              .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createSequentialGroup()
                  .addComponent(lblStartWidth, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMin5, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMinStartWidth, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMax4, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMaxStartWidth, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addGap(29)
                  .addComponent(rdbtnLockStartWidth)
                  .addGap(19)
                  .addComponent(rdbtnRandomStartWidth))
                .addGroup(groupLayout.createSequentialGroup()
                  .addComponent(lblGravityY, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMin4, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMinGravityY, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMax3, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMaxGravityY, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addGap(29)
                  .addComponent(rdbtnLockGravityY)
                  .addGap(19)
                  .addComponent(rdbtnRandomGravityY))
                .addGroup(groupLayout.createSequentialGroup()
                  .addComponent(lblGravityX, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMin3, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMinGravityX, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMax2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMaxGravityX, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addGap(29)
                  .addComponent(rdbtnLockGravityX)
                  .addGap(19)
                  .addComponent(rdbtnRandomGravityX))
                .addGroup(groupLayout.createSequentialGroup()
                  .addComponent(lblDeltaY, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMin2, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMinDeltaY, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMax1, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMaxDeltaY, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addGap(29)
                  .addComponent(rdbtnLockDeltaY)
                  .addGap(19)
                  .addComponent(rdbtnRandomDeltaY))
                .addGroup(groupLayout.createSequentialGroup()
                  .addComponent(lblDeltax, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMin1, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMinDeltaX, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addComponent(lblMax, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                  .addComponent(spinnerMaxDeltaX, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE)
                  .addGap(29)
                  .addComponent(rdbtnLockDeltaX)
                  .addGap(19)
                  .addComponent(rdbtnRandomDeltaX)))))
          .addContainerGap(12, Short.MAX_VALUE))
    );
    groupLayout.setVerticalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(10)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addComponent(labelEmitterData)
                .addComponent(lblParticleData)
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(lblLock))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(lblRandom)))
              .addGap(12)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblSpawnRate))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblDeltax))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblMin1))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMinDeltaX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblMax))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMaxDeltaX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(rdbtnLockDeltaX)
                .addComponent(rdbtnRandomDeltaX)))
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(38)
              .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(spinnerSpawnRate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(label)
                .addComponent(comboBoxAlign, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(9)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblSpawnAmount))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblDeltaY))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblMin2))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMinDeltaY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblMax1))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMaxDeltaY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(rdbtnLockDeltaY)
                .addComponent(rdbtnRandomDeltaY)))
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(10)
              .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(spinnerSpawnAmount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblPivotY)
                .addComponent(comboBoxValign, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(9)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblUpdateDelay))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblGravityX))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblMin3))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMinGravityX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblMax2))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMaxGravityX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(rdbtnLockGravityX)
                .addComponent(rdbtnRandomGravityX)))
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(10)
              .addComponent(spinnerUpdateRate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(9)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(labelTtl))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblGravityY))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblMin4))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMinGravityY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblMax3))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMaxGravityY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(rdbtnLockGravityY)
                .addComponent(rdbtnRandomGravityY)))
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(10)
              .addComponent(spinnerTTL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
          .addPreferredGap(ComponentPlacement.RELATED)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(9)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblMaxParticles))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblStartWidth))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblMin5))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMinStartWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblMax4))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMaxStartWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(rdbtnLockStartWidth)
                .addComponent(rdbtnRandomStartWidth)))
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(10)
              .addComponent(spinnerMaxParticles, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
          .addGap(9)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(4)
              .addComponent(lblParticleType))
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(1)
              .addComponent(comboBoxParticleType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(4)
              .addComponent(lblStartHeight))
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(4)
              .addComponent(lblMin6))
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(1)
              .addComponent(spinnerMinStartHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(4)
              .addComponent(lblMax5))
            .addGroup(groupLayout.createSequentialGroup()
              .addGap(1)
              .addComponent(spinnerMaxStartHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addComponent(rdbtnLockStartHeight)
            .addComponent(rdbtnRandomStartHeight))
          .addGap(5)
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(8)
                  .addComponent(lblMax6)
                  .addGap(16)
                  .addComponent(lblMax7))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(5)
                  .addComponent(spinnerMaxDeltaWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                  .addGap(10)
                  .addComponent(spinnerMaxDeltaHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(rdbtnLockDeltaWidth)
                  .addGap(9)
                  .addComponent(rdbtnLockDeltaHeight))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(rdbtnRandomDeltaWidth)
                  .addGap(9)
                  .addComponent(rdbtnRandomDeltaHeight))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(8)
                  .addComponent(lblDeltaWidth)
                  .addGap(16)
                  .addComponent(lblDeltaHeight))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(5)
                  .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(groupLayout.createSequentialGroup()
                      .addGap(3)
                      .addComponent(lblMin7))
                    .addComponent(spinnerMinDeltaWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                  .addGap(10)
                  .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(groupLayout.createSequentialGroup()
                      .addGap(3)
                      .addComponent(lblMin))
                    .addComponent(spinnerMinDeltaHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                  .addPreferredGap(ComponentPlacement.UNRELATED)
                  .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(groupLayout.createSequentialGroup()
                      .addGap(4)
                      .addComponent(lblParticleTtl))
                    .addGroup(groupLayout.createSequentialGroup()
                      .addGap(4)
                      .addComponent(labelMinParticleTtl))
                    .addGroup(groupLayout.createSequentialGroup()
                      .addGap(1)
                      .addComponent(spinnerMinParticleTTL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(rdbtnLockParticleTTL, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                    .addComponent(rdbtnRandomParticleTTL, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(8)
                  .addComponent(spinnerMaxParticleTTL, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(11)
                  .addComponent(lblMaxParticleTtl)))
              .addPreferredGap(ComponentPlacement.UNRELATED)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblStartX))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(label8))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMinStartX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(label9))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMaxStartX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(rdbtnLockStartX, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                .addComponent(rdbtnRandomStartX, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
              .addPreferredGap(ComponentPlacement.UNRELATED)
              .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(lblStartY))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(label10))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMinStartY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(4)
                  .addComponent(label11))
                .addGroup(groupLayout.createSequentialGroup()
                  .addGap(1)
                  .addComponent(spinnerMaxStartY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addComponent(rdbtnLockStartY, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
                .addComponent(rdbtnRandomStartY, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE))
              .addPreferredGap(ComponentPlacement.UNRELATED)
              .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(comboBoxCollisionType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblStaticPhysics)
                .addComponent(checkBoxFade))
              .addGap(12)
              .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(txt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(lblText)))
            .addComponent(tabbedPanel, GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE))
          .addContainerGap())
    );
    setLayout(groupLayout);
    this.setupChangedListeners();
  }

  private void initRadioButtons() {
    this.buttonGroupDeltaX.add(this.rdbtnLockDeltaX);
    this.buttonGroupDeltaX.add(this.rdbtnRandomDeltaX);

    this.buttonGroupDeltaY.add(this.rdbtnLockDeltaY);
    this.buttonGroupDeltaY.add(this.rdbtnRandomDeltaY);

    this.buttonGroupGravityX.add(this.rdbtnLockGravityX);
    this.buttonGroupGravityX.add(this.rdbtnRandomGravityX);

    this.buttonGroupGravityY.add(this.rdbtnLockGravityY);
    this.buttonGroupGravityY.add(this.rdbtnRandomGravityY);

    this.buttonGroupStartWidth.add(this.rdbtnLockStartWidth);
    this.buttonGroupStartWidth.add(this.rdbtnRandomStartWidth);

    this.buttonGroupStartHeight.add(this.rdbtnLockStartHeight);
    this.buttonGroupStartHeight.add(this.rdbtnRandomStartHeight);

    this.buttonGroupDeltaWidth.add(this.rdbtnLockDeltaWidth);
    this.buttonGroupDeltaWidth.add(this.rdbtnRandomDeltaWidth);

    this.buttonGroupDeltaHeight.add(this.rdbtnLockDeltaHeight);
    this.buttonGroupDeltaHeight.add(this.rdbtnRandomDeltaHeight);

    this.buttonGroupParticleTTL.add(this.rdbtnLockParticleTTL);
    this.buttonGroupParticleTTL.add(this.rdbtnRandomParticleTTL);
    
    this.buttonGroupStartX.add(this.rdbtnLockStartX);
    this.buttonGroupStartX.add(this.rdbtnRandomStartX);
    
    this.buttonGroupStartY.add(this.rdbtnLockStartY);
    this.buttonGroupStartY.add(this.rdbtnRandomStartY);
    
    this.rdbtnLockDeltaX.addItemListener(new ParticleRadioButtonListener(MapObjectProperty.Particle.DELTAX_RANDOM, this.rdbtnLockDeltaX, this.spinnerMaxDeltaX));
    this.rdbtnLockDeltaY.addItemListener(new ParticleRadioButtonListener(MapObjectProperty.Particle.DELTAY_RANDOM, this.rdbtnLockDeltaY, this.spinnerMaxDeltaY));
    this.rdbtnLockGravityX.addItemListener(new ParticleRadioButtonListener(MapObjectProperty.Particle.GRAVITYX_RANDOM, this.rdbtnLockGravityX, this.spinnerMaxGravityX));
    this.rdbtnLockGravityY.addItemListener(new ParticleRadioButtonListener(MapObjectProperty.Particle.GRAVITYY_RANDOM, this.rdbtnLockGravityY, this.spinnerMaxGravityY));
    this.rdbtnLockStartWidth.addItemListener(new ParticleRadioButtonListener(MapObjectProperty.Particle.STARTWIDTH_RANDOM, this.rdbtnLockStartWidth, this.spinnerMaxStartWidth));
    this.rdbtnLockStartHeight.addItemListener(new ParticleRadioButtonListener(MapObjectProperty.Particle.STARTHEIGHT_RANDOM, this.rdbtnLockStartHeight, this.spinnerMaxStartHeight));
    this.rdbtnLockDeltaWidth.addItemListener(new ParticleRadioButtonListener(MapObjectProperty.Particle.DELTAWIDTH_RANDOM, this.rdbtnLockDeltaWidth, this.spinnerMaxDeltaWidth));
    this.rdbtnLockDeltaHeight.addItemListener(new ParticleRadioButtonListener(MapObjectProperty.Particle.DELTAHEIGHT_RANDOM, this.rdbtnLockDeltaHeight, this.spinnerMaxDeltaHeight));
    this.rdbtnLockParticleTTL.addItemListener(new ParticleRadioButtonListener(MapObjectProperty.Particle.TTL_RANDOM, this.rdbtnLockParticleTTL, this.spinnerMaxParticleTTL));
    
    this.rdbtnLockStartX.addItemListener(new ParticleRadioButtonListener(MapObjectProperty.Particle.DELTAX_RANDOM, this.rdbtnLockStartX, this.spinnerMaxStartX));
    this.rdbtnLockStartY.addItemListener(new ParticleRadioButtonListener(MapObjectProperty.Particle.DELTAY_RANDOM, this.rdbtnLockStartY, this.spinnerMaxStartY));
    
    this.rdbtnRandomDeltaX.setSelected(true);
    this.rdbtnRandomDeltaY.setSelected(true);

    this.rdbtnLockGravityX.setSelected(true);
    this.rdbtnLockGravityY.setSelected(true);
    this.rdbtnLockStartWidth.setSelected(true);
    this.rdbtnLockStartHeight.setSelected(true);
    this.rdbtnLockDeltaWidth.setSelected(true);
    this.rdbtnLockDeltaHeight.setSelected(true);
    this.rdbtnLockParticleTTL.setSelected(true);
    
    this.rdbtnLockStartX.setSelected(true);
    this.rdbtnLockStartY.setSelected(true);
  }

  public void discardChanges() {
    this.bind(this.backupMapObject);
  }

  private void setupChangedListeners() {
    this.btnSelectColor.addActionListener(a -> {
      if (table.getSelectedRow() == -1) {
        return;
      }

      ParticleColor color = colors.get(table.getSelectedRow());
      Color result = JColorChooser.showDialog(null, Resources.get("panel_selectEmitterColor"), color.toColor());
      if (result == null) {
        return;
      }

      ParticleColor c = new ParticleColor(result);
      colors.set(table.getSelectedRow(), c);
      model.setValueAt(c, table.getSelectedRow(), 1);
      if (getDataSource() != null) {
        String commaSeperated = ArrayUtilities.getCommaSeparatedString(colors);
        this.getDataSource().setProperty(MapObjectProperty.Emitter.COLORS, commaSeperated);
      }
    });

    this.btnRemoveColor.addActionListener(a -> {
      for (int removeIndex = 0; removeIndex < colors.size(); removeIndex++) {
        if (removeIndex == table.getSelectedRow()) {
          colors.remove(removeIndex);
          break;
        }
      }

      if (table.getSelectedRow() != -1) {
        model.removeRow(table.getSelectedRow());
      }
    });

    this.btnAddColor.addActionListener(a -> {
      ParticleColor c = new ParticleColor();
      colors.add(c);
      model.addRow(new Object[] { null, c.toString() });
    });

    this.comboBoxParticleType.addActionListener(new MapObjectPropertyActionListener(m -> {
      ParticleType particleType = (ParticleType) this.comboBoxParticleType.getSelectedItem();
      this.updateTabbedGroup();

      this.txt.setEnabled(particleType == ParticleType.TEXT);
      m.setProperty(MapObjectProperty.Emitter.PARTICLETYPE, particleType);
    }));
    
    this.comboBoxCollisionType.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setProperty(MapObjectProperty.Particle.COLLISIONTYPE, this.comboBoxCollisionType.getSelectedItem().toString());
    }));
    
    this.comboBoxSprite.addActionListener(new MapObjectPropertyActionListener(m -> {
      String sprite = this.comboBoxSprite.getSelectedItem().toString();
      m.setProperty(MapObjectProperty.Particle.SPRITE, sprite);
    }));
    
    this.comboBoxAlign.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setProperty(MapObjectProperty.Emitter.ORIGIN_ALIGN, this.comboBoxAlign.getSelectedItem().toString());
    }));
    
    this.comboBoxValign.addActionListener(new MapObjectPropertyActionListener(m -> {
      m.setProperty(MapObjectProperty.Emitter.ORIGIN_VALIGN, this.comboBoxValign.getSelectedItem().toString());
    }));
    
    this.spinnerSpawnRate.addChangeListener(new SpinnerListener(MapObjectProperty.Emitter.SPAWNRATE, this.spinnerSpawnRate));
    this.spinnerSpawnAmount.addChangeListener(new SpinnerListener(MapObjectProperty.Emitter.SPAWNAMOUNT, this.spinnerSpawnAmount));
    this.spinnerUpdateRate.addChangeListener(new SpinnerListener(MapObjectProperty.Emitter.UPDATERATE, this.spinnerUpdateRate));
    this.spinnerTTL.addChangeListener(new SpinnerListener(MapObjectProperty.Emitter.TIMETOLIVE, this.spinnerTTL));
    this.spinnerMaxParticles.addChangeListener(new SpinnerListener(MapObjectProperty.Emitter.MAXPARTICLES, this.spinnerMaxParticles));

    this.spinnerMinDeltaX.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MINDELTAX, this.spinnerMinDeltaX));
    this.spinnerMaxDeltaX.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MAXDELTAX, this.spinnerMaxDeltaX));
    this.spinnerMinDeltaY.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MINDELTAY, this.spinnerMinDeltaY));
    this.spinnerMaxDeltaY.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MAXDELTAY, this.spinnerMaxDeltaY));

    this.spinnerMinGravityX.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MINGRAVITYX, this.spinnerMinGravityX));
    this.spinnerMaxGravityX.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MAXGRAVITYX, this.spinnerMaxGravityX));
    this.spinnerMinGravityY.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MINGRAVITYY, this.spinnerMinGravityY));
    this.spinnerMaxGravityY.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MAXGRAVITYY, this.spinnerMaxGravityY));

    this.spinnerMinStartWidth.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MINSTARTWIDTH, this.spinnerMinStartWidth));
    this.spinnerMaxStartWidth.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MAXSTARTWIDTH, this.spinnerMaxStartWidth));
    this.spinnerMinStartHeight.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MINSTARTHEIGHT, this.spinnerMinStartHeight));
    this.spinnerMaxStartHeight.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MAXSTARTHEIGHT, this.spinnerMaxStartHeight));

    this.spinnerMinDeltaWidth.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MINDELTAWIDTH, this.spinnerMinDeltaWidth));
    this.spinnerMaxDeltaWidth.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MAXDELTAWIDTH, this.spinnerMaxDeltaWidth));
    this.spinnerMinDeltaHeight.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MINDELTAHEIGHT, this.spinnerMinDeltaHeight));
    this.spinnerMaxDeltaHeight.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MAXDELTAHEIGHT, this.spinnerMaxDeltaHeight));
    this.spinnerMinParticleTTL.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MINTTL, this.spinnerMinParticleTTL));
    this.spinnerMaxParticleTTL.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MAXTTL, this.spinnerMaxParticleTTL));

    this.spinnerMinStartX.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MINX, this.spinnerMinStartX));
    this.spinnerMaxStartX.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MAXX, this.spinnerMaxStartX));
    this.spinnerMinStartY.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MINY, this.spinnerMinStartY));
    this.spinnerMaxStartY.addChangeListener(new SpinnerListener(MapObjectProperty.Particle.MAXY, this.spinnerMaxStartY));
    
    this.spinnerColorDeviation.addChangeListener(new SpinnerListener(MapObjectProperty.Emitter.COLORDEVIATION, this.spinnerColorDeviation));
    this.spinnerAlphaDeviation.addChangeListener(new SpinnerListener(MapObjectProperty.Emitter.ALPHADEVIATION, this.spinnerAlphaDeviation));

    this.txt.addActionListener(new MapObjectPropertyActionListener(m -> m.setProperty(MapObjectProperty.Particle.TEXT, this.txt.getText())));
    
    this.checkBoxFade.addActionListener(new MapObjectPropertyActionListener(m -> m.setProperty(MapObjectProperty.Particle.FADE, this.checkBoxFade.isSelected())));
  }
  
  private void updateTabbedGroup() {
    if (this.comboBoxParticleType.getSelectedItem() == ParticleType.SPRITE) {
      this.tabbedPanel.setSelectedIndex(1);
      tabbedPanel.setEnabledAt(0, false);
      tabbedPanel.setEnabledAt(1, true);
    } else {
      this.tabbedPanel.setSelectedIndex(0);
      tabbedPanel.setEnabledAt(0, true);
      tabbedPanel.setEnabledAt(1, false);
    }
  }

  @Override
  protected void clearControls() {
    this.comboBoxParticleType.setSelectedItem(ParticleType.RECTANGLE);
    this.comboBoxSprite.setSelectedItem(null);
    this.comboBoxSpriteType.setSelectedIndex(0);
    this.comboBoxCollisionType.setSelectedIndex(0);
    this.comboBoxAlign.setSelectedIndex(0);
    this.comboBoxValign.setSelectedIndex(0);
    
    this.spinnerSpawnRate.setValue(0);
    this.spinnerSpawnAmount.setValue(Emitter.DEFAULT_SPAWNAMOUNT);
    this.spinnerUpdateRate.setValue(0);
    this.spinnerTTL.setValue(0);
    this.spinnerMaxParticles.setValue(Emitter.DEFAULT_MAXPARTICLES);

    this.spinnerMinDeltaX.setValue(-PARTICLEDELTA_DEFAULT_VALUE);
    this.spinnerMaxDeltaX.setValue(PARTICLEDELTA_DEFAULT_VALUE);
    this.spinnerMinDeltaY.setValue(-PARTICLEDELTA_DEFAULT_VALUE);
    this.spinnerMaxDeltaY.setValue(PARTICLEDELTA_DEFAULT_VALUE);

    this.spinnerMinGravityX.setValue(0);
    this.spinnerMaxGravityX.setValue(0);
    this.spinnerMinGravityY.setValue(0);
    this.spinnerMaxGravityY.setValue(0);

    this.spinnerMinStartWidth.setValue(1);
    this.spinnerMaxStartWidth.setValue(1);
    this.spinnerMinStartHeight.setValue(1);
    this.spinnerMaxStartHeight.setValue(1);

    this.spinnerMinDeltaWidth.setValue(0);
    this.spinnerMaxDeltaWidth.setValue(0);
    this.spinnerMinDeltaHeight.setValue(0);
    this.spinnerMaxDeltaHeight.setValue(0);
    
    this.spinnerMinStartX.setValue(0);
    this.spinnerMaxStartX.setValue(0);
    
    this.spinnerMinStartY.setValue(0);
    this.spinnerMaxStartY.setValue(0);

    this.spinnerMinParticleTTL.setValue(PARTICLEMINTTL_DEFAULT_VALUE);
    this.spinnerMaxParticleTTL.setValue(0);

    this.spinnerColorDeviation.setValue(0);
    this.spinnerAlphaDeviation.setValue(0);

    this.txt.setText("");

    this.rdbtnRandomDeltaX.setSelected(true);
    this.rdbtnRandomDeltaY.setSelected(true);

    this.rdbtnLockGravityX.setSelected(true);
    this.rdbtnLockGravityY.setSelected(true);
    this.rdbtnLockStartWidth.setSelected(true);
    this.rdbtnLockStartHeight.setSelected(true);
    this.rdbtnLockDeltaWidth.setSelected(true);
    this.rdbtnLockDeltaHeight.setSelected(true);
    this.rdbtnLockParticleTTL.setSelected(true);
    this.rdbtnLockStartX.setSelected(true);
    this.rdbtnLockStartY.setSelected(true);
    this.model.setRowCount(0);
    this.colors.clear();
    
    this.checkBoxFade.setSelected(true);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    ParticleType type = mapObject.getEnumProperty(MapObjectProperty.Emitter.PARTICLETYPE, ParticleType.class, ParticleType.RECTANGLE);
    this.comboBoxParticleType.setSelectedItem(type);
    this.updateTabbedGroup();
    this.comboBoxSprite.setSelectedItem(mapObject.getStringProperty(MapObjectProperty.Particle.SPRITE));
    this.comboBoxCollisionType.setSelectedItem(mapObject.getEnumProperty(MapObjectProperty.Particle.COLLISIONTYPE, CollisionType.class, CollisionType.NONE));
    this.comboBoxAlign.setSelectedItem(mapObject.getEnumProperty(MapObjectProperty.Emitter.ORIGIN_ALIGN, Align.class, Align.CENTER));
    this.comboBoxValign.setSelectedItem(mapObject.getEnumProperty(MapObjectProperty.Emitter.ORIGIN_VALIGN, Valign.class, Valign.MIDDLE));
    
    // TODO: implement this
    this.comboBoxSpriteType.setSelectedIndex(0);

    this.spinnerSpawnRate.setValue(mapObject.getIntProperty(MapObjectProperty.Emitter.SPAWNRATE));
    this.spinnerSpawnAmount.setValue(mapObject.getIntProperty(MapObjectProperty.Emitter.SPAWNAMOUNT, Emitter.DEFAULT_SPAWNAMOUNT));
    this.spinnerUpdateRate.setValue(mapObject.getIntProperty(MapObjectProperty.Emitter.UPDATERATE));
    this.spinnerTTL.setValue(mapObject.getIntProperty(MapObjectProperty.Emitter.TIMETOLIVE));
    this.spinnerMaxParticles.setValue(mapObject.getIntProperty(MapObjectProperty.Emitter.MAXPARTICLES, Emitter.DEFAULT_MAXPARTICLES));

    // TODO: implement this
    this.spinnerColorDeviation.setValue(mapObject.getDoubleProperty(MapObjectProperty.Emitter.COLORDEVIATION));
    this.spinnerAlphaDeviation.setValue(mapObject.getDoubleProperty(MapObjectProperty.Emitter.ALPHADEVIATION));

    this.spinnerMinDeltaX.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MINDELTAX, -PARTICLEDELTA_DEFAULT_VALUE));
    this.spinnerMaxDeltaX.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MAXDELTAX, PARTICLEDELTA_DEFAULT_VALUE));
    this.spinnerMinDeltaY.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MINDELTAY, -PARTICLEDELTA_DEFAULT_VALUE));
    this.spinnerMaxDeltaY.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MAXDELTAY, PARTICLEDELTA_DEFAULT_VALUE));

    this.spinnerMinGravityX.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MINGRAVITYX));
    this.spinnerMaxGravityX.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MAXGRAVITYX));
    this.spinnerMinGravityY.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MINGRAVITYY));
    this.spinnerMaxGravityY.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MAXGRAVITYY));

    this.spinnerMinStartWidth.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MINSTARTWIDTH, 1));
    this.spinnerMaxStartWidth.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MAXSTARTWIDTH, 1));
    this.spinnerMinStartHeight.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MINSTARTHEIGHT, 1));
    this.spinnerMaxStartHeight.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MAXSTARTHEIGHT, 1));

    this.spinnerMinDeltaWidth.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MINDELTAWIDTH));
    this.spinnerMaxDeltaWidth.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MAXDELTAWIDTH));
    this.spinnerMinDeltaHeight.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MINDELTAHEIGHT));
    this.spinnerMaxDeltaHeight.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MAXDELTAHEIGHT));

    this.spinnerMinParticleTTL.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MINTTL, PARTICLEMINTTL_DEFAULT_VALUE));
    this.spinnerMaxParticleTTL.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MAXTTL));

    this.spinnerMinStartX.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MINX));
    this.spinnerMaxStartX.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MAXX));
    
    this.spinnerMinStartY.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MINY));
    this.spinnerMaxStartY.setValue(mapObject.getDoubleProperty(MapObjectProperty.Particle.MAXY));
    
    this.txt.setText(mapObject.getStringProperty(MapObjectProperty.Particle.TEXT));

    this.rdbtnRandomDeltaX.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.DELTAX_RANDOM, true));
    this.rdbtnRandomDeltaY.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.DELTAY_RANDOM, true));

    this.rdbtnRandomGravityX.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.GRAVITYX_RANDOM));
    this.rdbtnRandomGravityY.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.GRAVITYY_RANDOM));
    this.rdbtnRandomStartWidth.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.STARTWIDTH_RANDOM));
    this.rdbtnRandomStartHeight.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.STARTHEIGHT_RANDOM));
    this.rdbtnRandomDeltaWidth.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.DELTAWIDTH_RANDOM));
    this.rdbtnRandomDeltaHeight.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.DELTAHEIGHT_RANDOM));
    this.rdbtnRandomParticleTTL.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.TTL_RANDOM));
    
    this.rdbtnRandomStartX.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.X_RANDOM));
    this.rdbtnRandomStartY.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.Y_RANDOM));

    this.model.setRowCount(0);
    for (ParticleColor color : EmitterMapObjectLoader.getColors(mapObject)) {
      this.colors.add(color);
      this.model.addRow(new Object[] { null, color.toString() });
    }
    
    this.checkBoxFade.setSelected(mapObject.getBoolProperty(MapObjectProperty.Particle.FADE, true));
  }

  private static SpinnerNumberModel getParticleMinModel() {
    return new SpinnerNumberModel(-PARTICLEDELTA_DEFAULT_VALUE, -PARTICLESPINNER_MAX_VALUE, PARTICLESPINNER_MAX_VALUE, 0.1);
  }

  private static SpinnerNumberModel getParticleMaxModel() {
    return new SpinnerNumberModel(PARTICLEDELTA_DEFAULT_VALUE, -PARTICLESPINNER_MAX_VALUE, PARTICLESPINNER_MAX_VALUE, 0.1);
  }
  
  private static SpinnerNumberModel getLocationModel() {
    return new SpinnerNumberModel(0.0, -PARTICLESPINNER_MAX_VALUE, PARTICLESPINNER_MAX_VALUE, 0.1);
  }

  private static SpinnerNumberModel getDeltaModel() {
    return new SpinnerNumberModel(0.0, -PARTICLEDELTA_MAX_VALUE, PARTICLEDELTA_MAX_VALUE, 0.01);
  }

  private static SpinnerNumberModel getParticleDimensionModel() {
    return new SpinnerNumberModel(1.0, 0.0, PARTICLESPINNER_MAX_VALUE, 1.0);
  }

  private static SpinnerNumberModel getPercentModel() {
    return new SpinnerNumberModel(0.0, 0.0, 1.0, 0.01);
  }

  private class ParticleRadioButtonListener extends MapObjectPropertyItemListener {
    private final JSpinner max;

    public ParticleRadioButtonListener(String mapObjectPropery, JRadioButton button, JSpinner max) {
      super(m -> m.setProperty(mapObjectPropery, button.isSelected()));
      this.max = max;
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      this.max.setEnabled(e.getStateChange() != ItemEvent.SELECTED);
    }
  }

  private class ParticleColorCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

      // Cells are by default rendered as a JLabel.
      JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

      if (colors.size() - 1 < row) {
        return l;
      }

      Color bg = colors.get(row).toColor();
      l.setBackground(bg);
      l.setForeground(bg.getAlpha() > 100 ? Color.WHITE : Color.BLACK);

      // Return the JLabel which renders the cell.
      return l;
    }
  }
}

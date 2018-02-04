package de.gurkenlabs.utiliti.swing.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleColor;
import de.gurkenlabs.litiengine.graphics.particles.xml.ParticleType;
import de.gurkenlabs.util.ArrayUtilities;
import de.gurkenlabs.utiliti.EditorScreen;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.swing.ColorChooser;

@SuppressWarnings("serial")
public class EmitterPropertyPanel extends PropertyPanel<IMapObject> {
  private static final double PARTICLESPINNER_MAX_VALUE = 100.0;
  private DefaultTableModel model;
  private JTextField txt;
  private JButton btnSelectColor;
  private JTable table;
  private List<ParticleColor> colors;
  private IMapObject backupMapObject;
  private JTabbedPane tabbedPanel;
  private JPanel colorPanel;
  private JPanel spritePanel;
  
  private final JComboBox<ParticleType> comboBoxParticleType = new JComboBox<>();
  private final JComboBox<String> comboBoxSpriteType = new JComboBox<>();
  private final JComboBox<String> comboBoxSprite= new JComboBox<>();
  
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
  private JCheckBox chckbxStaticPhysics;
  
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
  private final JSpinner spinnerSpawnRate = new JSpinner();
  private final JSpinner spinnerSpawnAmount = new JSpinner();
  private final JSpinner spinnerUpdateDelay = new JSpinner();
  private final JSpinner spinnerTTL = new JSpinner();
  private final JSpinner spinnerMaxParticles = new JSpinner();
  private final JSpinner spinnerColorDeviation = new JSpinner(getPercentModel());
  private final JSpinner spinnerAlphaDeviation = new JSpinner(getPercentModel());
  private final JSpinner spinnerMinDeltaX = new JSpinner(getParticleModel());
  private final JSpinner spinnerMinDeltaY = new JSpinner(getParticleModel());
  private final JSpinner spinnerMinGravityX = new JSpinner(getParticleModel());
  private final JSpinner spinnerMinGravityY = new JSpinner(getParticleModel());
  private final JSpinner spinnerMinStartWidth = new JSpinner(getParticleModel());
  private final JSpinner spinnerMinStartHeight = new JSpinner(getParticleModel());
  private final JSpinner spinnerMinDeltaWidth = new JSpinner(getParticleModel());
  private final JSpinner spinnerMinDeltaHeight = new JSpinner(getParticleModel());
  private final JSpinner spinnerMaxDeltaX = new JSpinner(getParticleModel());
  private final JSpinner spinnerMaxDeltaY = new JSpinner(getParticleModel());
  private final JSpinner spinnerMaxGravityX = new JSpinner(getParticleModel());
  private final JSpinner spinnerMaxGravityY = new JSpinner(getParticleModel());
  private final JSpinner spinnerMaxStartWidth = new JSpinner(getParticleModel());
  private final JSpinner spinnerMaxStartHeight = new JSpinner(getParticleModel());
  private final JSpinner spinnerMaxDeltaWidth = new JSpinner(getParticleModel());
  private final JSpinner spinnerMaxDeltaHeight = new JSpinner(getParticleModel());

  /**
   * Create the dialog.
   */
  public EmitterPropertyPanel() {
    super();
    setBounds(100, 100, 700, 464);
    this.setBorder(new EmptyBorder(5, 5, 5, 5));

    this.colors = new ArrayList<>();
    
    this.initButtons();
    this.initSpinners();

    this.comboBoxParticleType.setModel(new DefaultComboBoxModel<ParticleType>(ParticleType.values()));
    this.comboBoxSpriteType.addItem(Resources.get("panel_animation"));
    this.comboBoxSpriteType.addItem(Resources.get("panel_spritesheet"));
    this.comboBoxSpriteType.setSelectedIndex(0);
    for (SpriteSheetInfo s : EditorScreen.instance().getGameFile().getSpriteSheets()) {
      this.comboBoxSprite.addItem(s.getName());
    }
    
    this.chckbxStaticPhysics = new JCheckBox("");

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

    this.table = new JTable();
    this.table.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "percentage", "color" }));
    this.model = (DefaultTableModel) table.getModel();
    this.table.setFont(Program.TEXT_FONT);
    this.table.getColumnModel().getColumn(1).setCellRenderer(new ParticleColorCellRenderer());
    scrollPane.setViewportView(this.table);
    this.spritePanel = new JPanel();
    this.tabbedPanel.addTab(Resources.get("panel_sprite"), null, spritePanel, null);
    this.tabbedPanel.setEnabledAt(1, false);
    
    // init all labels
    JLabel labelEmitterData = new JLabel(Resources.get("panel_emitterData"));
    labelEmitterData.setFont(new Font("Tahoma", Font.BOLD, 12));

    JLabel lblSpawnRate = new JLabel(Resources.get("panel_emitterSpawnRate"));
    JLabel lblSpawnAmount = new JLabel(Resources.get("panel_emitterSpawnAmount"));
    JLabel lblParticleData = new JLabel(Resources.get("panel_particleData"));
    lblParticleData.setFont(new Font("Tahoma", Font.BOLD, 12));
    
    JLabel lblDeltax = new JLabel(Resources.get("panel_particleDeltaX"));
    lblDeltax.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltax.setFont(new Font("Tahoma", Font.PLAIN, 11));
    
    JLabel lblDeltaY = new JLabel(Resources.get("panel_particleDeltaY"));
    lblDeltaY.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltaY.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin1 = new JLabel(Resources.get("panel_min"));
    lblMin1.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin1.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMin2 = new JLabel(Resources.get("panel_min"));
    lblMin2.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin2.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblLock = new JLabel(Resources.get("panel_lock"));
    lblLock.setHorizontalAlignment(SwingConstants.LEFT);
    lblLock.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblRandom = new JLabel(Resources.get("panel_random"));
    lblRandom.setHorizontalAlignment(SwingConstants.CENTER);
    lblRandom.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMax = new JLabel(Resources.get("panel_max"));
    lblMax.setEnabled(false);
    lblMax.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMax1 = new JLabel(Resources.get("panel_max"));
    lblMax1.setEnabled(false);
    lblMax1.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax1.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblUpdateDelay = new JLabel(Resources.get("panel_emitterUpdateDelay"));

    JLabel lblGravityX = new JLabel(Resources.get("panel_particleGravityX"));
    lblGravityX.setHorizontalAlignment(SwingConstants.LEFT);
    lblGravityX.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin3 = new JLabel(Resources.get("panel_min"));
    lblMin3.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin3.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMax2 = new JLabel(Resources.get("panel_max"));
    lblMax2.setEnabled(false);
    lblMax2.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax2.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel labelTtl = new JLabel(Resources.get("panel_emitterTTL"));

    JLabel lblGravityY = new JLabel(Resources.get("panel_particleGravityY"));
    lblGravityY.setHorizontalAlignment(SwingConstants.LEFT);
    lblGravityY.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin4 = new JLabel(Resources.get("panel_min"));
    lblMin4.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin4.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMax3 = new JLabel(Resources.get("panel_max"));
    lblMax3.setEnabled(false);
    lblMax3.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax3.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMaxParticles = new JLabel(Resources.get("panel_emitterMaxParticles"));
    
    JLabel lblStartWidth = new JLabel(Resources.get("panel_particleStartWidth"));
    lblStartWidth.setHorizontalAlignment(SwingConstants.LEFT);
    lblStartWidth.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin5 = new JLabel(Resources.get("panel_min"));
    lblMin5.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin5.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMax4 = new JLabel(Resources.get("panel_max"));
    lblMax4.setEnabled(false);
    lblMax4.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax4.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblParticleType = new JLabel(Resources.get("panel_particleType"));
    
    JLabel lblStartHeight = new JLabel(Resources.get("panel_particleStartHeight"));
    lblStartHeight.setHorizontalAlignment(SwingConstants.LEFT);
    lblStartHeight.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin6 = new JLabel(Resources.get("panel_min"));
    lblMin6.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin6.setFont(new Font("Tahoma", Font.ITALIC, 11));
    
    JLabel lblMax5 = new JLabel(Resources.get("panel_max"));
    lblMax5.setEnabled(false);
    lblMax5.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax5.setFont(new Font("Tahoma", Font.ITALIC, 11));
    
    JLabel lblColorDeviation = new JLabel(Resources.get("panel_colorDeviation"));

    JLabel lblAlphaDeviation = new JLabel(Resources.get("panel_alphaDeviation"));

    JLabel lblEmittertype = new JLabel(Resources.get("panel_spriteType"));

    JLabel lblSpritesheet = new JLabel(Resources.get("panel_sprite"));

    JLabel lblDeltaWidth = new JLabel(Resources.get("panel_particleDeltaWidth"));
    lblDeltaWidth.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltaWidth.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin7 = new JLabel(Resources.get("panel_min"));
    lblMin7.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin7.setFont(new Font("Tahoma", Font.ITALIC, 11));
    
    JLabel lblMax6 = new JLabel(Resources.get("panel_max"));
    lblMax6.setEnabled(false);
    lblMax6.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax6.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblDeltaHeight = new JLabel(Resources.get("panel_particleDeltaHeight"));
    lblDeltaHeight.setHorizontalAlignment(SwingConstants.LEFT);
    lblDeltaHeight.setFont(new Font("Tahoma", Font.PLAIN, 11));

    JLabel lblMin = new JLabel(Resources.get("panel_min"));
    lblMin.setHorizontalAlignment(SwingConstants.CENTER);
    lblMin.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblMax7 = new JLabel(Resources.get("panel_max"));
    lblMax7.setEnabled(false);
    lblMax7.setHorizontalAlignment(SwingConstants.CENTER);
    lblMax7.setFont(new Font("Tahoma", Font.ITALIC, 11));

    JLabel lblStaticPhysics = new JLabel(Resources.get("panel_particleStaticPhysics"));
    lblStaticPhysics.setHorizontalAlignment(SwingConstants.LEFT);
    lblStaticPhysics.setFont(new Font("Tahoma", Font.PLAIN, 11));


    JLabel lblText = new JLabel(Resources.get("panel_particleText"));
    lblText.setEnabled(false);
    lblText.setHorizontalAlignment(SwingConstants.LEFT);
    lblText.setFont(new Font("Tahoma", Font.PLAIN, 11));
    
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
            .addGroup(groupLayoutSpritePanel.createParallelGroup(Alignment.LEADING).addGroup(groupLayoutSpritePanel.createSequentialGroup().addGap(3).addComponent(lblEmittertype)).addComponent(comboBoxSpriteType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addGap(10)
            .addGroup(groupLayoutSpritePanel.createParallelGroup(Alignment.LEADING).addGroup(groupLayoutSpritePanel.createSequentialGroup().addGap(3).addComponent(lblSpritesheet)).addComponent(comboBoxSprite, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))));
    spritePanel.setLayout(groupLayoutSpritePanel);
    
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addGap(10)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(labelEmitterData).addGap(226).addComponent(lblParticleData).addGap(219).addComponent(lblLock).addGap(13).addComponent(lblRandom))
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
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(labelEmitterData).addComponent(lblParticleData).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(lblLock)).addGroup(groupLayout.createSequentialGroup().addGap(1).addComponent(lblRandom))).addGap(12)
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

  private void initSpinners() {
    this.spinnerMaxDeltaX.setEnabled(false);
    this.spinnerMaxDeltaY.setEnabled(false);
    this.spinnerMaxGravityX.setEnabled(false);
    this.spinnerMaxGravityY.setEnabled(false);
    this.spinnerMaxStartWidth.setEnabled(false);
    this.spinnerMaxStartHeight.setEnabled(false);
    this.spinnerMaxDeltaWidth.setEnabled(false);
    this.spinnerMaxDeltaHeight.setEnabled(false);
    
    this.spinnerMaxParticles.setModel(new SpinnerNumberModel(0, 0, 5000, 1));
  }

  private void initButtons() {
    this.buttonGroupDeltaX.add(rdbtnLockDeltaX);
    this.rdbtnLockDeltaX.setHorizontalAlignment(SwingConstants.CENTER);
    this.rdbtnLockDeltaX.setSelected(true);
    this.buttonGroupDeltaX.add(rdbtnRandomDeltaX);

    this.buttonGroupDeltaY.add(rdbtnLockDeltaY);
    this.rdbtnLockDeltaY.setSelected(true);

    this.buttonGroupDeltaY.add(rdbtnRandomDeltaY);
    this.buttonGroupGravityX.add(rdbtnLockGravityX);
    this.rdbtnLockGravityX.setSelected(true);

    this.buttonGroupGravityX.add(rdbtnRandomGravityX);
    this.buttonGroupGravityY.add(rdbtnLockGravityY);
    this.rdbtnLockGravityY.setSelected(true);

    this.buttonGroupGravityY.add(rdbtnRandomGravityY);
    this.buttonGroupStartWidth.add(rdbtnLockStartWidth);
    this.rdbtnLockStartWidth.setSelected(true);

    this.buttonGroupStartWidth.add(rdbtnRandomStartWidth);
    this.buttonGroupStartHeight.add(rdbtnLockStartHeight);
    this.rdbtnLockStartHeight.setSelected(true);
    this.buttonGroupStartHeight.add(rdbtnRandomStartHeight);
    this.btnSelectColor = new JButton();
    this.btnSelectColor.setIcon(new ImageIcon(Resources.getImage("button-color.png")));
    this.btnSelectColor.setMinimumSize(new Dimension(30, 10));
    this.btnSelectColor.setMaximumSize(new Dimension(30, 10));

    this.buttonGroupDeltaWidth.add(rdbtnLockDeltaWidth);
    this.rdbtnLockDeltaWidth.setSelected(true);

    this.buttonGroupDeltaWidth.add(rdbtnRandomDeltaWidth);
    this.buttonGroupDeltaHeight.add(rdbtnLockDeltaHeight);
    this.rdbtnLockDeltaHeight.setSelected(true);

    this.buttonGroupDeltaHeight.add(rdbtnRandomDeltaHeight);
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
      if (table.getSelectedRow() == -1) {
        return;
      }

      ParticleColor color = colors.get(table.getSelectedRow());
      Color result = ColorChooser.showRgbDialog(Resources.get("panel_selectEmitterColor"), color.toColor());
      if (result == null) {
        return;
      }

      ParticleColor c = new ParticleColor(result);
      colors.set(table.getSelectedRow(), c);
      model.setValueAt(c, table.getSelectedRow(), 1);
      if (getDataSource() != null) {
        String commaSeperated = ArrayUtilities.getCommaSeparatedString(colors);
        this.getDataSource().setCustomProperty(MapObjectProperty.EMITTER_COLORS, commaSeperated);
      }
    });

    btnRemoveColor.addActionListener(a -> {
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

    btnAddColor.addActionListener(a -> {
      ParticleColor c = new ParticleColor();
      colors.add(c);
      model.addRow(new Object[] { null, c.toString() });
    });


    this.comboBoxParticleType.addActionListener(new MapObjectPropertyActionListener(m -> {
      ParticleType particleType = (ParticleType) this.comboBoxParticleType.getSelectedItem();
      if (particleType == ParticleType.SPRITE) {
        this.tabbedPanel.setSelectedIndex(1);
        tabbedPanel.setEnabledAt(0, false);
        tabbedPanel.setEnabledAt(1, true);
      } else {
        this.tabbedPanel.setSelectedIndex(0);
        tabbedPanel.setEnabledAt(0, true);
        tabbedPanel.setEnabledAt(1, false);
      }
      m.setCustomProperty(MapObjectProperty.EMITTER_PARTICLETYPE, particleType.toString());
    }));

    this.spinnerSpawnRate.addChangeListener(new SpinnerListener(MapObjectProperty.EMITTER_SPAWNRATE, this.spinnerSpawnRate));
    this.spinnerSpawnAmount.addChangeListener(new SpinnerListener(MapObjectProperty.EMITTER_SPAWNAMOUNT, this.spinnerSpawnAmount));
    this.spinnerUpdateDelay.addChangeListener(new SpinnerListener(MapObjectProperty.EMITTER_UPDATEDELAY, this.spinnerUpdateDelay));
    this.spinnerTTL.addChangeListener(new SpinnerListener(MapObjectProperty.EMITTER_TIMETOLIVE, this.spinnerTTL));
    this.spinnerMaxParticles.addChangeListener(new SpinnerListener(MapObjectProperty.EMITTER_MAXPARTICLES, this.spinnerMaxParticles));

    this.spinnerMinDeltaX.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MINDELTAX, this.spinnerMinDeltaX));
    this.spinnerMaxDeltaX.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MAXDELTAX, this.spinnerMaxDeltaX));
    this.spinnerMinDeltaY.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MINDELTAY, this.spinnerMinDeltaY));
    this.spinnerMaxDeltaY.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MAXDELTAY, this.spinnerMaxDeltaY));

    this.spinnerMinGravityX.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MINGRAVITYX, this.spinnerMinGravityX));
    this.spinnerMaxGravityX.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MAXGRAVITYX, this.spinnerMaxGravityX));
    this.spinnerMinGravityY.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MINGRAVITYX, this.spinnerMinGravityY));
    this.spinnerMaxGravityY.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MAXGRAVITYX, this.spinnerMaxGravityY));

    this.spinnerMinStartWidth.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MINSTARTWIDTH, this.spinnerMinStartWidth));
    this.spinnerMaxStartWidth.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MAXSTARTWIDTH, this.spinnerMaxStartWidth));
    this.spinnerMinStartHeight.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MINSTARTHEIGHT, this.spinnerMinStartHeight));
    this.spinnerMaxStartHeight.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MAXSTARTHEIGHT, this.spinnerMaxStartHeight));

    this.spinnerMinDeltaWidth.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MINDELTAWIDTH, this.spinnerMinDeltaWidth));
    this.spinnerMaxDeltaWidth.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MAXDELTAWIDTH, this.spinnerMaxDeltaWidth));
    this.spinnerMinDeltaHeight.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MINDELTAHEIGHT, this.spinnerMinDeltaHeight));
    this.spinnerMaxDeltaHeight.addChangeListener(new SpinnerListener(MapObjectProperty.PARTICLE_MAXDELTAHEIGHT, this.spinnerMaxDeltaHeight));

    this.chckbxStaticPhysics.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_STATICPHYSICS, Boolean.toString(this.chckbxStaticPhysics.isSelected()))));

    this.txt.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.PARTICLE_TEXT, this.txt.getText())));

    this.spinnerColorDeviation.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.EMITTER_COLORDEVIATION, this.spinnerColorDeviation.getValue().toString())));
    this.spinnerAlphaDeviation.addChangeListener(new MapObjectPropertyChangeListener(m -> m.setCustomProperty(MapObjectProperty.EMITTER_ALPHADEVIATION, this.spinnerAlphaDeviation.getValue().toString())));

  }

  @Override
  protected void clearControls() {
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
  }

  private static SpinnerNumberModel getParticleModel() {
    return new SpinnerNumberModel(0.0, 0.0, PARTICLESPINNER_MAX_VALUE, 0.1);
  }
  
  private static SpinnerNumberModel getPercentModel() {
    return new SpinnerNumberModel(0.0, 0.0, 1.0, 0.01);
  }
  
  private class ParticleColorCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

      // Cells are by default rendered as a JLabel.
      JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

      Color bg = colors.get(row).toColor();
      l.setBackground(bg);
      l.setForeground(bg.getAlpha() > 100 ? Color.WHITE: Color.BLACK);
      
      // Return the JLabel which renders the cell.
      return l;
    }
  }

}

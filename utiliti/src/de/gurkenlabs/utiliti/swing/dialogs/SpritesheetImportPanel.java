package de.gurkenlabs.utiliti.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.animation.AnimationController;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.resources.TextureAtlas;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.io.Codec;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.swing.ControlBehavior;

@SuppressWarnings("serial")
public class SpritesheetImportPanel extends JPanel implements IUpdateable {
  private static final int PREVIEW_SIZE = 128;
  private static final int SPINNER_WIDTH = 100;

  private JTextField textField;
  private JTable tableKeyFrames;
  private JLabel labelAnimationPreview;
  private JList<SpriteFileWrapper> fileList;
  private DefaultListModel<SpriteFileWrapper> fileListModel;
  private DefaultTableModel treeModel;
  private JLabel labelImage;
  private JLabel labelWidth;
  private JLabel labelHeight;
  private JSpinner spinnerWidth;
  private JSpinner spinnerHeight;
  private boolean isUpdating;
  private transient AnimationController controller;

  private static final Logger log = Logger.getLogger(SpritesheetImportPanel.class.getName());

  public SpritesheetImportPanel(TextureAtlas atlas) {
    this();
    for (TextureAtlas.Sprite sprite : atlas.getSprites()) {
      fileListModel.addElement(new SpriteFileWrapper(sprite));
    }
    this.initModel();
  }

  public SpritesheetImportPanel(File... files) {
    this();
    for (File file : files) {
      fileListModel.addElement(new SpriteFileWrapper(file));
    }

    this.initModel();
  }

  public SpritesheetImportPanel(SpritesheetResource... infos) {
    this();

    for (SpritesheetResource info : infos) {
      fileListModel.addElement(new SpriteFileWrapper(info));
    }

    this.initModel();
  }

  public SpritesheetImportPanel() {
    this.controller = new AnimationController();
    fileListModel = new DefaultListModel<>();
    setPreferredSize(new Dimension(600, 500));
    setLayout(new BorderLayout(10, 10));

    fileList = new JList<>();
    fileList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (renderer instanceof JLabel && value instanceof SpriteFileWrapper) {
          ((JLabel) renderer).setText(((SpriteFileWrapper) value).getName());
        }
        return renderer;
      }
    });

    fileList.getSelectionModel().addListSelectionListener(e -> {
      this.isUpdating = true;
      try {
        SpriteFileWrapper file = fileList.getSelectedValue();
        labelImage.setIcon(file.getIcon());
        labelWidth.setText(file.getWidth() + "px");
        labelHeight.setText(file.getHeight() + "px");
        spinnerWidth.setModel(new SpinnerNumberModel(file.getSpriteWidth(), 1, file.getWidth(), 1));
        spinnerHeight.setModel(new SpinnerNumberModel(file.getSpriteHeight(), 1, file.getHeight(), 1));

        this.updateKeyframeTable(file);
        textField.setText(file.getName());

        this.updatePreview(file);

      } finally {
        this.isUpdating = false;
      }
    });

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setPreferredSize(new Dimension(150, 2));
    scrollPane.setViewportView(fileList);
    add(scrollPane, BorderLayout.WEST);

    labelAnimationPreview = new JLabel("");
    labelAnimationPreview.setPreferredSize(new Dimension(0, PREVIEW_SIZE));
    labelAnimationPreview.setMinimumSize(new Dimension(0, PREVIEW_SIZE));
    labelAnimationPreview.setMaximumSize(new Dimension(0, PREVIEW_SIZE));
    labelAnimationPreview.setHorizontalAlignment(SwingConstants.CENTER);
    scrollPane.setColumnHeaderView(labelAnimationPreview);

    JPanel panel = new JPanel();
    panel.setBorder(null);
    add(panel, BorderLayout.CENTER);

    JLabel lblSpritewidth = new JLabel("spritewidth:");

    spinnerWidth = new JSpinner();
    spinnerWidth.setPreferredSize(new Dimension(SPINNER_WIDTH, spinnerWidth.getPreferredSize().height));
    spinnerWidth.setMinimumSize(new Dimension(SPINNER_WIDTH, spinnerWidth.getPreferredSize().height));
    spinnerWidth.setMaximumSize(new Dimension(SPINNER_WIDTH, spinnerWidth.getPreferredSize().height));
    spinnerWidth.setModel(new SpinnerNumberModel(1, 1, null, 1));
    spinnerWidth.addChangeListener(e -> {
      if (this.isUpdating) {
        return;
      }

      fileList.getSelectedValue().setSpriteWidth((int) this.spinnerWidth.getValue());
      this.updateKeyframeTable(fileList.getSelectedValue());
      this.updatePreview(fileList.getSelectedValue());
    });

    JLabel lblSpriteheight = new JLabel("spriteheight:");

    spinnerHeight = new JSpinner();
    spinnerHeight.setPreferredSize(new Dimension(SPINNER_WIDTH, spinnerHeight.getPreferredSize().height));
    spinnerHeight.setMinimumSize(new Dimension(SPINNER_WIDTH, spinnerHeight.getPreferredSize().height));
    spinnerHeight.setMaximumSize(new Dimension(SPINNER_WIDTH, spinnerHeight.getPreferredSize().height));
    spinnerHeight.setModel(new SpinnerNumberModel(1, 1, null, 1));
    spinnerHeight.addChangeListener(e -> {
      if (this.isUpdating) {
        return;
      }

      fileList.getSelectedValue().setSpriteHeight((int) this.spinnerHeight.getValue());
      this.updateKeyframeTable(fileList.getSelectedValue());
      this.updatePreview(fileList.getSelectedValue());
    });

    JLabel lblNewLabel = new JLabel("width:");

    labelWidth = new JLabel("XXX");

    labelHeight = new JLabel("XXX");

    JLabel lblHeightText = new JLabel("height:");

    JLabel lblName = new JLabel("name:");

    textField = new JTextField();
    ControlBehavior.apply(textField);
    textField.setColumns(10);
    textField.addActionListener(e -> fileList.getSelectedValue().setName(textField.getText()));

    textField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        fileList.getSelectedValue().setName(textField.getText());
      }
    });

    JLabel lblKeyframes = new JLabel("keyframes:");

    labelImage = new JLabel("");
    labelImage.setHorizontalAlignment(SwingConstants.CENTER);
    labelImage.setMaximumSize(new Dimension(0, 128));
    labelImage.setMinimumSize(new Dimension(0, 128));
    labelImage.setPreferredSize(new Dimension(0, 128));

    JScrollPane scrollPane1 = new JScrollPane();
    GroupLayout glPanel = new GroupLayout(panel);
    glPanel
        .setHorizontalGroup(glPanel.createParallelGroup(Alignment.LEADING)
            .addGroup(glPanel.createSequentialGroup().addContainerGap()
                .addGroup(glPanel.createParallelGroup(Alignment.LEADING).addGroup(
                    glPanel.createSequentialGroup().addComponent(labelImage, GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE).addContainerGap())
                    .addGroup(glPanel.createSequentialGroup()
                        .addGroup(
                            glPanel.createParallelGroup(Alignment.LEADING).addComponent(lblKeyframes, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                                .addComponent(lblName, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                                .addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                                .addComponent(lblSpritewidth, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE))
                        .addGap(10)
                        .addGroup(glPanel.createParallelGroup(Alignment.LEADING, false).addComponent(scrollPane1, 0, 0, Short.MAX_VALUE)
                            .addGroup(glPanel.createSequentialGroup().addGroup(glPanel.createParallelGroup(Alignment.TRAILING, false)
                                .addComponent(labelWidth, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(spinnerWidth, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addGroup(glPanel.createParallelGroup(Alignment.LEADING, false)
                                    .addComponent(lblHeightText, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE).addComponent(lblSpriteheight, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addGroup(glPanel.createParallelGroup(Alignment.LEADING, false)
                                    .addComponent(labelHeight, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(spinnerHeight, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)))
                            .addComponent(textField))
                        .addContainerGap(29, Short.MAX_VALUE)))));
    glPanel.setVerticalGroup(glPanel.createParallelGroup(Alignment.LEADING)
        .addGroup(glPanel.createSequentialGroup().addContainerGap()
            .addComponent(labelImage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(glPanel.createParallelGroup(Alignment.LEADING).addGroup(
                glPanel.createParallelGroup(Alignment.BASELINE).addComponent(labelWidth, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewLabel))
                .addGroup(glPanel.createParallelGroup(Alignment.BASELINE)
                    .addComponent(lblHeightText, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelHeight, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)))
            .addGap(14)
            .addGroup(glPanel.createParallelGroup(Alignment.BASELINE).addComponent(lblSpriteheight).addComponent(lblSpritewidth)
                .addComponent(spinnerWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(spinnerHeight, GroupLayout.PREFERRED_SIZE,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.UNRELATED).addGroup(glPanel.createParallelGroup(Alignment.LEADING)
                .addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblName))
            .addGroup(glPanel.createParallelGroup(Alignment.LEADING)
                .addGroup(glPanel.createSequentialGroup().addGap(13).addComponent(lblKeyframes)).addGroup(
                    glPanel.createSequentialGroup().addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)))
            .addContainerGap()));

    tableKeyFrames = new JTable();
    scrollPane1.setViewportView(tableKeyFrames);
    treeModel = new DefaultTableModel(new Object[][] {}, new String[] { "sprite", "duration" }) {
      Class<?>[] columnTypes = new Class<?>[] { Integer.class, Integer.class };

      @Override
      public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
      }

      @Override
      public boolean isCellEditable(int row, int column) {
        return column != 0;
      }
    };

    tableKeyFrames.setModel(treeModel);
    treeModel.addTableModelListener(e -> {
      if (this.isUpdating) {
        return;
      }

      for (int row = 0; row < treeModel.getRowCount(); row++) {
        int keyFrame = (int) treeModel.getValueAt(row, 1);
        fileList.getSelectedValue().getKeyFrames()[row] = keyFrame;
      }

      this.updatePreview(fileList.getSelectedValue());
    });
    panel.setLayout(glPanel);

    Game.loop().attach(this);
  }

  @Override
  public void update() {
    if (this.isUpdating || !this.isVisible()) {
      return;
    }
    this.controller.update();
    BufferedImage img = this.controller.getCurrentImage();
    if (img != null) {
      this.labelAnimationPreview.setIcon(new ImageIcon(img));
    } else {
      this.labelAnimationPreview.setIcon(null);
    }
  }

  public Collection<SpritesheetResource> getSpriteSheets() {
    ArrayList<SpritesheetResource> infos = new ArrayList<>();

    for (int i = 0; i < this.fileList.getModel().getSize(); i++) {
      SpriteFileWrapper wrap = this.fileListModel.getElementAt(i);
      infos.add(wrap.createSpritesheetInfo());
    }

    return infos;
  }

  private void updateKeyframeTable(SpriteFileWrapper file) {
    this.isUpdating = true;

    try {
      int rowCount = treeModel.getRowCount();
      for (int i = rowCount - 1; i >= 0; i--) {
        treeModel.removeRow(i);
      }

      for (int i = 0; i < file.getKeyFrames().length; i++) {
        treeModel.addRow(new Object[] { i + 1, file.getKeyFrames()[i] });
      }
    } finally {
      this.isUpdating = false;
    }
  }

  private void initModel() {
    this.fileList.setModel(this.fileListModel);

    if (this.fileListModel.size() > 0) {
      this.fileList.setSelectedIndex(0);
    }
  }

  private void updatePreview(SpriteFileWrapper file) {
    this.isUpdating = true;
    try {
      this.labelImage.setIcon(file.getIcon());
      this.controller.getAll().clear();

      double factor = (double) PREVIEW_SIZE / Math.max(file.getSpriteWidth(), file.getSpriteHeight());

      BufferedImage img = Imaging.scale(file.getImage(), factor, true);

      Spritesheet sprite = new Spritesheet(img, file.getName() + "-preview", (int) (file.getSpriteWidth() * factor),
          (int) (file.getSpriteHeight() * factor));
      Animation newAnim = new Animation(sprite, true, file.keyFrames);

      this.controller.setDefault(newAnim);
      this.controller.play(newAnim.getName());
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING,
          "The sprite file {0} cannot be scaled correctly for the preview window. Please check if the image file dimensions are divisible by the desired sprite dimensions without remainder.",
          file.name);
    } finally {
      this.isUpdating = false;
    }
  }

  private class SpriteFileWrapper {
    private static final int MAX_WIDTH_ICON = 280;
    private static final int MAX_HEIGHT_ICON = 128;
    private final BufferedImage image;
    private ImageIcon icon;
    private int[] keyFrames;
    private final int width;
    private final int height;
    private int spriteWidth;
    private int spriteHeight;

    private String name;

    public SpriteFileWrapper(TextureAtlas.Sprite sprite) {
      this(Resources.images().get(sprite.getName()), FileUtilities.getFileName(sprite.getName()));
      this.spriteWidth = this.width;
      this.spriteHeight = this.height;
      this.updateSprite();
    }

    public SpriteFileWrapper(File file) {
      this(Resources.images().get(file.getAbsolutePath()), FileUtilities.getFileName(file.getName()));
      this.spriteWidth = this.width;
      this.spriteHeight = this.height;
      this.updateSprite();
    }

    public SpriteFileWrapper(SpritesheetResource info) {
      this(Codec.decodeImage(info.getImage()), info.getName());
      this.spriteWidth = info.getWidth();
      this.spriteHeight = info.getHeight();

      if (info.getKeyframes() != null && info.getKeyframes().length > 0) {
        this.keyFrames = info.getKeyframes();
        this.updateGridImage();
      } else {
        this.updateSprite();
      }
    }

    private SpriteFileWrapper(BufferedImage image, String name) {
      this.image = image;
      this.width = this.image.getWidth();
      this.height = this.image.getHeight();
      this.name = name;
    }

    public int getSpriteWidth() {
      return spriteWidth;
    }

    public int getSpriteHeight() {
      return spriteHeight;
    }

    public String getName() {
      return name;
    }

    public int[] getKeyFrames() {
      return keyFrames;
    }

    public ImageIcon getIcon() {
      return icon;
    }

    public BufferedImage getImage() {
      return this.image;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }

    public void setSpriteWidth(int spriteWidth) {
      this.spriteWidth = MathUtilities.clamp(spriteWidth, 1, this.getWidth());
      this.updateSprite();
    }

    public void setSpriteHeight(int spriteHeight) {
      this.spriteHeight = MathUtilities.clamp(spriteHeight, 1, this.getHeight());
      this.updateSprite();
    }

    public void setName(String name) {
      this.name = name;
    }

    public void updateSprite() {
      int totalSprites = (this.getWidth() / this.getSpriteWidth()) * (this.getHeight() / this.getSpriteHeight());
      this.keyFrames = new int[totalSprites];
      for (int i = 0; i < totalSprites; i++) {
        this.keyFrames[i] = Animation.DEFAULT_FRAME_DURATION;
      }

      this.updateGridImage();
    }

    private void updateGridImage() {
      BufferedImage scaled = Imaging.scale(this.image, MAX_WIDTH_ICON, MAX_HEIGHT_ICON, true, false);
      BufferedImage img = Imaging.getCompatibleImage(scaled.getWidth() + 1, scaled.getHeight() + 1);
      int cols = this.getWidth() / this.getSpriteWidth();
      int rows = this.getHeight() / this.getSpriteHeight();

      int scaledWidth = scaled.getWidth() / cols;
      int scaledHeight = scaled.getHeight() / rows;

      Graphics2D g = (Graphics2D) img.getGraphics();
      g.drawImage(scaled, 0, 0, null);
      g.setColor(Style.COLOR_COLLISION_BORDER);
      for (int i = 1; i < cols; i++) {
        g.drawLine(i * scaledWidth, 0, i * scaledWidth, scaled.getHeight() - 1);
      }

      for (int i = 1; i < rows; i++) {
        g.drawLine(0, i * scaledHeight, scaled.getWidth() - 1, i * scaledHeight);
      }

      g.drawRect(0, 0, scaled.getWidth() - 1, scaled.getHeight() - 1);

      g.dispose();
      this.icon = new ImageIcon(img);
    }

    public SpritesheetResource createSpritesheetInfo() {
      SpritesheetResource info = new SpritesheetResource(this.image, this.getName(), this.getSpriteWidth(), this.getSpriteHeight());

      boolean nonDefaultFrames = false;
      for (int i = 0; i < this.getKeyFrames().length; i++) {
        if (this.getKeyFrames()[i] != Animation.DEFAULT_FRAME_DURATION) {
          nonDefaultFrames = true;
          break;
        }
      }

      if (nonDefaultFrames) {
        info.setKeyframes(this.getKeyFrames());
      }

      return info;
    }
  }
}

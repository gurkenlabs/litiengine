package de.gurkenlabs.utiliti.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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

public class SpritesheetImportPanel extends JPanel implements IUpdateable {
  private static final int PREVIEW_SIZE = 128;
  private static final int SPINNER_WIDTH = 100;

  private JTextField textField;
  private final JTable tableKeyFrames;
  private final JLabel labelAnimationPreview;
  private final JList<SpriteFileWrapper> fileList;
  private final DefaultListModel<SpriteFileWrapper> fileListModel;
  private final DefaultTableModel treeModel;
  private JLabel labelImage;
  private JLabel labelImageSize;
  private JLabel labelFrameSize;
  private JSpinner spinnerColumns;
  private JSpinner spinnerRows;
  private boolean isUpdating;
  private final transient AnimationController controller;

  private static final Logger log = Logger.getLogger(SpritesheetImportPanel.class.getName());

  public SpritesheetImportPanel(TextureAtlas atlas) {
    this();
    for (TextureAtlas.Sprite sprite : atlas.getSprites()) {
      fileListModel.addElement(new SpriteFileWrapper(sprite));
    }
    this.initModel();
  }

  public SpritesheetImportPanel(Path... files) {
    this();
    for (Path file : files) {
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
    fileList.setCellRenderer(
      new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(
          JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          Component renderer =
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          if (renderer instanceof JLabel jl && value instanceof SpriteFileWrapper sfw) {
            jl.setText(sfw.getName());
          }
          return renderer;
        }
      });

    fileList
      .getSelectionModel()
      .addListSelectionListener(
        e -> {
          this.isUpdating = true;
          try {
            SpriteFileWrapper file = fileList.getSelectedValue();
            labelImage.setIcon(file.getIcon());
            labelImageSize.setText(file.getWidth() + " x " + file.getHeight() + " px");
            labelFrameSize.setText(
              file.getWidth() / (int) spinnerColumns.getValue() + " x " + file.getHeight() / (int) spinnerRows.getValue() + " px");
            spinnerColumns.setModel(
              new SpinnerNumberModel(1, 1, file.getWidth(), 1));
            spinnerRows.setModel(
              new SpinnerNumberModel(1, 1, file.getHeight(), 1));

            this.updateKeyframeTable(file);

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

    JLabel lblSpritecolumns = new JLabel("spriteColumns:");

    spinnerColumns = new JSpinner();
    spinnerColumns.setPreferredSize(new Dimension(SPINNER_WIDTH, spinnerColumns.getPreferredSize().height));
    spinnerColumns.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
    spinnerColumns.addChangeListener(e -> updateGrid());

    JLabel lblSpriterows = new JLabel("spriteRows:");

    spinnerRows = new JSpinner();
    spinnerRows.setPreferredSize(new Dimension(SPINNER_WIDTH, spinnerRows.getPreferredSize().height));
    spinnerRows.setModel(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
    spinnerRows.addChangeListener(e -> updateGrid());

    JLabel lblNewLabel = new JLabel("Image Size:");

    labelImageSize = new JLabel("XXX");

    labelFrameSize = new JLabel("XXX");

    JLabel lblHeightText = new JLabel("Frame Size:");

    JLabel lblName = new JLabel("name:");

    textField = new JTextField();
    ControlBehavior.apply(textField);
    textField.setColumns(10);
    fileList.addListSelectionListener(e -> textField.setText(fileList.getSelectedValue().getName()));

    textField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        updateSelectedFile();
      }
      @Override
      public void removeUpdate(DocumentEvent e) {
        updateSelectedFile();
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        updateSelectedFile();
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
    glPanel.setHorizontalGroup(
      glPanel
        .createParallelGroup(Alignment.LEADING)
        .addGroup(
          glPanel
            .createSequentialGroup()
            .addContainerGap()
            .addGroup(
              glPanel
                .createParallelGroup(Alignment.LEADING)
                .addGroup(
                  glPanel
                    .createSequentialGroup()
                    .addComponent(
                      labelImage, GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                    .addContainerGap())
                .addGroup(
                  glPanel
                    .createSequentialGroup()
                    .addGroup(
                      glPanel
                        .createParallelGroup(Alignment.LEADING)
                        .addComponent(
                          lblKeyframes,
                          GroupLayout.DEFAULT_SIZE,
                          74,
                          Short.MAX_VALUE)
                        .addComponent(
                          lblName,
                          GroupLayout.DEFAULT_SIZE,
                          74,
                          Short.MAX_VALUE)
                        .addComponent(
                          lblNewLabel,
                          GroupLayout.DEFAULT_SIZE,
                          74,
                          Short.MAX_VALUE)
                        .addComponent(
                          lblSpritecolumns,
                          GroupLayout.DEFAULT_SIZE,
                          74,
                          Short.MAX_VALUE))
                    .addGap(10)
                    .addGroup(
                      glPanel
                        .createParallelGroup(Alignment.LEADING, false)
                        .addComponent(scrollPane1, 0, 0, Short.MAX_VALUE)
                        .addGroup(
                          glPanel
                            .createSequentialGroup()
                            .addGroup(
                              glPanel
                                .createParallelGroup(
                                  Alignment.TRAILING, false)
                                .addComponent(
                                  labelImageSize,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE)
                                .addComponent(
                                  spinnerColumns,
                                  GroupLayout.DEFAULT_SIZE,
                                  50,
                                  Short.MAX_VALUE))
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addGroup(
                              glPanel
                                .createParallelGroup(
                                  Alignment.LEADING, false)
                                .addComponent(
                                  lblHeightText,
                                  Alignment.TRAILING,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE)
                                .addComponent(
                                  lblSpriterows,
                                  Alignment.TRAILING,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE))
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addGroup(
                              glPanel
                                .createParallelGroup(
                                  Alignment.LEADING, false)
                                .addComponent(
                                  labelFrameSize,
                                  GroupLayout.DEFAULT_SIZE,
                                  GroupLayout.DEFAULT_SIZE,
                                  Short.MAX_VALUE)
                                .addComponent(
                                  spinnerRows,
                                  GroupLayout.DEFAULT_SIZE,
                                  50,
                                  Short.MAX_VALUE)))
                        .addComponent(textField))
                    .addContainerGap(29, Short.MAX_VALUE)))));
    glPanel.setVerticalGroup(
      glPanel
        .createParallelGroup(Alignment.LEADING)
        .addGroup(
          glPanel
            .createSequentialGroup()
            .addContainerGap()
            .addComponent(
              labelImage,
              GroupLayout.PREFERRED_SIZE,
              GroupLayout.DEFAULT_SIZE,
              GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(
              glPanel
                .createParallelGroup(Alignment.LEADING)
                .addGroup(
                  glPanel
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(
                      labelImageSize,
                      GroupLayout.PREFERRED_SIZE,
                      19,
                      GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNewLabel))
                .addGroup(
                  glPanel
                    .createParallelGroup(Alignment.BASELINE)
                    .addComponent(
                      lblHeightText,
                      GroupLayout.PREFERRED_SIZE,
                      19,
                      GroupLayout.PREFERRED_SIZE)
                    .addComponent(
                      labelFrameSize,
                      GroupLayout.PREFERRED_SIZE,
                      19,
                      GroupLayout.PREFERRED_SIZE)))
            .addGap(14)
            .addGroup(
              glPanel
                .createParallelGroup(Alignment.BASELINE)
                .addComponent(lblSpriterows)
                .addComponent(lblSpritecolumns)
                .addComponent(
                  spinnerColumns,
                  GroupLayout.PREFERRED_SIZE,
                  GroupLayout.DEFAULT_SIZE,
                  GroupLayout.PREFERRED_SIZE)
                .addComponent(
                  spinnerRows,
                  GroupLayout.PREFERRED_SIZE,
                  GroupLayout.DEFAULT_SIZE,
                  GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addGroup(
              glPanel
                .createParallelGroup(Alignment.LEADING)
                .addComponent(
                  textField,
                  GroupLayout.PREFERRED_SIZE,
                  GroupLayout.DEFAULT_SIZE,
                  GroupLayout.PREFERRED_SIZE)
                .addComponent(lblName))
            .addGroup(
              glPanel
                .createParallelGroup(Alignment.LEADING)
                .addGroup(
                  glPanel
                    .createSequentialGroup()
                    .addGap(13)
                    .addComponent(lblKeyframes))
                .addGroup(
                  glPanel
                    .createSequentialGroup()
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(
                      scrollPane1,
                      GroupLayout.DEFAULT_SIZE,
                      153,
                      Short.MAX_VALUE)))
            .addContainerGap()));

    tableKeyFrames = new JTable();
    scrollPane1.setViewportView(tableKeyFrames);
    treeModel =
      new DefaultTableModel(new Object[][] {}, new String[] {"sprite", "duration"}) {
        final Class<?>[] columnTypes = new Class<?>[] {Integer.class, Integer.class};

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
    treeModel.addTableModelListener(
      e -> {
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
        treeModel.addRow(new Object[] {i + 1, file.getKeyFrames()[i]});
      }
    } finally {
      this.isUpdating = false;
    }
  }

  private void initModel() {
    this.fileList.setModel(this.fileListModel);

    if (!this.fileListModel.isEmpty()) {
      SpriteFileWrapper selectedValue = this.fileListModel.getElementAt(0);
      this.fileList.setSelectedValue(selectedValue, true);

      if (selectedValue.wasLoadedFromResource()) {
        // Use sprite width/height if loaded from a SpritesheetResource.
        spinnerColumns.setValue(selectedValue.getWidth() / selectedValue.getSpriteWidth());
        spinnerRows.setValue(selectedValue.getHeight() / selectedValue.getSpriteHeight());
      } else if (selectedValue.getWidth() % selectedValue.getHeight() == 0) {
        // If the height is divisible by the width, assume one row of square sprites.
        spinnerColumns.setValue(selectedValue.getWidth() / selectedValue.getHeight());
        spinnerRows.setValue(1);
      } else {
        // Otherwise, default to one column and one row.
        spinnerColumns.setValue(1);
        spinnerRows.setValue(1);
      }
    }
  }

  private void updateGrid() {
    SpriteFileWrapper file = fileList.getSelectedValue();
    labelFrameSize.setText(file.getWidth() / (int) spinnerColumns.getValue() + " x " + file.getHeight() / (int) spinnerRows.getValue() + " px");

    if (this.isUpdating) {
      return;
    }

    try {
      int columns = (int) spinnerColumns.getValue();
      int rows = (int) spinnerRows.getValue();

      fileList.getSelectedValue().setSpriteWidth(fileList.getSelectedValue().getWidth() / columns);
      fileList.getSelectedValue().setSpriteHeight(fileList.getSelectedValue().getHeight() / rows);

      this.updateKeyframeTable(fileList.getSelectedValue());
      this.updatePreview(fileList.getSelectedValue());
    } catch (NumberFormatException e) {
      log.log(Level.SEVERE, "non-numeric value entered!", e);
    }
  }

  private void updatePreview(SpriteFileWrapper file) {
    this.isUpdating = true;
    try {
      this.labelImage.setIcon(file.getIcon());
      this.controller.getAll().clear();

      double factor =
        (double) PREVIEW_SIZE / Math.max(file.getSpriteWidth(), file.getSpriteHeight());

      BufferedImage img = Imaging.scale(file.getImage(), factor);

      Spritesheet sprite =
        new Spritesheet(
          img,
          file.getName() + "-preview",
          (int) (file.getSpriteWidth() * factor),
          (int) (file.getSpriteHeight() * factor));
      Animation newAnim = new Animation(sprite, true, file.keyFrames);

      this.controller.setDefault(newAnim);
      this.controller.play(newAnim.getName());
    } catch (IllegalArgumentException e) {
      log.log(
        Level.WARNING,
        "The sprite file {0} cannot be scaled correctly for the preview window. Please check if the image file dimensions are divisible by the desired sprite dimensions without remainder.",
        file.name);
    } finally {
      this.isUpdating = false;
    }
  }
  private void updateSelectedFile() {
    int selectedIndex = fileList.getSelectedIndex();
    if (selectedIndex != -1) {
      SpriteFileWrapper selectedFile = fileListModel.getElementAt(selectedIndex);
      selectedFile.setName(textField.getText());
      fileList.repaint();
    }
  }

  private static class SpriteFileWrapper {
    private static final int MAX_WIDTH_ICON = 280;
    private static final int MAX_HEIGHT_ICON = 128;
    private final BufferedImage image;
    private ImageIcon icon;
    private int[] keyFrames;
    private final int width;
    private final int height;
    private int spriteWidth;
    private int spriteHeight;
    private boolean wasLoadedFromResource;

    private String name;

    public SpriteFileWrapper(TextureAtlas.Sprite sprite) {
      this(Resources.images().get(sprite.getName()), FileUtilities.getFileName(sprite.getName()));
      this.spriteWidth = this.width;
      this.spriteHeight = this.height;
      this.updateSprite();
    }

    public SpriteFileWrapper(Path file) {
      this(
        Resources.images().get(file),
        FileUtilities.getFileName(file.getName()));
      this.spriteWidth = this.width;
      this.spriteHeight = this.height;
      this.updateSprite();
    }

    public SpriteFileWrapper(SpritesheetResource info) {
      this(Codec.decodeImage(info.getImage()), info.getName());
      this.spriteWidth = info.getWidth();
      this.spriteHeight = info.getHeight();
      this.wasLoadedFromResource = true;

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

    public boolean wasLoadedFromResource() {
      return wasLoadedFromResource;
    }

    public void setSpriteWidth(int spriteWidth) {
      this.spriteWidth = Math.clamp(spriteWidth, 1, this.getWidth());
      this.updateSprite();
    }

    public void setSpriteHeight(int spriteHeight) {
      this.spriteHeight = Math.clamp(spriteHeight, 1, this.getHeight());
      this.updateSprite();
    }

    public void setName(String name) {
      this.name = name;
    }

    public void updateSprite() {
      int totalSprites =
        (this.getWidth() / this.getSpriteWidth()) * (this.getHeight() / this.getSpriteHeight());
      this.keyFrames = new int[totalSprites];
      for (int i = 0; i < totalSprites; i++) {
        this.keyFrames[i] = Animation.DEFAULT_FRAME_DURATION;
      }

      this.updateGridImage();
    }

    private void updateGridImage() {
      BufferedImage scaled = Imaging.scale(this.image, MAX_WIDTH_ICON, MAX_HEIGHT_ICON, true);
      BufferedImage img = Imaging.getCompatibleImage(Objects.requireNonNull(scaled).getWidth() + 1, scaled.getHeight() + 1);
      int cols = this.getWidth() / this.getSpriteWidth();
      int rows = this.getHeight() / this.getSpriteHeight();

      int scaledWidth = scaled.getWidth() / cols;
      int scaledHeight = scaled.getHeight() / rows;

      Graphics2D g = (Graphics2D) Objects.requireNonNull(img).getGraphics();
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
      SpritesheetResource info =
        new SpritesheetResource(
          this.image, this.getName(), this.getSpriteWidth(), this.getSpriteHeight());

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

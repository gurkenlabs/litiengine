package de.gurkenlabs.utiliti.swing.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

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
import javax.swing.table.DefaultTableModel;

import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.util.io.FileUtilities;

public class SpritesheetImportPanel extends JPanel {
  private JTextField textField;
  private JTable tableKeyFrames;
  private JList<SpriteFileWrapper> fileList;
  private DefaultListModel<SpriteFileWrapper> fileListModel;
  private DefaultTableModel treeModel;
  private JLabel labelImage;
  private JLabel labelWidth;
  private JLabel labelHeight;
  private JSpinner spinnerWidth;
  private JSpinner spinnerHeight;
  private boolean isUpdating;

  public SpritesheetImportPanel(File... files) {
    this();
    fileListModel = new DefaultListModel<>();
    for (File file : files) {
      fileListModel.addElement(new SpriteFileWrapper(file));
    }

    this.fileList.setModel(this.fileListModel);

    if (files.length > 0) {
      this.fileList.setSelectedIndex(0);
    }
  }

  public SpritesheetImportPanel(SpriteSheetInfo... infos) {
    this();
    fileListModel = new DefaultListModel<>();
    for (SpriteSheetInfo info : infos) {
      fileListModel.addElement(new SpriteFileWrapper(info));
    }

    this.fileList.setModel(this.fileListModel);

    if (infos.length > 0) {
      this.fileList.setSelectedIndex(0);
    }
  }

  public SpritesheetImportPanel() {
    setPreferredSize(new Dimension(454, 392));
    setLayout(new BorderLayout(0, 0));

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

        spinnerWidth.setValue(file.getSpriteWidth());
        spinnerHeight.setValue(file.getSpriteHeight());

        this.updateKeyframeTable(file);
        textField.setText(file.getName());
      } finally {
        this.isUpdating = false;
      }
    });

    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setPreferredSize(new Dimension(150, 2));
    scrollPane.setViewportView(fileList);
    add(scrollPane, BorderLayout.WEST);

    JPanel panel = new JPanel();
    panel.setBorder(null);
    add(panel, BorderLayout.CENTER);

    JLabel lblSpritewidth = new JLabel("spritewidth:");

    spinnerWidth = new JSpinner();
    spinnerWidth.setModel(new SpinnerNumberModel(1, 1, null, 1));
    spinnerWidth.addChangeListener(e -> {
      if (this.isUpdating) {
        return;
      }

      fileList.getSelectedValue().setSpriteWidth((int) this.spinnerWidth.getValue());
      this.updateKeyframeTable(fileList.getSelectedValue());
    });

    JLabel lblSpriteheight = new JLabel("spriteheight:");

    spinnerHeight = new JSpinner();
    spinnerHeight.setModel(new SpinnerNumberModel(1, 1, null, 1));
    spinnerHeight.addChangeListener(e -> {
      if (this.isUpdating) {
        return;
      }

      fileList.getSelectedValue().setSpriteHeight((int) this.spinnerHeight.getValue());
      this.updateKeyframeTable(fileList.getSelectedValue());
    });

    JLabel lblNewLabel = new JLabel("width:");
    lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));

    labelWidth = new JLabel("XXX");
    labelWidth.setFont(new Font("Tahoma", Font.PLAIN, 15));

    labelHeight = new JLabel("XXX");
    labelHeight.setFont(new Font("Tahoma", Font.PLAIN, 15));

    JLabel lblHeightText = new JLabel("height:");
    lblHeightText.setFont(new Font("Tahoma", Font.PLAIN, 15));

    JLabel lblName = new JLabel("name:");

    textField = new JTextField();
    textField.setColumns(10);
    textField.addActionListener(e -> {
      fileList.getSelectedValue().setName(textField.getText());
    });

    textField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        fileList.getSelectedValue().setName(textField.getText());
      }
    });

    JLabel lblKeyframes = new JLabel("keyframes:");

    labelImage = new JLabel("");
    labelImage.setHorizontalAlignment(JLabel.CENTER);
    labelImage.setMaximumSize(new Dimension(0, 128));
    labelImage.setMinimumSize(new Dimension(0, 128));
    labelImage.setPreferredSize(new Dimension(0, 128));

    JScrollPane scrollPane_1 = new JScrollPane();
    GroupLayout gl_panel = new GroupLayout(panel);
    gl_panel
        .setHorizontalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
            .addGroup(gl_panel.createSequentialGroup().addContainerGap()
                .addGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(gl_panel.createSequentialGroup().addComponent(labelImage, GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE).addContainerGap()).addGroup(gl_panel.createSequentialGroup()
                    .addGroup(gl_panel.createParallelGroup(Alignment.LEADING).addComponent(lblKeyframes, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE).addComponent(lblName, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE).addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                        .addComponent(lblSpritewidth, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE))
                    .addGap(10)
                    .addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false).addComponent(scrollPane_1, 0, 0, Short.MAX_VALUE)
                        .addGroup(gl_panel.createSequentialGroup().addGroup(gl_panel.createParallelGroup(Alignment.TRAILING, false).addComponent(labelWidth, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(spinnerWidth, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false).addComponent(lblHeightText, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(lblSpriteheight, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(ComponentPlacement.UNRELATED)
                            .addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false).addComponent(labelHeight, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(spinnerHeight, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)))
                        .addComponent(textField))
                    .addContainerGap(29, Short.MAX_VALUE)))));
    gl_panel.setVerticalGroup(gl_panel.createParallelGroup(Alignment.LEADING)
        .addGroup(gl_panel.createSequentialGroup().addContainerGap().addComponent(labelImage, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(labelWidth, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE).addComponent(lblNewLabel))
                .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(lblHeightText, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE).addComponent(labelHeight, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)))
            .addGap(14)
            .addGroup(gl_panel.createParallelGroup(Alignment.BASELINE).addComponent(lblSpriteheight).addComponent(lblSpritewidth).addComponent(spinnerWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(spinnerHeight, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.UNRELATED).addGroup(gl_panel.createParallelGroup(Alignment.LEADING).addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblName))
            .addGroup(gl_panel.createParallelGroup(Alignment.LEADING).addGroup(gl_panel.createSequentialGroup().addGap(13).addComponent(lblKeyframes))
                .addGroup(gl_panel.createSequentialGroup().addPreferredGap(ComponentPlacement.UNRELATED).addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 153, Short.MAX_VALUE)))
            .addContainerGap()));

    tableKeyFrames = new JTable();
    scrollPane_1.setViewportView(tableKeyFrames);
    treeModel = new DefaultTableModel(new Object[][] {}, new String[] { "sprite", "duration" }) {
      Class[] columnTypes = new Class[] { Integer.class, Integer.class };

      @Override
      public Class getColumnClass(int columnIndex) {
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
    });
    panel.setLayout(gl_panel);
  }

  public Collection<SpriteSheetInfo> getSpriteSheets() {
    ArrayList<SpriteSheetInfo> infos = new ArrayList<>();

    for (int i = 0; i < this.fileList.getModel().getSize(); i++) {
      SpriteFileWrapper wrap = this.fileListModel.getElementAt(0);
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

  private class SpriteFileWrapper {
    private final BufferedImage image;
    private final ImageIcon icon;
    private int[] keyFrames;
    private final int width;
    private final int height;
    private int spriteWidth;
    private int spriteHeight;

    private String name;

    public SpriteFileWrapper(File file) {
      this(Resources.getImage(file.getAbsolutePath()), FileUtilities.getFileName(file.getName()));
      this.spriteWidth = this.width;
      this.spriteHeight = this.height;
      this.updateKeyFrames();
    }

    public SpriteFileWrapper(SpriteSheetInfo info) {
      this(ImageProcessing.decodeToImage(info.getImage()), info.getName());
      this.spriteWidth = info.getWidth();
      this.spriteHeight = info.getHeight();

      if (info.getKeyframes() != null) {
        this.keyFrames = info.getKeyframes();
      } else {
        this.updateKeyFrames();
      }
    }

    private SpriteFileWrapper(BufferedImage image, String name) {
      this.icon = new ImageIcon(ImageProcessing.scaleImage(image, 280, 128, true));
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

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }

    public void setSpriteWidth(int spriteWidth) {
      this.spriteWidth = spriteWidth;
      this.updateKeyFrames();
    }

    public void setSpriteHeight(int spriteHeight) {
      this.spriteHeight = spriteHeight;
      this.updateKeyFrames();
    }

    public void setName(String name) {
      this.name = name;
    }

    public void updateKeyFrames() {
      int totalSprites = (this.getWidth() / this.getSpriteWidth()) * (this.getHeight() / this.getSpriteHeight());
      this.keyFrames = new int[totalSprites];
      for (int i = 0; i < totalSprites; i++) {
        this.keyFrames[i] = Animation.DEFAULT_FRAME_DURATION;
      }
    }

    public SpriteSheetInfo createSpritesheetInfo() {
      SpriteSheetInfo info = new SpriteSheetInfo(this.image, this.getName(), this.getSpriteWidth(), this.getSpriteHeight());

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

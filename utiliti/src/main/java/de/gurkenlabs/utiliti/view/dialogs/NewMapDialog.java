package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.environment.tilemap.IMapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerAxis;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerIndex;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class NewMapDialog extends JDialog {
  private static final long serialVersionUID = 1L;

  private final JComboBox<String> orientationCombo;
  private final JTextField nameTextField;
  private final JSpinner widthSpinner;
  private final JSpinner heightSpinner;
  private final JSpinner tileWidthSpinner;
  private final JSpinner tileHeightSpinner;
  private final JComboBox<String> staggerAxisCombo;
  private final JComboBox<String> staggerIndexCombo;
  private final JSpinner hexSideLengthSpinner;
  private final JLabel staggerAxisLabel;
  private final JLabel staggerIndexLabel;
  private final JLabel hexSideLengthLabel;
  private final JPanel staggerPanel;
  private boolean confirmed;

  public NewMapDialog(Container owner) {
    super(null, Resources.strings().get("newmap_title"), Dialog.ModalityType.APPLICATION_MODAL);

    this.nameTextField = new JTextField("map", 20);

    this.orientationCombo = new JComboBox<>(new String[]{
      "Orthogonal",
      "Isometric",
      "Staggered",
      "Hexagonal"
    });

    this.widthSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 9999, 1));
    ControlBehavior.apply(this.widthSpinner);

    this.heightSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 9999, 1));
    ControlBehavior.apply(this.heightSpinner);

    this.tileWidthSpinner = new JSpinner(new SpinnerNumberModel(32, 1, 256, 1));
    ControlBehavior.apply(this.tileWidthSpinner);

    this.tileHeightSpinner = new JSpinner(new SpinnerNumberModel(32, 1, 256, 1));
    ControlBehavior.apply(this.tileHeightSpinner);

    this.staggerAxisCombo = new JComboBox<>(new String[]{"X", "Y"});
    this.staggerAxisCombo.setSelectedItem("Y");

    this.staggerIndexCombo = new JComboBox<>(new String[]{"Odd", "Even"});
    this.staggerIndexCombo.setSelectedItem("Odd");

    this.hexSideLengthSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 256, 2));
    ControlBehavior.apply(this.hexSideLengthSpinner);

    this.staggerAxisLabel = new JLabel(Resources.strings().get("newmap_staggeraxis") + ":");
    this.staggerIndexLabel = new JLabel(Resources.strings().get("newmap_staggerindex") + ":");
    this.hexSideLengthLabel = new JLabel(Resources.strings().get("newmap_hexsidelength") + ":");

    this.staggerPanel = new JPanel(new GridBagLayout());
    this.staggerPanel.setBorder(BorderFactory.createTitledBorder(Resources.strings().get("newmap_stagger")));
    GridBagConstraints staggerGbc = new GridBagConstraints();
    staggerGbc.insets = new Insets(3, 5, 3, 5);
    staggerGbc.fill = GridBagConstraints.HORIZONTAL;

    staggerGbc.gridx = 0;
    staggerGbc.gridy = 0;
    this.staggerPanel.add(this.staggerAxisLabel, staggerGbc);
    staggerGbc.gridx = 1;
    staggerGbc.weightx = 1;
    this.staggerPanel.add(this.staggerAxisCombo, staggerGbc);

    staggerGbc.gridx = 0;
    staggerGbc.gridy = 1;
    staggerGbc.weightx = 0;
    this.staggerPanel.add(this.staggerIndexLabel, staggerGbc);
    staggerGbc.gridx = 1;
    staggerGbc.weightx = 1;
    this.staggerPanel.add(this.staggerIndexCombo, staggerGbc);

    staggerGbc.gridx = 0;
    staggerGbc.gridy = 2;
    staggerGbc.weightx = 0;
    this.staggerPanel.add(this.hexSideLengthLabel, staggerGbc);
    staggerGbc.gridx = 1;
    staggerGbc.weightx = 1;
    this.staggerPanel.add(this.hexSideLengthSpinner, staggerGbc);

    this.staggerPanel.setVisible(false);

    this.orientationCombo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        updateStaggerVisibility();
      }
    });

    this.confirmed = false;

    JPanel contentPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Map settings panel
    JPanel mapPanel = new JPanel(new GridBagLayout());
    mapPanel.setBorder(BorderFactory.createTitledBorder(Resources.strings().get("newmap_map")));
    GridBagConstraints mapGbc = new GridBagConstraints();
    mapGbc.insets = new Insets(3, 5, 3, 5);
    mapGbc.fill = GridBagConstraints.HORIZONTAL;

    mapGbc.gridx = 0;
    mapGbc.gridy = 0;
    mapPanel.add(new JLabel(Resources.strings().get("newmap_name") + ":"), mapGbc);
    mapGbc.gridx = 1;
    mapGbc.weightx = 1;
    mapPanel.add(this.nameTextField, mapGbc);

    mapGbc.gridx = 0;
    mapGbc.gridy = 1;
    mapPanel.add(new JLabel(Resources.strings().get("newmap_orientation") + ":"), mapGbc);
    mapGbc.gridx = 1;
    mapGbc.weightx = 1;
    mapPanel.add(this.orientationCombo, mapGbc);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1;
    contentPanel.add(mapPanel, gbc);

    // Stagger panel (for Staggered/Hexagonal)
    gbc.gridy = 1;
    contentPanel.add(this.staggerPanel, gbc);

    // Map size panel
    JPanel sizePanel = new JPanel(new GridBagLayout());
    sizePanel.setBorder(BorderFactory.createTitledBorder(Resources.strings().get("newmap_size")));
    GridBagConstraints sizeGbc = new GridBagConstraints();
    sizeGbc.insets = new Insets(3, 5, 3, 5);
    sizeGbc.fill = GridBagConstraints.HORIZONTAL;

    sizeGbc.gridx = 0;
    sizeGbc.gridy = 0;
    sizePanel.add(new JLabel(Resources.strings().get("newmap_width") + ":"), sizeGbc);
    sizeGbc.gridx = 1;
    sizeGbc.weightx = 1;
    sizePanel.add(this.widthSpinner, sizeGbc);

    sizeGbc.gridx = 0;
    sizeGbc.gridy = 1;
    sizeGbc.weightx = 0;
    sizePanel.add(new JLabel(Resources.strings().get("newmap_height") + ":"), sizeGbc);
    sizeGbc.gridx = 1;
    sizeGbc.weightx = 1;
    sizePanel.add(this.heightSpinner, sizeGbc);

    gbc.gridy = 2;
    contentPanel.add(sizePanel, gbc);

    // Tile size panel
    JPanel tilePanel = new JPanel(new GridBagLayout());
    tilePanel.setBorder(BorderFactory.createTitledBorder(Resources.strings().get("newmap_tilesize")));
    GridBagConstraints tileGbc = new GridBagConstraints();
    tileGbc.insets = new Insets(3, 5, 3, 5);
    tileGbc.fill = GridBagConstraints.HORIZONTAL;

    tileGbc.gridx = 0;
    tileGbc.gridy = 0;
    tilePanel.add(new JLabel(Resources.strings().get("newmap_width") + ":"), tileGbc);
    tileGbc.gridx = 1;
    tileGbc.weightx = 1;
    tilePanel.add(this.tileWidthSpinner, tileGbc);

    tileGbc.gridx = 0;
    tileGbc.gridy = 1;
    tileGbc.weightx = 0;
    tilePanel.add(new JLabel(Resources.strings().get("newmap_height") + ":"), tileGbc);
    tileGbc.gridx = 1;
    tileGbc.weightx = 1;
    tilePanel.add(this.tileHeightSpinner, tileGbc);

    gbc.gridy = 3;
    contentPanel.add(tilePanel, gbc);

    // Buttons
    JButton okButton = new JButton(Resources.strings().get("newmap_ok"));
    okButton.addActionListener(e -> {
      this.confirmed = true;
      this.setVisible(false);
    });

    JButton cancelButton = new JButton(Resources.strings().get("newmap_cancel"));
    cancelButton.addActionListener(e -> {
      this.confirmed = false;
      this.setVisible(false);
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    gbc.gridy = 4;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;
    contentPanel.add(buttonPanel, gbc);

    this.setContentPane(contentPanel);
    this.pack();
    this.setMinimumSize(new Dimension(350, this.getHeight()));
    if (owner != null) {
      this.setLocationRelativeTo(owner);
    } else {
      this.setLocationRelativeTo(null);
    }
    this.setResizable(false);
  }

  private void updateStaggerVisibility() {
    String selected = (String) this.orientationCombo.getSelectedItem();
    boolean showStagger = "Staggered".equals(selected) || "Hexagonal".equals(selected);
    boolean showHexSide = "Hexagonal".equals(selected);
    this.staggerPanel.setVisible(showStagger);
    this.hexSideLengthLabel.setVisible(showHexSide);
    this.hexSideLengthSpinner.setVisible(showHexSide);
    this.pack();
  }

  public String getName() {
    return this.nameTextField.getText();
  }

  public IMapOrientation getOrientation() {
    String selected = (String) this.orientationCombo.getSelectedItem();
    if (selected == null) {
      return MapOrientations.ORTHOGONAL;
    }
    return switch (selected) {
      case "Isometric" -> MapOrientations.ISOMETRIC;
      case "Staggered" -> MapOrientations.ISOMETRIC_STAGGERED;
      case "Hexagonal" -> MapOrientations.HEXAGONAL;
      default -> MapOrientations.ORTHOGONAL;
    };
  }

  public StaggerAxis getStaggerAxis() {
    String selected = (String) this.staggerAxisCombo.getSelectedItem();
    return "X".equals(selected) ? StaggerAxis.X : StaggerAxis.Y;
  }

  public StaggerIndex getStaggerIndex() {
    String selected = (String) this.staggerIndexCombo.getSelectedItem();
    return "Even".equals(selected) ? StaggerIndex.EVEN : StaggerIndex.ODD;
  }

  public int getHexSideLength() {
    return (Integer) this.hexSideLengthSpinner.getValue();
  }

  public int getMapWidth() {
    return (Integer) this.widthSpinner.getValue();
  }

  public int getMapHeight() {
    return (Integer) this.heightSpinner.getValue();
  }

  public int getTileWidth() {
    return (Integer) this.tileWidthSpinner.getValue();
  }

  public int getTileHeight() {
    return (Integer) this.tileHeightSpinner.getValue();
  }

  public boolean isConfirmed() {
    return this.confirmed;
  }
}

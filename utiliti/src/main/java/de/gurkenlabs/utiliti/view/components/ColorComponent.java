package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class ColorComponent extends JPanel {
  private final JButton btnColorSwatch;
  private final JToggleButton btnSelectColor;
  private final JButton btnClearColor;
  private final JTextField textFieldColor;
  private final JSlider sliderAlpha;
  private final JSpinner spinnerAlpha;
  private final Color clearColor;
  private JWindow colorPickerWindow;
  private boolean updatingControls;
  private boolean separateAlphaField;
  private Runnable clearAction;

  private final transient List<ActionListener> listeners;

  public ColorComponent() {
    this(Color.WHITE);
  }

  public ColorComponent(Color clearColor) {
    this(clearColor, null);
  }

  public ColorComponent(Color clearColor, String labelKey) {
    this.clearColor = clearColor;
    int height = (PropertyPanel.CONTROL_HEIGHT + PropertyPanel.CONTROL_MARGIN) * 2;
    int width = PropertyPanel.CONTROL_WIDTH
        + (labelKey != null ? PropertyPanel.LABEL_WIDTH + PropertyPanel.GUTTER_WIDTH : 0);
    this.setOpaque(false);
    this.setSize(width, height);
    this.setPreferredSize(new Dimension(width, height));
    this.listeners = new java.util.concurrent.CopyOnWriteArrayList<>();
    this.textFieldColor = ControlBehavior.apply(new JTextField());
    this.textFieldColor.setBackground(Style.raisedSurface());
    this.textFieldColor.setForeground(Style.text());
    this.textFieldColor.setCaretColor(Style.accent());
    this.textFieldColor.setEditable(true);
    this.textFieldColor.setColumns(9);
    this.textFieldColor.addActionListener(a -> this.setColor(ColorHelper.decode(this.textFieldColor.getText())));

    this.btnColorSwatch = new ColorSwatchButton();
    styleColorActionButton(this.btnColorSwatch);
    this.btnColorSwatch.setFocusable(false);

    this.btnSelectColor = Style.iconToggleButton(Icons.EYEDROPPER_16, false);
    this.btnSelectColor.setToolTipText(Resources.strings().get("colorComponent_selectColor"));
    this.btnSelectColor.getAccessibleContext().setAccessibleName(Resources.strings().get("colorComponent_selectColor"));
    this.btnSelectColor.addActionListener(a -> {
      if (this.btnSelectColor.isSelected()) {
        this.startColorPicker();
      } else {
        this.stopColorPicker();
      }
    });
    this.addHierarchyListener(event -> {
      if (!this.isShowing()) {
        this.stopColorPicker();
      }
    });

    this.btnClearColor = Style.iconButton(Icons.DELETE_16);
    Style.styleButton(this.btnClearColor, Style.ButtonVariant.DESTRUCTIVE);
    this.btnClearColor.setToolTipText(Resources.strings().get("colorComponent_clearColor"));
    this.btnClearColor.getAccessibleContext().setAccessibleName(Resources.strings().get("colorComponent_clearColor"));
    this.btnClearColor.addActionListener(a -> {
      if (this.clearAction != null) {
        this.clearAction.run();
      } else {
        this.clear();
      }
    });

    final JLabel lblAlpha = new JLabel(Resources.strings().get("panel_alpha"));
    final JLabel lblColor = labelKey != null ? new JLabel(Resources.strings().get(labelKey)) : null;
    if (lblColor != null) {
      lblColor.setHorizontalAlignment(SwingConstants.TRAILING);
      lblColor.setVerticalAlignment(SwingConstants.CENTER);
      lblAlpha.setHorizontalAlignment(SwingConstants.TRAILING);
      lblAlpha.setVerticalAlignment(SwingConstants.CENTER);
    }

    this.sliderAlpha = new JSlider(0, 100, 0);
    this.sliderAlpha.setOpaque(false);
    this.sliderAlpha.setToolTipText(Resources.strings().get("panel_alpha"));
    this.sliderAlpha.getAccessibleContext().setAccessibleName(Resources.strings().get("panel_alpha"));
    this.sliderAlpha.addChangeListener(a -> this.setAlphaPercentage(this.sliderAlpha.getValue()));

    this.spinnerAlpha = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    this.spinnerAlpha.setEditor(new JSpinner.NumberEditor(this.spinnerAlpha, "0'%'"));
    this.spinnerAlpha.addChangeListener(a -> this.setAlphaPercentage((int) this.spinnerAlpha.getValue()));
    ControlBehavior.apply(this.spinnerAlpha);
    this.spinnerAlpha.getAccessibleContext().setAccessibleName(Resources.strings().get("panel_alpha"));

    GroupLayout groupLayout = new GroupLayout(this);
    GroupLayout.SequentialGroup colorRow = groupLayout.createSequentialGroup();
    GroupLayout.SequentialGroup alphaRow = groupLayout.createSequentialGroup();
    if (lblColor != null) {
      colorRow.addComponent(
              lblColor, PropertyPanel.LABEL_WIDTH, PropertyPanel.LABEL_WIDTH, PropertyPanel.LABEL_WIDTH)
          .addGap(PropertyPanel.GUTTER_WIDTH);
      alphaRow.addComponent(
              lblAlpha, PropertyPanel.LABEL_WIDTH, PropertyPanel.LABEL_WIDTH, PropertyPanel.LABEL_WIDTH)
          .addGap(PropertyPanel.GUTTER_WIDTH);
    } else {
      alphaRow.addComponent(lblAlpha).addPreferredGap(ComponentPlacement.RELATED);
    }
    colorRow
        .addComponent(
            btnColorSwatch,
            PropertyPanel.CONTROL_HEIGHT,
            GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(textFieldColor, GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(
            btnSelectColor,
            PropertyPanel.CONTROL_HEIGHT,
            GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(
            btnClearColor,
            PropertyPanel.CONTROL_HEIGHT,
            GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE);
    alphaRow
        .addComponent(sliderAlpha, GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(
            spinnerAlpha,
            PropertyPanel.SPINNER_WIDTH,
            PropertyPanel.SPINNER_WIDTH,
            PropertyPanel.SPINNER_WIDTH);
    groupLayout.setHorizontalGroup(
        groupLayout
            .createParallelGroup(Alignment.LEADING)
            .addGroup(colorRow)
            .addGroup(alphaRow));
    GroupLayout.ParallelGroup colorComponents = groupLayout
        .createParallelGroup(Alignment.BASELINE)
        .addComponent(
            btnSelectColor,
            PropertyPanel.CONTROL_HEIGHT,
            GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(
            btnColorSwatch,
            PropertyPanel.CONTROL_HEIGHT,
            GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(
            btnClearColor,
            PropertyPanel.CONTROL_HEIGHT,
            GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(
            textFieldColor,
            GroupLayout.PREFERRED_SIZE,
            PropertyPanel.CONTROL_HEIGHT,
            GroupLayout.PREFERRED_SIZE);
    if (lblColor != null) {
      colorComponents.addComponent(lblColor);
    }
    groupLayout.setVerticalGroup(
        groupLayout
            .createParallelGroup(Alignment.LEADING)
            .addGroup(
                groupLayout
                    .createSequentialGroup()
                    .addGroup(colorComponents)
                    .addGap(PropertyPanel.CONTROL_MARGIN)
                    .addGroup(
                        groupLayout
                            .createParallelGroup(Alignment.CENTER)
                            .addComponent(lblAlpha)
                            .addComponent(sliderAlpha)
                            .addComponent(
                                spinnerAlpha,
                                PropertyPanel.CONTROL_HEIGHT,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))));
    setLayout(groupLayout);
  }

  private static void styleColorActionButton(JButton button) {
    button.setOpaque(false);
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setFocusable(true);
    button.setBorder(new RoundedBorder(Style.border(), Style.CORNER_RADIUS, 4));
  }

  private void startColorPicker() {
    if (GraphicsEnvironment.isHeadless()
        || !GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT)) {
      this.btnSelectColor.setSelected(false);
      Toolkit.getDefaultToolkit().beep();
      return;
    }

    final Robot robot;
    try {
      robot = new Robot();
    } catch (AWTException | SecurityException exception) {
      this.btnSelectColor.setSelected(false);
      Toolkit.getDefaultToolkit().beep();
      return;
    }

    Rectangle bounds = screenBounds();
    Window owner = SwingUtilities.getWindowAncestor(this);
    JWindow picker = new JWindow(owner);
    this.colorPickerWindow = picker;
    picker.setAlwaysOnTop(true);
    picker.setBackground(new Color(0, 0, 0, 1));
    picker.getContentPane().setBackground(new Color(0, 0, 0, 1));
    picker.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    picker.setBounds(bounds);
    picker.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent event) {
        if (event.getButton() != MouseEvent.BUTTON1) {
          cancelColorPicker();
          return;
        }

        Point location = event.getLocationOnScreen();
        picker.setVisible(false);
        Timer sampleDelay = new Timer(
            40, action -> applyPickedColor(robot.getPixelColor(location.x, location.y)));
        sampleDelay.setRepeats(false);
        sampleDelay.start();
      }
    });
    installCancelAction(picker.getRootPane());
    picker.setVisible(true);
    picker.requestFocusInWindow();
  }

  private void applyPickedColor(Color pickedColor) {
    Color current = this.getColor();
    int alpha = current != null ? current.getAlpha() : 255;
    this.stopColorPicker();
    this.setColor(new Color(pickedColor.getRed(), pickedColor.getGreen(), pickedColor.getBlue(), alpha));
  }

  private void cancelColorPicker() {
    this.stopColorPicker();
  }

  private void stopColorPicker() {
    if (this.colorPickerWindow != null) {
      this.colorPickerWindow.dispose();
      this.colorPickerWindow = null;
    }
    this.btnSelectColor.setSelected(false);
  }

  private void installCancelAction(JRootPane rootPane) {
    InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = rootPane.getActionMap();
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelColorPicker");
    actionMap.put("cancelColorPicker", new AbstractAction() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent event) {
        cancelColorPicker();
      }
    });
  }

  private static Rectangle screenBounds() {
    Rectangle bounds = new Rectangle();
    for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
      for (GraphicsConfiguration configuration : device.getConfigurations()) {
        bounds = bounds.union(configuration.getBounds());
      }
    }
    return bounds;
  }

  private void setAlphaPercentage(int percentage) {
    if (this.updatingControls) {
      return;
    }

    Color oldColor = this.getColor();
    if (oldColor == null) {
      return;
    }

    int alpha = Math.round(percentage * 255 / 100f);
    this.setColor(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), alpha));
  }

  private static final class ColorSwatchButton extends JButton {
    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int checkerSize = Math.max(3, getHeight() / 5);
        for (int y = 2; y < getHeight() - 2; y += checkerSize) {
          for (int x = 2; x < getWidth() - 2; x += checkerSize) {
            boolean alternate =
                ((x - 2) / checkerSize + (y - 2) / checkerSize) % 2 == 0;
            g2.setColor(alternate ? Style.raisedSurface() : Style.border());
            g2.fillRect(x, y, checkerSize, checkerSize);
          }
        }
        g2.setColor(getBackground() != null ? getBackground() : Style.surface());
        g2.fillRoundRect(
            2,
            2,
            getWidth() - 4,
            getHeight() - 4,
            Style.CORNER_RADIUS,
            Style.CORNER_RADIUS);
        g2.setColor(isFocusOwner() ? Style.accent() : Style.border());
        g2.setStroke(new java.awt.BasicStroke(isFocusOwner() ? 2f : 1f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Style.CORNER_RADIUS, Style.CORNER_RADIUS);
      } finally {
        g2.dispose();
      }
      super.paintComponent(g);
    }
  }

  public void addActionListener(ActionListener listener) {
    this.listeners.add(listener);
  }

  public void removeActionListener(ActionListener listener) {
    this.listeners.remove(listener);
  }

  public void setClearAction(Runnable clearAction) {
    this.clearAction = clearAction;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    for (java.awt.Component component : getComponents()) {
      component.setEnabled(enabled);
    }
  }

  public int getAlpha() {
    Color color = this.getColor();
    return color != null ? color.getAlpha() : Math.round(this.getAlphaPercentage() * 255 / 100f);
  }

  public int getAlphaPercentage() {
    return (int) this.spinnerAlpha.getValue();
  }

  public String getHexColor() {
    return this.textFieldColor.getText();
  }

  public Color getColor() {
    Color color = ColorHelper.decode(this.textFieldColor.getText());
    if (!this.separateAlphaField || color == null) {
      return color;
    }
    return new Color(color.getRed(), color.getGreen(), color.getBlue(),
        Math.round(this.getAlphaPercentage() * 255 / 100f));
  }

  public void setSeparateAlphaField(boolean separateAlphaField) {
    this.separateAlphaField = separateAlphaField;
    Color color = this.getColor();
    if (color != null) {
      this.setColor(color);
    }
  }

  public void setColor(Color color) {
    if (color == null) {
      return;
    }
    this.updatingControls = true;
    try {
      this.textFieldColor.setText(this.separateAlphaField
          ? String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue())
          : ColorHelper.encode(color));
      this.textFieldColor.setBackground(Style.raisedSurface());
      this.textFieldColor.setForeground(Style.text());
      this.btnColorSwatch.setBackground(color);
      int percentage = Math.round(color.getAlpha() * 100 / 255f);
      this.sliderAlpha.setValue(percentage);
      this.spinnerAlpha.setValue(percentage);
    } finally {
      this.updatingControls = false;
    }
    for (ActionListener listener : this.listeners) {
      listener.actionPerformed(null);
    }
  }

  public void clear() {
    this.setColor(this.clearColor);
  }
}

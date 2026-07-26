package de.gurkenlabs.utiliti.view.dialogs;

import com.github.weisj.darklaf.ui.text.DarkTextUI;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.UriUtilities;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.KeyBindings;
import de.gurkenlabs.utiliti.model.KeyBindings.Command;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.model.Style.Theme;
import de.gurkenlabs.utiliti.model.UserPreferences;
import de.gurkenlabs.utiliti.view.components.RoundedSearchBox;
import de.gurkenlabs.utiliti.view.components.UI;
import de.gurkenlabs.utiliti.view.renderers.GridRenderer;
import de.gurkenlabs.utiliti.view.renderers.Renderers;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.AbstractCellEditor;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

/** Application settings for utiLITI. */
public final class SettingsDialog extends JDialog {
  private static final int DIALOG_WIDTH = 1280;
  private static final int DIALOG_HEIGHT = 800;

  private final UserPreferences preferences = Editor.preferences();
  private final JComboBox<LocaleOption> language;
  private final JToggleButton lightTheme;
  private final JToggleButton darkTheme;
  private final JSpinner uiScale;
  private final JSlider uiScaleSlider;
  private final JComboBox<String> editorFontFamily;
  private final JSpinner editorFontSize;
  private final JLabel editorFontPreview;
  private JPanel uiPreview;
  private final JCheckBox reopenLastProject;
  private final JSpinner editorFpsCap;
  private final JSpinner gridLineWidth;
  private final JSpinner snapDivision;
  private final JButton gridColorButton;
  private Color gridColor;
  private final KeymapTableModel keymapModel;
  private final JLabel restartNotice;

  private SettingsDialog(Window owner) {
    super(owner, text("settings_title"), ModalityType.APPLICATION_MODAL);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    try {
      BufferedImage icon = ImageIO.read(SettingsDialog.class.getResource("/litiengine-icon.png"));
      this.setIconImage(icon);
    } catch (IOException | IllegalArgumentException ignored) {
      // The dialog remains usable if the packaged application icon is unavailable.
    }

    this.language = new JComboBox<>(LocaleOption.values());
    this.language.setSelectedItem(selectedLocale());
    this.lightTheme = themeButton(text("menu_view_theme_light"), Icons.SETTINGS_THEME_LIGHT_16);
    this.darkTheme = themeButton(text("menu_view_theme_dark"), Icons.SETTINGS_THEME_DARK_16);
    this.lightTheme.getAccessibleContext().setAccessibleDescription(text("settings_theme_description"));
    this.darkTheme.getAccessibleContext().setAccessibleDescription(text("settings_theme_description"));
    ButtonGroup themeGroup = new ButtonGroup();
    themeGroup.add(this.lightTheme);
    themeGroup.add(this.darkTheme);
    (this.preferences.getTheme() == Theme.LIGHT ? this.lightTheme : this.darkTheme).setSelected(true);
    this.uiScale = new JSpinner(new SpinnerNumberModel(
        Math.round(this.preferences.getUiScale() * 100),
        Math.round(UserPreferences.UI_SCALE_MIN * 100),
        Math.round(UserPreferences.UI_SCALE_MAX * 100), 10));
    ControlBehavior.apply(this.uiScale);
    this.uiScaleSlider = new JSlider(
        Math.round(UserPreferences.UI_SCALE_MIN * 100),
        Math.round(UserPreferences.UI_SCALE_MAX * 100),
        Math.round(this.preferences.getUiScale() * 100));
    this.uiScaleSlider.setMajorTickSpacing(25);
    this.uiScaleSlider.setPaintTicks(true);
    this.uiScaleSlider.setPaintLabels(true);
    this.uiScaleSlider.addChangeListener(event -> this.uiScale.setValue(this.uiScaleSlider.getValue()));
    this.uiScale.addChangeListener(event -> this.uiScaleSlider.setValue(((Number) this.uiScale.getValue()).intValue()));
    this.uiScale.getAccessibleContext().setAccessibleName(text("settings_ui_scale"));
    this.uiScale.getAccessibleContext().setAccessibleDescription(text("settings_ui_scale_description"));
    this.uiScaleSlider.getAccessibleContext().setAccessibleName(text("settings_ui_scale"));
    this.uiScaleSlider.getAccessibleContext().setAccessibleDescription(text("settings_ui_scale_description"));
    this.editorFontFamily = new JComboBox<>(availableFontFamilies());
    this.editorFontFamily.setSelectedItem(this.preferences.getEditorFontFamily());
    this.editorFontFamily.setRenderer(new FontFamilyRenderer());
    this.editorFontSize = new JSpinner(new SpinnerNumberModel(
        this.preferences.getEditorFontSize(),
        UserPreferences.EDITOR_FONT_SIZE_MIN,
        UserPreferences.EDITOR_FONT_SIZE_MAX, 1));
    ControlBehavior.apply(this.editorFontSize);
    this.editorFontPreview = new JLabel(text("settings_editor_font_preview"));
    this.editorFontPreview.setForeground(Style.mutedText());
    this.editorFontFamily.addActionListener(event -> this.updateFontPreview());
    this.editorFontSize.addChangeListener(event -> this.updateFontPreview());
    this.updateFontPreview();
    this.editorFontFamily.getAccessibleContext().setAccessibleName(text("settings_font_family"));
    this.editorFontFamily.getAccessibleContext().setAccessibleDescription(text("settings_editor_font_description"));
    this.editorFontSize.getAccessibleContext().setAccessibleName(text("settings_font_size"));
    this.editorFontSize.getAccessibleContext().setAccessibleDescription(text("settings_editor_font_description"));
    this.reopenLastProject = new JCheckBox(
        text("settings_reopen_last_project"), this.preferences.reopenLastProject());
    this.editorFpsCap = new JSpinner(new SpinnerNumberModel(
        this.preferences.getEditorFpsCap(),
        UserPreferences.EDITOR_FPS_CAP_MIN,
        UserPreferences.EDITOR_FPS_CAP_MAX, 1));
    ControlBehavior.apply(this.editorFpsCap);
    this.gridLineWidth = new JSpinner(new SpinnerNumberModel(
        (double) this.preferences.getGridLineWidth(), 1.0, 5.0, 0.1));
    ControlBehavior.apply(this.gridLineWidth);
    this.snapDivision = new JSpinner(new SpinnerNumberModel(
        this.preferences.getSnapDivision(), 1, 10, 1));
    ControlBehavior.apply(this.snapDivision);
    this.gridColor = this.preferences.getGridColor();
    this.gridColorButton = new JButton(Icons.COLOR_16);
    this.updateGridColorButton();
    this.gridColorButton.addActionListener(event -> {
      Color selected = JColorChooser.showDialog(this, text("panel_selectGridColor"), this.gridColor);
      if (selected != null) {
        this.gridColor = selected;
        this.updateGridColorButton();
      }
    });

    this.keymapModel = new KeymapTableModel(KeyBindings.snapshot());
    this.restartNotice = new JLabel(text("settings_restart_notice"));
    this.restartNotice.setForeground(Style.COLOR_ORANGE);
    this.restartNotice.setVisible(false);

    this.setContentPane(this.createContent());
    this.getRootPane().registerKeyboardAction(
        event -> this.dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW);
    Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    int width = Math.min(DIALOG_WIDTH, screen.width);
    int height = Math.min(DIALOG_HEIGHT, screen.height);
    this.setMinimumSize(new Dimension(Math.min(900, width), Math.min(620, height)));
    this.setSize(width, height);
    this.addWindowListener(new WindowAdapter() {
      @Override public void windowClosed(WindowEvent event) {
        Point location = getLocation();
        preferences.setSettingsDialogX(location.x);
        preferences.setSettingsDialogY(location.y);
        Game.config().save();
      }
    });
  }

  public static void show(Component owner) {
    Window window = owner == null ? null : SwingUtilities.getWindowAncestor(owner);
    SettingsDialog dialog = new SettingsDialog(window);
    int x = dialog.preferences.getSettingsDialogX();
    int y = dialog.preferences.getSettingsDialogY();
    if (isVisibleOnScreen(x, y, dialog.getWidth(), dialog.getHeight())) {
      dialog.setLocation(x, y);
    } else {
      dialog.setLocationRelativeTo(owner);
    }
    dialog.setVisible(true);
  }

  static boolean isVisibleOnScreen(int x, int y, int width, int height) {
    if (x == Integer.MIN_VALUE || y == Integer.MIN_VALUE) {
      return false;
    }
    Rectangle dialog = new Rectangle(x, y, width, height);
    for (java.awt.GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
      if (device.getDefaultConfiguration().getBounds().intersects(dialog)) {
        return true;
      }
    }
    return false;
  }

  private JPanel createContent() {
    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(Style.background());
    root.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));

    CardLayout cards = new CardLayout();
    JPanel content = new JPanel(cards);
    content.setOpaque(false);
    content.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 0));
    content.add(scrollable(this.createAppearancePanel()), Category.APPEARANCE.name());
    content.add(scrollable(this.createGeneralPanel()), Category.GENERAL.name());
    content.add(scrollable(this.createGridPanel()), Category.GRID.name());
    content.add(scrollable(this.createKeymapPanel()), Category.KEYMAP.name());

    DefaultListModel<Category> categoryModel = new DefaultListModel<>();
    for (Category category : Category.values()) {
      categoryModel.addElement(category);
    }
    JList<Category> categoryList = new JList<>(categoryModel);
    categoryList.setOpaque(false);
    categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    categoryList.setFixedCellHeight(68);
    categoryList.setCellRenderer(new CategoryRenderer());
    categoryList.addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting() && categoryList.getSelectedValue() != null) {
        cards.show(content, categoryList.getSelectedValue().name());
      }
    });

    JTextField search = new JTextField() {
      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false);
        putClientProperty("JComponent.outline", "none");
      }

      @Override protected void paintBorder(Graphics graphics) {
        // The parent search box owns the only visible border.
      }
    };
    String searchPlaceholder = text("settings_search");
    search.putClientProperty(DarkTextUI.KEY_DEFAULT_TEXT, searchPlaceholder);
    search.setToolTipText(searchPlaceholder);
    search.getAccessibleContext().setAccessibleName(searchPlaceholder);
    search.setBorder(BorderFactory.createEmptyBorder());
    search.setOpaque(false);
    search.putClientProperty("JComponent.outline", "none");
    search.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent event) { filter(); }
      @Override public void removeUpdate(DocumentEvent event) { filter(); }
      @Override public void changedUpdate(DocumentEvent event) { filter(); }

      private void filter() {
        String query = search.getText().strip().toLowerCase(Locale.ROOT);
        categoryModel.clear();
        for (Category category : Category.values()) {
          if (category.searchText().contains(query)) {
            categoryModel.addElement(category);
          }
        }
        if (!categoryModel.isEmpty()) {
          categoryList.setSelectedIndex(0);
        }
      }
    });
    RoundedSearchBox searchBox = new RoundedSearchBox(search, 0);
    searchBox.getClearButton().addActionListener(event -> search.setText(""));

    JPanel navigation = new JPanel(new BorderLayout(0, 10));
    navigation.setOpaque(false);
    navigation.setPreferredSize(new Dimension(300, 0));
    navigation.setBackground(Style.background());
    navigation.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Style.border()));
    navigation.add(searchBox, BorderLayout.NORTH);
    JScrollPane categoryScroll = new JScrollPane(categoryList);
    categoryScroll.setOpaque(false);
    categoryScroll.getViewport().setOpaque(false);
    categoryScroll.setBorder(null);
    categoryScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    navigation.add(categoryScroll, BorderLayout.CENTER);
    navigation.add(this.createSupportCard(), BorderLayout.SOUTH);

    root.add(navigation, BorderLayout.WEST);
    root.add(content, BorderLayout.CENTER);
    root.add(this.createFooter(), BorderLayout.SOUTH);
    root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "focusSearch");
    root.getActionMap().put("focusSearch", new javax.swing.AbstractAction() {
      @Override public void actionPerformed(java.awt.event.ActionEvent event) {
        search.requestFocusInWindow();
        search.selectAll();
      }
    });
    categoryList.setSelectedValue(Category.APPEARANCE, true);
    return root;
  }

  private JPanel createSupportCard() {
    JPanel card = new RoundedSurfacePanel(new BorderLayout(12, 0));
    card.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 14));
    card.add(new JLabel(Icons.SUPPORT_32), BorderLayout.WEST);

    JPanel copy = new JPanel();
    copy.setOpaque(false);
    copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
    JButton support = new JButton(text("support_the_devs"), Icons.EXTERNAL_16);
    support.setHorizontalTextPosition(SwingConstants.LEFT);
    support.setHorizontalAlignment(SwingConstants.LEFT);
    support.setIconTextGap(8);
    support.setContentAreaFilled(false);
    support.setBorder(BorderFactory.createEmptyBorder());
    support.setFocusPainted(false);
    support.setFont(support.getFont().deriveFont(Font.BOLD));
    support.setAlignmentX(Component.LEFT_ALIGNMENT);
    support.setMaximumSize(support.getPreferredSize());
    support.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    support.addActionListener(event -> UriUtilities.openWebpage(
        URI.create(Resources.strings().getFrom("links", "link_opencollective"))));
    JLabel description = new JLabel(text("settings_support_description"));
    description.setForeground(Style.mutedText());
    description.setAlignmentX(Component.LEFT_ALIGNMENT);
    copy.add(support);
    copy.add(Box.createRigidArea(new Dimension(0, 4)));
    copy.add(description);
    card.add(copy, BorderLayout.CENTER);
    card.setPreferredSize(new Dimension(0, 82));
    return card;
  }

  private JPanel createGeneralPanel() {
    JPanel panel = settingsPanel(Category.GENERAL);
    JPanel body = verticalBody();
    body.add(settingRow(
        Icons.HISTORY_16,
        text("settings_reopen_last_project"),
        text("settings_reopen_last_project_description"),
        this.reopenLastProject));
    body.add(rowSeparator());
    body.add(settingRow(
        Icons.SETTINGS_DISPLAY_24,
        text("settings_editor_fps_cap"),
        text("settings_editor_fps_cap_description"),
        this.editorFpsCap));
    panel.add(topAligned(body), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createAppearancePanel() {
    JPanel panel = new JPanel(new BorderLayout(0, 22));
    panel.setOpaque(false);
    JPanel top = new JPanel(new GridLayout(1, 2, 28, 0));
    top.setOpaque(false);
    top.add(settingsHeader(Category.APPEARANCE));
    top.add(this.createPreview());
    panel.add(top, BorderLayout.NORTH);
    JPanel body = verticalBody();
    this.language.setPreferredSize(new Dimension(300, 34));
    body.add(settingRow(
        Icons.SETTINGS_LANGUAGE_24,
        text("settings_language"),
        text("settings_language_description"),
        this.language));
    body.add(rowSeparator());

    JPanel themes = new JPanel(new GridLayout(1, 2, 10, 0));
    themes.setOpaque(false);
    themes.setPreferredSize(new Dimension(300, 64));
    themes.add(this.lightTheme);
    themes.add(this.darkTheme);
    body.add(settingRow(
        Icons.SETTINGS_THEME_LIGHT_24,
        text("menu_view_theme"),
        text("settings_theme_description"),
        themes));
    body.add(rowSeparator());

    JPanel scale = new JPanel(new BorderLayout(10, 4));
    scale.setOpaque(false);
    JPanel scaleValue = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
    scaleValue.setOpaque(false);
    this.uiScale.setPreferredSize(new Dimension(84, 32));
    scaleValue.add(this.uiScale);
    scaleValue.add(new JLabel("%"));
    scale.add(scaleValue, BorderLayout.NORTH);
    scale.add(this.uiScaleSlider, BorderLayout.CENTER);
    JLabel hint = new JLabel(text("settings_scale_hint"), Icons.SETTINGS_INFO_16, SwingConstants.LEFT);
    hint.setForeground(Style.COLOR_ACCENT_BLUE);
    scale.add(hint, BorderLayout.SOUTH);
    scale.setPreferredSize(new Dimension(430, 100));
    body.add(settingRow(
        Icons.SETTINGS_DISPLAY_24,
        text("settings_ui_scale"),
        text("settings_ui_scale_description"),
        scale));
    body.add(rowSeparator());

    this.editorFontFamily.setPreferredSize(new Dimension(280, 34));
    this.editorFontSize.setPreferredSize(new Dimension(76, 34));
    JPanel fontInputs = new JPanel(new BorderLayout(10, 0));
    fontInputs.setOpaque(false);
    fontInputs.add(this.editorFontFamily, BorderLayout.CENTER);
    fontInputs.add(this.editorFontSize, BorderLayout.EAST);
    JPanel fontControl = new JPanel(new BorderLayout(0, 8));
    fontControl.setOpaque(false);
    fontControl.add(fontInputs, BorderLayout.NORTH);
    fontControl.add(this.editorFontPreview, BorderLayout.CENTER);
    fontControl.setPreferredSize(new Dimension(370, 70));
    body.add(settingRow(
        Icons.SETTINGS_FONT_24,
        text("settings_editor_font"),
        text("settings_editor_font_description"),
        fontControl));
    panel.add(topAligned(body), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createGridPanel() {
    JPanel panel = settingsPanel(Category.GRID);
    JPanel body = verticalBody();
    body.add(settingRow(
        Icons.PENCIL_16,
        text("menu_view_gridStroke"),
        text("settings_grid_stroke_description"),
        this.gridLineWidth));
    body.add(rowSeparator());
    body.add(settingRow(
        Icons.COLOR_16,
        text("menu_view_gridColor"),
        text("settings_grid_color_description"),
        this.gridColorButton));
    body.add(rowSeparator());
    body.add(settingRow(
        Icons.FIT_16,
        text("menu_view_snapDivision"),
        text("settings_snap_division_description"),
        this.snapDivision));
    panel.add(topAligned(body), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createKeymapPanel() {
    JPanel panel = settingsPanel(Category.KEYMAP);
    JTable table = new JTable(this.keymapModel);
    table.putClientProperty("terminateEditOnFocusLost", true);
    table.setRowHeight(30);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getColumnModel().getColumn(0).setPreferredWidth(320);
    table.getColumnModel().getColumn(1).setPreferredWidth(180);
    table.getColumnModel().getColumn(1).setCellEditor(new ShortcutEditor());

    JPanel commands = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    JButton resetSelected = new JButton(text("settings_reset_selected"));
    resetSelected.addActionListener(event -> {
      int row = table.getSelectedRow();
      if (row >= 0) {
        this.keymapModel.reset(table.convertRowIndexToModel(row));
      }
    });
    JButton resetAll = new JButton(text("settings_reset_all"));
    resetAll.addActionListener(event -> this.keymapModel.resetAll());
    commands.add(resetSelected);
    commands.add(resetAll);

    JPanel tablePanel = new JPanel(new BorderLayout());
    tablePanel.setOpaque(false);
    tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
    tablePanel.add(commands, BorderLayout.SOUTH);
    panel.add(tablePanel, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createFooter() {
    JPanel footer = new JPanel(new BorderLayout());
    footer.setOpaque(false);
    footer.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()),
        BorderFactory.createEmptyBorder(16, 8, 0, 4)));
    JPanel leading = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    leading.setOpaque(false);
    JButton reset = new JButton(text("settings_reset_defaults"), Icons.HISTORY_16);
    reset.addActionListener(event -> this.resetSettings());
    leading.add(reset);
    leading.add(this.restartNotice);
    footer.add(leading, BorderLayout.WEST);

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    buttons.setOpaque(false);
    JButton ok = new JButton(text("settings_ok"));
    ok.addActionListener(event -> {
      if (this.applySettings()) {
        this.dispose();
      }
    });
    JButton cancel = new JButton(text("settings_cancel"));
    cancel.addActionListener(event -> this.dispose());
    JButton apply = new JButton(text("settings_apply"));
    apply.addActionListener(event -> this.applySettings());
    buttons.add(ok);
    buttons.add(cancel);
    buttons.add(apply);
    footer.add(buttons, BorderLayout.EAST);
    this.getRootPane().setDefaultButton(ok);
    return footer;
  }

  private boolean applySettings() {
    String conflict = this.keymapModel.findConflict();
    if (conflict != null) {
      JOptionPane.showMessageDialog(this, text("settings_keymap_conflict", conflict),
          text("settings_keymap_conflict_title"), JOptionPane.WARNING_MESSAGE);
      return false;
    }

    LocaleOption locale = (LocaleOption) this.language.getSelectedItem();
    boolean restartRequired = locale != null
        && (!java.util.Objects.equals(locale.language, this.preferences.getPreferredLanguage())
        || !java.util.Objects.equals(locale.country, this.preferences.getPreferredCountry()));
    float scale = ((Number) this.uiScale.getValue()).floatValue() / 100f;
    restartRequired |= Float.compare(scale, this.preferences.getUiScale()) != 0;
    String fontFamily = (String) this.editorFontFamily.getSelectedItem();
    int fontSize = ((Number) this.editorFontSize.getValue()).intValue();
    restartRequired |= !java.util.Objects.equals(fontFamily, this.preferences.getEditorFontFamily())
        || fontSize != this.preferences.getEditorFontSize();

    if (locale != null) {
      this.preferences.setPreferredLanguage(locale.language);
      this.preferences.setPreferredCountry(locale.country);
    }
    this.preferences.setUiScale(scale);
    this.preferences.setEditorFontFamily(fontFamily);
    this.preferences.setEditorFontSize(fontSize);
    this.preferences.setReopenLastProject(this.reopenLastProject.isSelected());
    int fpsCap = ((Number) this.editorFpsCap.getValue()).intValue();
    this.preferences.setEditorFpsCap(fpsCap);
    Game.config().client().setMaxFps(fpsCap);
    Game.loop().setTickRate(fpsCap);
    this.preferences.setGridLineWidth(((Number) this.gridLineWidth.getValue()).floatValue());
    this.preferences.setGridColor(ColorHelper.encode(this.gridColor));
    this.preferences.setSnapDivision(((Number) this.snapDivision.getValue()).intValue());
    KeyBindings.save(this.keymapModel.bindings());
    Theme selectedTheme = this.lightTheme.isSelected() ? Theme.LIGHT : Theme.DARK;
    if (selectedTheme != null && selectedTheme != this.preferences.getTheme()) {
      UI.setTheme(selectedTheme);
    }
    Renderers.get(GridRenderer.class).clearCache();
    UI.refreshKeyBindings();
    Game.config().save();
    this.restartNotice.setVisible(restartRequired || this.restartNotice.isVisible());
    return true;
  }

  private void updateGridColorButton() {
    this.gridColorButton.setText(String.format(
        "#%02X%02X%02X", this.gridColor.getRed(), this.gridColor.getGreen(), this.gridColor.getBlue()));
  }

  private void updateFontPreview() {
    String family = (String) this.editorFontFamily.getSelectedItem();
    int size = ((Number) this.editorFontSize.getValue()).intValue();
    Font font = previewFont(family, size);
    this.editorFontPreview.setFont(font);
    if (this.uiPreview != null) {
      applyPreviewFont(this.uiPreview, font);
      this.uiPreview.revalidate();
      this.uiPreview.repaint();
    }
  }

  private static void applyPreviewFont(Component component, Font font) {
    if (component.getFont() != null) {
      component.setFont(font.deriveFont(component.getFont().getStyle(), font.getSize2D()));
    }
    if (component instanceof java.awt.Container container) {
      for (Component child : container.getComponents()) {
        applyPreviewFont(child, font);
      }
    }
  }

  private void resetSettings() {
    UserPreferences defaults = new UserPreferences();
    this.language.setSelectedItem(LocaleOption.SYSTEM);
    (defaults.getTheme() == Theme.LIGHT ? this.lightTheme : this.darkTheme).setSelected(true);
    this.uiScale.setValue(Math.round(defaults.getUiScale() * 100));
    this.editorFontFamily.setSelectedItem(defaults.getEditorFontFamily());
    this.editorFontSize.setValue(defaults.getEditorFontSize());
    this.reopenLastProject.setSelected(defaults.reopenLastProject());
    this.editorFpsCap.setValue(defaults.getEditorFpsCap());
    this.gridLineWidth.setValue((double) defaults.getGridLineWidth());
    this.snapDivision.setValue(defaults.getSnapDivision());
    this.gridColor = defaults.getGridColor();
    this.updateGridColorButton();
    this.keymapModel.resetAll();
  }

  private static JPanel settingsPanel(Category category) {
    JPanel panel = new JPanel(new BorderLayout(0, 18));
    panel.setOpaque(false);
    panel.add(settingsHeader(category), BorderLayout.NORTH);
    return panel;
  }

  private static JPanel settingsHeader(Category category) {
    JPanel header = new JPanel(new BorderLayout(14, 0));
    header.setOpaque(false);
    Icon headerIcon = category == Category.APPEARANCE ? Icons.SETTINGS_APPEARANCE_40 : category.icon;
    JLabel icon = new JLabel(headerIcon);
    icon.setHorizontalAlignment(SwingConstants.CENTER);
    icon.setVerticalAlignment(SwingConstants.CENTER);
    icon.setPreferredSize(new Dimension(46, 52));
    header.add(icon, BorderLayout.WEST);
    JPanel copy = new JPanel(new GridBagLayout());
    copy.setOpaque(false);
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    JLabel title = new JLabel(category.toString());
    title.setFont(title.getFont().deriveFont(Font.BOLD, 26f));
    copy.add(title, constraints);
    constraints.gridy = 1;
    constraints.insets = new Insets(6, 0, 0, 0);
    JLabel description = new JLabel(text(category.descriptionKey));
    description.setForeground(Style.mutedText());
    copy.add(description, constraints);
    header.add(copy, BorderLayout.CENTER);
    return header;
  }

  private JPanel createPreview() {
    JPanel preview = new RoundedSurfacePanel(new BorderLayout(0, 10));
    preview.setBorder(BorderFactory.createEmptyBorder(14, 16, 12, 16));
    JPanel heading = new JPanel(new GridBagLayout());
    heading.setOpaque(false);
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    JLabel title = new JLabel(text("settings_preview"));
    title.setFont(title.getFont().deriveFont(Font.BOLD));
    heading.add(title, constraints);
    constraints.gridy = 1;
    JLabel description = new JLabel(text("settings_preview_description"));
    description.setForeground(Style.mutedText());
    heading.add(description, constraints);
    preview.add(heading, BorderLayout.NORTH);

    JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    controls.setOpaque(false);
    JButton primary = new JButton(text("settings_preview_primary"));
    primary.putClientProperty("Editor.buttonVariant", Style.ButtonVariant.PRIMARY);
    controls.add(primary);
    controls.add(new JButton(text("settings_preview_secondary")));
    controls.add(new JCheckBox(text("settings_preview_checkbox"), true));
    JRadioButton option = new JRadioButton(text("settings_preview_option"), true);
    controls.add(option);
    preview.add(controls, BorderLayout.CENTER);
    JSlider previewScale = new JSlider(0, 100, 65);
    previewScale.setFocusable(false);
    preview.add(previewScale, BorderLayout.SOUTH);
    preview.setMaximumSize(new Dimension(Integer.MAX_VALUE, 136));
    this.uiPreview = preview;
    this.updateFontPreview();
    return preview;
  }

  private static JPanel verticalBody() {
    JPanel body = new JPanel();
    body.setOpaque(false);
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    body.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 8));
    return body;
  }

  private static JPanel settingRow(Icon icon, String title, String description, JComponent control) {
    JPanel row = new JPanel(new BorderLayout(16, 0));
    row.setOpaque(false);
    row.setBorder(BorderFactory.createEmptyBorder(14, 4, 14, 4));
    row.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel iconLabel = new JLabel(icon);
    iconLabel.setPreferredSize(new Dimension(28, 28));
    iconLabel.setVerticalAlignment(SwingConstants.TOP);
    row.add(iconLabel, BorderLayout.WEST);

    JPanel copy = new JPanel();
    copy.setOpaque(false);
    copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
    JLabel titleLabel = new JLabel(title);
    titleLabel.setLabelFor(control);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    JLabel descriptionLabel = new JLabel(description);
    descriptionLabel.setForeground(Style.mutedText());
    descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    copy.add(titleLabel);
    copy.add(Box.createRigidArea(new Dimension(0, 4)));
    copy.add(descriptionLabel);
    copy.setPreferredSize(new Dimension(300, 48));
    row.add(copy, BorderLayout.CENTER);
    control.getAccessibleContext().setAccessibleName(title);
    control.getAccessibleContext().setAccessibleDescription(description);
    row.add(control, BorderLayout.EAST);
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Math.max(80, control.getPreferredSize().height + 32)));
    return row;
  }

  private static JScrollPane scrollable(JPanel panel) {
    ScrollablePanel viewport = new ScrollablePanel();
    viewport.add(panel, BorderLayout.CENTER);
    JScrollPane scrollPane = new JScrollPane(viewport);
    scrollPane.setBorder(null);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    return scrollPane;
  }

  private static final class ScrollablePanel extends JPanel implements javax.swing.Scrollable {
    private ScrollablePanel() {
      super(new BorderLayout());
      setOpaque(false);
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
      return Math.max(16, visible.height - 16);
    }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
  }

  private static JSeparator rowSeparator() {
    JSeparator separator = new JSeparator();
    separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
    return separator;
  }

  private static Component sectionGap() {
    return Box.createRigidArea(new Dimension(0, 14));
  }

  private static JToggleButton themeButton(String text, Icon icon) {
    JToggleButton button = new ThemeToggleButton(text, icon);
    button.setHorizontalTextPosition(SwingConstants.CENTER);
    button.setVerticalTextPosition(SwingConstants.BOTTOM);
    button.setIconTextGap(6);
    return button;
  }

  private static final class ThemeToggleButton extends JToggleButton {
    private ThemeToggleButton(String text, Icon icon) {
      super(text, icon);
      this.setOpaque(false);
      this.setContentAreaFilled(false);
      this.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
      this.setFocusPainted(false);
    }

    @Override protected void paintComponent(Graphics graphics) {
      Graphics2D g = (Graphics2D) graphics.create();
      Color accent = Style.accent();
      if (this.isSelected()) {
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 36));
        g.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 10, 10);
      }
      g.setColor(this.isSelected() ? accent : Style.border());
      g.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 10, 10);
      if (this.isFocusOwner()) {
        g.setColor(accent);
        g.setStroke(new java.awt.BasicStroke(2f));
        g.drawRoundRect(2, 2, this.getWidth() - 5, this.getHeight() - 5, 8, 8);
      }
      g.dispose();
      super.paintComponent(graphics);
    }
  }

  private static JPanel formPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 20));
    return panel;
  }

  private static JPanel topAligned(JPanel form) {
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setOpaque(false);
    wrapper.add(form, BorderLayout.NORTH);
    return wrapper;
  }

  private static void addRow(JPanel panel, int row, String label, JComponent control) {
    GridBagConstraints constraints = baseConstraints(row);
    constraints.weightx = 0;
    constraints.insets = new Insets(5, 0, 5, 18);
    panel.add(new JLabel(label + ":"), constraints);
    constraints.gridx = 1;
    constraints.weightx = 1;
    constraints.insets = new Insets(5, 0, 5, 0);
    panel.add(control, constraints);
  }

  private static void addFullRow(JPanel panel, int row, JComponent component) {
    GridBagConstraints constraints = baseConstraints(row);
    constraints.gridwidth = 2;
    constraints.weightx = 1;
    constraints.insets = new Insets(5, 0, 5, 0);
    panel.add(component, constraints);
  }

  private static GridBagConstraints baseConstraints(int row) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = row;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    return constraints;
  }

  private LocaleOption selectedLocale() {
    return LocaleOption.valueOf(resolveLocaleOption(
        this.preferences.getPreferredLanguage(), this.preferences.getPreferredCountry()));
  }

  static String resolveLocaleOption(String language, String country) {
    if (language == null || language.isBlank() || country == null || country.isBlank()) {
      return LocaleOption.SYSTEM.name();
    }
    for (LocaleOption option : LocaleOption.values()) {
      if (java.util.Objects.equals(option.language, language)
          && java.util.Objects.equals(option.country, country)) {
        return option.name();
      }
    }
    return LocaleOption.ENGLISH.name();
  }

  private static String text(String key, Object... arguments) {
    return Resources.strings().get(key, arguments);
  }

  private static String[] availableFontFamilies() {
    String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    if (java.util.Arrays.stream(installed).anyMatch("Roboto"::equals)) {
      return installed;
    }
    String[] families = new String[installed.length + 1];
    families[0] = "Roboto";
    System.arraycopy(installed, 0, families, 1, installed.length);
    return families;
  }

  private static Font previewFont(String family, int size) {
    if (family == null || family.isBlank() || "Roboto".equals(family)) {
      Font roboto = Resources.fonts().get("Roboto-Regular.ttf", Font.PLAIN, size);
      if (roboto != null) {
        return roboto.deriveFont((float) size);
      }
    }
    return new Font(family == null ? Font.SANS_SERIF : family, Font.PLAIN, size);
  }

  private enum Category {
    APPEARANCE("settings_appearance", "settings_appearance_description", "settings_appearance_nav_description", Icons.SETTINGS_APPEARANCE_24),
    GENERAL("settings_general", "settings_general_description", "settings_general_nav_description", Icons.SETTINGS_24),
    GRID("settings_grid", "settings_grid_description", "settings_grid_nav_description", Icons.SETTINGS_GRID_24),
    KEYMAP("settings_keymap", "settings_keymap_description", "settings_keymap_nav_description", Icons.SETTINGS_KEYMAP_24);

    private final String resourceKey;
    private final String descriptionKey;
    private final String navigationDescriptionKey;
    private final Icon icon;

    Category(String resourceKey, String descriptionKey, String navigationDescriptionKey, Icon icon) {
      this.resourceKey = resourceKey;
      this.descriptionKey = descriptionKey;
      this.navigationDescriptionKey = navigationDescriptionKey;
      this.icon = icon;
    }

    @Override public String toString() {
      return text(this.resourceKey);
    }

    private String searchText() {
      StringBuilder value = new StringBuilder()
          .append(this).append(' ')
          .append(text(this.descriptionKey)).append(' ')
          .append(text(this.navigationDescriptionKey));
      switch (this) {
        case APPEARANCE -> value
            .append(' ').append(text("settings_language"))
            .append(' ').append(text("settings_language_description"))
            .append(' ').append(text("menu_view_theme"))
            .append(' ').append(text("settings_theme_description"))
            .append(' ').append(text("settings_ui_scale"))
            .append(' ').append(text("settings_ui_scale_description"));
        case GENERAL -> value
            .append(' ').append(text("settings_reopen_last_project"))
            .append(' ').append(text("settings_reopen_last_project_description"));
        case GRID -> value
            .append(' ').append(text("menu_view_gridStroke"))
            .append(' ').append(text("settings_grid_stroke_description"))
            .append(' ').append(text("menu_view_gridColor"))
            .append(' ').append(text("settings_grid_color_description"))
            .append(' ').append(text("menu_view_snapDivision"))
            .append(' ').append(text("settings_snap_division_description"));
        case KEYMAP -> {
          for (Command command : Command.values()) {
            value.append(' ').append(text(command.resourceKey()));
          }
        }
      }
      return value.toString().toLowerCase(Locale.ROOT);
    }
  }

  private enum LocaleOption {
    SYSTEM(null, null, "settings_language_system_default"),
    ENGLISH("en", "US", "menu_view_language_en_US"),
    GERMAN("de", "DE", "menu_view_language_de_DE"),
    SPANISH("es", "ES", "menu_view_language_es_ES"),
    FRENCH("fr", "FR", "menu_view_language_fr_FR");

    private final String language;
    private final String country;
    private final String resourceKey;

    LocaleOption(String language, String country, String resourceKey) {
      this.language = language;
      this.country = country;
      this.resourceKey = resourceKey;
    }

    @Override public String toString() {
      return text(this.resourceKey);
    }
  }

  private static final class CategoryRenderer implements ListCellRenderer<Category> {
    @Override public Component getListCellRendererComponent(
        JList<? extends Category> list, Category category, int index, boolean selected, boolean focused) {
      JPanel row = new CategoryRow(selected);
      row.setLayout(new BorderLayout(14, 0));
      row.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 12));
      JLabel icon = new JLabel(category.icon);
      icon.setHorizontalAlignment(SwingConstants.CENTER);
      icon.setVerticalAlignment(SwingConstants.TOP);
      icon.setPreferredSize(new Dimension(28, 28));
      row.add(icon, BorderLayout.WEST);
      JPanel copy = new JPanel();
      copy.setOpaque(false);
      copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
      JLabel title = new JLabel(category.toString());
      title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
      title.setAlignmentX(Component.LEFT_ALIGNMENT);
      JLabel description = new JLabel(text(category.navigationDescriptionKey));
      description.setForeground(Style.mutedText());
      description.setAlignmentX(Component.LEFT_ALIGNMENT);
      copy.add(title);
      copy.add(Box.createRigidArea(new Dimension(0, 3)));
      copy.add(description);
      row.add(copy, BorderLayout.CENTER);
      return row;
    }
  }

  private static final class FontFamilyRenderer extends DefaultListCellRenderer {
    @Override public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean selected, boolean focused) {
      JLabel label = (JLabel) super.getListCellRendererComponent(
          list, value, index, selected, focused);
      if (value instanceof String family) {
        label.setFont(previewFont(family, 14));
      }
      return label;
    }
  }

  private static final class CategoryRow extends JPanel {
    private final boolean selected;

    private CategoryRow(boolean selected) {
      this.selected = selected;
      this.setOpaque(false);
    }

    @Override protected void paintComponent(Graphics graphics) {
      if (this.selected) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setColor(new Color(53, 116, 242, 78));
        g.fillRoundRect(4, 3, this.getWidth() - 8, this.getHeight() - 6, 10, 10);
        g.dispose();
      }
      super.paintComponent(graphics);
    }
  }

  private static final class RoundedSurfacePanel extends JPanel {
    private RoundedSurfacePanel(java.awt.LayoutManager layout) {
      super(layout);
      this.setOpaque(false);
    }

    @Override protected void paintComponent(Graphics graphics) {
      Graphics2D g = (Graphics2D) graphics.create();
      g.setColor(Style.surface());
      g.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 12, 12);
      g.setColor(Style.border());
      g.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 12, 12);
      g.dispose();
      super.paintComponent(graphics);
    }
  }

  private static final class KeymapTableModel extends AbstractTableModel {
    private final List<Command> commands = new ArrayList<>(List.of(Command.values()));
    private final EnumMap<Command, KeyStroke> bindings;

    private KeymapTableModel(Map<Command, KeyStroke> bindings) {
      this.bindings = new EnumMap<>(Command.class);
      this.bindings.putAll(bindings);
    }

    @Override public int getRowCount() { return this.commands.size(); }
    @Override public int getColumnCount() { return 2; }
    @Override public String getColumnName(int column) {
      return text(column == 0 ? "settings_action" : "settings_shortcut");
    }
    @Override public Object getValueAt(int row, int column) {
      Command command = this.commands.get(row);
      return column == 0 ? text(command.resourceKey()) : KeyBindings.format(this.bindings.get(command));
    }
    @Override public boolean isCellEditable(int row, int column) { return column == 1; }
    @Override public void setValueAt(Object value, int row, int column) {
      if (column == 1) {
        this.bindings.put(this.commands.get(row), (KeyStroke) value);
        this.fireTableCellUpdated(row, column);
      }
    }

    private void reset(int row) {
      Command command = this.commands.get(row);
      this.bindings.put(command, command.defaultKeyStroke());
      this.fireTableCellUpdated(row, 1);
    }

    private void resetAll() {
      this.bindings.clear();
      this.bindings.putAll(KeyBindings.defaults());
      this.fireTableDataChanged();
    }

    private EnumMap<Command, KeyStroke> bindings() {
      return new EnumMap<>(this.bindings);
    }

    private KeyStroke bindingAt(int row) {
      return this.bindings.get(this.commands.get(row));
    }

    private String findConflict() {
      Map<KeyStroke, Command> used = new HashMap<>();
      for (Command command : this.commands) {
        KeyStroke keyStroke = this.bindings.get(command);
        if (keyStroke == null) {
          continue;
        }
        Command existing = used.putIfAbsent(keyStroke, command);
        if (existing != null) {
          return KeyBindings.format(keyStroke) + " (" + text(existing.resourceKey()) + ", "
              + text(command.resourceKey()) + ")";
        }
      }
      return null;
    }
  }

  private static final class ShortcutEditor extends AbstractCellEditor implements TableCellEditor {
    private final JTextField field = new JTextField();
    private KeyStroke value;

    private ShortcutEditor() {
      this.field.setEditable(false);
      this.field.setHorizontalAlignment(SwingConstants.LEFT);
      this.field.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override public void keyPressed(KeyEvent event) {
          if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            event.consume();
            cancelCellEditing();
            return;
          }
          if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            value = null;
          } else if (!isModifier(event.getKeyCode()) && isSafeShortcut(event)) {
            value = KeyStroke.getKeyStroke(event.getKeyCode(), event.getModifiersEx());
          } else {
            if (!isModifier(event.getKeyCode())) {
              java.awt.Toolkit.getDefaultToolkit().beep();
              event.consume();
            }
            return;
          }
          field.setText(KeyBindings.format(value));
          event.consume();
          stopCellEditing();
        }
      });
    }

    @Override public Object getCellEditorValue() { return this.value; }
    @Override public Component getTableCellEditorComponent(
        JTable table, Object value, boolean selected, int row, int column) {
      this.value = table.getModel() instanceof KeymapTableModel model
          ? model.bindingAt(table.convertRowIndexToModel(row))
          : null;
      this.field.setText(text("settings_press_shortcut"));
      SwingUtilities.invokeLater(this.field::requestFocusInWindow);
      return this.field;
    }

    private static boolean isModifier(int keyCode) {
      return keyCode == KeyEvent.VK_CONTROL || keyCode == KeyEvent.VK_SHIFT
          || keyCode == KeyEvent.VK_ALT || keyCode == KeyEvent.VK_META;
    }

    private static boolean isSafeShortcut(KeyEvent event) {
      if ((event.getModifiersEx() & (KeyEvent.CTRL_DOWN_MASK | KeyEvent.META_DOWN_MASK)) != 0) {
        return true;
      }
      int keyCode = event.getKeyCode();
      if (event.getKeyChar() != KeyEvent.CHAR_UNDEFINED
          && !Character.isISOControl(event.getKeyChar())) {
        return false;
      }
      return keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F24
          || keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT
          || keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN
          || keyCode == KeyEvent.VK_HOME || keyCode == KeyEvent.VK_END
          || keyCode == KeyEvent.VK_PAGE_UP || keyCode == KeyEvent.VK_PAGE_DOWN
          || keyCode == KeyEvent.VK_INSERT || keyCode == KeyEvent.VK_DELETE
          || keyCode == KeyEvent.VK_PRINTSCREEN
          || keyCode == KeyEvent.VK_PAUSE;
    }
  }
}

package de.gurkenlabs.utiliti.view.dialogs;

import com.github.weisj.darklaf.ui.text.DarkTextUI;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.UriUtilities;
import de.gurkenlabs.utiliti.controller.ControlBehavior;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.ProjectLaunchRequest;
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
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
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
import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
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
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
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
import javax.swing.RowFilter;
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
import javax.swing.table.TableRowSorter;

/** Application settings for utiLITI. */
public final class SettingsDialog extends JDialog {
  private static final int DIALOG_WIDTH = 1280;
  private static final int DIALOG_HEIGHT = 800;
  private static final String EMPTY_CARD = "__EMPTY__";

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
  private final JTextField gradleLaunchArguments;
  private final JComboBox<LogLevelOption> logLevel;
  private final JCheckBox mcpEnabled;
  private final JTextField mcpPort;
  private final JSpinner gridLineWidth;
  private final JSpinner snapDivision;
  private final JButton gridColorButton;
  private Color gridColor;
  private final KeymapTableModel keymapModel;
  private final JLabel restartNotice;
  private final CardLayout contentCards = new CardLayout();
  private final JPanel contentPanel = new JPanel(this.contentCards);
  private final DefaultListModel<Category> categoryModel = new DefaultListModel<>();
  private final JList<Category> categoryList = new JList<>(this.categoryModel);
  private final List<SettingItem> settingItems = new ArrayList<>();
  private final Map<Category, Integer> matchCounts = new EnumMap<>(Category.class);
  private String currentSearchQuery = "";
  private TableRowSorter<KeymapTableModel> keymapSorter;
  private JLabel emptySearchDescription;

  private enum LogLevelOption {
    INFO("INFO (Default - Clean User Logs)"),
    FINE("FINE (Detailed Technical Diagnostics)"),
    WARNING("WARNING (Warnings & Errors)"),
    SEVERE("SEVERE (Errors Only)");

    private final String label;
    LogLevelOption(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }

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
    this.gradleLaunchArguments = new JTextField(this.preferences.getGradleLaunchArguments());
    this.gradleLaunchArguments.setColumns(34);
    this.gradleLaunchArguments.setToolTipText(text("settings_gradle_launch_arguments_description"));
    this.gradleLaunchArguments.getAccessibleContext().setAccessibleName(
        text("settings_gradle_launch_arguments"));
    this.gradleLaunchArguments.getAccessibleContext().setAccessibleDescription(
        text("settings_gradle_launch_arguments_description"));
    this.logLevel = new JComboBox<>(LogLevelOption.values());
    try {
      this.logLevel.setSelectedItem(LogLevelOption.valueOf(this.preferences.getLogLevel().toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException e) {
      this.logLevel.setSelectedItem(LogLevelOption.INFO);
    }
    this.mcpEnabled = new JCheckBox(text("settings_mcp_enable"));
    this.mcpEnabled.setSelected(this.preferences.isMcpEnabled());
    ControlBehavior.apply(this.mcpEnabled);
    this.mcpPort = new JTextField(String.valueOf(this.preferences.getMcpPort()));
    this.mcpPort.setColumns(6);
    this.mcpPort.setHorizontalAlignment(JTextField.RIGHT);
    ((AbstractDocument) this.mcpPort.getDocument()).setDocumentFilter(new DocumentFilter() {
      @Override
      public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
          throws BadLocationException {
        if (string != null && isValid(fb.getDocument().getText(0, fb.getDocument().getLength()) + string)) {
          super.insertString(fb, offset, string, attr);
        }
      }

      @Override
      public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
          throws BadLocationException {
        String current = fb.getDocument().getText(0, fb.getDocument().getLength());
        String next = current.substring(0, offset) + (text != null ? text : "") + current.substring(offset + length);
        if (next.isEmpty() || isValid(next)) {
          super.replace(fb, offset, length, text, attrs);
        }
      }

      private boolean isValid(String text) {
        if (!text.matches("\\d{0,5}")) {
          return false;
        }
        if (text.isEmpty()) {
          return true;
        }
        try {
          int val = Integer.parseInt(text);
          return val <= 65535;
        } catch (NumberFormatException e) {
          return false;
        }
      }
    });
    ControlBehavior.apply(this.mcpPort);
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
  }

  public static void show(Component owner) {
    Window window = owner instanceof Window win ? win : (owner == null ? null : SwingUtilities.getWindowAncestor(owner));
    SettingsDialog dialog = new SettingsDialog(window);
    dialog.setLocationRelativeTo(window);
    clampToScreen(dialog);
    dialog.setVisible(true);
  }

  static void clampToScreen(Window window) {
    if (GraphicsEnvironment.isHeadless()) {
      return;
    }
    GraphicsConfiguration gc = window.getGraphicsConfiguration();
    Rectangle bounds = gc != null
        ? gc.getBounds()
        : GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
    Insets insets = gc != null
        ? Toolkit.getDefaultToolkit().getScreenInsets(gc)
        : new Insets(0, 0, 0, 0);
    int minX = bounds.x + insets.left;
    int minY = bounds.y + insets.top;
    int maxX = bounds.x + bounds.width - insets.right - window.getWidth();
    int maxY = bounds.y + bounds.height - insets.bottom - window.getHeight();
    int x = maxX >= minX ? Math.max(minX, Math.min(window.getX(), maxX)) : minX;
    int y = maxY >= minY ? Math.max(minY, Math.min(window.getY(), maxY)) : minY;
    window.setLocation(x, y);
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

    this.settingItems.clear();
    this.contentPanel.setOpaque(false);
    this.contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 0));
    this.contentPanel.add(scrollable(this.createAppearancePanel()), Category.APPEARANCE.name());
    this.contentPanel.add(scrollable(this.createGeneralPanel()), Category.GENERAL.name());
    this.contentPanel.add(scrollable(this.createGridPanel()), Category.GRID.name());
    this.contentPanel.add(scrollable(this.createKeymapPanel()), Category.KEYMAP.name());
    this.contentPanel.add(scrollable(this.createMcpPanel()), Category.MCP.name());

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
    this.contentPanel.add(this.createEmptyStatePanel(search), EMPTY_CARD);

    this.categoryModel.clear();
    for (Category category : Category.values()) {
      this.categoryModel.addElement(category);
    }
    this.categoryList.setOpaque(false);
    this.categoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.categoryList.setFixedCellHeight(68);
    this.categoryList.setCellRenderer(new CategoryRenderer());
    this.categoryList.addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting() && this.categoryList.getSelectedValue() != null) {
        this.contentCards.show(this.contentPanel, this.categoryList.getSelectedValue().name());
      }
    });

    String searchPlaceholder = text("settings_search");
    search.putClientProperty(DarkTextUI.KEY_DEFAULT_TEXT, searchPlaceholder);
    search.setToolTipText(searchPlaceholder);
    search.getAccessibleContext().setAccessibleName(searchPlaceholder);
    search.setBorder(BorderFactory.createEmptyBorder());
    search.setOpaque(false);
    search.putClientProperty("JComponent.outline", "none");
    search.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent event) { filter(search.getText()); }
      @Override public void removeUpdate(DocumentEvent event) { filter(search.getText()); }
      @Override public void changedUpdate(DocumentEvent event) { filter(search.getText()); }
    });
    RoundedSearchBox searchBox = new RoundedSearchBox(search, 0);
    searchBox.getClearButton().addActionListener(event -> search.setText(""));

    JPanel navigation = new JPanel(new BorderLayout(0, 10));
    navigation.setOpaque(false);
    navigation.setPreferredSize(new Dimension(300, 0));
    navigation.setBackground(Style.background());
    navigation.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Style.border()));
    navigation.add(searchBox, BorderLayout.NORTH);
    JScrollPane categoryScroll = new JScrollPane(this.categoryList);
    categoryScroll.setOpaque(false);
    categoryScroll.getViewport().setOpaque(false);
    categoryScroll.setBorder(null);
    categoryScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    navigation.add(categoryScroll, BorderLayout.CENTER);
    navigation.add(this.createSupportCard(), BorderLayout.SOUTH);

    root.add(navigation, BorderLayout.WEST);
    root.add(this.contentPanel, BorderLayout.CENTER);
    root.add(this.createFooter(), BorderLayout.SOUTH);
    root.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK), "focusSearch");
    root.getActionMap().put("focusSearch", new javax.swing.AbstractAction() {
      @Override public void actionPerformed(java.awt.event.ActionEvent event) {
        search.requestFocusInWindow();
        search.selectAll();
      }
    });
    this.categoryList.setSelectedValue(Category.APPEARANCE, true);
    this.contentCards.show(this.contentPanel, Category.APPEARANCE.name());
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

  private JPanel createEmptyStatePanel(JTextField search) {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setOpaque(false);
    JPanel card = new RoundedSurfacePanel(new BorderLayout(0, 16));
    card.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));
    JLabel icon = new JLabel(Icons.SEARCH_16);
    icon.setHorizontalAlignment(SwingConstants.CENTER);
    card.add(icon, BorderLayout.NORTH);

    JPanel copy = new JPanel();
    copy.setOpaque(false);
    copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
    JLabel title = new JLabel(text("settings_no_results"));
    title.setFont(title.getFont().deriveFont(Font.BOLD, 17f));
    title.setAlignmentX(Component.CENTER_ALIGNMENT);
    this.emptySearchDescription = new JLabel();
    this.emptySearchDescription.setForeground(Style.mutedText());
    this.emptySearchDescription.setAlignmentX(Component.CENTER_ALIGNMENT);

    copy.add(title);
    copy.add(Box.createRigidArea(new Dimension(0, 8)));
    copy.add(this.emptySearchDescription);
    card.add(copy, BorderLayout.CENTER);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    actions.setOpaque(false);
    JButton clearButton = new JButton(text("settings_clear_search"), Icons.CLEAR_CONSOLE_16);
    clearButton.addActionListener(e -> search.setText(""));
    actions.add(clearButton);
    card.add(actions, BorderLayout.SOUTH);

    panel.add(card);
    return panel;
  }

  private void filter(String rawQuery) {
    this.currentSearchQuery = rawQuery == null ? "" : rawQuery.strip().toLowerCase(Locale.ROOT);
    this.matchCounts.clear();

    for (Category category : Category.values()) {
      int matches;
      if (category == Category.KEYMAP) {
        matches = this.updateKeymap(this.currentSearchQuery);
      } else {
        matches = this.updateSettingRows(category, this.currentSearchQuery);
      }
      this.matchCounts.put(category, matches);
    }

    Category previousSelection = this.categoryList.getSelectedValue();
    this.categoryModel.clear();
    for (Category category : Category.values()) {
      if (this.currentSearchQuery.isEmpty() || this.matchCounts.getOrDefault(category, 0) > 0) {
        this.categoryModel.addElement(category);
      }
    }

    if (this.categoryModel.isEmpty()) {
      this.emptySearchDescription.setText(text("settings_no_results_description", rawQuery == null ? "" : rawQuery.strip()));
      this.contentCards.show(this.contentPanel, EMPTY_CARD);
    } else {
      if (previousSelection != null && this.categoryModel.contains(previousSelection)) {
        this.categoryList.setSelectedValue(previousSelection, true);
        this.contentCards.show(this.contentPanel, previousSelection.name());
      } else {
        this.categoryList.setSelectedIndex(0);
        this.contentCards.show(this.contentPanel, this.categoryModel.get(0).name());
      }
    }
    this.categoryList.repaint();
  }

  private int updateSettingRows(Category category, String query) {
    boolean categoryMatchesDirectly = category.matchesCategory(query);
    int matchCount = 0;
    List<SettingItem> categoryItems = this.settingItems.stream()
        .filter(item -> item.category == category)
        .toList();

    for (SettingItem item : categoryItems) {
      boolean matches = query.isEmpty() || categoryMatchesDirectly || item.matches(query);
      item.row.setVisible(matches);
      item.updateHighlight(query);
      if (matches && (!query.isEmpty() || categoryMatchesDirectly)) {
        matchCount++;
      }
    }

    for (SettingItem item : categoryItems) {
      if (item.separator != null) {
        if (!item.row.isVisible()) {
          item.separator.setVisible(false);
        } else {
          boolean hasNextVisible = false;
          int index = categoryItems.indexOf(item);
          for (int i = index + 1; i < categoryItems.size(); i++) {
            if (categoryItems.get(i).row.isVisible()) {
              hasNextVisible = true;
              break;
            }
          }
          item.separator.setVisible(hasNextVisible);
        }
      }
    }

    return matchCount;
  }

  private int updateKeymap(String query) {
    boolean categoryMatchesDirectly = Category.KEYMAP.matchesCategory(query);
    if (query.isEmpty() || categoryMatchesDirectly) {
      if (this.keymapSorter != null) {
        this.keymapSorter.setRowFilter(null);
      }
      return this.keymapModel.getRowCount();
    }

    int matchCount = 0;
    for (int row = 0; row < this.keymapModel.getRowCount(); row++) {
      String action = this.keymapModel.getValueAt(row, 0).toString().toLowerCase(Locale.ROOT);
      String shortcut = this.keymapModel.getValueAt(row, 1).toString().toLowerCase(Locale.ROOT);
      if (action.contains(query) || shortcut.contains(query)) {
        matchCount++;
      }
    }

    if (this.keymapSorter != null) {
      this.keymapSorter.setRowFilter(new RowFilter<>() {
        @Override
        public boolean include(Entry<? extends KeymapTableModel, ? extends Integer> entry) {
          String action = entry.getStringValue(0).toLowerCase(Locale.ROOT);
          String shortcut = entry.getStringValue(1).toLowerCase(Locale.ROOT);
          return action.contains(query) || shortcut.contains(query);
        }
      });
    }
    return matchCount;
  }

  private JPanel addSetting(
      JPanel body,
      Category category,
      Icon icon,
      String title,
      String description,
      JComponent control,
      boolean addSeparator,
      String... extraKeywords) {
    SettingRowPanel row = settingRow(icon, title, description, control);
    body.add(row);
    JSeparator separator = null;
    if (addSeparator) {
      separator = rowSeparator();
      body.add(separator);
    }
    String[] allTokens = new String[extraKeywords.length + 2];
    allTokens[0] = title;
    allTokens[1] = description;
    System.arraycopy(extraKeywords, 0, allTokens, 2, extraKeywords.length);
    this.settingItems.add(new SettingItem(
        category, row, separator, row.titleLabel, row.descriptionLabel, title, description, allTokens));
    return row;
  }

  private static final class SettingItem {
    private final Category category;
    private final SettingRowPanel row;
    private final JSeparator separator;
    private final JLabel titleLabel;
    private final JLabel descriptionLabel;
    private final String originalTitle;
    private final String originalDescription;
    private final String searchText;

    private SettingItem(
        Category category,
        SettingRowPanel row,
        JSeparator separator,
        JLabel titleLabel,
        JLabel descriptionLabel,
        String originalTitle,
        String originalDescription,
        String... tokens) {
      this.category = category;
      this.row = row;
      this.separator = separator;
      this.titleLabel = titleLabel;
      this.descriptionLabel = descriptionLabel;
      this.originalTitle = originalTitle;
      this.originalDescription = originalDescription;
      StringBuilder sb = new StringBuilder();
      for (String token : tokens) {
        if (token != null && !token.isBlank()) {
          sb.append(' ').append(token);
        }
      }
      this.searchText = sb.toString().toLowerCase(Locale.ROOT);
    }

    private boolean matches(String query) {
      return query.isEmpty() || this.searchText.contains(query);
    }

    private void updateHighlight(String query) {
      if (query == null || query.isBlank()) {
        this.row.setHighlighted(false);
        this.titleLabel.setText(this.originalTitle);
        this.descriptionLabel.setText(this.originalDescription);
      } else {
        boolean matches = this.matches(query);
        this.row.setHighlighted(matches);
        this.titleLabel.setText(highlightHtml(this.originalTitle, query));
        this.descriptionLabel.setText(highlightHtml(this.originalDescription, query));
      }
    }
  }

  static String highlightHtml(String text, String query) {
    if (text == null || text.isBlank()) {
      return text == null ? "" : text;
    }
    if (query == null || query.isBlank()) {
      return text;
    }
    String lowerText = text.toLowerCase(Locale.ROOT);
    String lowerQuery = query.strip().toLowerCase(Locale.ROOT);
    int idx = lowerText.indexOf(lowerQuery);
    if (idx < 0) {
      return text;
    }

    StringBuilder sb = new StringBuilder("<html>");
    int lastIdx = 0;
    while (idx >= 0) {
      sb.append(escapeHtml(text.substring(lastIdx, idx)));
      sb.append("<span style=\"background-color: #f59e0b; color: #18181b; font-weight: bold;\">");
      sb.append(escapeHtml(text.substring(idx, idx + lowerQuery.length())));
      sb.append("</span>");
      lastIdx = idx + lowerQuery.length();
      idx = lowerText.indexOf(lowerQuery, lastIdx);
    }
    sb.append(escapeHtml(text.substring(lastIdx)));
    sb.append("</html>");
    return sb.toString();
  }

  static String escapeHtml(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  private JPanel createMcpPanel() {
    JPanel panel = settingsPanel(Category.MCP);
    JPanel body = verticalBody();
    this.addSetting(
        body,
        Category.MCP,
        Icons.CONSOLE_16,
        text("settings_mcp_enable"),
        text("settings_mcp_enable_description"),
        this.mcpEnabled,
        true,
        "mcp", "model context protocol", "server", "ai", "assistant", "tools", "enable");
    this.addSetting(
        body,
        Category.MCP,
        Icons.CONSOLE_16,
        text("settings_mcp_port"),
        text("settings_mcp_port_description"),
        this.mcpPort,
        false,
        "port", "http", "connection", "listen", "8080", "network");
    panel.add(topAligned(body), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createGeneralPanel() {
    JPanel panel = settingsPanel(Category.GENERAL);
    JPanel body = verticalBody();
    this.addSetting(
        body,
        Category.GENERAL,
        Icons.HISTORY_16,
        text("settings_reopen_last_project"),
        text("settings_reopen_last_project_description"),
        this.reopenLastProject,
        true,
        "startup", "continue", "recent", "project", "open");
    this.addSetting(
        body,
        Category.GENERAL,
        Icons.SETTINGS_DISPLAY_24,
        text("settings_editor_fps_cap"),
        text("settings_editor_fps_cap_description"),
        this.editorFpsCap,
        true,
        "fps", "frame rate", "refresh rate", "performance", "limit", "cap", "hz");
    this.addSetting(
        body,
        Category.GENERAL,
        Icons.GREEN_PLAY_16,
        text("settings_gradle_launch_arguments"),
        text("settings_gradle_launch_arguments_description"),
        this.gradleLaunchArguments,
        true,
        "gradle", "launch", "vm", "arguments", "options", "flags", "run", "debug", "--stacktrace", "--info");
    this.addSetting(
        body,
        Category.GENERAL,
        Icons.CONSOLE_16,
        text("settings_log_level"),
        text("settings_log_level_description"),
        this.logLevel,
        false,
        "logging", "verbosity", "logger", "log level", "info", "fine", "warning", "severe", "console", "diagnostics");
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
    this.addSetting(
        body,
        Category.APPEARANCE,
        Icons.SETTINGS_LANGUAGE_24,
        text("settings_language"),
        text("settings_language_description"),
        this.language,
        true,
        "locale", "system", "english", "german", "spanish", "french", "deutsch", "español", "français");

    JPanel themes = new JPanel(new GridLayout(1, 2, 10, 0));
    themes.setOpaque(false);
    themes.setPreferredSize(new Dimension(300, 64));
    themes.add(this.lightTheme);
    themes.add(this.darkTheme);
    this.addSetting(
        body,
        Category.APPEARANCE,
        Icons.SETTINGS_THEME_LIGHT_24,
        text("menu_view_theme"),
        text("settings_theme_description"),
        themes,
        true,
        "dark", "light", "appearance", "mode", "color");

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
    this.addSetting(
        body,
        Category.APPEARANCE,
        Icons.SETTINGS_DISPLAY_24,
        text("settings_ui_scale"),
        text("settings_ui_scale_description"),
        scale,
        true,
        "scale", "zoom", "size", "percentage", "dpi", text("settings_scale_hint"));

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
    this.addSetting(
        body,
        Category.APPEARANCE,
        Icons.SETTINGS_FONT_24,
        text("settings_editor_font"),
        text("settings_editor_font_description"),
        fontControl,
        false,
        "font", "family", "size", "typography", "roboto", text("settings_font_family"), text("settings_font_size"));
    panel.add(topAligned(body), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createGridPanel() {
    JPanel panel = settingsPanel(Category.GRID);
    JPanel body = verticalBody();
    this.addSetting(
        body,
        Category.GRID,
        Icons.PENCIL_16,
        text("menu_view_gridStroke"),
        text("settings_grid_stroke_description"),
        this.gridLineWidth,
        true,
        "grid line width", "stroke", "thickness", "pixel", "width");
    this.addSetting(
        body,
        Category.GRID,
        Icons.COLOR_16,
        text("menu_view_gridColor"),
        text("settings_grid_color_description"),
        this.gridColorButton,
        true,
        "color", "rgba", "picker", "background");
    this.addSetting(
        body,
        Category.GRID,
        Icons.FIT_16,
        text("menu_view_snapDivision"),
        text("settings_snap_division_description"),
        this.snapDivision,
        false,
        "snap", "snapping", "division", "subdivisions", "tile", "grid");
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
    this.keymapSorter = new TableRowSorter<>(this.keymapModel);
    table.setRowSorter(this.keymapSorter);
    table.getColumnModel().getColumn(0).setCellRenderer(new KeymapCellRenderer());
    table.getColumnModel().getColumn(1).setCellRenderer(new KeymapCellRenderer());

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

  private final class KeymapCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      String text = value != null ? value.toString() : "";
      if (!currentSearchQuery.isEmpty() && text.toLowerCase(Locale.ROOT).contains(currentSearchQuery)) {
        this.setText(highlightHtml(text, currentSearchQuery));
        if (!isSelected) {
          Color accent = Style.accent();
          this.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 26));
        }
      } else {
        this.setText(text);
        if (!isSelected) {
          this.setBackground(null);
        }
      }
      this.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
      return c;
    }
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
    try {
      ProjectLaunchRequest.parseBuildArguments(this.gradleLaunchArguments.getText());
    } catch (IllegalArgumentException error) {
      JOptionPane.showMessageDialog(this, error.getMessage(),
          text("settings_gradle_launch_arguments"), JOptionPane.WARNING_MESSAGE);
      this.gradleLaunchArguments.requestFocusInWindow();
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
    this.preferences.setGradleLaunchArguments(this.gradleLaunchArguments.getText());
    Game.config().client().setMaxFps(fpsCap);
    Game.loop().setTickRate(fpsCap);
    this.preferences.setGridLineWidth(((Number) this.gridLineWidth.getValue()).floatValue());
    this.preferences.setGridColor(ColorHelper.encode(this.gridColor));
    this.preferences.setSnapDivision(((Number) this.snapDivision.getValue()).intValue());
    LogLevelOption selectedLogLevel = (LogLevelOption) this.logLevel.getSelectedItem();
    if (selectedLogLevel != null) {
      this.preferences.setLogLevel(selectedLogLevel.name());
      de.gurkenlabs.utiliti.controller.LoggingManager.applyLogLevel(selectedLogLevel.name());
    }
    int configuredPort = parseMcpPort();
    if (configuredPort < 1024 || configuredPort > 65535) {
      JOptionPane.showMessageDialog(this,
          "The MCP Server Port must be a valid number between 1024 and 65535.",
          text("settings_mcp_port"),
          JOptionPane.WARNING_MESSAGE);
      this.mcpPort.requestFocusInWindow();
      this.mcpPort.selectAll();
      return false;
    }
    boolean mcpStateChanged = this.mcpEnabled.isSelected() != this.preferences.isMcpEnabled()
        || configuredPort != this.preferences.getMcpPort();
    this.preferences.setMcpEnabled(this.mcpEnabled.isSelected());
    this.preferences.setMcpPort(configuredPort);
    if (mcpStateChanged) {
      if (this.preferences.isMcpEnabled()) {
        de.gurkenlabs.utiliti.mcp.McpServer.instance().stop();
        de.gurkenlabs.utiliti.mcp.McpServer.instance().start();
      } else {
        de.gurkenlabs.utiliti.mcp.McpServer.instance().stop();
      }
    }
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

  private int parseMcpPort() {
    try {
      String text = this.mcpPort.getText().trim();
      if (!text.isEmpty()) {
        int p = Integer.parseInt(text);
        if (p >= 1024 && p <= 65535) {
          return p;
        }
      }
    } catch (NumberFormatException e) {
      // Invalid format
    }
    return -1;
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
    this.gradleLaunchArguments.setText(defaults.getGradleLaunchArguments());
    this.mcpEnabled.setSelected(defaults.isMcpEnabled());
    this.mcpPort.setText(String.valueOf(defaults.getMcpPort()));
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

  private static final class SettingRowPanel extends JPanel {
    private boolean highlighted;
    private JLabel titleLabel;
    private JLabel descriptionLabel;

    private SettingRowPanel(java.awt.LayoutManager layout) {
      super(layout);
      this.setOpaque(false);
    }

    private void setHighlighted(boolean highlighted) {
      if (this.highlighted != highlighted) {
        this.highlighted = highlighted;
        this.repaint();
      }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      if (this.highlighted) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color accent = Style.accent();
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 26));
        g.fillRoundRect(2, 2, this.getWidth() - 4, this.getHeight() - 4, 10, 10);
        g.setColor(accent);
        g.fillRoundRect(2, 6, 4, this.getHeight() - 12, 4, 4);
        g.dispose();
      }
      super.paintComponent(graphics);
    }
  }

  private static SettingRowPanel settingRow(Icon icon, String title, String description, JComponent control) {
    SettingRowPanel row = new SettingRowPanel(new BorderLayout(16, 0));
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
    row.titleLabel = titleLabel;
    row.descriptionLabel = descriptionLabel;
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

  enum Category {
    APPEARANCE("settings_appearance", "settings_appearance_description", "settings_appearance_nav_description", Icons.SETTINGS_APPEARANCE_24),
    GENERAL("settings_general", "settings_general_description", "settings_general_nav_description", Icons.SETTINGS_24),
    GRID("settings_grid", "settings_grid_description", "settings_grid_nav_description", Icons.SETTINGS_GRID_24),
    KEYMAP("settings_keymap", "settings_keymap_description", "settings_keymap_nav_description", Icons.SETTINGS_KEYMAP_24),
    MCP("settings_mcp", "settings_mcp_description", "settings_mcp_nav_description", Icons.CONSOLE_24);

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

    boolean matchesCategory(String query) {
      if (query == null || query.isBlank()) {
        return true;
      }
      String q = query.strip().toLowerCase(Locale.ROOT);
      return this.name().toLowerCase(Locale.ROOT).contains(q)
          || this.toString().toLowerCase(Locale.ROOT).contains(q)
          || text(this.descriptionKey).toLowerCase(Locale.ROOT).contains(q)
          || text(this.navigationDescriptionKey).toLowerCase(Locale.ROOT).contains(q);
    }

    String searchText() {
      StringBuilder value = new StringBuilder()
          .append(this).append(' ')
          .append(text(this.descriptionKey)).append(' ')
          .append(text(this.navigationDescriptionKey));
      switch (this) {
        case APPEARANCE -> value
            .append(" language locale theme dark light scale zoom font size typography ")
            .append(' ').append(text("settings_language"))
            .append(' ').append(text("settings_language_description"))
            .append(' ').append(text("menu_view_theme"))
            .append(' ').append(text("settings_theme_description"))
            .append(' ').append(text("settings_ui_scale"))
            .append(' ').append(text("settings_ui_scale_description"))
            .append(' ').append(text("settings_editor_font"))
            .append(' ').append(text("settings_editor_font_description"))
            .append(' ').append(text("settings_font_family"))
            .append(' ').append(text("settings_font_size"));
        case GENERAL -> value
            .append(" startup reopen fps cap frame rate gradle launch arguments logging verbosity ")
            .append(' ').append(text("settings_reopen_last_project"))
            .append(' ').append(text("settings_reopen_last_project_description"))
            .append(' ').append(text("settings_editor_fps_cap"))
            .append(' ').append(text("settings_editor_fps_cap_description"))
            .append(' ').append(text("settings_gradle_launch_arguments"))
            .append(' ').append(text("settings_gradle_launch_arguments_description"))
            .append(' ').append(text("settings_log_level"))
            .append(' ').append(text("settings_log_level_description"));
        case GRID -> value
            .append(" grid stroke line width thickness color snap division ")
            .append(' ').append(text("menu_view_gridStroke"))
            .append(' ').append(text("settings_grid_stroke_description"))
            .append(' ').append(text("menu_view_gridColor"))
            .append(' ').append(text("settings_grid_color_description"))
            .append(' ').append(text("menu_view_snapDivision"))
            .append(' ').append(text("settings_snap_division_description"));
        case MCP -> value
            .append(" mcp model context protocol server port enable ai assistant ")
            .append(' ').append(text("settings_mcp_enable"))
            .append(' ').append(text("settings_mcp_enable_description"))
            .append(' ').append(text("settings_mcp_port"))
            .append(' ').append(text("settings_mcp_port_description"));
        case KEYMAP -> {
          value.append(" keymap shortcut shortcuts hotkeys actions bindings ");
          for (Command command : Command.values()) {
            value.append(' ').append(text(command.resourceKey()));
            KeyStroke ks = command.defaultKeyStroke();
            if (ks != null) {
              value.append(' ').append(KeyBindings.format(ks));
            }
          }
        }
      }
      return value.toString().toLowerCase(Locale.ROOT);
    }
  }

  static boolean matchesCategoryOrSettings(Category category, String query) {
    if (query == null || query.isBlank()) {
      return true;
    }
    String q = query.strip().toLowerCase(Locale.ROOT);
    return category.matchesCategory(q) || category.searchText().contains(q);
  }

  static boolean matchesKeymap(Command command, KeyStroke binding, String query) {
    if (query == null || query.isBlank()) {
      return true;
    }
    String q = query.strip().toLowerCase(Locale.ROOT);
    String action = text(command.resourceKey()).toLowerCase(Locale.ROOT);
    String shortcut = binding != null ? KeyBindings.format(binding).toLowerCase(Locale.ROOT) : "";
    return action.contains(q) || shortcut.contains(q) || command.name().toLowerCase(Locale.ROOT).contains(q);
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

  private final class CategoryRenderer implements ListCellRenderer<Category> {
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
      JLabel title = new JLabel();
      if (!currentSearchQuery.isEmpty() && category.toString().toLowerCase(Locale.ROOT).contains(currentSearchQuery)) {
        title.setText(highlightHtml(category.toString(), currentSearchQuery));
      } else {
        title.setText(category.toString());
      }
      title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
      title.setAlignmentX(Component.LEFT_ALIGNMENT);

      String subtitleText;
      if (currentSearchQuery.isEmpty()) {
        subtitleText = text(category.navigationDescriptionKey);
      } else {
        int count = matchCounts.getOrDefault(category, 0);
        subtitleText = count == 1
            ? text("settings_matches_singular", count)
            : text("settings_matches_plural", count);
      }

      JLabel description = new JLabel(subtitleText);
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
      Map<KeyBindings.Command.CommandGroup, Map<KeyStroke, Command>> groupUsed = new EnumMap<>(KeyBindings.Command.CommandGroup.class);
      for (KeyBindings.Command.CommandGroup group : KeyBindings.Command.CommandGroup.values()) {
        groupUsed.put(group, new HashMap<>());
      }

      for (Command command : this.commands) {
        KeyStroke keyStroke = this.bindings.get(command);
        if (keyStroke == null) {
          continue;
        }
        if (command.group() == KeyBindings.Command.CommandGroup.GLOBAL) {
          for (KeyBindings.Command.CommandGroup group : KeyBindings.Command.CommandGroup.values()) {
            Command existing = groupUsed.get(group).putIfAbsent(keyStroke, command);
            if (existing != null && existing != command) {
              return KeyBindings.format(keyStroke) + " (" + text(existing.resourceKey()) + ", "
                  + text(command.resourceKey()) + ")";
            }
          }
        } else {
          Command existingGlobal = groupUsed.get(KeyBindings.Command.CommandGroup.GLOBAL).get(keyStroke);
          if (existingGlobal != null && existingGlobal != command) {
            return KeyBindings.format(keyStroke) + " (" + text(existingGlobal.resourceKey()) + ", "
                + text(command.resourceKey()) + ")";
          }
          Map<KeyStroke, Command> used = groupUsed.get(command.group());
          Command existing = used.putIfAbsent(keyStroke, command);
          if (existing != null && existing != command) {
            return KeyBindings.format(keyStroke) + " (" + text(existing.resourceKey()) + ", "
                + text(command.resourceKey()) + ")";
          }
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

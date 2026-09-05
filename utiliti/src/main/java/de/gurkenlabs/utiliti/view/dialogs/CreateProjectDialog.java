package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.GradleProjectCreator;
import de.gurkenlabs.utiliti.controller.MavenCentralVersions;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Collects and previews the files for a new runnable Gradle LITIENGINE project. */
public final class CreateProjectDialog extends EditorDialog {
  private final JTextField gameName = new JTextField("My LITI Game");
  private final JTextField gameVersion = new JTextField("1.0.0");
  private final JTextField projectName = new JTextField("my-liti-game");
  private final JTextField namespace = new JTextField("com.example.mylitigame");
  private final JTextField location = new JTextField(defaultLocation());
  private final JComboBox<GradleProjectCreator.BuildScript> buildScript =
    new JComboBox<>(GradleProjectCreator.BuildScript.values());
  private final JComboBox<String> version = new JComboBox<>();
  private final JButton reloadVersions = Style.iconButton(Icons.RELOAD_16);
  private final JLabel versionStatus = new JLabel(" ");
  private final JLabel previewPath = new JLabel();
  private final JTextArea preview = new JTextArea();
  private final JLabel validation = new JLabel(" ");
  private final JButton create = button(Resources.strings().get("dialog_create_project_action"), Style.ButtonVariant.PRIMARY, 154);
  private boolean versionsLoaded;
  private GradleProjectCreator.Options result;

  private CreateProjectDialog(Component parent) {
    super(parent, Resources.strings().get("input_create_new_project"), Icons.FILE_NEW_16);
    this.setPreferredSize(new Dimension(980, 700));
    this.setMinimumSize(new Dimension(880, 640));

    JPanel content = new JPanel(new GridBagLayout());
    content.setOpaque(false);
    content.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.weighty = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(0, 0, 0, 18);
    constraints.weightx = 0.58;
    content.add(this.createForm(), constraints);
    constraints.gridx = 1;
    constraints.insets = new Insets(0, 0, 0, 0);
    constraints.weightx = 0.42;
    content.add(this.createPreview(), constraints);
    this.body().add(content, BorderLayout.CENTER);
    this.body().add(this.createFooter(), BorderLayout.SOUTH);

    DocumentListener listener = new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent event) { refresh(); }
      @Override public void removeUpdate(DocumentEvent event) { refresh(); }
      @Override public void changedUpdate(DocumentEvent event) { refresh(); }
    };
    this.gameName.getDocument().addDocumentListener(listener);
    this.gameVersion.getDocument().addDocumentListener(listener);
    this.projectName.getDocument().addDocumentListener(listener);
    this.namespace.getDocument().addDocumentListener(listener);
    this.location.getDocument().addDocumentListener(listener);
    Component editorComponent = this.version.getEditor().getEditorComponent();
    if (editorComponent instanceof JTextField editorField) {
      editorField.getDocument().addDocumentListener(listener);
    }
    this.version.addActionListener(event -> this.refresh());
    this.buildScript.addActionListener(event -> this.refresh());
    this.reloadVersions.setToolTipText(Resources.strings().get("dialog_create_project_versions_reload"));
    this.reloadVersions.addActionListener(event -> this.loadVersions());
    this.create.addActionListener(event -> this.accept());
    this.getRootPane().setDefaultButton(this.create);
    this.loadVersions();
  }

  public static GradleProjectCreator.Options show(Component parent) {
    CreateProjectDialog dialog = new CreateProjectDialog(parent);
    dialog.showCentered();
    return dialog.result;
  }

  private JPanel createForm() {
    JPanel fields = new JPanel();
    fields.setOpaque(false);
    fields.setLayout(new javax.swing.BoxLayout(fields, javax.swing.BoxLayout.Y_AXIS));

    JPanel identity = new JPanel(new BorderLayout(0, 14));
    identity.setOpaque(false);
    JPanel names = new JPanel(new GridLayout(1, 2, 12, 0));
    names.setOpaque(false);
    names.add(field(Resources.strings().get("dialog_create_project_game_name"), this.gameName));
    names.add(field(Resources.strings().get("dialog_create_project_name"), this.projectName));
    identity.add(names, BorderLayout.NORTH);
    JPanel metadata = new JPanel(new GridLayout(1, 2, 12, 0));
    metadata.setOpaque(false);
    metadata.add(field(Resources.strings().get("dialog_create_project_namespace"), this.namespace));
    metadata.add(field(Resources.strings().get("dialog_create_project_game_version"), this.gameVersion));
    identity.add(metadata, BorderLayout.CENTER);
    fields.add(fixedHeight(card(Resources.strings().get("dialog_create_project_identity"), identity)));
    fields.add(javax.swing.Box.createVerticalStrut(14));

    JPanel setup = new JPanel(new BorderLayout(0, 14));
    setup.setOpaque(false);
    JPanel locationRow = field(Resources.strings().get("dialog_create_project_parent"), this.location);
    JButton browse = Style.iconButton(Icons.FOLDER_OPEN_16);
    browse.setToolTipText(Resources.strings().get("dialog_create_project_browse"));
    browse.addActionListener(event -> this.browse());
    locationRow.add(browse, BorderLayout.EAST);
    setup.add(locationRow, BorderLayout.NORTH);

    JPanel choices = new JPanel(new GridLayout(1, 2, 12, 0));
    choices.setOpaque(false);
    choices.add(field(Resources.strings().get("dialog_create_project_build_script"), this.buildScript));
    JPanel versionField = field(Resources.strings().get("dialog_create_project_version"), this.version);
    versionField.add(this.reloadVersions, BorderLayout.EAST);
    choices.add(versionField);
    setup.add(choices, BorderLayout.CENTER);
    this.versionStatus.setFont(this.versionStatus.getFont().deriveFont(12f));
    this.versionStatus.setForeground(Style.mutedText());
    setup.add(this.versionStatus, BorderLayout.SOUTH);
    fields.add(fixedHeight(card(Resources.strings().get("dialog_create_project_gradle"), setup)));
    return fields;
  }

  private JPanel createPreview() {
    JPanel content = new JPanel(new BorderLayout(0, 10));
    content.setOpaque(false);
    JPanel panel = card(Resources.strings().get("dialog_create_project_preview"), content);
    this.previewPath.setForeground(Style.mutedText());
    this.previewPath.setFont(this.previewPath.getFont().deriveFont(12f));
    content.add(this.previewPath, BorderLayout.NORTH);

    this.preview.setEditable(false);
    this.preview.setOpaque(false);
    this.preview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
    this.preview.setForeground(Style.text());
    this.preview.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 8));
    JScrollPane scroll = new JScrollPane(this.preview);
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    scroll.setBorder(BorderFactory.createLineBorder(Style.border()));
    content.add(scroll, BorderLayout.CENTER);

    JPanel previewFooter = new JPanel(new BorderLayout(0, 8));
    previewFooter.setOpaque(false);
    JLabel explanation = new JLabel("<html>" + Resources.strings().get("dialog_create_project_explanation") + "</html>");
    explanation.setForeground(Style.mutedText());
    explanation.setFont(explanation.getFont().deriveFont(11f));
    previewFooter.add(explanation, BorderLayout.NORTH);

    JLabel summary = new JLabel("<html><font color='#4ade80'>✓</font> "
      + Resources.strings().get("dialog_create_project_summary") + "</html>");
    summary.setForeground(Style.mutedText());
    previewFooter.add(summary, BorderLayout.SOUTH);
    content.add(previewFooter, BorderLayout.SOUTH);
    return panel;
  }

  private JPanel createFooter() {
    JPanel footer = new JPanel(new BorderLayout());
    footer.setOpaque(false);
    footer.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()),
      BorderFactory.createEmptyBorder(14, 24, 14, 24)));
    this.validation.setForeground(Style.COLOR_RED);
    footer.add(this.validation, BorderLayout.CENTER);
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    actions.setOpaque(false);
    JButton cancel = button(Resources.strings().get("dialog_cancel"), Style.ButtonVariant.SECONDARY, 104);
    cancel.addActionListener(event -> this.close());
    actions.add(cancel);
    actions.add(this.create);
    footer.add(actions, BorderLayout.EAST);
    return footer;
  }

  private void browse() {
    JFileChooser chooser = new JFileChooser(this.location.getText());
    chooser.setDialogTitle(Resources.strings().get("dialog_create_project_browse"));
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      this.location.setText(chooser.getSelectedFile().toPath().toString());
    }
  }

  private void loadVersions() {
    this.versionsLoaded = false;
    this.version.setEditable(false);
    this.version.removeAllItems();
    this.version.addItem(Resources.strings().get("dialog_create_project_versions_loading"));
    this.version.setEnabled(false);
    this.reloadVersions.setEnabled(false);
    this.versionStatus.setForeground(Style.mutedText());
    this.versionStatus.setText(Resources.strings().get("dialog_create_project_versions_source"));
    this.refresh();
    CompletableFuture.supplyAsync(() -> {
      try {
        return MavenCentralVersions.load();
      } catch (Exception exception) {
        throw new java.util.concurrent.CompletionException(exception);
      }
    }).whenComplete((versions, failure) -> SwingUtilities.invokeLater(() -> this.applyVersions(versions, failure)));
  }

  private void applyVersions(List<String> versions, Throwable failure) {
    if (!this.isDisplayable()) {
      return;
    }
    this.version.removeAllItems();
    this.reloadVersions.setEnabled(true);
    if (failure != null || versions == null || versions.isEmpty()) {
      this.version.setEditable(true);
      this.version.setEnabled(true);
      String fallback = defaultEngineVersion();
      this.version.addItem(fallback);
      this.version.setSelectedItem(fallback);
      this.versionsLoaded = true;
      this.versionStatus.setForeground(Style.COLOR_RED);
      this.versionStatus.setText(Resources.strings().get("dialog_create_project_versions_failed"));
      this.refresh();
      return;
    }
    this.version.setEditable(false);
    versions.forEach(this.version::addItem);
    this.version.setEnabled(true);
    this.versionsLoaded = true;
    this.versionStatus.setForeground(Style.mutedText());
    this.versionStatus.setText(Resources.strings().get("dialog_create_project_versions_published", versions.size()));
    this.refresh();
  }

  private void refresh() {
    try {
      GradleProjectCreator.Options options = this.options();
      this.previewPath.setText(options.projectRoot().toString());
      this.preview.setText(String.join(System.lineSeparator(), GradleProjectCreator.preview(options)));
      if (!this.versionsLoaded) {
        throw new IllegalArgumentException(Resources.strings().get("dialog_create_project_version_required"));
      }
      if (Files.exists(options.projectRoot())) {
        throw new IllegalArgumentException(Resources.strings().get("dialog_create_project_exists"));
      }
      this.validation.setText(" ");
      this.create.setEnabled(true);
    } catch (RuntimeException exception) {
      this.validation.setText(exception.getMessage());
      this.create.setEnabled(false);
    }
  }

  private void accept() {
    this.result = this.options();
    this.close();
  }

  private GradleProjectCreator.Options options() {
    if (this.location.getText().isBlank()) {
      throw new IllegalArgumentException(Resources.strings().get("dialog_create_project_location_required"));
    }
    Object selectedVersion = this.version.isEditable()
      ? this.version.getEditor().getItem()
      : this.version.getSelectedItem();
    return new GradleProjectCreator.Options(
      Path.of(this.location.getText()),
      this.projectName.getText(),
      this.gameName.getText(),
      this.gameVersion.getText(),
      this.namespace.getText(),
      selectedVersion == null ? "" : selectedVersion.toString(),
      (GradleProjectCreator.BuildScript) this.buildScript.getSelectedItem());
  }

  private static JPanel field(String label, Component component) {
    JPanel panel = new JPanel(new BorderLayout(8, 6));
    panel.setOpaque(false);
    JLabel caption = new JLabel(label);
    caption.setForeground(Style.mutedText());
    caption.setFont(caption.getFont().deriveFont(12f));
    panel.add(caption, BorderLayout.NORTH);
    component.setPreferredSize(new Dimension(200, 34));
    panel.add(component, BorderLayout.CENTER);
    return panel;
  }

  private static JPanel card(String title, JPanel content) {
    JPanel card = new JPanel(new BorderLayout(0, 12));
    card.setBackground(Style.surface());
    card.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(Style.border()),
      BorderFactory.createEmptyBorder(16, 16, 16, 16)));
    JLabel heading = new JLabel(title);
    heading.setFont(heading.getFont().deriveFont(Font.BOLD, 15f));
    heading.setForeground(Style.text());
    card.add(heading, BorderLayout.NORTH);
    card.add(content, BorderLayout.CENTER);
    return card;
  }

  private static JPanel fixedHeight(JPanel panel) {
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    return panel;
  }

  private static JButton button(String text, Style.ButtonVariant variant, int width) {
    JButton button = Style.textButton(text);
    Style.styleButton(button, variant);
    Dimension size = new Dimension(width, 38);
    button.setPreferredSize(size);
    button.setMinimumSize(size);
    button.setMaximumSize(size);
    return button;
  }

  private static String defaultLocation() {
    return Path.of(System.getProperty("user.home"), "Projects").toString();
  }

  static String defaultEngineVersion() {
    String version = Game.info().getVersion();
    if (version == null || version.isBlank()) {
      return "0.12.0";
    }
    String normalized = version.trim();
    if (normalized.toUpperCase(java.util.Locale.ROOT).endsWith("-SNAPSHOT")) {
      String stripped = normalized.substring(0, normalized.length() - "-SNAPSHOT".length()).trim();
      return stripped.isEmpty() ? "0.12.0" : stripped;
    }
    return normalized;
  }
}

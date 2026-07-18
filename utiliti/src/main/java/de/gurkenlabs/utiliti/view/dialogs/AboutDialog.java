package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.UriUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.imageio.ImageIO;

/** Displays application, project, and runtime information. */
public final class AboutDialog extends JDialog {
  private static final String LINKS = "links";
  private static final Color ACCENT = new Color(55, 139, 255);
  private static final int COLUMN_WIDTH = 470;
  private static final int DIALOG_WIDTH = 1140;

  private AboutDialog(Window owner) {
    super(owner, Resources.strings().get("menu_help_about") + " utiLITI", ModalityType.APPLICATION_MODAL);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setContentPane(this.createContent());
    this.getRootPane().registerKeyboardAction(
      event -> this.dispose(),
      KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
      JComponent.WHEN_IN_FOCUSED_WINDOW);
    this.setMinimumSize(new Dimension(900, 650));
  }

  public static void show(Component owner) {
    Window window = owner == null ? null : SwingUtilities.getWindowAncestor(owner);
    AboutDialog dialog = new AboutDialog(window);
    Dimension screen = dialog.getGraphicsConfiguration().getBounds().getSize();
    dialog.setSize(
      Math.min(DIALOG_WIDTH, (int) (screen.width * .92)),
      Math.min(760, (int) (screen.height * .92)));
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);
  }

  private JPanel createContent() {
    JPanel root = new JPanel(new BorderLayout(0, 16));
    root.setBorder(BorderFactory.createEmptyBorder(28, 32, 18, 32));

    JPanel columns = new JPanel(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.weighty = 1;
    constraints.fill = GridBagConstraints.BOTH;

    constraints.gridx = 0;
    constraints.weightx = 1;
    constraints.insets = new Insets(0, 0, 0, 28);
    columns.add(this.createProductPanel(), constraints);

    constraints.gridx = 1;
    constraints.weightx = 0;
    constraints.insets = new Insets(0, 0, 0, 0);
    columns.add(new JSeparator(SwingConstants.VERTICAL), constraints);

    constraints.gridx = 2;
    constraints.weightx = 1;
    constraints.insets = new Insets(0, 28, 0, 0);
    columns.add(this.createDetailsPanel(), constraints);

    root.add(columns, BorderLayout.CENTER);
    root.add(this.createFooter(), BorderLayout.SOUTH);
    return root;
  }

  private JPanel createProductPanel() {
    JPanel panel = verticalPanel(COLUMN_WIDTH);
    panel.add(this.createBrand());
    panel.add(gap(18));
    panel.add(wrappedLabel(Resources.strings().get("menu_help_abouttext"), COLUMN_WIDTH, 16));
    panel.add(gap(24));
    panel.add(feature(Symbol.PENCIL, "Map & Scene Editing", "Create and edit tilemaps, layers, objects and scenes with an intuitive workflow."));
    panel.add(gap(14));
    panel.add(feature(Symbol.CUBE, "Project Management", "Organize resources, manage assets and configure your LITIENGINE projects."));
    panel.add(gap(14));
    panel.add(feature(Symbol.CODE, "Open Source", "Built with Java and Swing. 100% open source and community driven."));
    panel.add(gap(14));

    JPanel support = feature(Symbol.HEART, "Support the Devs", "Support the ongoing development of LITIENGINE and utiLITI on OpenCollective.");
    support.setMaximumSize(new Dimension(COLUMN_WIDTH, 92));
    JPanel supportCopy = (JPanel) support.getComponent(1);
    supportCopy.add(externalLinkButton("opencollective.com/litiengine", link("link_opencollective")));
    panel.add(support);
    panel.add(Box.createVerticalGlue());
    panel.add(separator());
    panel.add(gap(10));
    panel.add(this.createProjectLinks());
    return panel;
  }

  private JPanel createBrand() {
    JPanel brand = new JPanel(new BorderLayout(14, 0));
    brand.setAlignmentX(Component.LEFT_ALIGNMENT);
    brand.setMaximumSize(new Dimension(COLUMN_WIDTH, 78));

    JLabel logo = createEngineLogo(72);
    if (logo != null) {
      brand.add(logo, BorderLayout.WEST);
    }

    JPanel name = verticalPanel(225);
    JLabel title = new JLabel("utiLITI");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 36f));
    alignLeft(title);
    name.add(title);
    JLabel subtitle = new JLabel("The editor for LITIENGINE");
    subtitle.setForeground(ACCENT);
    subtitle.setFont(subtitle.getFont().deriveFont(16f));
    alignLeft(subtitle);
    name.add(subtitle);
    brand.add(name, BorderLayout.CENTER);

    JLabel version = new RoundedLabel(Game.info().getVersion());
    version.setForeground(ACCENT);
    version.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
    JPanel badgeHolder = new JPanel(new GridBagLayout());
    badgeHolder.add(version);
    brand.add(badgeHolder, BorderLayout.EAST);
    return brand;
  }

  private JPanel createDetailsPanel() {
    JPanel panel = verticalPanel(COLUMN_WIDTH);
    panel.add(heading("Version"));
    panel.add(gap(12));
    panel.add(bodyLabel("utiLITI " + Game.info().getVersion()));
    panel.add(bodyLabel("Java: " + System.getProperty("java.version") + " (vendor: " + System.getProperty("java.vendor") + ")"));
    panel.add(bodyLabel("Runtime: " + System.getProperty("java.vm.name")));
    panel.add(bodyLabel("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")"));
    panel.add(bodyLabel("Look & Feel: " + UIManager.getLookAndFeel().getName()));
    panel.add(gap(22));
    panel.add(separator());
    panel.add(gap(22));
    panel.add(this.createEnginePanel());
    panel.add(gap(22));
    panel.add(separator());
    panel.add(gap(22));
    panel.add(heading("Acknowledgements"));
    panel.add(gap(8));
    panel.add(wrappedLabel("Thanks to all contributors and the community for making LITIENGINE and utiLITI possible!", COLUMN_WIDTH, 15));
    panel.add(externalLinkButton("View Contributors", link("link_LITIengine_contributors")));
    panel.add(Box.createVerticalGlue());
    return panel;
  }

  private JPanel createEnginePanel() {
    JPanel engine = new JPanel(new BorderLayout(18, 0));
    engine.setAlignmentX(Component.LEFT_ALIGNMENT);
    engine.setMaximumSize(new Dimension(COLUMN_WIDTH, 210));

    JPanel copy = verticalPanel(335);
    copy.add(heading("LITIENGINE"));
    copy.add(gap(10));
    copy.add(bodyLabel("Powered by LITIENGINE"));
    copy.add(externalLinkButton("litiengine.com", link("link_LITIengine_home")));
    copy.add(gap(10));
    copy.add(wrappedLabel("A modern, easy to use Java 2D game engine for desktop, web and mobile platforms.", 320, 15));
    copy.add(gap(12));
    copy.add(this.createBadges());
    engine.add(copy, BorderLayout.CENTER);

    JLabel logo = createEngineLogo(96);
    if (logo != null) {
      engine.add(logo, BorderLayout.EAST);
    }
    return engine;
  }

  private JPanel createProjectLinks() {
    JPanel links = new JPanel(new GridBagLayout());
    links.setAlignmentX(Component.LEFT_ALIGNMENT);
    links.setMaximumSize(new Dimension(COLUMN_WIDTH, 40));
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;

    constraints.gridx = 0;
    constraints.weightx = 1;
    links.add(iconLink(Symbol.GLOBE, "Website", "litiengine.com", link("link_LITIengine_home"), true), constraints);

    constraints.gridx = 1;
    constraints.weightx = 1.7;
    links.add(iconLink(Symbol.GITHUB, "GitHub", "github.com/gurkenlabs/litiengine", link("link_LITIengine_github"), true), constraints);

    constraints.gridx = 2;
    constraints.weightx = 1.2;
    links.add(iconLink(Symbol.DOCUMENT, "Docs", "docs.litiengine.com", link("link_LITIengine_docs"), true), constraints);
    return links;
  }

  private JPanel createBadges() {
    JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    badges.setAlignmentX(Component.LEFT_ALIGNMENT);
    badges.add(badge("Java", new Color(196, 126, 25)));
    badges.add(badge("Open Source", new Color(45, 155, 82)));
    badges.add(badge("2D", ACCENT));
    badges.add(badge("Cross Platform", new Color(152, 90, 207)));
    return badges;
  }

  private JPanel createFooter() {
    JPanel footer = new JPanel(new BorderLayout());
    JPanel copyright = verticalPanel(360);
    String year = new SimpleDateFormat("yyyy").format(new Date());
    copyright.add(bodyLabel("\u00a9 " + year + " Steffen Wilke and Matthias Wilke"));
    copyright.add(bodyLabel("All rights reserved."));
    footer.add(copyright, BorderLayout.WEST);
    JButton close = new JButton("Close");
    close.addActionListener(event -> this.dispose());
    close.setPreferredSize(new Dimension(96, 36));
    this.getRootPane().setDefaultButton(close);
    footer.add(close, BorderLayout.EAST);
    return footer;
  }

  private static JPanel feature(Symbol symbol, String title, String description) {
    JPanel feature = new JPanel(new BorderLayout(18, 0));
    feature.setAlignmentX(Component.LEFT_ALIGNMENT);
    feature.setMaximumSize(new Dimension(COLUMN_WIDTH, 68));

    JLabel icon = new JLabel(new LineIcon(symbol, 48));
    icon.setForeground(ACCENT);
    icon.setPreferredSize(new Dimension(48, 48));
    icon.setVerticalAlignment(SwingConstants.TOP);
    feature.add(icon, BorderLayout.WEST);

    JPanel copy = verticalPanel(COLUMN_WIDTH - 66);
    copy.add(heading(title));
    copy.add(gap(2));
    copy.add(wrappedLabel(description, COLUMN_WIDTH - 66, 15));
    feature.add(copy, BorderLayout.CENTER);
    return feature;
  }

  private static JPanel iconLink(Symbol symbol, String title, String address, String url, boolean accentAddress) {
    JPanel link = new JPanel(new BorderLayout(8, 0));
    JLabel icon = new JLabel(new LineIcon(symbol, 22));
    icon.setForeground(secondaryTextColor());
    icon.setVerticalAlignment(SwingConstants.TOP);
    link.add(icon, BorderLayout.WEST);

    JPanel copy = verticalPanel(220);
    JLabel heading = new JLabel(title);
    heading.setFont(heading.getFont().deriveFont(Font.BOLD, 14f));
    alignLeft(heading);
    copy.add(heading);
    JLabel addressLink = linkLabel(address, url);
    addressLink.setFont(addressLink.getFont().deriveFont(11f));
    addressLink.setForeground(accentAddress ? ACCENT : secondaryTextColor());
    copy.add(addressLink);
    link.add(copy, BorderLayout.CENTER);
    return link;
  }

  private static JButton externalLinkButton(String text, String url) {
    JButton button = linkButton(text, url);
    button.setIcon(new LineIcon(Symbol.EXTERNAL, 12));
    button.setHorizontalTextPosition(SwingConstants.LEFT);
    button.setIconTextGap(6);
    return button;
  }

  private static JLabel linkLabel(String text, String url) {
    JLabel label = new JLabel(text);
    label.setForeground(ACCENT);
    label.setToolTipText(text);
    label.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    label.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent event) {
        UriUtilities.openWebpage(URI.create(url));
      }
    });
    return label;
  }

  private static JButton linkButton(String text, String url) {
    JButton button = new JButton(text);
    button.setForeground(ACCENT);
    button.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
    button.setContentAreaFilled(false);
    button.setFocusPainted(false);
    button.setHorizontalAlignment(SwingConstants.LEFT);
    button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    button.setAlignmentX(Component.LEFT_ALIGNMENT);
    button.addActionListener(event -> UriUtilities.openWebpage(URI.create(url)));
    return button;
  }

  private static JLabel badge(String text, Color color) {
    JLabel badge = new JLabel(text);
    badge.setForeground(color);
    badge.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(withAlpha(color, 90)),
      BorderFactory.createEmptyBorder(5, 9, 5, 9)));
    return badge;
  }

  private static JLabel heading(String text) {
    JLabel label = new JLabel(text);
    label.setFont(label.getFont().deriveFont(Font.BOLD, 17f));
    alignLeft(label);
    return label;
  }

  private static JLabel bodyLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(label.getFont().deriveFont(15f));
    label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
    alignLeft(label);
    return label;
  }

  private static JTextArea wrappedLabel(String text, int width, int fontSize) {
    JTextArea label = new JTextArea(text);
    label.setEditable(false);
    label.setFocusable(false);
    label.setOpaque(false);
    label.setLineWrap(true);
    label.setWrapStyleWord(true);
    label.setFont(UIManager.getFont("Label.font").deriveFont((float) fontSize));
    label.setForeground(UIManager.getColor("Label.foreground"));
    label.setBorder(null);
    label.setSize(new Dimension(width, Short.MAX_VALUE));
    int height = label.getPreferredSize().height;
    label.setPreferredSize(new Dimension(width, height));
    label.setMaximumSize(new Dimension(width, height));
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    return label;
  }

  private static JPanel verticalPanel(int width) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
    return panel;
  }

  private static JSeparator separator() {
    JSeparator separator = new JSeparator();
    separator.setAlignmentX(Component.LEFT_ALIGNMENT);
    separator.setMaximumSize(new Dimension(COLUMN_WIDTH, 1));
    return separator;
  }

  private static Component gap(int height) {
    return Box.createRigidArea(new Dimension(0, height));
  }

  private static JLabel createEngineLogo(int size) {
    try {
      BufferedImage source = ImageIO.read(AboutDialog.class.getResource("/litiengine-icon.png"));
      return new JLabel(new ScaledImageIcon(source, size));
    } catch (IOException | IllegalArgumentException _) {
      return null;
    }
  }

  private static void alignLeft(JComponent component) {
    component.setAlignmentX(Component.LEFT_ALIGNMENT);
  }

  private static String link(String key) {
    return Resources.strings().getFrom(LINKS, key);
  }

  private static Color withAlpha(Color color, int alpha) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
  }

  private static Color secondaryTextColor() {
    Color color = UIManager.getColor("Label.disabledForeground");
    return color != null ? color : UIManager.getColor("Label.foreground");
  }

  private enum Symbol {
    PENCIL,
    CUBE,
    CODE,
    HEART,
    GLOBE,
    GITHUB,
    DOCUMENT,
    EXTERNAL
  }

  private record LineIcon(Symbol symbol, int size) implements Icon {
    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      Graphics2D g = (Graphics2D) graphics.create();
      g.translate(x, y);
      double scale = this.size / 48.0;
      g.scale(scale, scale);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(component.getForeground() != null ? component.getForeground() : ACCENT);
      g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      switch (this.symbol) {
        case PENCIL -> paintPencil(g);
        case CUBE -> paintCube(g);
        case CODE -> paintCode(g);
        case HEART -> paintHeart(g);
        case GLOBE -> paintGlobe(g);
        case GITHUB -> paintGithub(g);
        case DOCUMENT -> paintDocument(g);
        case EXTERNAL -> paintExternal(g);
      }
      g.dispose();
    }

    @Override
    public int getIconWidth() {
      return this.size;
    }

    @Override
    public int getIconHeight() {
      return this.size;
    }

    private static void paintPencil(Graphics2D g) {
      Path2D pencil = new Path2D.Double();
      pencil.moveTo(9, 36);
      pencil.lineTo(12, 25);
      pencil.lineTo(32, 5);
      pencil.quadTo(36, 1, 40, 5);
      pencil.lineTo(43, 8);
      pencil.quadTo(47, 12, 43, 16);
      pencil.lineTo(23, 36);
      pencil.closePath();
      g.draw(pencil);
      g.drawLine(12, 25, 23, 36);
      g.drawLine(32, 6, 43, 17);
    }

    private static void paintCube(Graphics2D g) {
      Path2D cube = new Path2D.Double();
      cube.moveTo(24, 3);
      cube.lineTo(43, 13);
      cube.lineTo(43, 35);
      cube.lineTo(24, 45);
      cube.lineTo(5, 35);
      cube.lineTo(5, 13);
      cube.closePath();
      g.draw(cube);
      g.drawLine(5, 13, 24, 24);
      g.drawLine(43, 13, 24, 24);
      g.drawLine(24, 24, 24, 45);
    }

    private static void paintCode(Graphics2D g) {
      g.drawLine(17, 10, 5, 24);
      g.drawLine(5, 24, 17, 38);
      g.drawLine(31, 10, 43, 24);
      g.drawLine(43, 24, 31, 38);
      g.drawLine(28, 5, 20, 43);
    }

    private static void paintHeart(Graphics2D g) {
      Path2D heart = new Path2D.Double();
      heart.moveTo(24, 43);
      heart.lineTo(7, 27);
      heart.curveTo(-3, 17, 3, 5, 14, 5);
      heart.curveTo(20, 5, 23, 9, 24, 12);
      heart.curveTo(27, 7, 30, 5, 35, 5);
      heart.curveTo(46, 5, 51, 17, 41, 27);
      heart.closePath();
      g.draw(heart);
    }

    private static void paintGlobe(Graphics2D g) {
      g.draw(new Ellipse2D.Double(5, 5, 38, 38));
      g.draw(new Ellipse2D.Double(15, 5, 18, 38));
      g.drawLine(5, 24, 43, 24);
    }

    private static void paintGithub(Graphics2D g) {
      g.draw(new Ellipse2D.Double(5, 5, 38, 38));
      Path2D cat = new Path2D.Double();
      cat.moveTo(14, 32);
      cat.curveTo(10, 28, 10, 17, 17, 14);
      cat.lineTo(16, 9);
      cat.lineTo(22, 13);
      cat.curveTo(24, 12, 27, 12, 29, 13);
      cat.lineTo(35, 9);
      cat.lineTo(34, 15);
      cat.curveTo(40, 20, 38, 30, 33, 33);
      g.draw(cat);
      g.drawLine(19, 34, 19, 41);
      g.drawLine(29, 34, 29, 41);
    }

    private static void paintDocument(Graphics2D g) {
      Path2D page = new Path2D.Double();
      page.moveTo(10, 4);
      page.lineTo(30, 4);
      page.lineTo(40, 14);
      page.lineTo(40, 44);
      page.lineTo(10, 44);
      page.closePath();
      g.draw(page);
      g.drawLine(30, 4, 30, 14);
      g.drawLine(30, 14, 40, 14);
      g.drawLine(17, 23, 33, 23);
      g.drawLine(17, 31, 33, 31);
    }

    private static void paintExternal(Graphics2D g) {
      g.drawLine(17, 12, 37, 12);
      g.drawLine(37, 12, 37, 32);
      g.drawLine(37, 12, 20, 29);
      g.drawLine(31, 36, 11, 36);
      g.drawLine(11, 36, 11, 16);
    }
  }

  private static final class RoundedLabel extends JLabel {
    private RoundedLabel(String text) {
      super(text);
      this.setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      Graphics2D g = (Graphics2D) graphics.create();
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(new Color(28, 44, 63));
      g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 12, 12);
      g.dispose();
      super.paintComponent(graphics);
    }
  }

  private record ScaledImageIcon(BufferedImage image, int size) implements Icon {
    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      Graphics2D g = (Graphics2D) graphics.create();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.drawImage(this.image, x, y, this.size, this.size, null);
      g.dispose();
    }

    @Override
    public int getIconWidth() {
      return this.size;
    }

    @Override
    public int getIconHeight() {
      return this.size;
    }
  }
}

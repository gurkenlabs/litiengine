package de.gurkenlabs.utiliti.swing.menus;

import java.net.URI;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.UriUtilities;

@SuppressWarnings("serial")
public final class HelpMenu extends JMenu {
  public HelpMenu() {
    super(Resources.strings().get("menu_help"));
    this.setMnemonic('H');

    JMenuItem tutorialMenuItem = new JMenuItem(Resources.strings().get("menu_help_tutorial"));
    tutorialMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_tutorials"))));

    JMenuItem docsMenuItem = new JMenuItem(Resources.strings().get("menu_help_docs"));
    docsMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_docs"))));

    JMenuItem forumMenuItem = new JMenuItem(Resources.strings().get("menu_help_forum"));
    forumMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_forum"))));

    JMenuItem javadocsMenuItem = new JMenuItem(Resources.strings().get("menu_help_javadocs"));
    javadocsMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_javadocs"))));

    JMenuItem bugMenuItem = new JMenuItem(Resources.strings().get("menu_help_bug"));
    bugMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_bug"))));

    JMenuItem releaseMenuItem = new JMenuItem(Resources.strings().get("menu_help_releasenotes"));
    releaseMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_LITIengine_releasenotes"))));

    JMenuItem patreonMenuItem = new JMenuItem(Resources.strings().get("menu_help_patreon"));
    patreonMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_patreon"))));

    JMenuItem payPalMenuItem = new JMenuItem(Resources.strings().get("menu_help_paypal"));
    payPalMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom("links", "link_paypal"))));

    JMenuItem aboutMenuItem = new JMenuItem(Resources.strings().get("menu_help_about"));
    aboutMenuItem.addActionListener(event -> JOptionPane.showMessageDialog(((JFrame) Game.window().getHostControl()), Resources.strings().get("menu_help_abouttext") + "\n" + Resources.strings().get("menu_help_releases") + Resources.strings().getFrom("links", "link_LITIengine_releases") + "\n\n"
        + Resources.strings().get("copyright_gurkenlabs", "2019") + "\n" + Resources.strings().get("copyright_LITIengine"), Resources.strings().get("menu_help_about") + " " + Game.info().getVersion(), JOptionPane.INFORMATION_MESSAGE));

    this.add(tutorialMenuItem);
    this.add(docsMenuItem);
    this.add(forumMenuItem);
    this.add(javadocsMenuItem);
    this.addSeparator();
    this.add(releaseMenuItem);
    this.add(bugMenuItem);
    this.addSeparator();
    this.add(patreonMenuItem);
    this.add(payPalMenuItem);
    this.addSeparator();
    this.add(aboutMenuItem);
  }
}

package com.litiengine.utiliti.swing.menus;

import java.net.URI;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.litiengine.Game;
import com.litiengine.resources.Resources;
import com.litiengine.util.UriUtilities;

@SuppressWarnings("serial")
public final class HelpMenu extends JMenu {
  private static final String LINKS = "links";

  public HelpMenu() {
    super(Resources.strings().get("menu_help"));
    this.setMnemonic('H');

    JMenuItem docsMenuItem = new JMenuItem(Resources.strings().get("menu_help_docs"));
    docsMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_docs"))));

    JMenuItem javadocsMenuItem = new JMenuItem(Resources.strings().get("menu_help_javadocs"));
    javadocsMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_javadocs"))));

    JMenuItem tutorialMenuItem = new JMenuItem(Resources.strings().get("menu_help_tutorials"));
    tutorialMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_tutorials"))));

    JMenuItem forumMenuItem = new JMenuItem(Resources.strings().get("menu_help_forum"));
    forumMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_forum"))));

    JMenuItem bugMenuItem = new JMenuItem(Resources.strings().get("menu_help_bug"));
    bugMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_bug"))));

    JMenuItem releaseMenuItem = new JMenuItem(Resources.strings().get("menu_help_releasenotes"));
    releaseMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_releasenotes"))));

    JMenuItem openCollectiveMenuItem = new JMenuItem(Resources.strings().get("menu_help_opencollective"));
    openCollectiveMenuItem.addActionListener(event -> UriUtilities.openWebpage(URI.create(Resources.strings().getFrom(LINKS, "link_opencollective"))));

    String aboutMessage = String.format("%s%n%n%s", Resources.strings().get("menu_help_abouttext"), Resources.strings().get("copyright", "2020"));
    String aboutTitle = String.format("%s %s, %s", Resources.strings().get("menu_help_about"), Resources.strings().get("menu_help_utiliti"), Game.info().getVersion());
    JMenuItem aboutMenuItem = new JMenuItem(Resources.strings().get("menu_help_about"));
    aboutMenuItem.addActionListener(event -> JOptionPane.showMessageDialog(((JFrame) Game.window().getHostControl()), aboutMessage, aboutTitle, JOptionPane.INFORMATION_MESSAGE));

    this.add(docsMenuItem);
    this.add(javadocsMenuItem);
    this.add(tutorialMenuItem);
    this.add(forumMenuItem);
    this.addSeparator();
    this.add(releaseMenuItem);
    this.add(bugMenuItem);
    this.addSeparator();
    this.add(openCollectiveMenuItem);
    this.add(aboutMenuItem);
  }
}

package de.gurkenlabs.utiliti.view.menus;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.UriUtilities;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.view.dialogs.AboutDialog;
import java.net.URI;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public final class HelpMenu extends JMenu {
  private static final String LINKS = "links";

  public HelpMenu() {
    super(Resources.strings().get("menu_help"));
    this.setMnemonic(this.getText().charAt(0));

    JMenuItem docsMenuItem = new JMenuItem(Resources.strings().get("menu_help_docs"), Icons.DOCUMENTATION_16);
    docsMenuItem.addActionListener(
      event -> UriUtilities.openWebpage(
        URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_docs"))));

    JMenuItem javadocsMenuItem = new JMenuItem(Resources.strings().get("menu_help_javadocs"), Icons.API_16);
    javadocsMenuItem.addActionListener(
      event -> UriUtilities.openWebpage(
        URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_javadocs"))));

    JMenuItem tutorialMenuItem = new JMenuItem(Resources.strings().get("menu_help_tutorials"), Icons.TUTORIAL_16);
    tutorialMenuItem.addActionListener(
      event -> UriUtilities.openWebpage(
        URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_tutorials"))));

    JMenuItem forumMenuItem = new JMenuItem(Resources.strings().get("menu_help_forum"), Icons.FORUM_16);
    forumMenuItem.addActionListener(
      event -> UriUtilities.openWebpage(
        URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_forum"))));

    JMenuItem bugMenuItem = new JMenuItem(Resources.strings().get("menu_help_bug"), Icons.BUG_16);
    bugMenuItem.addActionListener(
      event -> UriUtilities.openWebpage(
        URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_bug"))));

    JMenuItem releaseMenuItem = new JMenuItem(Resources.strings().get("menu_help_releasenotes"), Icons.RELEASE_NOTES_16);
    releaseMenuItem.addActionListener(
      event -> UriUtilities.openWebpage(
        URI.create(Resources.strings().getFrom(LINKS, "link_LITIengine_releasenotes"))));

    JMenuItem openCollectiveMenuItem =
      new JMenuItem(Resources.strings().get("support_the_devs"), Icons.SUPPORT_16);
    openCollectiveMenuItem.addActionListener(
      event -> UriUtilities.openWebpage(
        URI.create(Resources.strings().getFrom(LINKS, "link_opencollective"))));

    JMenuItem aboutMenuItem = new JMenuItem(Resources.strings().get("menu_help_about"), Icons.ABOUT_16);
    aboutMenuItem.addActionListener(
      event -> AboutDialog.show(Game.window().getHostControl()));

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

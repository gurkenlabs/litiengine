package de.gurkenlabs.litiengine;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.xml.CustomPropertyProvider;

/**
 * This class contains basic information about a LITIengine game.
 * The information can be accessed via <code>Game.getInfo()</code> and the infrastructure also internally uses this information
 * e.g. to setup the main window of the Game by providing an appropriate title.
 * <p>
 * It should be the first thing that you do in you application entry point to setup or load this information.
 * Note that it's possible to keep this information in an XML file and load it up by calling <code>Game.setInfo(String)</code>.
 * </p>
 * 
 * @see Game#info()
 * @see Game#setInfo(String)
 */
@XmlRootElement(name = "gameinfo")
public class GameInfo extends CustomPropertyProvider {
  private static final Logger log = Logger.getLogger(GameInfo.class.getName());
  private static final long serialVersionUID = 3340166298303962177L;

  @XmlElement
  private String name;

  @XmlElement
  private String subtitle;

  @XmlElement
  private String description;

  @XmlElement
  private String website;

  @XmlElement
  private String version;

  @XmlElement
  private String company;

  @XmlElement
  private String publisher;

  @XmlElement(name = "developer")
  private String[] developers;

  public GameInfo() {
    this.company = "gurkenlabs";
    this.name = "LITIengine Game";
    this.subtitle = "The pure 2D java game engine";
    this.description = "A game, created with the allmighty LITIengine.";
    this.developers = new String[] { "Steffen Wilke", "Matthias Wilke" };
    this.version = "v1.0";
    this.website = "https://litiengine.com";
  }

  /**
   * Gets the company that created the game.
   * 
   * @return The company that created the game.
   */
  @XmlTransient
  public String getCompany() {
    return this.company;
  }

  /**
   * Gets a textual description that explains what the game is all about.
   * 
   * @return The game's description.
   */
  @XmlTransient
  public String getDescription() {
    return this.description;
  }

  /**
   * Gets the web site of this game project.
   * 
   * @return The web site of the game.
   */
  @XmlTransient
  public String getWebsite() {
    return this.website;
  }

  /**
   * Gets the {@link #getWebsite()} as an <code>URL</code> object that can be used to further process the information.
   * (e.g. the web site can be opened in the browser).
   * 
   * @return The game's web site as <code>URL</code>
   * 
   * @see URL
   * @see #getWebsite()
   */
  @XmlTransient
  public URL getWebsiteURL() {
    if (this.getWebsite() == null || this.getWebsite().isEmpty()) {
      return null;
    }

    try {
      return new URL(this.getWebsite());
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, this.getWebsite() + ": " + e.getMessage(), e);
      return null;
    }
  }

  /**
   * Gets the developers of the game. This can e.g. be used for credits.
   * 
   * @return The game's developers.
   */
  @XmlTransient
  public String[] getDevelopers() {
    return this.developers;
  }

  /**
   * Gets the name of the LITIengine game.
   * 
   * @return The game's name.
   */
  @XmlTransient
  public String getName() {
    return this.name;
  }

  /**
   * Gets the publisher of the game.
   * 
   * @return The game's publisher.
   */
  @XmlTransient
  public String getPublisher() {
    return this.publisher;
  }

  /**
   * Gets the sub title of the game. It is basically an addendum to the {@link #getName()}.
   * 
   * @return The game's sub title.
   */
  @XmlTransient
  public String getSubTitle() {
    return this.subtitle;
  }

  /**
   * Gets the version of the game.
   * 
   * @return The game's version.
   */
  @XmlTransient
  public String getVersion() {
    return this.version;
  }

  /**
   * Gets the title of the game.<br>
   * This will be used as the title of the game's window by default and includes the core information about the game:
   * <ul>
   * <li>The game's name</li>
   * <li>The game's version</li>
   * <li><i>opt. The game's subtitle</i></li>
   * </ul>
   * 
   * @return The game's title.
   * 
   * @see #getName()
   * @see #getSubTitle()
   * @see #getVersion()
   */
  public String getTitle() {
    return this.getSubTitle() != null && !this.getSubTitle().isEmpty() ? this.getName() + " " + this.getVersion() + " - " + this.getSubTitle() : this.getName() + " " + this.getVersion();
  }

  /**
   * Sets the company that created the game.
   * 
   * @param company
   *          The company that created the game.
   */
  public void setCompany(final String company) {
    this.company = company;
  }

  /**
   * Sets the game's description. This can be seen as additional information about the game and will not be part of the game's title.
   * 
   * @param description
   *          The game's description.
   * 
   * @see #getTitle()
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Sets the game's developers.
   * 
   * @param developers
   *          The game's developers.
   */
  public void setDevelopers(final String... developers) {
    this.developers = developers;
  }

  /**
   * Sets the game's name.
   * <br>
   * This is the most basic information about a game and will be part of the game's title.
   * 
   * @param name
   *          The game's name.
   * 
   * @see #getTitle()
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Sets the game's sub title.
   * <br>
   * This is basically an addendum to the game's name and will also be part of the game's title.
   * 
   * @param subTitle
   *          The game's sub title.
   * 
   * @see #getName()
   * @see #getTitle()
   */
  public void setSubTitle(final String subTitle) {
    this.subtitle = subTitle;
  }

  /**
   * Sets the game's version.
   * <br>
   * This is a textual representation of the game's version and will also be part of the game's title.<br>
   * Examples for good semantic version strings:
   * <ul>
   * <li>v0.1.0</li>
   * <li>0.1.5.2</li>
   * <li>v1.0.0-RC1</li>
   * <li>v0.0.1-alpha</li>
   * </ul>
   * 
   * @param version
   *          The game's version.
   * 
   * @see #getTitle()
   */
  public void setVersion(final String version) {
    this.version = version;
  }

  /**
   * Sets the game's web site.
   * 
   * @param website
   *          The game's web site.
   */
  public void setWebsite(final String website) {
    this.website = website;
  }
}
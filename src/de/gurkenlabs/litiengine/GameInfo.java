package de.gurkenlabs.litiengine;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.xml.CustomPropertyProvider;

@XmlRootElement(name = "gameinfo")
public class GameInfo extends CustomPropertyProvider {
  private static final Logger log = Logger.getLogger(GameInfo.class.getName());
  private static final long serialVersionUID = 3340166298303962177L;

  @XmlElement
  private String name;

  @XmlElement
  private String subTitle;

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
    this.subTitle = "The pure 2D java game engine";
    this.description = "A game, created with the allmighty LITIengine.";
    this.developers = new String[] { "Steffen Wilke", "Matthias Wilke" };
    this.version = "v1.0";
    this.website = "https://litiengine.com";
  }

  @XmlTransient
  public String getCompany() {
    return this.company;
  }

  @XmlTransient
  public String getDescription() {
    return this.description;
  }

  @XmlTransient
  public String getWebsite() {
    return this.website;
  }

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

  @XmlTransient
  public String[] getDevelopers() {
    return this.developers;
  }

  @XmlTransient
  public String getName() {
    return this.name;
  }

  @XmlTransient
  public String getPublisher() {
    return this.publisher;
  }

  @XmlTransient
  public String getSubTitle() {
    return this.subTitle;
  }

  @XmlTransient
  public String getVersion() {
    return this.version;
  }

  public void setCompany(final String company) {
    this.company = company;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setDevelopers(final String[] developers) {
    this.developers = developers;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setSubTitle(final String subTitle) {
    this.subTitle = subTitle;
  }

  public void setVersion(final String version) {
    this.version = version;
  }

  public void setWebsite(final String website) {
    this.website = website;
  }

  public String getTitle() {
    return this.getSubTitle() != null && !this.getSubTitle().isEmpty() ? this.getName() + " " + this.getVersion() + " - " + this.getSubTitle() : this.getName() + " " + this.getVersion();
  }
}
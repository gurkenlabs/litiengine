package de.gurkenlabs.litiengine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.util.io.FileUtilities;

@XmlRootElement(name = "game")
public class GameFile implements Serializable {
  public static final String FILE_EXTENSION = "env";

  private static final long serialVersionUID = -2101786184799276518L;

  public static GameFile load(final String file) {
    try {
      final JAXBContext jaxbContext = JAXBContext.newInstance(GameFile.class);
      final Unmarshaller um = jaxbContext.createUnmarshaller();

      GameFile gameFile = null;
      try {
        final GZIPInputStream zipStream = new GZIPInputStream(FileUtilities.getGameResource(file));
        gameFile = (GameFile) um.unmarshal(zipStream);
      } catch (final ZipException e) {
        InputStream stream = null;
        stream = FileUtilities.getGameResource(file);
        if (stream == null) {
          return null;
        }

        gameFile = (GameFile) um.unmarshal(stream);
      }

      if (gameFile == null) {
        return null;
      }

      for (final Map map : gameFile.getMaps()) {
        map.updateTileTerrain();
      }

      return gameFile;
    } catch (final JAXBException jaxe) {
      jaxe.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  @XmlElementWrapper(name = "maps")
  @XmlElement(name = "map")
  private final List<Map> maps;

  @XmlElementWrapper(name = "spriteFiles")
  @XmlElement(name = "spritefile")
  private String[] spriteFiles;

  @XmlElementWrapper(name = "spriteSheets")
  @XmlElement(name = "sprite")
  private List<SpriteSheetInfo> spriteSheets;

  public GameFile() {
    this.spriteSheets = new ArrayList<>();
    this.maps = new ArrayList<>();
    this.spriteFiles = new String[] {};
  }

  @XmlTransient
  public List<Map> getMaps() {
    return this.maps;
  }

  @XmlTransient
  public String[] getSpriteFiles() {
    return this.spriteFiles;
  }

  @XmlTransient
  public List<SpriteSheetInfo> getTileSets() {
    return this.spriteSheets;
  }

  public String save(String fileName, final boolean compress) {

    if (!fileName.endsWith("." + FILE_EXTENSION)) {
      fileName += "." + FILE_EXTENSION;
    }

    final File newFile = new File(fileName);
    if (newFile.exists()) {
      newFile.delete();
    }

    try {
      final JAXBContext jaxbContext = JAXBContext.newInstance(GameFile.class);
      final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      // output pretty printed
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      final OutputStream out = new FileOutputStream(newFile);
      try {
        if (compress) {
          final GZIPOutputStream stream = new GZIPOutputStream(out);
          jaxbMarshaller.marshal(this, stream);
          stream.flush();
          stream.close();
        } else {
          jaxbMarshaller.marshal(this, out);
        }
      } finally {
        out.flush();
        out.close();
      }
    } catch (final JAXBException ex) {
      ex.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }

    return newFile.toString();
  }

  public void setSpriteFiles(final String[] spriteFiles) {
    this.spriteFiles = spriteFiles;
  }

  public void setSpriteSheets(final List<SpriteSheetInfo> spriteSheets) {
    this.spriteSheets = spriteSheets;
  }
}

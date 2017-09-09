package de.gurkenlabs.litiengine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import de.gurkenlabs.util.io.XmlUtilities;

@XmlRootElement(name = "game")
public class GameFile implements Serializable {
  private static final Logger log = Logger.getLogger(GameFile.class.getName());
  public static final String FILE_EXTENSION = "env";

  private static final long serialVersionUID = -2101786184799276518L;

  @XmlElementWrapper(name = "maps")
  @XmlElement(name = "map")
  private final List<Map> maps;

  @XmlElementWrapper(name = "spriteFiles")
  @XmlElement(name = "spritefile")
  private String[] spriteFiles;

  @XmlElementWrapper(name = "spriteSheets")
  @XmlElement(name = "sprite")
  private List<SpriteSheetInfo> tilesets;

  public GameFile() {
    this.tilesets = new ArrayList<>();
    this.maps = new ArrayList<>();
    this.spriteFiles = new String[] {};
  }

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
    } catch (final JAXBException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return null;
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
    return this.tilesets;
  }

  public String save(String fileName, final boolean compress) {

    if (!fileName.endsWith("." + FILE_EXTENSION)) {
      fileName += "." + FILE_EXTENSION;
    }

    final File newFile = new File(fileName);
    if (newFile.exists()) {
      newFile.delete();
    }

    Collections.sort(this.getMaps());

    try {
      final JAXBContext jaxbContext = JAXBContext.newInstance(GameFile.class);
      final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      // output pretty printed
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

      final FileOutputStream fileOut = new FileOutputStream(newFile);
      if (compress) {
        final GZIPOutputStream stream = new GZIPOutputStream(fileOut);
        jaxbMarshaller.marshal(this, stream);
        stream.flush();
        stream.close();
      } else {

        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // first: marshal to byte array
        jaxbMarshaller.marshal(this, out);
        out.flush();

        // second: postprocess xml and then write it to the file
        XmlUtilities.saveWithCustomIndetation(new ByteArrayInputStream(out.toByteArray()), fileOut, 1);
        out.close();
      }
    } catch (final JAXBException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return newFile.toString();
  }

  public void setSpriteFiles(final String[] spriteFiles) {
    this.spriteFiles = spriteFiles;
  }

  public void setTileSets(final List<SpriteSheetInfo> tileSets) {
    this.tilesets = tileSets;
  }

  void beforeMarshal(Marshaller m) {
    List<SpriteSheetInfo> duplicates = new ArrayList<>();
    for (SpriteSheetInfo sprite : this.getTileSets()) {
      if (this.getTileSets().stream().anyMatch(x -> !x.equals(sprite) && x.getName().equals(sprite.getName()) && x.getImage().equals(sprite.getImage()))) {
        duplicates.add(sprite);
      }
    }

    this.tilesets.removeAll(duplicates);
  }
}

package de.gurkenlabs.litiengine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
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

import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.particles.xml.EmitterData;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

@XmlRootElement(name = "game")
public class GameFile implements Serializable {
  private static final Logger log = Logger.getLogger(GameFile.class.getName());
  public static final String FILE_EXTENSION = "ltx";

  private static final long serialVersionUID = -2101786184799276518L;

  @XmlElementWrapper(name = "maps")
  @XmlElement(name = "map")
  private List<Map> maps;

  @XmlElementWrapper(name = "spriteSheets")
  @XmlElement(name = "sprite")
  private List<SpriteSheetInfo> spriteSheets;

  @XmlElementWrapper(name = "tilesets")
  @XmlElement(name = "tileset")
  private List<Tileset> tilesets;

  @XmlElementWrapper(name = "emitters")
  @XmlElement(name = "emitter")
  private List<EmitterData> emitters;

  @XmlElementWrapper(name = "blueprints")
  @XmlElement(name = "blueprint")
  private List<Blueprint> blueprints;

  public GameFile() {
    this.spriteSheets = new ArrayList<>();
    this.maps = new ArrayList<>();
    this.tilesets = new ArrayList<>();
    this.emitters = new ArrayList<>();
    this.blueprints = new ArrayList<>();
  }

  public static GameFile load(final String file) {
    try {
      GameFile gameFile = getGameFileFromFile(file);
      if (gameFile == null) {
        return null;
      }

      gameFile.getMaps().parallelStream().forEach(map -> {
        map.updateTileTerrain();
        for (final Tileset tileset : map.getRawTileSets()) {
          tileset.load(gameFile.getTilesets());
        }
      });

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
  public List<SpriteSheetInfo> getSpriteSheets() {
    return this.spriteSheets;
  }

  @XmlTransient
  public List<Tileset> getTilesets() {
    return this.tilesets;
  }

  @XmlTransient
  public List<EmitterData> getEmitters() {
    return this.emitters;
  }

  @XmlTransient
  public List<Blueprint> getBluePrints() {
    return this.blueprints;
  }

  public String save(final String fileName, final boolean compress) {
    String fileNameWithExtension = fileName;
    if (!fileNameWithExtension.endsWith("." + FILE_EXTENSION)) {
      fileNameWithExtension += "." + FILE_EXTENSION;
    }

    final File newFile = new File(fileNameWithExtension);
    if (newFile.exists()) {
      try {
        Files.delete(newFile.toPath().toAbsolutePath());
      } catch (IOException e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
    }

    Collections.sort(this.getMaps());

    try (FileOutputStream fileOut = new FileOutputStream(newFile, false)) {
      final JAXBContext jaxbContext = XmlUtilities.getContext(GameFile.class);
      final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      // output pretty printed
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

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

  private static GameFile getGameFileFromFile(String file) throws JAXBException, IOException {
    final JAXBContext jaxbContext = XmlUtilities.getContext(GameFile.class);
    final Unmarshaller um = jaxbContext.createUnmarshaller();
    try (InputStream inputStream = FileUtilities.getGameResource(file)) {

      // try to get compressed game file
      final GZIPInputStream zipStream = new GZIPInputStream(inputStream);
      return (GameFile) um.unmarshal(zipStream);
    } catch (final ZipException e) {

      // if it fails to load the compressed file, get it from plain XML
      return XmlUtilities.readFromFile(GameFile.class, file);
    }
  }

  void beforeMarshal(Marshaller m) {
    List<SpriteSheetInfo> distinctList = new ArrayList<>();
    for (SpriteSheetInfo sprite : this.getSpriteSheets()) {
      if (distinctList.stream().anyMatch(x -> x.getName().equals(sprite.getName()) && x.getImage().equals(sprite.getImage()))) {
        continue;
      }

      distinctList.add(sprite);
    }

    this.spriteSheets = distinctList;

    List<Tileset> distinctTilesets = new ArrayList<>();
    for (Tileset tileset : this.getTilesets()) {
      if (distinctTilesets.stream().anyMatch(x -> x.getName().equals(tileset.getName()))) {
        continue;
      }

      distinctTilesets.add(tileset);
    }

    this.tilesets = distinctTilesets;
  }
}

package de.gurkenlabs.litiengine.resources;

import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

/**
 * Represents a resource bundle that can be serialized. This class is used to manage various game resources such as maps, sprite sheets, tilesets,
 * emitters, blueprints, and sounds.
 */
@XmlRootElement(name = "litidata")
public class ResourceBundle implements Serializable {
  private static final Logger log = Logger.getLogger(ResourceBundle.class.getName());
  public static final String FILE_EXTENSION = "litidata";
  public static final float CURRENT_VERSION = 1.0f;

  @Serial private static final long serialVersionUID = -2101786184799276518L;

  @XmlAttribute(name = "version")
  private float version;

  @XmlElementWrapper(name = "maps")
  @XmlElement(name = "map")
  private final List<TmxMap> maps;

  @XmlElementWrapper(name = "spriteSheets")
  @XmlElement(name = "sprite")
  private List<SpritesheetResource> spriteSheets;

  @XmlElementWrapper(name = "tilesets")
  @XmlElement(name = "tileset")
  private List<Tileset> tilesets;

  @XmlElementWrapper(name = "emitters")
  @XmlElement(name = "emitter")
  private final List<EmitterData> emitters;

  @XmlElementWrapper(name = "blueprints")
  @XmlElement(name = "blueprint")
  private final List<Blueprint> blueprints;

  @XmlElementWrapper(name = "sounds")
  @XmlElement(name = "sound")
  private final List<SoundResource> sounds;

  /**
   * Constructs a new ResourceBundle instance. Initializes the lists for sprite sheets, maps, tilesets, emitters, blueprints, and sounds.
   */
  public ResourceBundle() {
    this.spriteSheets = new ArrayList<>();
    this.maps = new ArrayList<>();
    this.tilesets = new ArrayList<>();
    this.emitters = new ArrayList<>();
    this.blueprints = new ArrayList<>();
    this.sounds = new ArrayList<>();
  }

  /**
   * Loads a ResourceBundle from a file path.
   *
   * @param file The file path to load the ResourceBundle from.
   * @return The loaded ResourceBundle, or null if loading fails.
   */
  public static ResourceBundle load(String file) {
    return load(Resources.getLocation(file));
  }

  /**
   * Loads a ResourceBundle from a URL.
   *
   * @param file The URL to load the ResourceBundle from.
   * @return The loaded ResourceBundle, or null if loading fails.
   */
  public static ResourceBundle load(final URL file) {
    try {
      ResourceBundle gameFile = getResourceBundle(file);
      if (gameFile == null) {
        return null;
      }

      for (Tileset tileset : gameFile.getTilesets()) {
        tileset.finish(file);
      }

      for (TmxMap map : gameFile.getMaps()) {
        for (final ITileset tileset : map.getTilesets()) {
          if (tileset instanceof Tileset ts) {
            ts.load(gameFile.getTilesets());
          }
        }
        map.finish(file);
      }

      return gameFile;
    } catch (final JAXBException | IOException e) {
      log.log(Level.SEVERE, "{} - {}", new Object[] {file, e.getMessage()});
    }

    return null;
  }

  /**
   * Gets the list of maps in this resource bundle.
   *
   * @return The list of maps.
   */
  @XmlTransient
  public List<TmxMap> getMaps() {
    return this.maps;
  }

  /**
   * Gets the list of sprite sheets in this resource bundle.
   *
   * @return The list of sprite sheets.
   */
  @XmlTransient
  public List<SpritesheetResource> getSpriteSheets() {
    return this.spriteSheets;
  }

  /**
   * Gets the list of tilesets in this resource bundle.
   *
   * @return The list of tilesets.
   */
  @XmlTransient
  public List<Tileset> getTilesets() {
    return this.tilesets;
  }

  /**
   * Gets the list of emitters in this resource bundle.
   *
   * @return The list of emitters.
   */
  @XmlTransient
  public List<EmitterData> getEmitters() {
    return this.emitters;
  }

  /**
   * Gets the list of blueprints in this resource bundle.
   *
   * @return The list of blueprints.
   */
  @XmlTransient
  public List<Blueprint> getBluePrints() {
    return this.blueprints;
  }

  /**
   * Gets the list of sounds in this resource bundle.
   *
   * @return The list of sounds.
   */
  @XmlTransient
  public List<SoundResource> getSounds() {
    return this.sounds;
  }

  /**
   * Saves the ResourceBundle to a file.
   *
   * @param fileName The name of the file to save the ResourceBundle to.
   * @param compress Whether to compress the file or not.
   * @return The path of the saved file as a string.
   */
  public String save(final String fileName, final boolean compress) {
    String fileNameWithExtension = fileName;
    if (!fileNameWithExtension.endsWith("." + FILE_EXTENSION)) {
      fileNameWithExtension += "." + FILE_EXTENSION;
    }

    final Path newFile = Path.of(fileNameWithExtension);
    if (!Files.exists(newFile)) {
      try {
        Files.delete(newFile);
      } catch (IOException e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
    }

    Collections.sort(this.getMaps());
    Collections.sort(this.getSpriteSheets());
    Collections.sort(this.getTilesets());
    Collections.sort(this.getEmitters());
    Collections.sort(this.getBluePrints());
    Collections.sort(this.getSounds());

    try (OutputStream fileOut = Files.newOutputStream(newFile, StandardOpenOption.CREATE_NEW)) {
      final JAXBContext jaxbContext = XmlUtilities.getContext(ResourceBundle.class);
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
        XmlUtilities.saveWithCustomIndentation(new ByteArrayInputStream(out.toByteArray()), fileOut, 1);
        out.close();
      }
    } catch (final JAXBException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return newFile.toString();
  }

  /**
   * Prepares the ResourceBundle for marshalling. This method ensures that the lists of sprite sheets and tilesets contain only distinct elements. It
   * also sets the version of the ResourceBundle if it is not already set.
   *
   * @param m The Marshaller instance used for marshalling.
   */
  void beforeMarshal(Marshaller m) {
    List<SpritesheetResource> distinctList = new ArrayList<>();
    for (SpritesheetResource sprite : this.getSpriteSheets()) {
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

    if (this.version == 0) {
      this.version = CURRENT_VERSION;
    }
  }

  /**
   * Retrieves a ResourceBundle from a specified URL. This method attempts to load the ResourceBundle from a compressed GZIP stream first. If that
   * fails, it falls back to loading from plain XML.
   *
   * @param file The URL of the resource bundle file.
   * @return The loaded ResourceBundle.
   * @throws JAXBException If an error occurs during the unmarshalling process.
   * @throws IOException   If an I/O error occurs.
   */
  private static ResourceBundle getResourceBundle(URL file) throws JAXBException, IOException {
    final JAXBContext jaxbContext = XmlUtilities.getContext(ResourceBundle.class);
    final Unmarshaller um = jaxbContext.createUnmarshaller();
    try (InputStream inputStream = Resources.get(file)) {

      // try to get compressed game file
      final GZIPInputStream zipStream = new GZIPInputStream(inputStream);
      return (ResourceBundle) um.unmarshal(zipStream);
    } catch (final ZipException e) {

      // if it fails to load the compressed file, get it from plain XML
      return XmlUtilities.read(ResourceBundle.class, file);
    }
  }
}

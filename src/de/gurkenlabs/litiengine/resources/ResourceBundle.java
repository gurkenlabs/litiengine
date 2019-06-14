package de.gurkenlabs.litiengine.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterData;
import de.gurkenlabs.litiengine.util.io.URLAdapter;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;

@XmlRootElement(name = "litidata")
public class ResourceBundle implements Serializable {
  private static final Logger log = Logger.getLogger(ResourceBundle.class.getName());
  public static final String FILE_EXTENSION = "litidata";
  public static final float CURRENT_VERSION = 1.0f;

  private static final long serialVersionUID = -2101786184799276518L;

  @XmlAttribute(name = "version")
  private float version;

  @XmlElementWrapper(name = "maps")
  @XmlElement(name = "map")
  private List<TmxMap> maps;

  @XmlElementWrapper(name = "spriteSheets")
  @XmlElement(name = "sprite")
  private List<SpritesheetResource> spriteSheets;

  @XmlElementWrapper(name = "tilesets")
  @XmlElement(name = "tileset")
  private List<Tileset> tilesets;

  @XmlElementWrapper(name = "emitters")
  @XmlElement(name = "emitter")
  private List<EmitterData> emitters;

  @XmlElementWrapper(name = "blueprints")
  @XmlElement(name = "blueprint")
  private List<Blueprint> blueprints;

  @XmlElementWrapper(name = "sounds")
  @XmlElement(name = "sound")
  private List<SoundResource> sounds;

  public ResourceBundle() {
    this.spriteSheets = new ArrayList<>();
    this.maps = new ArrayList<>();
    this.tilesets = new ArrayList<>();
    this.emitters = new ArrayList<>();
    this.blueprints = new ArrayList<>();
    this.sounds = new ArrayList<>();
  }

  public static ResourceBundle load(String file) {
    return load(Resources.getLocation(file));
  }

  public static ResourceBundle load(final URL file) {
    try {
      ResourceBundle gameFile = getGameFileFromFile(file);
      if (gameFile == null) {
        return null;
      }

      for (SpritesheetResource res : gameFile.getSpriteSheets()) {
        Resources.images().add(new URL(file, '#' + res.getName()), Resources.spritesheets().load(res).getImage());
      }

      for (SoundResource res : gameFile.getSounds()) {
        Resources.sounds().add(new URL(file, '#' + res.getName()), Resources.sounds().load(res));
      }

      for (Tileset tileset : gameFile.getTilesets()) {
        tileset.finish(file);
        Resources.tilesets().add(new URL(file, '#' + tileset.getName()), tileset);
      }

      for (TmxMap map : gameFile.getMaps()) {
        map.finish(file);
        Resources.maps().add(new URL(file, '#' + map.getName()), map);
      }

      return gameFile;
    } catch (final JAXBException | IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return null;
  }

  @XmlTransient
  public List<TmxMap> getMaps() {
    return this.maps;
  }

  @XmlTransient
  public List<SpritesheetResource> getSpriteSheets() {
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
  
  @XmlTransient
  public List<SoundResource> getSounds() {
    return this.sounds;
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

    Collections.sort(this.getMaps(), Resource.BY_NAME);
    Collections.sort(this.getSpriteSheets(), Resource.BY_NAME);
    Collections.sort(this.getTilesets(), Resource.BY_NAME);
    Collections.sort(this.getEmitters(), Resource.BY_NAME);
    Collections.sort(this.getBluePrints(), Resource.BY_NAME);
    Collections.sort(this.getSounds(), Resource.BY_NAME);

    try (FileOutputStream fileOut = new FileOutputStream(newFile, false)) {
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

  private static ResourceBundle getGameFileFromFile(URL file) throws JAXBException, IOException {
    final JAXBContext jaxbContext = XmlUtilities.getContext(ResourceBundle.class);
    final Unmarshaller um = jaxbContext.createUnmarshaller();
    um.setAdapter(URLAdapter.class, new URLAdapter(file) {
      // TODO: come the beta release, this can be removed
      @Override
      public URL unmarshal(String v) throws MalformedURLException {
        if (v.indexOf('/') == -1 && v.indexOf('\\') == -1 && v.indexOf('.') == -1 && !v.startsWith("#")) {
          log.warning("Could not safely determine whether the reference \"" + v + "\" was referring to another resource in the bundle or a separate file.\nPrepend \"./\" or \"#\" to clear this ambiguity.");
          v = '#' + v;
        }
        return super.unmarshal(v);
      }
    });
    try (InputStream inputStream = Resources.get(file)) {

      // try to get compressed game file
      final GZIPInputStream zipStream = new GZIPInputStream(inputStream);
      return (ResourceBundle) um.unmarshal(zipStream);
    } catch (final ZipException e) {

      // if it fails to load the compressed file, get it from plain XML
      return XmlUtilities.readFromFile(ResourceBundle.class, file);
    }
  }

  @SuppressWarnings("unused")
  private void beforeMarshal(Marshaller m) {
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
}

package de.gurkenlabs.litiengine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

import javax.print.attribute.standard.Compression;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.tilemap.IMap;
import de.gurkenlabs.tilemap.xml.Map;
import de.gurkenlabs.util.io.FileUtilities;
import de.gurkenlabs.util.zip.CompressionUtilities;

@XmlRootElement(name = "game")
public class GameFile implements Serializable {
  public static final String FILE_EXTENSION = "env";

  private static final long serialVersionUID = -2101786184799276518L;

  @XmlElementWrapper(name = "maps")
  @XmlElement(name = "map")
  private List<Map> maps;

  @XmlElementWrapper(name = "spriteSheets")
  @XmlElement(name = "sprite")
  private List<SpriteSheetInfo> spriteSheets;

  public GameFile() {
    this.spriteSheets = new ArrayList<>();
    this.maps = new ArrayList<>();
  }

  @XmlTransient
  public List<Map> getMaps() {
    return maps;
  }

  @XmlTransient
  public List<SpriteSheetInfo> getSpriteSheets() {
    return spriteSheets;
  }

  public void setSpriteSheets(List<SpriteSheetInfo> spriteSheets) {
    this.spriteSheets = spriteSheets;
  }

  public static GameFile load(String file) {
    try {
      final JAXBContext jaxbContext = JAXBContext.newInstance(GameFile.class);
      final Unmarshaller um = jaxbContext.createUnmarshaller();

      GameFile gameFile = null;
      try {
        GZIPInputStream zipStream = new GZIPInputStream(new FileInputStream(file));
        gameFile = (GameFile) um.unmarshal(zipStream);
      } catch (ZipException e) {
        InputStream stream = null;
        stream = FileUtilities.getGameFile(file);
        if (stream == null) {
          return null;
        }

        gameFile = (GameFile) um.unmarshal(stream);
      }

      if (gameFile == null) {
        return null;
      }

      for (Map map : gameFile.getMaps()) {
        map.updateTileTerrain();
      }

      return gameFile;
    } catch (final JAXBException jaxe) {
      jaxe.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public String save(String fileName, boolean compress) {
    
    if(!fileName.endsWith("." + FILE_EXTENSION)){
      fileName +="." + FILE_EXTENSION;
    }
    
    File newFile = new File(fileName);

    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(GameFile.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      // output pretty printed
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      OutputStream out = new FileOutputStream(newFile);
      try {
        if (compress) {
          GZIPOutputStream stream = new GZIPOutputStream(out);
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
    } catch (JAXBException ex) {
      ex.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    return newFile.toString();
  }
}

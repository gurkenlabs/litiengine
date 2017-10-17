package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "data")
public class TileData {
  protected static final String ENCODING_BASE64 = "base64";
  protected static final String ENCODING_CSV = "csv";
  protected static final String COMPRESSION_GZIP = "gzip";
  protected static final String COMPRESSION_ZLIB = "zlib";
  private static final Logger log = Logger.getLogger(TileData.class.getName());

  @XmlAttribute
  private String encoding;

  @XmlAttribute
  private String compression;

  @XmlValue
  private String value;

  @XmlTransient
  private List<Tile> parsedTiles;

  @XmlTransient
  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  @XmlTransient
  public String getCompression() {
    return compression;
  }

  public void setCompression(String compression) {
    this.compression = compression;
  }

  @XmlTransient
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  protected List<Tile> parseTiles() {
    if (this.parsedTiles != null) {
      return this.parsedTiles;
    }

    if (this.getEncoding() == null || this.getEncoding().isEmpty()) {
      return new ArrayList<>();
    }

    if (this.getEncoding().equals(ENCODING_BASE64)) {
      this.parsedTiles = this.parseBase64Data();
    } else if (this.getEncoding().equals(ENCODING_CSV)) {
      this.parsedTiles = this.parseCsvData();
    } else {
      throw new IllegalArgumentException("Unsupported tile layer encoding " + this.getEncoding());
    }

    return this.parsedTiles;
  }

  protected List<Tile> parseBase64Data() {
    List<Tile> parsed = new ArrayList<>();

    String enc = this.value.trim();
    byte[] dec = DatatypeConverter.parseBase64Binary(enc);
    try (ByteArrayInputStream bais = new ByteArrayInputStream(dec)) {
      InputStream is;

      if (this.getCompression() == null || this.getCompression().isEmpty()) {
        is = bais;
      } else if (this.getCompression().equals(COMPRESSION_GZIP)) {
        is = new GZIPInputStream(bais, dec.length);
      } else if (this.getCompression().equals(COMPRESSION_ZLIB)) {
        is = new InflaterInputStream(bais);
      } else {
        throw new IllegalArgumentException("Unsupported tile layer compression method" + this.getCompression());
      }

      int read;

      while ((read = is.read()) != -1) {
        long tileId = 0;
        tileId |= read;

        read = is.read();
        long flags = read << Byte.SIZE;

        read = is.read();
        flags |= read << Byte.SIZE * 2;

        read = is.read();
        flags |= read << Byte.SIZE * 3;
        tileId |= flags;

        parsed.add(new Tile(tileId, false));
      }

    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return parsed;
  }

  protected List<Tile> parseCsvData() {

    List<Tile> parsed = new ArrayList<>();

    // trim 'space', 'tab', 'newline'. pay attention to additional unicode chars
    // like \u2028, \u2029, \u0085 if necessary
    String[] csvTileIds = this.value.trim().split("[\\s]*,[\\s]*");

    for (String gid : csvTileIds) {
      long tileId = Long.parseLong(gid);

      if (tileId > Integer.MAX_VALUE) {
        parsed.add(new Tile(tileId, true));
      } else {
        parsed.add(new Tile((int) tileId));
      }
    }

    return parsed;
  }
}
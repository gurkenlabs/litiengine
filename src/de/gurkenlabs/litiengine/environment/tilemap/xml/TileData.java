package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlMixed;
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

  @XmlMixed
  @XmlElementRef(type = TileChunk.class, name = "chunk")
  private List<Object> rawValue;

  @XmlTransient
  private String value;

  private transient List<TileChunk> chunks;

  @XmlTransient
  private List<Tile> parsedTiles;

  @XmlTransient
  private int width;

  @XmlTransient
  private int height;

  @XmlTransient
  public String getEncoding() {
    return encoding;
  }

  @XmlTransient
  public String getCompression() {
    return compression;
  }

  @XmlTransient
  public String getValue() {
    return value;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setCompression(String compression) {
    this.compression = compression;
  }

  public void setValue(String value) {
    this.value = value;
  }

  protected boolean isInfinite() {
    return this.chunks != null && !this.chunks.isEmpty();
  }

  protected int getWidth() {
    return this.width;
  }

  protected int getHeight() {
    return this.height;
  }

  protected List<Tile> parseTiles() {
    if (this.parsedTiles != null) {
      return this.parsedTiles;
    }

    if (this.getEncoding() == null || this.getEncoding().isEmpty()) {
      return new ArrayList<>();
    }

    if (this.isInfinite()) {
      this.parsedTiles = this.parseChunkData();
    } else {
      this.parsedTiles = this.parseData();
    }

    return this.parsedTiles;
  }

  protected static List<Tile> parseBase64Data(String value, String compression) {
    List<Tile> parsed = new ArrayList<>();

    String enc = value.trim();
    byte[] dec = DatatypeConverter.parseBase64Binary(enc);
    try (ByteArrayInputStream bais = new ByteArrayInputStream(dec)) {
      InputStream is;

      if (compression == null || compression.isEmpty()) {
        is = bais;
      } else if (compression.equals(COMPRESSION_GZIP)) {
        is = new GZIPInputStream(bais, dec.length);
      } else if (compression.equals(COMPRESSION_ZLIB)) {
        is = new InflaterInputStream(bais);
      } else {
        throw new IllegalArgumentException("Unsupported tile layer compression method" + compression);
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

        if (tileId == Tile.NONE) {
          parsed.add(Tile.EMPTY);
        } else {
          parsed.add(new Tile(tileId, false));
        }
      }

    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }

    return parsed;
  }

  protected static List<Tile> parseCsvData(String value) {

    List<Tile> parsed = new ArrayList<>();

    // trim 'space', 'tab', 'newline'. pay attention to additional unicode chars
    // like \u2028, \u2029, \u0085 if necessary
    String[] csvTileIds = value.trim().split("[\\s]*,[\\s]*");

    for (String gid : csvTileIds) {
      long tileId = Long.parseLong(gid);

      if (tileId > Integer.MAX_VALUE) {
        parsed.add(new Tile(tileId, true));
      } else {
        if (tileId == Tile.NONE) {
          parsed.add(Tile.EMPTY);
        } else {
          parsed.add(new Tile((int) tileId));
        }
      }
    }

    return parsed;
  }

  void afterUnmarshal(Unmarshaller u, Object parent) {
    this.processRawData();

    if (this.isInfinite()) {
      // make sure that the chunks are organized top-left to bottom right
      // this is important for their data to be parsed in the right order
      Collections.sort(this.chunks);

      this.updateDimensionsByTileData();
    }
  }

  private void processRawData() {
    if (this.rawValue == null || this.rawValue.isEmpty()) {
      return;
    }

    List<TileChunk> rawChunks = new ArrayList<>();
    String v = null;
    for (Object val : this.rawValue) {
      if (val instanceof String) {
        String trimmedValue = ((String) val).trim();
        if (!trimmedValue.isEmpty()) {
          v = trimmedValue;
        }
      }

      if (val instanceof TileChunk) {
        rawChunks.add((TileChunk) val);
      }
    }

    if (rawChunks.isEmpty()) {
      this.value = v;
      return;
    }

    this.chunks = rawChunks;
  }

  private void updateDimensionsByTileData() {
    int minX = 0;
    int maxX = 0;
    int minY = 0;
    int maxY = 0;
    int maxChunkWidth = 0;
    int maxChunkHeight = 0;

    for (TileChunk chunk : this.chunks) {
      if (chunk.getX() < minX) {
        minX = chunk.getX();
      }

      if (chunk.getY() < minY) {
        minY = chunk.getY();
      }

      if (chunk.getX() + chunk.getWidth() > maxX) {
        maxX = chunk.getX();
        maxChunkWidth = chunk.getWidth();
      }

      if (chunk.getY() + chunk.getHeight() > maxY) {
        maxY = chunk.getY();
        maxChunkHeight = chunk.getHeight();
      }
    }

    this.width = (maxX + maxChunkWidth) - minX;
    this.height = (maxY + maxChunkHeight) - minY;
  }

  private List<Tile> parseChunkData() {
    List<Tile> tiles = new ArrayList<>();
    if (this.getEncoding().equals(ENCODING_BASE64)) {
      for (TileChunk chunk : this.chunks) {
        tiles.addAll(parseBase64Data(chunk.getValue(), this.compression));
      }
    } else if (this.getEncoding().equals(ENCODING_CSV)) {
      for (TileChunk chunk : this.chunks) {
        tiles.addAll(parseCsvData(chunk.getValue()));
      }
    } else {
      throw new IllegalArgumentException("Unsupported tile layer encoding " + this.getEncoding());
    }

    return tiles;
  }

  private List<Tile> parseData() {
    List<Tile> tiles;
    if (this.getEncoding().equals(ENCODING_BASE64)) {
      tiles = parseBase64Data(this.value, this.compression);
    } else if (this.getEncoding().equals(ENCODING_CSV)) {
      tiles = parseCsvData(this.value);
    } else {
      throw new IllegalArgumentException("Unsupported tile layer encoding " + this.getEncoding());
    }

    return tiles;
  }
}
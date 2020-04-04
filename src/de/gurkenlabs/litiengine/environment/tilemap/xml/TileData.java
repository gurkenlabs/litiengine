package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.util.ArrayUtilities;
import de.gurkenlabs.litiengine.util.io.Codec;

public class TileData {
  private static final Logger log = Logger.getLogger(TileData.class.getName());

  public static class Encoding {
    public static final String BASE64 = "base64";
    public static final String CSV = "csv";

    private Encoding() {
    }

    public static boolean isValid(String encoding) {
      return encoding != null && !encoding.isEmpty() && (encoding.equals(BASE64) || encoding.equals(CSV));
    }
  }

  public static class Compression {
    public static final String GZIP = "gzip";
    public static final String ZLIB = "zlib";
    public static final String NONE = null;

    private Compression() {
    }

    public static boolean isValid(String compression) {
      // null equals no compression which is an accepted value
      return compression == null || !compression.isEmpty() && (compression.equals(GZIP) || compression.equals(ZLIB));
    }
  }

  @XmlAttribute
  private String encoding;

  @XmlAttribute
  private String compression;

  @XmlMixed
  @XmlElementRef(type = TileChunk.class, name = "chunk")
  private List<Object> rawValue;

  @XmlTransient
  private String value;

  @XmlTransient
  private List<TileChunk> chunks;

  @XmlTransient
  private List<Tile> tiles;

  @XmlTransient
  private int width;

  @XmlTransient
  private int height;

  @XmlTransient
  private int offsetX;

  @XmlTransient
  private int offsetY;

  @XmlTransient
  private int minChunkOffsetXMap;

  @XmlTransient
  private int minChunkOffsetYMap;

  /**
   * Instantiates a new <code>TileData</code> instance.
   */
  public TileData() {
    // keep for serialization
  }

  public TileData(List<Tile> tiles, int width, int height, String encoding, String compression) throws TmxException {
    if (!Encoding.isValid(encoding)) {
      throw new TmxException("Invalid tile data encoding '" + encoding + "'. Supported encodings are " + Encoding.CSV + " and " + Encoding.BASE64 + ".");
    }

    if (!Compression.isValid(compression)) {
      throw new TmxException("Invalid tile data compression '" + compression + "'. Supported compressions are " + Compression.GZIP + " and " + Compression.ZLIB + ".");
    }

    this.tiles = tiles;
    this.encoding = encoding;
    this.compression = compression;
    this.width = width;
    this.height = height;
  }

  @XmlTransient
  public String getEncoding() {
    return this.encoding;
  }

  @XmlTransient
  public String getCompression() {
    return this.compression;
  }

  @XmlTransient
  public String getValue() {
    return this.value;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setCompression(String compression) {
    this.compression = compression;
  }

  public void setValue(String value) {
    this.value = value;
    if (this.rawValue == null) {
      this.rawValue = new CopyOnWriteArrayList<>();
    }

    this.rawValue.add(0, value);
  }

  public List<Tile> getTiles() {
    if (this.tiles != null) {
      return this.tiles;
    }

    if (this.getEncoding() == null || this.getEncoding().isEmpty()) {
      return new ArrayList<>();
    }

    try {
      if (this.isInfinite()) {
        this.tiles = this.parseChunkData();
      } else {
        this.tiles = this.parseData();
      }
    } catch (InvalidTileLayerException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      return new ArrayList<>();
    }

    return this.tiles;
  }

  public static String encode(TileData data) throws IOException {
    if (data.getEncoding() == null) {
      return null;
    }

    if (data.getEncoding().equals(Encoding.CSV)) {
      return encodeCsv(data);
    } else if (data.getEncoding().equals(Encoding.BASE64)) {
      return encodeBase64(data);
    }

    return null;
  }

  private static String encodeCsv(TileData data) {
    StringBuilder sb = new StringBuilder();
    if (!data.getTiles().isEmpty()) {
      sb.append('\n');
    }

    for (int i = 0; i < data.getTiles().size(); i++) {
      sb.append(data.getTiles().get(i).getGridId());

      if (i < data.getTiles().size() - 1) {
        sb.append(',');
      }

      if (i != 0 && (i + 1) % data.getWidth() == 0) {
        sb.append('\n');
      }
    }

    return sb.toString();
  }

  private static String encodeBase64(TileData data) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      OutputStream out = baos;

      if (data.getCompression() != null && Compression.isValid(data.getCompression())) {
        if (data.getCompression().equals(Compression.GZIP)) {
          out = new GZIPOutputStream(baos);
        } else if (data.getCompression().equals(Compression.ZLIB)) {
          out = new DeflaterOutputStream(baos);
        }
      }

      for (Tile tile : data.getTiles()) {
        int gid = 0;

        if (tile != null) {
          gid = tile.getGridId();
        }

        out.write(gid);
        out.write(gid >> Byte.SIZE);
        out.write(gid >> Byte.SIZE * 2);
        out.write(gid >> Byte.SIZE * 3);
      }

      if (data.getCompression() != null && data.getCompression().equals(Compression.GZIP)) {
        ((GZIPOutputStream) out).finish();
      }

      if (data.getCompression() != null && data.getCompression().equals(Compression.ZLIB)) {
        ((DeflaterOutputStream) out).finish();
      }

      return Codec.encode(baos.toByteArray());
    }
  }

  protected void setMinChunkOffsets(int x, int y) {
    this.minChunkOffsetXMap = x;
    this.minChunkOffsetYMap = y;
  }

  protected boolean isInfinite() {
    return this.chunks != null && !this.chunks.isEmpty();
  }

  protected int getWidth() {
    if (this.isInfinite() && this.minChunkOffsetXMap != 0) {
      return this.width + (this.offsetX - this.minChunkOffsetXMap);
    }

    return this.width;
  }

  protected int getHeight() {
    if (this.isInfinite() && this.minChunkOffsetYMap != 0) {
      return this.height + (this.offsetY - this.minChunkOffsetYMap);
    }

    return this.height;
  }

  protected int getOffsetX() {
    return this.offsetX;
  }

  protected int getOffsetY() {
    return this.offsetY;
  }

  protected static List<Tile> parseBase64Data(String value, String compression) throws InvalidTileLayerException {
    List<Tile> parsed = new ArrayList<>();

    String enc = value.trim();
    byte[] dec;
    try {
      dec = DatatypeConverter.parseBase64Binary(enc);
    } catch (IllegalArgumentException e) {
      throw new InvalidTileLayerException("invalid base64 string", e);
    }
    try (ByteArrayInputStream bais = new ByteArrayInputStream(dec)) {
      InputStream is;

      if (compression == null || compression.isEmpty()) {
        is = bais;
      } else if (compression.equals(Compression.GZIP)) {
        is = new GZIPInputStream(bais, dec.length);
      } else if (compression.equals(Compression.ZLIB)) {
        is = new InflaterInputStream(bais);
      } else {
        throw new IllegalArgumentException("Unsupported tile layer compression method " + compression);
      }

      int read;

      while ((read = is.read()) != -1) {
        int tileId = 0;
        tileId |= read;

        read = is.read();
        int flags = read << Byte.SIZE;

        read = is.read();
        flags |= read << Byte.SIZE * 2;

        read = is.read();
        flags |= read << Byte.SIZE * 3;
        tileId |= flags;

        if (tileId == Tile.NONE) {
          parsed.add(Tile.EMPTY);
        } else {
          parsed.add(new Tile(tileId));
        }
      }

    } catch (IOException e) {
      throw new InvalidTileLayerException(e);
    }

    return parsed;
  }

  protected static List<Tile> parseCsvData(String value) throws InvalidTileLayerException {

    List<Tile> parsed = new ArrayList<>();

    // trim 'space', 'tab', 'newline'. pay attention to additional unicode chars
    // like \u2028, \u2029, \u0085 if necessary
    String[] csvTileIds = value.trim().split("[\\s]*,[\\s]*");

    for (String gid : csvTileIds) {
      int tileId;
      try {
        tileId = Integer.parseUnsignedInt(gid);
      } catch (NumberFormatException e) {
        throw new InvalidTileLayerException(e);
      }

      if (tileId > Integer.MAX_VALUE) {
        parsed.add(new Tile(tileId));
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
    this.processMixedData();

    if (this.isInfinite()) {
      // make sure that the chunks are organized top-left to bottom right
      // this is important for their data to be parsed in the right order
      Collections.sort(this.chunks);

      this.updateDimensionsByTileData();
    }
  }

  /**
   * This method processes the {@link XmlMixed} contents that were unmarshalled and extract either the string value containing the information
   * about the layer of a set of {@link TileChunk}s if the map is infinite.
   */
  private void processMixedData() {
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

  /**
   * For infinite maps, the size of a tile layer depends on the <code>TileChunks</code> it contains.
   */
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

    this.offsetX = minX;
    this.offsetY = minY;
  }

  private List<Tile> parseChunkData() throws InvalidTileLayerException {
    // first fill a two-dimensional array with all the information of the chunks
    Tile[][] tileArr = new Tile[this.getHeight()][this.getWidth()];

    if (this.getEncoding().equals(Encoding.BASE64)) {
      for (TileChunk chunk : this.chunks) {
        List<Tile> chunkTiles = parseBase64Data(chunk.getValue(), this.compression);
        this.addTiles(tileArr, chunk, chunkTiles);
      }
    } else if (this.getEncoding().equals(Encoding.CSV)) {
      for (TileChunk chunk : this.chunks) {
        List<Tile> chunkTiles = parseCsvData(chunk.getValue());
        this.addTiles(tileArr, chunk, chunkTiles);
      }
    } else {
      throw new IllegalArgumentException("Unsupported tile layer encoding " + this.getEncoding());
    }

    // fill up the rest of the map with Tile.EMPTY
    for (int y = 0; y < tileArr.length; y++) {
      for (int x = 0; x < tileArr[y].length; x++) {
        if (tileArr[y][x] == null) {
          tileArr[y][x] = Tile.EMPTY;
        }
      }
    }

    return ArrayUtilities.toList(tileArr);
  }

  private void addTiles(Tile[][] tileArr, TileChunk chunk, List<Tile> chunkTiles) {
    int startX = chunk.getX() - this.minChunkOffsetXMap;
    int startY = chunk.getY() - this.minChunkOffsetYMap;

    int index = 0;

    for (int y = startY; y < startY + chunk.getHeight(); y++) {
      for (int x = startX; x < startX + chunk.getWidth(); x++) {
        tileArr[y][x] = chunkTiles.get(index);
        index++;
      }
    }
  }

  private List<Tile> parseData() throws InvalidTileLayerException {
    List<Tile> tiles;
    if (this.getEncoding().equals(Encoding.BASE64)) {
      tiles = parseBase64Data(this.value, this.compression);
    } else if (this.getEncoding().equals(Encoding.CSV)) {
      tiles = parseCsvData(this.value);
    } else {
      throw new IllegalArgumentException("Unsupported tile layer encoding " + this.getEncoding());
    }

    return tiles;
  }
}
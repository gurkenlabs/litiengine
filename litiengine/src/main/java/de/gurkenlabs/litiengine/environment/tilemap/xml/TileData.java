package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.util.io.Codec;
import jakarta.xml.bind.DatatypeConverter;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlMixed;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Represents the tile data for a tile map. This class handles the encoding, compression, and storage of tile data.
 */
public class TileData {
  private static final Logger log = Logger.getLogger(TileData.class.getName());

  /**
   * Utility class for tile data encoding types. Provides constants and validation methods for encoding types.
   */
  public static class Encoding {
    /**
     * Constant for Base64 encoding.
     */
    public static final String BASE64 = "base64";

    /**
     * Constant for CSV encoding.
     */
    public static final String CSV = "csv";

    /**
     * Private constructor to prevent instantiation.
     */
    private Encoding() {
    }

    /**
     * Validates if the provided encoding is valid.
     *
     * @param encoding The encoding to validate.
     * @return {@code true} if the encoding is valid, {@code false} otherwise.
     */
    public static boolean isValid(String encoding) {
      return encoding != null && !encoding.isEmpty() && (encoding.equals(BASE64) || encoding.equals(CSV));
    }
  }

  /**
   * Utility class for tile data compression types. Provides constants and validation methods for compression types.
   */
  public static class Compression {
    /**
     * Constant for GZIP compression.
     */
    public static final String GZIP = "gzip";

    /**
     * Constant for ZLIB compression.
     */
    public static final String ZLIB = "zlib";

    /**
     * Constant for no compression.
     */
    public static final String NONE = null;

    /**
     * Private constructor to prevent instantiation.
     */
    private Compression() {
    }

    /**
     * Validates if the provided compression is valid.
     *
     * @param compression The compression to validate.
     * @return {@code true} if the compression is valid, {@code false} otherwise.
     */
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
   * Default constructor for the {@code TileData} class. This constructor is required for serialization purposes.
   */
  public TileData() {
    // keep for serialization
  }

  /**
   * Constructs a new {@code TileData} instance with the specified parameters.
   *
   * @param tiles       The list of tiles.
   * @param width       The width of the tile data.
   * @param height      The height of the tile data.
   * @param encoding    The encoding type of the tile data.
   * @param compression The compression type of the tile data.
   * @throws TmxException If the encoding or compression type is invalid.
   */
  public TileData(List<Tile> tiles, int width, int height, String encoding, String compression) throws TmxException {
    if (!Encoding.isValid(encoding)) {
      throw new TmxException(
        "Invalid tile data encoding '" + encoding + "'. Supported encodings are " + Encoding.CSV + " and " + Encoding.BASE64 + ".");
    }

    if (!Compression.isValid(compression)) {
      throw new TmxException(
        "Invalid tile data compression '" + compression + "'. Supported compressions are " + Compression.GZIP + " and " + Compression.ZLIB + ".");
    }

    this.tiles = tiles;
    this.encoding = encoding;
    this.compression = compression;
    this.width = width;
    this.height = height;
  }

  /**
   * Copy constructor for the {@link TileData} class. Creates a new instance of the {@link TileData} class by copying the properties from the provided
   * {@link TileData} object.
   *
   * @param original The original {@link TileData} object to copy from.
   */
  public TileData(TileData original) {
    this.encoding = original.getEncoding();
    this.compression = original.getCompression();
    this.rawValue = original.rawValue != null ? new ArrayList<>(original.rawValue) : null;
    this.value = original.getValue();
    this.chunks = original.chunks != null ? new ArrayList<>(original.chunks) : null;
    this.tiles = original.tiles != null ? new CopyOnWriteArrayList<>(original.getTiles()) : null;
    this.width = original.getWidth();
    this.height = original.getHeight();
    this.offsetX = original.getOffsetX();
    this.offsetY = original.getOffsetY();
    this.minChunkOffsetXMap = original.minChunkOffsetXMap;
    this.minChunkOffsetYMap = original.minChunkOffsetYMap;
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

  /**
   * Sets the encoding type for the tile data.
   *
   * @param encoding The encoding type to set.
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  /**
   * Sets the compression type for the tile data.
   *
   * @param compression The compression type to set.
   */
  public void setCompression(String compression) {
    this.compression = compression;
  }

  /**
   * Sets the value for the tile data and updates the raw value list.
   *
   * @param value The value to set.
   */
  public void setValue(String value) {
    this.value = value;
    if (this.rawValue == null) {
      this.rawValue = new CopyOnWriteArrayList<>();
    }

    this.rawValue.addFirst(value);
  }

  /**
   * Retrieves the list of tiles for the tile data. If the tiles are already parsed, it returns the cached list. Otherwise, it parses the tile data
   * based on the encoding and compression.
   *
   * @return The list of tiles.
   */
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

  /**
   * Encodes the provided {@code TileData} instance into a string based on its encoding type.
   *
   * @param data The {@code TileData} instance to encode.
   * @return The encoded string representation of the tile data.
   * @throws IOException If an I/O error occurs during encoding.
   */
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

  /**
   * Encodes the tile data into a CSV string format.
   *
   * @param data The {@code TileData} instance containing the tile data to encode.
   * @return The encoded CSV string representation of the tile data.
   */
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

  /**
   * Encodes the tile data into a Base64 string format.
   *
   * @param data The {@code TileData} instance containing the tile data to encode.
   * @return The encoded Base64 string representation of the tile data.
   * @throws IOException If an I/O error occurs during encoding.
   */
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

      if (data.getCompression() != null && data.getCompression().equals(Compression.GZIP) && out instanceof GZIPOutputStream gzout) {
        gzout.finish();
      }

      if (data.getCompression() != null && data.getCompression().equals(Compression.ZLIB) && out instanceof DeflaterOutputStream dfout) {
        dfout.finish();
      }

      return Codec.encode(baos.toByteArray());
    }
  }

  /**
   * Sets the minimum chunk offsets for the tile data.
   *
   * @param x The minimum chunk offset on the x-axis.
   * @param y The minimum chunk offset on the y-axis.
   */
  protected void setMinChunkOffsets(int x, int y) {
    this.minChunkOffsetXMap = x;
    this.minChunkOffsetYMap = y;
  }

  /**
   * Checks if the tile data represents an infinite map.
   *
   * @return {@code true} if the map is infinite, {@code false} otherwise.
   */
  protected boolean isInfinite() {
    return this.chunks != null && !this.chunks.isEmpty();
  }

  /**
   * Retrieves the width of the tile data. For infinite maps, the width is adjusted based on the chunk offsets.
   *
   * @return The width of the tile data.
   */
  protected int getWidth() {
    if (this.isInfinite() && this.minChunkOffsetXMap != 0) {
      return this.width + (this.offsetX - this.minChunkOffsetXMap);
    }

    return this.width;
  }

  /**
   * Retrieves the height of the tile data. For infinite maps, the height is adjusted based on the chunk offsets.
   *
   * @return The height of the tile data.
   */
  protected int getHeight() {
    if (this.isInfinite() && this.minChunkOffsetYMap != 0) {
      return this.height + (this.offsetY - this.minChunkOffsetYMap);
    }

    return this.height;
  }

  /**
   * Retrieves the x-axis offset of the tile data.
   *
   * @return The x-axis offset.
   */
  protected int getOffsetX() {
    return this.offsetX;
  }

  /**
   * Retrieves the y-axis offset of the tile data.
   *
   * @return The y-axis offset.
   */
  protected int getOffsetY() {
    return this.offsetY;
  }

  /**
   * Parses the tile data from a Base64 encoded string.
   *
   * @param value       The Base64 encoded string containing the tile data.
   * @param compression The compression type used on the tile data (e.g., GZIP, ZLIB, or null for no compression).
   * @return A list of {@code Tile} objects parsed from the Base64 encoded string.
   * @throws InvalidTileLayerException If the Base64 string is invalid or an I/O error occurs during parsing.
   */
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

  /**
   * Parses the tile data from a CSV formatted string.
   *
   * @param value The CSV formatted string containing the tile data.
   * @return A list of {@code Tile} objects parsed from the CSV string.
   * @throws InvalidTileLayerException If the CSV string is invalid or an error occurs during parsing.
   */
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
          parsed.add(new Tile(tileId));
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
   * This method processes the {@link XmlMixed} contents that were unmarshalled and extract either the string value containing the information about
   * the layer of a set of {@link TileChunk}s if the map is infinite.
   */
  private void processMixedData() {
    if (this.rawValue == null || this.rawValue.isEmpty()) {
      return;
    }

    List<TileChunk> rawChunks = new ArrayList<>();
    String v = null;
    for (Object val : this.rawValue) {
      if (val instanceof String s) {
        String trimmedValue = s.trim();
        if (!trimmedValue.isEmpty()) {
          v = trimmedValue;
        }
      }

      if (val instanceof TileChunk tc) {
        rawChunks.add(tc);
      }
    }

    if (rawChunks.isEmpty()) {
      this.value = v;
      return;
    }

    this.chunks = rawChunks;
  }

  /**
   * Updates the dimensions of the tile data based on the contained tile chunks. This method calculates the minimum and maximum x and y coordinates of
   * the chunks and adjusts the width and height of the tile data accordingly.
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

  /**
   * Parses the tile data from the contained tile chunks. This method processes the chunks based on the encoding type and fills a two-dimensional
   * array with the tile information. It also ensures that the rest of the map is filled with {@code Tile.EMPTY}.
   *
   * @return A list of {@code Tile} objects parsed from the chunks.
   * @throws InvalidTileLayerException If an error occurs during parsing.
   */
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
    return Arrays.stream(tileArr).flatMap(Arrays::stream).toList();
  }

  /**
   * Adds tiles from a chunk to the specified two-dimensional tile array. This method places the tiles from the chunk into the correct positions in
   * the tile array.
   *
   * @param tileArr    The two-dimensional array to which the tiles will be added.
   * @param chunk      The tile chunk containing the tiles to be added.
   * @param chunkTiles The list of tiles from the chunk to be added to the array.
   */
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

  /**
   * Parses the tile data based on the encoding type. This method processes the tile data string and converts it into a list of {@code Tile} objects.
   *
   * @return A list of {@code Tile} objects parsed from the tile data string.
   * @throws InvalidTileLayerException If an error occurs during parsing.
   */
  private List<Tile> parseData() throws InvalidTileLayerException {
    List<Tile> tmpTiles;
    if (this.getEncoding().equals(Encoding.BASE64)) {
      tmpTiles = parseBase64Data(this.value, this.compression);
    } else if (this.getEncoding().equals(Encoding.CSV)) {
      tmpTiles = parseCsvData(this.value);
    } else {
      throw new IllegalArgumentException("Unsupported tile layer encoding " + this.getEncoding());
    }

    return tmpTiles;
  }
}

/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import de.gurkenlabs.util.image.ImageProcessing;
import de.gurkenlabs.util.image.ImageSerializer;
import de.gurkenlabs.util.io.FileUtilities;
import de.gurkenlabs.util.zip.CompressionUtilities;

/**
 * The Class ImageCache.
 */
public class ImageCache {
  public static final String CACHE_DUMP_NAME = "imagecache.dump";

  /** The Constant CACHE_DIRECTORY. */
  public static final String CACHE_DIRECTORY = "cache/";

  public static final String MAP_DIRECTORY = "map";
  public static final String SPRITES_DIRECTORY = "sprites";

  public static final String IMAGES_DIRECTORY = "images";

  public static final ImageCache MAPS = new ImageCache("map");

  public static final ImageCache SPRITES = new ImageCache("sprites");

  public static final ImageCache IMAGES = new ImageCache("images");

  public static void loadCache(final String path) {
    InputStream in = FileUtilities.getGameResource(CACHE_DUMP_NAME);
    final File cacheFile = new File(path, CACHE_DUMP_NAME);
    if (in == null) {
      System.out.println("loading stream from " + cacheFile.toPath() + " failed!");
      return;
    }

    try {
      CompressionUtilities.unzip(in, new File(CACHE_DIRECTORY));
      System.out.println("cache loaded from " + cacheFile.toPath());
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public static void saveCache(final String path) {
    final File cacheFile = new File(path, CACHE_DUMP_NAME);
    try {
      if (cacheFile.exists()) {
        cacheFile.delete();
      }

      CompressionUtilities.zip(new File(CACHE_DIRECTORY), cacheFile);
      System.out.println("cache dumped to " + cacheFile.toPath());
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /** The cache. */
  private final ConcurrentHashMap<String, BufferedImage> cache;

  /** The sub folder. */
  private final String subFolder;

  /**
   * Contains key.
   *
   * @param key
   *          the key
   * @return true, if successful
   */
  public boolean containsKey(final String key) {
    return this.cache.containsKey(key) || new File(this.getFileName(key)).exists();
  }

  /**
   * Gets the.
   *
   * @param key
   *          the key
   * @return the buffered image
   */
  public BufferedImage get(final String key) {
    if (this.cache.containsKey(key)) {
      return this.cache.get(key);
    }

    return this.loadImage(key);
  }

  /**
   * Load all.
   */
  public void loadAll() {
    final File dir = new File(this.getSubFolderName());
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }
    final File[] directoryListing = dir.listFiles();
    if (directoryListing != null) {
      for (final File child : directoryListing) {
        if (!child.isFile()) {
          continue;
        }

        final BufferedImage img = this.loadImage(child.getName());

        // clean up cached file if the image is null
        if (img == null) {
          child.delete();
        }
      }
    }
  }

  /**
   * Put.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   * @return the buffered image
   */
  public BufferedImage put(final String key, final BufferedImage value) {
    return this.cache.put(key, value);
  }

  /**
   * Put persistent.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   * @return the buffered image
   */
  public BufferedImage putPersistent(final String key, final BufferedImage value) {
    if (key == null || key.isEmpty() || value == null) {
      return null;
    }

    this.cache.put(key, value);
    this.saveImage(key, value);
    return value;
  }

  public void clearPersistent() {
    final File dir = new File(this.getSubFolderName());
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }

    System.out.println("deleted '" + dir.toString() + "'");
    FileUtilities.deleteDir(dir);
    this.cache.clear();
  }

  /**
   * Instantiates a new image cache.
   *
   * @param subfolder
   *          the subfolder
   */
  private ImageCache(final String subfolder) {
    this.cache = new ConcurrentHashMap<>();
    this.subFolder = subfolder;
  }

  /**
   * Gets the file name.
   *
   * @param key
   *          the key
   * @return the file name
   */
  private String getFileName(final String key) {
    return this.getSubFolderName() + "\\" + key;
  }

  /**
   * Gets the sub folder name.
   *
   * @return the sub folder name
   */
  private String getSubFolderName() {
    return CACHE_DIRECTORY + this.subFolder;
  }

  /**
   * Load image.
   *
   * @param key
   *          the key
   * @return the buffered image
   */
  private synchronized BufferedImage loadImage(final String key) {
    final BufferedImage img = ImageSerializer.loadImage(this.getFileName(key));
    if (img == null) {
      return null;
    }

    this.cache.put(key, img);
    return img;
  }

  /**
   * Save image.
   *
   * @param key
   *          the key
   * @param value
   *          the value
   */
  private void saveImage(final String key, final BufferedImage value) {
    ImageSerializer.saveImage(this.getFileName(key), value);
  }
}

package de.gurkenlabs.litiengine.resources;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ImageCache {

  public static final ImageCache IMAGES = new ImageCache();

  public static final ImageCache SPRITES = new ImageCache();

  private final ConcurrentHashMap<String, BufferedImage> cache;

  private final List<Consumer<ImageCache>> clearConsumers;

  private ImageCache() {
    this.cache = new ConcurrentHashMap<>();
    this.clearConsumers = new CopyOnWriteArrayList<>();
  }

  public static void clearAll() {
    IMAGES.clear();
    SPRITES.clear();
  }

  public void clear() {
    this.cache.clear();

    for (Consumer<ImageCache> cons : this.clearConsumers) {
      cons.accept(this);
    }
  }

  public void onCleared(Consumer<ImageCache> cons) {
    this.clearConsumers.add(cons);
  }

  public void clear(final String regex) {
    List<String> remove = new ArrayList<>();
    for (String key : this.cache.keySet()) {
      if (key.matches(regex)) {
        remove.add(key);
      }
    }

    for (String key : remove) {
      this.cache.remove(key);
    }

    for (Consumer<ImageCache> cons : this.clearConsumers) {
      cons.accept(this);
    }
  }

  public boolean containsKey(final String key) {
    return this.cache.containsKey(key);
  }

  public BufferedImage get(final String key) {
    if (this.cache.containsKey(key)) {
      return this.cache.get(key);
    }

    return null;
  }

  public BufferedImage put(final String key, final BufferedImage value) {
    return this.cache.put(key, value);
  }

  public int size() {
    return this.cache.size();
  }
}

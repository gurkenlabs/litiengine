package de.gurkenlabs.litiengine.video;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.resources.VideoResource;

public abstract class VideoManager extends GuiComponent implements VideoPlayer {

  protected static final Logger log = Logger.getLogger(VideoManager.class.getName());
  protected static ArrayList<String> loadedNatives = new ArrayList<String>();
  
  {
    if(!nativesLoaded()) {
      try {
        loadNatives();
      } catch (LinkageError e) {
          log.log(Level.SEVERE, e, () -> e.getMessage());
          throw e;
      } catch (SecurityException e) {
        log.log(Level.SEVERE, e, () -> e.getMessage());
        throw e;
      }
    }

    initialize();
  }
  
  /**
   * Creates a new VideoManager which can play the
   * specified video.
   * 
   * Subclasses MUST overwrite this constructor
   * 
   * @param video the video to load
   * 
   * @throws LinkageError if the native binaries were unable to load
   */
  protected VideoManager(VideoResource video) {
    super(0,0);
    setVideo(video);
  }
  
  /**
   * Initializes the media player
   * 
   * @throws IllegalStateException if the media player has already been initialized
   */
  protected abstract void initialize();
  
  /**
   * Load the native library required to play videos with this video manager
   * 
   * Should add a string representation of the library to {@link #loadedNatives}
   * if it successfully loads
   * 
   * @throws LinkageError if the library is unable to load
   * @throws IllegalStateException if the library is already loaded
   */
  protected abstract void loadNatives();
  
  /**
   * @return true if the native library required to play the video has been loaded
   */
  protected abstract boolean nativesLoaded();
  
  public static boolean nativeLoaded(String libName) {
    return loadedNatives.contains(libName);
  }

  @Override
  public void render(Graphics2D g) {
    getPanel().update(g);
  }
}

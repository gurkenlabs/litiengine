package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

final class SpriteAnimationPreview extends JPanel {
  private final JLabel preview = new JLabel("", SwingConstants.CENTER) {
    @Override
    protected void paintComponent(Graphics graphics) {
      Graphics2D g = (Graphics2D) graphics.create();
      TransparencyGrid.paint(g, getWidth(), getHeight());
      g.dispose();
      super.paintComponent(graphics);
    }
  };
  private final Timer timer = new Timer(Animation.DEFAULT_FRAME_DURATION, _ -> advanceFrame());
  private Spritesheet spritesheet;
  private String resourceName;
  private int[] frameDurations = new int[0];
  private int currentFrame;

  SpriteAnimationPreview() {
    super(new BorderLayout());
    setOpaque(false);
    this.preview.setOpaque(false);
    this.preview.setBorder(BorderFactory.createLineBorder(Style.border()));
    this.preview.setPreferredSize(new Dimension(0, 112));
    this.preview.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    this.preview.setToolTipText(Resources.strings().get("spriteEditor_doubleClickToEdit"));
    this.preview.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
          openSpriteEditor();
        }
      }
    });
    add(this.preview, BorderLayout.CENTER);
  }

  @Override
  public void addNotify() {
    super.addNotify();
    if (frameCount() > 1) {
      this.timer.start();
    }
  }

  @Override
  public void removeNotify() {
    this.timer.stop();
    super.removeNotify();
  }

  void setSpritesheet(Spritesheet spritesheet) {
    setSpritesheet(spritesheet, spritesheet != null ? spritesheet.getName() : null);
  }

  void setSpritesheet(Spritesheet spritesheet, String resourceName) {
    boolean running = this.timer.isRunning();
    this.timer.stop();
    this.spritesheet = spritesheet;
    this.resourceName = resourceName;
    this.currentFrame = 0;
    this.frameDurations = loadFrameDurations(spritesheet);
    renderFrame();
    updateTimerDelay();
    if ((running || isShowing()) && frameCount() > 1) {
      this.timer.start();
    }
  }

  void start() {
    if (!this.timer.isRunning() && frameCount() > 1) {
      this.timer.start();
    }
  }

  void stop() {
    this.timer.stop();
  }

  private void advanceFrame() {
    if (frameCount() == 0) {
      this.timer.stop();
      this.preview.setIcon(null);
      return;
    }
    this.currentFrame = (this.currentFrame + 1) % frameCount();
    renderFrame();
    updateTimerDelay();
  }

  private void renderFrame() {
    if (frameCount() == 0) {
      this.preview.setIcon(null);
      return;
    }
    var image = this.spritesheet.getSprite(this.currentFrame);
    var scaled = image != null ? Imaging.scale(image, 96, 96, true) : null;
    this.preview.setIcon(scaled != null ? new ImageIcon(scaled) : null);
  }

  private void updateTimerDelay() {
    int delay = this.currentFrame < this.frameDurations.length
        ? this.frameDurations[this.currentFrame]
        : Animation.DEFAULT_FRAME_DURATION;
    this.timer.setDelay(Math.max(1, delay));
    this.timer.setInitialDelay(Math.max(1, delay));
  }

  private void openSpriteEditor() {
    if (this.spritesheet == null || Editor.instance().getGameFile() == null) {
      return;
    }
    resolveSpriteResource().ifPresent(UI::showSpriteInspector);
  }

  private java.util.Optional<SpritesheetResource> resolveSpriteResource() {
    return Editor.instance().getGameFile().getSpriteSheets().stream()
        .filter(resource -> resource.getName().equals(this.resourceName)
            || Resources.spritesheets().get(resource.getName()) == this.spritesheet)
        .findFirst();
  }

  private int frameCount() {
    return this.spritesheet != null ? this.spritesheet.getTotalNumberOfSprites() : 0;
  }

  private static int[] loadFrameDurations(Spritesheet spritesheet) {
    if (spritesheet == null || spritesheet.getTotalNumberOfSprites() == 0) {
      return new int[0];
    }
    int frameCount = spritesheet.getTotalNumberOfSprites();
    int[] configured = Resources.spritesheets().getCustomKeyFrameDurations(spritesheet);
    int[] durations = new int[frameCount];
    Arrays.fill(durations, Animation.DEFAULT_FRAME_DURATION);
    System.arraycopy(configured, 0, durations, 0, Math.min(configured.length, durations.length));
    return durations;
  }

  javax.swing.Icon getIconForTest() {
    return this.preview.getIcon();
  }

  boolean isRunningForTest() {
    return this.timer.isRunning();
  }

  int getCurrentFrameForTest() {
    return this.currentFrame;
  }

  int getTimerDelayForTest() {
    return this.timer.getDelay();
  }

  void advanceFrameForTest() {
    advanceFrame();
  }
}

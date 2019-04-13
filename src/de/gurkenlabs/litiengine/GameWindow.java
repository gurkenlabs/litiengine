package de.gurkenlabs.litiengine;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import de.gurkenlabs.litiengine.graphics.RenderComponent;
import de.gurkenlabs.litiengine.gui.screens.Resolution;

public final class GameWindow {
  private static final Logger log = Logger.getLogger(GameWindow.class.getName());
  private static final int ICONIFIED_MAX_FPS = 1;
  private static final int NONE_FOCUS_MAX_FPS = 10;

  private final List<Consumer<Dimension>> resolutionChangedConsumer;

  private final JFrame hostControl;
  private final RenderComponent renderCanvas;

  private float resolutionScale = 1;

  private Dimension resolution;
  private Point screenLocation;

  public GameWindow() {
    this.hostControl = new JFrame();

    this.resolutionChangedConsumer = new CopyOnWriteArrayList<>();

    this.renderCanvas = new RenderComponent(Game.config().graphics().getResolution());
    if (!Game.isInNoGUIMode()) {
      this.hostControl.setBackground(Color.BLACK);
      this.hostControl.add(this.renderCanvas);

      this.initializeEventListeners();

      this.hostControl.setTitle(Game.info().getTitle());
      this.hostControl.setResizable(false);
      this.hostControl.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      initializeWindowEventListeners(this.hostControl);
    }
  }

  public void init() {
    if (Game.isInNoGUIMode()) {
      this.resolution = new Dimension(0, 0);
      this.hostControl.setVisible(false);
      return;
    }

    if (Game.config().graphics().isFullscreen()) {
      this.hostControl.setUndecorated(true);
      GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

      if (gd.isFullScreenSupported()) {
        gd.setFullScreenWindow(this.hostControl);
      } else {
        log.log(Level.SEVERE, "Full screen is not supported on this device.");
        this.hostControl.setExtendedState(Frame.MAXIMIZED_BOTH);
        this.hostControl.setVisible(true);
      }
      this.setResolution(Resolution.custom(this.getSize().width, this.getSize().height, "fullscreen"));
    } else {
      this.setResolution(Game.config().graphics().getResolution());
      this.hostControl.setVisible(true);
    }

    this.getRenderComponent().init();
    this.resolution = this.getRenderComponent().getSize();
    this.hostControl.requestFocus();
  }

  public boolean isFocusOwner() {
    if (this.getRenderComponent() instanceof Component && this.getRenderComponent().isFocusOwner()) {
      return true;
    }

    return this.hostControl.isFocusOwner();
  }

  public void onResolutionChanged(final Consumer<Dimension> resolutionConsumer) {
    if (this.resolutionChangedConsumer.contains(resolutionConsumer)) {
      return;
    }

    this.resolutionChangedConsumer.add(resolutionConsumer);
  }

  public void setResolution(Resolution res) {
    this.setResolution(res.getDimension());
  }

  public float getResolutionScale() {
    return this.resolutionScale;
  }

  public Point2D getCenter() {
    return new Point2D.Double(this.getWidth() / 2.0, this.getHeight() / 2.0);
  }

  public Container getHostControl() {
    return this.hostControl;
  }

  public Dimension getSize() {
    return this.hostControl.getSize();
  }

  public int getWidth() {
    return this.hostControl.getWidth();
  }

  public int getHeight() {
    return this.hostControl.getHeight();
  }

  public RenderComponent getRenderComponent() {
    return this.renderCanvas;
  }

  public Dimension getResolution() {
    return this.resolution;
  }

  public Point getWindowLocation() {
    if (this.screenLocation != null) {
      return this.screenLocation;
    }

    this.screenLocation = this.hostControl.getLocationOnScreen();
    return this.screenLocation;
  }

  public void setIconImage(Image image) {
    this.hostControl.setIconImage(image);
  }

  public void setTitle(String name) {
    this.hostControl.setTitle(name);
  }

  private void setResolution(Dimension dim) {
    Dimension insetAwareDimension = new Dimension(dim.width + this.hostControl.getInsets().left + this.hostControl.getInsets().right, dim.height + this.hostControl.getInsets().top + this.hostControl.getInsets().bottom);

    if (Game.config().graphics().enableResolutionScaling()) {
      this.resolutionScale = (float) (dim.getWidth() / Resolution.Ratio16x9.RES_1920x1080.getWidth());
      Game.graphics().setBaseRenderScale(Game.graphics().getBaseRenderScale() * this.resolutionScale);
    }

    this.hostControl.setSize(insetAwareDimension);
  }

  private void initializeEventListeners() {
    this.getRenderComponent().addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent evt) {
        resolution = getRenderComponent().getSize();
        resolutionChangedConsumer.forEach(consumer -> consumer.accept(GameWindow.this.getSize()));
      }
    });

    this.hostControl.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentMoved(final ComponentEvent evt) {
        screenLocation = null;
      }
    });

  }

  private static void initializeWindowEventListeners(Window window) {

    window.addWindowStateListener(e -> {
      if (e.getNewState() == Frame.ICONIFIED) {
        Game.renderLoop().setMaxFps(ICONIFIED_MAX_FPS);
      } else {
        Game.renderLoop().setMaxFps(Game.config().client().getMaxFps());
      }
    });

    window.addWindowFocusListener(new WindowFocusListener() {
      @Override
      public void windowLostFocus(WindowEvent e) {
        if (Game.config().graphics().reduceFramesWhenNotFocused()) {
          Game.renderLoop().setMaxFps(NONE_FOCUS_MAX_FPS);
        }
      }

      @Override
      public void windowGainedFocus(WindowEvent e) {
        Game.renderLoop().setMaxFps(Game.config().client().getMaxFps());
      }
    });

    window.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent event) {
        if (Game.terminating()) {
          System.exit(Game.EXIT_GAME_CLOSED);
        }
      }
    });
  }
}

package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.EnvironmentListener;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.Scroll;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Owns all fixed chrome around the heavyweight map Canvas. */
public final class ViewportPanel extends JPanel {
  private final ViewportToolbar toolbar;
  private final CoordinateRuler horizontalRuler;
  private final CoordinateRuler verticalRuler;
  private final StatusBar statusBar;
  private final ScrollHandlerBar horizontalScroll;
  private final ScrollHandlerBar verticalScroll;
  private final JPanel corner;

  public ViewportPanel(Canvas canvas, ViewportToolbar toolbar) {
    super(new BorderLayout());
    this.toolbar = toolbar;
    this.horizontalRuler = new CoordinateRuler(Adjustable.HORIZONTAL);
    this.verticalRuler = new CoordinateRuler(Adjustable.VERTICAL);
    this.statusBar = new StatusBar();
    this.horizontalScroll = new ScrollHandlerBar(Adjustable.HORIZONTAL);
    this.verticalScroll = new ScrollHandlerBar(Adjustable.VERTICAL);

    setMinimumSize(new Dimension(250, 100));
    setBorder(null);

    this.corner = new JPanel();
    this.corner.setOpaque(true);
    this.corner.setPreferredSize(new Dimension(CoordinateRuler.verticalWidth(), CoordinateRuler.horizontalHeight()));
    this.corner.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Style.border()));

    JPanel rulerHeader = new JPanel(new BorderLayout());
    rulerHeader.add(this.corner, BorderLayout.WEST);
    rulerHeader.add(this.horizontalRuler, BorderLayout.CENTER);

    JPanel canvasHost = new JPanel(new BorderLayout());
    canvasHost.setOpaque(true);
    canvasHost.add(canvas, BorderLayout.CENTER);
    canvasHost.add(this.horizontalScroll, BorderLayout.SOUTH);
    canvasHost.add(this.verticalScroll, BorderLayout.EAST);

    JPanel stage = new JPanel(new BorderLayout());
    stage.add(rulerHeader, BorderLayout.NORTH);
    stage.add(this.verticalRuler, BorderLayout.WEST);
    stage.add(canvasHost, BorderLayout.CENTER);

    add(this.toolbar, BorderLayout.NORTH);
    add(stage, BorderLayout.CENTER);
    add(this.statusBar, BorderLayout.SOUTH);

    Scroll.init(this.verticalScroll, this.horizontalScroll);
    EnvironmentListener environmentListener = new EnvironmentListener() {
      @Override public void loaded(Environment environment) {
        if (environment != null && environment.getMap() != null) {
          Scroll.updateScrollHandlers();
        } else {
          updateScrollVisibility(false);
        }
        repaintRulers();
      }

      @Override public void unloaded(Environment environment) {
        updateScrollVisibility(false);
        repaintRulers();
      }
    };
    environmentListener.loaded(Game.world().environment());
    Game.world().addListener(environmentListener);
    Game.world().camera().onZoom(event -> repaintRulers());
    Game.world().camera().onFocus(event -> repaintRulers());
    canvas.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent event) {
        Scroll.updateScrollHandlers();
        repaintRulers();
      }
    });
    refreshTheme();
  }

  void refreshTheme() {
    setBackground(Style.background());
    this.toolbar.refreshTheme();
    this.statusBar.refreshTheme();
    this.corner.setBackground(Style.workspaceTop());
    this.corner.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Style.border()));
    this.horizontalRuler.updateUI();
    this.verticalRuler.updateUI();
    repaint();
  }

  StatusBar getStatusBar() {
    return this.statusBar;
  }

  ScrollHandlerBar getHorizontalScroll() {
    return this.horizontalScroll;
  }

  ScrollHandlerBar getVerticalScroll() {
    return this.verticalScroll;
  }

  private void updateScrollVisibility(boolean visible) {
    Runnable update = () -> {
      this.horizontalScroll.setVisible(visible);
      this.verticalScroll.setVisible(visible);
      revalidate();
    };
    if (SwingUtilities.isEventDispatchThread()) {
      update.run();
    } else {
      SwingUtilities.invokeLater(update);
    }
  }

  private void repaintRulers() {
    SwingUtilities.invokeLater(() -> {
      this.horizontalRuler.repaint();
      this.verticalRuler.repaint();
    });
  }
}

package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TilesetEntry;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

final class TileCollisionEditorPanel extends JPanel {
  enum Mode {
    SELECT,
    RECTANGLE
  }

  private enum DragOp {
    MOVE, RESIZE_NW, RESIZE_N, RESIZE_NE, RESIZE_E, RESIZE_SE, RESIZE_S, RESIZE_SW, RESIZE_W
  }

  private static final Color SHAPE_FILL = new Color(230, 70, 70, 72);
  private static final Color SHAPE_OUTLINE = new Color(245, 85, 85);
  private static final Color GRID_COLOR = new Color(128, 128, 128, 40);
  private static final Color HANDLE_FILL = new Color(255, 255, 255, 200);
  private static final Color HANDLE_STROKE = new Color(245, 85, 85);
  private static final int CANVAS_PADDING = 12;
  private static final int HANDLE_RADIUS = 2;
  private static final float GRIP_FRACTION = 0.15f;

  private final CollisionCanvas canvas;
  private final JButton deleteButton;
  private TilesetEntry entry;
  private BufferedImage image;
  private int tileWidth;
  private int tileHeight;
  private Consumer<Runnable> mutationHandler = Runnable::run;
  private Runnable changed = () -> {};
  private Mode mode = Mode.SELECT;
  private IMapObject selectedShape;
  private Point2D.Float rectangleStart;
  private Point2D.Float rectangleEnd;

  private DragOp dragOp;
  private Point2D.Float dragAnchor;
  private Rectangle2D.Float dragOriginalBounds;

  TileCollisionEditorPanel() {
    super(new BorderLayout(0, 6));
    setOpaque(false);

    JPanel tools = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    tools.setOpaque(false);
    ButtonGroup modes = new ButtonGroup();
    JToggleButton select = modeButton(
        Resources.strings().get("tilesetEditor_collisionSelect"), Icons.POINTER_16, Mode.SELECT, true);
    JToggleButton rectangle = modeButton(
        Resources.strings().get("tilesetEditor_collisionRectangle"), Icons.COLLISIONBOX_16, Mode.RECTANGLE, false);
    modes.add(select);
    modes.add(rectangle);
    tools.add(select);
    tools.add(rectangle);

    this.deleteButton = new JButton(Icons.DELETE_16);
    this.deleteButton.setToolTipText(Resources.strings().get("tilesetEditor_collisionDelete"));
    this.deleteButton.getAccessibleContext().setAccessibleName(
        Resources.strings().get("tilesetEditor_collisionDelete"));
    this.deleteButton.addActionListener(_ -> deleteSelectedShape());
    Style.styleButton(this.deleteButton, Style.ButtonVariant.DESTRUCTIVE);
    tools.add(this.deleteButton);
    add(tools, BorderLayout.NORTH);

    this.canvas = new CollisionCanvas();
    this.canvas.setPreferredSize(new Dimension(0, 240));
    this.canvas.setMinimumSize(new Dimension(120, 160));
    this.canvas.setFocusable(true);
    this.canvas.setBorder(BorderFactory.createLineBorder(Style.border()));
    this.canvas.getAccessibleContext().setAccessibleName(
        Resources.strings().get("tilesetEditor_collisionCanvas"));
    this.canvas.getAccessibleContext().setAccessibleDescription(
        Resources.strings().get("tilesetEditor_collisionCanvasHelp"));
    installInputActions();
    add(this.canvas, BorderLayout.CENTER);
    updateEnabledState();
  }

  void bind(
      TilesetEntry entry,
      BufferedImage image,
      int tileWidth,
      int tileHeight,
      Consumer<Runnable> mutationHandler,
      Runnable changed) {
    this.entry = entry;
    this.image = image;
    this.tileWidth = Math.max(0, tileWidth);
    this.tileHeight = Math.max(0, tileHeight);
    this.mutationHandler = mutationHandler != null ? mutationHandler : Runnable::run;
    this.changed = changed != null ? changed : () -> {};
    this.selectedShape = null;
    cancelDraft();
    updateEnabledState();
    this.canvas.repaint();
  }

  void refresh(TilesetEntry entry, BufferedImage image) {
    this.entry = entry;
    this.image = image;
    this.selectedShape = null;
    cancelDraft();
    updateEnabledState();
    this.canvas.repaint();
  }

  private JToggleButton modeButton(String tooltip, javax.swing.Icon icon, Mode buttonMode, boolean selected) {
    JToggleButton button = Style.iconToggleButton(icon, selected);
    Style.styleButton(button, Style.ButtonVariant.SECONDARY);
    button.setToolTipText(tooltip);
    button.getAccessibleContext().setAccessibleName(tooltip);
    button.addActionListener(_ -> setMode(buttonMode));
    return button;
  }

  private void setMode(Mode mode) {
    this.mode = mode;
    this.selectedShape = null;
    cancelDraft();
    updateEnabledState();
    this.canvas.repaint();
    this.canvas.requestFocusInWindow();
  }

  private void updateEnabledState() {
    boolean enabled = this.entry != null && this.tileWidth > 0 && this.tileHeight > 0;
    this.canvas.setEnabled(enabled);
    this.deleteButton.setEnabled(enabled && this.selectedShape != null);
  }

  private void installInputActions() {
    this.canvas.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteShape");
    this.canvas.getActionMap().put("deleteShape", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent event) {
        deleteSelectedShape();
      }
    });
    this.canvas.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancelShape");
    this.canvas.getActionMap().put("cancelShape", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent event) {
        cancelDraft();
        canvas.repaint();
      }
    });
  }

  private void handlePressed(MouseEvent event) {
    if (!SwingUtilities.isLeftMouseButton(event) || !imageBounds().contains(event.getPoint())) {
      return;
    }
    this.canvas.requestFocusInWindow();
    Point2D.Float point = canvasToTile(event.getPoint());
    if (point == null) {
      return;
    }
    if (this.mode == Mode.SELECT) {
      if (this.selectedShape != null) {
        DragOp op = hitTestDragOp(point, this.selectedShape);
        if (op != null) {
          this.dragOp = op;
          this.dragAnchor = point;
          this.dragOriginalBounds = new Rectangle2D.Float(
              (float) this.selectedShape.getX(), (float) this.selectedShape.getY(),
              (float) this.selectedShape.getWidth(), (float) this.selectedShape.getHeight());
          return;
        }
      }
      this.selectedShape = shapeAt(point);
      updateEnabledState();
    } else if (this.mode == Mode.RECTANGLE) {
      this.rectangleStart = snapPoint(point);
      this.rectangleEnd = snapPoint(point);
    }
    this.canvas.repaint();
  }

  private void handleDragged(MouseEvent event) {
    Point2D.Float point = canvasToTile(event.getPoint());
    if (point == null) {
      return;
    }
    if (this.mode == Mode.RECTANGLE && this.rectangleStart != null) {
      this.rectangleEnd = snapPoint(point);
    } else if (this.mode == Mode.SELECT && this.dragOp != null && this.selectedShape != null) {
      applyDrag(point);
    }
    this.canvas.repaint();
  }

  private void handleReleased(MouseEvent event) {
    if (!SwingUtilities.isLeftMouseButton(event)) {
      return;
    }
    if (this.mode == Mode.RECTANGLE && this.rectangleStart != null) {
      Point2D.Float point = canvasToTile(event.getPoint());
      if (point != null) {
        this.rectangleEnd = snapPoint(point);
      }
      commitRectangle();
    } else if (this.mode == Mode.SELECT && this.dragOp != null) {
      commitDrag();
    }
  }

  private void handleMoved(MouseEvent event) {
    if (this.mode != Mode.SELECT) {
      this.canvas.setCursor(Cursor.getDefaultCursor());
      return;
    }
    Point2D.Float point = canvasToTile(event.getPoint());
    if (point == null || this.selectedShape == null) {
      this.canvas.setCursor(Cursor.getDefaultCursor());
      return;
    }
    DragOp op = hitTestDragOp(point, this.selectedShape);
    this.canvas.setCursor(cursorForOp(op));
  }

  private void applyDrag(Point2D.Float current) {
    float dx = current.x - this.dragAnchor.x;
    float dy = current.y - this.dragAnchor.y;
    Rectangle2D.Float o = this.dragOriginalBounds;
    float x = o.x, y = o.y, w = o.width, h = o.height;

    switch (this.dragOp) {
      case MOVE -> { x = snap(o.x + dx); y = snap(o.y + dy); }
      case RESIZE_NW -> { x = snap(o.x + dx); y = snap(o.y + dy); w = o.width - dx; h = o.height - dy; }
      case RESIZE_N -> { y = snap(o.y + dy); h = o.height - dy; }
      case RESIZE_NE -> { w = o.width + dx; y = snap(o.y + dy); h = o.height - dy; }
      case RESIZE_E -> { w = o.width + dx; }
      case RESIZE_SE -> { w = o.width + dx; h = o.height + dy; }
      case RESIZE_S -> { h = o.height + dy; }
      case RESIZE_SW -> { x = snap(o.x + dx); w = o.width - dx; h = o.height + dy; }
      case RESIZE_W -> { x = snap(o.x + dx); w = o.width - dx; }
    }

    if (w < 1) { w = 1; if (this.dragOp == DragOp.RESIZE_NW || this.dragOp == DragOp.RESIZE_W || this.dragOp == DragOp.RESIZE_SW) x = o.x + o.width - 1; }
    if (h < 1) { h = 1; if (this.dragOp == DragOp.RESIZE_NW || this.dragOp == DragOp.RESIZE_N || this.dragOp == DragOp.RESIZE_NE) y = o.y + o.height - 1; }
    if (this.dragOp == DragOp.MOVE) {
      x = Math.clamp(x, 0, Math.max(0, this.tileWidth - w));
      y = Math.clamp(y, 0, Math.max(0, this.tileHeight - h));
    } else {
      x = Math.clamp(x, 0, this.tileWidth - 1);
      y = Math.clamp(y, 0, this.tileHeight - 1);
      if (x + w > this.tileWidth) w = this.tileWidth - x;
      if (y + h > this.tileHeight) h = this.tileHeight - y;
    }

    this.selectedShape.setLocation(x, y);
    this.selectedShape.setWidth(w);
    this.selectedShape.setHeight(h);
  }

  private void commitDrag() {
    if (this.selectedShape != null) {
      this.selectedShape.setLocation(Math.round((float) this.selectedShape.getX()), Math.round((float) this.selectedShape.getY()));
      this.selectedShape.setWidth(Math.round((float) this.selectedShape.getWidth()));
      this.selectedShape.setHeight(Math.round((float) this.selectedShape.getHeight()));
      this.changed.run();
    }
    this.dragOp = null;
    this.dragAnchor = null;
    this.dragOriginalBounds = null;
  }

  private void commitRectangle() {
    Rectangle2D.Float bounds = draftRectangle();
    this.rectangleStart = null;
    this.rectangleEnd = null;
    if (bounds == null || bounds.width <= 0 || bounds.height <= 0 || this.entry == null) {
      this.canvas.repaint();
      return;
    }

    MapObject object = new MapObject();
    object.setLocation(Math.round(bounds.x), Math.round(bounds.y));
    object.setWidth(Math.round(bounds.width));
    object.setHeight(Math.round(bounds.height));
    addShape(object);
  }

  private void addShape(MapObject object) {
    this.mutationHandler.accept(() -> {
      MapObjectLayer collision = this.entry.getOrCreateCollisionInfo();
      object.setId(nextObjectId(collision));
      collision.addMapObject(object);
    });
    this.selectedShape = object;
    this.changed.run();
    updateEnabledState();
    this.canvas.repaint();
  }

  private void deleteSelectedShape() {
    if (this.entry == null || this.selectedShape == null || this.entry.getCollisionInfo() == null) {
      return;
    }
    IMapObject shape = this.selectedShape;
    this.mutationHandler.accept(() -> {
      this.entry.getCollisionInfo().removeMapObject(shape);
      if (this.entry.getCollisionInfo().getMapObjects().isEmpty()) {
        this.entry.clearCollisionInfo();
      }
    });
    this.selectedShape = null;
    this.changed.run();
    updateEnabledState();
    this.canvas.repaint();
  }

  private static int nextObjectId(MapObjectLayer layer) {
    return layer.getMapObjects().stream().mapToInt(IMapObject::getId).max().orElse(0) + 1;
  }

  private void cancelDraft() {
    this.rectangleStart = null;
    this.rectangleEnd = null;
    this.dragOp = null;
    this.dragAnchor = null;
    this.dragOriginalBounds = null;
  }

  private Rectangle2D.Float draftRectangle() {
    if (this.rectangleStart == null || this.rectangleEnd == null) {
      return null;
    }
    float x = Math.min(this.rectangleStart.x, this.rectangleEnd.x);
    float y = Math.min(this.rectangleStart.y, this.rectangleEnd.y);
    return new Rectangle2D.Float(
        x, y, Math.abs(this.rectangleEnd.x - this.rectangleStart.x),
        Math.abs(this.rectangleEnd.y - this.rectangleStart.y));
  }

  private IMapObject shapeAt(Point2D point) {
    if (this.entry == null || this.entry.getCollisionInfo() == null) {
      return null;
    }
    java.util.List<IMapObject> shapes = this.entry.getCollisionInfo().getMapObjects();
    for (int i = shapes.size() - 1; i >= 0; i--) {
      IMapObject shape = shapes.get(i);
      if (shape.getBoundingBox().contains(point)) {
        return shape;
      }
    }
    return null;
  }

  private DragOp hitTestDragOp(Point2D.Float p, IMapObject shape) {
    Rectangle2D b = shape.getBoundingBox();
    float gripX = (float) Math.max(2, b.getWidth() * GRIP_FRACTION);
    float gripY = (float) Math.max(2, b.getHeight() * GRIP_FRACTION);
    boolean nearTop = p.y >= b.getY() - gripY && p.y <= b.getY() + gripY;
    boolean nearBottom = p.y >= b.getY() + b.getHeight() - gripY && p.y <= b.getY() + b.getHeight() + gripY;
    boolean nearLeft = p.x >= b.getX() - gripX && p.x <= b.getX() + gripX;
    boolean nearRight = p.x >= b.getX() + b.getWidth() - gripX && p.x <= b.getX() + b.getWidth() + gripX;

    if (nearTop && nearLeft) return DragOp.RESIZE_NW;
    if (nearTop && nearRight) return DragOp.RESIZE_NE;
    if (nearBottom && nearLeft) return DragOp.RESIZE_SW;
    if (nearBottom && nearRight) return DragOp.RESIZE_SE;
    if (nearTop) return DragOp.RESIZE_N;
    if (nearBottom) return DragOp.RESIZE_S;
    if (nearLeft) return DragOp.RESIZE_W;
    if (nearRight) return DragOp.RESIZE_E;
    if (b.contains(p)) return DragOp.MOVE;
    return null;
  }

  private static Cursor cursorForOp(DragOp op) {
    if (op == null) return Cursor.getDefaultCursor();
    return switch (op) {
      case MOVE -> Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
      case RESIZE_NW -> Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
      case RESIZE_N -> Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
      case RESIZE_NE -> Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
      case RESIZE_E -> Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
      case RESIZE_SE -> Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
      case RESIZE_S -> Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
      case RESIZE_SW -> Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
      case RESIZE_W -> Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
    };
  }

  private static Point2D.Float snapPoint(Point2D.Float p) {
    return new Point2D.Float(Math.round(p.x), Math.round(p.y));
  }

  private Point2D.Float canvasToTile(Point point) {
    Rectangle2D.Float bounds = imageBounds();
    if (this.entry == null || bounds.width <= 0 || bounds.height <= 0) {
      return null;
    }
    float x = (point.x - bounds.x) * this.tileWidth / bounds.width;
    float y = (point.y - bounds.y) * this.tileHeight / bounds.height;
    return clamp(x, y);
  }

  private Rectangle2D.Float imageBounds() {
    if (this.tileWidth <= 0 || this.tileHeight <= 0) {
      return new Rectangle2D.Float();
    }
    float availableWidth = Math.max(1, this.canvas.getWidth() - CANVAS_PADDING * 2f);
    float availableHeight = Math.max(1, this.canvas.getHeight() - CANVAS_PADDING * 2f);
    float scale = Math.min(availableWidth / this.tileWidth, availableHeight / this.tileHeight);
    float width = this.tileWidth * scale;
    float height = this.tileHeight * scale;
    return new Rectangle2D.Float((this.canvas.getWidth() - width) / 2f, (this.canvas.getHeight() - height) / 2f, width, height);
  }

  private void paintCanvas(Graphics graphics) {
    Graphics2D g = (Graphics2D) graphics.create();
    try {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Rectangle2D.Float bounds = imageBounds();
      if (this.entry == null || bounds.width <= 0 || bounds.height <= 0) {
        g.setColor(Style.mutedText());
        String text = Resources.strings().get("tilesetEditor_collisionEmpty");
        int x = Math.max(8, (this.canvas.getWidth() - g.getFontMetrics().stringWidth(text)) / 2);
        g.drawString(text, x, this.canvas.getHeight() / 2);
        return;
      }
      g.setColor(Style.surface());
      g.fill(bounds);
      if (this.image != null) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(this.image, Math.round(bounds.x), Math.round(bounds.y), Math.round(bounds.width), Math.round(bounds.height), null);
      }
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g.setColor(Style.border());
      g.draw(bounds);

      g.translate(bounds.x, bounds.y);
      float sx = bounds.width / this.tileWidth;
      float sy = bounds.height / this.tileHeight;

      paintPixelGrid(g, sx, sy);

      g.scale(sx, sy);

      float strokeScale = Math.max(sx, sy);
      g.setStroke(new BasicStroke(Math.max(0.5f, 1f / strokeScale)));
      if (this.entry.getCollisionInfo() != null) {
        for (IMapObject shape : this.entry.getCollisionInfo().getMapObjects()) {
          paintShape(g, shape, shape == this.selectedShape);
        }
      }
      Rectangle2D.Float rectangle = draftRectangle();
      if (rectangle != null) {
        paintShape(g, rectangle, false);
      }
      if (this.selectedShape != null && this.mode == Mode.SELECT) {
        paintHandles(g, this.selectedShape, strokeScale);
      }
    } finally {
      g.dispose();
    }
  }

  private void paintPixelGrid(Graphics2D g, float sx, float sy) {
    if (sx < 3 && sy < 3) {
      return;
    }
    g.setColor(GRID_COLOR);
    g.setStroke(new BasicStroke(1f));
    for (int x = 1; x < this.tileWidth; x++) {
      int cx = Math.round(x * sx);
      g.drawLine(cx, 0, cx, Math.round(this.tileHeight * sy));
    }
    for (int y = 1; y < this.tileHeight; y++) {
      int cy = Math.round(y * sy);
      g.drawLine(0, cy, Math.round(this.tileWidth * sx), cy);
    }
  }

  private static void paintShape(Graphics2D g, IMapObject object, boolean selected) {
    java.awt.Shape shape = object.getBoundingBox();
    paintShape(g, shape, selected);
  }

  private static void paintShape(Graphics2D g, java.awt.Shape shape, boolean selected) {
    g.setColor(SHAPE_FILL);
    g.fill(shape);
    g.setColor(selected ? Style.accent() : SHAPE_OUTLINE);
    g.draw(shape);
  }

  private static void paintHandles(Graphics2D g, IMapObject shape, float scale) {
    Rectangle2D b = shape.getBoundingBox();
    double bx = b.getX(), by = b.getY(), bw = b.getWidth(), bh = b.getHeight();
    double[][] centers = {
      {bx, by}, {bx + bw, by}, {bx, by + bh}, {bx + bw, by + bh},
      {bx + bw / 2.0, by}, {bx + bw / 2.0, by + bh},
      {bx, by + bh / 2.0}, {bx + bw, by + bh / 2.0}
    };
    java.awt.geom.AffineTransform t = g.getTransform();
    g.setTransform(new java.awt.geom.AffineTransform());
    g.setColor(HANDLE_FILL);
    g.setStroke(new BasicStroke(1f));
    int r = HANDLE_RADIUS;
    for (double[] c : centers) {
      java.awt.geom.Point2D.Float sp = new java.awt.geom.Point2D.Float((float) c[0], (float) c[1]);
      t.transform(sp, sp);
      int sx = Math.round(sp.x);
      int sy = Math.round(sp.y);
      g.fillRect(sx - r, sy - r, r * 2, r * 2);
      g.setColor(HANDLE_STROKE);
      g.drawRect(sx - r, sy - r, r * 2, r * 2);
      g.setColor(HANDLE_FILL);
    }
    g.setTransform(t);
  }

  private static float snap(float v) {
    return Math.round(v);
  }

  void createRectangleForTest(float startX, float startY, float endX, float endY) {
    this.rectangleStart = clamp(startX, startY);
    this.rectangleEnd = clamp(endX, endY);
    commitRectangle();
  }

  void selectShapeForTest(float x, float y) {
    this.selectedShape = shapeAt(new Point2D.Float(x, y));
    updateEnabledState();
  }

  void deleteSelectedShapeForTest() {
    deleteSelectedShape();
  }

  void dragShapeForTest(float dx, float dy) {
    if (this.selectedShape == null) return;
    this.dragOp = DragOp.MOVE;
    this.dragAnchor = new Point2D.Float(0, 0);
    this.dragOriginalBounds = new Rectangle2D.Float(
        (float) this.selectedShape.getX(), (float) this.selectedShape.getY(),
        (float) this.selectedShape.getWidth(), (float) this.selectedShape.getHeight());
    applyDrag(new Point2D.Float(dx, dy));
    commitDrag();
  }

  private Point2D.Float clamp(float x, float y) {
    return new Point2D.Float(Math.clamp(x, 0, this.tileWidth), Math.clamp(y, 0, this.tileHeight));
  }

  private final class CollisionCanvas extends JPanel {
    private CollisionCanvas() {
      setOpaque(true);
      setBackground(Style.raisedSurface());
      addMouseListener(new MouseAdapter() {
        @Override public void mousePressed(MouseEvent event) {
          handlePressed(event);
        }

        @Override public void mouseReleased(MouseEvent event) {
          handleReleased(event);
        }
      });
      addMouseMotionListener(new MouseMotionAdapter() {
        @Override public void mouseDragged(MouseEvent event) {
          handleDragged(event);
        }

        @Override public void mouseMoved(MouseEvent event) {
          handleMoved(event);
        }
      });
    }

    @Override protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);
      paintCanvas(graphics);
    }
  }
}

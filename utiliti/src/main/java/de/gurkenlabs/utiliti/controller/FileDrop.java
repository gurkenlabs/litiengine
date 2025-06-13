package de.gurkenlabs.utiliti.controller;

import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serial;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class makes it easy to drag and drop files from the operating system to a Java program. Any {@code java.awt.Component} can be dropped onto,
 * but only {@code javax.swing.JComponent}s will indicate the drop event with a changed border.
 *
 * <p>
 * To use this class, construct a new {@code FileDrop} by passing it the target component and a {@code Listener} to receive notification when file(s)
 * have been dropped. Here is an example:
 *
 * <p>
 * {@code JPanel myPanel = new JPanel(); new FileDrop( myPanel, new FileDrop.Listener() { public void filesDropped( java.io.File[] files ) { // handle
 * file drop ... } // end filesDropped }); // end FileDrop.Listener }
 *
 * <p>
 * You can specify the border that will appear when files are being dragged by calling the constructor with a {@code javax.swing.border.Border}. Only
 * {@code JComponent}s will show any indication with a border.
 *
 * <p>
 * You can turn on some debugging features by passing a {@code PrintStream} object (such as {@code System.out}) into the full constructor. A
 * {@code null} value will result in no extra debugging information being output.
 */
public class FileDrop {
  private static final Logger LOG = Logger.getLogger(FileDrop.class.getName());
  private static final String ZERO_CHAR_STRING = "" + (char) 0;

  /**
   * Discover if the running JVM is modern enough to have drag and drop.
   */
  private static Boolean supportsDnD;

  private DropTargetListener dropListener;

  /**
   * Constructor with a specified border and debugging optionally turned on. With Debugging turned on, more status messages will be displayed to
   * {@code out}. A common way to use this constructor is with {@code System.out} or {@code System.err}. A {@code null} value for the parameter
   * {@code out} will result in no debugging output.
   *
   * @param c        Component on which files will be dropped.
   * @param listener Listens for {@code filesDropped}.
   * @since 1.0
   */
  public FileDrop(final Component c, final Listener listener) {
    this(c, false, listener);
  }

  /**
   * Full constructor with a specified border and debugging optionally turned on. With Debugging turned on, more status messages will be displayed to
   * {@code out}. A common way to use this constructor is with {@code System.out} or {@code System.err}. A {@code null} value for the parameter
   * {@code out} will result in no debugging output.
   *
   * @param c         Component on which files will be dropped.
   * @param recursive Recursively set children as drop targets.
   * @param listener  Listens for {@code filesDropped}.
   * @since 1.0
   */
  public FileDrop(final Component c, final boolean recursive, final Listener listener) {

    if (!supportsDnD()) {
      LOG.log(Level.WARNING, "FileDrop: Drag and drop is not supported with this JVM");
      return;
    }

    // Make a drop listener
    this.dropListener = new FileDropTargetListener(listener);

    // Make the component (and possibly children) drop targets
    makeDropTarget(c, recursive);
  }

  private static boolean supportsDnD() {
    if (supportsDnD == null) {
      boolean support;
      try {
        Class.forName("java.awt.dnd.DnDConstants");
        support = true;
      } catch (Exception e) {
        support = false;
      }
      supportsDnD = support;
    }
    return supportsDnD;
  }

  private void makeDropTarget(final Component c, boolean recursive) {
    // Make drop target
    final DropTarget dt = new DropTarget();
    try {
      dt.addDropTargetListener(dropListener);
    } catch (TooManyListenersException e) {
      LOG.log(Level.SEVERE, e.getMessage(), e);
    }

    // Listen for hierarchy changes and remove the drop target when the parent
    // gets cleared out.
    c.addHierarchyListener(
      evt -> {
        LOG.log(Level.FINE, "FileDrop: Hierarchy changed.");
        Component parent = c.getParent();
        if (parent == null) {
          c.setDropTarget(null);
          LOG.log(Level.FINE, "FileDrop: Drop target cleared from component.");
        } else {
          new DropTarget(c, this.dropListener);
          LOG.log(Level.FINE, "FileDrop: Drop target added to component.");
        }
      });

    if (c.getParent() != null) {
      new DropTarget(c, dropListener);
    }

    if (recursive && (c instanceof Container container)) {
      // Get it's components
      Component[] comps = container.getComponents();

      // Set it's components as listeners also
      for (Component comp : comps) {
        makeDropTarget(comp, true);
      }
    }
  }

  /**
   * Removes the drag-and-drop hooks from the component and optionally from the all children. You should call this if you add and remove components
   * after you've set up the drag-and-drop. This will recursively unregister all components contained within <var>c</var> if <var>c</var> is a
   * {@link java.awt.Container}.
   *
   * @param c The component to unregister as a drop target
   * @return Was the operation successful?
   * @since 1.0
   */
  public static boolean remove(Component c) {
    return remove(c, true);
  }

  /**
   * Removes the drag-and-drop hooks from the component and optionally from the all children. You should call this if you add and remove components
   * after you've set up the drag-and-drop.
   *
   * @param c         The component to unregister
   * @param recursive Recursively unregister components within a container
   * @return Was the operation successful?
   * @since 1.0
   */
  public static boolean remove(Component c, boolean recursive) {
    // Make sure we support dnd
    if (!supportsDnD()) {
      return false;
    }

    LOG.log(Level.FINE, "FileDrop: Removing drag-and-drop hooks.");
    c.setDropTarget(null);
    if (recursive && (c instanceof Container container)) {
      Component[] comps = container.getComponents();
      for (Component comp : comps) {
        remove(comp, true);
      }

      return true;
    } else {
      return false;
    }
  }

  public static class FileDropTargetListener implements DropTargetListener {
    private final Listener listener;

    public FileDropTargetListener(Listener listener) {
      this.listener = listener;
    }

    @Override
    public void dragEnter(DropTargetDragEvent evt) {
      LOG.log(Level.FINE, "FileDrop: dragEnter event.");

      // Is this an acceptable drag event?
      if (isDragOk(evt)) {
        // Acknowledge that it's okay to enter
        evt.acceptDrag(DnDConstants.ACTION_COPY);
        LOG.log(Level.FINE, "FileDrop: event accepted.");
      } else {
        // Reject the drag event
        evt.rejectDrag();
        LOG.log(Level.FINE, "FileDrop: event rejected.");
      }
    }

    @Override
    public void dragOver(DropTargetDragEvent evt) {
      // This is called continually as long as the mouse is over the drag
      // target.
    }

    @Override
    public void drop(DropTargetDropEvent evt) {
      LOG.log(Level.FINE, "FileDrop: drop event.");
      try {
        // Get whatever was dropped
        Transferable tr = evt.getTransferable();

        // Is it a file list?
        if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
          // Say we'll take it.
          evt.acceptDrop(DnDConstants.ACTION_COPY);
          LOG.log(Level.FINE, "FileDrop: file list accepted.");

          // Get a useful list
          @SuppressWarnings("unchecked")
          List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);

          // Convert list to array
          Path[] filesTemp = new Path[fileList.size()];
          fileList.toArray(filesTemp);

          // Alert listener to drop.
          if (listener != null) {
            listener.filesDropped(filesTemp);
          }

          // Mark that drop is completed.
          evt.getDropTargetContext().dropComplete(true);
          LOG.log(Level.FINE, "FileDrop: drop complete.");
        } else {
          // this section will check for a reader flavor.
          DataFlavor[] flavors = tr.getTransferDataFlavors();
          boolean handled = false;
          for (DataFlavor flavor : flavors) {
            if (!flavor.isRepresentationClassReader()) {
              continue;
            }

            // Say we'll take it.
            evt.acceptDrop(DnDConstants.ACTION_COPY);
            LOG.log(Level.FINE, "FileDrop: reader accepted.");

            Reader reader = flavor.getReaderForText(tr);

            BufferedReader br = new BufferedReader(reader);

            if (listener != null) {
              listener.filesDropped(createFileArray(br));
            }

            // Mark that drop is completed.
            evt.getDropTargetContext().dropComplete(true);
            LOG.log(Level.FINE, "FileDrop: drop complete.");
            handled = true;
            break;
          }

          if (!handled) {
            LOG.log(Level.FINE, "FileDrop: not a file list or reader - abort.");
            evt.rejectDrop();
          }
        }
      } catch (IOException | UnsupportedFlavorException e) {
        LOG.log(Level.SEVERE, e.getMessage(), e);
        evt.rejectDrop();
      }
    }

    @Override
    public void dragExit(DropTargetEvent evt) {
      LOG.log(Level.FINE, "FileDrop: dragExit event.");
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent evt) {
      LOG.log(Level.FINE, "FileDrop: dropActionChanged event.");
      // Is this an acceptable drag event?
      if (isDragOk(evt)) {
        evt.acceptDrag(DnDConstants.ACTION_COPY);
        LOG.log(Level.FINE, "FileDrop: event accepted.");
      } else {
        evt.rejectDrag();
        LOG.log(Level.FINE, "FileDrop: event rejected.");
      }
    }

    /**
     * Determine if the dragged data is a file list.
     */
    private static boolean isDragOk(final DropTargetDragEvent evt) {
      boolean ok = false;

      // Get data flavors being dragged
      DataFlavor[] flavors = evt.getCurrentDataFlavors();

      // See if any of the flavors are a file list
      int i = 0;
      while (!ok && i < flavors.length) {
        // Is the flavor a file list?
        final DataFlavor curFlavor = flavors[i];
        if (curFlavor.equals(DataFlavor.javaFileListFlavor)
          || curFlavor.isRepresentationClassReader()) {
          ok = true;
        }
        i++;
      }

      if (flavors.length == 0) {
        LOG.log(Level.FINE, "FileDrop: no data flavors.");
      }

      return ok;
    }

    private static Path[] createFileArray(BufferedReader bReader) {
      try {
        List<Path> list = new ArrayList<>();
        String line;
        while ((line = bReader.readLine()) != null) {
          try {
            // kde seems to append a 0 char to the end of the reader
            if (ZERO_CHAR_STRING.equals(line)) {
              continue;
            }
            Path file = Path.of(new URI(line));
            list.add(file);
          } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error with {0}: {1}", new String[] {line, ex.getMessage()});
          }
        }

        return list.toArray(new Path[0]);
      } catch (IOException _) {
        LOG.log(Level.SEVERE, "FileDrop: IOException");
      }
      return new Path[0];
    }
  }

  /**
   * Implement this inner interface to listen for when files are dropped. For example your class declaration may begin like this:
   * {@code public class MyClass implements FileDrop.Listener ... public void filesDropped( java.io.File[] files ) { ... } // end filesDropped ... }
   *
   * @since 1.1
   */
  public interface Listener {

    /**
     * This method is called when files have been successfully dropped.
     *
     * @param files An array of {@code File}s that were dropped.
     * @since 1.0
     */
    void filesDropped(Path[] files);
  }

  /**
   * This is the event that fires when files are dropped onto a registered drop target.
   *
   * <p>
   * I'm releasing this code into the Public Domain. Enjoy.
   *
   * @author Robert Harder
   * @author rob@iharder.net
   * @version 1.2
   */
  public static class Event extends EventObject {
    @Serial
    private static final long serialVersionUID = 885026812546045019L;
    private final File[] files;

    /**
     * Constructs an {@link Event} with the array of files that were dropped and the {@link FileDrop} that initiated the event.
     *
     * @param files  The array of files that were dropped
     * @param source The event source
     * @since 1.1
     */
    public Event(File[] files, Object source) {
      super(source);
      this.files = files;
    }

    /**
     * Returns an array of files that were dropped on a registered drop target.
     *
     * @return array of files that were dropped
     * @since 1.1
     */
    public File[] getFiles() {
      return files;
    }
  }

  /**
   * At last an easy way to encapsulate your custom objects for dragging and dropping in your Java programs! When you need to create a
   * {@link java.awt.datatransfer.Transferable} object, use this class to wrap your object. For example:
   *
   * <pre>
   * {@code
   * ...
   * MyCoolClass myObj = new MyCoolClass();
   * Transferable xfer = new TransferableObject( myObj );
   * ...
   * }
   * </pre>
   * <p>
   * Or if you need to know when the data was actually dropped, like when you're moving data out of a list, say, you can use the
   * {@link TransferableObject.Fetcher} inner class to return your object Just in Time. For example:
   *
   * <pre>
   * {@code
   * ...
   * final MyCoolClass myObj = new MyCoolClass();
   *
   * TransferableObject.Fetcher fetcher = new TransferableObject.Fetcher()
   * {   public Object getObject(){ return myObj; }
   * }; // end fetcher
   *
   * Transferable xfer = new TransferableObject( fetcher );
   * ...
   * }
   * </pre>
   * <p>
   * The {@link java.awt.datatransfer.DataFlavor} associated with {@link TransferableObject} has the representation class
   * {@code net.iharder.dnd.TransferableObject.class} and MIME type {@code application/x-net.iharder.dnd.TransferableObject}. This data flavor is
   * accessible via the static {@link #DATA_FLAVOR} property.
   *
   * <p>
   * I'm releasing this code into the Public Domain. Enjoy.
   *
   * @author Robert Harder
   * @author rob@iharder.net
   * @version 1.2
   */
  public static class TransferableObject implements Transferable {
    /**
     * The MIME type for {@link #DATA_FLAVOR} is {@code application/x-net.iharder.dnd.TransferableObject}.
     *
     * @since 1.1
     */
    public static final String MIME_TYPE = "application/x-net.iharder.dnd.TransferableObject";

    /**
     * The default {@link java.awt.datatransfer.DataFlavor} for {@link TransferableObject} has the representation class
     * {@code net.iharder.dnd.TransferableObject.class} and the MIME type {@code application/x-net.iharder.dnd.TransferableObject}.
     *
     * @since 1.1
     */
    public static final DataFlavor DATA_FLAVOR =
      new DataFlavor(FileDrop.TransferableObject.class, MIME_TYPE);

    private Fetcher fetcher;
    private Object data;

    private DataFlavor customFlavor;

    /**
     * Creates a new {@link TransferableObject} that wraps <var>data</var>. Along with the {@link #DATA_FLAVOR} associated with this class, this
     * creates a custom data flavor with a representation class determined from {@code data.getClass()} and the MIME type
     * {@code application/x-net.iharder.dnd.TransferableObject}.
     *
     * @param data The data to transfer
     * @since 1.1
     */
    public TransferableObject(Object data) {
      this.data = data;
      this.customFlavor = new DataFlavor(data.getClass(), MIME_TYPE);
    }

    /**
     * Creates a new {@link TransferableObject} that will return the object that is returned by <var>fetcher</var>. No custom data flavor is set other
     * than the default {@link #DATA_FLAVOR}.
     *
     * @param fetcher The {@link Fetcher} that will return the data object
     * @see Fetcher
     * @since 1.1
     */
    public TransferableObject(Fetcher fetcher) {
      this.fetcher = fetcher;
    }

    /**
     * Creates a new {@link TransferableObject} that will return the object that is returned by <var>fetcher</var>. Along with the
     * {@link #DATA_FLAVOR} associated with this class, this creates a custom data flavor with a representation class <var>dataClass</var> and the
     * MIME type {@code application/x-net.iharder.dnd.TransferableObject}.
     *
     * @param dataClass The {@link java.lang.Class} to use in the custom data flavor
     * @param fetcher   The {@link Fetcher} that will return the data object
     * @see Fetcher
     * @since 1.1
     */
    public TransferableObject(Class<?> dataClass, Fetcher fetcher) {
      this.fetcher = fetcher;
      this.customFlavor = new DataFlavor(dataClass, MIME_TYPE);
    }

    /**
     * Returns the custom {@link java.awt.datatransfer.DataFlavor} associated with the encapsulated object or {@code null} if the {@link Fetcher}
     * constructor was used without passing a {@link java.lang.Class}.
     *
     * @return The custom data flavor for the encapsulated object
     * @since 1.1
     */
    public DataFlavor getCustomDataFlavor() {
      return customFlavor;
    }

    /**
     * Returns a two- or three-element array containing first the custom data flavor, if one was created in the constructors, second the default
     * {@link #DATA_FLAVOR} associated with {@link TransferableObject}, and third the stringFlavor.
     *
     * @return An array of supported data flavors
     * @since 1.1
     */
    @Override
    public DataFlavor[] getTransferDataFlavors() {
      if (customFlavor != null) {
        return new DataFlavor[] {customFlavor, DATA_FLAVOR, DataFlavor.stringFlavor};
      } else {
        return new DataFlavor[] {DATA_FLAVOR, DataFlavor.stringFlavor};
      }
    }

    /**
     * Returns the data encapsulated in this {@link TransferableObject}. If the {@link Fetcher} constructor was used, then this is when the
     * {@link Fetcher#getObject getObject()} method will be called. If the requested data flavor is not supported, then the
     * {@link Fetcher#getObject getObject()} method will not be called.
     *
     * @param flavor The data flavor for the data to return
     * @return The dropped data
     * @since 1.1
     */
    @Override
    public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
      // Native object
      if (flavor.equals(DATA_FLAVOR)) {
        return fetcher == null ? data : fetcher.getObject();
      }

      // String
      if (flavor.equals(DataFlavor.stringFlavor)) {
        return fetcher == null ? data.toString() : fetcher.getObject().toString();
      }

      // We can't do anything else
      throw new UnsupportedFlavorException(flavor);
    }

    /**
     * Returns {@code true} if <var>flavor</var> is one of the supported flavors. Flavors are supported using the {@code equals(...)} method.
     *
     * @param flavor The data flavor to check
     * @return Whether or not the flavor is supported
     * @since 1.1
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      // Native object
      if (flavor.equals(DATA_FLAVOR)) {
        return true;
      }

      // String
      return flavor.equals(DataFlavor.stringFlavor);
    }

    /**
     * Instead of passing your data directly to the {@link TransferableObject} constructor, you may want to know exactly when your data was received
     * in case you need to remove it from its source (or do anyting else to it). When the {@link #getTransferData getTransferData(...)} method is
     * called on the {@link TransferableObject}, the {@link Fetcher}'s {@link #getObject getObject()} method will be called.
     *
     * @author Robert Harder
     * @version 1.1
     * @since 1.1
     */
    public interface Fetcher {
      /**
       * Return the object being encapsulated in the {@link TransferableObject}.
       *
       * @return The dropped object
       * @since 1.1
       */
      Object getObject();
    }
  }
}

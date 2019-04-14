package de.gurkenlabs.utiliti.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.utiliti.Cursors;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.components.MapComponent;
import de.gurkenlabs.utiliti.swing.menus.AddMenu;

@SuppressWarnings("serial")
public final class ToolBar extends JToolBar {
  private boolean isChanging;

  public ToolBar() {
    JButton cr = initButton(Icons.ToolBar.CREATE, a -> EditorScreen.instance().create());
    JButton op = initButton(Icons.ToolBar.LOAD, a -> EditorScreen.instance().load());
    JButton sv = initButton(Icons.ToolBar.SAVE, a -> EditorScreen.instance().save(false));
    JButton undo = initButton(Icons.ToolBar.UNDO, a -> UndoManager.instance().undo());
    JButton redo = initButton(Icons.ToolBar.REDO, a -> UndoManager.instance().redo());
    undo.setEnabled(false);
    redo.setEnabled(false);

    JToggleButton place = new JToggleButton();
    place.setIcon(Icons.ToolBar.ADD);
    requestFocusOnMouseDown(place);

    JToggleButton ed = new JToggleButton();
    ed.setIcon(Icons.ToolBar.EDIT);
    ed.setSelected(true);
    requestFocusOnMouseDown(ed);

    JToggleButton mv = new JToggleButton();
    mv.setIcon(Icons.ToolBar.MOVE);
    mv.setEnabled(false);
    requestFocusOnMouseDown(mv);

    ed.addActionListener(a -> {
      ed.setSelected(true);
      place.setSelected(false);
      mv.setSelected(false);
      this.isChanging = true;
      EditorScreen.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_EDIT);
      this.isChanging = false;

      System.out.println("edit action");
      Game.window().getRenderComponent().setCursor(Cursors.DEFAULT, 0, 0);
    });

    place.addActionListener(a -> {
      AddMenu.getPopup().show(place, 0, place.getHeight());
      place.setSelected(true);
      ed.setSelected(false);
      mv.setSelected(false);
      this.isChanging = true;
      EditorScreen.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_CREATE);
      this.isChanging = false;
    });

    mv.addActionListener(a -> {
      mv.setSelected(true);
      ed.setSelected(false);
      place.setSelected(false);
      this.isChanging = true;
      System.out.println("move action");
      EditorScreen.instance().getMapComponent().setEditMode(MapComponent.EDITMODE_MOVE);
      this.isChanging = false;

      Game.window().getRenderComponent().setCursor(Cursors.MOVE, 0, 0);
    });

    EditorScreen.instance().getMapComponent().onEditModeChanged(i -> {

      if (this.isChanging) {
        return;
      }
      
      System.out.println("changing" + i);

      if (i == MapComponent.EDITMODE_CREATE) {
        ed.setSelected(false);
        mv.setSelected(false);
        place.setSelected(true);
        Game.window().getRenderComponent().setCursor(Cursors.ADD, 0, 0);
      }else if (i == MapComponent.EDITMODE_EDIT) {
        place.setSelected(false);
        mv.setSelected(false);
        ed.setSelected(true);
        Game.window().getRenderComponent().setCursor(Cursors.DEFAULT, 0, 0);
      }else if (i == MapComponent.EDITMODE_MOVE) {
        if (!mv.isEnabled()) {
          return;
        }

        ed.setSelected(false);
        place.setSelected(false);
        mv.setSelected(true);
        Game.window().getRenderComponent().setCursor(Cursors.MOVE, 0, 0);
      }
    });

    JButton del = new JButton();
    del.setIcon(Icons.DELETE);
    del.setEnabled(false);
    del.addActionListener(a -> EditorScreen.instance().getMapComponent().delete());

    // copy
    JButton cop = new JButton();
    cop.setIcon(Icons.ToolBar.COPY);
    cop.setEnabled(false);
    ActionListener copyAction = a -> EditorScreen.instance().getMapComponent().copy();
    cop.addActionListener(copyAction);
    cop.getModel().setMnemonic('C');
    KeyStroke keyStroke = KeyStroke.getKeyStroke('C', Event.CTRL_MASK, false);
    cop.registerKeyboardAction(copyAction, keyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // paste
    JButton paste = new JButton();
    paste.setIcon(Icons.ToolBar.PASTE);

    ActionListener pasteAction = a -> EditorScreen.instance().getMapComponent().paste();
    paste.addActionListener(pasteAction);
    paste.getModel().setMnemonic('V');
    KeyStroke keyStrokePaste = KeyStroke.getKeyStroke('V', Event.CTRL_MASK, false);
    paste.registerKeyboardAction(pasteAction, keyStrokePaste, JComponent.WHEN_IN_FOCUSED_WINDOW);

    // cut
    JButton cut = new JButton();
    cut.setIcon(Icons.ToolBar.CUT);
    cut.setEnabled(false);
    ActionListener cutAction = a -> EditorScreen.instance().getMapComponent().cut();
    cut.addActionListener(cutAction);
    cut.getModel().setMnemonic('X');
    KeyStroke keyStrokeCut = KeyStroke.getKeyStroke('X', Event.CTRL_MASK, false);
    cut.registerKeyboardAction(cutAction, keyStrokeCut, JComponent.WHEN_IN_FOCUSED_WINDOW);

    EditorScreen.instance().getMapComponent().onFocusChanged(mo -> {
      if (mv.isSelected()) {
        mv.setSelected(false);
        ed.setSelected(true);
      }

      mv.setEnabled(mo != null);
      del.setEnabled(mo != null);
      cop.setEnabled(mo != null);
      cut.setEnabled(mo != null);
      undo.setEnabled(UndoManager.instance().canUndo());
      redo.setEnabled(UndoManager.instance().canRedo());
      paste.setEnabled(EditorScreen.instance().getMapComponent().getCopiedBlueprint() != null);
    });

    EditorScreen.instance().getMapComponent().onCopyTargetChanged(target -> paste.setEnabled(target != null));
    EditorScreen.instance().getMapComponent().onEditModeChanged(mode -> paste.setEnabled(EditorScreen.instance().getMapComponent().getCopiedBlueprint() != null));

    UndoManager.onUndoStackChanged(manager -> {
      EditorScreen.instance().getMapComponent().updateTransformControls();
      undo.setEnabled(manager.canUndo());
      redo.setEnabled(manager.canRedo());
    });

    JButton colorButton = new JButton();
    colorButton.setIcon(Icons.ToolBar.COLOR);
    colorButton.setEnabled(false);

    JTextField colorText = new JTextField();
    colorText.setHorizontalAlignment(SwingConstants.CENTER);
    colorText.setMinimumSize(new Dimension(70, 20));
    colorText.setMaximumSize(new Dimension(70, 50));
    colorText.setEnabled(false);

    JSpinner spinnerAmbientAlpha = new JSpinner();
    spinnerAmbientAlpha.setToolTipText("Adjust ambient alpha.");
    spinnerAmbientAlpha.setModel(new SpinnerNumberModel(0, 0, 255, 1));
    spinnerAmbientAlpha.setMaximumSize(new Dimension(50, 50));
    spinnerAmbientAlpha.setEnabled(true);
    spinnerAmbientAlpha.addChangeListener(e -> {
      if (Game.world().environment() == null || Game.world().environment().getMap() == null || this.isChanging) {
        return;
      }

      Game.world().environment().getAmbientLight().setAlpha((int) spinnerAmbientAlpha.getValue());
      String hex = ColorHelper.encode(Game.world().environment().getAmbientLight().getColor());
      colorText.setText(hex);
      Game.world().environment().getMap().setValue(MapProperty.AMBIENTCOLOR, hex);
    });

    colorButton.addActionListener(a -> {
      if (Game.world().environment() == null || Game.world().environment().getMap() == null || this.isChanging) {
        return;
      }

      Color color = null;
      if (colorText.getText() != null && !colorText.getText().isEmpty()) {
        Color solid = ColorHelper.decode(colorText.getText());
        color = new Color(solid.getRed(), solid.getGreen(), solid.getBlue(), (int) spinnerAmbientAlpha.getValue());
      }
      Color result = JColorChooser.showDialog(null, Resources.strings().get("panel_selectAmbientColor"), color);
      if (result == null) {
        return;
      }

      spinnerAmbientAlpha.setValue(result.getAlpha());

      Game.world().environment().getMap().setValue(MapProperty.AMBIENTCOLOR, colorText.getText());
      Game.world().environment().getAmbientLight().setColor(result);
      String hex = ColorHelper.encode(Game.world().environment().getAmbientLight().getColor());
      colorText.setText(hex);
    });

    EditorScreen.instance().getMapComponent().onMapLoaded(map -> {
      this.isChanging = true;
      colorButton.setEnabled(map != null);
      spinnerAmbientAlpha.setEnabled(map != null);

      String colorValue = map.getStringValue(MapProperty.AMBIENTCOLOR, "#00000000");
      colorText.setText(colorValue);
      Color color = ColorHelper.decode(colorText.getText());
      if (color != null) {
        spinnerAmbientAlpha.setValue(color.getAlpha());
      }

      this.isChanging = false;
    });

    this.add(cr);
    this.add(op);
    this.add(sv);
    this.addSeparator();

    this.add(undo);
    this.add(redo);
    this.addSeparator();

    this.add(place);
    this.add(ed);
    this.add(mv);
    this.add(del);
    this.add(cop);
    this.add(paste);
    this.add(cut);
    this.addSeparator();

    this.add(colorButton);
    this.add(Box.createHorizontalStrut(5));
    this.add(colorText);
    this.add(Box.createHorizontalStrut(5));
    this.add(spinnerAmbientAlpha);
  }

  private static JButton initButton(Icon icon, ActionListener listener) {
    JButton button = new JButton();
    button.setIcon(icon);
    button.addActionListener(listener);
    requestFocusOnMouseDown(button);
    return button;
  }

  private static void requestFocusOnMouseDown(JComponent button) {
    button.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        super.mousePressed(e);

        if (button.isEnabled()) {
          button.requestFocus();
        }
      }
    });
  }
}

package de.gurkenlabs.utiliti;

import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;

import de.gurkenlabs.litiengine.resources.Resources;

public final class Style {
  private static final Logger log = Logger.getLogger(Style.class.getName());

  public static void initSwingComponentStyle() {
    try {
      JPopupMenu.setDefaultLightWeightPopupEnabled(false);
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.getDefaults().put("SplitPane.border", BorderFactory.createEmptyBorder());
      setDefaultSwingFont(new FontUIResource(Resources.fonts().get("OpenSans.ttf", Font.PLAIN, 11)));
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
  }

  public static void setDefaultSwingFont(FontUIResource font) {
    UIManager.put("Button.font", font);
    UIManager.put("ToggleButton.font", font);
    UIManager.put("RadioButton.font", font);
    UIManager.put("CheckBox.font", font);
    UIManager.put("ColorChooser.font", font);
    UIManager.put("ComboBox.font", font);
    UIManager.put("Label.font", font);
    UIManager.put("List.font", font);
    UIManager.put("MenuBar.font", font);
    UIManager.put("MenuItem.font", font);
    UIManager.put("RadioButtonMenuItem.font", font);
    UIManager.put("CheckBoxMenuItem.font", font);
    UIManager.put("Menu.font", font);
    UIManager.put("PopupMenu.font", font);
    UIManager.put("OptionPane.font", font);
    UIManager.put("Panel.font", font);
    UIManager.put("ProgressBar.font", font);
    UIManager.put("ScrollPane.font", font);
    UIManager.put("Viewport.font", font);
    UIManager.put("TabbedPane.font", font);
    UIManager.put("Table.font", font);
    UIManager.put("TableHeader.font", font);
    UIManager.put("TextField.font", font);
    UIManager.put("PasswordField.font", font);
    UIManager.put("TextArea.font", font);
    UIManager.put("TextPane.font", font);
    UIManager.put("EditorPane.font", font);
    UIManager.put("TitledBorder.font", font);
    UIManager.put("ToolBar.font", font);
    UIManager.put("ToolTip.font", font);
    UIManager.put("Tree.font", font);
  }
}

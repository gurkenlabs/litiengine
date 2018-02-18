package de.gurkenlabs.utiliti.swing.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.utiliti.EditorScreen;
import de.gurkenlabs.utiliti.swing.TagPanel;

@SuppressWarnings("serial")
public class MapObjectPanel extends PropertyPanel<IMapObject> {
  private final Map<MapObjectType, PropertyPanel<IMapObject>> panels;
  private PropertyPanel<IMapObject> currentPanel;
  private JPanel componentPanel;
  private JPanel collWrapper = new JPanel();
  private final CollisionPanel collisionPanel;
  private final CustomPanel customPanel;
  private final JTextField textFieldName;
  private final JComboBox<MapObjectType> comboBoxType;
  private final JSpinner spinnerY;
  private final JSpinner spinnerX;
  private final JSpinner spinnerWidth;
  private final JSpinner spinnerHeight;
  private final JLabel labelEntityID;
  private TagPanel tagPanel;
  private JLabel lblTags;

  /**
   * Create the panel.
   */
  public MapObjectPanel() {
    this.panels = new ConcurrentHashMap<>();
    this.panels.put(MapObjectType.PROP, new PropPanel());
    this.panels.put(MapObjectType.COLLISIONBOX, new CollisionBoxPanel());
    this.panels.put(MapObjectType.STATICSHADOW, new StaticShadowPanel());
    this.panels.put(MapObjectType.TRIGGER, new TriggerPanel());
    this.panels.put(MapObjectType.LIGHTSOURCE, new LightSourcePanel());
    this.panels.put(MapObjectType.DECORMOB, new DecorMobPanel());
    this.panels.put(MapObjectType.SPAWNPOINT, new SpawnpointPanel());
    this.panels.put(MapObjectType.EMITTER, new EmitterPanel());
    this.collisionPanel = new CollisionPanel();
    this.customPanel = new CustomPanel();

    setMinimumSize(new Dimension(250, 500));

    JLabel lblX = new JLabel(Resources.get("panel_x"));

    JLabel lblYcoordinate = new JLabel(Resources.get("panel_y"));

    JLabel lblWidth = new JLabel(Resources.get("panel_width"));

    JLabel lblHeight = new JLabel(Resources.get("panel_height"));

    JLabel lblName = new JLabel(Resources.get("panel_name"));

    this.textFieldName = new JTextField();
    this.textFieldName.setColumns(10);

    JLabel lblType = new JLabel(Resources.get("panel_type"));
    this.comboBoxType = new JComboBox<>();
    this.comboBoxType.setModel(new DefaultComboBoxModel<MapObjectType>(MapObjectType.values()));

    this.spinnerX = new JSpinner();
    this.spinnerY = new JSpinner();
    this.spinnerWidth = new JSpinner();
    this.spinnerHeight = new JSpinner();

    JLabel lblNewLabel = new JLabel("ID");
    lblNewLabel.setFont(lblNewLabel.getFont().deriveFont(Font.BOLD).deriveFont(12f));

    this.labelEntityID = new JLabel("####");
    this.labelEntityID.setFont(labelEntityID.getFont().deriveFont(12f));

    componentPanel = new JPanel();
    componentPanel.setBorder(null);

    this.tagPanel = new TagPanel();

    lblTags = new JLabel("tags");
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.TRAILING).addComponent(componentPanel, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
        .addGroup(groupLayout.createSequentialGroup().addContainerGap()
            .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(lblNewLabel, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE).addComponent(lblX, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblWidth, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE).addComponent(lblName, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE).addComponent(lblTags, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE))
                .addComponent(lblType, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(tagPanel, GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE).addComponent(labelEntityID)
                .addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(spinnerWidth, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE).addComponent(spinnerX, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false).addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE).addComponent(lblYcoordinate, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)).addGap(0)
                    .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(spinnerY, GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE).addComponent(spinnerHeight, GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)))
                .addComponent(comboBoxType, 0, 372, Short.MAX_VALUE).addComponent(textFieldName, GroupLayout.DEFAULT_SIZE, 372, Short.MAX_VALUE))
            .addGap(6)));
    groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup().addContainerGap().addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblNewLabel).addComponent(labelEntityID)).addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(lblX).addComponent(spinnerX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblYcoordinate).addComponent(spinnerY, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(spinnerWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblHeight, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                .addComponent(spinnerHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblWidth, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(textFieldName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblName, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE))
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGap(7).addComponent(tagPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(groupLayout.createSequentialGroup().addPreferredGap(ComponentPlacement.UNRELATED).addComponent(lblTags, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE).addComponent(comboBoxType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(lblType, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)).addGap(10)
            .addComponent(componentPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    componentPanel.setLayout(new BorderLayout(0, 0));
    collWrapper.setLayout(new BorderLayout());
    componentPanel.add(this.collWrapper, BorderLayout.NORTH);
    componentPanel.add(this.customPanel, BorderLayout.SOUTH);
    setLayout(groupLayout);

    this.setupChangedListeners();
    this.comboBoxType.setSelectedItem(MapObjectType.AREA);
  }

  public MapObjectType getObjectType() {
    return (MapObjectType) this.comboBoxType.getSelectedItem();
  }

  @Override
  public void bind(IMapObject mapObject) {
    super.bind(mapObject);
    this.isFocussing = true;
    if (this.currentPanel != null) {
      this.currentPanel.bind(this.getDataSource());
    }

    if (this.collisionPanel != null) {
      this.collisionPanel.bind(this.getDataSource());
    }

    this.customPanel.bind(this.getDataSource());
    this.isFocussing = false;
  }

  public void setupControls() {
    Input.keyboard().onKeyPressed(KeyEvent.VK_RIGHT, e -> {
      if (Game.getScreenManager().getRenderComponent().hasFocus()) {
        this.spinnerX.setValue(this.spinnerX.getNextValue());
        EditorScreen.instance().getMapComponent().updateTransformControls();
      }
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_LEFT, e -> {
      if (Game.getScreenManager().getRenderComponent().hasFocus()) {
        this.spinnerX.setValue(this.spinnerX.getPreviousValue());
        EditorScreen.instance().getMapComponent().updateTransformControls();
      }
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_UP, e -> {
      if (Game.getScreenManager().getRenderComponent().hasFocus()) {
        this.spinnerY.setValue(this.spinnerY.getPreviousValue());
        EditorScreen.instance().getMapComponent().updateTransformControls();
      }
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_DOWN, e -> {
      if (Game.getScreenManager().getRenderComponent().hasFocus()) {
        this.spinnerY.setValue(this.spinnerY.getNextValue());
        EditorScreen.instance().getMapComponent().updateTransformControls();
      }
    });
  }

  private void switchPanel(MapObjectType type) {
    PropertyPanel<IMapObject> panel = this.panels.get(type);
    if (panel == null) {
      if (this.currentPanel != null) {
        this.componentPanel.remove(this.currentPanel);
        this.currentPanel.bind(null);
        this.currentPanel = null;
        this.componentPanel.revalidate();
        this.componentPanel.repaint();
      }

      this.collWrapper.remove(this.collisionPanel);
      this.collWrapper.revalidate();
      this.collWrapper.repaint();
      return;
    }

    if (panel == this.currentPanel) {
      return;
    }

    if (this.currentPanel != null) {
      this.currentPanel.bind(null);
      this.componentPanel.remove(this.currentPanel);
    }

    this.componentPanel.add(panel, BorderLayout.CENTER);

    // TODO: support all types that implement ICollisionEntity
    if (type == MapObjectType.PROP || type == MapObjectType.DECORMOB) {
      this.collWrapper.add(this.collisionPanel);
      this.collWrapper.revalidate();
      this.collWrapper.repaint();
    } else {
      this.collWrapper.remove(this.collisionPanel);
      this.collWrapper.revalidate();
      this.collWrapper.repaint();
    }

    this.currentPanel = panel;
    this.currentPanel.bind(this.getDataSource());
    this.componentPanel.revalidate();
    this.componentPanel.repaint();
  }

  @Override
  protected void clearControls() {
    // clear controls
    this.spinnerX.setValue(0);
    this.spinnerY.setValue(0);
    this.spinnerWidth.setValue(0);
    this.spinnerHeight.setValue(0);
    if (this.getDataSource() == null) {
      this.comboBoxType.setSelectedItem(MapObjectType.AREA);
    }
    this.textFieldName.setText("");
    this.labelEntityID.setText("####");
    this.comboBoxType.setEnabled(true);
    this.tagPanel.clear();
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.spinnerX.setValue((int) mapObject.getLocation().getX());
    this.spinnerY.setValue((int) mapObject.getLocation().getY());
    this.spinnerWidth.setValue((int) mapObject.getDimension().getWidth());
    this.spinnerHeight.setValue((int) mapObject.getDimension().getHeight());

    MapObjectType type = MapObjectType.get(mapObject.getType());
    this.comboBoxType.setSelectedItem(type);
    this.textFieldName.setText(mapObject.getName());
    this.labelEntityID.setText(Integer.toString(mapObject.getId()));
    this.comboBoxType.setEnabled(false);

    this.tagPanel.bind(mapObject.getCustomProperty(MapObjectProperty.TAGS));
  }

  private void setupChangedListeners() {
    comboBoxType.addItemListener(new MapObjectPropertyItemListener(m -> {
      MapObjectType type = (MapObjectType) comboBoxType.getSelectedItem();
      m.setType(type.toString());
    }));

    comboBoxType.addItemListener(e -> {
      MapObjectType type = (MapObjectType) comboBoxType.getSelectedItem();
      switchPanel(type);
    });

    this.textFieldName.addFocusListener(new MapObjectPropteryFocusListener(m -> m.setName(textFieldName.getText())));

    this.textFieldName.addActionListener(new MapObjectPropertyActionListener(m -> m.setName(textFieldName.getText())));

    this.spinnerX.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setX((int) spinnerX.getValue());
      EditorScreen.instance().getMapComponent().updateTransformControls();
    }));

    this.spinnerY.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setY((int) spinnerY.getValue());
      EditorScreen.instance().getMapComponent().updateTransformControls();
    }));

    this.spinnerWidth.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setWidth((int) spinnerWidth.getValue());
      EditorScreen.instance().getMapComponent().updateTransformControls();
    }));

    this.spinnerHeight.addChangeListener(new MapObjectPropertyChangeListener(m -> {
      m.setHeight((int) spinnerHeight.getValue());
      EditorScreen.instance().getMapComponent().updateTransformControls();
    }));
    
    this.tagPanel.addActionListener(new MapObjectPropertyActionListener(m -> m.setCustomProperty(MapObjectProperty.TAGS, this.tagPanel.getTagsString())));
  }
}

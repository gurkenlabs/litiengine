package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.Customization.ToggleButton;
import com.github.weisj.darklaf.ui.togglebutton.ToggleButtonConstants;
import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.Particle;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.particles.ParticleType;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterAttributes;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.components.EmitterPanel.EmitterPropertyGroup;
import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

public abstract class EmitterPropertyPanel extends PropertyPanel {

  protected transient Emitter emitter;

  private EmitterPropertyPanel() {
    super();
    this.setBorder(STANDARDBORDER);
  }

  public static EmitterPropertyPanel getEmitterPropertyPanel(EmitterPropertyGroup category) {
    return switch (category) {
      case EMISSION -> new EmissionPanel();
      case APPEARANCE -> new ParticleStylePanel();
      case TRANSFORM -> new CompositePanel(
          new String[] {"emitter_size", "emitter_origin", "emitter_rotation"},
          new ParticleSizePanel(), new ParticleOriginPanel(), new ParticleRotationPanel());
      case PHYSICS -> new CompositePanel(
          new String[] {"emitter_velocity", "emitter_acceleration", "emitter_collision"},
          new ParticleVelocityPanel(), new ParticleAccelerationPanel(), new ParticleCollisionPanel());
    };
  }

  protected abstract LayoutManager createLayout();

  protected abstract void setupChangedListeners();

  private static final class CompositePanel extends EmitterPropertyPanel {
    private final EmitterPropertyPanel[] panels;

    private CompositePanel(String[] titleKeys, EmitterPropertyPanel... panels) {
      this.panels = panels;
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      for (int index = 0; index < panels.length; index++) {
        CollapsibleSection section = new CollapsibleSection(titleKeys[index], panels[index]);
        section.setInfoText(titleKeys[index] + "_info");
        add(section);
        if (index < panels.length - 1) {
          add(javax.swing.Box.createVerticalStrut(CONTROL_MARGIN * 2));
        }
      }
    }

    @Override
    public void bind(IMapObject mapObject) {
      for (EmitterPropertyPanel panel : this.panels) {
        panel.bind(mapObject);
      }
    }

    @Override
    public void bindAll(java.util.List<IMapObject> mapObjects) {
      for (EmitterPropertyPanel panel : this.panels) {
        panel.bindAll(mapObjects);
      }
    }

    @Override
    protected LayoutManager createLayout() {
      return new BorderLayout();
    }

    @Override
    protected void setupChangedListeners() {
      // Child panels own their listeners.
    }

    @Override
    protected void clearControls() {
      // Child panels are bound directly.
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      // Child panels are bound directly.
    }
  }

  private static class EmissionPanel extends EmitterPropertyPanel {
    private final JSpinner spawnRateSpinner;
    private final JSpinner spawnAmountSpinner;
    private final JSpinner updateDelaySpinner;
    private final JSpinner durationSpinner;
    private final JToggleButton infiniteDuration;
    private final JSpinner maxParticlesSpinner;
    private final DualSpinner ttl;
    private int lastFiniteDuration = 1000;

    private EmissionPanel() {
      super();
      spawnRateSpinner =
        new JSpinner(
          new SpinnerNumberModel(
            EmitterAttributes.DEFAULT_SPAWNRATE, 10, Integer.MAX_VALUE, STEP_COARSE));
      spawnAmountSpinner =
        new JSpinner(new SpinnerNumberModel(EmitterAttributes.DEFAULT_SPAWNAMOUNT, 1, 500, STEP_ONE));
      updateDelaySpinner =
        new JSpinner(
          new SpinnerNumberModel(
            EmitterAttributes.DEFAULT_UPDATERATE, 0, Integer.MAX_VALUE, STEP_COARSE));
      durationSpinner =
        new JSpinner(
          new SpinnerNumberModel(
            EmitterAttributes.DEFAULT_DURATION, 0, Integer.MAX_VALUE, STEP_SPARSE));
      infiniteDuration = Style.iconToggleButton(null, false);
      infiniteDuration.setText(Resources.strings().get("emitter_durationInfinite"));
      maxParticlesSpinner =
        new JSpinner(
          new SpinnerNumberModel(
            EmitterAttributes.DEFAULT_MAXPARTICLES, 1, Integer.MAX_VALUE, STEP_ONE));
      ttl =
        new DualSpinner(
          MapObjectProperty.Particle.TTL_MIN,
          MapObjectProperty.Particle.TTL_MAX,
          Integer.MIN_VALUE,
          Integer.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_PARTICLE_TTL,
          EmitterAttributes.DEFAULT_MAX_PARTICLE_TTL,
          STEP_SPARSE);
      ttl.setRangePresentation("ms");
      compactSpinner(spawnRateSpinner);
      compactSpinner(spawnAmountSpinner);
      compactSpinner(updateDelaySpinner);
      compactSpinner(durationSpinner);
      compactSpinner(maxParticlesSpinner);
      durationSpinner.setToolTipText(Resources.strings().get("emitter_duration_tip"));
      ttl.setToolTipText(Resources.strings().get("emitter_particleTTL_tip"));
      updateDelaySpinner.setToolTipText(Resources.strings().get("emitter_updateDelay_tip"));
      JPanel durationControl = new JPanel(new GridLayout(1, 2, CONTROL_MARGIN * 2, 0));
      durationControl.setOpaque(false);
      durationControl.add(infiniteDuration);
      durationControl.add(unitControl(durationSpinner, "ms"));

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      CollapsibleSection spawn = new CollapsibleSection("emitter_sectionSpawn", formPanel(
          new LayoutItem("emitter_spawnrate", unitControl(spawnRateSpinner, "ms")),
          new LayoutItem("emitter_spawnamount", unitControl(
              spawnAmountSpinner, Resources.strings().get("unit_particles")))));
      spawn.setInfoText("emitter_sectionSpawn_info");
      add(spawn);
      add(javax.swing.Box.createVerticalStrut(CONTROL_MARGIN * 2));
      CollapsibleSection limits = new CollapsibleSection("emitter_sectionLimits", formPanel(
          new LayoutItem("emitter_duration", durationControl),
          new LayoutItem("emitter_maxparticles", unitControl(
              maxParticlesSpinner, Resources.strings().get("unit_particles"))),
          new LayoutItem("emitter_particleTTL", ttl)));
      limits.setInfoText("emitter_sectionLimits_info");
      add(limits);
      add(javax.swing.Box.createVerticalStrut(CONTROL_MARGIN * 2));
      CollapsibleSection advanced = new CollapsibleSection("emitter_sectionAdvanced", formPanel(
          new LayoutItem("emitter_updateDelay", unitControl(updateDelaySpinner, "ms"))), false);
      advanced.setInfoText("emitter_sectionAdvanced_info");
      add(advanced);
      setupChangedListeners();
      updateDurationMode();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      ttl.bind(mapObject);
    }

    @Override
    public void bindAll(java.util.List<IMapObject> mapObjects) {
      super.bindAll(mapObjects);
      ttl.bindAll(mapObjects);
    }

    @Override
    protected LayoutManager createLayout() {
      return new BorderLayout();
    }

    @Override
    protected void clearControls() {
      spawnRateSpinner.setValue(EmitterAttributes.DEFAULT_SPAWNRATE);
      spawnAmountSpinner.setValue(EmitterAttributes.DEFAULT_SPAWNAMOUNT);
      updateDelaySpinner.setValue(EmitterAttributes.DEFAULT_UPDATERATE);
      durationSpinner.setValue(EmitterAttributes.DEFAULT_DURATION);
      maxParticlesSpinner.setValue(EmitterAttributes.DEFAULT_MAXPARTICLES);
      emitter = null;
      updateDurationMode();
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      emitter = Game.world().environment().getEmitter(mapObject.getId());
      spawnRateSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNRATE, 0));
      spawnAmountSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.SPAWNAMOUNT, 0));
      updateDelaySpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.UPDATERATE, 0));
      durationSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.DURATION, 0));
      updateDurationMode();
      maxParticlesSpinner.setValue(mapObject.getIntValue(MapObjectProperty.Emitter.MAXPARTICLES, 0));
    }

    @Override
    protected void setupChangedListeners() {
      setup(spawnRateSpinner, MapObjectProperty.Emitter.SPAWNRATE);
      setup(spawnAmountSpinner, MapObjectProperty.Emitter.SPAWNAMOUNT);
      setup(updateDelaySpinner, MapObjectProperty.Emitter.UPDATERATE);
      setup(durationSpinner, MapObjectProperty.Emitter.DURATION);
      infiniteDuration.addActionListener(event -> {
        if (infiniteDuration.isSelected()) {
          int duration = ((Number) durationSpinner.getValue()).intValue();
          if (duration > 0) {
            lastFiniteDuration = duration;
          }
          durationSpinner.setValue(0);
        } else if (((Number) durationSpinner.getValue()).intValue() == 0) {
          durationSpinner.setValue(lastFiniteDuration);
        }
        updateDurationMode();
      });
      setup(maxParticlesSpinner, MapObjectProperty.Emitter.MAXPARTICLES);
    }

    private void updateDurationMode() {
      int duration = ((Number) durationSpinner.getValue()).intValue();
      if (duration > 0) {
        lastFiniteDuration = duration;
      }
      infiniteDuration.setSelected(duration == 0);
      durationSpinner.setEnabled(duration != 0);
    }
  }

  private static class ParticleStylePanel extends EmitterPropertyPanel {
    private final JComboBox<ParticleType> comboBoxParticleType;
    private final JComboBox<ParticleType> shape;
    private final JToggleButton shapeSource;
    private final JToggleButton textSource;
    private final JToggleButton spriteSource;
    private final JToggleButton fade;
    private final JToggleButton outlineOnly;
    private final JComboBox<RenderMode> renderMode;
    private final DualSpinner outlineThickness;
    private final JToggleButton antiAliasing;
    private final EmitterColorPanel colorPanel;
    private final EmitterTextPanel textPanel;
    private final EmitterSpritePanel spritePanel;
    private final JPanel sourceSettings;
    private final CardLayout sourceSettingsLayout;
    private final JLabel sourceSettingsLabel;
    private final CollapsibleSection fillSection;
    private final JPanel colorsSection;

    private ParticleStylePanel() {
      super();
      comboBoxParticleType = new JComboBox<>(new DefaultComboBoxModel<>(ParticleType.values()));
      shape = new JComboBox<>(new ParticleType[] {
        ParticleType.RECTANGLE,
        ParticleType.ELLIPSE,
        ParticleType.TRIANGLE,
        ParticleType.DIAMOND,
        ParticleType.LINE
      });
      shape.setRenderer((list, value, index, selected, focus) -> {
        JLabel label = (JLabel) new DefaultListCellRenderer()
            .getListCellRendererComponent(list, value, index, selected, focus);
        if (value != null) {
          label.setText(Resources.strings().get("particle_shape_" + value.name().toLowerCase()));
          label.setIcon(new ShapeIcon(value));
        }
        return label;
      });
      shapeSource = sourceButton("emitter_sourceShape");
      textSource = sourceButton("emitter_sourceText");
      spriteSource = sourceButton("emitter_sourceSprite");
      ButtonGroup sourceGroup = new ButtonGroup();
      sourceGroup.add(shapeSource);
      sourceGroup.add(textSource);
      sourceGroup.add(spriteSource);
      fade = new JToggleButton();
      fade.putClientProperty(
        ToggleButton.KEY_VARIANT, ToggleButton.VARIANT_SLIDER);
      outlineOnly = new JToggleButton();
      outlineOnly.putClientProperty(
        ToggleButton.KEY_VARIANT, ToggleButton.VARIANT_SLIDER);
      renderMode = new JComboBox<>(RenderMode.values());
      outlineThickness =
        new DualSpinner(
          Particle.OUTLINETHICKNESS_MIN,
          Particle.OUTLINETHICKNESS_MAX,
          0,
          Short.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_WIDTH,
          EmitterAttributes.DEFAULT_MAX_WIDTH,
          STEP_FINE);
      outlineThickness.setRangePresentation("px");
      antiAliasing = new JToggleButton();
      antiAliasing.putClientProperty(
        ToggleButton.KEY_VARIANT, ToggleButton.VARIANT_SLIDER);
      colorPanel = new EmitterColorPanel();
      textPanel = new EmitterTextPanel();
      spritePanel = new EmitterSpritePanel();
      sourceSettingsLayout = new CardLayout();
      sourceSettings = new JPanel(sourceSettingsLayout);
      sourceSettingsLabel = new JLabel();
      sourceSettings.setOpaque(false);
      sourceSettings.add(shape, "shape");
      sourceSettings.add(textPanel, "text");
      sourceSettings.add(spritePanel, "sprite");

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      CollapsibleSection renderingSection = new CollapsibleSection(
          "emitter_sectionRendering", createRenderingPanel());
      renderingSection.setInfoText("emitter_sectionRendering_info");
      fillSection = new CollapsibleSection(
          "emitter_sectionFillOutline", createFillPanel());
      fillSection.setInfoText("emitter_sectionFillOutline_info");
      colorsSection = colorPanel;
      add(renderingSection);
      add(javax.swing.Box.createVerticalStrut(CONTROL_MARGIN * 2));
      add(fillSection);
      add(javax.swing.Box.createVerticalStrut(CONTROL_MARGIN * 2));
      add(colorsSection);
      setupChangedListeners();
      updateSourceControls();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      colorPanel.bind(mapObject);
      textPanel.bind(mapObject);
      spritePanel.bind(mapObject);
      outlineThickness.bind(mapObject);
    }

    @Override
    public void bindAll(java.util.List<IMapObject> mapObjects) {
      super.bindAll(mapObjects);
      colorPanel.bindAll(mapObjects);
      textPanel.bindAll(mapObjects);
      spritePanel.bindAll(mapObjects);
      outlineThickness.bindAll(mapObjects);
    }

    @Override
    protected void clearControls() {
      comboBoxParticleType.setSelectedItem(EmitterAttributes.DEFAULT_PARTICLE_TYPE);
      fade.setSelected(EmitterAttributes.DEFAULT_FADE);
      outlineOnly.setSelected(EmitterAttributes.DEFAULT_OUTLINE_ONLY);
      antiAliasing.setSelected(EmitterAttributes.DEFAULT_ANTIALIASING);
      renderMode.setSelectedItem(outlineOnly.isSelected() ? RenderMode.OUTLINE : RenderMode.FILLED);
      updateOutlineAvailability();
      updateSourceControls();
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      emitter = Game.world().environment().getEmitter(mapObject.getId());
      comboBoxParticleType.setSelectedItem(
        mapObject.getEnumValue(
          MapObjectProperty.Emitter.PARTICLETYPE,
          ParticleType.class,
          EmitterAttributes.DEFAULT_PARTICLE_TYPE));
      updateSourceControls();
      fade.setSelected(
        mapObject.getBoolValue(MapObjectProperty.Particle.FADE, EmitterAttributes.DEFAULT_FADE));
      outlineOnly.setSelected(
        mapObject.getBoolValue(
          MapObjectProperty.Particle.OUTLINEONLY, EmitterAttributes.DEFAULT_OUTLINE_ONLY));
      antiAliasing.setSelected(
        mapObject.getBoolValue(
          MapObjectProperty.Particle.ANTIALIASING, EmitterAttributes.DEFAULT_ANTIALIASING));
      renderMode.setSelectedItem(outlineOnly.isSelected() ? RenderMode.OUTLINE : RenderMode.FILLED);
      updateOutlineAvailability();
    }

    @Override
    protected LayoutManager createLayout() {
      return new BorderLayout();
    }

    @Override
    protected void setupChangedListeners() {
      setup(comboBoxParticleType, MapObjectProperty.Emitter.PARTICLETYPE);
      comboBoxParticleType.addItemListener(e -> {
        updateSourceControls();
        updateOutlineAvailability();
      });
      shape.addActionListener(event -> {
        if (shape.getSelectedItem() != null && shapeSource.isSelected()) {
          comboBoxParticleType.setSelectedItem(shape.getSelectedItem());
        }
      });
      shapeSource.addActionListener(event -> {
        ParticleType selected = (ParticleType) comboBoxParticleType.getSelectedItem();
        comboBoxParticleType.setSelectedItem(isShape(selected) ? selected : shape.getSelectedItem());
      });
      textSource.addActionListener(event -> comboBoxParticleType.setSelectedItem(ParticleType.TEXT));
      spriteSource.addActionListener(event -> comboBoxParticleType.setSelectedItem(ParticleType.SPRITE));
      setup(fade, MapObjectProperty.Particle.FADE);
      setup(outlineOnly, MapObjectProperty.Particle.OUTLINEONLY);
      outlineOnly.addActionListener(event -> updateOutlineAvailability());
      renderMode.addActionListener(event -> {
        boolean selected = renderMode.getSelectedItem() == RenderMode.OUTLINE;
        if (outlineOnly.isSelected() != selected) {
          outlineOnly.doClick();
        }
      });
      setup(antiAliasing, MapObjectProperty.Particle.ANTIALIASING);
    }

    private void updateSourceControls() {
      ParticleType selected = (ParticleType) comboBoxParticleType.getSelectedItem();
      if (selected == ParticleType.TEXT) {
        textSource.setSelected(true);
        sourceSettingsLabel.setText(Resources.strings().get("emitter_sourceText"));
        sourceSettingsLayout.show(sourceSettings, "text");
        sizeSourceSettings(textPanel);
      } else if (selected == ParticleType.SPRITE) {
        spriteSource.setSelected(true);
        sourceSettingsLabel.setText(Resources.strings().get("emitter_sourceSprite"));
        sourceSettingsLayout.show(sourceSettings, "sprite");
        sizeSourceSettings(spritePanel);
      } else {
        shapeSource.setSelected(true);
        sourceSettingsLabel.setText(Resources.strings().get("emitter_sourceShape"));
        if (selected != null) {
          shape.setSelectedItem(selected);
        }
        sourceSettingsLayout.show(sourceSettings, "shape");
        sizeSourceSettings(shape);
      }
      fillSection.setVisible(isShape(selected));
      colorsSection.setVisible(selected != ParticleType.SPRITE);
    }

    private void sizeSourceSettings(java.awt.Component active) {
      int height = Math.max(CONTROL_HEIGHT, active.getPreferredSize().height);
      sourceSettings.setPreferredSize(new java.awt.Dimension(CONTROL_WIDTH, height));
      sourceSettings.setMinimumSize(new java.awt.Dimension(CONTROL_MIN_WIDTH, height));
      sourceSettings.revalidate();
    }

    private void updateOutlineAvailability() {
      boolean available = outlineOnly.isSelected()
          || comboBoxParticleType.getSelectedItem() == ParticleType.LINE;
      outlineThickness.setEnabled(available);
      outlineThickness.setToolTipText(available
          ? null
          : Resources.strings().get("particle_outlinethickness_unavailable"));
    }

    private JPanel createRenderingPanel() {
      JPanel panel = formPanel();
      JPanel sources = new JPanel(new GridLayout(1, 3));
      sources.setOpaque(false);
      sources.setMinimumSize(new java.awt.Dimension(0, CONTROL_HEIGHT));
      sources.setPreferredSize(new java.awt.Dimension(CONTROL_WIDTH, CONTROL_HEIGHT));
      sources.add(shapeSource);
      sources.add(textSource);
      sources.add(spriteSource);
      addFormRow(panel, 0, "emitter_particleType", sources);
      addFormRow(panel, 1, sourceSettingsLabel, sourceSettings);
      addFormRow(panel, 2, "particle_fade", fade);
      addFormRow(panel, 3, "particle_antiAliasing", antiAliasing);
      return panel;
    }

    private JPanel createFillPanel() {
      JPanel panel = formPanel();
      addFormRow(panel, 0, "emitter_renderMode", renderMode);
      addFormRow(panel, 1, "particle_outlinethickness", outlineThickness);
      return panel;
    }

    private static JPanel formPanel() {
      JPanel panel = new JPanel(new GridBagLayout());
      panel.setOpaque(false);
      panel.setBorder(new javax.swing.border.EmptyBorder(CONTROL_MARGIN * 2, CONTROL_MARGIN * 3,
          CONTROL_MARGIN * 2, CONTROL_MARGIN * 3));
      return panel;
    }

    private static void addFormRow(JPanel panel, int row, String labelKey, java.awt.Component control) {
      addFormRow(panel, row, new JLabel(Resources.strings().get(labelKey)), control);
    }

    private static void addFormRow(
        JPanel panel, int row, JLabel labelComponent, java.awt.Component control) {
      GridBagConstraints label = new GridBagConstraints();
      label.gridx = 0;
      label.gridy = row;
      label.anchor = GridBagConstraints.LINE_END;
      label.insets = new Insets(CONTROL_MARGIN, 0, CONTROL_MARGIN, GUTTER_WIDTH);
      label.weightx = 0;
      panel.add(labelComponent, label);

      GridBagConstraints field = new GridBagConstraints();
      field.gridx = 1;
      field.gridy = row;
      field.fill = GridBagConstraints.HORIZONTAL;
      field.insets = new Insets(CONTROL_MARGIN, 0, CONTROL_MARGIN, 0);
      field.weightx = 1;
      panel.add(control, field);
    }

    private static JToggleButton sourceButton(String labelKey) {
      JToggleButton button = Style.iconToggleButton(null, false);
      button.setText(Resources.strings().get(labelKey));
      button.putClientProperty("Editor.groupedToolbarButton", true);
      button.setMinimumSize(new java.awt.Dimension(0, CONTROL_HEIGHT));
      return button;
    }

    private static boolean isShape(ParticleType type) {
      return type != null && type != ParticleType.TEXT && type != ParticleType.SPRITE;
    }

    private static final class ShapeIcon implements Icon {
      private static final int SIZE = 16;
      private final ParticleType type;

      private ShapeIcon(ParticleType type) {
        this.type = type;
      }

      @Override
      public int getIconWidth() {
        return SIZE;
      }

      @Override
      public int getIconHeight() {
        return SIZE;
      }

      @Override
      public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g.setColor(component.isEnabled() ? Style.text() : Style.COLOR_DISABLED_TEXT);
          g.setStroke(new BasicStroke(1.5f));
          switch (this.type) {
            case RECTANGLE -> g.drawRoundRect(x + 2, y + 3, 12, 10, 2, 2);
            case ELLIPSE -> g.drawOval(x + 2, y + 2, 12, 12);
            case TRIANGLE -> g.drawPolygon(new Polygon(
                new int[] {x + 8, x + 2, x + 14}, new int[] {y + 2, y + 14, y + 14}, 3));
            case DIAMOND -> g.drawPolygon(new Polygon(
                new int[] {x + 8, x + 14, x + 8, x + 2},
                new int[] {y + 2, y + 8, y + 14, y + 8}, 4));
            case LINE -> g.drawLine(x + 2, y + 13, x + 14, y + 3);
            default -> {
              // The shape selector excludes text and sprite particles.
            }
          }
        } finally {
          g.dispose();
        }
      }
    }

    private enum RenderMode {
      FILLED("emitter_renderModeFilled"),
      OUTLINE("emitter_renderModeOutline");

      private final String labelKey;

      RenderMode(String labelKey) {
        this.labelKey = labelKey;
      }

      @Override
      public String toString() {
        return Resources.strings().get(this.labelKey);
      }
    }
  }

  private static class ParticleSizePanel extends EmitterPropertyPanel {
    private final DualSpinner startWidth;
    private final DualSpinner startHeight;
    private final DualSpinner deltaWidth;
    private final DualSpinner deltaHeight;

    private ParticleSizePanel() {
      super();
      startWidth =
        new DualSpinner(
          MapObjectProperty.Particle.STARTWIDTH_MIN,
          MapObjectProperty.Particle.STARTWIDTH_MAX,
          0,
          Short.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_WIDTH,
          EmitterAttributes.DEFAULT_MAX_WIDTH,
          STEP_ONE);
      startHeight =
        new DualSpinner(
          MapObjectProperty.Particle.STARTHEIGHT_MIN,
          MapObjectProperty.Particle.STARTHEIGHT_MAX,
          0,
          Short.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_HEIGHT,
          EmitterAttributes.DEFAULT_MAX_HEIGHT,
          STEP_ONE);
      deltaWidth =
        new DualSpinner(
          MapObjectProperty.Particle.DELTAWIDTH_MIN,
          MapObjectProperty.Particle.DELTAWIDTH_MAX,
          Short.MIN_VALUE,
          Short.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_DELTA_WIDTH,
          EmitterAttributes.DEFAULT_MAX_DELTA_WIDTH,
          STEP_FINEST);
      deltaHeight =
        new DualSpinner(
          MapObjectProperty.Particle.DELTAHEIGHT_MIN,
          MapObjectProperty.Particle.DELTAHEIGHT_MAX,
          Short.MIN_VALUE,
          Short.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_DELTA_HEIGHT,
          EmitterAttributes.DEFAULT_MAX_DELTA_HEIGHT,
          STEP_FINEST);
      startWidth.setRangePresentation("px");
      startHeight.setRangePresentation("px");
      deltaWidth.setRangePresentation("px/s");
      deltaHeight.setRangePresentation("px/s");
      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      startWidth.bind(mapObject);
      startHeight.bind(mapObject);
      deltaWidth.bind(mapObject);
      deltaHeight.bind(mapObject);
    }

    @Override
    public void bindAll(java.util.List<IMapObject> mapObjects) {
      super.bindAll(mapObjects);
      startWidth.bindAll(mapObjects);
      startHeight.bindAll(mapObjects);
      deltaWidth.bindAll(mapObjects);
      deltaHeight.bindAll(mapObjects);
    }

    @Override
    protected void clearControls() {
      // do nothing
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      // do nothing
    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems =
        new LayoutItem[] {
          new LayoutItem("emitter_startWidth", startWidth),
          new LayoutItem("emitter_startHeight", startHeight),
          new LayoutItem("emitter_deltaWidth", deltaWidth),
          new LayoutItem("emitter_deltaHeight", deltaHeight)
        };
      return this.createLayout(layoutItems);
    }

    @Override
    protected void setupChangedListeners() {
      // do nothing
    }
  }

  private static class ParticleOriginPanel extends EmitterPropertyPanel {
    private final JComboBox<Align> comboBoxAlign;
    private final JComboBox<Valign> comboBoxValign;
    private final AnchorPicker anchorPicker;
    private final DualSpinner offsetX;
    private final DualSpinner offsetY;

    private ParticleOriginPanel() {
      super();
      comboBoxAlign = new JComboBox<>(new DefaultComboBoxModel<>(Align.values()));
      comboBoxValign = new JComboBox<>(new DefaultComboBoxModel<>(Valign.values()));
      anchorPicker = new AnchorPicker(comboBoxAlign, comboBoxValign);
      offsetX =
        new DualSpinner(
          MapObjectProperty.Particle.OFFSET_X_MIN,
          MapObjectProperty.Particle.OFFSET_X_MAX,
          Short.MIN_VALUE,
          Short.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_OFFSET_X,
          EmitterAttributes.DEFAULT_MAX_OFFSET_X,
          STEP_ONE);
      offsetY =
        new DualSpinner(
          MapObjectProperty.Particle.OFFSET_Y_MIN,
          MapObjectProperty.Particle.OFFSET_Y_MAX,
          Short.MIN_VALUE,
          Short.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_OFFSET_Y,
          EmitterAttributes.DEFAULT_MAX_OFFSET_Y,
          STEP_ONE);
      offsetX.setRangePresentation("px");
      offsetY.setRangePresentation("px");

      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      offsetX.bind(mapObject);
      offsetY.bind(mapObject);
    }

    @Override
    public void bindAll(java.util.List<IMapObject> mapObjects) {
      super.bindAll(mapObjects);
      offsetX.bindAll(mapObjects);
      offsetY.bindAll(mapObjects);
    }

    @Override
    protected void clearControls() {
      comboBoxAlign.setSelectedItem(EmitterAttributes.DEFAULT_ORIGIN_ALIGN);
      comboBoxValign.setSelectedItem(EmitterAttributes.DEFAULT_ORIGIN_VALIGN);
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());
      comboBoxAlign.setSelectedItem(
        mapObject.getEnumValue(
          MapObjectProperty.Emitter.ORIGIN_ALIGN,
          Align.class,
          EmitterAttributes.DEFAULT_ORIGIN_ALIGN));
      comboBoxValign.setSelectedItem(
        mapObject.getEnumValue(
          MapObjectProperty.Emitter.ORIGIN_VALIGN,
          Valign.class,
          EmitterAttributes.DEFAULT_ORIGIN_VALIGN));
    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems =
        new LayoutItem[] {
          new LayoutItem(anchorPicker, CONTROL_HEIGHT * 5),
          new LayoutItem("offsetX", offsetX),
          new LayoutItem("offsetY", offsetY)
        };
      return this.createLayout(layoutItems);
    }

    @Override
    protected void setupChangedListeners() {
      setup(comboBoxAlign, MapObjectProperty.Emitter.ORIGIN_ALIGN);
      setup(comboBoxValign, MapObjectProperty.Emitter.ORIGIN_VALIGN);
    }
  }

  private static class ParticleRotationPanel extends EmitterPropertyPanel {
    private final DualSpinner startAngle;
    private final DualSpinner deltaAngle;

    private ParticleRotationPanel() {
      super();
      startAngle =
        new DualSpinner(
          MapObjectProperty.Particle.ANGLE_MIN,
          MapObjectProperty.Particle.ANGLE_MAX,
          -360,
          360,
          EmitterAttributes.DEFAULT_MIN_ANGLE,
          EmitterAttributes.DEFAULT_MAX_ROTATION,
          STEP_ONE);
      deltaAngle =
        new DualSpinner(
          MapObjectProperty.Particle.DELTA_ANGLE_MIN,
          MapObjectProperty.Particle.DELTA_ANGLE_MAX,
          -360,
          360,
          EmitterAttributes.DEFAULT_MIN_DELTA_ANGLE,
          EmitterAttributes.DEFAULT_MAX_DELTA_ANGLE,
          STEP_FINE);
      startAngle.setRangePresentation("deg");
      deltaAngle.setRangePresentation("deg/s");

      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      startAngle.bind(mapObject);
      deltaAngle.bind(mapObject);
    }

    @Override
    public void bindAll(java.util.List<IMapObject> mapObjects) {
      super.bindAll(mapObjects);
      startAngle.bindAll(mapObjects);
      deltaAngle.bindAll(mapObjects);
    }

    @Override
    protected void clearControls() {
      // do nothing
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());
    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems =
        new LayoutItem[] {
          new LayoutItem("particle_startAngle", startAngle),
          new LayoutItem("particle_deltaAngle", deltaAngle)
        };
      return this.createLayout(layoutItems);
    }

    @Override
    protected void setupChangedListeners() {
      // do nothing
    }
  }

  private static class ParticleVelocityPanel extends EmitterPropertyPanel {
    private final DualSpinner velocityX;
    private final DualSpinner velocityY;

    private ParticleVelocityPanel() {
      super();
      velocityX =
        new DualSpinner(
          MapObjectProperty.Particle.VELOCITY_X_MIN,
          MapObjectProperty.Particle.VELOCITY_X_MAX,
          Short.MIN_VALUE,
          Short.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_VELOCITY_X,
          EmitterAttributes.DEFAULT_MAX_VELOCITY_X,
          STEP_FINEST);
      velocityY =
        new DualSpinner(
          MapObjectProperty.Particle.VELOCITY_Y_MIN,
          MapObjectProperty.Particle.VELOCITY_Y_MAX,
          Short.MIN_VALUE,
          Short.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_VELOCITY_Y,
          EmitterAttributes.DEFAULT_MAX_VELOCITY_Y,
          STEP_FINEST);
      velocityX.setRangePresentation("px/s");
      velocityY.setRangePresentation("px/s");
      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      velocityX.bind(mapObject);
      velocityY.bind(mapObject);
    }

    @Override
    public void bindAll(java.util.List<IMapObject> mapObjects) {
      super.bindAll(mapObjects);
      velocityX.bindAll(mapObjects);
      velocityY.bindAll(mapObjects);
    }

    @Override
    protected void clearControls() {
      // do nothing
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());
    }

    @Override
    protected LayoutManager createLayout() {
      LayoutItem[] layoutItems =
        new LayoutItem[] {
          new LayoutItem("emitter_velocityX", velocityX),
          new LayoutItem("emitter_velocityY", velocityY)
        };
      return this.createLayout(layoutItems);
    }

    @Override
    protected void setupChangedListeners() {
      // do nothing
    }
  }

  private static class ParticleCollisionPanel extends EmitterPropertyPanel {
    JComboBox<Collision> collisionType;
    JToggleButton fadeOnCollision;
    JPanel collisionInfo;

    private ParticleCollisionPanel() {
      super();
      collisionType = new JComboBox<>(Collision.values());
      fadeOnCollision = new JToggleButton();
      fadeOnCollision.putClientProperty(
        ToggleButton.KEY_VARIANT, ToggleButton.VARIANT_SLIDER);
      collisionInfo = infoNote("particle_fadeOnCollision_note");
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(formPanel(
          new LayoutItem("collisionType", collisionType),
          new LayoutItem("particle_fadeOnCollision", fadeOnCollision)));
      add(collisionInfo);
      setupChangedListeners();
      updateFadeAvailability();
    }

    @Override
    protected void clearControls() {
      collisionType.setSelectedItem(EmitterAttributes.DEFAULT_COLLISION);
      fadeOnCollision.setSelected(EmitterAttributes.DEFAULT_FADE_ON_COLLISION);
      updateFadeAvailability();
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      collisionType.setSelectedItem(
        mapObject.getEnumValue(
          MapObjectProperty.COLLISION_TYPE, Collision.class, EmitterAttributes.DEFAULT_COLLISION));
      fadeOnCollision.setSelected(
        mapObject.getBoolValue(
          MapObjectProperty.Particle.FADEONCOLLISION, EmitterAttributes.DEFAULT_FADE_ON_COLLISION));
      updateFadeAvailability();
    }

    @Override
    protected LayoutManager createLayout() {
      return new BorderLayout();
    }

    @Override
    protected void setupChangedListeners() {
      setup(collisionType, MapObjectProperty.COLLISION_TYPE);
      collisionType.addActionListener(event -> updateFadeAvailability());
      setup(fadeOnCollision, MapObjectProperty.Particle.FADEONCOLLISION);
    }

    private void updateFadeAvailability() {
      boolean available = collisionType.getSelectedItem() != Collision.NONE;
      fadeOnCollision.setEnabled(available);
      collisionInfo.setVisible(!available);
      fadeOnCollision.setToolTipText(available ? null : Resources.strings().get("particle_fadeOnCollision_unavailable"));
    }
  }

  private static class ParticleAccelerationPanel extends EmitterPropertyPanel {
    private final DualSpinner accelerationX;
    private final DualSpinner accelerationY;

    private ParticleAccelerationPanel() {
      super();
      accelerationX = new DualSpinner(
          MapObjectProperty.Particle.ACCELERATION_X_MIN,
          MapObjectProperty.Particle.ACCELERATION_X_MAX,
          Short.MIN_VALUE,
          Short.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_ACCELERATION_X,
          EmitterAttributes.DEFAULT_MAX_ACCELERATION_X,
          STEP_FINEST);
      accelerationY = new DualSpinner(
          MapObjectProperty.Particle.ACCELERATION_Y_MIN,
          MapObjectProperty.Particle.ACCELERATION_Y_MAX,
          Short.MIN_VALUE,
          Short.MAX_VALUE,
          EmitterAttributes.DEFAULT_MIN_ACCELERATION_Y,
          EmitterAttributes.DEFAULT_MAX_ACCELERATION_Y,
          STEP_FINEST);
      accelerationX.setRangePresentation("px/s^2");
      accelerationY.setRangePresentation("px/s^2");
      setLayout(createLayout());
      setupChangedListeners();
    }

    @Override
    public void bind(IMapObject mapObject) {
      super.bind(mapObject);
      accelerationX.bind(mapObject);
      accelerationY.bind(mapObject);
    }

    @Override
    public void bindAll(java.util.List<IMapObject> mapObjects) {
      super.bindAll(mapObjects);
      accelerationX.bindAll(mapObjects);
      accelerationY.bindAll(mapObjects);
    }

    @Override
    protected LayoutManager createLayout() {
      return createLayout(new LayoutItem[] {
        new LayoutItem("emitter_accelerationX", accelerationX),
        new LayoutItem("emitter_accelerationY", accelerationY)
      });
    }

    @Override
    protected void setupChangedListeners() {
      // Dual spinners own their listeners.
    }

    @Override
    protected void clearControls() {
      // Dual spinners are bound directly.
    }

    @Override
    protected void setControlValues(IMapObject mapObject) {
      this.emitter = Game.world().environment().getEmitter(mapObject.getId());
    }
  }

  private static LayoutItem section(String resource) {
    JLabel label = sectionLabel(resource);
    return new LayoutItem(label);
  }

  private static JLabel sectionLabel(String resource) {
    JLabel label = new JLabel(Resources.strings().get(resource));
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    return label;
  }

  private static JPanel formPanel(LayoutItem... items) {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setOpaque(false);
    for (int row = 0; row < items.length; row++) {
      LayoutItem item = items[row];
      GridBagConstraints labelConstraints = new GridBagConstraints();
      labelConstraints.gridx = 0;
      labelConstraints.gridy = row;
      labelConstraints.anchor = GridBagConstraints.LINE_END;
      labelConstraints.insets = new Insets(CONTROL_MARGIN, 0, CONTROL_MARGIN, GUTTER_WIDTH);
      labelConstraints.weightx = 0;
      if (item.getLabel() != null) {
        item.getLabel().setPreferredSize(LABEL_SIZE);
        panel.add(item.getLabel(), labelConstraints);
      }

      GridBagConstraints controlConstraints = new GridBagConstraints();
      controlConstraints.gridx = 1;
      controlConstraints.gridy = row;
      controlConstraints.fill = GridBagConstraints.NONE;
      controlConstraints.anchor = GridBagConstraints.LINE_START;
      controlConstraints.insets = new Insets(CONTROL_MARGIN, 0, CONTROL_MARGIN, 0);
      controlConstraints.weightx = 1;
      panel.add(item.getComponent(), controlConstraints);
    }
    return panel;
  }

  private static JPanel infoNote(String resourceKey) {
    JPanel panel = new JPanel(new BorderLayout(CONTROL_MARGIN * 2, 0));
    panel.setOpaque(false);
    panel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
        javax.swing.BorderFactory.createLineBorder(Style.border()),
        new javax.swing.border.EmptyBorder(CONTROL_MARGIN * 2, CONTROL_MARGIN * 2,
            CONTROL_MARGIN * 2, CONTROL_MARGIN * 2)));
    panel.add(new JLabel(de.gurkenlabs.utiliti.model.Icons.ABOUT_16), BorderLayout.LINE_START);
    panel.add(new JLabel("<html>" + Resources.strings().get(resourceKey) + "</html>"), BorderLayout.CENTER);
    return panel;
  }

  private static void compactSpinner(JSpinner spinner) {
    spinner.setPreferredSize(SPINNER_SIZE);
    spinner.setMinimumSize(SPINNER_SIZE);
  }

  private static JPanel unitControl(JSpinner spinner, String unit) {
    JPanel panel = new JPanel(new BorderLayout(CONTROL_MARGIN * 2, 0));
    panel.setOpaque(false);
    panel.add(spinner, BorderLayout.LINE_START);
    panel.add(new JLabel(unit), BorderLayout.CENTER);
    return panel;
  }

  private static final class AnchorPicker extends javax.swing.JPanel {
    private static final Align[] COLUMNS = {
      Align.LEFT, Align.CENTER_LEFT, Align.CENTER, Align.CENTER_RIGHT, Align.RIGHT
    };
    private static final Valign[] ROWS = {
      Valign.TOP, Valign.MIDDLE_TOP, Valign.MIDDLE, Valign.MIDDLE_DOWN, Valign.DOWN
    };
    private final Map<Align, Map<Valign, JToggleButton>> buttons = new EnumMap<>(Align.class);

    private AnchorPicker(JComboBox<Align> align, JComboBox<Valign> valign) {
      super(new GridLayout(ROWS.length, COLUMNS.length, CONTROL_MARGIN, CONTROL_MARGIN));
      ButtonGroup group = new ButtonGroup();
      for (Valign row : ROWS) {
        for (Align column : COLUMNS) {
          JToggleButton button = new JToggleButton("o");
          button.setToolTipText(anchorName(column, row));
          button.getAccessibleContext().setAccessibleName(anchorName(column, row));
          button.addActionListener(event -> {
            align.setSelectedItem(column);
            valign.setSelectedItem(row);
          });
          group.add(button);
          add(button);
          buttons.computeIfAbsent(column, ignored -> new EnumMap<>(Valign.class)).put(row, button);
        }
      }
      Runnable updateSelection = () -> {
        JToggleButton selected = buttons
            .getOrDefault((Align) align.getSelectedItem(), Map.of())
            .get((Valign) valign.getSelectedItem());
        if (selected != null) {
          selected.setSelected(true);
        }
      };
      align.addActionListener(event -> updateSelection.run());
      valign.addActionListener(event -> updateSelection.run());
      updateSelection.run();
    }

    private static String anchorName(Align align, Valign valign) {
      return Resources.strings().get("emitter_anchor_" + valign.name().toLowerCase()) + " "
          + Resources.strings().get("emitter_anchor_" + align.name().toLowerCase());
    }
  }
}

package de.gurkenlabs.utiliti.swing.panels;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.utiliti.listeners.MapObjectPropertyChangeListener;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class DualSpinner extends PropertyPanel {

  private final JSpinner spnDim1;
  private final JSpinner spnDim2;

  private final String dim1Name;
  private final String dim2Name;
  private final Number dim1Default;
  private final Number dim2Default;
  private final boolean exclusiveBounds;


  public DualSpinner(
    SpinnerNumberModel model1,
    SpinnerNumberModel model2,
    String label1,
    String label2) {
    this(null, null, model1, model2, label1, label2, false);
  }

  public DualSpinner(String name1,
    String name2,
    SpinnerNumberModel model1,
    SpinnerNumberModel model2) {
    this(name1, name2, model1, model2, "min", "max", true);
  }

  public DualSpinner(
    String name1,
    String name2,
    SpinnerNumberModel model1,
    SpinnerNumberModel model2,
    String label1,
    String label2,
    boolean exclusiveBounds) {
    this.dim1Name = name1;
    this.dim2Name = name2;
    JLabel lblDim1 = new JLabel(label1);
    lblDim1.setMinimumSize(LABEL_SIZE);
    this.dim1Default = model1.getNumber();
    this.dim2Default = model2.getNumber();
    this.exclusiveBounds = exclusiveBounds;
    this.spnDim1 = new JSpinner(model1);
    this.spnDim1.setMinimumSize(SPINNER_SIZE);
    JLabel lblDim2 = new JLabel(label2);
    lblDim2.setMinimumSize(LABEL_SIZE);
    this.spnDim2 = new JSpinner(model2);
    this.spnDim2.setMinimumSize(SPINNER_SIZE);

    GroupLayout grplayout = new GroupLayout(this);
    grplayout.setAutoCreateGaps(true);
    grplayout.setHorizontalGroup(
      grplayout
        .createSequentialGroup()
        .addContainerGap()
        .addComponent(lblDim1)
        .addComponent(spnDim1)
        .addComponent(lblDim2)
        .addComponent(spnDim2)
        .addContainerGap());
    grplayout.setVerticalGroup(
      grplayout
        .createParallelGroup()
        .addComponent(lblDim1, GroupLayout.Alignment.CENTER)
        .addComponent(spnDim1)
        .addComponent(lblDim2, GroupLayout.Alignment.CENTER)
        .addComponent(spnDim2));
    setLayout(grplayout);
    setupChangedListeners();
  }

  public JSpinner getSpinner1() {
    return this.spnDim1;
  }

  public JSpinner getSpinner2() {
    return this.spnDim2;
  }

  public void addSpinnerListeners(
    Predicate<IMapObject> newValueCheck1,
    Predicate<IMapObject> newValueCheck2,
    Consumer<IMapObject> spinner1Action,
    Consumer<IMapObject> spinner2Action) {
    spnDim1.addChangeListener(
      new MapObjectPropertyChangeListener(this, newValueCheck1, spinner1Action));
    spnDim2.addChangeListener(
      new MapObjectPropertyChangeListener(this, newValueCheck2, spinner2Action));
  }

  public void setValues(Number v1, Number v2) {
    SpinnerNumberModel model1 = (SpinnerNumberModel) spnDim1.getModel();
    SpinnerNumberModel model2 = (SpinnerNumberModel) spnDim2.getModel();
    if (v1.getClass() == model1.getValue().getClass()) {
      spnDim1.getModel().setValue(v1);
    } else if (v1 instanceof Float || v1 instanceof Double) {
      spnDim1.setModel(
        new SpinnerNumberModel(v1.doubleValue(), ((Number) model1.getMinimum()).doubleValue(),
          ((Number) model1.getMaximum()).doubleValue(), model1.getStepSize().doubleValue()));
    } else if (v1 instanceof Byte || v1 instanceof Short || v1 instanceof Integer) {
      spnDim1.setModel(
        new SpinnerNumberModel(v1.intValue(), ((Number) model1.getMinimum()).intValue(),
          ((Number) model1.getMaximum()).intValue(), model1.getStepSize().intValue()));
    } else if (v1 instanceof Long) {
      spnDim1.setModel(
        new SpinnerNumberModel(v1.longValue(), ((Number) model1.getMinimum()).longValue(),
          ((Number) model1.getMaximum()).longValue(), model1.getStepSize().longValue()));
    }

    if (v2.getClass() == model2.getValue().getClass()) {
      spnDim2.getModel().setValue(v2);
    } else if (v2 instanceof Float || v2 instanceof Double) {
      spnDim2.setModel(
        new SpinnerNumberModel(v2.doubleValue(), ((Number) model2.getMinimum()).doubleValue(),
          ((Number) model2.getMaximum()).doubleValue(), model2.getStepSize().doubleValue()));
    } else if (v2 instanceof Byte || v2 instanceof Short || v2 instanceof Integer) {
      spnDim2.setModel(
        new SpinnerNumberModel(v2.intValue(), ((Number) model2.getMinimum()).intValue(),
          ((Number) model2.getMaximum()).intValue(), model2.getStepSize().intValue()));
    } else if (v2 instanceof Long) {
      spnDim2.setModel(
        new SpinnerNumberModel(v2.longValue(), ((Number) model2.getMinimum()).longValue(),
          ((Number) model2.getMaximum()).longValue(), model2.getStepSize().longValue()));
    }
  }

  @Override
  protected void clearControls() {
    setValues(dim1Default, dim2Default);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    if (dim1Name != null && dim2Name != null) {
      setValues(
        mapObject.getNumber(dim1Name, dim1Default),
        mapObject.getNumber(dim2Name, dim2Default));
    }
  }

  protected void setupChangedListeners() {
    if (dim1Name != null && dim2Name != null) {
      setup((SpinnerNumberModel) spnDim1.getModel(), dim1Name);
      setup((SpinnerNumberModel) spnDim2.getModel(), dim2Name);
    }
    if (!this.exclusiveBounds) {
      return;
    }
    spnDim1.addChangeListener(
      e -> {
        if (((SpinnerNumberModel) spnDim1.getModel()).getNumber().floatValue()
          > ((SpinnerNumberModel) spnDim2.getModel()).getNumber().floatValue()) {
          spnDim2.setValue(spnDim1.getValue());
        }
      });
    spnDim2.addChangeListener(
      e -> {
        if (((SpinnerNumberModel) spnDim1.getModel()).getNumber().floatValue()
          > ((SpinnerNumberModel) spnDim2.getModel()).getNumber().floatValue()) {
          spnDim1.setValue(spnDim2.getValue());
        }
      });
  }
}

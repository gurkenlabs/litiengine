package de.gurkenlabs.utiliti.swing.panels;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.resources.Resources;

@SuppressWarnings("serial")
public class DualSpinner extends PropertyPanel {
  private JSpinner min;
  private JLabel minLbl;
  private JSpinner max;
  private JLabel maxLbl;

  private String minPropertyName;
  private String maxPropertyName;
  private float defaultMin;
  private float defaultMax;

  public DualSpinner(String minPropertyName, String maxPropertyName, float lowerBound, float upperBound, float defaultMin, float defaultMax, float step) {
    this.minPropertyName = minPropertyName;
    this.maxPropertyName = maxPropertyName;
    this.defaultMin = defaultMin;
    this.defaultMax = defaultMax;
    min = new JSpinner(new SpinnerNumberModel(defaultMin, lowerBound, upperBound, step));
    min.setMinimumSize(SPINNER_SIZE);
    minLbl = new JLabel(Resources.strings().get("min"));
    minLbl.setMinimumSize(LABEL_SIZE);

    max = new JSpinner(new SpinnerNumberModel(defaultMax, lowerBound, upperBound, step));
    max.setMinimumSize(SPINNER_SIZE);
    maxLbl = new JLabel(Resources.strings().get("max"));
    maxLbl.setMinimumSize(LABEL_SIZE);

    GroupLayout grplayout = new GroupLayout(this);
    grplayout.setAutoCreateGaps(true);
    grplayout.setHorizontalGroup(grplayout.createSequentialGroup().addContainerGap().addComponent(minLbl).addComponent(min).addComponent(maxLbl).addComponent(max).addContainerGap());
    grplayout.setVerticalGroup(grplayout.createParallelGroup().addComponent(minLbl, GroupLayout.Alignment.CENTER).addComponent(min).addComponent(maxLbl, GroupLayout.Alignment.CENTER).addComponent(max));
    this.setLayout(grplayout);
    setupChangedListeners();
  }

  @Override
  protected void clearControls() {
    min.setValue((double) this.defaultMin);
    max.setValue((double) this.defaultMax);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    min.setValue((double) mapObject.getFloatValue(this.minPropertyName, this.defaultMin));
    max.setValue((double) mapObject.getFloatValue(this.maxPropertyName, this.defaultMax));
  }

  protected void setupChangedListeners() {
    setup(min, this.minPropertyName);

    min.addChangeListener(e -> {
      double minV = (double) min.getValue();
      double maxV = (double) max.getValue();
      if (minV > maxV) {
        max.setValue(min.getValue());
      }
    });
    setup(max, this.maxPropertyName);
    max.addChangeListener(e -> {
      double minV = (double) min.getValue();
      double maxV = (double) max.getValue();
      if (maxV < minV) {
        min.setValue(max.getValue());
      }
    });

  }
}

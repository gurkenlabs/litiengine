package com.litiengine.utiliti.swing.panels;

import java.util.function.Consumer;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.litiengine.environment.tilemap.IMapObject;

@SuppressWarnings("serial")
public class DualSpinner extends PropertyPanel {
  private JSpinner spnDim1;
  private JLabel lblDim1;
  private JSpinner spnDim2;
  private JLabel lblDim2;

  private String dim1Name;
  private String dim2Name;
  private float dim1Default;
  private float dim2Default;

  private float lowerBound;
  private float upperBound;
  private float step;

  private boolean exclusiveBounds;

  public DualSpinner(String dim1Name, String dim2Name, float lowerBound, float upperBound, float dim1Default, float dim2Default, float step) {
    this(dim1Name, "min", dim2Name, "max", lowerBound, upperBound, dim1Default, dim2Default, step, true);
  }

  public DualSpinner(String dim1Lbl, String dim2Lbl, float lowerBound, float upperBound) {
    this(null, dim1Lbl, null, dim2Lbl, lowerBound, upperBound, 0, 0, 1, false);
  }

  public DualSpinner(String dim1Name, String dim1Lbl, String dim2Name, String dim2Lbl, float lowerBound, float upperBound, float dim1Default, float dim2Default, float step, boolean exclusiveBounds) {
    this.dim1Name = dim1Name;
    this.dim2Name = dim2Name;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.step = step;
    this.dim1Default = dim1Default;
    this.dim2Default = dim2Default;
    this.exclusiveBounds = exclusiveBounds;

    this.lblDim1 = new JLabel(dim1Lbl);
    this.lblDim1.setMinimumSize(LABEL_SIZE);
    this.spnDim1 = new JSpinner(new SpinnerNumberModel(this.dim1Default, this.lowerBound, this.upperBound, this.step));
    this.spnDim1.setMinimumSize(SPINNER_SIZE);
    this.lblDim2 = new JLabel(dim2Lbl);
    this.lblDim2.setMinimumSize(LABEL_SIZE);
    this.spnDim2 = new JSpinner(new SpinnerNumberModel(this.dim2Default, this.lowerBound, this.upperBound, this.step));
    this.spnDim2.setMinimumSize(SPINNER_SIZE);

    GroupLayout grplayout = new GroupLayout(this);
    grplayout.setAutoCreateGaps(true);
    grplayout.setHorizontalGroup(grplayout.createSequentialGroup().addContainerGap().addComponent(lblDim1).addComponent(spnDim1).addComponent(lblDim2).addComponent(spnDim2).addContainerGap());
    grplayout.setVerticalGroup(grplayout.createParallelGroup().addComponent(lblDim1, GroupLayout.Alignment.CENTER).addComponent(spnDim1).addComponent(lblDim2, GroupLayout.Alignment.CENTER).addComponent(spnDim2));
    this.setLayout(grplayout);
    setupChangedListeners();
  }

  public JSpinner getSpinner1() {
    return this.spnDim1;
  }

  public JSpinner getSpinner2() {
    return this.spnDim2;
  }

  public void addSpinnerListeners(Consumer<IMapObject> spinner1Action, Consumer<IMapObject> spinner2Action) {
    spnDim1.addChangeListener(new MapObjectPropertyChangeListener(spinner1Action));
    spnDim2.addChangeListener(new MapObjectPropertyChangeListener(spinner2Action));
  }

  public void setValues(float v1, float v2) {
    spnDim1.setValue((double) v1);
    spnDim2.setValue((double) v2);
  }

  public void setValues(double v1, double v2) {
    spnDim1.setValue(v1);
    spnDim2.setValue(v2);
  }

  @Override
  protected void clearControls() {
    setValues(this.dim1Default, this.dim2Default);
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    if (this.dim1Name != null && this.dim2Name != null) {
      setValues(mapObject.getFloatValue(this.dim1Name, this.dim1Default), mapObject.getFloatValue(this.dim2Name, this.dim2Default));
    }
  }

  protected void setupChangedListeners() {
    if (this.dim1Name != null && this.dim2Name != null) {
      setup(spnDim1, this.dim1Name);
      setup(spnDim2, this.dim2Name);
    }
    if (!this.exclusiveBounds) {
      return;
    }
    spnDim1.addChangeListener(e -> {
      double minV = (double) spnDim1.getValue();
      double maxV = (double) spnDim2.getValue();
      if (minV > maxV) {
        spnDim2.setValue(spnDim1.getValue());
      }
    });
    spnDim2.addChangeListener(e -> {
      double minV = (double) spnDim1.getValue();
      double maxV = (double) spnDim2.getValue();
      if (maxV < minV) {
        spnDim1.setValue(spnDim2.getValue());
      }
    });

  }
}

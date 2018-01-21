package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;

public class EmitterPanel extends PropertyPanel<IMapObject> {

  public EmitterPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_emitter"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);
    
    JLabel lblEmitterData = new JLabel("Emitter Data");
    lblEmitterData.setFont(new Font("Tahoma", Font.BOLD, 11));
    
    Box horizontalBox = Box.createHorizontalBox();
    
    Box horizontalBox_1 = Box.createHorizontalBox();
    
    Component horizontalGlue_2 = Box.createHorizontalGlue();
    horizontalBox_1.add(horizontalGlue_2);
    
    JButton btnPause = new JButton("Pause");
    horizontalBox_1.add(btnPause);
    
    Component horizontalGlue_3 = Box.createHorizontalGlue();
    horizontalBox_1.add(horizontalGlue_3);
    
    JButton btnRestart = new JButton("Restart");
    horizontalBox_1.add(btnRestart);
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addContainerGap()
          .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
            .addComponent(lblEmitterData, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
            .addComponent(horizontalBox_1, GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
            .addComponent(horizontalBox, GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE))
          .addContainerGap())
    );
    groupLayout.setVerticalGroup(
      groupLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(groupLayout.createSequentialGroup()
          .addGap(8)
          .addComponent(horizontalBox_1, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(ComponentPlacement.RELATED)
          .addComponent(lblEmitterData, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(horizontalBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
          .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    
    Component horizontalGlue_4 = Box.createHorizontalGlue();
    horizontalBox_1.add(horizontalGlue_4);
    
    JButton btnCustomize = new JButton("Customize");
    horizontalBox.add(btnCustomize);
    
    Component horizontalGlue = Box.createHorizontalGlue();
    horizontalBox.add(horizontalGlue);
    
    JButton btnLoad = new JButton("Load...");
    horizontalBox.add(btnLoad);
    
    Component horizontalGlue_1 = Box.createHorizontalGlue();
    horizontalBox.add(horizontalGlue_1);
    
    JButton btnSave = new JButton("Save...");
    horizontalBox.add(btnSave);
    btnCustomize.addActionListener(a -> {
    EmitterPropertyPanel panel = new EmitterPropertyPanel();
    panel.bind((CustomEmitter)Game.getEnvironment().getEmitter(this.getDataSource().getId()));

    int option = JOptionPane.showConfirmDialog(null, panel, Resources.get("panel_emitterProperties"), JOptionPane.OK_CANCEL_OPTION);
    if (option == JOptionPane.CANCEL_OPTION) {
      panel.discardChanges();
    }
    });
    setLayout(groupLayout);

    this.setupChangedListeners();
  }

  private void setupChangedListeners() {
  }

  @Override
  protected void clearControls() {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    // TODO Auto-generated method stub
    
  }
}

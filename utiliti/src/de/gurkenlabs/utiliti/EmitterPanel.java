package de.gurkenlabs.utiliti;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;

public class EmitterPanel extends PropertyPanel<IMapObject> {
  private ImageIcon play, pause, rewind;
  private CustomEmitter dataSource;

  public EmitterPanel() {
    TitledBorder border = new TitledBorder(new LineBorder(new Color(128, 128, 128)), Resources.get("panel_emitter"), TitledBorder.LEADING, TitledBorder.TOP, null, null);
    border.setTitleFont(border.getTitleFont().deriveFont(Font.BOLD));
    setBorder(border);

    play= new ImageIcon(Resources.getImage("button-play.png"));
    pause= new ImageIcon(Resources.getImage("button-pause.png"));
    rewind= new ImageIcon(Resources.getImage("button-rewind.png"));
    
    JLabel lblEmitterData = new JLabel("Emitter Data");
    lblEmitterData.setFont(new Font("Tahoma", Font.BOLD, 11));

    Box horizontalBox = Box.createHorizontalBox();

    JButton btnCustomize = new JButton("Customize");
    btnCustomize.addActionListener(a -> {
      EmitterPropertyPanel panel = new EmitterPropertyPanel();
      panel.bind(this.getDataSource());

      int option = JOptionPane.showConfirmDialog(null, panel, Resources.get("panel_emitterProperties"), JOptionPane.OK_CANCEL_OPTION);
      if (option == JOptionPane.CANCEL_OPTION) {
        panel.discardChanges();
      }
    });

    JButton btnLoad = new JButton(Resources.get("menu_import"));

    JButton btnSave = new JButton(Resources.get("menu_export"));
    
    JToggleButton btnPause = new JToggleButton(Resources.get("panel_pause"));
    btnPause.setIcon(pause);
    btnPause.addActionListener(a -> {
      if(dataSource!=null) {
        dataSource.togglePaused();
      }

      if(btnPause.isSelected()) {
        btnPause.setIcon(play);
        btnPause.setText(Resources.get("panel_play"));
      } else {
        btnPause.setIcon(pause);
        btnPause.setText(Resources.get("panel_pause"));
      }
      
    });

    JButton btnRestart = new JButton(Resources.get("panel_rewind"));
    btnRestart.setIcon(rewind);
    GroupLayout groupLayout = new GroupLayout(this);
    groupLayout.setHorizontalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(horizontalBox, GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                    .addGroup(groupLayout.createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                            .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(lblEmitterData, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(btnCustomize))
                            .addComponent(btnPause, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                            .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(btnLoad)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(btnSave))
                            .addComponent(btnRestart, 0, 0, Short.MAX_VALUE))))
                .addContainerGap()));
    groupLayout.setVerticalGroup(
        groupLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(groupLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(btnPause)
                    .addComponent(btnRestart))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(btnCustomize)
                    .addComponent(lblEmitterData, GroupLayout.PREFERRED_SIZE, 13, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLoad)
                    .addComponent(btnSave))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(horizontalBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE)));
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

  
  @Override
  protected IMapObject getDataSource() {
    // TODO Auto-generated method stub
    return super.getDataSource();
  }

  public void bind(CustomEmitter e) {
    dataSource = e;
  }
}

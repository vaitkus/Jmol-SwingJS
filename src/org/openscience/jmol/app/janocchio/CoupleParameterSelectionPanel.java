/*  
 *  The Janocchio program is (C) 2007 Eli Lilly and Co.
 *  Authors: David Evans and Gary Sharman
 *  Contact : janocchio-users@lists.sourceforge.net.
 * 
 *  It is derived in part from Jmol 
 *  (C) 2002-2006 The Jmol Development Team
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *  All we ask is that proper credit is given for our work, which includes
 *  - but is not limited to - adding the above copyright notice to the beginning
 *  of your source code files, and to any copyright notice that you may distribute
 *  with programs based on this work.
 *
 *  This program is distributed in the hope that it will be useful, on an 'as is' basis,
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * coupleParameterSelectionPanel.java
 *
 * Created on 06 June 2006, 15:52
 */

package org.openscience.jmol.app.janocchio;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;

public class CoupleParameterSelectionPanel extends JPanel {
  CoupleTable coupleTable;

  /** Creates new form noeParameterSelectionPanel 
   * @param coupleTable */
  public CoupleParameterSelectionPanel(CoupleTable coupleTable) {
    this.coupleTable = coupleTable;
    initComponents();
    eq0RadioButton.setSelected(true);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
  void initComponents() {
    chButtonGroup = new ButtonGroup();
    titleLabel = new JLabel();
    jLabel2 = new JLabel();
    jLabel1 = new JLabel();
    jLabel3 = new JLabel();
    jLabel4 = new JLabel();
    eq0RadioButton = new JRadioButton();
    eq1RadioButton = new JRadioButton();
    eq2RadioButton = new JRadioButton();
    eq3RadioButton = new JRadioButton();

    setAutoscrolls(true);
    titleLabel.setText("Parameters for J Calculation");

    jLabel2.setText("3JHH:");

    jLabel1.setText("Altona if two sp3 carbons");
    jLabel1.setToolTipText("(Tetrahedron 36, 2783-2792)");

    jLabel3.setText("Karplus otherwise");
    jLabel3
        .setToolTipText("-90<theta<90: 8.5*cos(theta)^2 - 0.28;else 9.5*cos(theta)^2 - 0.28");

    jLabel4.setText("3JCH:");

    chButtonGroup.add(eq0RadioButton);
    eq0RadioButton.setText("none");
    eq0RadioButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    eq0RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    eq0RadioButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        eq0RadioButtonActionPerformed(evt);
      }
    });

    chButtonGroup.add(eq1RadioButton);
    eq1RadioButton.setText("Wasylichen");
    eq1RadioButton
        .setToolTipText("3.56*cos(2*theta) - cos(theta) + 4.26 (Can. J. Chem. (1973) 51 961)");
    eq1RadioButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    eq1RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    eq1RadioButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        eq1RadioButtonActionPerformed(evt);
      }
    });

    chButtonGroup.add(eq2RadioButton);
    eq2RadioButton.setText("Tvaroska");
    eq2RadioButton
        .setToolTipText("4.5 - 0.87*cos(theta) + cos(2*theta)  (Adv. Carbohydrate Chem. Biochem. (1995) 51, 15-61)");
    eq2RadioButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    eq2RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    eq2RadioButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        eq2RadioButtonActionPerformed(evt);
      }
    });

    chButtonGroup.add(eq3RadioButton);
    eq3RadioButton.setText("Aydin");
    eq3RadioButton
        .setToolTipText("5.8 * cos(theta)^2 - 1.6*cos(theta) + 0.28*sin(2*theta) - 0.02*sin(theta) + 0.52 (Mag. Res. Chem. (1990) 28, 448-457)");
    eq3RadioButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    eq3RadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
    eq3RadioButton.addActionListener(new java.awt.event.ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        eq3RadioButtonActionPerformed(evt);
      }
    });

    GroupLayout layout = new GroupLayout(this);
    this.setLayout(layout);
    layout
        .setHorizontalGroup(layout
            .createParallelGroup(Alignment.LEADING)
            .addGroup(
                layout
                    .createSequentialGroup()
                    .addGroup(
                        layout
                            .createParallelGroup(Alignment.LEADING)
                            .addGroup(
                                layout.createSequentialGroup()
                                    .addGap(54, 54, 54)
                                    .addComponent(titleLabel))
                            .addGroup(
                                layout
                                    .createSequentialGroup()
                                    .addContainerGap()
                                    .addGroup(
                                        layout
                                            .createParallelGroup(
                                                Alignment.TRAILING)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel2))
                                    .addPreferredGap(ComponentPlacement.RELATED)
                                    .addGroup(
                                        layout
                                            .createParallelGroup(
                                                Alignment.LEADING)
                                            .addComponent(jLabel3)
                                            .addComponent(jLabel1,
                                                GroupLayout.DEFAULT_SIZE, 219,
                                                Short.MAX_VALUE)
                                            .addGroup(
                                                Alignment.TRAILING,
                                                layout
                                                    .createSequentialGroup()
                                                    .addGroup(
                                                        layout
                                                            .createParallelGroup(
                                                                Alignment.LEADING)
                                                            .addComponent(
                                                                eq2RadioButton)
                                                            .addComponent(
                                                                eq0RadioButton)
                                                            .addComponent(
                                                                eq1RadioButton)
                                                            .addComponent(
                                                                eq3RadioButton))
                                                    .addGap(39, 39, 39)))))
                    .addContainerGap()));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(
            layout
                .createSequentialGroup()
                .addComponent(titleLabel, GroupLayout.PREFERRED_SIZE, 24,
                    GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(
                    layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(jLabel2).addComponent(jLabel1))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(10, 10, 10)
                .addGroup(
                    layout.createParallelGroup(Alignment.BASELINE)
                        .addComponent(jLabel4)
                .addComponent(eq0RadioButton))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(eq1RadioButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(eq2RadioButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(eq3RadioButton)
                .addContainerGap(63, Short.MAX_VALUE)));
  }// </editor-fold>//GEN-END:initComponents

  void eq0RadioButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eq1RadioButtonActionPerformed
  // TODO add your handling code here:
    coupleTable.setCHequation("none");
  }//GEN-LAST:event_eq1RadioButtonActionPerformed

  void eq1RadioButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eq1RadioButtonActionPerformed
  // TODO add your handling code here:
    coupleTable.setCHequation("was");
  }//GEN-LAST:event_eq1RadioButtonActionPerformed

  void eq3RadioButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eq3RadioButtonActionPerformed
  // TODO add your handling code here:
    coupleTable.setCHequation("ayd");
  }//GEN-LAST:event_eq3RadioButtonActionPerformed

  void eq2RadioButtonActionPerformed(@SuppressWarnings("unused") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eq2RadioButtonActionPerformed
  // TODO add your handling code here:
    coupleTable.setCHequation("tva");
  }//GEN-LAST:event_eq2RadioButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  ButtonGroup chButtonGroup;
  JRadioButton eq0RadioButton, eq1RadioButton;
  JRadioButton eq2RadioButton;
  JRadioButton eq3RadioButton;
  JLabel jLabel1;
  JLabel jLabel2;
  JLabel jLabel3;
  JLabel jLabel4;
  JLabel titleLabel;
  // End of variables declaration//GEN-END:variables

}

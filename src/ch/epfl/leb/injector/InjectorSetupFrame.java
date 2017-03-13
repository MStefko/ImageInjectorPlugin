/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.epfl.leb.injector;

import javax.swing.JFileChooser;
import java.io.File;
import org.micromanager.PropertyMap;
/**
 *
 * @author stefko
 */
public class InjectorSetupFrame extends javax.swing.JDialog {

    /**
     * Creates new form InjectorSetupFrame
     */
    PropertyMap.PropertyMapBuilder builder;
    InjectorConfigurator configurator;
    File tiff_file;
    public InjectorSetupFrame(java.awt.Frame parent, boolean modal, 
            InjectorConfigurator in_configurator) {
        super(parent, modal);
        configurator = in_configurator;
        builder = configurator.getSettings().copy();
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        frames_per_second = new javax.swing.JTextField();
        OK_button = new javax.swing.JButton();
        choose_file_button = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        label_filepath = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Frames per second:");

        frames_per_second.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        frames_per_second.setText("100");
        frames_per_second.setPreferredSize(new java.awt.Dimension(30, 20));

        OK_button.setText("OK");
        OK_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                OK_buttonMouseClicked(evt);
            }
        });

        choose_file_button.setText("Choose file...");
        choose_file_button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                choose_file_buttonMouseClicked(evt);
            }
        });

        jLabel2.setText("Current file:");

        label_filepath.setText("<none>");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addComponent(OK_button))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(label_filepath))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(frames_per_second, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(choose_file_button))))
                .addContainerGap(112, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(frames_per_second, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(choose_file_button)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(label_filepath))
                .addGap(14, 14, 14)
                .addComponent(OK_button)
                .addGap(22, 22, 22))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void OK_buttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_OK_buttonMouseClicked
        int FPS;
        try {
            FPS = Integer.parseInt(frames_per_second.getText());
        } catch (NumberFormatException ex) {
            configurator.getApp().logs().showError("Wrong input format.");
            return;
        }
        builder.putInt("framesPerSecond", FPS);
        PropertyMap pm = builder.build();
        configurator.setPropertyMap(pm);
        configurator.context.plugin.getImageStreamer().setFile(tiff_file);
        this.setVisible(false);
    }//GEN-LAST:event_OK_buttonMouseClicked

    private void choose_file_buttonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_choose_file_buttonMouseClicked
        String filepath = "";
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(choose_file_button);
        if  (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        tiff_file = fc.getSelectedFile();
        filepath = tiff_file.getAbsolutePath();
        label_filepath.setText(filepath);
        
    }//GEN-LAST:event_choose_file_buttonMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(InjectorSetupFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(InjectorSetupFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(InjectorSetupFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(InjectorSetupFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                InjectorSetupFrame dialog = new InjectorSetupFrame(new javax.swing.JFrame(), true, null);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OK_button;
    private javax.swing.JButton choose_file_button;
    private javax.swing.JTextField frames_per_second;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel label_filepath;
    // End of variables declaration//GEN-END:variables
}

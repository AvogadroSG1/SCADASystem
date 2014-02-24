/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package test;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author Shawn
 */
public class Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        JOptionPane pane = new JOptionPane("Hello", JOptionPane.ERROR_MESSAGE);
        final JDialog createDialog = pane.createDialog("Bah");
        createDialog.setModal(false);
        createDialog.setVisible(true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Thread started");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                System.out.println("Time over");
                createDialog.dispose();
                
                System.out.println("setVis(false)");
            }
        });
        thread.start();
    }
    
}

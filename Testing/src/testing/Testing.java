/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package testing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Shawn
 */
public class Testing {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String date = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss").format(new Date());
        System.out.println(date);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowListen(frame));
        frame.setVisible(true);
    }
    
    private static class WindowListen extends WindowAdapter {

        private final JFrame frame;

        public WindowListen(JFrame frame) {
            this.frame = frame;
        }
        
        
        
        @Override
        public void windowClosed(WindowEvent e) {
            super.windowClosed(e); //To change body of generated methods, choose Tools | Templates.
            System.out.println("Window closed");
        }

        @Override
        public void windowClosing(WindowEvent e) {
            super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
            System.out.println("Window closing");
            String pin = JOptionPane.showInputDialog(null, "Enter pin");
            if(pin.equals("0000"))
                frame.dispose();
        }
        
        
        
    }
    
}

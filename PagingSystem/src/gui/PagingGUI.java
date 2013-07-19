/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import pagingsystem.PagingSystem;
import util.PageAndVoiceProperties;

/**
 *
 * @author Shawn
 */
public class PagingGUI extends JTabbedPane {
    /*
    public static void main(String[] args) {
       
        
        PagingGUI gui = null;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());;
        } catch (Exception ex) {}
        try {
            gui = new PagingGUI();
        } catch (IOException ex) {
            Logger.getLogger(PagingGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        gui.setVisible(true);
    }*/
    
    private PagingSystem ps;
    
    public PagingGUI(PageAndVoiceProperties props) throws IOException {
        super();
        Logger.getGlobal().log(Level.INFO, "-------Making Paging System with props!-------");
        ps = new PagingSystem(props);
        Logger.getGlobal().log(Level.INFO, "-------Paging System Created, starting init()-------");
        init();
        Logger.getGlobal().log(Level.INFO, "-------initialized with init()-------");
    }
    
    private void init() {
        this.addTab("Paging", ps.getPagingSystemPanel());
        this.addTab("Alerts", ps.getAlertMonitorPanel());
        this.addTab("Employees", ps.getEmployeePanel());
    }
    
    public PagingSystem getPagingSystem() {
        return ps;
    }
}

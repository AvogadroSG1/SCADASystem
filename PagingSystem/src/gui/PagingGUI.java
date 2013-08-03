/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import pagingsystem.PagingSystem;
import util.PageAndVoiceProperties;

/**
 *
 * @author Shawn
 */
public class PagingGUI extends JRootPane {
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
    private JTabbedPane tabbed;
    
    public PagingGUI(PageAndVoiceProperties props) throws IOException {
        super();
        Logger.getGlobal().log(Level.INFO, "-------Making Paging System with props!-------");
        ps = new PagingSystem(props);
        Logger.getGlobal().log(Level.INFO, "-------Paging System Created, starting init()-------");
        init();
        Logger.getGlobal().log(Level.INFO, "-------initialized with init()-------");
    }
    
    private void init() {
        tabbed = new JTabbedPane();
        
        tabbed.addTab("Paging", ps.getPagingSystemPanel());
        tabbed.addTab("Alerts", ps.getAlertMonitorPanel());
        tabbed.addTab("Employees", ps.getEmployeePanel());
        
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(tabbed, BorderLayout.CENTER);
        
        this.setGlassPane(new InactiveGlassPanel());
        
        setGlassVisible(true); //because the paging system will initially be off.
    }
    
    public PagingSystem getPagingSystem() {
        return ps;
    }
    
    
    public void addTab(String name, JComponent component) {
        tabbed.addTab(name, component);
    }
    
    public void setGlassVisible(boolean bool) {
        this.getGlassPane().setVisible(bool);
    }
    
    class InactiveGlassPanel extends JComponent {
        
        public InactiveGlassPanel() {
            super();
            
            this.addMouseListener(new MouseAdapter(){});
            this.addMouseMotionListener(new MouseAdapter(){});
        }

        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            
            Color tGray = new Color(25, 25, 25, 200); //r,g,b,alpha
            
            g.setColor(tGray);
            
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            
            g.setColor(Color.white);
            
            Font font = new Font("Arial", Font.BOLD, 16);
            g.setFont(font);
            
            String string =  "Paging Disabled";
            FontMetrics metrics = g.getFontMetrics();
            
            int stringWidth = metrics.stringWidth(string);
            int stringHeight = metrics.getHeight();
            
            int x = this.getWidth() / 2 - stringWidth / 2;
            int y = this.getHeight() / 2 - stringHeight / 2;
            
            g.drawString(string, x, y);
        }

        @Override
        public void setVisible(boolean aFlag) {
            super.setVisible(aFlag);
            requestFocus();
        }
        
    }
}

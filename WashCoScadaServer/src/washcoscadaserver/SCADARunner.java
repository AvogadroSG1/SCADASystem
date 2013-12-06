/*
 * Author: Peter O'Connor
 * Purpose: To implement SCADA Monitoring throughout Washington County
 * Version: 1.0a
 * 
 * Contact: avogadrosg1@gmail.com
 * 
 */
package washcoscadaserver;

import gui.ServerGUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class SCADARunner
{

    static final Logger log = Logger.getGlobal();
    
    static SCADAServer server;
    
    static ServerGUI gui;
    
    public static void main(String[] args) 
    {
        String[] verbose = {"v"};
        dispatch(verbose);
        
        server = new SCADAServer();
        
        gui = new ServerGUI(server);
        gui.setVisible(true);
    }
    
    static void dispatch(String[] args)
    {
        for(String s: args)
        {
            s = s.replaceAll("-", "");
            char command = s.charAt(0);
            
            switch (command)
            {
                case 'v':
                    log.setLevel(Level.ALL);
            try 
            {
                FileHandler fh = new FileHandler("log.xml");
                log.addHandler(fh);
                log.info("Hai");
            } catch (IOException ex) 
            {
                Logger.getGlobal().info(ex.toString());
            } catch (SecurityException ex) 
            {
                Logger.getGlobal().info(ex.toString());
            }
            }
        }
    }
    
    public static SCADAServer getServer() {
        return server;
    }
}

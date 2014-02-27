/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import SCADASite.SCADASite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import shutdown.Pin;
import shutdown.ShutdownSecurity;
import washcoscadaserver.SCADAServer;

/**
 *
 * @author Shawn
 */
public class ServerGUI extends JFrame {
    
    private final SCADAServer server;
    
    private JButton clearAllButton;
    private JLightSwitch checkAlarmSwitch, pageSwitch;
    private SCADAJTree tree;
    private SiteStatusList list;
    private NotificationSystemPanel notificationPanel;
    
    private Thread checking;
    
    private GridBagConstraints notifyConstr, mainConstr, treeConstr;
    
    public ServerGUI(SCADAServer server) {
        super("SCADA System");
        this.server = server;
        init();
    }
    
    private void init() {
        setupGridBags();
         
        tree = new SCADAJTree();
        list = new SiteStatusList();
        
        for(SCADASite site: server.getSCADASites()) {
            tree.addSite(site);
            list.addSite(site);
        }
        
        JScrollPane treeScroll = new JScrollPane(tree);
        
        notificationPanel = server.getPageServ().getNotificationSystem().getNotificationSystemPanel();
        
        checkAlarmSwitch = new JLightSwitch("Alarms");
        checkAlarmSwitch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAlarmsActionPerformed();
            }
        });
        
        pageSwitch = new JLightSwitch("Paging");
        pageSwitch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pageSwitchActionPerformed();
            }
        });
        
        clearAllButton = new JButton("Clear All Pages");
        clearAllButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                server.getPageServ().stopAllRunningPages();
            }
            
        });
        
        
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new GridLayout(1, 4));
        toolbar.add(checkAlarmSwitch);
        toolbar.add(pageSwitch);
        toolbar.add(server.getPageServ().getNotificationSystem().getPagingSystem().getPagingProgressPanel());
        toolbar.add(clearAllButton);
        
        JScrollPane scrollStatus = new JScrollPane(list);
        scrollStatus.setPreferredSize(new Dimension(500,700));
        scrollStatus.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollStatus.setAutoscrolls(true);
        
        JPanel temp = new JPanel(new GridBagLayout());
        
        temp.add(tree, treeConstr);
        temp.add(scrollStatus, mainConstr);
        temp.add(notificationPanel, notifyConstr);
        
        this.setLayout(new BorderLayout());
        
        this.add(toolbar, BorderLayout.NORTH);
        this.add(temp, BorderLayout.CENTER);
        
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new EnterPinWindowAdapter());
        this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
    }
    
    private void checkAlarmsActionPerformed() {
        
        if(!server.isChecking()) {
            server.startChecking();
        } else {
            server.stopChecking();
        }
    }
    
    private void pageSwitchActionPerformed() {
        server.switchPaging();
        
        if(server.getPageServ().isActive()) {
            notificationPanel.setGlassVisible(false);
        } else {
            notificationPanel.setGlassVisible(true);
        }
    }
    
    public boolean isChecking() {
        return checking != null;
    }
    
    private void setupGridBags() {
        notifyConstr = new GridBagConstraints();
        mainConstr = new GridBagConstraints();
        treeConstr = new GridBagConstraints();
        
        treeConstr.gridx = 0;
        treeConstr.gridy = 0;
        treeConstr.gridwidth = 1;
        treeConstr.gridheight = 3;
        treeConstr.fill = GridBagConstraints.BOTH;
        treeConstr.weightx = 0.25;
        treeConstr.weighty = 1;
        
        mainConstr.gridx = 1;
        mainConstr.gridy = 0;
        mainConstr.gridwidth = 3;
        mainConstr.gridheight = 2;
        mainConstr.fill = GridBagConstraints.BOTH;
        mainConstr.weightx = 1;
        mainConstr.weighty = 1;
        
        
        notifyConstr.gridx = 1;
        notifyConstr.gridy = 2;
        notifyConstr.gridwidth = 3;
        notifyConstr.gridheight = 1;
        notifyConstr.fill = GridBagConstraints.BOTH;
        notifyConstr.weightx = 1;
        notifyConstr.weighty = 0.5;
        
    }
    
    private class EnterPinWindowAdapter extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            super.windowClosing(e);
            try {
                String inputPin = JOptionPane.showInputDialog(ServerGUI.this, "Enter pin for shutdown").trim();
                
                Pin[] pins = ShutdownSecurity.getPins();
                if(pins.length == 0) // there are no pins, thus no security. just close
                    System.exit(1);
                
                for(Pin pin: pins) {
                    if(pin.getPin().equals(inputPin)) {
                        String date = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss").format(new Date());
                        Logger.getGlobal().log(Level.SEVERE, pin.getPersonName() + " closed the program on " + date);
                        System.exit(2);
                    }
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(ServerGUI.this, "Error while opening pin file\n" + ex.getMessage());
            }
        }
        
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import SCADASite.SCADASite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import pagingsystem.PagingSystem;
import washcoscadaserver.SCADARunner;
import washcoscadaserver.SCADAServer;

/**
 *
 * @author Shawn
 */
public class ServerGUI extends JFrame {
    
    private final SCADAServer server;
    private final PagingSystem ps;
    
    private JButton clearAllButton;
    private JLightSwitch checkAlarmSwitch, pageSwitch;
    private SCADAJTree tree;
    
    private Thread checking;
    
    public ServerGUI(PagingSystem ps) {
        super("SCADA System");
        server = SCADARunner.getServer();
        this.ps = ps;
        init();
    }
    
    private void init() {
        tree = new SCADAJTree();
        
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
                server.clearAllPages();
            }
            
        });
        
        
        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new GridLayout(1, 4));
        toolbar.add(checkAlarmSwitch);
        toolbar.add(pageSwitch);
        toolbar.add(ps.getPagingProgressPanel());
        toolbar.add(clearAllButton);
        
        JPanel temp = new JPanel(new BorderLayout());
        
        JScrollPane scrollStatus = new JScrollPane(SCADARunner.getLogArea());
        scrollStatus.setPreferredSize(new Dimension(500,700));
        scrollStatus.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollStatus.setAutoscrolls(true);
        
        temp.add(scrollStatus);
        temp.add(server.getPageServ().getPagingGUI(), BorderLayout.SOUTH);
        
        this.setLayout(new BorderLayout());
        
        this.add(toolbar, BorderLayout.NORTH);
        this.add(temp, BorderLayout.CENTER);
        this.add(tree, BorderLayout.WEST);
    }
    
    private void checkAlarmsActionPerformed() {
        
        if(checking == null)
            {
                checking = (Thread) new CheckAlarmTask();
                checking.start();
            }
            else
            {
                checking = null;
                server.pagingOff();
            }
    }
    
    private final class CheckAlarmTask extends Thread
    {
        @Override
        public void run() 
        {
            boolean checking = true;
            while(checking)
            {
            server.checkForAlarms(); 
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                    Logger.getLogger(SCADAServer.class.getName()).log(Level.SEVERE, null, ex);
                    // when this thread is interrupted it should be closed.
                    checking = false;
            }
            }
        }
    }
    
    private void pageSwitchActionPerformed() {
        server.switchPaging();
    }
    
    public void updateTree(ArrayList<SCADASite> sites) {
        tree.setSCADASites(sites);
    }
    
    public boolean isChecking() {
        return checking != null;
    }
}

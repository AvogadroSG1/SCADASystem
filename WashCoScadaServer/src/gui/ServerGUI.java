/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import SCADASite.SCADASite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
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
    
    private PagingGUI pageGUI;
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
        
        for(SCADASite site: server.getSCADASites()) {
            tree.addSite(site);
        }
        
        JScrollPane treeScroll = new JScrollPane(tree);
        
        
        pageGUI = server.getPageServ().getPagingGUI();
        
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
        temp.add(pageGUI, BorderLayout.SOUTH);
        
        this.setLayout(new BorderLayout());
        
        this.add(toolbar, BorderLayout.NORTH);
        this.add(temp, BorderLayout.CENTER);
        this.add(treeScroll, BorderLayout.WEST);
        
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
            pageGUI.setGlassVisible(false);
        } else {
            pageGUI.setGlassVisible(true);
        }
    }
    
    public boolean isChecking() {
        return checking != null;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import SCADASite.SCADASite;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private SiteStatusList list;
    
    private Thread checking;
    
    private GridBagConstraints pageConstr, mainConstr, treeConstr;
    
    public ServerGUI(PagingSystem ps) {
        super("SCADA System");
        server = SCADARunner.getServer();
        this.ps = ps;
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
        
        JScrollPane scrollStatus = new JScrollPane(list);
        scrollStatus.setPreferredSize(new Dimension(500,700));
        scrollStatus.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollStatus.setAutoscrolls(true);
        
        JPanel temp = new JPanel(new GridBagLayout());
        
        temp.add(tree, treeConstr);
        temp.add(scrollStatus, mainConstr);
        temp.add(pageGUI, pageConstr);
        
        this.setLayout(new BorderLayout());
        
        this.add(toolbar, BorderLayout.NORTH);
        this.add(temp, BorderLayout.CENTER);
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
    
    private void setupGridBags() {
        pageConstr = new GridBagConstraints();
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
        
        
        pageConstr.gridx = 1;
        pageConstr.gridy = 2;
        pageConstr.gridwidth = 3;
        pageConstr.gridheight = 1;
        pageConstr.fill = GridBagConstraints.BOTH;
        pageConstr.weightx = 1;
        pageConstr.weighty = 0.5;
        
    }
}

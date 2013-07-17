/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package washcoscadaserver;

import gui.PagingGUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class ControlPanel extends JPanel
{
    private SCADAServer server;
    private JButton pageButton, modemButton, clearAllButton, startServButton;
    protected PagingGUI pagePanel;
    private JFrame scadaFrame;
    private Thread checking;
    private JFrame pageFrame;
    
    public ControlPanel(SCADAServer aServ, JFrame aFrame)
    {
        super();
        server = aServ;
        scadaFrame = aFrame;
        setPreferredSize(new Dimension(200,700));
        setBackground(Color.black);
        makeButtons();
    }
    
    public void makeButtons()
    {
        ActionListener al = new ControlListener();
        pageButton = new JButton("Turn Paging On/Off");
        pageButton.setForeground(Color.red);
        clearAllButton = new JButton("Clear all Notifications");
        startServButton = new JButton("Start/Stop SCADA Server");
        startServButton.setForeground(Color.red);
        
        clearAllButton.addActionListener(al);
        pageButton.addActionListener(al);
        startServButton.addActionListener(al);
        
        //pageButton.setForeground(Color.green.darker());
        
        this.add(startServButton);
        this.add(pageButton);
        this.add(clearAllButton);
    }
    
    public boolean isChecking()
    {
        return checking != null;
    }
    
    private class ControlListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) 
        {
            boolean result= false;
            if(e.getSource() == pageButton)
            {
                //if(checking != null)
                //{
                result = server.switchPaging();

                if (result)
                {
                    //BorderLayout bl = (BorderLayout) scadaFrame.getLayout();
                    //bl.getLayoutComponent(BorderLayout.SOUTH);
                    //scadaFrame.remove(((BorderLayout)scadaFrame.getLayout()).getLayoutComponent(BorderLayout.SOUTH));
                    pageFrame = new JFrame("PagingSystem");
                    //pageFrame.setSize(new Dimension(700,300));
                    pageFrame.setSize(700, 300);
                    //scadaFrame.remove(SCADARunner.pagingHolder);
                    server.pageServ.getPagingGUI().setPreferredSize(new Dimension(700,250));
                    pageFrame.add(server.pageServ.getPagingGUI(), BorderLayout.SOUTH);
                    pageFrame.setVisible(true);
                    //scadaFrame.revalidate();
                    pageButton.setForeground(Color.green.darker());
                }
                else
                {   
                    JPanel pagingHolder = new JPanel();
                    pagingHolder.setPreferredSize(new Dimension(700, 250));
                    JLabel labelTemp = new JLabel("Paging System Inactive.");
                    pagingHolder.add(labelTemp);
                    pageButton.setForeground(Color.red);
                }
           }
                //else
                  //  JOptionPane.showMessageDialog(null, "Paging not started, server is not active.");
            //}
            else if (e.getSource() == startServButton)
            {
                if(checking == null)
                {
                    checking = (Thread) new CheckAlarmTask();
                    checking.start();
                    startServButton.setForeground(Color.green.darker());
                }
                else
                {
                    checking = null;
                    startServButton.setForeground(Color.red);
                    server.pagingOff();
                    pageButton.setForeground(Color.red);
                }
            }
            else if (e.getSource() == clearAllButton)
            {
                server.clearAllPages();
            }
        }
        
    }
 
    private final class CheckAlarmTask extends Thread
    {
        @Override
        public void run() 
        {
            while(true)
            {
            server.checkForAlarms(); 
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SCADAServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
        }
    }
}

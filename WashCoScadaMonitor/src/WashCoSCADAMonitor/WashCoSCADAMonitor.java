/*
 * Author: Peter O'Connor
 * Purpose: To implement SCADA Monitoring throughout Washington County
 * Version: 1.0a
 * 
 * Contact: avogadrosg1@gmail.com
 * 
 */
package WashCoSCADAMonitor;

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import SCADASite.*;
import javax.sound.sampled.*;

public class WashCoSCADAMonitor extends JFrame implements WashCoSCADAConstants, Runnable
{
    public static void main(String[] args)
    {
        WashCoSCADAMonitor frame = new WashCoSCADAMonitor();
        frame.start();
        frame.setVisible(true);
    }
    
    private JPanel controls;
    private JButton monitorButton, user;
    private MapPanel mp;
    private SitePanel sp;
    private InfoPanel infop;
    private Thread monitor = null;
    private ArrayList<SCADASite> sites;
    private ArrayList<SitePoint> points;
    private boolean newDataIncomming = true;
    private boolean initSites = false;
    private boolean initStream = true;
    private int numSites = 0;
    private boolean monitoring = false;
    private Socket scadaConnection = null;
    private ObjectInputStream in = null;
    private ObjectOutputStream out = null;
    private boolean gotSitesOnce = false;
    private Scanner fileIn = null;
    private int atSite;
    private SCADASite siteToMon = null;
    private int siteToMonitor = -1;
    
    public WashCoSCADAMonitor()
    {
        sites = new ArrayList<SCADASite>();
        points = new ArrayList<SitePoint>();
        
        setSize(new Dimension(FRAME_WIDTH,FRAME_HEIGHT));
        this.setLayout(new BorderLayout());
        setTitle("Washington County SCADA System");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        
        monitor = new Thread(this);
        
        //this.makeControlPanel();
        mp = new MapPanel();     
        sp = new SitePanel();
        infop = new InfoPanel();  
        
        this.addMouseListener(new clickListener());
        this.add(mp, BorderLayout.CENTER);
        //this.add(controls, BorderLayout.EAST);
        this.add(sp, BorderLayout.SOUTH);
        this.add(infop, BorderLayout.EAST);
        monitor = new Thread(WashCoSCADAMonitor.this);
    }
    
    private void makeControlPanel()
    {
        monitorButton = new JButton("Monitor");
        monitorButton.addActionListener(new StartListener());
        user = new JButton("Admin*");
        monitorButton.setBackground(Color.GREEN);
        controls = new JPanel();
        controls.setPreferredSize(new Dimension(100, 600));
        controls.setLayout(new GridLayout(2,1));
        controls.add(monitorButton);
        controls.add(user);
    }
    
    public void start()
    {
        monitor.start();
    }
    
    @Override
    public void run()
    {
        
        if(initStream)
        {
            atSite = 0;
            
            File serverInfo = new File("server.ini");

            try
            {
            fileIn = new Scanner(serverInfo);
            } catch(FileNotFoundException ex)
            {
                JOptionPane.showMessageDialog(null, "Configuration File: server.ini not found.");
            }

            try 
            {
                scadaConnection = new Socket(fileIn.nextLine().trim(), 10000);
                out = new ObjectOutputStream(scadaConnection.getOutputStream());
                in = new ObjectInputStream(scadaConnection.getInputStream());
                initStream = false;
                initSites = true;
            } catch (UnknownHostException e) 
            {
                JOptionPane.showMessageDialog(this, "Unknown Host.  Check ini file.  Contact your administrator is this persists.");
            } catch (IOException e) 
            {
                JOptionPane.showMessageDialog(this, "Had trouble connecting to server.  Contact your administrator is this persists.");
            }
            
        }
        
        while(initSites)
        {
            try 
            {
                try 
                {
                    Object temp = in.readObject(); 
                    if(temp instanceof String)
                    {
                        initSites= false;
                        monitoring = true;
                        if(!gotSitesOnce)
                        {
                        mp.setSitePoints(points);
                        gotSitesOnce = true;
                        }
                        sp.clearText();
                        sp.setText("Got all the sites!");
                    }
                    else
                    {
                        numSites++;
                        SCADASite tSite = (SCADASite) temp;
                        sites.add(tSite);
                        
                        Integer alarmInt = 0;
                        if(tSite.getAlarm())
                            alarmInt = 2;
                        else if(tSite.getWarning())
                            alarmInt = 1;
                        points.add(new SitePoint(tSite.getLon(), tSite.getLat(), alarmInt, tSite.getName()));
                    }
                } catch (ClassNotFoundException ex) 
                {
                    System.out.println("Error processing Sites.");
                }


            } catch (IOException ex) 
            {
                
            }
        }
        
        while(monitoring)
        {
            try 
            {

                try 
                {
                    Object temp = in.readObject(); 
                    
                    if(temp instanceof String)
                    {
                        atSite = 0;
                        //sp.clearText();
                        //sp.setText("Updated!");
                        System.out.println("Got data");
                    }
                    else if(temp instanceof SCADASite)
                    {
                        SCADASite tSite = (SCADASite) temp;
                        
                        Integer alarmInt = 0;
                        if(tSite.getAlarm())
                        {
                            points.get(atSite).setAlarm(2);
                        }
                        else if(tSite.getWarning())
                        {
                            points.get(atSite).setAlarm(1);
                        }
                        else
                            points.get(atSite).setAlarm(0);
                        
                        if(siteToMon != null && tSite.getName().equals(siteToMon.getName()))
                        {
                            sp.setText(tSite.getStatus());
                            //System.out.println("I setted text!");
                            sp.repaint();
                        }
                        
                        
                        mp.repaint();
                        
                        atSite++;
                    }
                } catch (ClassNotFoundException ex) 
                {
                    System.out.println("Error processing Sites.");
                }


            } catch (IOException ex) 
            {
                
            }
        }
        
        System.err.println("Thread ended");
    }
    


    private class StartListener implements ActionListener {

        public StartListener() {
        }

        @Override
        public void actionPerformed(ActionEvent ae) 
        {
            /*if(initSites)
            {
                monitor = new Thread(WashCoSCADAMonitor.this);
                monitor.start();
                monitorButton.setText("Stop");
                monitorButton.setBackground(Color.RED);
            }
            else if(!monitoring)
            {
                monitor = new Thread(WashCoSCADAMonitor.this);
                monitor.start();
                monitoring = true;
                monitorButton.setText("Stop");
                monitorButton.setBackground(Color.RED);
            }
            else
            {
                System.out.println("Stopped!");
                monitoring = false;
                monitorButton.setText("Start");
                monitorButton.setBackground(Color.GREEN);
            }
          mp.buttonClick(ae);*/
        }
    }
    
    private class clickListener implements MouseListener
    {

        @Override
        public void mouseClicked(MouseEvent me) 
        {
            int xClick = me.getX();
            int yClick = me.getY();
            
            for(int i = 0; i < sites.size(); i++)
            {
                SCADASite ss = sites.get(i);
                
                System.out.println("Site X: " + ss.getLon());
                System.out.println("Site Y: " + ss.getLat());
                System.out.println("Your X: " + xClick);
                System.out.println("Your Y: " + (yClick - WashCoSCADAConstants.FRAME_TITLE_OFFSET));
                
                if(Math.abs(xClick - ss.getLon()) < CLICK_DISTANCE && 
                        Math.abs(yClick - ss.getLat() - FRAME_TITLE_OFFSET) < CLICK_DISTANCE)
                {
                    
                    siteToMonitor = i;
                    siteToMon = ss;
                    sp.setText(ss.getStatus());
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent me) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void mouseEntered(MouseEvent me) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void mouseExited(MouseEvent me) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
  
    private class InitThread implements Runnable
    {

        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    private class GetSitesThread implements Runnable
    {

        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    private class MonitorThread implements Runnable
    {

        @Override
        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
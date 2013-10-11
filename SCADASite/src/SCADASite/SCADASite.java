/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SCADASite;

import java.io.Serializable;
import java.util.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.util.*;

/**
 *
 * @author Avogadro
 */
public class SCADASite implements Serializable, Comparable
{
    private String name, statusString, critInfo;
    private double lon, lat;
    private ArrayList<SCADAComponent> components = new ArrayList<SCADAComponent>();
    private final int DISCRETE_OFFSET = 10001;
    private final int REGISTER_OFFSET = 30001;
    private boolean connected = true;
    private long startdis; 
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private Date date;
    
    private ArrayList<Alert> alerts;
    //private ArrayList<Integer> alarms;
    //private ArrayList<Integer> oldalarms;
    private CheckThread checkThread = new CheckThread();
    
    private final int id;
    private boolean justDisconnected = false;
    private Status status = new Status();   // status is mainly used for telling the status of this scadasite. just changed doesn't really work here because of the discretes updating
    private Status oldStatus = new Status();
    
    
    public SCADASite(int aId, String aName, String aLat, String aLon, ArrayList<SCADAComponent> scs)
    {
        id = aId;
        name = aName;
        lon = Double.parseDouble(aLon);
        lat = Double.parseDouble(aLat);
        components = scs;
        statusString = "";
        startdis = -1;
        date = new Date();
        alerts = new ArrayList<Alert>();
    }
    
    //Returns the SCADAComponents
    public ArrayList<SCADAComponent> getComponents()
    {
        return components;
    }
    
    @Override
    public String toString()
    {
        String give = name + "\n" + lon + lat + "\n" + components;
        return give;
    }
    
    public String getName()
    {
        return name;
    }
    
    public double getLat()
    {
        return lat;
    }
    
    public double getLon()
    {
        return lon;
    }
    
    public int getID() {
        return id;
    }
    
    //Checking for alarms by going through all of the SCADAComponents
    public synchronized void checkAlarms()
    {
        oldStatus = status;
        status.setStatusCode(Status.NORMAL); // just incase the status is less than it was last time
        status.setJustChanged(false); //so that we can update it later
        
        critInfo = "";
        
        statusString = this.getName() + "\n";
        
        for(int siteid = 0; siteid < components.size(); siteid++)
            {
                SCADAComponent sc = components.get(siteid);
                if(sc.isModBus())
                {
                    statusString += sc.getName() + "\n" + dateFormat.format(date) + "\n";
                    
                    try 
                    {
                        ArrayList<Discrete> discretes = sc.getDiscretes();
                        ArrayList<Register> registers = sc.getRegisters();
                        
                        InetAddress astr = sc.getIP();
                        ModbusTCPMaster mbm = new ModbusTCPMaster(astr.getHostAddress(), 502);
                        mbm.connect();
                        
                        
                        for(int i = 0; i < discretes.size(); i++)
                        {
                            Discrete currentD = discretes.get(i);
                            int addy = currentD.getPort();
                            String dname = currentD.getName();

                            BitVector bv = mbm.readInputDiscretes(addy-DISCRETE_OFFSET, 1);
                            statusString += "\n"+ dname + " at Discrete: \t" + addy + ":\t";
                            
                            /* checks for critical alarm*/
                            if(bv.getBit(0) && currentD.getWarning() == 2)
                            {
                                statusString += "CRITICAL\n";

                                //changeStatus(critical);
                                //alarms.add(2);
                                critInfo = currentD.getName();
                                //critical = true;
                                Alert temp = new Alert(this, currentD, dateFormat.format(date));
                                if(!alerts.contains(temp))
                                {
                                    alerts.add(temp);
                                }
                                critInfo += currentD.getName() + "\n";
                                currentD.setStatus(Status.CRITICAL);
                                
                                if(!inQueue(currentD))
                                    alerts.add(new Alert(this, currentD, dateFormat.format(date)));
                            }
                            else if(bv.getBit(0) && currentD.getWarning() == 1)
                            {
                                statusString += "Warning\n";
                                //changeStatus(warning);
                                //warning = true;
                                //alarms.add(1);
                                critInfo = "";
                                Alert temp = new Alert(this, currentD, dateFormat.format(date));
                                if(!alerts.contains(temp))
                                {
                                    alerts.add(temp);
                                }

                                currentD.setStatus(Status.WARNING);
                                
                                if(!inQueue(currentD))
                                    alerts.add(new Alert(this, currentD, dateFormat.format(date)));
                            }
                            else if(bv.getBit(0) && currentD.getWarning() == 0)
                            {
                                statusString += "Not Normal\n";
                                //changeStatus(notNormal);
                                //notNormal = true;
                                //alarms.add(0);
                                critInfo = "";
                                Alert temp = new Alert(this, currentD, dateFormat.format(date));
                                if(!alerts.contains(temp))
                                {
                                    alerts.add(temp);
                                }

                                currentD.setStatus(Status.NOTNORMAL);
                                
                                if(!inQueue(currentD))
                                    alerts.add(new Alert(this, currentD, dateFormat.format(date)));
                            }
                            else
                            {
                                //normal = true;
                                //alarms.add(-1);
                                statusString += "Normal\n";
                                currentD.setStatus(Status.NORMAL);
                                if(inQueue(currentD)) {
                                    
                                    for(int j = 0; j < alerts.size(); j++) {
                                        
                                        if(alerts.get(j).getDiscrete() == currentD) {
                                            alerts.remove(j);
                                            j = alerts.size();
                                        }
                                        
                                    }
                                    
                                }
                            }
                            
                            if(currentD.getStatus().getStatusCode() > this.status.getStatusCode()) {
                                status.setStatusCode(currentD.getStatus().getStatusCode());
                            }
                            
                            if(alerts.size() == 0)
                                this.status.setStatusCode(Status.NORMAL);
                            
                        }
                        
                        //justChanged = assignJustChanged();
                        
                        for(int i = 0; i < registers.size(); i++)
                        {
                            int addy = registers.get(i).getPort();
                            String rname = registers.get(i).getName();
                            
                            InputRegister[] ir = mbm.readInputRegisters(addy-REGISTER_OFFSET, 1);
                            statusString += "\n" + rname + " at Register: \t" + addy + ":\t" + ir[0].getValue() + "\n";
                        }
                        
                        //Got through connections
                        date = new Date();
                        justDisconnected = false;
                        connected = true;
                        startdis = -1;
                        
                        mbm.disconnect();
                    }
                    catch(Exception e)
                    {
                        System.out.println("Disconnected");
                        statusString += "Disconnceted.\nLast connection on: " +
                                dateFormat.format(date);
                        
                        if(!justDisconnected) { //if just disconnected
                            justDisconnected = true;
                        } else {
                            justDisconnected = false;
                        }
                        
                        critInfo = this.getName() + " disconnceted.\nLast connection on: " +
                                dateFormat.format(date);
                        
                        connected = false;
                        siteid = components.size();
                    }
                }
            }
        
        /*
        if(alarms.contains(2))
            critical = true;
        else if (alarms.contains(1))
        {
            critical = false;
            warning = true;
        }
        else if (alarms.contains(0))
        {
            critical = false;
            warning = false;
            notNormal = true;
        }
        else
        {
            critical = false;
            warning = false;
            notNormal = false;
        }

        if (!oldalarms.contains(2) && critical)
        {
            newAlarm = true;
        }

        oldalarms = alarms;
=======
>>>>>>> SCADA-Framework-Update*/
    }
    
    private boolean inQueue(Discrete check)
    {
        for(Alert a : alerts)
            if(a.getDiscrete() == check)
                return true;
        
        return false;
    }
    
    public ArrayList<Alert> getAlerts()
    {
        return alerts;
    }
    
    public String getStatus()
    {
            return statusString;
    }
    
    public boolean isWarning()
    {
        return status.isWarning() || !connected;
    }
    
    public boolean isCritical()
    {
        return status.isCritical();
    }
    
    public boolean isNormal() {
        return status.isNormal();
    }
    
    public String getCritcialInfo()
    {
        return critInfo;
    }
    public boolean isNewAlarm()
    {
        if(didJustChange() && status.isCritical())
            return true;
            
        return false;
    }
    /*
    public void ackNewAlrm()
    {
        newAlarm = false;
    }*/
    
    public boolean connected()
    {
        return connected;
    }
    
    public boolean getConnected()
    {
        return connected;
    }
    
    public boolean didJustChange() {
        if(justDisconnected)
            return true;
        
        if(status.getStatusCode() != oldStatus.getStatusCode())
            return true;
        
        
        return false;
    }
    
    public boolean equals(SCADASite other)
    {
        System.out.println(other.getName() + "Compared!");
        return this.getID() == other.getID();
    }
    
    @Override
    public int compareTo(Object o) 
    {
        if(o instanceof SCADASite) {
            SCADASite ss = (SCADASite) o;

            return ss.getID() - this.getID();
        }else return -1;
    }
    
    private Stack<SCADAUpdateListener> listeners = new Stack();
    
    public void addSCADAUpdateListener(SCADAUpdateListener listener) {
        listeners.add(listener);
    }
    
    public void removeSCADAUpdateListener(SCADAUpdateListener listener) {
        listeners.remove(listener);
    }
/*    
    private void clearStatus() {
        notNormal = false;
        critical = false;
        warning = false;
        normal = false;
    }
    
    private void assignOlds() {
        oldNormal = normal;
        oldNotNormal = notNormal;
        oldWarning = warning;
        oldCritical = critical;
=======
    public void removeSCADAUpdateListener(SCADAUpdateListener listener) {
        listeners.remove(listener);
>>>>>>> SCADA-Framework-Update*/
    //}
    
    public void notifyAllSCADAListeners(SCADASite site) {
        for(SCADAUpdateListener listener: listeners) {
            listener.update(site);
        }
    }
    
    private class CheckThread extends Thread {
        
        @Override
        public void run() {
            while(true) {
                SCADASite.this.checkAlarms();
                if(didJustChange()) {
                    notifyAllSCADAListeners(SCADASite.this);
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SCADASite.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
    }
    
    public void startChecking() {
        checkThread.start();
    }
    
    public void stopChecking() {
        checkThread.interrupt();
    }
}

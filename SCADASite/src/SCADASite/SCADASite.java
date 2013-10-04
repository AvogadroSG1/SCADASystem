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
    
    
    private final int id;
    private boolean justDisconnected = false;
    private Status status = new Status();
    
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
        status.setStatusCode(Status.NORMAL); // just incase the status is less than it was last time
        status.setJustChanged(false); //so that we can update it later
        
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
                                critInfo = currentD.getName();
                                alerts.add(new Alert(this, currentD.getName(), dateFormat.format(date)));
                                currentD.setStatus(Status.CRITICAL);
                            }
                            else if(bv.getBit(0) && currentD.getWarning() == 1)
                            {
                                statusString += "Warning\n";
                                critInfo = "";
                                alerts.add(new Alert(this, currentD.getName(), dateFormat.format(date)));
                                currentD.setStatus(Status.WARNING);
                            }
                            else if(bv.getBit(0) && currentD.getWarning() == 0)
                            {
                                statusString += "Not Normal\n";
                                critInfo = "";
                                alerts.add(new Alert(this, currentD.getName(), dateFormat.format(date)));
                                currentD.setStatus(Status.NOTNORMAL);
                            }
                            else
                            {
                                statusString += "Normal\n";
                                critInfo = "";
                                currentD.setStatus(Status.NORMAL);
                            }
                            
                            if(currentD.getStatus().getStatusCode() > this.status.getStatusCode()) {
                                status.setStatusCode(currentD.getStatus().getStatusCode());
                            }
                            if(currentD.getStatus().didJustChange()) {
                                status.setJustChanged(true);
                            }
                            
                        }
                        
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
    }
    
    private boolean inQueue(Discrete check)
    {
        for(Alert a : alerts)
            if(a.equals(check))
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
    
    public boolean getWarning()
    {
        return status.isWarning();
    }
    
    public boolean getAlarm()
    {
        return status.isCritical() || !connected;
    }
    
    public String getCritcialInfo()
    {
        return critInfo;
    }
    public boolean isNewAlarm()
    {
        if(justDisconnected)
            return true;
        
        for(SCADAComponent comp: components) {
            for(Discrete discrete: comp.getDiscretes()) {
                if(discrete.getStatus().didJustChange() && discrete.getStatus().isCritical())
                    return true; // if any of the discretes just changed and is a critical, then there is a new alarm
            }
        }
        
        return false;
    }
    
    public boolean connected()
    {
        return connected;
    }
    
    public boolean getConnected()
    {
        return connected;
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
}

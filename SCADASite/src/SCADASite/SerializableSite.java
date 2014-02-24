/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package SCADASite;

import java.io.Serializable;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.wimpi.modbus.facade.ModbusTCPMaster;
import net.wimpi.modbus.procimg.InputRegister;
import net.wimpi.modbus.util.BitVector;

/**
 *
 * @author Shawn
 */
public class SerializableSite implements Serializable, Comparable {
    
    private final int id;
    private final String name, statusString;
    private final double lon, lat;
    private final ArrayList<SCADAComponent> components;
    private boolean connected = true;
    private final ArrayList<Alert> alerts;
    
    private final Status status;  
    private boolean justDisconnected = false;
    
    
    public SerializableSite(int aId, String aName, String aLat, String aLon, ArrayList<SCADAComponent> scs, Status status, ArrayList<Alert> alerts)
    {
        super();
        id = aId;
        name = aName;
        lon = Double.parseDouble(aLon);
        lat = Double.parseDouble(aLat);
        components = scs;
        statusString = "";
        this.alerts = alerts;
        this.status = status;
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
    
    public boolean isNewAlarm()
    {
        if(didJustChange() && status.isCritical())
            return true;
            
        return false;
    }
    
    public boolean getConnected()
    {
        return connected;
    }
    
    public boolean didJustChange() {
        if(justDisconnected)
            return true;
        
        return status.didJustChange();
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

    public void setJustDisconnected(boolean justDisconnected) {
        this.justDisconnected = justDisconnected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
    
    
    
    
}

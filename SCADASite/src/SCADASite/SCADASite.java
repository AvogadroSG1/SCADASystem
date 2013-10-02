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
    private Boolean critical = false, warning = false, notNormal = false, normal = false;
    private boolean connected = true, newAlarm = false;
    private long startdis; 
    private DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private Date date;
    private ArrayList<Alert> alerts;
    
    private final int id;
    private boolean justChanged;
    private boolean oldCritical, oldWarning, oldNotNormal, oldNormal;
    
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
        statusString = this.getName() + "\n";
        assignOlds();
        for(int siteid = 0; siteid < components.size(); siteid++)
            {
                SCADAComponent sc = components.get(siteid);
                if(sc.isModBus())
                {
                    if(connected)
                        statusString += sc.getName() + "\n" + dateFormat.format(date) + "\n";
                    else
                        statusString += "Disconnceted.\nLast connection on: " +
                                dateFormat.format(date);
                    
                    
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
                            
                            //System.out.println(addy);
                            BitVector bv = mbm.readInputDiscretes(addy-DISCRETE_OFFSET, 1);
                            statusString += "\n"+ dname + " at Discrete: \t" + addy + ":\t";
                            if(bv.getBit(0) && currentD.getWarning() == 2)
                            {
                                if(critical) //checks to see if critical is already on
                                    newAlarm = false;
                                else
                                    newAlarm = true; 
                                statusString += "CRITICAL\n";
                                changeStatus(critical);
                                critInfo = currentD.getName();
                                alerts.add(new Alert(this, currentD.getName(), dateFormat.format(date)));
                                
                            }
                            else if(bv.getBit(0) && currentD.getWarning() == 1)
                            {
                                statusString += "Warning\n";
                                changeStatus(warning);
                                critInfo = "";
                                alerts.add(new Alert(this, currentD.getName(), dateFormat.format(date)));
                            }
                            else if(bv.getBit(0) && currentD.getWarning() == 0)
                            {
                                statusString += "Not Normal\n";
                                changeStatus(notNormal);
                                critInfo = "";
                                alerts.add(new Alert(this, currentD.getName(), dateFormat.format(date)));
                            }
                            else
                            {
                                statusString += "Normal\n";
                                changeStatus(normal);
                                critInfo = "";
                            }
                            
                        }
                        
                        justChanged = assignJustChanged();
                        
                        for(int i = 0; i < registers.size(); i++)
                        {
                            int addy = registers.get(i).getPort();
                            String rname = registers.get(i).getName();
                            
                            InputRegister[] ir = mbm.readInputRegisters(addy-REGISTER_OFFSET, 1);
                            statusString += "\n" + rname + " at Register: \t" + addy + ":\t" + ir[0].getValue() + "\n";
                        }
                        
                        //Got through connections
                        date = new Date();
                        connected = true;
                        startdis = -1;
                        
                        mbm.disconnect();
                    }
                    catch(Exception e)
                    {
                        System.out.println("Disconnected");
                        statusString += "Disconnceted.\nLast connection on: " +
                                dateFormat.format(date);
                        warning = true;
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
        return warning;
    }
    
    public boolean getAlarm()
    {
        return critical;
    }
    
    public String getCritcialInfo()
    {
        return critInfo;
    }
    public boolean isNewAlarm()
    {
        return newAlarm && critical;
    }
    
    public boolean connected()
    {
        return connected;
    }
    
    public boolean getConnected()
    {
        return connected;
    }
    
    public boolean didJustChange() {
        return justChanged;
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
    
    /*
     * This status sets all the statuses to false, then the boolean passed by reference becomes true
     */
    private void changeStatus(Boolean status) {
        notNormal = false;
        critical = false;
        warning = false;
        normal = false;
        
        status = true;
    }
    
    private void assignOlds() {
        oldNormal = normal;
        oldNotNormal = notNormal;
        oldWarning = warning;
        oldCritical = critical;
    }
    
    private boolean assignJustChanged() {
        if(critical.booleanValue() != oldCritical)
            return true;
        if(warning.booleanValue() != oldWarning)
            return true;
        if(notNormal.booleanValue() != oldNotNormal)
            return true;
        if(oldNormal != normal.booleanValue())
            return true;
        
        return false;
            
    }
}

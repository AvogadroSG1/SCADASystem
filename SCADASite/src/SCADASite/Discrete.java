/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SCADASite;

import java.io.Serializable;

/**
 *
 * @author Avogadro
 */
public class Discrete implements Serializable
{
    final String name;
    final int port;
    final int warningType;
    
    private Status status = new Status();
    
    public Discrete(String aName, int aPort, int aWarning)
    {
        name = aName;
        port = aPort;
        warningType = aWarning;
    }
    
    public int getPort()
    {
        return port;
    }
    
    public String getName()
    {
        return name;
    }
    
    public int getWarning()
    {
        return warningType;
    }
    
    public void setStatus(int statusCode) {
        status.setStatusCode(statusCode);
    }
    
    public Status getStatus() {
        return status;
    }
    
}

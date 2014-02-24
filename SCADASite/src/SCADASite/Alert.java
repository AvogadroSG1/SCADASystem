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
public class Alert implements Serializable
{
    private final int siteID;
    private final String siteName;
    private final Discrete discrete;
    private final String time;
    
    
    public Alert(SCADASite aSS,Discrete aDiscrete, String aTime)
    {
        this.siteID = aSS.getID();
        this.siteName = aSS.getName();
        this.discrete = aDiscrete;
        this.time = aTime;
    }
    
    public int getSS()
    {
        return siteID;
    }
    
    public String getSiteName()
    {
        return siteName;
    }
    
    public Discrete getDiscrete()
    {
        return discrete;
    }
    
    public String getTime()
    {
        return time;
    }
    
        public boolean equals(Alert other)
    {
        return other.discrete.getName().equals(this.discrete.getName());
    }

}

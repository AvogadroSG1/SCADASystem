/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package SCADASite;

/**
 *
 * @author Avogadro
 */
public class Alert
{
    private final SCADASite ss;
    private final String site;
    private final Discrete discrete;
    private final String time;
    
    
    public Alert(SCADASite aSS,Discrete aDiscrete, String aTime)
    {
        this.ss = aSS;
        this.site = ss.getName();
        this.discrete = aDiscrete;
        this.time = aTime;
    }
    
    public SCADASite getSS()
    {
        return ss;
    }
    
    public String getSiteName()
    {
        return site;
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

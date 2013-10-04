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
    private final String name;
    private final String time;
    
    public Alert(SCADASite aSS,String aName, String aTime)
    {
        this.ss = aSS;
        this.site = ss.getName();
        this.name = aName;
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
    
    public String getDiscreteName()
    {
        return name;
    }
    
    public String getTime()
    {
        return time;
    }
    
        public boolean equals(Alert other)
    {
        return other.name.equals(this.name);
    }
}
